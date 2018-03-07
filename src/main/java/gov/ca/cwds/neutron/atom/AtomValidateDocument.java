package gov.ca.cwds.neutron.atom;

import java.io.IOException;

import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;

import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.jetpack.CheeseRay;

public interface AtomValidateDocument extends AtomShared {

  default ElasticSearchPerson readPerson(String json) throws NeutronCheckedException {
    try {
      return ElasticSearchPerson.MAPPER.readValue(json, ElasticSearchPerson.class);
    } catch (Exception e) {
      throw CheeseRay.checked(getLogger(), e, "ERROR READING PERSON DOC! {}", e.getMessage(), e);
    }
  }

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

        person = ElasticSearchPerson.readPerson(json);
        logger.trace("person: {}", person);

        validateDocument(person);
        ret = true;
      }
    } catch (IOException e) {
      // Do NOT re-throw and kill a job over a validation issue.
      getLogger().error("ERROR READING DOCUMENT! doc id: {}", docId, e);
    }

    return ret;
  }

  default boolean validateDocument(final ElasticSearchPerson person)
      throws NeutronCheckedException {
    return true;
  }

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

  default void failValidation() {
    getFlightLog().failValidation();
  }

}
