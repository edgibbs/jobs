package gov.ca.cwds.neutron.rocket;

import static gov.ca.cwds.neutron.rocket.ClientSQLResource.INS_LAST_CHG_KEY_BUNDLE;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_ALL_CLIENT_LAST_CHG;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;

import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.ClientPersonIndexerJob;
import gov.ca.cwds.neutron.atom.AtomLoadStepHandler;
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

  private static final int BUNDLE_KEY_SIZE = 10000;

  private final List<String> keys = new ArrayList<>(BUNDLE_KEY_SIZE * 2);

  private int rangeStart = 0;

  private int rangeEnd = 0;

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
   * DB2 doesn't deal well with large sets of keys. Split lists of changed keys into bundles and
   * commit frequently.
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
    try (final Session session = getRocket().getJobDao().grabSession()) {
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
    int counter = 0;
    String k = null;
    final ClientPersonIndexerJob rocket = getRocket();

    try {
      while (rocket.isRunning() && rs.next() && (k = rs.getString(1)) != null) {
        CheeseRay.logEvery(LOGGER, 5000, ++counter, "Read", "client key");
        keys.add(k);
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ CLIENT KEY! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} client keys.", counter);
  }

  @Override
  protected void loadClientRange(Connection con, final PreparedStatement stmtInsClient,
      Pair<String, String> range) throws SQLException {
    LOGGER.debug("loadClientRange(): begin");
    con.commit();
    final int clientCount = insertNextKeyBundle(con, rangeStart, rangeEnd);
    LOGGER.info("loadClientRange(): client count: {}", clientCount);
  }

  protected int insertNextKeyBundle(Connection con, int start, int end) {
    LOGGER.debug("insertNextKeyBundle(): begin");
    int ret = 0;

    try (final PreparedStatement ps = con.prepareStatement(INS_LAST_CHG_KEY_BUNDLE, TFO, CRO)) {
      LOGGER.debug("key bundle: start: {}, end: {}", start, end);
      con.commit();

      final List<String> subset = keys.subList(start, Math.max(end, Math.min(keys.size() - 1, 0)));
      LOGGER.debug("insertNextKeyBundle(): subset size: {}", subset.size());

      for (String key : subset) {
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

      final Date overrideLastChgDate = rocket.getFlightPlan().getOverrideLastEndTime();
      final String sqlChangedClients = NeutronDB2Utils.prepLastChangeSQL(SEL_ALL_CLIENT_LAST_CHG,
          rocket.determineLastSuccessfulRunTime(),
          overrideLastChgDate != null ? overrideLastChgDate : new Date());

      // Get list changed clients and process in bundles of BUNDLE_KEY_SIZE.
      LOGGER.info("LAST CHANGE: Get changed client keys");
      try (final PreparedStatement stmt = con.prepareStatement(sqlChangedClients, TFO, CRO)) {
        read(stmt, rs -> readClientKeys(rs));
      } finally {
        // Auto-close statement.
      }

      final int totalKeys = keys.size();
      LOGGER.info("keys: {}", totalKeys);
      rangeStart = rangeEnd = 0;
      con.commit(); // free db resources

      // CATCH: commit clears temp tables, forcing us to find changed clients again.
      // OPTION: use a standing client id table and clear it before each run.

      // 0-999, 1000-1999, 2000-2999, etc.
      for (rangeStart = 0; rangeStart < totalKeys; rangeStart += BUNDLE_KEY_SIZE) {
        rangeEnd = Math.min(rangeStart + BUNDLE_KEY_SIZE - 1, totalKeys - 1); //
        range = Pair.of(String.valueOf(rangeStart), String.valueOf(rangeEnd));
        LOGGER.debug("last change key subset range: {}", range);
        super.handleSecondaryJdbc(con, range);
      }

      LOGGER.info("***** DONE retrieving data *****");
      con.commit(); // free db resources
    } catch (Exception e) {
      rocket.fail();
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
  public void handleFinishRange(Pair<String, String> range) {
    keys.clear();
    super.handleFinishRange(range);
  }

}
