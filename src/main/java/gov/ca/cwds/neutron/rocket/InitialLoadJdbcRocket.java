package gov.ca.cwds.neutron.rocket;

import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import java.sql.Connection;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.data.BaseDaoImpl;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

public abstract class InitialLoadJdbcRocket<T extends PersistentObject, M extends ApiGroupNormalizer<?>>
    extends BasePersonRocket<T, M> {

  private static final long serialVersionUID = 1L;

  /**
   *
   * @param dao
   * @param esDao
   * @param lastRunFile
   * @param mapper
   * @param flightPlan
   * @param launchDirector
   */
  public InitialLoadJdbcRocket(BaseDaoImpl<T> dao, ElasticsearchDao esDao, String lastRunFile,
      ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
  }

  @Override
  public String getJdbcOrderBy() {
    return " ORDER BY CLIENT_ID ";
  }

  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    final StringBuilder buf = new StringBuilder();
    buf.append("SELECT x.* FROM ").append(dbSchemaName).append('.').append(getInitialLoadViewName())
        .append(" x ");

    if (!getFlightPlan().isLoadSealedAndSensitive()) {
      buf.append(" WHERE x.CLIENT_SENSITIVITY_IND = 'N' ");
    }

    buf.append(getJdbcOrderBy()).append(" FOR READ ONLY WITH UR ");
    return buf.toString();
  }

  /**
   * Synchronize to prevent deadlocks when grabbing connections from the C3P0 connection pool.
   * 
   * @return a connection.
   * @throws SQLException on database error
   */
  protected synchronized Connection getConnection() throws SQLException {
    return NeutronJdbcUtils.prepConnection(getJobDao().getSessionFactory());
  }

}
