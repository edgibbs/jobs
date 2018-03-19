package gov.ca.cwds.data.es;

import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.jetpack.CheeseRay;

/**
 * A DAO for Elasticsearch.
 *
 * <p>
 * Let Guice manage inject object instances. Don't manage instances in this class.
 * </p>
 *
 * <p>
 * OPTION: In order to avoid minimize connections to Elasticsearch, this DAO class should either be
 * final, so that other classes cannot instantiate a client or else the ES client should be injected
 * by the framework.
 * </p>
 *
 * <p>
 * OPTION: allow child DAO classes to connect to a configured index of choice and read specified
 * document type(s).
 * </p>
 *
 * @author CWDS API Team
 * 
 * @deprecated Use gov.ca.cwds.data.es.ElasticsearchDao instead
 */
@Deprecated
public class Elasticsearch5xDao implements Closeable, ApiMarker {

  /**
   * Default serialization.
   */
  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(Elasticsearch5xDao.class);

  private static final int NUMBER_OF_SHARDS = 5;

  private static final int NUMBER_OF_REPLICAS = 1;

  /**
   * Client is thread safe.
   */
  private transient Client client;

  /**
   * Elasticsearch configuration
   */
  private transient ElasticsearchConfiguration5x config;

  /**
   * Constructor.
   *
   * @param client The ElasticSearch client
   * @param config The ElasticSearch configuration which is read from .yaml file
   */
  @Inject
  public Elasticsearch5xDao(Client client, ElasticsearchConfiguration5x config) {
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
    final IndexMetaData indexMetaData = client.admin().cluster()
        .state(Requests.clusterStateRequest()).actionGet().getState().getMetaData().index(index);
    return indexMetaData != null;
  }

  /**
   * Create an index before blasting documents into it.
   *
   * @param index index name or alias
   * @param numShards number of shards
   * @param numReplicas number of replicas
   * @throws IOException on disconnect, hang, etc.
   */
  private void createIndex(final String index, int numShards, int numReplicas) throws IOException {
    LOGGER.warn("CREATE ES INDEX {} with {} shards and {} replicas", index, numShards, numReplicas);
    final Settings indexSettings = Settings.builder().put("number_of_shards", numShards)
        .put("number_of_replicas", numReplicas).build();
    CreateIndexRequest indexRequest = new CreateIndexRequest(index, indexSettings);
    getClient().admin().indices().create(indexRequest).actionGet();

    final String mapping = IOUtils.toString(
        this.getClass().getResourceAsStream("/elasticsearch/mapping/map_person_5x_snake.json"));
    getClient().admin().indices().preparePutMapping(index)
        .setType(getConfig().getElasticsearchDocType()).setSource(mapping, XContentType.JSON).get();
  }

  /**
   * Create an index, if needed, before blasting documents into it.
   *
   * <p>
   * Defaults to 5 shards and 1 replica.
   * </p>
   *
   * <p>
   * Method is intentionally synchronized to prevent race conditions and multiple attempts to create
   * the same index.
   * </p>
   *
   * @param index index name or alias
   * @throws NeutronCheckedException on thread interrupt, disconnect, hang, etc.
   */
  public synchronized void createIndexIfNeeded(final String index) throws NeutronCheckedException {
    try {
      if (!doesIndexExist(index)) {
        LOGGER.warn("ES INDEX {} DOES NOT EXIST!!", index);
        createIndex(index, NUMBER_OF_SHARDS, NUMBER_OF_REPLICAS);

        // Give Elasticsearch a moment to catch its breath.
        Thread.sleep(2000); // NOSONAR
      }
    } catch (InterruptedException | IOException e) {
      throw CheeseRay.checked(LOGGER, e, "CREATE INDEX FAILED! {}", e.getMessage());
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
    return new IndexRequest(getConfig().getElasticsearchAlias(),
        getConfig().getElasticsearchDocType(), id).source(mapper.writeValueAsString(obj),
            XContentType.JSON);
  }

  /**
   * Stop the ES client, if started.
   */
  private void stop() {
    if (client != null) {
      this.client.close();
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
  public Client getClient() {
    return client;
  }

  /**
   * @return the Elasticsearch configuration
   */
  public ElasticsearchConfiguration5x getConfig() {
    return config;
  }
}
