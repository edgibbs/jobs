package gov.ca.cwds.jobs.util.elastic;

import gov.ca.cwds.cals.Identified;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.data.es.Elasticsearch5xDao;
import gov.ca.cwds.jobs.JobsException;
import gov.ca.cwds.jobs.util.JobWriter;

/**
 * @author CWDS TPT-2
 *
 * @param <T> persistence class type
 */
public class ElasticJobWriter<T extends Identified<String>> implements JobWriter<T> {

  private static final Logger LOGGER = LogManager.getLogger(ElasticJobWriter.class);
  protected Elasticsearch5xDao elasticsearchDao;
  protected BulkProcessor bulkProcessor;
  protected ObjectMapper objectMapper;

  /**
   * Constructor.
   * 
   * @param elasticsearchDao ES DAO
   * @param objectMapper Jackson object mapper
   */
  public ElasticJobWriter(Elasticsearch5xDao elasticsearchDao, ObjectMapper objectMapper) {
    this.elasticsearchDao = elasticsearchDao;
    this.objectMapper = objectMapper;
    bulkProcessor =
        BulkProcessor.builder(elasticsearchDao.getClient(), new BulkProcessor.Listener() {
          @Override
          public void beforeBulk(long executionId, BulkRequest request) {
            LOGGER.warn("Ready to execute bulk of {} actions", request.numberOfActions());
          }

          @Override
          public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
            LOGGER.warn("Executed bulk of {} actions", request.numberOfActions());
          }

          @Override
          public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
            LOGGER.error("ERROR EXECUTING BULK", failure);
          }
        }).build();
  }

  @Override
  public void write(List<T> items) throws Exception {
    items.stream().map(item -> {
      try {
        return elasticsearchDao.bulkAdd(objectMapper, item.getId(), item);
      } catch (JsonProcessingException e) {
        throw new JobsException(e);
      }
    }).forEach(bulkProcessor::add);
    bulkProcessor.flush();
  }

  @Override
  public void destroy() throws Exception {
    bulkProcessor.awaitClose(3000, TimeUnit.MILLISECONDS);
    elasticsearchDao.close();
  }
}
