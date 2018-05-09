package gov.ca.cwds.jobs.util.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.jdbc.Work;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.neutron.atom.AtomLoadEventHandler;
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

  private final AtomLoadEventHandler<T> handler;

  /**
   * Constructor.
   * 
   * @param handler results handler
   */
  public WorkSecondaryResults(AtomLoadEventHandler<T> handler) {
    this.handler = handler;
  }

  /**
   * Call {@link AtomLoadEventHandler#eventHandleSecondaryJdbc(Connection, Pair)}.
   * 
   * @param con current database connection
   */
  @Override
  public void execute(Connection con) throws SQLException {
    con.setSchema(NeutronJdbcUtils.getDBSchemaName());
    con.setAutoCommit(false);
    NeutronDB2Utils.enableParallelism(con);
    handler.eventHandleSecondaryJdbc(con, Pair.<String, String>of("a", "b"));
  }

}
