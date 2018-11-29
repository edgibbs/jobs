package gov.ca.cwds.jobs;

import static java.lang.Math.max;
import static java.lang.Math.min;

import gov.ca.cwds.data.persistence.cms.CmsKeyIdGenerator;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.flight.FlightPlan;
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
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates CANS Client IDs against legacy DB2 CLIET_T table and reports not valid ones.
 *
 * @author CWDS API Team
 */
public class ReportFaultCANSClientIdsJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReportFaultCANSClientIdsJob.class);
  private static final String HIBERNATE_CONFIG_CMS = "jobs-cms-hibernate.cfg.xml";
  private static final String HIBERNATE_CONFIG_NS = "jobs-ns-hibernate.cfg.xml";
  private static final String NQ_CANS_CLIENTS_ALL =
      "SELECT p.id, p.external_id, p.first_name, p.middle_name, p.last_name, p.suffix, p.dob, p.gender"
          + ", c.name AS county_name, a.status AS cans_status, a.event_date AS event_date"
          + ", u.external_id AS user_id, u.first_name AS u_first_name, u.last_name AS u_last_name "
          + " FROM {h-schema}person p"
          + " LEFT JOIN {h-schema}county c ON p.county_id = c.id"
          + " LEFT JOIN ("
          + "     SELECT DISTINCT ON (person_id) person_id, status, event_date, created_by, updated_by "
          + "     FROM {h-schema}assessment"
          + "     ORDER BY person_id asc, "
          + "             event_date desc, " // Most recent
          + "             status desc"  // IN-PROCESS is more important then COMPLETED, DELETED
          + " ) a ON p.id = a.person_id"
          + " LEFT JOIN {h-schema}person u"
          + "     ON (CASE WHEN a.updated_by IS NOT NULL THEN a.updated_by ELSE a.created_by END) = u.id"
          + " WHERE p.person_role = 'CLIENT'"
          + " ORDER BY user_id, county_name, last_name, first_name"
          + " FOR READ ONLY";

  private static final String NQ_CMS_CLIENT_FIND =
      "SELECT 1 FROM {h-schema}CLIENT_T"
          + " WHERE IDENTIFIER = :cmsKey"
          + " FOR READ ONLY WITH UR";

  private SessionFactory cmsSessionFactory;
  private SessionFactory cansSessionFactory;

  private String baseDir;
  private List<CansClient> clientList;
  private String reportFileName;

  //Excel related
  private Workbook workbook;
  private Sheet sheet;
  private CellStyle dateCellStyle;
  private Field[] columns = CansClient.class.getDeclaredFields();
  private int nextRowNum = 1; // headerRow is 0
  private int[] columnsWidth = new int[columns.length];


  private ReportFaultCANSClientIdsJob() {
    cmsSessionFactory = new Configuration().configure(HIBERNATE_CONFIG_CMS).buildSessionFactory();
    cansSessionFactory = new Configuration().configure(HIBERNATE_CONFIG_NS).buildSessionFactory();
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

      job.baseDir = Optional
          .ofNullable(FlightPlan.parseCommandLine(args).getBaseDirectory())
          .map(dir -> Paths.get(dir).toAbsolutePath().toString())
          .orElseGet(new File(".")::getAbsolutePath);

      job.buildClientList();
      job.generateReport();

      LOGGER.info("DONE validating client ids. Report is in the file:\n {}",
          job.reportFileName);
    } catch (Exception e) {
      LOGGER.error("\n\nEXECUTION FAILED !!!\n {}\n\n", e.getMessage(), e);

      System.exit(-1);
    }
    System.exit(0);
  }

  private static <T> List<T> mapNQResults(List<Object[]> objectArrayList, Class<T> genericType) {
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
  private static <T> List<Field> getNQResultColumnAnnotatedFields(Class<T> genericType) {
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

  private static void setObjectProperty(Object object, Field field, Object fieldValue) {
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

  private Session grabCmsSession() {
    try {
      return cmsSessionFactory.getCurrentSession();
    } catch (HibernateException e) {
      LOGGER.info("Can't get current session. Opening new one.", e);
      return cmsSessionFactory.openSession();
    }
  }

  private Session grabCansSession() {
    try {
      return this.cansSessionFactory.getCurrentSession();
    } catch (HibernateException e) {
      LOGGER.info("Can't get current session. Opening new one.", e);
      return this.cansSessionFactory.openSession();
    }
  }

  @SuppressWarnings("unchecked")
  private void buildClientList() {
    Session session = grabCansSession();
    Transaction txn = session.beginTransaction();
    try {
      clientList = mapNQResults(
          session.createNativeQuery(NQ_CANS_CLIENTS_ALL).setReadOnly(true)
              .getResultList(),
          CansClient.class);
    } catch (Exception e) {
      txn.rollback();
      LOGGER.error("Error while working with CANS database: {}\n", e.getMessage(), e);
      throw e;
    } finally {
      txn.rollback();
    }
  }

  private void generateReport() {
    buildReportFileName();
    try {
      initReport();
      for (CansClient clientDto : clientList) {
        if (!isValidClientId(clientDto)) {
          reportClient(clientDto);
        }
      }
    } catch (Exception e) {
      LOGGER.error("Error while validating CANS clients against CWS/CMS database: {}\n",
          e.getMessage(), e);
      throw e;
    } finally {
      finalizeReport();
    }
  }

  private void initReport() {
    // Create a Workbook
    workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

    /* CreationHelper helps us create instances of various things like DataFormat,
       Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
    CreationHelper createHelper = workbook.getCreationHelper();

    // Create a Sheet
    sheet = workbook.createSheet("CANS Clients");

    // Create a Font for styling header cells
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 14);
    headerFont.setColor(IndexedColors.RED.getIndex());

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

    // Create Cell Style for formatting Date
    dateCellStyle = workbook.createCellStyle();
    dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("MM/dd/yyyy"));
  }

  private void finalizeReport() {
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
      LOGGER.error("Error saving generated report:\n {}\n\n {}\n", reportFileName,
          e.getMessage(), e);
    }
  }

  private boolean isValidClientId(CansClient clientDto) {
    final String externalId = clientDto.getExternalId();
    try {
      //Convert to CMS Key
      clientDto
          .setCmsKey(CmsKeyIdGenerator.getKeyFromUIIdentifier(externalId));

    } catch (IllegalArgumentException e) {
      LOGGER.info("Client [id: {}] -> Error getting CMS Key from UI Id: {}", clientDto.id,
          e.getMessage(), e);
      //Let see if it matches base62 10 character pattern
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
            .setParameter("cmsKey", clientDto.cmsKey, StringType.INSTANCE).setReadOnly(true)
            .getResultList().isEmpty()) {

          LOGGER.info("Client [id: {}] -> Not found in CMS.", clientDto.id);
          clientDto.setComment("Client Not found in CMS");
          txn.rollback();
          return false;
        }
        txn.rollback();
        LOGGER.info("Client [id: {}] -> Found in CMS.", clientDto.id);
        return true;
      } catch (Exception e) {
        txn.rollback();
        LOGGER.error("Client [id: {}] -> Error while working with CMS database: {}\n", clientDto.id,
            e.getMessage(), e);
        throw e;
      } finally {
        txn.rollback();
      }
    } else {
      LOGGER.info("Client [id: {}] -> Client Id is [null].", clientDto.id);
      clientDto.setComment("Client Id is [null]");
      return false;

    }
  }

  private void buildReportFileName() {
    reportFileName = Paths.get(baseDir).toAbsolutePath().toString() + File.separatorChar
        + getClass().getSimpleName()
        + "_"
        + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date())
        + ".xlsx";
  }

  private void reportClient(CansClient clientPojo) {
    Row row = sheet.createRow(nextRowNum++);
    // Create cells
    for (int index = 0; index < columns.length; index++) {
      Cell cell = row.createCell(index);
      Field field = columns[index];
      int valueLength = 0;
      try {
        if (field.getType() == Long.class) {
          cell.setCellValue((Long) field.get(clientPojo));
          valueLength = String.valueOf(cell.getNumericCellValue()).trim().length();
        } else if (field.getType() == Date.class) {
          cell.setCellValue((Date) field.get(clientPojo));
          cell.setCellStyle(dateCellStyle);
          valueLength = 10;
        } else {
          //Rest is Strings
          cell.setCellValue((String) field.get(clientPojo));
          valueLength = cell.getStringCellValue().trim().length();

        }
        columnsWidth[index] = max(columnsWidth[index], valueLength + valueLength / 2);
      } catch (IllegalAccessException e) {
        //Skip the cell
        LOGGER.error("Error: {}", e);
      }
    }
  }

  //-----------------------------------------------------------------------------------------------
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  private @interface NQResultEntity {

  }

  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  private @interface NQResultColumn {

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

    CansClient() {
    }

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
