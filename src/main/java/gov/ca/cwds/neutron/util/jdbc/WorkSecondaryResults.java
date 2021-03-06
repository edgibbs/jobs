package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang3.tuple.Pair;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.neutron.atom.AtomLoadStepHandler;

/**
 * Execute arbitrary SQL statements. Allows reuse of JDBC and Hibernate handlers when switching
 * between Hibernate queries and raw JDBC without major architectural changes.
 * 
 * <p>
 * Examples include inserting keys into a global temporary table and executing one or more SELECT
 * statements.
 * </p>
 * 
 * @author CWDS API Team
 * @param <T> persistence type
 */
public class WorkSecondaryResults<T extends PersistentObject> extends NeutronWorkConnectionStealer {

  private final AtomLoadStepHandler<T> handler;

  /**
   * Constructor.
   * 
   * @param handler results handler
   */
  public WorkSecondaryResults(AtomLoadStepHandler<T> handler) {
    this.handler = handler;
  }

  /**
   * Call {@link AtomLoadStepHandler#handleSecondaryJdbc(Connection, Pair)}.
   * 
   * @param con current database connection
   */
  @Override
  public void execute(Connection con) throws SQLException {
    setConnection(con);
    NeutronJdbcUtils.enableBatchSettings(con);
    handler.handleSecondaryJdbc(con, Pair.<String, String>of("a", "b"));
  }

}
