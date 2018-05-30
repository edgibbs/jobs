package gov.ca.cwds.neutron.rocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.enums.NeutronElasticsearchDefaults;
import gov.ca.cwds.neutron.flight.FlightPlan;

/**
 * Drops and creates a Elasticsearch People Summary index, if requested.
 * 
 * @author CWDS API Team
 */
public class IndexResetPeopleSummaryRocket extends IndexResetRocket {

  private static final long serialVersionUID = 1L;

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao arbitrary DAO
   * @param esDao ElasticSearch DAO for the target index
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   * @param launchDirector launch director
   */
  @Inject
  public IndexResetPeopleSummaryRocket(final ReplicatedOtherAdultInPlacemtHomeDao dao,
      @Named("elasticsearch.dao.people-summary") final ElasticsearchDao esDao,
      final ObjectMapper mapper, FlightPlan flightPlan, AtomLaunchDirector launchDirector) {
    super(dao, esDao, mapper, flightPlan, launchDirector);
  }

  @Override
  protected String getIndexSettingsLocation() {
    return NeutronElasticsearchDefaults.SETTINGS_PEOPLE_SUMMARY.getValue();
  }

  @Override
  protected String getDocumentMappingLocation() {
    return NeutronElasticsearchDefaults.MAPPING_PEOPLE_SUMMARY.getValue();
  }

}
