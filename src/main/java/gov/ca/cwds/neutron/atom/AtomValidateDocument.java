package gov.ca.cwds.neutron.atom;

import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
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
    final String[] docIds = getFlightLog().getAffectedDocumentIds();
    long totalHits = 0;

    if (docIds != null && docIds.length > 0) {
      final String[] affectedDocIds = getFlightLog().getAffectedDocumentIds();
      if (affectedDocIds != null && affectedDocIds.length > 0) {
        final Client esClient = getEsDao().getClient();
        final MultiSearchResponse multiResponse = esClient.prepareMultiSearch()
            .add(esClient.prepareSearch().setQuery(QueryBuilders.idsQuery().addIds(affectedDocIds)))
            .get();

        for (MultiSearchResponse.Item item : multiResponse.getResponses()) {
          final SearchHits hits = item.getResponse().getHits();
          totalHits += hits.getTotalHits();
          processDocumentHits(hits);
        }
      }
    }

    getLogger().info("total hits: {}", totalHits);
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

        logger.info("validate doc id: {}", docId);
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
