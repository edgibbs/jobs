package gov.ca.cwds.neutron.rocket;

import static gov.ca.cwds.neutron.rocket.ClientSQLResource.INS_LST_CHG_KEY_BUNDLE;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_CLI_IDS_LST_CHG;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;

import gov.ca.cwds.data.persistence.cms.client.RawAddress;
import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.client.RawClientAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.ClientPersonIndexerJob;
import gov.ca.cwds.neutron.atom.AtomLoadStepHandler;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

/**
 * Last change logic for {@link ClientPersonIndexerJob}, Client loader rocket for the People Summary
 * index.
 * 
 * @author CWDS API Team
 * @see AtomLoadStepHandler
 */
@SuppressWarnings({"findsecbugs:SQL_INJECTION_JDBC"})
public class PeopleSummaryLastChangeHandler extends PeopleSummaryThreadHandler {

  private static final long serialVersionUID = 1L;

  private static final int BUNDLE_KEY_SIZE = 2000;

  private static final int LOG_EVERY = NeutronIntegerDefaults.LOG_EVERY.getValue() / 10;

  /**
   * Client primary keys.
   */
  private final List<String> keys = new ArrayList<>(BUNDLE_KEY_SIZE * 2);

  private int rangeStart = 0;

  private int rangeEnd = 0;

  private static AtomicLong lastReplicationDelay = new AtomicLong(30000L);

  /**
   * Preferred ctor.
   * 
   * @param rocket parent people summary rocket
   */
  public PeopleSummaryLastChangeHandler(ClientPersonIndexerJob rocket) {
    super(rocket);
  }

  /**
   * {@inheritDoc}
   * 
   * Handle additional JDBC statements, if any.
   * 
   * <p>
   * DB2 doesn't deal well with large sets of keys; buffers overflow and lock up. Split lists of
   * changed keys into bundles and commit frequently.
   * </p>
   * 
   * <p>
   * CAUTION: Prod can hang here when calling {@code SessionFactory.getCurrentSession()}.
   * </p>
   */
  @Override
  public List<ReplicatedClient> fetchLastRunNormalizedResults(Date lastRunDate,
      Set<String> deletionResults) {
    final Pair<String, String> range = Pair.<String, String>of("a", "b"); // dummy range
    handleStartRange(range);
    this.deletionResults = deletionResults;
    final ClientPersonIndexerJob rocket = getRocket();

    Connection con = null;
    try (final Session session = rocket.getJobDao().grabSession()) {
      con = NeutronJdbcUtils.prepConnection(session);
      handleSecondaryJdbc(con, range);

      // Merge placement homes and index into Elasticsearch.
      handleJdbcDone(range);
      final List<ReplicatedClient> ret = getResults();
      LOGGER.info("FETCHED {} LAST CHANGE RESULTS", ret.size());
      return ret;
    } catch (Exception e) {
      rocket.fail();
      throw CheeseRay.runtime(LOGGER, e, "ERROR EXECUTING LAST CHANGE! {}", e.getMessage());
    } finally {
      handleFinishRange(range);
      rocket.getFlightLog().markRangeComplete(range);
      rocket.doneRetrieve();
    }
  }

  /**
   * Jiggle the handle. Flush and clear session.
   * 
   * @param session active Hibernate session
   */
  protected void clearSession(final Session session) {
    LOGGER.trace("Flush and clear session");
    session.clear();
    session.flush();
  }

  protected void readClientKeys(final ResultSet rs) {
    LOGGER.trace("readClientKeys(): begin");
    int counter = 0;
    String key = null;
    final ClientPersonIndexerJob rocket = getRocket();

    try {
      while (rocket.isRunning() && rs.next() && (key = rs.getString(1)) != null) {
        CheeseRay.logEvery(LOGGER, LOG_EVERY, ++counter, "Read", "client key");
        keys.add(key);
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ CLIENT KEY! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} client keys.", counter);
  }

  @Override
  protected void loadClientRange(Connection con, final PreparedStatement stmtInsClient,
      Pair<String, String> range) throws SQLException {
    LOGGER.trace("loadClientRange(): begin");
    con.commit();
    final int clientCount = insertNextKeyBundle(con, rangeStart, rangeEnd);
    LOGGER.info("loadClientRange(): client count: {}", clientCount);
  }

  protected int insertNextKeyBundle(Connection con, int start, int end) {
    LOGGER.trace("insertNextKeyBundle(): begin");
    int ret = 0;

    try (final PreparedStatement ps = con.prepareStatement(INS_LST_CHG_KEY_BUNDLE, TFO, CRO)) {
      con.commit();
      final List<String> bundle = keys.subList(start, end);
      LOGGER.debug("bundle size: {}, start: {}, end: {}", bundle.size(), start, end);

      for (String key : bundle) {
        CheeseRay.logEvery(LOGGER, ++ret, "insert bundle keys", "keys");
        ps.setString(1, key);
        ps.addBatch();
      }
      ps.executeBatch();
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "ERROR INSERTING CLIENT KEYS!: {}", e.getMessage());
    }

    LOGGER.debug("insertNextKeyBundle(): done: count: {}", ret);
    return ret;
  }

  @Override
  protected boolean isInitialLoad() {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Read placement home addresses per rule R-02294, Client Abstract Most Recent Address. Read from
   * tables using same logic as Initial Load.
   * </p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public void handleSecondaryJdbc(Connection con, Pair<String, String> range) throws SQLException {
    final ClientPersonIndexerJob rocket = getRocket();
    final Date lastRunTime = rocket.getFlightLog().getLastChangeSince();
    LOGGER.info("handleSecondaryJdbc(): last successful run: {}", lastRunTime);
    handleStartRange(range);

    // ---------------------------
    // RETRIEVE DATA
    // ---------------------------

    // SNAP-725: use the same retrieval logic as Initial Load.
    try (final Session session = rocket.getJobDao().grabSession()) {
      NeutronJdbcUtils.enableBatchSettings(session);
      NeutronJdbcUtils.enableBatchSettings(con);

      // SNAP-808: process changed records only once.
      final Date overrideLastChgDate = rocket.getFlightPlan().getOverrideLastEndTime();
      final String sqlChangedClients = NeutronDB2Utils.prepLastChangeSQL(SEL_CLI_IDS_LST_CHG,
          rocket.determineLastSuccessfulRunTime(),
          overrideLastChgDate != null ? overrideLastChgDate : new Date());

      // Get list changed clients and process in bundles of BUNDLE_KEY_SIZE.
      LOGGER.info("LAST CHANGE: Get changed client keys");
      step(STEP.FIND_CHANGED_CLIENT);
      LOGGER.debug("What Changed SQL\n{}", sqlChangedClients);
      try (final PreparedStatement stmt = con.prepareStatement(sqlChangedClients, TFO, CRO)) {
        read(stmt, rs -> readClientKeys(rs));
      } finally {
        // Auto-close statement.
      }

      // Add re-run client keys.
      final Deque<String> rerunIds = rocket.getFlightPlan().getDequeRerunIds();
      if (rerunIds != null && !rerunIds.isEmpty()) {
        String id;
        while ((id = rerunIds.pollLast()) != null) {
          LOGGER.warn("***** RE-RUN CLIENT ID {} *****", id);
          keys.add(id);
        }
      }

      final int totalKeys = keys.size();
      LOGGER.info("keys: {}", totalKeys);
      rangeStart = rangeEnd = 0;
      con.commit(); // free db resources

      // CATCH: commit clears temp tables, forcing us to find changed clients again.
      // SNAP-808: save changed records to standing table.

      // 0-999, 1000-1999, 2000-2999, etc.
      for (rangeStart = 0; rangeStart < totalKeys; rangeStart += BUNDLE_KEY_SIZE) {
        rangeEnd = Math.min(rangeStart + BUNDLE_KEY_SIZE - 1, Math.max(totalKeys, 1));
        range = Pair.of(String.valueOf(rangeStart), String.valueOf(rangeEnd));
        LOGGER.info("last change key subset range {} of {}", range, totalKeys);
        super.handleSecondaryJdbc(con, range);
      }

      LOGGER.info("***** DONE retrieving data *****");
      con.commit(); // free db resources
    } catch (Exception e) {
      rocket.fail(); // Last change: fail the WHOLE job.
      try {
        con.rollback();
      } catch (Exception e2) {
        LOGGER.warn("handleSecondaryJdbc: NESTED EXCEPTION", e2);
      }
      throw CheeseRay.runtime(LOGGER, e, "OUTER EXTRACT ERROR!: {}", e.getMessage());
    } finally {
      // session goes out of scope
    }

    LOGGER.info("handleSecondaryJdbc: DONE");
  }

  @Override
  protected void calcReplicationDelay() {
    final FlightLog fl = getRocket().getFlightLog();
    final OptionalDouble avgRepTimeClient = rawClients.values().stream()
        .filter(RawClient::hasAddedTime).mapToLong(RawClient::calcReplicationTime).average();

    final OptionalDouble avgRepTimeAddress = rawClients.values().stream()
        .flatMap(c -> c.getClientAddress().values().stream()).map(RawClientAddress::getAddress)
        .filter(RawAddress::hasAddedTime).mapToLong(RawAddress::calcReplicationTime).average();

    long maxRepLag =
        (long) Math.max(avgRepTimeClient.isPresent() ? avgRepTimeClient.getAsDouble() : 0,
            avgRepTimeAddress.isPresent() ? avgRepTimeAddress.getAsDouble() : 0);
    maxRepLag = maxRepLag == 0 ? lastReplicationDelay.get() : maxRepLag;
    fl.addOtherMetric(STEP.REPLICATION_TIME.name().toLowerCase(), "" + maxRepLag);
    fl.addOtherMetric(STEP.REPLICATION_TIME_SECS.name().toLowerCase(), "" + (maxRepLag / 1000));
    lastReplicationDelay.set(maxRepLag);
    LOGGER.info("replication time: client: {}, address: {}, max lag: {}", avgRepTimeClient,
        avgRepTimeAddress, maxRepLag);
  }

  @Override
  public void handleFinishRange(Pair<String, String> range) {
    keys.clear();
    super.handleFinishRange(range);
  }

  @Override
  public String getEventType() {
    return "neutron_lc_client";
  }

}
