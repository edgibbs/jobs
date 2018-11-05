package gov.ca.cwds.neutron.rocket;

import java.io.IOException;
import java.util.Date;

import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.NeutronElasticSearchDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;

/**
 * Test Elasticsearch mass search capability for automatic validation.
 * 
 * @author CWDS API Team
 */
public class SanityCheckRocket
    extends BasePersonRocket<ReplicatedOtherAdultInPlacemtHome, ReplicatedOtherAdultInPlacemtHome> {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(SanityCheckRocket.class);

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao OtherAdultInPlacemtHome DAO
   * @param esDao ElasticSearch DAO
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   * @param launchDirector launch scheduler
   * @param lastRunFile last run file
   */
  @Inject
  public SanityCheckRocket(final ReplicatedOtherAdultInPlacemtHomeDao dao,
      final NeutronElasticSearchDao esDao, final ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector, @LastRunFile String lastRunFile) {
    super(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
  }

  @Override
  public Date launch(Date lastSuccessfulRunTime) {
    LOGGER.info("SANITY CHECK!");
    final RestHighLevelClient client = esDao.getClient();
    final MultiSearchRequest multiSearchRequest =
        new MultiSearchRequest().add(new SearchRequest().source(new SearchSourceBuilder()
            .query(QueryBuilders.idsQuery().addIds("OpvBkr00ND", "Jw3ny5K00h", "EuCrckE04M"))));

    try {
      final MultiSearchResponse sr = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);
      long totalHits = 0;

      for (MultiSearchResponse.Item item : sr.getResponses()) {
        final SearchResponse response = item.getResponse();
        final SearchHits hits = response.getHits();
        totalHits += hits.getTotalHits();

        try {
          for (SearchHit hit : hits.getHits()) {
            final String json = hit.getSourceAsString();
            LOGGER.info("json: {}", json);

            final ElasticSearchPerson person = readPerson(json);
            LOGGER.info("person: {}", person);
          }
        } catch (NeutronCheckedException e) {
          LOGGER.warn("whatever", e);
        }
      }

      LOGGER.info("total hits: {}", totalHits);
    } catch (NullPointerException | IOException e) {
      CheeseRay.runtime(LOGGER, e, "FAILED MULTISEARCH! {}", e.getMessage());
    }

    try {
      launchDirector.stopScheduler(false);
    } catch (Exception e) {
      CheeseRay.runtime(LOGGER, e, "FAILED TO STOP SCHEDULER! {}", e.getMessage());
    }

    return lastSuccessfulRunTime;
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(SanityCheckRocket.class, args);
  }

}
