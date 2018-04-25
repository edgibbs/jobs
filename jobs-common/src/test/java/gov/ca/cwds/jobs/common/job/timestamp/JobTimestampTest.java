package gov.ca.cwds.jobs.common.job.timestamp;

import static gov.ca.cwds.jobs.common.job.BatchTestSavePointBatchIterator.BROKEN_ENTITY;
import static gov.ca.cwds.jobs.common.job.BatchTestSavePointBatchIterator.SECOND_TIMESTAMP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import gov.ca.cwds.jobs.common.exception.JobsException;
import gov.ca.cwds.jobs.common.inject.JobRunner;
import gov.ca.cwds.jobs.common.job.BatchTestSavePointBatchIterator;
import gov.ca.cwds.jobs.common.job.TestModule;
import gov.ca.cwds.jobs.common.job.identifiers.EmptyTimestampChangedIdentifiersService;
import gov.ca.cwds.jobs.common.job.identifiers.SingleBatchChangedEntitiesIdentifiersService;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Alexander Serbin on 2/14/2018.
 */
public class JobTimestampTest {

  private FilesystemSavepointOperator timestampOperator;
  private LastRunDirHelper lastRunDirHelper = new LastRunDirHelper("temp");

  @Before
  public void beforeMethod() throws IOException {
    timestampOperator = new FilesystemSavepointOperator(
        lastRunDirHelper.getLastRunDir().toString());
    lastRunDirHelper.createTimestampDirectory();
  }

  @After
  public void afterMethod() throws IOException {
    lastRunDirHelper.deleteTimestampDirectory();
  }

  @Test
  public void test_timestamp_is_created_if_job_successful() throws IOException {
    assertFalse(timestampOperator.savepointExists());
    TestModule testModule = new TestModule(getModuleArgs());
    testModule
        .setChangedEntitiesIdentifiersClass(SingleBatchChangedEntitiesIdentifiersService.class);
    JobRunner.run(testModule);
    assertTrue(timestampOperator.savepointExists());
    LocalDateTime timestamp = timestampOperator.readTimestamp();
    assertTrue(timestamp.until(LocalDateTime.now(), ChronoUnit.SECONDS) < 1);
  }

  @Test(expected = IllegalStateException.class)
  public void test_timestamp_is_not_created_if_crash_happens()
      throws IOException {
    try {
      assertFalse(timestampOperator.savepointExists());
      runCrashingJob();
    } finally {
      assertFalse(timestampOperator.savepointExists());
    }
  }

  @Test(expected = IllegalStateException.class)
  public void test_timestamp_is_not_updated_if_crash_happens()
      throws IOException {
    LocalDateTime timestamp = LocalDateTime.of(2017, 1, 20, 5, 25);
    try {
      assertFalse(timestampOperator.savepointExists());
      timestampOperator.writeTimestamp(timestamp);
      runCrashingJob();
    } finally {
      assertTrue(timestampOperator.savepointExists());
      assertTrue(timestamp.equals(timestampOperator.readTimestamp()));
    }
  }

  @Test(expected = JobsException.class)
  public void last_successfull_batch_save_point_test() {
    try {
      assertFalse(timestampOperator.savepointExists());
      TestModule testModule = new TestModule(getModuleArgs());
      testModule
          .setChangedEntitiesIdentifiersClass(SingleBatchChangedEntitiesIdentifiersService.class);
      testModule.setJobBatchIteratorClass(BatchTestSavePointBatchIterator.class);
      testModule.setChangedEntityService(identifier -> {
        if (identifier == BROKEN_ENTITY) {
          return BROKEN_ENTITY;
        } else {
          return new Object();
        }
      });
      testModule.setBulkWriter(items -> {
        if (!items.isEmpty() && items.get(0) == BROKEN_ENTITY) {
          throw new IllegalStateException("Broken batch");
        }
        ;
      });
      JobRunner.run(testModule);
    } finally {
      assertTrue(timestampOperator.savepointExists());
      assertEquals(SECOND_TIMESTAMP, timestampOperator.readTimestamp());
    }
  }

  @Test
  public void test_all_timestamps_null() {
    assertFalse(timestampOperator.savepointExists());
    TestModule testModule = new TestModule(getModuleArgs());
    testModule.setChangedEntitiesIdentifiersClass(EmptyTimestampChangedIdentifiersService.class);
    JobRunner.run(testModule);
    assertTrue(timestampOperator.savepointExists());
    LocalDateTime timestamp = timestampOperator.readTimestamp();
    assertTrue(timestamp.until(LocalDateTime.now(), ChronoUnit.SECONDS) < 1);
  }

  private void runCrashingJob() {
    TestModule testModule = new TestModule(getModuleArgs());
    testModule
        .setChangedEntitiesIdentifiersClass(SingleBatchChangedEntitiesIdentifiersService.class);
    testModule.setBulkWriter(items -> {
      if (true) {
        throw new IllegalStateException();
      }
    });
    JobRunner.run(testModule);
  }

  private String[] getModuleArgs() {
    String configFilePath = Paths.get("src", "test", "resources", "config.yaml").normalize()
        .toAbsolutePath().toString();
    return new String[]{"-c", configFilePath, "-l", lastRunDirHelper.getLastRunDir().toString()};
  }

}
