package gov.ca.cwds.jobs.common.inject;

import com.google.inject.Inject;
import gov.ca.cwds.jobs.common.exception.JobExceptionHandler;
import gov.ca.cwds.jobs.common.job.Job;
import gov.ca.cwds.jobs.common.job.JobPreparator;
import gov.ca.cwds.jobs.common.job.timestamp.SavepointOperator;
import gov.ca.cwds.jobs.common.job.utils.ConsumerCounter;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Alexander Serbin on 3/5/2018.
 */
public class JobImpl<T> implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobImpl.class);

  @Inject
  private SavepointOperator savepointOperator;

  @Inject
  private BatchProcessor<T> batchProcessor;

  @Inject
  private JobPreparator jobPreparator;

  @Override
  public void run() {
    try {
      jobPreparator.run();
      batchProcessor.init();
      batchProcessor.processBatches();
      LocalDateTime now = LocalDateTime.now();
      savepointOperator.writeTimestamp(now);
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Updating job timestamp to the current moment {}", now);
        LOGGER.info("Added {} entities to the Elastic Search index", ConsumerCounter.getCounter());
      }
    } finally {
      JobExceptionHandler.reset();
      close();
    }
  }

  @Override
  public void close() {
    batchProcessor.destroy();
  }
}
