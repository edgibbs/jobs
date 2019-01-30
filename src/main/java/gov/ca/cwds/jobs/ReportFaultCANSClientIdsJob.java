package gov.ca.cwds.jobs;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.data.persistence.cms.CmsKeyIdGenerator;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.flight.FlightPlan;

/**
 * Validates CANS Client IDs against legacy DB2 CLIENT_T table and reports invalid ones.
 *
 * @author CWDS API Team
 */
@SuppressWarnings({"findsecbugs:PATH_TRAVERSAL_IN", "findsecbugs:PATH_TRAVERSAL_OUT",
    "fb-contrib:CLI_CONSTANT_LIST_INDEX"})
public class ReportFaultCANSClientIdsJob {

  private static final String CMS_KEY = "cmsKey";
  private static final String PARAM_EXTERNAL_ID = "externalId";
  private static final String PARAM_ID = "id";
  private static final String PARAM_NEW_EXTERNAL_ID = "newExternalId";
  private static final String PARAM_NEW_PERSON_ID = "newPersonId";
  private static final String PARAM_PERSON_ID = "personId";
  private static final Logger LOGGER = LoggerFactory.getLogger(ReportFaultCANSClientIdsJob.class);
  private static final String HIBERNATE_CONFIG_CMS = "jobs-cms-hibernate.cfg.xml";
  private static final String HIBERNATE_CONFIG_NS = "jobs-ns-hibernate.cfg.xml";
  private static final String CLIENT_NOT_FOUND_IN_CMS = "Client Not found in CMS.";
  private static final String SUCCESS = "SUCCESS";
  private static final String FAILED = "FAILED";
  private static final String SESSION_INFO = "Can't get current session. Opening new one.";
  private static final String NQ_CANS_CLIENTS_ALL =
      "SELECT p.id, p.external_id, p.first_name, p.middle_name, p.last_name, p.suffix, p.dob, p.gender"
          + ", c.name AS county_name, a.status AS cans_status, a.event_date AS event_date"
          + ", u.external_id AS user_id, u.first_name AS u_first_name, u.last_name AS u_last_name "
          + " FROM {h-schema}person p LEFT JOIN {h-schema}county c ON p.county_id = c.id"
          + " LEFT JOIN ("
          + "     SELECT DISTINCT ON (person_id) person_id, status, event_date, created_by, updated_by "
          + "     FROM {h-schema}assessment ORDER BY person_id asc, "
          + "             event_date desc, " // Most recent
          + "             status desc" // IN-PROCESS is more important then COMPLETED, DELETED
          + " ) a ON p.id = a.person_id LEFT JOIN {h-schema}person u"
          + "     ON (CASE WHEN a.updated_by IS NOT NULL THEN a.updated_by ELSE a.created_by END) = u.id"
          + " WHERE p.person_role = 'CLIENT'"
          + " ORDER BY user_id, county_name, last_name, first_name FOR READ ONLY";
  private static final String NQ_CMS_CLIENT_FIND =
      "SELECT IDENTIFIER FROM {h-schema}CLIENT_T WHERE IDENTIFIER = :cmsKey FOR READ ONLY WITH UR";
  private static final String NQ_CANS_CLIENT_FIND_BY_EXTERNAL_ID =
      "SELECT id FROM {h-schema}person WHERE external_id = :externalId FOR READ ONLY";
  private static final String NQ_CMS_MERGE_CLIENT_FIND =
      "SELECT KEPTOBJ_ID FROM {h-schema}MRHIST_T WHERE DELOBJ_ID = :cmsKey";
  private static final String NQ_CANS_CLIENT_EXTERNAL_ID_UPDATE = "UPDATE {h-schema}person"
      + " SET external_id = :newExternalId WHERE id = :id AND external_id = :externalId";
  private static final String NQ_CANS_ASSESSMENTS_UPDATE_PERSON_ID =
      "UPDATE {h-schema}assessment" + " SET person_id = :newPersonId WHERE person_id = :personId";
  private static final String NQ_CANS_ASSESSMENTS_AUD_UPDATE_PERSON_ID =
      "UPDATE {h-schema}assessment_aud"
          + " SET person_id = :newPersonId WHERE person_id = :personId";
  private static final String NQ_CANS_CLIENT_DELETE = "DELETE FROM {h-schema}person WHERE id = :id";

  private SessionFactory cansSessionFactory;
  private SessionFactory cmsSessionFactory;
  private String baseDir;
  private List<CansClient> clientList;
  private String reportFileName;

  // Excel related
  private Workbook workbook;
  private Sheet sheet;
  private CellStyle detailsRowStyle;
  private CellStyle detailsRowFixedStyle;
  private CellStyle dateCellStyle;
  private CellStyle dateCellFixedStyle;
  private Field[] columns = CansClient.class.getDeclaredFields();
  private int nextRowNum = 1; // headerRow is 0
  private int[] columnsWidth = new int[columns.length];


  protected ReportFaultCANSClientIdsJob(SessionFactory cansSessionFactory,
      SessionFactory cmsSessionFactory) {
    this.cansSessionFactory = cansSessionFactory;
    this.cmsSessionFactory = cmsSessionFactory;
  }

  protected ReportFaultCANSClientIdsJob() {
    cansSessionFactory = new Configuration().configure(HIBERNATE_CONFIG_NS).buildSessionFactory();
    cmsSessionFactory = new Configuration().configure(HIBERNATE_CONFIG_CMS).buildSessionFactory();
  }

  /**
   * Job entry point.
   *
   * @param args command line arguments... ignored
   */
  public static void main(String... args) {
    LOGGER.info("Validating CANS Client IDs...");
    try {
      LaunchCommand.setSysPropsFromEnvVars();
      final ReportFaultCANSClientIdsJob job = new ReportFaultCANSClientIdsJob();

      job.baseDir = Optional.ofNullable(FlightPlan.parseCommandLine(args).getBaseDirectory())
          .map(dir -> Paths.get(dir).toAbsolutePath().toString())
          .orElseGet(new File(".")::getAbsolutePath);

      job.buildClientList();
      job.generateReport();

      LOGGER.info("DONE validating client ids. Report is in the file:\n {}", job.reportFileName);
    } catch (Exception e) {
      LOGGER.error("\n\nEXECUTION FAILED !!!\n {}\n\n", e.getMessage(), e);

      System.exit(-1);
    }

    System.exit(0);
  }

  protected static <T> List<T> mapNQResults(List<Object[]> objectArrayList, Class<T> genericType) {
    List<T> ret = new ArrayList<>();
    List<Field> mappingFields = getNQResultColumnAnnotatedFields(genericType);
    try {
      for (Object[] objectArr : objectArrayList) {
        T t = genericType.newInstance();
        for (int index = 0; index < objectArr.length; index++) {
          setObjectProperty(t, mappingFields.get(index), objectArr[index]);
        }
        ret.add(t);
      }
    } catch (InstantiationException ie) {
      LOGGER.debug("Cannot instantiate: ", ie);
      ret.clear();
    } catch (IllegalAccessException iae) {
      LOGGER.debug("Illegal access: ", iae);
      ret.clear();
    }
    return ret;
  }

  // Get ordered list of fields
  protected static <T> List<Field> getNQResultColumnAnnotatedFields(Class<T> genericType) {
    Field[] fields = genericType.getDeclaredFields();
    List<Field> orderedFields = Arrays.asList(new Field[fields.length]);
    for (Field field : fields) {
      if (field.isAnnotationPresent(NQResultColumn.class)) {
        NQResultColumn nqrc = field.getAnnotation(NQResultColumn.class);
        orderedFields.set(nqrc.index(), field);
      }
    }
    return orderedFields;
  }

  protected static void setObjectProperty(Object object, Field field, Object fieldValue) {
    try {
      if (fieldValue != null && field.getType() != fieldValue.getClass()) {
        if (field.getType() == Long.class && fieldValue instanceof BigInteger) {
          field.set(object, ((BigInteger) fieldValue).longValue());
        }
      } else {
        field.set(object, fieldValue);
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  protected Session grabCansSession() {
    try {
      return this.cansSessionFactory.getCurrentSession();
    } catch (HibernateException e) {
      LOGGER.info(SESSION_INFO, e);
      return this.cansSessionFactory.openSession();
    }
  }

  protected Session grabCmsSession() {
    try {
      return cmsSessionFactory.getCurrentSession();
    } catch (HibernateException e) {
      LOGGER.info(SESSION_INFO, e);
      return cmsSessionFactory.openSession();
    }
  }

  @SuppressWarnings("unchecked")
  protected void buildClientList() {
    Session session = grabCansSession();
    Transaction txn = session.beginTransaction();
    try {
      clientList = mapNQResults(
          session.createNativeQuery(NQ_CANS_CLIENTS_ALL).setReadOnly(true).getResultList(),
          CansClient.class);
    } catch (Exception e) {
      txn.rollback();
      LOGGER.error("Error while working with CANS database:\n {}", e.getMessage(), e);
      throw e;
    } finally {
      txn.rollback();
    }
  }

  protected void generateReport() {
    buildReportFileName();
    try {
      initReport();
      for (CansClient clientDto : clientList) {
        if (!isValidClientId(clientDto)) {
          attemptToFix(clientDto);
          reportClient(clientDto);
        }
      }
    } catch (Exception e) {
      LOGGER.error("Error while validating CANS clients against CWS/CMS database:\n {}",
          e.getMessage(), e);
      throw e;
    } finally {
      finalizeReport();
    }
  }

  protected void initReport() {
    // Create a Workbook
    workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

    /*
     * CreationHelper helps us create instances of various things like DataFormat, Hyperlink,
     * RichTextString etc, in a format (HSSF, XSSF) independent way
     */
    CreationHelper createHelper = workbook.getCreationHelper();

    // Create a Sheet
    sheet = workbook.createSheet("CANS Clients");

    // Create a Font for styling header cells
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 14);
    headerFont.setColor(IndexedColors.BLUE.getIndex());

    // Create a CellStyle with the font
    CellStyle headerCellStyle = workbook.createCellStyle();
    headerCellStyle.setFont(headerFont);

    // Create a Row
    Row headerRow = sheet.createRow(0);

    // Create cells
    for (int index = 0; index < columns.length; index++) {
      Cell cell = headerRow.createCell(index);
      String cellValue = columns[index].getName();
      cell.setCellValue(cellValue);
      columnsWidth[index] = max(columnsWidth[index], cellValue.length() + cellValue.length() / 2);
      cell.setCellStyle(headerCellStyle);
    }

    // Create data Format for Dates cells
    Short dateCellDataFormat = createHelper.createDataFormat().getFormat("MM/dd/yyyy");
    // Create Cell Style for formatting Date
    dateCellStyle = workbook.createCellStyle();
    dateCellStyle.setDataFormat(dateCellDataFormat);
    dateCellFixedStyle = workbook.createCellStyle();
    dateCellFixedStyle.setDataFormat(dateCellDataFormat);

    // Create a Row Style for styling details rows with a font color
    Font detailsFont = workbook.createFont();
    detailsFont.setColor(IndexedColors.RED.getIndex());
    detailsRowStyle = workbook.createCellStyle();
    detailsRowStyle.setFont(detailsFont);
    dateCellStyle.setFont(detailsFont);
    Font detailsFontFixed = workbook.createFont();
    detailsFontFixed.setColor(IndexedColors.GREEN.getIndex());
    detailsRowFixedStyle = workbook.createCellStyle();
    detailsRowFixedStyle.setFont(detailsFontFixed);
    dateCellFixedStyle.setFont(detailsFontFixed);
  }

  protected void finalizeReport() {
    // Resize all columns to fit the content size
    for (int i = 0; i < columns.length; i++) {
      sheet.setColumnWidth(i, min(columnsWidth[i] * 256, 255 * 256));
    }
    try (FileOutputStream fileOut = new FileOutputStream(reportFileName)) {
      // Write the output to a file
      workbook.write(fileOut);
      fileOut.close();
      // Closing the workbook
      workbook.close();
    } catch (IOException e) {
      LOGGER.error("Error saving generated report:\n {}\n\n {}\n", reportFileName, e.getMessage(),
          e);
    }
  }

  protected boolean isValidClientId(CansClient clientDto) {
    final String externalId = clientDto.getExternalId();
    try {
      // Convert to CMS Key
      clientDto.setCmsKey(CmsKeyIdGenerator.getKeyFromUIIdentifier(externalId));

    } catch (IllegalArgumentException e) {
      LOGGER.info("Client [id: {}] -> Error getting CMS Key from UI Id: {}", clientDto.id,
          e.getMessage());
      // Let see if it matches base62 10 character pattern
      if (!externalId.matches("[0-9a-zA-Z]{10}")) {
        clientDto.setComment(e.getMessage());
        return false;
      } else {
        LOGGER.info("Client [id: {}] -> External Id [{}] matches CMS Key format. Continue ...",
            clientDto.id, externalId);
        clientDto.setCmsKey(externalId);
      }
    }

    if (clientDto.cmsKey != null) {
      Session session = grabCmsSession();
      Transaction txn = session.beginTransaction();
      try {
        if (session.createNativeQuery(NQ_CMS_CLIENT_FIND)
            .setParameter(CMS_KEY, clientDto.cmsKey, StringType.INSTANCE).setReadOnly(true)
            .getResultList().isEmpty()) {

          LOGGER.info("Client [id: {}] -> Not found in CMS.", clientDto.id);
          clientDto.setComment(CLIENT_NOT_FOUND_IN_CMS);
          txn.rollback();
          return false;
        }
        txn.rollback();
        LOGGER.info("Client [id: {}] -> Found in CMS.", clientDto.id);
        return true;
      } catch (Exception e) {
        txn.rollback();
        LOGGER.error("Client [id: {}] -> Error while working with CMS database:\n {}", clientDto.id,
            e.getMessage(), e);
        clientDto.setComment(clientDto.comment.concat(" Error while working with CMS database: ")
            .concat(e.getMessage()));
      } finally {
        txn.rollback();
      }
    } else {
      LOGGER.info("Client [id: {}] -> Client Id is [null].", clientDto.id);
      clientDto.setComment("Client Id is [null].");
    }
    return false;
  }

  protected void attemptToFix(CansClient clientDto) {
    // Attempt to fix automatically assuming it might be deleted as a result of merge.
    if (cmsSessionFactory == null || !clientDto.comment.equals(CLIENT_NOT_FOUND_IN_CMS)) {
      // But only those NOT_FOUND_IN_CMS
      return;
    }

    LOGGER.info("Client [id: {}] -> Attempting to fix...", clientDto.id);
    clientDto.setComment(clientDto.comment + " Attempting to fix... ");
    String newKey = null;
    Session cmsSession = grabCmsSession();
    Transaction cmsTxn = cmsSession.beginTransaction();
    String sourceRowKey = clientDto.cmsKey;

    // Finding Merge Target Row Key
    try {
      List<Object> cmsObjectArrayList;
      do {
        cmsObjectArrayList = cmsSession.createNativeQuery(NQ_CMS_MERGE_CLIENT_FIND)
            .setParameter(CMS_KEY, sourceRowKey, StringType.INSTANCE).setReadOnly(true)
            .getResultList();
        if (cmsObjectArrayList.isEmpty()) {
          newKey = null;
        } else {
          newKey = (String) cmsObjectArrayList.get(0);
        }
        // Prepare for chained merge
        sourceRowKey = newKey;
        if (newKey != null) {
          // Check to see if found Merge Target exist in CLIENT table
          cmsObjectArrayList = cmsSession.createNativeQuery(NQ_CMS_CLIENT_FIND)
              .setParameter(CMS_KEY, newKey, StringType.INSTANCE).setReadOnly(true).getResultList();
          if (!cmsObjectArrayList.isEmpty()) {
            // Found - no need to look for chained merge
            sourceRowKey = null;
          } else {
            newKey = null;
          }
        }
      } while (sourceRowKey != null); // If merge target not found
      // continue with finding it's merge target
    } catch (Exception e) {
      cmsTxn.rollback();
      LOGGER.error("Client [id: {}] -> Error while working with CMS database:\n {}", clientDto.id,
          e.getMessage(), e);
      clientDto.setComment(clientDto.comment.concat(" Error while working with CMS database: ")
          .concat(e.getMessage()));
    } finally {
      cmsTxn.rollback();
    }

    // Updating CANS Client Row ExternalId with found Merge Targer Row Key
    if (newKey != null) {
      LOGGER.info("Client [id: {}] -> Merge Target Id Found. Updating CANS...", clientDto.id);
      clientDto.setComment(clientDto.comment + " Merge Target Id Found. Updating CANS...");
      Session cansSession = grabCansSession();
      Transaction cansTxn = cansSession.beginTransaction();
      try {
        // Store external_id in a format of original value CANS 1.0 or 1.1
        final String newExternalId = clientDto.externalId.equals(clientDto.cmsKey) ? newKey
            : CmsKeyIdGenerator.getUIIdentifierFromKey(newKey);

        // See if CANS already has record with this external id
        List<Object> cansObjectArrayList = cansSession
            .createNativeQuery(NQ_CANS_CLIENT_FIND_BY_EXTERNAL_ID)
            .setParameter(PARAM_EXTERNAL_ID, newExternalId, StringType.INSTANCE).getResultList();
        if (cansObjectArrayList.isEmpty()) {
          // Not Found - Just UPDATE the external_id field in the original person record
          cansSession.createNativeQuery(NQ_CANS_CLIENT_EXTERNAL_ID_UPDATE)
              .setParameter(PARAM_ID, clientDto.id, LongType.INSTANCE)
              .setParameter(PARAM_EXTERNAL_ID, clientDto.externalId, StringType.INSTANCE)
              .setParameter(PARAM_NEW_EXTERNAL_ID, newExternalId, StringType.INSTANCE)
              .executeUpdate();
          LOGGER.info("Client [id: {}] -> " + SUCCESS + ". New External Id: [{}].", clientDto.id,
              newExternalId);
          clientDto.setComment(
              clientDto.comment + SUCCESS + ". New Client External Id: [" + newExternalId + "].");
          return;
        } else {
          // Found existing record with same external id:
          // - re-associate assessments from original person record to the found one.
          Long newPersonId = ((BigInteger) cansObjectArrayList.get(0)).longValue();
          grabCansSession().createNativeQuery(NQ_CANS_ASSESSMENTS_UPDATE_PERSON_ID)
              .setParameter(PARAM_PERSON_ID, clientDto.id, LongType.INSTANCE)
              .setParameter(PARAM_NEW_PERSON_ID, newPersonId, LongType.INSTANCE).executeUpdate();
          // - update assessment auditntable as well.
          grabCansSession().createNativeQuery(NQ_CANS_ASSESSMENTS_AUD_UPDATE_PERSON_ID)
              .setParameter(PARAM_PERSON_ID, clientDto.id, LongType.INSTANCE)
              .setParameter(PARAM_NEW_PERSON_ID, newPersonId, LongType.INSTANCE).executeUpdate();
          LOGGER.info(
              "Client [id: {}] -> New External Id: [{}]. There is existing Client [id: {}]"
                  + " with this External Id. Re-associating assessments to existing Client.",
              clientDto.id, newExternalId, newPersonId);
          clientDto.setComment(clientDto.comment + ". New External Id: [" + newExternalId
              + "]. There is existing" + " Client [id: " + newPersonId.toString()
              + "]  with this External Id." + " Re-associating assessments to existing Client.");

          // - delete original person record.
          grabCansSession().createNativeQuery(NQ_CANS_CLIENT_DELETE)
              .setParameter(PARAM_ID, clientDto.id, LongType.INSTANCE).executeUpdate();
          LOGGER.info("Client [id: {}] -> Deleting Client.", clientDto.id);
          clientDto.setComment(clientDto.comment + ". Deleting Client.");

          LOGGER.info("Client [id: {}] -> " + SUCCESS, clientDto.id);
          clientDto.setComment(clientDto.comment + SUCCESS);
          return;
        }
      } catch (Exception e) {
        cansTxn.rollback();
        LOGGER.error("Client [id: {}] -> Error while working with CANS database:\n {}",
            clientDto.id, e.getMessage(), e);
        clientDto.setComment(clientDto.comment.concat(" Error while working with CANS database: ")
            .concat(e.getMessage()));
      } finally {
        cansTxn.commit();
      }
    }
    LOGGER.info("Client [id: {}] ->  Merge Target Id NOT Found. {}", clientDto.id, FAILED);
    clientDto.setComment(clientDto.comment + " Merge Target Id NOT Found. " + FAILED);
  }

  protected void buildReportFileName() {
    reportFileName = Paths.get(baseDir).toAbsolutePath().toString() + File.separatorChar
        + getClass().getSimpleName() + "_"
        + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date()) + ".xlsx";
  }

  protected void reportClient(CansClient clientPojo) {
    Row row = sheet.createRow(nextRowNum++);
    CellStyle cellStyle =
        clientPojo.comment.contains(SUCCESS) ? detailsRowFixedStyle : detailsRowStyle;
    CellStyle dateStyle = clientPojo.comment.contains(SUCCESS) ? dateCellFixedStyle : dateCellStyle;

    // Create cells
    for (int index = 0; index < columns.length; index++) {
      Cell cell = row.createCell(index);
      cell.setCellStyle(cellStyle);
      Field field = columns[index];
      int valueLength = 0;
      try {
        if (field.getType() == Long.class) {
          cell.setCellValue((Long) field.get(clientPojo));
          valueLength = String.valueOf(cell.getNumericCellValue()).trim().length();
        } else if (field.getType() == Date.class) {
          cell.setCellValue((Date) field.get(clientPojo));
          cell.setCellStyle(dateStyle);
          valueLength = 10;
        } else {
          // Rest is Strings
          cell.setCellValue((String) field.get(clientPojo));
          valueLength = cell.getStringCellValue().trim().length();
        }
        columnsWidth[index] = max(columnsWidth[index], valueLength + valueLength / 2);
      } catch (IllegalAccessException e) {
        // Skip the cell
        LOGGER.error("Error: {}", e);
      }
    }
  }

  // -----------------------------------------------------------------------------------------------
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  protected @interface NQResultEntity {

  }

  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  protected @interface NQResultColumn {
    int index();
  }

  @NQResultEntity
  static class CansClient {

    @NQResultColumn(index = 0)
    Long id;

    @NQResultColumn(index = 1)
    String externalId;

    @NQResultColumn(index = 2)
    String firstName;

    @NQResultColumn(index = 3)
    String middleName;

    @NQResultColumn(index = 4)
    String lastName;

    @NQResultColumn(index = 5)
    String suffix;

    @NQResultColumn(index = 6)
    Date dob;

    @NQResultColumn(index = 7)
    String gender;

    @NQResultColumn(index = 8)
    String countyName;

    @NQResultColumn(index = 9)
    String cansStatus;

    @NQResultColumn(index = 10)
    Date eventDate;

    @NQResultColumn(index = 11)
    String userId;

    @NQResultColumn(index = 12)
    String userFirstName;

    @NQResultColumn(index = 13)
    String userLastName;

    String cmsKey;
    String comment;

    CansClient() {}

    String getExternalId() {
      return externalId;
    }

    void setCmsKey(String cmsKey) {
      this.cmsKey = cmsKey;
    }

    void setComment(String comment) {
      this.comment = comment;
    }

  }
}
