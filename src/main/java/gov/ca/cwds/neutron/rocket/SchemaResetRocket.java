package gov.ca.cwds.neutron.rocket;

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
   */
  @Inject
  public SchemaResetRocket(final DbResetStatusDao dao, final ObjectMapper mapper,
      @LastRunFile String lastRunFile, FlightPlan flightPlan) {
    super(dao, null, lastRunFile, mapper, flightPlan);
    this.dao = dao;
  }

  @Override
  public Date launch(Date lastRunDate) {
    LOGGER.warn("RESET TEST SCHEMA!!!");

    try {
      refreshSchema();
      done();
    } catch (Exception e) {
      fail();
      CheeseRay.checked(LOGGER, e, "SCHEMA RESET ERROR!! {}", e.getMessage());
    }

    return lastRunDate;
  }

  public String getDbSchema() {
    final Session session = getJobDao().getSessionFactory().getCurrentSession();
    getOrCreateTransaction(); // HACK

    final String targetTransactionalSchema =
        ((String) session.getSessionFactory().getProperties().get("hibernate.default_schema"))
            .replaceFirst("CWSRS", "CWSNS").replaceAll("\"", "");
    LOGGER.info("CALL SCHEMA RESET: target schema: {}", targetTransactionalSchema);
    return targetTransactionalSchema;
  }

  /**
   * Refresh a DB2 test schema by calling a stored procedure.
   * 
   * @throws NeutronCheckedException on database error
   */
  protected void refreshSchema() throws NeutronCheckedException {
    if (!isLargeDataSet()) {
      LOGGER.warn("\n\n\n   ********** RESET SCHEMA!! ********** \n\n\n");

      final Session session = getJobDao().getSessionFactory().getCurrentSession();
      getOrCreateTransaction();

      final ProcedureCall proc = session.createStoredProcedureCall("CWSTMP.SPREFDBS");
      proc.registerStoredProcedureParameter("SCHEMANM", String.class, ParameterMode.IN);
      proc.registerStoredProcedureParameter("RETSTATUS", String.class, ParameterMode.OUT);
      proc.registerStoredProcedureParameter("RETMESSAG", String.class, ParameterMode.OUT);

      proc.setParameter("SCHEMANM", getDbSchema());
      proc.execute();

      final String returnStatus = (String) proc.getOutputParameterValue("RETSTATUS");
      final String returnMsg = (String) proc.getOutputParameterValue("RETMESSAG");
      LOGGER.info("refresh schema proc: status: {}, msg: {}", returnStatus, returnMsg);

      if (StringUtils.isNotBlank(returnStatus) && returnStatus.charAt(0) != '0') {
        fail();
        CheeseRay.runtime(LOGGER, "SCHEMA RESET ERROR! {}", returnMsg);
      } else {
        // if schema refresh operation does not finish in 120 minutes, we timeout with an exception
        int schemaRefreshTimeoutSeconds = 1 * 60;
        int waitTimeSeconds = 5;
        int accumulatedWaitTimeSeconds = 0;

        while (!schemaRefreshCompleted(waitTimeSeconds)) {
          accumulatedWaitTimeSeconds += waitTimeSeconds;
          LOGGER.info("seconds waited: {}, timeout: {}", accumulatedWaitTimeSeconds,
              schemaRefreshTimeoutSeconds);

          if (accumulatedWaitTimeSeconds >= schemaRefreshTimeoutSeconds) {
            final StringBuilder buf = new StringBuilder();
            buf.append("Schema refresh operation timed out after ")
                .append(accumulatedWaitTimeSeconds / 60).append(" minutes");

            LOGGER.error("Schema refresh operation timed out after {} seconds",
                accumulatedWaitTimeSeconds);
            throw new IllegalStateException(buf.toString());
          }
        }

        LOGGER.warn("DB2 SCHEMA RESET COMPLETED!");
      }
    } else {
      LOGGER.warn("SAFETY! DB2 SCHEMA RESET PROHIBITED ON LARGE DATA SETS!");
    }
  }

  private boolean schemaRefreshCompleted(int waitTimeSeconds) {
    try {
      TimeUnit.SECONDS.sleep(waitTimeSeconds);
    } catch (InterruptedException e) {
      String errorMsg = "Schema refresh operation wait interrupted";
      CheeseRay.runtime(LOGGER, e, "SCHEMA RESET ERROR! {}", errorMsg);
    }

    boolean completed = false;
    String status = findSchemaRefreshStatus();

    if (status.equalsIgnoreCase("S")) {
      completed = true;
    } else if (status.equalsIgnoreCase("F")) {
      String errorMsg = "Schema refresh operation failed.";
      CheeseRay.runtime(LOGGER, "SCHEMA RESET ERROR! {}", errorMsg);
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
