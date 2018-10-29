package gov.ca.cwds.data.es;

import java.io.Closeable;
import java.io.IOException;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.rest.ElasticsearchConfiguration;

/**
 * A DAO for Elasticsearch with writing indexes functionality. It is not intended for searching, nor
 * it can contain any index-specific code or hardcoded mapping.
 *
 * <p>
 * Let Guice manage inject object instances. Don't manage instances in this class.
 * </p>
 *
 * @author CWDS API Team
 */
public class NeutronElasticSearchDao implements Closeable {

  private static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(NeutronElasticSearchDao.class);

  /**
   * Client is thread safe.
   */
  private RestHighLevelClient client;

  /**
   * Elasticsearch configuration
   */
  private ElasticsearchConfiguration config;

  /**
   * Constructor.
   *
   * @param client The ElasticSearch client
   * @param config The ElasticSearch configuration which is read from .yaml file
   */
  @Inject
  public NeutronElasticSearchDao(RestHighLevelClient client, ElasticsearchConfiguration config) {
    this.client = client;
    this.config = config;
  }

  /**
   * Check whether Elasticsearch already has the chosen index.
   *
   * @param index index name or alias
   * @return whether the index exists
   */
  private boolean doesIndexExist(final String index) {
    final GetIndexRequest request = new GetIndexRequest();
    request.indices(index);
    boolean exists = false;
    try {
      exists = client.indices().exists(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
      LOGGER.warn("Can't validate index [" + index + "] existence", e);
    }
    return exists;
  }

  /**
   * Create an index before blasting documents into it.
   */
  private void createIndex() {
    LOGGER.warn("CREATING ES INDEX [{}] for type [{}]", config.getElasticsearchAlias(),
        config.getElasticsearchDocType());

    final CreateIndexRequest createIndexRequest =
        new CreateIndexRequest(config.getElasticsearchAlias());
    createIndexRequest.settings(config.getIndexSettingFile(), XContentType.JSON);
    createIndexRequest.mapping(config.getElasticsearchDocType(), config.getDocumentMappingFile(),
        XContentType.JSON);

    try {
      client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      LOGGER.error("Unable to create index [" + config.getElasticsearchAlias() + "]", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Create an index, if missing.
   *
   * <p>
   * Method is intentionally synchronized to prevent race conditions and multiple attempts to create
   * the same index.
   * </p>
   */
  @SuppressWarnings({"findbugs:SWL_SLEEP_WITH_LOCK_HELD", "squid:S2276"})
  public synchronized void createIndexIfMissing() {
    final String index = config.getElasticsearchAlias();
    if (!doesIndexExist(index)) {
      LOGGER.warn("ES INDEX {} DOES NOT EXIST!!", index);
      createIndex();
      try {
        // Give Elasticsearch a moment to catch its breath.
        Thread.sleep(8000L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        LOGGER.warn("Interrupted!");
      }
    }
  }

  /**
   * Prepare an index request for bulk operations.
   *
   * @param mapper Jackson ObjectMapper
   * @param id ES document id
   * @param obj document object
   * @return prepared IndexRequest
   * @throws JsonProcessingException if unable to serialize JSON
   */
  public IndexRequest bulkAdd(final ObjectMapper mapper, final String id, final Object obj)
      throws JsonProcessingException {
    final IndexRequest ret =
        new IndexRequest(config.getElasticsearchAlias(), config.getElasticsearchDocType(), id);
    ret.source(mapper.writeValueAsBytes(obj), XContentType.JSON);
    return ret;
  }

  /**
   * Prepare an delete request for bulk operations.
   *
   * @param id ES document id
   * @return prepared DeleteRequest
   */
  public DeleteRequest bulkDelete(final String id) {
    return new DeleteRequest(config.getElasticsearchAlias(), config.getElasticsearchDocType(), id);
  }

  /**
   * Stop the ES client, if started.
   */
  private void stop() throws IOException {
    if (client != null) {
      client.close();
    }
  }

  @Override
  public void close() throws IOException {
    try {
      stop();
    } catch (Exception e) {
      final String msg = "Error closing ElasticSearch DAO: " + e.getMessage();
      LOGGER.error(msg, e);
      throw new IOException(msg, e);
    }
  }

  /**
   * @return the client
   */
  public RestHighLevelClient getClient() {
    return client;
  }

}
