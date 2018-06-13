package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.function.Function;

public abstract class NeutronWorkTotalImpl implements NeutronWorkTotal {

  private final Function<Connection, PreparedStatement> prepStmtMaker;
  private int totalProcessed = 0;

  public NeutronWorkTotalImpl(Function<Connection, PreparedStatement> prepStmtMaker) {
    this.prepStmtMaker = prepStmtMaker;
  }

  /**
   * {@inheritDoc}
   * 
   * @see gov.ca.cwds.neutron.util.jdbc.NeutronWorkTotal#getTotalInserted()
   */
  @Override
  public int getTotalProcessed() {
    return totalProcessed;
  }

  protected void setTotalProcessed(int totalInserted) {
    this.totalProcessed = totalInserted;
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
