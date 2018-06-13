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
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

public class PeopleSummaryLastChangeHandler extends PeopleSummaryThreadHandler {

  private static final long serialVersionUID = 1L;

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
      throw CheeseRay.runtime(LOGGER, e, "ERROR EXECUTING LAST CHANGE SQL! {}", e.getMessage());
    } finally {
      handleFinishRange(range);
      getRocket().getFlightLog().markRangeComplete(range);
      getRocket().doneRetrieve();
    }
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

    handleStartRange(range);

    Transaction txn = null;
    List<EsClientPerson> recs = null;
    int totalClientAddressRetrieved = 0;
    final int increment = 1000;

    try (final Session session = rocket.getJobDao().grabSession()) {
      final PreparedStatement stmtSelPlacementAddress =
          con.prepareStatement(ClientSQLResource.SELECT_PLACEMENT_ADDRESS);
      con = NeutronJdbcUtils.prepConnection(session);
      txn = rocket.grabTransaction();

      // STEP #1: Store changed client keys in GT_REFR_CLT and record the total inserted.
      LOGGER.info("STEP #1: Store changed client keys in GT_REFR_CLT");
      final int totalKeys =
          rocket.runInsertAllLastChangeKeys(session, lastRunTime, rocket.getPrepLastChangeSQLs());
      recs = new ArrayList<>(totalKeys * 4);
      LOGGER.info("total keys found: {}", totalKeys);

      // 1-1000, 1001-2000, 2001-3000, etc.
      for (int start = 1; start < totalKeys; start += increment) {
        final int end = start + increment - 1;
        LOGGER.info("STEP #2: CLEAR GT_ID");
        session.createNativeQuery("DELETE FROM GT_ID").executeUpdate();

        LOGGER.info("STEP #3: SELECT keys into GT_ID, bundle: start: {}, end: {}", start, end);
        rocket.runInsertRownumBundle(session, start, end, ClientSQLResource.INSERT_NEXT_BUNDLE);

        LOGGER.info("STEP #4: Pull from client address view");
        final NativeQuery<EsClientPerson> q = session.getNamedNativeQuery(queryName);
        NeutronJdbcUtils.optimizeQuery(q);

        try {
          { // scope brace
            final List<EsClientPerson> resultsClientAddress = q.list();
            recs.addAll(resultsClientAddress); // read from key bundle
            final int recsRetrievedThisBundle = resultsClientAddress.size();
            totalClientAddressRetrieved += recsRetrievedThisBundle;
            LOGGER.info("FOUND {} CLIENT ADDRESS RECORDS FOR BUNDLE: {} .. {}",
                recsRetrievedThisBundle, start, end);
          }

          session.flush();
          session.clear();

          LOGGER.info("STEP #5: pull placement homes.");
          readPlacementAddress(stmtSelPlacementAddress);
        } finally {
          // leave it
        }
      }

      // Release database resources.
      txn.commit(); // clear temp tables
    } catch (Exception e) {
      rocket.fail();
      if (txn.getStatus().canRollback()) {
        txn.rollback();
      }
      throw CheeseRay.runtime(LOGGER, e, "EXTRACT SQL ERROR!: {}", e.getMessage());
    } finally {
      // session goes out of scope
    }

    try {
      LOGGER.info("DATA RETRIEVAL DONE: client address: {}", totalClientAddressRetrieved);
      Object lastId = new Object();
      final List<ReplicatedClient> results = new ArrayList<>(recs.size()); // Size appropriately

      // ---------------------------
      // NORMALIZATION:
      // ---------------------------

      // Convert denormalized rows to normalized persistence objects.
      final List<EsClientPerson> groupRecs = new ArrayList<>(50);
      for (EsClientPerson m : recs) {
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

      // ---------------------------
      // NORMALIZATION DONE.
      // ---------------------------

      try (final Session session = rocket.getJobDao().grabSession()) {
        txn = rocket.grabTransaction();

        if (rocket.mustDeleteLimitedAccessRecords()) {
          LOGGER.info("OMIT LIMITED ACCESS RECORDS");
          rocket.loadRecsForDeletion(entityClass, session,
              rocket.getFlightPlan().getOverrideLastRunStartTime(), deletionResults);
        }

        txn.commit();
      } finally {
        if (txn.getStatus().canRollback()) {
          txn.rollback();
        }
      } // session goes out of scope

      groupRecs.clear();

      // Merge placement homes and index into Elasticsearch.
      handleJdbcDone(range);

    } catch (Exception e) {
      rocket.fail();
      throw CheeseRay.runtime(LOGGER, e, "CLIENT GROUPING ERROR!: {}", e.getMessage());
    } finally {
      // Override in multi-thread mode to avoid killing the indexer thread.
      rocket.doneRetrieve();
    }

  }

}
