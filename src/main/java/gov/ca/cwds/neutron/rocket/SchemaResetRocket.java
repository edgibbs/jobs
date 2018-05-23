package gov.ca.cwds.neutron.rocket;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

  private transient DbResetStatusDao dao;

  private int timeoutSeconds = 90 * 60;

  private int pollPeriodInSeconds = 60;

  private final Lock lock = new ReentrantLock();

  private final Condition condWait = lock.newCondition();
  private final Condition condDone = lock.newCondition();

  private Condition condition;

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
        int secondsWaited = 0;

        while (!schemaRefreshCompleted(pollPeriodInSeconds)) {
          secondsWaited += pollPeriodInSeconds;
          LOGGER.info("seconds waited: {}, timeout: {}", secondsWaited, timeoutSeconds);

          if (!isRunning()) {
            throw new IllegalStateException("SCHEMA RESET: FLIGHT ABORTED!");
          }

          if (secondsWaited >= timeoutSeconds) {
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
    if (lock.tryLock()) {
      try {
        condDone.await(waitTimeSeconds, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        fail();
        Thread.currentThread().interrupt();
        throw CheeseRay.runtime(LOGGER, e, "DB2 SCHEMA RESET INTERRUPTED! {}", e.getMessage());
      } finally {
        lock.unlock();
      }
    }

    if (!isRunning()) {
      throw new IllegalStateException("SCHEMA RESET: FLIGHT ABORTED!");
    }

    boolean completed = false;
    final String status = findSchemaRefreshStatus();

    if (status.equalsIgnoreCase("S")) { // success
      completed = true;
    } else if (status.equalsIgnoreCase("F")) { // fail
      throw new IllegalStateException("DB2 SCHEMA RESET OPERATION FAILED!");
    }

    return completed;
  }

  private String findSchemaRefreshStatus() {
    return dao.findBySchemaStartTime(getDbSchema()).getRefreshStatus();
  }

  public int getSchemaRefreshTimeoutSeconds() {
    return timeoutSeconds;
  }

  public void setSchemaRefreshTimeoutSeconds(int schemaRefreshTimeoutSeconds) {
    this.timeoutSeconds = schemaRefreshTimeoutSeconds;
  }

  public int getPollPeriodInSeconds() {
    return pollPeriodInSeconds;
  }

  public void setPollPeriodInSeconds(int pollPeriodInSeconds) {
    this.pollPeriodInSeconds = pollPeriodInSeconds;
  }

  @Override
  public void done() {
    super.done();

    try {
      if (lock.tryLock(3, TimeUnit.SECONDS)) {
        try {
          condDone.signal();
        } finally {
          lock.unlock();
        }
      }
    } catch (InterruptedException e) {
      fail();
      Thread.currentThread().interrupt();
      throw CheeseRay.runtime(LOGGER, e, "DB2 SCHEMA RESET INTERRUPTED! {}", e.getMessage());
    }
  }

  @Override
  public void fail() {
    super.fail();

    try {
      if (lock.tryLock(3, TimeUnit.SECONDS)) {
        try {
          condDone.signal();
        } finally {
          lock.unlock();
        }
      }
    } catch (InterruptedException e) {
      fail();
      Thread.currentThread().interrupt();
      throw CheeseRay.runtime(LOGGER, e, "DB2 SCHEMA RESET INTERRUPTED! {}", e.getMessage());
    }
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
