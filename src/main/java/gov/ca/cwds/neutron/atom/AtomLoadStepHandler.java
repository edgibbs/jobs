package gov.ca.cwds.neutron.atom;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import gov.ca.cwds.data.persistence.PersistentObject;

/**
 * Interface defines load steps, primarily for initial (full) mode. In last change mode, the range
 * is arbitrary (like 'a' - 'b'), since range only applies to initial mode.
 * 
 * @author CWDS API Team
 * @param <N> persistent, normalized type
 */
public interface AtomLoadStepHandler<N extends PersistentObject> {

  /**
   * Process results sets from the main, original query (view or MQT). Default implementation is
   * no-op.
   * 
   * @param rs result set for this key range
   * @param con raw JDBC connection
   * @throws SQLException on database error
   */
  default void handleMainResults(final ResultSet rs, Connection con) throws SQLException {
    // Provide your own solution, for now.
  }

  /**
   * Execute arbitrary JDBC as needed on the same connection.
   * 
   * @param con database connection
   * @param range key range
   * @throws SQLException on database error
   */
  default void handleSecondaryJdbc(final Connection con, Pair<String, String> range)
      throws SQLException {
    // Default is no-op.
  }

  /**
   * Begin step, before initial load key range processing begins. Allocate resources. Default
   * implementation is no-op.
   * 
   * @param range key range
   */
  default void handleStartRange(final Pair<String, String> range) {
    // Default is no-op.
  }

  /**
   * Terminal step, after after initial load key range processing completes. De-allocate resources.
   * Default implementation is no-op.
   * 
   * @param range key range
   */
  default void handleFinishRange(final Pair<String, String> range) {
    // Default is no-op.
  }

  /**
   * Intermediate step, after {@link Connection#commit()} and before
   * {@link #handleFinishRange(Pair)}. Process data, such as normalization. Default implementation
   * is no-op.
   * 
   * @param range key range
   */
  default void handleJdbcDone(final Pair<String, String> range) {
    // Default is no-op.
  }

  /**
   * Return the handler's results.
   * 
   * @return clean, normalized results
   */
  default List<N> getResults() {
    return new ArrayList<>();
  }

  /**
   * Retrieve records from Hibernate, JDBC, or whatever.
   * 
   * @param lastRunDate last successful run date-time
   * @param deletionResults sensitive records to delete, if any
   * @return list of normalized records, ready for Elasticsearch
   */
  default List<N> fetchLastRunNormalizedResults(final Date lastRunDate,
      final Set<String> deletionResults) {
    return new ArrayList<>();
  }

  default String getEventType() {
    return "";
  }

}
