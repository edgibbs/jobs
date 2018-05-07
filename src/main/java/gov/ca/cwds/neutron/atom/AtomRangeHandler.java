package gov.ca.cwds.neutron.atom;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.tuple.Pair;

public interface AtomRangeHandler {

  /**
   * Process results sets from {@link #pullRange(Pair)}.
   * 
   * @param rs result set for this key range
   * @throws SQLException on database error
   */
  default void handleRangeResults(final ResultSet rs) throws SQLException {
    // Provide your own solution, for now.
  }

  default void handleSecondaryJdbc(final Connection con, Pair<String, String> range)
      throws SQLException {
    // Default is no-op.
  }

  default void beforeRange(final Pair<String, String> p) {
    // Default is no-op.
  }

  default void afterRange(final Pair<String, String> p) {
    // Default is no-op.
  }

  default void afterReads(final Pair<String, String> p) {
    // Default is no-op.
  }

}
