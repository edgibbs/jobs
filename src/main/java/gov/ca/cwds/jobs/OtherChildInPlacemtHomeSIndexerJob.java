package gov.ca.cwds.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import gov.ca.cwds.dao.cms.ReplicatedOtherChildInPlacemtHomeDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;

/**
 * Rocket to load Other Child In Placement Home from CMS into ElasticSearch.
 *
 * @author CWDS API Team
 */
public class OtherChildInPlacemtHomeSIndexerJob extends OtherChildInPlacemtHomeIndexerJob {

  private static final long serialVersionUID = 1L;

  /**
   * Construct rocket with all required dependencies.
   *
   * @param dao OtherChildInPlacemtHomeDao DAO
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   * @param launchDirector launch director
   */
  @Inject
  public OtherChildInPlacemtHomeSIndexerJob(final ReplicatedOtherChildInPlacemtHomeDao dao,
      @Named("elasticsearch.dao.people-summary") final ElasticsearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
  }
}
