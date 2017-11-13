package gov.ca.cwds.neutron.atom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.ca.cwds.data.ApiTypedIdentifier;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPerson.ESOptionalCollection;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.data.std.ApiPersonAware;
import gov.ca.cwds.jobs.exception.NeutronException;
import gov.ca.cwds.neutron.jetpack.JobLogs;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;

public interface AtomPersonDocPrep<T extends PersistentObject> extends ApiMarker, AtomShared {

  /**
   * Set optional ES person collections before serializing JSON for insert. Child classes which
   * handle optional collections should override this method.
   *
   * <p>
   * <strong>Example:</strong>
   * </p>
   * 
   * <pre>
   * {@code esp.setScreenings((List<ElasticSearchPerson.ElasticSearchPersonScreening>) col);}
   * </pre>
   * 
   * @param esp ES document, already prepared by
   *        {@link ElasticTransformer#buildElasticSearchPersonDoc(ApiPersonAware)}
   * @param t target ApiPersonAware instance
   * @param list list of ES child objects
   */
  default void setInsertCollections(ElasticSearchPerson esp, T t,
      List<? extends ApiTypedIdentifier<String>> list) {
    // Default, no-op.
  }

  /**
   * Get the optional element name populated by this job or null if none.
   * 
   * @return optional element name
   */
  default String getOptionalElementName() {
    return null;
  }

  /**
   * Which optional ES collections to retain for insert JSON. Child classes that populate optional
   * collections should override this method.
   * 
   * @return array of optional collections to keep in insert JSON
   */
  default ESOptionalCollection[] keepCollections() {
    return new ESOptionalCollection[] {ESOptionalCollection.NONE};
  }

  /**
   * Return the optional collection used to build the update JSON, if any. Child classes that
   * populate optional collections should override this method.
   * 
   * @param esp ES person document object
   * @param t normalized type
   * @return List of ES person elements
   */
  default List<ApiTypedIdentifier<String>> getOptionalCollection(ElasticSearchPerson esp, T t) {
    return new ArrayList<>();
  }

  default <E> UpdateRequest prepareUpsertRequest(ElasticSearchPerson esp, T p, List<E> elements)
      throws NeutronException {
    final StringBuilder buf = new StringBuilder();
    buf.append("{\"").append(getOptionalElementName()).append("\":[");
    if (!elements.isEmpty()) {
      try {
        buf.append(elements.stream().map(ElasticTransformer::jsonify).sorted(String::compareTo)
            .collect(Collectors.joining(",")));
      } catch (Exception e) {
        throw JobLogs.runtime(getLogger(), e, "ERROR SERIALIZING RELATIONSHIPS! {}",
            e.getMessage());
      }
    }
    buf.append("]}");
    String insertJson;
    try {
      insertJson = getMapper().writeValueAsString(esp);
    } catch (JsonProcessingException e) {
      throw JobLogs.checked(getLogger(), e, "FAILED TO WRITE OBJECT TO JSON! {}", e.getMessage());
    }
    final String updateJson = buf.toString();
    final String alias = getEsDao().getConfig().getElasticsearchAlias();
    final String docType = getEsDao().getConfig().getElasticsearchDocType();
    return new UpdateRequest(alias, docType, esp.getId()).doc(updateJson, XContentType.JSON).upsert(
        new IndexRequest(alias, docType, esp.getId()).source(insertJson, XContentType.JSON));
  }

}
