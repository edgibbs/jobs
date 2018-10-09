package gov.ca.cwds.neutron.atom;

import static gov.ca.cwds.neutron.enums.NeutronIntegerDefaults.FETCH_SIZE;
import static gov.ca.cwds.neutron.enums.NeutronIntegerDefaults.QUERY_TIMEOUT_IN_SECONDS;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import javax.persistence.ParameterMode;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.hibernate.procedure.ProcedureCall;
import org.slf4j.Logger;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicatedEntity;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.util.NeutronThreadUtils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

/**
 * Common functions and features for initial (full) load.
 * 
 * @author CWDS API Team
 * @param <N> normalized type
 * @param <D> denormalized type
 */
public interface AtomInitialLoad<N extends PersistentObject, D extends ApiGroupNormalizer<?>>
    extends AtomHibernate<N, D>, AtomShared, AtomRocketControl, AtomLoadStepHandler<N> {

  /**
   * Restrict initial load key ranges from flight plan (command line).
   * 
   * @param allKeyPairs all key ranges for this rocket
   * @return list of key pairs to execute
   */
  default List<Pair<String, String>> limitRange(final List<Pair<String, String>> allKeyPairs) {
    List<Pair<String, String>> ret;
    final FlightPlan flightPlan = getFlightPlan();
    if (flightPlan != null && flightPlan.isRangeGiven()) {
      final List<Pair<String, String>> list = new ArrayList<>(allKeyPairs.size());

      final int start = ((int) flightPlan.getStartBucket()) - 1;
      final int end = ((int) flightPlan.getEndBucket()) - 1;

      for (int i = start; i <= end; i++) {
        list.add(allKeyPairs.get(i));
      }

      ret = list;
    } else {
      ret = allKeyPairs;
    }

    return ret;
  }

  /**
   * @return true if the rocket provides its own key ranges
   */
  default boolean isInitialLoadJdbc() {
    return false;
  }

  /**
   * Get the view or materialized query table name, if used. Any child classes relying on a
   * denormalized view must define the name.
   * 
   * @return name of view or materialized query table or null if none
   */
  default String getInitialLoadViewName() {
    return null;
  }

  /**
   * Get initial load SQL query.
   * 
   * @param dbSchemaName The DB schema name
   * @return Initial load query
   */
  default String getInitialLoadQuery(String dbSchemaName) {
    return null;
  }

  /**
   * Mark a record for deletion. Intended for replicated records with deleted flag.
   * 
   * @param t bean to check
   * @return true if marked for deletion
   */
  default boolean isDelete(N t) {
    return t instanceof CmsReplicatedEntity ? CmsReplicatedEntity.isDelete((CmsReplicatedEntity) t)
        : false;
  }

  default int nextThreadNumber() {
    return 1;
  }

  /**
   * Read records from the given key range, typically within a single partition on large tables.
   * Default prep query for initial load takes the form
   * {@code WHERE X.CLT_IDENTIFIER BETWEEN ':fromId' AND ':toId'}.
   * 
   * <p>
   * Pass optional SQL to execute to prepare
   * </p>
   * 
   * @param range partition range to read
   * @param sql optional SQL statement
   * @return List of persistent type N
   * 
   * @see AtomLoadStepHandler#handleStartRange(Pair)
   * @see AtomLoadStepHandler#handleJdbcDone(Pair)
   * @see AtomLoadStepHandler#handleFinishRange(Pair)
   */
  default List<N> pullRange(final Pair<String, String> range, String sql) {
    final String origThreadName = Thread.currentThread().getName();
    final Logger log = getLogger();
    final FlightLog flightLog = getFlightLog();

    final String threadName =
        "extract_" + nextThreadNumber() + "_" + range.getLeft() + "_" + range.getRight();
    nameThread(threadName);

    log.info("BEGIN: extract thread {}", threadName);
    flightLog.markRangeStart(range);
    handleStartRange(range);

    final String query = StringUtils.isNotBlank(sql) ? sql.trim()
        : getInitialLoadQuery(getDBSchemaName()).replaceAll(":fromId", range.getLeft())
            .replaceAll(":toId", range.getRight());
    log.info("query: {}", query);

    // SNAP-709: Connection is closed. ERRORCODE=-4470, SQLSTATE=08003.
    try {
      Connection con = null;
      try (final Session session = getJobDao().grabSession()) { // Auto-close statement.
        con = NeutronJdbcUtils.prepConnection(session);
        con.commit(); // cleanup

        try (final Statement stmt = con.createStatement()) {
          stmt.setMaxRows(0);
          stmt.setQueryTimeout(QUERY_TIMEOUT_IN_SECONDS.getValue()); // SNAP-709
          stmt.setFetchSize(FETCH_SIZE.getValue());

          try (final ResultSet rs = stmt.executeQuery(query)) {
            handleMainResults(rs);
          } finally {
            // Close result set.
          }
        } finally {
          // Close statement.
        }

        // Handle additional JDBC statements, if any.
        handleSecondaryJdbc(con, range);
        con.commit(); // Clear temp tables
      } catch (Exception e) {
        try {
          con.rollback();
        } catch (Exception e2) {
          log.trace("NESTED EXCEPTION", e2);
        }
        throw e;
      } finally {
        // Close statement, connection, and session.
      }

      // Done reading data. Process data, like cleansing and normalizing.
      handleJdbcDone(range);
      log.info("RANGE COMPLETED SUCCESSFULLY! {}-{}", range.getLeft(), range.getRight());
      return getResults();
    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(log, e, "RANGE FAILED! {}-{} : {}", range.getLeft(), range.getRight(),
          e.getMessage());
    } finally {
      handleFinishRange(range);
      flightLog.markRangeComplete(range);
      nameThread(origThreadName);
    }
  }

  /**
   * Return partition keys for initial load. Supports native named query, "findPartitionedBuckets".
   * 
   * @return list of partition key pairs
   * @throws NeutronCheckedException on parse or dynamic error
   */
  default List<Pair<String, String>> getPartitionRanges() throws NeutronCheckedException {
    return new ArrayList<>();
  }

  default void doneMultiThreadRetrieve() {
    doneRetrieve();
  }

  default void startMultiThreadRetrieve() {
    // Implement marker.
  }

  /**
   * The "extract" part of ETL. Single producer, chained consumers. This rocket normalizes
   * <strong>without the transform thread</strong>.
   */
  default void pullMultiThreadJdbc() {
    nameThread("extract_main");
    final Logger log = getLogger();
    log.info("BEGIN: main extract thread");
    doneTransform(); // no transform/normalize thread

    try {
      startMultiThreadRetrieve();
      final List<Pair<String, String>> ranges = getPartitionRanges();
      log.info(">>>>>>>> # OF RANGES: {} <<<<<<<<", ranges);
      final List<ForkJoinTask<?>> tasks = new ArrayList<>(ranges.size());
      final ForkJoinPool threadPool =
          new ForkJoinPool(NeutronThreadUtils.calcReaderThreads(getFlightPlan()));

      // Queue range threads.
      for (Pair<String, String> p : ranges) {
        tasks.add(threadPool.submit(() -> pullRange(p, null)));
      }

      // Join threads. Don't let this method return, until all range threads finish.
      for (ForkJoinTask<?> task : tasks) {
        task.get();
      }

    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(log, e, "MULTI-THREAD JDBC ERROR! {}", e.getMessage());
    } finally {
      doneMultiThreadRetrieve();
    }

    log.info("DONE: main extract thread");
  }

  /**
   * Source Materialized Query Table to be refreshed before running initial load. Defaults to null,
   * meaning that an MQT does not apply.
   * 
   * @return MQT name or null if none
   */
  default String getMQTName() {
    return null;
  }

  /**
   * Refresh DB2 materialized query tables (MQT) by calling a stored procedure.
   */
  default void refreshMQT() {
    final Logger log = getLogger();
    final String mqt = getMQTName();
    if (getFlightPlan().isRefreshMqt() && StringUtils.isNotBlank(mqt)) {
      log.warn("REFRESH MQT!");
      final Session session = getJobDao().grabSession();
      grabTransaction();
      final String schema =
          (String) session.getSessionFactory().getProperties().get("hibernate.default_schema");

      final ProcedureCall proc = session.createStoredProcedureCall(schema + ".SPREFRSMQT");
      proc.registerStoredProcedureParameter("MQTNAME", String.class, ParameterMode.IN);
      proc.registerStoredProcedureParameter("RETSTATUS", String.class, ParameterMode.OUT);
      proc.registerStoredProcedureParameter("RETMESSAG", String.class, ParameterMode.OUT);

      proc.setParameter("MQTNAME", mqt);
      proc.execute();

      final String returnStatus = (String) proc.getOutputParameterValue("RETSTATUS");
      final String returnMsg = (String) proc.getOutputParameterValue("RETMESSAG");
      log.info("refresh MQT proc: status: {}, msg: {}", returnStatus, returnMsg);

      if (returnStatus.charAt(0) != '0') {
        throw CheeseRay.runtime(log, "MQT REFRESH ERROR! {}", returnMsg);
      }
    }
  }

}
