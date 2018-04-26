package gov.ca.cwds.jobs.common.batch;

import com.google.inject.Inject;
import gov.ca.cwds.jobs.common.Constants;
import gov.ca.cwds.jobs.common.JobMode;
import gov.ca.cwds.jobs.common.api.ChangedEntitiesIdentifiersService;
import gov.ca.cwds.jobs.common.identifier.ChangedEntityIdentifier;
import gov.ca.cwds.jobs.common.inject.JobBatchSize;
import gov.ca.cwds.jobs.common.job.timestamp.SavepointOperator;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Alexander Serbin on 3/29/2018.
 */
public class JobBatchIteratorImpl implements JobBatchIterator {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(JobBatchIteratorImpl.class);

  @Inject
  @JobBatchSize
  private int batchSize;

  @Inject
  private ChangedEntitiesIdentifiersService changedEntitiesIdentifiersService;

  @Inject
  private SavepointOperator savepointOperator;

  private AtomicInteger nextOffset = new AtomicInteger(0);

  private JobMode jobMode;

  @Override
  public void init() {
    jobMode = defineJobMode();
  }

  private JobMode defineJobMode() {
    if (!savepointOperator.savepointExists()) {
      LOGGER.info("Processing initial load");
      return JobMode.INITIAL_LOAD;
    } else if (isTimestampMoreThanOneMonthOld()) {
      LOGGER.info("Processing initial load - resuming after save point {}", savepointOperator
          .readTimestamp());
      return JobMode.INITIAL_LOAD_RESUME;
    } else {
      LocalDateTime timestamp = savepointOperator.readTimestamp();
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Processing incremental load after timestamp {}",
            Constants.DATE_TIME_FORMATTER.format(timestamp));
        return JobMode.INCREMENTAL_LOAD;
      }
      return JobMode.INCREMENTAL_LOAD;
    }
  }

  private boolean isTimestampMoreThanOneMonthOld() {
    ChangedEntityIdentifier savepoint = savepointOperator.readSavepoint();

    return StringUtils.isNumeric(savepoint)
        || savepointOperator.readTimestamp().until(LocalDateTime.now(), ChronoUnit.MONTHS) > 1;
  }

  @Override
  public List<JobBatch> getNextPortion() {
    List<ChangedEntityIdentifier> identifiers = getNextPage();
    if (identifiers.isEmpty()) {
      return Collections.emptyList();
    }
    LocalDateTime lastTimeStamp = identifiers.get(identifiers.size() - 1).getTimestamp();
    if (lastTimeStamp == null) {
      nextOffset.addAndGet(batchSize);
      return Collections.singletonList(new JobBatch(identifiers));
    } else {
      return calculateNextPortion(identifiers);
    }
  }

  protected List<ChangedEntityIdentifier> getNextPage() {
    return getNextPage(new PageRequest(nextOffset.get(), batchSize));
  }

  protected List<ChangedEntityIdentifier> getNextPage(PageRequest pageRequest) {
    LOGGER.info("{}", pageRequest);
    if (jobMode == JobMode.INITIAL_LOAD) {
      return changedEntitiesIdentifiersService.getIdentifiersForInitialLoad(pageRequest);
    } else if (jobMode == JobMode.INITIAL_LOAD_RESUME) {
      return changedEntitiesIdentifiersService
          .getIdentifiersForResumingInitialLoad(savepointOperator.readTimestamp(), pageRequest);
    } else if (jobMode == JobMode.INCREMENTAL_LOAD) {
      return changedEntitiesIdentifiersService
          .getIdentifiersForIncrementalLoad(savepointOperator.readTimestamp(), pageRequest);
    }
    throw new IllegalStateException("Unexpected job mode");
  }

  private List<JobBatch> calculateNextPortion(
      List<ChangedEntityIdentifier> identifiers) {
    List<JobBatch> nextPortion = new ArrayList<>();
    List<ChangedEntityIdentifier> nextIdentifiersPage = identifiers;
    while ((!nextIdentifiersPage.isEmpty() && getLastTimestamp(identifiers) ==
        getLastTimestamp(nextIdentifiersPage))) {
      nextOffset.addAndGet(batchSize);
      nextPortion.add(new JobBatch(nextIdentifiersPage));
      nextIdentifiersPage = getNextPage();
    }

    List<ChangedEntityIdentifier> nextIdentifier = getNextIdentifier();

    while (!nextIdentifier.isEmpty() &&
        (nextIdentifier.get(0).getTimestamp() == getLastTimestamp(identifiers))) {
      nextOffset.addAndGet(batchSize);
      assert nextIdentifier.size() == 1;
      nextPortion.get(nextPortion.size() - 1).getChangedEntityIdentifiers()
          .add(nextIdentifier.get(0));
      nextIdentifier = getNextIdentifier();
    }
    nextPortion.get(nextPortion.size() - 1).setTimestamp(getLastTimestamp(identifiers));
    return nextPortion;
  }

  private List<ChangedEntityIdentifier> getNextIdentifier() {
    return getNextPage(new PageRequest(nextOffset.get(), 1));
  }

  private static LocalDateTime getLastTimestamp(List<ChangedEntityIdentifier> identifiers) {
    return identifiers.get(identifiers.size() - 1).getTimestamp();
  }

  @Override
  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setChangedEntitiesIdentifiersService(
      ChangedEntitiesIdentifiersService changedEntitiesIdentifiersService) {
    this.changedEntitiesIdentifiersService = changedEntitiesIdentifiersService;
  }

  public void setSavepointOperator(
      SavepointOperator savepointOperator) {
    this.savepointOperator = savepointOperator;
  }

  public void setJobMode(JobMode jobMode) {
    this.jobMode = jobMode;
  }

  public JobMode getJobMode() {
    return jobMode;
  }

  public ChangedEntitiesIdentifiersService getChangedEntitiesIdentifiersService() {
    return changedEntitiesIdentifiersService;
  }

  public SavepointOperator getSavepointOperator() {
    return savepointOperator;
  }

  public AtomicInteger getNextOffset() {
    return nextOffset;
  }

  public void setNextOffset(AtomicInteger nextOffset) {
    this.nextOffset = nextOffset;
  }
}
