package gov.ca.cwds.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import gov.ca.cwds.dao.cms.ReplicatedServiceProviderDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.es.NeutronElasticSearchDao;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;

/**
 * Rocket to load Service Provider from CMS into ElasticSearch.
 *
 * @author CWDS API Team
 */
public class ServiceProviderSIndexerJob extends ServiceProviderIndexerJob {

  private static final long serialVersionUID = 1L;

  /**
   * Construct rocket with all required dependencies.
   *
   * @param dao ServiceProvider DAO
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line opts
   * @param launchDirector launch director
   */
  @Inject
  public ServiceProviderSIndexerJob(final ReplicatedServiceProviderDao dao,
      @Named("elasticsearch.dao.people-summary") final NeutronElasticSearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
  }

}
