package gov.ca.cwds.jobs.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.jdbc.Work;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.neutron.atom.AtomHibernate;
import gov.ca.cwds.neutron.atom.AtomLoadEventHandler;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

/**
 * Execute arbitrary SELECT statements, typically for last change runs. Lets us reuse JDBC handlers
 * from initial load in last change mode without changing the architecture when switching between
 * Hibernate and raw JDBC.
 * 
 * <p>
 * Examples include one or more SELECT statements after insert keys into a global temporary table.
 * </p>
 * 
 * @author CWDS API Team
 * @param <T> persistence type
 */
public class WorkSecondaryResults<T extends PersistentObject> implements Work {

  private static final ConditionalLogger LOGGER = new JetPackLogger(WorkSecondaryResults.class);

  private final Date lastRunTime;
  private final Function<Connection, PreparedStatement> prepStmtMaker;
  private final AtomLoadEventHandler<T> handler;

  /**
   * Constructor.
   * 
   * @param lastRunTime last successful run time
   * @param prepStmtMaker Function to produce prepared statement
   * @see AtomHibernate#getPreparedStatementMaker(String)
   */
  public WorkSecondaryResults(Date lastRunTime,
      final Function<Connection, PreparedStatement> prepStmtMaker,
      AtomLoadEventHandler<T> handler) {
    this.lastRunTime = lastRunTime != null ? new Date(lastRunTime.getTime()) : null;
    this.prepStmtMaker = prepStmtMaker;
    this.handler = handler;
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
    con.setSchema(NeutronJdbcUtils.getDBSchemaName());
    con.setAutoCommit(false);
    NeutronDB2Utils.enableParallelism(con);

    final String strLastRunTime = NeutronJdbcUtils.makeTimestampStringLookBack(lastRunTime);
    LOGGER.info("strLastRunTime: {}", strLastRunTime);

    try (final PreparedStatement stmt = createPreparedStatement(con)) {
      handler.eventHandleSecondaryJdbc(con, Pair.<String, String>of("a", "b"));
    }
  }

}
