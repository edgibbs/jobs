package gov.ca.cwds.neutron.rocket;

import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_TIMESTAMP;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.UPD_TIMESTAMP;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.DbResetStatusDao;
import gov.ca.cwds.data.persistence.cms.DatabaseResetEntry;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.jetpack.JobLogs;
import gov.ca.cwds.neutron.rocket.PeopleSummaryThreadHandler.STEP;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

/**
 * Rocket samples replication lag by updating records in the transactional schema and polls for
 * changes in its companion, replicated schema. See SNAP-796.
 * 
 * @author CWDS API Team
 */
public class ReplicationLagRocket extends BasePersonRocket<DatabaseResetEntry, DatabaseResetEntry> {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(ReplicationLagRocket.class);

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

  public Pair<String, String> findSchemas(final Session session) {
    final String replSchema =
        ((String) session.getSessionFactory().getProperties().get("hibernate.default_schema"))
            .replaceAll("\"", "");
    final String txnlSchema =
        replSchema.replaceFirst("CWSRS", "CWSNS").replaceFirst("CWSREP", "CWSPRD");
    return Pair.of(replSchema, txnlSchema);
  }

  /**
   * Update records.
   */
  protected boolean measureReplicationLag() {
    boolean ret = false;
    try (final Session session = getJobDao().grabSession()) {
      final Pair<String, String> schemas = findSchemas(session);
      repSchema = schemas.getLeft();
      txnSchema = schemas.getRight();

      final Connection con = NeutronJdbcUtils.prepConnection(session);
      try (
          final PreparedStatement stmtUpd =
              con.prepareStatement(UPD_TIMESTAMP.replaceAll("TX_SCHEMA", txnSchema));
          final PreparedStatement stmtSel =
              con.prepareStatement(SEL_TIMESTAMP.replaceAll("TX_SCHEMA", txnSchema))) {
        grabTransaction();
        stmtUpd.executeUpdate();
        con.commit();

        final long delay = 500L; // shorter delay is more accurate but pressures DB more
        final int maxChecks = 240;
        final long start = System.currentTimeMillis();

        for (int i = 1; i < maxChecks; i++) {
          JobLogs.logEvery(LOGGER, 10, i, "time replication: delay (millis): {}", delay);
          Thread.sleep(delay);
          ret = verify(stmtSel.executeQuery());
          con.rollback();
          if (ret) {
            final String lag = "" + (System.currentTimeMillis() - start);
            LOGGER.info("Replication took {} millis", lag);
            getFlightLog().addOtherMetric(STEP.REPLICATION_TIME.name().toLowerCase(), lag);
            break;
          }
        }

      } finally {
        // Auto-close statements.
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "ERROR CHECKING REPLICATION LAG! {}", e.getMessage());
    } finally {
      // Auto-close session.
    }

    return ret;
  }

  protected boolean verify(final ResultSet rs) throws SQLException {
    boolean ret = false;
    while (isRunning() && rs.next()) {
      final Date one = rs.getTimestamp(1);
      final Date two = rs.getTimestamp(2);
      ret = one != null && two != null && one.equals(two);
      break;
    }

    return ret;
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
