package gov.ca.cwds.neutron.atom;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import gov.ca.cwds.data.persistence.PersistentObject;

/**
 * Interface defines load steps, primarily for initial (full) mode. In last change mode, the range
 * is arbitrary (like 'a' - 'b'), since range only applies to initial mode.
 * 
 * @author CWDS API Team
 * @param <N> normalized type
 */
public interface AtomLoadEventHandler<N extends PersistentObject> {

  /**
   * Process results sets from {@link #pullRange(Pair)}. Default implementation is no-op.
   * 
   * @param rs result set for this key range
   * @throws SQLException on database error
   */
  default void eventHandleMainResults(final ResultSet rs) throws SQLException {
    // Provide your own solution, for now.
  }

  /**
   * Execute arbitrary JDBC as needed on the same connection.
   * 
   * @param con database connection
   * @param range key range
   * @throws SQLException on database error
   */
  default void eventHandleSecondaryJdbc(final Connection con, Pair<String, String> range)
      throws SQLException {
    // Default is no-op.
  }

  /**
   * Begin step, before initial load key range processing begins. Allocate resources. Default
   * implementation is no-op.
   * 
   * @param range key range
   */
  default void eventStartRange(final Pair<String, String> range) {
    // Default is no-op.
  }

  /**
   * Terminal step, after after initial load key range processing completes. De-allocate resources.
   * Default implementation is no-op.
   * 
   * @param range key range
   */
  default void eventFinishRange(final Pair<String, String> range) {
    // Default is no-op.
  }

  /**
   * Intermediate step, after {@link Connection#commit()} and before
   * {@link #eventFinishRange(Pair)}. Process data, such as normalization. Default implementation is
   * no-op.
   * 
   * @param range key range
   */
  default void eventJdbcDone(final Pair<String, String> range) {
    // Default is no-op.
  }

  default List<N> getResults() {
    return new ArrayList<>();
  }

}
