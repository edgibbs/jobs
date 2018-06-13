package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.jdbc.Work;

import gov.ca.cwds.neutron.atom.AtomLoadStepHandler;

/**
 * Steal a connection from a Hibernate session and make it available to the caller.
 * 
 * @author CWDS API Team
 */
public class NeutronWorkConnectionStealer implements Work {

  private Connection conn;

  /**
   * Constructor.
   * 
   * @param handler results handler
   */
  public NeutronWorkConnectionStealer() {}

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

  public Connection getConnection() {
    return conn;
  }

  public void setConnection(Connection conn) {
    this.conn = conn;
  }

}
