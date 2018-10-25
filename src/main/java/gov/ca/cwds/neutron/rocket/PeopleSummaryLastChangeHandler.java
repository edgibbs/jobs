package gov.ca.cwds.neutron.rocket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.ca.cwds.data.persistence.cms.EsClientPerson;
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

  private static final int BUNDLE_KEY_COUNT = 10000;

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
   * DB2 doesn't deal well with large sets of keys. Split lists of changed keys
   */
  @Override
  public List<ReplicatedClient> fetchLastRunNormalizedResults(Date lastRunDate,
      Set<String> deletionResults) {
    final Pair<String, String> range = Pair.<String, String>of("a", "b"); // dummy range
    handleStartRange(range);
    this.deletionResults = deletionResults;
    final ClientPersonIndexerJob rocket = getRocket();

    // Today we normalize while reading records to minimize memory at the expense of some CPU.
    // Read from the view, old school.
    // addAll(getRocket().extractLastRunRecsFromView(lastRunDate, deletionResults));

    LOGGER.info("After view: count: {}", normalized.size());

    // Prod periodically hangs here when calling SessionFactory.getCurrentSession();
    // Handle additional JDBC statements, if any.
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

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Read placement home addresses per rule R-02294, Client Abstract Most Recent Address.
   * </p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public void handleSecondaryJdbc(Connection con, Pair<String, String> range) throws SQLException {
    final ClientPersonIndexerJob rocket = getRocket();
    final Date lastRunTime = rocket.getFlightLog().getLastChangeSince();
    LOGGER.info("handleSecondaryJdbc(): last successful run: {}", lastRunTime);

    // TODO: no longer needed.
    final Class<?> entityClass = rocket.getDenormalizedClass(); // view entity class
    final String queryName = rocket.getFlightPlan().isLoadSealedAndSensitive()
        ? entityClass.getName() + ".findAllUpdatedAfter"
        : entityClass.getName() + ".findAllUpdatedAfterWithUnlimitedAccess";

    LOGGER.debug("query name: {}", queryName);
    handleStartRange(range);

    Transaction txn = null;
    List<EsClientPerson> recs = null;
    int totalClientAddressRetrieved = 0;

    // ---------------------------
    // RETRIEVE DATA
    // ---------------------------

    // SNAP-725: use the same retrieval logic as Initial Load.
    // NEXT: no big transaction. Commit early and often.
    try (final Session session = rocket.getJobDao().grabSession()) {
      NeutronJdbcUtils.enableBatchSettings(session);
      NeutronJdbcUtils.enableBatchSettings(con);

      final String sqlPlacementAddress = NeutronDB2Utils.prepLastChangeSQL(
          ClientSQLResource.SEL_PLACEMENT_ADDR, rocket.determineLastSuccessfulRunTime(),
          rocket.getFlightPlan().getOverrideLastEndTime());
      LOGGER.info("SQL for Placement Address: \n{}", sqlPlacementAddress);
      final PreparedStatement stmtSelPlacementAddress = con.prepareStatement(sqlPlacementAddress);
      con = NeutronJdbcUtils.prepConnection(session);
      txn = rocket.grabTransaction();

      LOGGER.info("STEP #1: Store changed client keys in GT_REFR_CLT");
      final int totalKeys =
          rocket.runInsertAllLastChangeKeys(session, lastRunTime, rocket.getPrepLastChangeSQLs());
      recs = new ArrayList<>(totalKeys * 3);
      LOGGER.info("total keys found: {}", totalKeys);

      // OPTION: Commit often and early. This block is one big transaction.
      // Better to reinsert client id's as needed.

      // 1-1000, 1001-2000, 2001-3000, etc.
      for (int start = 1; start < totalKeys; start += BUNDLE_KEY_COUNT) {
        final int end = start + BUNDLE_KEY_COUNT - 1;
        LOGGER.info("STEP #2: CLEAR GT_ID");
        session.createNativeQuery("DELETE FROM GT_ID").executeUpdate();
        this.clearSession(session);

        LOGGER.info("STEP #3: INSERT keys into GT_ID, bundle: start: {}, end: {}", start, end);
        rocket.runInsertRownumBundle(session, start, end, ClientSQLResource.INSERT_NEXT_BUNDLE);
        this.clearSession(session);

        // NEW SCHOOL: same logic as Initial Load.
        super.handleSecondaryJdbc(con, range);
        super.handleJdbcDone(range);
      }

      LOGGER.info(" ***** commit transaction, DONE retrieving data *****");
      txn.commit(); // release database resources, clear temp tables
    } catch (Exception e) {
      rocket.fail();
      try {
        txn.rollback();
      } catch (Exception e2) {
        LOGGER.error("handleSecondaryJdbc: NESTED EXCEPTION", e2);
      }
      throw CheeseRay.runtime(LOGGER, e, "OUTER EXTRACT ERROR!: {}", e.getMessage());
    } finally {
      // session goes out of scope
    }

    final List<Thread> threads = new ArrayList<>();
    rocket.addThread(true, rocket::threadIndex, threads);

    LOGGER.info("handleSecondaryJdbc: DONE");
  }

}
