package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Function;

import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;

/**
 * Execute DML prior to retrieving records by integer range, typically for last change runs.
 * 
 * <p>
 * Examples include populating a global temporary table prior to reading from a view.
 * </p>
 * 
 * @author CWDS API Team
 */
public class WorkPrepareRownumBundle extends NeutronWorkInsert {

  private static final ConditionalLogger LOGGER = new JetPackLogger(WorkPrepareRownumBundle.class);

  private final int start;
  private final int end;

  /**
   * Constructor.
   * 
   * @param start beginning of range
   * @param end end of range
   * @param prepStmtMaker Function to produce prepared statement
   */
  public WorkPrepareRownumBundle(int start, int end,
      final Function<Connection, PreparedStatement> prepStmtMaker) {
    super(prepStmtMaker);
    this.start = start;
    this.end = end;
  }

  /**
   * Execute the PreparedStatement.
   * 
   * @param con current database connection
   */
  @Override
  public void execute(Connection con) throws SQLException {
    NeutronDB2Utils.enableBatchSettings(con);

    try (final PreparedStatement stmt = createPreparedStatement(con)) {
      stmt.setInt(1, start);
      stmt.setInt(2, end);

      setTotalInserted(stmt.executeUpdate());
      LOGGER.info("Total keys {} inserted", getTotalInserted());
    }
  }

}
