package gov.ca.cwds.neutron.launch.listener;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeutronSchedulerListener implements SchedulerListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronSchedulerListener.class);

  @Override
  public void jobScheduled(Trigger trigger) {
    LOGGER.debug("job scheduled");
  }

  @Override
  public void jobUnscheduled(TriggerKey triggerKey) {
    LOGGER.debug("job unscheduled");
  }

  @Override
  public void triggerFinalized(Trigger trigger) {
    LOGGER.debug("trigger finalized");
  }

  @Override
  public void triggerPaused(TriggerKey triggerKey) {
    LOGGER.debug("trigger paused");
  }

  @Override
  public void triggersPaused(String triggerGroup) {
    LOGGER.debug("triggers paused");
  }

  @Override
  public void triggerResumed(TriggerKey triggerKey) {
    LOGGER.debug("trigger resumed");
  }

  @Override
  public void triggersResumed(String triggerGroup) {
    LOGGER.debug("triggers resumed");
  }

  @Override
  public void jobAdded(JobDetail jobDetail) {
    LOGGER.debug("job added");
  }

  @Override
  public void jobDeleted(JobKey jobKey) {
    LOGGER.debug("job deleted");
  }

  @Override
  public void jobPaused(JobKey jobKey) {
    LOGGER.debug("job paused");
  }

  @Override
  public void jobsPaused(String jobGroup) {
    LOGGER.debug("jobs paused");
  }

  @Override
  public void jobResumed(JobKey jobKey) {
    LOGGER.debug("job resumed");
  }

  @Override
  public void jobsResumed(String jobGroup) {
    LOGGER.debug("jobs resumed");
  }

  @Override
  public void schedulerError(String msg, SchedulerException cause) {
    LOGGER.warn("scheduler error: {}", msg, cause);

    // NO ZOMBIES!
    // On OutOfMemoryError, exit and dump heap.
    if (cause.getCause() != null) {
      final Throwable t = cause.getCause();
      if (t instanceof OutOfMemoryError) {
        LOGGER.error("\n\nOUT OF MEMORY! EXIT!\n\n", cause);
        System.exit(-1);
      }
    }
  }

  @Override
  public void schedulerInStandbyMode() {
    LOGGER.debug("scheduler in standby mode");
  }

  @Override
  public void schedulerStarted() {
    LOGGER.debug("scheduler started");
  }

  @Override
  public void schedulerStarting() {
    LOGGER.debug("scheduler starting");
  }

  @Override
  public void schedulerShutdown() {
    LOGGER.debug("scheduler shutdown");
  }

  @Override
  public void schedulerShuttingdown() {
    LOGGER.debug("scheduler shuttingdown");
  }

  @Override
  public void schedulingDataCleared() {
    LOGGER.debug("scheduling data cleared");
  }

}
