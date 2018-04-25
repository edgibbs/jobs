package gov.ca.cwds.jobs.common.inject;

import com.google.inject.Inject;
import gov.ca.cwds.jobs.common.batch.JobBatch;
import gov.ca.cwds.jobs.common.batch.JobBatchIterator;
import gov.ca.cwds.jobs.common.elastic.ElasticSearchBulkCollector;
import gov.ca.cwds.jobs.common.exception.JobExceptionHandler;
import gov.ca.cwds.jobs.common.exception.JobsException;
import gov.ca.cwds.jobs.common.job.timestamp.SavepointOperator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Alexander Serbin on 4/2/2018.
 */
public class BatchProcessor<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessor.class);

  @Inject
  private ElasticSearchBulkCollector<T> elasticSearchBulkCollector;

  @Inject
  private BatchReadersPool<T> batchReadersPool;

  @Inject
  private JobBatchIterator batchIterator;

  @Inject
  private SavepointOperator savepointOperator;

  public void init() {
    batchIterator.init();
    batchReadersPool.init(elasticSearchBulkCollector);
  }

  public void processBatches() {
    JobTimeReport jobTimeReport = new JobTimeReport();
    List<JobBatch> portion = batchIterator.getNextPortion();
    do {
      if (LOGGER.isInfoEnabled()) {
        printPortionInformation(portion);
      }
      for (JobBatch aPortion : portion) {
        processBatch(aPortion);
      }
      portion = batchIterator.getNextPortion();
    } while (!portion.isEmpty());
    jobTimeReport.printTimeSpent();
  }

  private void printPortionInformation(List<JobBatch> portion) {
    LOGGER.info("New portion: {} batches", portion.size());
    for (int i = 0; i < portion.size(); i++) {
      LOGGER.info("Batch {} size = {}, batch timestamp = {}", i + 1, portion.get(i).getSize(),
          portion.get(i).getTimestamp());
    }
  }

  private void processBatch(JobBatch jobBatch) {
    batchReadersPool.loadEntities(jobBatch.getChangedEntityIdentifiers());
    if (!JobExceptionHandler.isExceptionHappened()) {
      savepointOperator.writeTimestamp(jobBatch.getTimestamp());
      if (LOGGER.isInfoEnabled()) {
//          jobTimeReport.printTimeReport(portionBatchNumber);
      }
      if (!jobBatch.isEmptyTimestamp()) {
        LOGGER.info("Save point has been reached. Save point batch timestamp is {}",
            jobBatch.getTimestamp());
      }
    } else {
      LOGGER.error("Exception occured during batch processing. Job has been terminated." +
          " Batch timestamp {} has not been recorded", jobBatch.getTimestamp());
      throw new JobsException("Exception occured during batch processing");
    }

  }

  public void destroy() {
    batchReadersPool.destroy();
    elasticSearchBulkCollector.destroy();
  }
}