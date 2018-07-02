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
import org.hibernate.query.NativeQuery;

import gov.ca.cwds.data.persistence.cms.EsClientPerson;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.ClientPersonIndexerJob;
import gov.ca.cwds.neutron.atom.AtomLoadStepHandler;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.util.NeutronThreadUtils;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

/**
 * Last change logic for {@link ClientPersonIndexerJob}.
 * 
 * @author CWDS API Team
 * @see AtomLoadStepHandler
 */
public class PeopleSummaryLastChangeHandler extends PeopleSummaryThreadHandler {

  private static final long serialVersionUID = 1L;

  private static final int BUNDLE_KEY_COUNT = 500;

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

    // Read from the view, old school.
    // addAll(getRocket().extractLastRunRecsFromView(lastRunDate, deletionResults));
    LOGGER.info("After view: count: {}", normalized.size());

    // Handle additional JDBC statements, if any.
    try (final Session session = getRocket().getJobDao().grabSession();
        final Connection con = NeutronJdbcUtils.prepConnection(session)) {
      handleSecondaryJdbc(con, range);

      // Merge placement homes and index into Elasticsearch.
      handleJdbcDone(range);
      final List<ReplicatedClient> ret = getResults();
      LOGGER.info("FETCHED {} LAST CHANGE RESULTS", ret.size());
      return ret;
    } catch (Exception e) {
      getRocket().fail();
      throw CheeseRay.runtime(LOGGER, e, "ERROR EXECUTING LAST CHANGE! {}", e.getMessage());
    } finally {
      handleFinishRange(range);
      getRocket().getFlightLog().markRangeComplete(range);
      getRocket().doneRetrieve();
    }
  }

  /**
   * Jiggle the handle, flush, and clear session.
   * 
   * @param session active Hibernate session
   */
  protected void clearSession(final Session session) {
    LOGGER.info("Flush and clear session");
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
    LOGGER.info("PULL VIEW: last successful run: {}", lastRunTime);
    final Class<?> entityClass = rocket.getDenormalizedClass(); // view entity class
    final String queryName = rocket.getFlightPlan().isLoadSealedAndSensitive()
        ? entityClass.getName() + ".findAllUpdatedAfter"
        : entityClass.getName() + ".findAllUpdatedAfterWithUnlimitedAccess";

    LOGGER.info("query name: {}", queryName);
    handleStartRange(range);

    Transaction txn = null;
    List<EsClientPerson> recs = null;
    int totalClientAddressRetrieved = 0;
    NeutronThreadUtils.freeMemory();

    // ---------------------------
    // RETRIEVE DATA
    // ---------------------------

    try (final Session session = rocket.getJobDao().grabSession()) {
      NeutronJdbcUtils.enableBatchSettings(session);
      NeutronJdbcUtils.enableBatchSettings(con);

      final String sqlPlacementAddress = NeutronDB2Utils.prepLastChangeSQL(
          ClientSQLResource.SELECT_PLACEMENT_ADDRESS, rocket.determineLastSuccessfulRunTime(),
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

      // 1-1000, 1001-2000, 2001-3000, etc.
      for (int start = 1; start < totalKeys; start += BUNDLE_KEY_COUNT) {
        final int end = start + BUNDLE_KEY_COUNT - 1;
        LOGGER.info("STEP #2: CLEAR GT_ID");
        session.createNativeQuery("DELETE FROM GT_ID").executeUpdate();
        this.clearSession(session);

        LOGGER.info("STEP #3: INSERT keys into GT_ID, bundle: start: {}, end: {}", start, end);
        rocket.runInsertRownumBundle(session, start, end, ClientSQLResource.INSERT_NEXT_BUNDLE);
        this.clearSession(session);

        try {
          { // scope brace
            LOGGER.info("STEP #4: prep query for client address view");
            final NativeQuery<EsClientPerson> q = session.getNamedNativeQuery(queryName);
            NeutronJdbcUtils.readOnlyQuery(q);

            LOGGER.info("STEP #5: pull from client address view");
            final List<EsClientPerson> resultsClientAddress = q.list();
            recs.addAll(resultsClientAddress); // read from key bundle

            final int recsRetrievedThisBundle = resultsClientAddress.size();
            totalClientAddressRetrieved += recsRetrievedThisBundle;
            LOGGER.info("FOUND {} CLIENT ADDRESS RECORDS FOR BUNDLE: {} .. {}",
                recsRetrievedThisBundle, start, end);
          }

          LOGGER.info("STEP #6: read placement home addresses: \n{}", sqlPlacementAddress);
          this.clearSession(session);
          readPlacementAddress(stmtSelPlacementAddress);

          // ======================================
          // ADD NEW SQL RETRIEVAL STEPS HERE!
          // ======================================

        } catch (Exception e) {
          rocket.fail();
          throw CheeseRay.runtime(LOGGER, e,
              "PeopleSummaryLastChangeHandler.handleSecondaryJdbc: INNER EXTRACT ERROR!: {}",
              e.getMessage());
        } finally {
          // leave it
        }
      }

      LOGGER.info(" ***** commit transaction, DONE pulling data *****");
      txn.commit(); // release database resources, clear temp tables
    } catch (Exception e) {
      rocket.fail();
      throw CheeseRay.runtime(LOGGER, e, "OUTER EXTRACT ERROR!: {}", e.getMessage());
    } finally {
      // session goes out of scope
    }

    final List<Thread> threads = new ArrayList<>();
    rocket.addThread(true, rocket::threadIndex, threads);
    NeutronThreadUtils.freeMemory();

    try {
      LOGGER.info("DATA RETRIEVAL DONE: client address: {}", totalClientAddressRetrieved);
      Object lastId = new Object();
      List<ReplicatedClient> results = new ArrayList<>(recs.size()); // Size appropriately

      // ---------------------------
      // NORMALIZE
      // ---------------------------

      // Convert denormalized rows to normalized persistence objects.
      LOGGER.info("NORMALIZE");
      int cntr = 0;
      final List<EsClientPerson> groupRecs = new ArrayList<>(50);
      for (EsClientPerson m : recs) {
        CheeseRay.logEvery(LOGGER, ++cntr, "Normalize", "recs");
        if (!lastId.equals(m.getNormalizationGroupKey()) && !groupRecs.isEmpty()) {
          results.add(rocket.normalizeSingle(groupRecs));
          groupRecs.clear();
        }

        groupRecs.add(m);
        lastId = m.getNormalizationGroupKey();
        if (lastId == null) {
          // Could be a data error (invalid data in db).
          LOGGER.warn("NULL Normalization Group Key: {}", m);
          lastId = new Object();
        }
      }

      if (!groupRecs.isEmpty()) {
        results.add(rocket.normalizeSingle(groupRecs));
      }

      groupRecs.clear();
      recs = new ArrayList<>(); // release memory
      NeutronThreadUtils.freeMemory();
      LOGGER.info("NORMALIZATION DONE, scan for limited access");

      // ---------------------------
      // CHECK LIMITED ACCESS
      // ---------------------------

      try (final Session session = rocket.getJobDao().grabSession()) {
        LOGGER.info("Grabbed session");
        txn = rocket.grabTransaction();
        LOGGER.info("Grabbed transaction");

        if (rocket.mustDeleteLimitedAccessRecords()) {
          LOGGER.info("OMIT LIMITED ACCESS RECORDS: count: {}", deletionResults.size());
          rocket.loadRecsForDeletion(entityClass, session,
              rocket.getFlightPlan().getOverrideLastRunStartTime(), deletionResults);
        }

        LOGGER.info("commit transaction, limited access check");
        txn.commit();
      } finally {
        // session goes out of scope
      }

      LOGGER.info("check access limitations");
      addAll(results);
      results = new ArrayList<>(); // release memory
      NeutronThreadUtils.freeMemory();

      // Remove sealed and sensitive, if not permitted to view them.
      if (!deletionResults.isEmpty()) {
        LOGGER.info("Delete limited access records");
        deletionResults.stream().sequential().forEach(normalized::remove);
      }

      // ---------------------------
      // INDEX DOCUMENTS
      // ---------------------------

      LOGGER.info("START INDEXER THREAD");
      for (Thread t : threads) {
        t.start();
      }

      LOGGER.info("Merge placement homes into client records and queue index");
      handleJdbcDone(range);
      doneThreadRetrieve();
      rocket.doneRetrieve();

      // free memory
      this.placementHomeAddresses.clear();
      this.normalized.clear();
      NeutronThreadUtils.freeMemory();

      LOGGER.info("Retrieval done, waiting on indexing");
      for (Thread t : threads) {
        LOGGER.info("Wait for indexer thread");
        t.join(120000); // safety, two minutes tops
      }

    } catch (Exception e) {
      rocket.fail();
      Thread.currentThread().interrupt();
      throw CheeseRay.runtime(LOGGER, e, "LAST CHANGE ERROR!: {}", e.getMessage());
    } finally {
      rocket.doneRetrieve();
    }

    LOGGER.info("PeopleSummaryLastChangeHandler.handleSecondaryJdbc: DONE");
  }

}
