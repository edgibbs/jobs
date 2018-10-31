package gov.ca.cwds.data.es;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions.Type;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.common.ApiFileAssistant;
import gov.ca.cwds.neutron.enums.NeutronElasticsearchDefaults;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
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

  private static final int TIMEOUT_MILLIS = 1500;
  private static final int NUMBER_OF_SHARDS = 5;
  private static final int NUMBER_OF_REPLICAS = 1;

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

  public ElasticsearchConfiguration getConfig() {
    return config;
  }

  /**
   * Create an index before blasting documents into it.
   */
  protected void createIndex() {
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
      throw new NeutronRuntimeException(e);
    }
  }

  /**
   * Create an index before blasting documents into it.
   *
   * @param index index name or alias
   * @param numShards number of shards
   * @param numReplicas number of replicas
   * @throws IOException on disconnect, hang, etc.
   */
  protected void createIndex(final String index, int numShards, int numReplicas)
      throws IOException {
    LOGGER.warn("CREATE ES INDEX {} with {} shards and {} replicas", index, numShards, numReplicas);
    final Settings indexSettings = Settings.builder().put("number_of_shards", numShards)
        .put("number_of_replicas", numReplicas).build();
    client.indices().create(new CreateIndexRequest(index, indexSettings), RequestOptions.DEFAULT);

    final PutMappingRequest reqMapping = new PutMappingRequest(index);
    reqMapping.type(getConfig().getElasticsearchDocType());
    final String mapping = IOUtils.toString(
        this.getClass()
            .getResourceAsStream(NeutronElasticsearchDefaults.MAPPING_PEOPLE_SUMMARY.getValue()),
        Charset.defaultCharset().name());
    reqMapping.source(mapping, XContentType.JSON);
    client.indices().putMapping(reqMapping, RequestOptions.DEFAULT);
  }

  /**
   * Create an index, if missing.
   *
   * <p>
   * Method is intentionally synchronized to prevent race conditions and multiple attempts to create
   * the same index.
   * </p>
   * 
   * @throws NeutronCheckedException on error
   */
  @SuppressWarnings({"findbugs:SWL_SLEEP_WITH_LOCK_HELD", "squid:S2276"})
  public synchronized void createIndexIfMissing() throws NeutronCheckedException {
    final String index = config.getElasticsearchAlias();
    if (!doesIndexExist(index)) {
      LOGGER.warn("ES INDEX {} DOES NOT EXIST!!", index);
      createIndex();
      try {
        // Give Elasticsearch a moment to catch its breath.
        Thread.sleep(5000L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        LOGGER.warn("Interrupted!");
      }
    }
  }

  private String readFile(String sourceFile) throws IOException {
    return new ApiFileAssistant().readFile(sourceFile);
  }

  /**
   * Create ES index based on supplied parameters.
   *
   * @param index Index name
   * @param type Index document type
   * @param settingsJsonFile Setting file
   * @param mappingJsonFile Mapping file
   * @throws NeutronCheckedException on error
   */
  public synchronized void createIndex(final String index, final String type,
      final String settingsJsonFile, final String mappingJsonFile) throws NeutronCheckedException {
    LOGGER.warn("CREATING ES INDEX [{}] for type [{}] with settings [{}] and mappings [{}]...",
        index, type, settingsJsonFile, mappingJsonFile);

    try {
      final String settingsSource = readFile(settingsJsonFile);
      final String mappingSource = readFile(mappingJsonFile);
      final CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
      createIndexRequest.settings(settingsSource, XContentType.JSON);
      createIndexRequest.mapping(type, mappingSource, XContentType.JSON);

      client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "FAILED TO CREATE INDEX '{}'! {}", index, e.getMessage());
    }
  }

  public synchronized void createIndexIfNeeded(final String index, final String type,
      final String settingsJsonFile, final String mappingJsonFile) throws NeutronCheckedException {
    LOGGER.warn("CREATING ES INDEX [{}] for type [{}] with settings [{}] and mappings [{}]...",
        index, type, settingsJsonFile, mappingJsonFile);

    if (!doesIndexExist(index)) {
      try {
        LOGGER.warn("ES INDEX {} DOES NOT EXIST!!", index);
        final String settingsSource = readFile(settingsJsonFile);
        final String mappingSource = readFile(mappingJsonFile);
        final CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
        createIndexRequest.settings(settingsSource, XContentType.JSON);
        createIndexRequest.mapping(type, mappingSource, XContentType.JSON);

        client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
      } catch (Exception e) {
        throw CheeseRay.checked(LOGGER, e, "FAILED TO CREATE INDEX '{}'! {}", index,
            e.getMessage());
      }
    }
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

  public synchronized void deleteIndex(final String index) throws NeutronCheckedException {
    try {
      if (doesIndexExist(index) && getClient().indices()
          .delete(new DeleteIndexRequest(index).timeout(TimeValue.timeValueMillis(TIMEOUT_MILLIS)),
              RequestOptions.DEFAULT)
          .isAcknowledged()) {
        LOGGER.warn("\n\n\t>>>>>> DELETE INDEX {}! <<<<<<\n\n", index);
      }
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "DELETE INDEX FAILED! {}", e.getMessage());
    }
  }

  /**
   * Creates or swaps index alias
   *
   * @param alias Alias name
   * @param index Index name
   * @return true if successful
   * @throws NeutronCheckedException on error
   */
  public synchronized boolean createOrSwapAlias(final String alias, final String index)
      throws NeutronCheckedException {
    String oldIndex = StringUtils.EMPTY;

    if (doesIndexExist(alias)) {
      LOGGER.warn("CAN'T CREATE ALIAS {}! Index with the same name already exists!", alias);
      return false;
    } else if (!doesIndexExist(index)) {
      LOGGER.warn("CAN'T CREATE ALIAS {}! Index with the name {} doesn't exist!", alias, index);
      return false;
    } else if (doesAliasExist(alias)) {
      try {
        // Map of indices and their aliases.
        final Map<String, Set<AliasMetaData>> aliases = client.indices()
            .getAlias(new GetAliasesRequest(alias), RequestOptions.DEFAULT).getAliases();
        if (!aliases.isEmpty()) {
          for (Map.Entry<String, Set<AliasMetaData>> entry : aliases.entrySet()) {
            oldIndex = entry.getKey();
            break;
          }
        }
      } catch (Exception e) {
        throw CheeseRay.checked(LOGGER, e, "DELETE INDEX FAILED! {}", e.getMessage());
      }

      LOGGER.info("Swapping Alias {} from Index {} to Index {}.", alias, oldIndex, index);
    } else {
      LOGGER.info("Creating Alias {} for Index {}.", alias, index);
    }

    return createOrSwapAlias(alias, index, oldIndex);
  }

  /**
   *
   * @param alias Alias name
   * @param index New Index name
   * @param oldIndex Current Index Name
   * @return true if successful
   */
  private boolean createOrSwapAlias(final String alias, final String index, final String oldIndex) {
    try {
      if (StringUtils.isBlank(oldIndex)) {
        return client.indices()
            .updateAliases(new IndicesAliasesRequest()
                .addAliasAction(new AliasActions(Type.ADD).index(index).alias(alias))
                .timeout(TimeValue.timeValueMillis(TIMEOUT_MILLIS)), RequestOptions.DEFAULT)
            .isAcknowledged();
      } else {
        return client.indices()
            .updateAliases(new IndicesAliasesRequest()
                .addAliasAction(new AliasActions(Type.REMOVE).index(oldIndex).alias(alias))
                .addAliasAction(new AliasActions(Type.ADD).index(index).alias(alias))
                .timeout(TimeValue.timeValueMillis(TIMEOUT_MILLIS)), RequestOptions.DEFAULT)
            .isAcknowledged();
      }
    } catch (IOException e) {
      throw CheeseRay.runtime(LOGGER, e, "CREATE OR SWAP ALIAS FAILED! {}", e.getMessage());
    }
  }

  /**
   * Check whether Elasticsearch cluster already contains the given index or alias.
   *
   * @param indexOrAlias index name or alias
   * @return whether the index or alias exists
   * @throws NeutronCheckedException on error
   */
  public boolean doesIndexExist(final String indexOrAlias) throws NeutronCheckedException {
    try {
      return client.indices().exists(new GetIndexRequest().indices(indexOrAlias),
          RequestOptions.DEFAULT);
    } catch (IOException e) {
      throw CheeseRay.checked(LOGGER, e, "INDEX CHECK FAILED! {}", e.getMessage());
    }
  }

  public boolean doesAliasExist(final String alias) throws NeutronCheckedException {
    try {
      return client.indices().existsAlias(new GetAliasesRequest(alias), RequestOptions.DEFAULT);
    } catch (IOException e) {
      throw CheeseRay.checked(LOGGER, e, "ALIAS CHECK FAILED! {}", e.getMessage());
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
