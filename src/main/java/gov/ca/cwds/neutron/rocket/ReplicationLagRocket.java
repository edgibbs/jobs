package gov.ca.cwds.neutron.rocket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.DbResetStatusDao;
import gov.ca.cwds.data.persistence.cms.DatabaseResetEntry;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.rocket.PeopleSummaryThreadHandler.STEP;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

/**
 * Rocket samples replication lag by updating records in the transactional schema and polls for
 * changes in its companion, replicated schema. See SNAP-796.
 * 
 * @author CWDS API Team
 */
@SuppressWarnings({"fb-contrib:SIL_SQL_IN_LOOP", "fb-contrib:JVR_JDBC_VENDOR_RELIANCE",
    "findsecbugs:SQL_INJECTION_JDBC"})
public class ReplicationLagRocket extends BasePersonRocket<DatabaseResetEntry, DatabaseResetEntry> {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(ReplicationLagRocket.class);

  private static volatile Float lastReplicationSeconds = 3.0F;

  protected String repSchema;
  protected String txnSchema;

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
  public ReplicationLagRocket(final DbResetStatusDao dao, final ObjectMapper mapper,
      @LastRunFile String lastRunFile, FlightPlan flightPlan, AtomLaunchDirector launchDirector) {
    super(dao, null, lastRunFile, mapper, flightPlan, launchDirector);
  }

  @Override
  public Date launch(Date lastRunDate) {
    try {
      measureReplicationLag();
      done();
    } catch (Exception e) {
      Thread.currentThread().interrupt();
      fail();
      throw CheeseRay.runtime(LOGGER, e, "ERROR CHECKING REPLICATION LAG! {}", e.getMessage());
    }

    return new Date();
  }

  /**
   * Measure actual replication time.
   */
  protected void measureReplicationLag() {
    Float lagSeconds = null;
    final FlightLog fl = getFlightLog();
    try (final Session session = getJobDao().grabSession()) {
      final Connection con = NeutronJdbcUtils.prepConnection(session);
      try (final PreparedStatement stmtSel =
          con.prepareStatement(ClientSQLResource.SEL_REPL_TIME_REAL)) {
        lagSeconds = pull(stmtSel.executeQuery());
        con.rollback();

        if (lagSeconds != null) {
          LOGGER.info("Replication took {} seconds", lagSeconds);
          lastReplicationSeconds = lagSeconds;
          fl.addOtherMetric(STEP.REPLICATION_TIME_SECS.name().toLowerCase(), lagSeconds.toString());
          fl.addOtherMetric(STEP.REPLICATION_TIME_MILLIS.name().toLowerCase(),
              new Float(lagSeconds * 1000).toString());
        }

      } finally {
        // Auto-close statements.
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "ERROR CHECKING REPLICATION LAG! {}", e.getMessage());
    } finally {
      // Auto-close session/connection.
    }
  }

  protected Float pull(final ResultSet rs) throws SQLException {
    Float ret = null;
    while (isRunning() && rs.next()) {
      ret = rs.getFloat(3);
      break;
    }

    return ret;
  }

  public static Float getLastReplicationSeconds() {
    return lastReplicationSeconds;
  }

  /**
   * Rocket launch point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(ReplicationLagRocket.class, args);
  }

}
