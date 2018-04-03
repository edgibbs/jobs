package gov.ca.cwds.neutron.rocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.neutron.enums.NeutronElasticsearchDefaults;
import gov.ca.cwds.neutron.flight.FlightPlan;

/**
 * Drops and creates a Elasticsearch People index, if requested.
 * 
 * @author CWDS API Team
 */
public class IndexResetPeopleRocket extends IndexResetRocket {

  private static final long serialVersionUID = 1L;

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao arbitrary DAO
   * @param esDao ElasticSearch DAO for the target index
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   */
  @Inject
  public IndexResetPeopleRocket(final ReplicatedOtherAdultInPlacemtHomeDao dao,
      @Named("elasticsearch.dao.people") final ElasticsearchDao esDao, final ObjectMapper mapper,
      FlightPlan flightPlan) {
    super(dao, esDao, mapper, flightPlan);
  }

  @Override
  protected String getIndexSettingsLocation() {
    return NeutronElasticsearchDefaults.ES_PEOPLE_INDEX_SETTINGS.getValue();
  }

  @Override
  protected String getDocumentMappingLocation() {
    return getFlightPlan().isLegacyPeopleMapping()
        ? NeutronElasticsearchDefaults.ES_LEGACY_PEOPLE_PERSON_MAPPING.getValue()
        : NeutronElasticsearchDefaults.ES_PEOPLE_PERSON_MAPPING.getValue();
  }

}
