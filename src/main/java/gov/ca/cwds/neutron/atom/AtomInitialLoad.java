package gov.ca.cwds.neutron.atom;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import javax.persistence.ParameterMode;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.RandomStringGenerator;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.procedure.ProcedureCall;
import org.slf4j.Logger;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicatedEntity;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.util.NeutronThreadUtils;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;

/**
 * Common functions and features for initial (full) load.
 * 
 * @author CWDS API Team
 *
 * @param <N> normalized type
 * @param <D> denormalized type
 */
public interface AtomInitialLoad<N extends PersistentObject, D extends ApiGroupNormalizer<?>>
    extends AtomHibernate<N, D>, AtomShared, AtomRocketControl {

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

  /**
   * "Work-around" (gentle euphemism for a <strong>HACK</strong>) for annoying condition where a
   * transaction should have started but did not.
   * 
   * <p>
   * Get the current transaction from the current session or start a new transaction.
   * </p>
   * 
   * @return current, active transaction
   */
  default Transaction grabTransaction() {
    Transaction txn = null;
    final Session session = getJobDao().getSessionFactory().getCurrentSession();
    try {
      txn = session.beginTransaction();
    } catch (Exception e) { // NOSONAR
      txn = session.getTransaction();
    }
    return txn;
  }

  default int nextThreadNumber() {
    return 1;
  }

  /**
   * Process results sets from {@link #pullRange(Pair)}.
   * 
   * @param rs result set for this key range
   * @throws SQLException on database error
   */
  default void handleRangeResults(final ResultSet rs) throws SQLException {
    // Provide your own solution, for now.
  }

  default void handleCustomJdbc(final Connection con, Pair<String, String> range)
      throws SQLException {
    // Default is no-op.
  }

  /**
   * Read records from the given key range, typically within a single partition on large tables.
   * 
   * @param p partition range to read
   */
  default void pullRange(final Pair<String, String> p) {
    final String threadName =
        "extract_" + nextThreadNumber() + "_" + p.getLeft() + "_" + p.getRight();
    nameThread(threadName);
    final Logger log = getLogger();
    log.info("BEGIN: extract thread {}", threadName);
    getFlightLog().markRangeStart(p);

    try (Connection con = getJobDao().getSessionFactory().getSessionFactoryOptions()
        .getServiceRegistry().getService(ConnectionProvider.class).getConnection()) {
      con.setSchema(getDBSchemaName());
      con.setAutoCommit(false);

      final String query = getInitialLoadQuery(getDBSchemaName()).replaceAll(":fromId", p.getLeft())
          .replaceAll(":toId", p.getRight());
      log.info("query: {}", query);
      NeutronDB2Utils.enableParallelism(con);

      try (Statement stmt = con.createStatement()) {
        // Handle additional JDBC statements, if any.
        handleCustomJdbc(con, p);

        stmt.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue()); // faster
        stmt.setMaxRows(0);
        stmt.setQueryTimeout(0);
        handleRangeResults(stmt.executeQuery(query));

        con.commit();
      } catch (Exception e) {
        con.rollback();
        throw e;
      }

      log.info("RANGE COMPLETED SUCCESSFULLY! {}-{}", p.getLeft(), p.getRight());
    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(log, e, "FAILED TO PULL RANGE! {}-{} : {}", p.getLeft(), p.getRight(),
          e.getMessage());
    } finally {
      getFlightLog().markRangeComplete(p);
      nameThread(new RandomStringGenerator.Builder().withinRange('a', 'z').build().generate(10));
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
      final List<Pair<String, String>> ranges = getPartitionRanges();
      log.info(">>>>>>>> # OF RANGES: {} <<<<<<<<", ranges);
      final List<ForkJoinTask<?>> tasks = new ArrayList<>(ranges.size());
      final ForkJoinPool threadPool =
          new ForkJoinPool(NeutronThreadUtils.calcReaderThreads(getFlightPlan()));

      // Queue execution.
      for (Pair<String, String> p : ranges) {
        tasks.add(threadPool.submit(() -> pullRange(p)));
      }

      // Join threads. Don't return from method until they complete.
      for (ForkJoinTask<?> task : tasks) {
        task.get();
      }

    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(log, e, "ERROR IN MULTI-THREAD JDBC! {}", e.getMessage());
    } finally {
      doneRetrieve();
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
    if (getFlightPlan().isRefreshMqt() && StringUtils.isNotBlank(getMQTName())) {
      log.warn("REFRESH MQT!");
      final Session session = getJobDao().getSessionFactory().getCurrentSession();
      grabTransaction(); // HACK
      final String schema =
          (String) session.getSessionFactory().getProperties().get("hibernate.default_schema");

      final ProcedureCall proc = session.createStoredProcedureCall(schema + ".SPREFRSMQT");
      proc.registerStoredProcedureParameter("MQTNAME", String.class, ParameterMode.IN);
      proc.registerStoredProcedureParameter("RETSTATUS", String.class, ParameterMode.OUT);
      proc.registerStoredProcedureParameter("RETMESSAG", String.class, ParameterMode.OUT);

      proc.setParameter("MQTNAME", getMQTName());
      proc.execute();

      final String returnStatus = (String) proc.getOutputParameterValue("RETSTATUS");
      final String returnMsg = (String) proc.getOutputParameterValue("RETMESSAG");
      log.info("refresh MQT proc: status: {}, msg: {}", returnStatus, returnMsg);

      if (returnStatus.charAt(0) != '0') {
        CheeseRay.runtime(log, "MQT REFRESH ERROR! {}", returnMsg);
      }
    }
  }

}
