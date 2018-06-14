package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils;

/**
 * Execute DML prior to retrieving records, typically for last change runs.
 * 
 * <p>
 * Examples include populating a global temporary table prior to reading from a view.
 * </p>
 * 
 * @author CWDS API Team
 */
public class WorkPrepareLastChange extends NeutronWorkTotalImpl {

  private static final ConditionalLogger LOGGER = new JetPackLogger(WorkPrepareLastChange.class);

  private final Date lastRunTime;
  private final String sql;

  /**
   * Constructor.
   * 
   * @param lastRunTime last successful run time
   * @param sql SQL to run
   * @param prepStmtMaker Function to produce prepared statement
   */
  public WorkPrepareLastChange(Date lastRunTime, String sql,
      final Function<Connection, PreparedStatement> prepStmtMaker) {
    super(prepStmtMaker);
    this.lastRunTime = lastRunTime != null ? new Date(lastRunTime.getTime()) : null;
    this.sql = sql;
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
    NeutronJdbcUtils.enableBatchSettings(con);

    final String strLastRunTime = NeutronDateUtils.makeTimestampStringLookBack(lastRunTime);
    LOGGER.info("strLastRunTime: {}", strLastRunTime);

    try (final PreparedStatement stmt = createPreparedStatement(con)) {
      for (int i = 1; i <= StringUtils.countMatches(sql, "?"); i++) {
        stmt.setString(i, strLastRunTime);
      }

      LOGGER.info("Find keys changed since {}", strLastRunTime);
      setTotalProcessed(stmt.executeUpdate());
      LOGGER.info("Total keys {} changed since {}", getTotalProcessed(), strLastRunTime);
    }
  }

}
