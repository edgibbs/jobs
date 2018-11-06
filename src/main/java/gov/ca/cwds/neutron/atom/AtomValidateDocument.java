package gov.ca.cwds.neutron.atom;

import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;

import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.NeutronElasticSearchPerson;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.jetpack.CheeseRay;

/**
 * Validate Elasticsearch documents just written to ensure data quality.
 *
 * @author CWDS API Team
 */
public interface AtomValidateDocument extends AtomShared {

  /**
   * Validate a random set of Elasticsearch documents from the rocket's last flight.
   *
   * @throws NeutronCheckedException Elasticsearch error or JSON parse error
   */
  default void validateDocuments() throws NeutronCheckedException {
    // TODO: multi-search not yet working with ES 6.4.x.
    final Logger log = getLogger();
    final String[] docIds = getFlightLog().getAffectedDocumentIds();
    long totalHits = 0;

    if (docIds != null && docIds.length > 0) {
      final String[] affectedDocIds = getFlightLog().getAffectedDocumentIds();
      if (affectedDocIds != null && affectedDocIds.length > 0) {
        final RestHighLevelClient esClient = getEsDao().getClient();
        final MultiSearchRequest multiSearchRequest =
            new MultiSearchRequest().add(new SearchRequest().source(
                new SearchSourceBuilder().query(QueryBuilders.idsQuery().addIds(affectedDocIds))));

        try {
          final MultiSearchResponse multiResponse =
              esClient.msearch(multiSearchRequest, RequestOptions.DEFAULT);
          for (MultiSearchResponse.Item item : multiResponse.getResponses()) {
            final SearchHits hits = item.getResponse().getHits();
            totalHits += hits.getTotalHits();
            processDocumentHits(hits);
          }
        } catch (Exception e) {
          CheeseRay.checked(log, e, "FAILED MULTISEARCH! {}", e.getMessage());
        }
      }
    }

    log.info("total hits: {}", totalHits);
  }

  /**
   * Validate Elasticsearch search hits.
   *
   * @param hits Elasticsearch search hits
   * @return true = validation passes
   * @throws NeutronCheckedException Elasticsearch error or JSON parse error
   */
  default boolean processDocumentHits(final SearchHits hits) throws NeutronCheckedException {
    int docId = 0;
    String json;
    ElasticSearchPerson person;
    final Logger logger = getLogger();
    boolean ret = false;

    try {
      for (SearchHit hit : hits.getHits()) {
        docId = hit.docId();
        json = hit.getSourceAsString();

        logger.debug("validate doc id: {}", docId);
        logger.trace("json: {}", json);

        person = readPerson(json);
        logger.trace("person: {}", person);

        validateDocument(person);
        ret = true;
      }
    } catch (Exception e) {
      // Do NOT re-throw and abort a flight over a validation issue.
      // Instead, note the validation error in the flight log.
      logger.error("ERROR READING DOCUMENT! doc id: {}", docId, e);
      failValidation();
    }

    return ret;
  }

  default boolean validateDocument(final ElasticSearchPerson person)
      throws NeutronCheckedException {
    return true;
  }

  default void failValidation() {
    getFlightLog().failValidation();
  }

  /**
   * Convenience method to construction an {@link ElasticSearchPerson} from JSON.
   *
   * @param json JSON to read
   * @return populated ES person object
   * @throws NeutronCheckedException Elasticsearch error or JSON parse error
   */
  default ElasticSearchPerson readPerson(String json) throws NeutronCheckedException {
    try {
      return getMapper().readValue(json, NeutronElasticSearchPerson.class);
    } catch (Exception e) {
      throw CheeseRay.checked(getLogger(), e, "ERROR READING PERSON DOC! {}", e.getMessage(), e);
    }
  }

}
