package gov.ca.cwds.jobs.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.jdbc.Work;

import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

/**
 * Execute DML prior to retrieving records, typically for last change runs.
 * 
 * <p>
 * Examples include populating a global temporary table prior to reading from a view.
 * </p>
 * 
 * @author CWDS API Team
 */
public class WorkPrepareLastChange implements Work {

  private static final ConditionalLogger LOGGER = new JetPackLogger(WorkPrepareLastChange.class);

  private final Date lastRunTime;
  private final String sql;
  private final Function<Connection, PreparedStatement> prepStmtMaker;

  /**
   * Constructor.
   * 
   * @param lastRunTime last successful run time
   * @param sql SQL to run
   * @param prepStmtMaker Function to produce prepared statement
   */
  public WorkPrepareLastChange(Date lastRunTime, String sql,
      final Function<Connection, PreparedStatement> prepStmtMaker) {
    this.lastRunTime = lastRunTime != null ? new Date(lastRunTime.getTime()) : null;
    this.sql = sql;
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
   * <p>
   * <strong>Beware</strong> that DB2 may not optimize a PreparedStatement the same as regular SQL.
   * </p>
   * 
   * @param con current database connection
   */
  @Override
  public void execute(Connection con) throws SQLException {
    con.setSchema(NeutronJdbcUtils.getDBSchemaName());
    con.setAutoCommit(false);
    NeutronDB2Utils.enableParallelism(con);

    final String strLastRunTime = NeutronJdbcUtils.makeSimpleTimestampString(lastRunTime);
    try (final PreparedStatement stmt = createPreparedStatement(con)) {
      for (int i = 1; i <= StringUtils.countMatches(sql, "?"); i++) {
        stmt.setString(i, strLastRunTime); // String or Timestamp, that is the question.
      }

      LOGGER.debug("Find keys new/changed since {}", strLastRunTime);
      final int cntNewChanged = stmt.executeUpdate();
      LOGGER.info("Total keys {} changed since {}", cntNewChanged, strLastRunTime);
    }
  }

}
