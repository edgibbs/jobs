package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.function.Function;

import org.hibernate.jdbc.Work;

/**
 * Abstract Hibernate {@link Work} tracks total records processed.
 * 
 * @author CWDS API Team
 */
public abstract class NeutronWorkTotalImpl implements NeutronWorkTotal {

  private final Function<Connection, PreparedStatement> prepStmtMaker;
  private int totalProcessed = 0;

  public NeutronWorkTotalImpl(Function<Connection, PreparedStatement> prepStmtMaker) {
    this.prepStmtMaker = prepStmtMaker;
  }

  /**
   * {@inheritDoc}
   * 
   * @see gov.ca.cwds.neutron.util.jdbc.NeutronWorkTotal#getTotalProcessed()
   */
  @Override
  public int getTotalProcessed() {
    return totalProcessed;
  }

  protected void setTotalProcessed(int totalProcessed) {
    this.totalProcessed = totalProcessed;
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
