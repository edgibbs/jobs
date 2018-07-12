package gov.ca.cwds.neutron.launch;

import java.util.List;
import java.util.TimerTask;

import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;

/**
 * Timer task to abort runaway rockets.
 * 
 * @author CWDS API Team
 */
public class AbortFlightTimerTask extends TimerTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbortFlightTimerTask.class);

  private final LaunchDirector director;

  @Inject
  public AbortFlightTimerTask(LaunchDirector director) {
    this.director = director;
  }

  protected void abortRunningJob(JobExecutionContext ctx) {
    final NeutronRocket job = (NeutronRocket) ctx.getJobInstance();
    final FlightLog flightLog = job.getRocket().getFlightLog();
    final int fifteenMinutes = (15 * 60 * 1000);
    final BasePersonRocket<?, ?> rocket = job.getRocket();
    final FlightPlan flightPlan = job.getRocket().getFlightPlan();

    if (flightPlan.isLastRunMode()
        && (flightLog.isRunning() && ctx.getJobRunTime() > fifteenMinutes)
        || flightLog.isFailed()) {
      LOGGER.warn("ABORT ROCKET! rocket: {}", rocket.getClass());
    }
  }

  @Override
  public void run() {
    final Scheduler scheduler = director.getScheduler();

    try {
      final List<JobExecutionContext> currentlyExecuting = scheduler.getCurrentlyExecutingJobs();
      currentlyExecuting.stream().sequential().forEach(this::abortRunningJob);
    } catch (SchedulerException e) {
      LOGGER.warn("SCHEDULER ERROR! {}", e.getMessage(), e);
    }
  }

}
