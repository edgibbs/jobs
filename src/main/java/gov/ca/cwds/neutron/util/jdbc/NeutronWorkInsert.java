package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.function.Function;

import org.hibernate.jdbc.Work;

public abstract class NeutronWorkInsert implements Work {

  private final Function<Connection, PreparedStatement> prepStmtMaker;
  private int totalInserted = 0;

  public NeutronWorkInsert(Function<Connection, PreparedStatement> prepStmtMaker) {
    this.prepStmtMaker = prepStmtMaker;
  }

  public int getTotalInserted() {
    return totalInserted;
  }

  protected void setTotalInserted(int totalInserted) {
    this.totalInserted = totalInserted;
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

}
