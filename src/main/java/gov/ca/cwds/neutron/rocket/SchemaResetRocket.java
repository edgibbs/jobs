package gov.ca.cwds.neutron.rocket;

import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.persistence.ParameterMode;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.procedure.ProcedureCall;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.DbResetStatusDao;
import gov.ca.cwds.data.persistence.cms.DatabaseResetEntry;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;

/**
 * Refreshes a <strong>TEST</strong> transactional schema and its companion, replicated schema.
 * 
 * @author CWDS API Team
 */
public class SchemaResetRocket extends BasePersonRocket<DatabaseResetEntry, DatabaseResetEntry> {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(SchemaResetRocket.class);

  private DbResetStatusDao dao;

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao arbitrary DAO to fulfill interface
   * @param mapper Jackson ObjectMapper
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param flightPlan command line options
   * @param launchDirector launch director
   */
  @Inject
  public SchemaResetRocket(final DbResetStatusDao dao, final ObjectMapper mapper,
      @LastRunFile String lastRunFile, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, null, lastRunFile, mapper, flightPlan, launchDirector);
    this.dao = dao;
  }

  @Override
  public Date launch(Date lastRunDate) {
    try {
      refreshSchema();
      done();
    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(LOGGER, e, "DB2 SCHEMA RESET ERROR!! {}", e.getMessage());
    }

    return lastRunDate;
  }

  public String getDbSchema() {
    final Session session = getJobDao().getSessionFactory().getCurrentSession();
    grabTransaction(); // HACK

    final String targetTransactionalSchema =
        ((String) session.getSessionFactory().getProperties().get("hibernate.default_schema"))
            .replaceFirst("CWSRS", "CWSNS").replaceAll("\"", "");
    LOGGER.info("CALL DB2 SCHEMA RESET: target schema: {}", targetTransactionalSchema);
    return targetTransactionalSchema;
  }

  /**
   * Refresh a DB2 test schema by calling a stored procedure.
   * 
   * @throws NeutronCheckedException on database error
   */
  protected void refreshSchema() throws NeutronCheckedException {
    if (!isLargeDataSet()) {
      LOGGER.warn("\n\n\n\t   ********** RESET DB2 SCHEMA!! ********** \n\n\n");

      final Session session = getJobDao().grabSession();
      grabTransaction();

      final ProcedureCall proc = session.createStoredProcedureCall("CWSTMP.SPREFDBS");
      proc.registerStoredProcedureParameter("SCHEMANM", String.class, ParameterMode.IN);
      proc.registerStoredProcedureParameter("RETSTATUS", String.class, ParameterMode.OUT);
      proc.registerStoredProcedureParameter("RETMESSAG", String.class, ParameterMode.OUT);

      proc.setParameter("SCHEMANM", getDbSchema());
      proc.execute();

      final String returnStatus = (String) proc.getOutputParameterValue("RETSTATUS");
      final String returnMsg = (String) proc.getOutputParameterValue("RETMESSAG");
      LOGGER.info("reset schema proc: status: {}, msg: {}", returnStatus, returnMsg);

      if (StringUtils.isNotBlank(returnStatus) && returnStatus.charAt(0) != '0') {
        fail();
        throw CheeseRay.checked(LOGGER, "DB2 SCHEMA RESET ERROR! {}", returnMsg);
      } else {
        // If schema reset operation does not finish in 90 minutes, we timeout with an exception
        final int schemaRefreshTimeoutSeconds = 90 * 60;
        final int pollPeriodInSeconds = 60;
        int secondsWaited = 0;

        while (!schemaRefreshCompleted(pollPeriodInSeconds)) {
          secondsWaited += pollPeriodInSeconds;
          LOGGER.info("seconds waited: {}, timeout: {}", secondsWaited,
              schemaRefreshTimeoutSeconds);

          if (secondsWaited >= schemaRefreshTimeoutSeconds) {
            final StringBuilder buf = new StringBuilder();
            buf.append("DB2 schema reset operation timed out after ").append(secondsWaited / 60)
                .append(" minutes");

            LOGGER.error("DB2 schema reset operation timed out after {} seconds", secondsWaited);
            throw new IllegalStateException(buf.toString());
          }
        }

        LOGGER.warn("DB2 SCHEMA RESET COMPLETED!");
      }
    } else {
      LOGGER.error("SAFETY! DB2 SCHEMA RESET PROHIBITED ON LARGE DATA SETS!");
      fail();
    }
  }

  private boolean schemaRefreshCompleted(int waitTimeSeconds) {
    try {
      TimeUnit.SECONDS.sleep(waitTimeSeconds);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      String errorMsg = "DB2 schema reset operation timeout!";
      throw CheeseRay.runtime(LOGGER, e, "DB2 SCHEMA RESET ERROR! {}", errorMsg);
    }

    boolean completed = false;
    String status = findSchemaRefreshStatus();

    if ("S".equalsIgnoreCase(status)) {
      completed = true;
    } else if ("F".equalsIgnoreCase(status)) {
      throw new IllegalStateException("DB2 SCHEMA RESET OPERATION FAILED!");
    }

    return completed;
  }

  private String findSchemaRefreshStatus() {
    return dao.findBySchemaStartTime(getDbSchema()).getRefreshStatus();
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(SchemaResetRocket.class, args);
  }

}
