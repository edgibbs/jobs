package gov.ca.cwds.neutron.rocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.es.NeutronElasticSearchDao;
import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.jobs.ClientIndexerJob;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.atom.AtomRowMapper;
import gov.ca.cwds.neutron.atom.AtomValidateDocument;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;

/**
 * Rocket to load sealed and sensitive Clients from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class RestrictedClientRocket extends ClientIndexerJob
    implements AtomRowMapper<RawClient>, AtomValidateDocument {

  private static final long serialVersionUID = 1L;

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao Client DAO
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   * @param launchDirector launch director
   */
  @Inject
  public RestrictedClientRocket(final ReplicatedClientDao dao, final NeutronElasticSearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
  }

  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    final StringBuilder buf = new StringBuilder();
    buf.append("SELECT x.* FROM ").append(dbSchemaName).append('.').append(getInitialLoadViewName())
        .append(" x WHERE x.clt_identifier BETWEEN ':fromId' AND ':toId' ")
        .append(" AND x.CLT_SENSTV_IND in ('S','R') ").append(getJdbcOrderBy())
        .append(" FOR READ ONLY WITH UR ");
    return buf.toString();
  }

  /**
   * If sealed or sensitive data must NOT be loaded then any records indexed with sealed or
   * sensitive flag must be deleted.
   */
  @Override
  public boolean mustDeleteLimitedAccessRecords() {
    return false;
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception unhandled launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(RestrictedClientRocket.class, args);
  }

}
