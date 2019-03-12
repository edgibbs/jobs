package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.rocket.ClientSQLResource;

/**
 * Execute DML prior to retrieving records, typically for last change runs.
 * 
 * <p>
 * Examples include populating a global temporary table prior to reading from a view.
 * </p>
 * 
 * @author CWDS API Team
 */
public class WorkPrepareWhatChanged extends NeutronWorkTotalImpl {

  private static final ConditionalLogger LOGGER = new JetPackLogger(WorkPrepareWhatChanged.class);

  private final Timestamp startTs;
  private final Timestamp endTs;
  private final int runId;
  private final String sql;

  /**
   * Constructor.
   * 
   * @param startDate start of date range
   * @param endDate end of date range
   * @param runId run id from DB2 sequence
   * @param sql SQL to run
   * @param prepStmtMaker Function to produce prepared statement
   */
  public WorkPrepareWhatChanged(Date startDate, Date endDate, int runId, String sql,
      final Function<Connection, PreparedStatement> prepStmtMaker) {
    super(prepStmtMaker);

    Objects.requireNonNull(startDate, "Start date is required");
    Objects.requireNonNull(endDate, "End date is required");

    this.startTs = new Timestamp(startDate.getTime());
    this.endTs = new Timestamp(endDate.getTime());
    this.runId = runId;
    this.sql = sql;
  }

  /**
   * Set parameters and execute the Prepared Statement.
   * 
   * <p>
   * <strong>WARNING!</strong>. DB2 may not optimize prepared statements in the same way as dynamic
   * SQL and vice versa.
   * </p>
   * 
   * @param con database connection
   * @see ClientSQLResource#INS_LST_CHG_ALL_DYNAMIC
   */
  @Override
  public void execute(Connection con) throws SQLException {
    NeutronJdbcUtils.enableBatchSettings(con);

    try (final PreparedStatement stmt = createPreparedStatement(con)) {
      stmt.setInt(1, runId);
      final int totalParams = StringUtils.countMatches(sql, "?") - 1;
      for (int i = 2; i <= totalParams; i++) {
        stmt.setTimestamp(i++, startTs);
        stmt.setTimestamp(i, endTs);
      }

      LOGGER.info("Find keys changed since {}", startTs);
      setTotalProcessed(stmt.executeUpdate());
      LOGGER.info("Total keys {} changed since {}", getTotalProcessed(), startTs);
    }
  }

  public Timestamp getStartTs() {
    return startTs;
  }

  public Timestamp getEndTs() {
    return endTs;
  }

  public String getSql() {
    return sql;
  }

  public int getRunId() {
    return runId;
  }

}
