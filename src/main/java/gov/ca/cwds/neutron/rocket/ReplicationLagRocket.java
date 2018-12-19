package gov.ca.cwds.neutron.rocket;

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

  protected String replSchema;
  protected String txnlSchema;

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

    return lastRunDate;
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
      replSchema = schemas.getLeft();
      txnlSchema = schemas.getRight();

      final Connection con = NeutronJdbcUtils.prepConnection(session);
      try (
          final PreparedStatement stmtUpd = con.prepareStatement(
              ClientSQLResource.UPD_TIMESTAMP.replaceAll("TX_SCHEMA", txnlSchema));
          final PreparedStatement stmtSel = con.prepareStatement(
              ClientSQLResource.SEL_TIMESTAMP.replaceAll("TX_SCHEMA", txnlSchema))) {
        grabTransaction();
        stmtUpd.executeUpdate();
        con.commit();

        final long delayBetweenChecks = 500L;
        final int maxChecks = 240;
        final Object[] args = {};

        for (int i = 1; i < maxChecks; i++) {
          JobLogs.logEvery(LOGGER, 10, i, "time replication", args);
          Thread.sleep(delayBetweenChecks);
          ret = verify(stmtSel.executeQuery());
          con.rollback();
          if (ret) {
            LaunchCommand.getInstance().getCommonFlightPlan();
            LOGGER.info("Replication caught up in {} milliseconds", i * delayBetweenChecks);
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
      ret = one.equals(two);
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
