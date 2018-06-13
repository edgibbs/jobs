package gov.ca.cwds.neutron.rocket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang3.tuple.Pair;

import gov.ca.cwds.jobs.ClientPersonIndexerJob;
import gov.ca.cwds.neutron.jetpack.CheeseRay;

public class PeopleSummaryLastChangeHandler extends PeopleSummaryThreadHandler {

  private static final long serialVersionUID = 1L;

  public PeopleSummaryLastChangeHandler(ClientPersonIndexerJob rocket) {
    super(rocket);
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Read placement home addresses per rule R-02294, Client Abstract Most Recent Address.
   * </p>
   */
  @Override
  public void handleSecondaryJdbc(Connection con, Pair<String, String> range) throws SQLException {
    try (final PreparedStatement stmtSelPlacementAddress =
        con.prepareStatement(ClientSQLResource.SELECT_PLACEMENT_ADDRESS)) {
      readPlacementAddress(stmtSelPlacementAddress);
    } catch (Exception e) {
      con.rollback();
      throw CheeseRay.runtime(LOGGER, e, "SECONDARY JDBC FAILED! {}", e.getMessage(), e);
    }
  }

}
