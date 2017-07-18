package gov.ca.cwds.jobs.util.elastic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.ca.cwds.cals.RecordChangeOperation;
import gov.ca.cwds.cals.service.dto.changed.ChangedDTO;
import gov.ca.cwds.data.es.Elasticsearch5xDao;
import gov.ca.cwds.jobs.JobsException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author CWDS TPT-2
 */
public class CalsElasticJobWriter<T extends ChangedDTO<?>> extends ElasticJobWriter<T> {

  private static final Logger LOGGER = LogManager.getLogger(CalsElasticJobWriter.class);

  /**
   * Constructor.
   *
   * @param elasticsearchDao ES DAO
   * @param objectMapper Jackson object mapper
   */
  public CalsElasticJobWriter(Elasticsearch5xDao elasticsearchDao,
      ObjectMapper objectMapper) {
    super(elasticsearchDao, objectMapper);
  }

  @Override
  public void write(List<T> items) throws Exception {
    items.stream().forEach(item -> {
      try {
        RecordChangeOperation recordChangeOperation = item.getRecordChangeOperation();

        LOGGER.info("Preparing to delete item: ID {}", item.getId());
        bulkProcessor.add(elasticsearchDao.bulkDelete(item.getId()));

        if (RecordChangeOperation.I == recordChangeOperation
            || RecordChangeOperation.U == recordChangeOperation) {
          LOGGER.info("Preparing to insert item: ID {}", item.getId());
          bulkProcessor.add(elasticsearchDao.bulkAdd(objectMapper, item.getId(), item.getDTO()));
        }
      } catch (JsonProcessingException e) {
        throw new JobsException(e);
      }
    });
    bulkProcessor.flush();
  }
}
