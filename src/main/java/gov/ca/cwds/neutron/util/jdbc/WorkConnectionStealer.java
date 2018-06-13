package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.jdbc.Work;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.neutron.atom.AtomLoadStepHandler;

/**
 * Steal a session's connection and make it available to the caller.
 * 
 * @author CWDS API Team
 * @param <T> persistence type
 */
public class WorkConnectionStealer<T extends PersistentObject> implements Work {

  private Connection conn;

  /**
   * Constructor.
   * 
   * @param handler results handler
   */
  public WorkConnectionStealer() {}

  /**
   * Call {@link AtomLoadStepHandler#handleSecondaryJdbc(Connection, Pair)}.
   * 
   * @param con current database connection
   */
  @Override
  public void execute(Connection con) throws SQLException {
    conn = con;
    NeutronDB2Utils.enableBatchSettings(con);
  }

  public Connection getConn() {
    return conn;
  }

  public void setConn(Connection conn) {
    this.conn = conn;
  }

}
