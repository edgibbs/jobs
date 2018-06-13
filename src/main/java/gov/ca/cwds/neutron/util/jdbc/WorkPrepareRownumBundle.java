package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Function;

import org.hibernate.jdbc.Work;

import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;

/**
 * Execute DML prior to retrieving records, typically for last change runs.
 * 
 * <p>
 * Examples include populating a global temporary table prior to reading from a view.
 * </p>
 * 
 * @author CWDS API Team
 */
public class WorkPrepareRownumBundle implements Work {

  private static final ConditionalLogger LOGGER = new JetPackLogger(WorkPrepareRownumBundle.class);

  private final String sql;
  private final int start;
  private final int end;
  private final Function<Connection, PreparedStatement> prepStmtMaker;

  /**
   * Constructor.
   * 
   * @param lastRunTime last successful run time
   * @param sql SQL to run
   * @param prepStmtMaker Function to produce prepared statement
   */
  public WorkPrepareRownumBundle(String sql, int start, int end,
      final Function<Connection, PreparedStatement> prepStmtMaker) {
    this.sql = sql;
    this.start = start;
    this.end = end;
    this.prepStmtMaker = prepStmtMaker;
  }

  /**
   * Apply the {@link #prepStmtMaker} function to the connection.
   * 
   * @param con current database connection
   * @return prepared statement
   */
  protected PreparedStatement createPreparedStatement(Connection con) {
    return prepStmtMaker.apply(con);
  }

  /**
   * Execute the PreparedStatement.
   * 
   * <p>
   * <strong>WARNING!</strong>. DB2 may not optimize a PreparedStatement the same as regular SQL.
   * </p>
   * 
   * @param con current database connection
   */
  @Override
  public void execute(Connection con) throws SQLException {
    NeutronDB2Utils.enableBatchSettings(con);

    try (final PreparedStatement stmt = createPreparedStatement(con)) {
      stmt.setInt(1, start);
      stmt.setInt(2, end);

      final int cntNewChanged = stmt.executeUpdate();
      LOGGER.info("Total keys {} inserted", cntNewChanged);
    }
  }

}
