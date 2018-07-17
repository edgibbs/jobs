package gov.ca.cwds.neutron.launch;

import java.util.List;
import java.util.TimerTask;

import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;

/**
 * Timer task to abort stalled or runaway rockets.
 * 
 * @author CWDS API Team
 */
public class AbortFlightTimerTask extends TimerTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbortFlightTimerTask.class);

  private final Scheduler scheduler;

  private final int timeToAbort;

  @Inject
  public AbortFlightTimerTask(Scheduler scheduler,
      @Named("zombie.killer.killAtMillis") String strTimeToAbort) {
    this.scheduler = scheduler;

    final int iTimeToAbort = Integer.parseInt(strTimeToAbort);
    LOGGER.info("Zombie Killer! iTimeToAbort: {}", iTimeToAbort);
    this.timeToAbort = iTimeToAbort > 0 ? iTimeToAbort : (15 * 60 * 1000); // fifteen minutes
  }

  protected void abortRunningJob(JobExecutionContext ctx) {
    final NeutronRocket job = (NeutronRocket) ctx.getJobInstance();
    final FlightLog flightLog = job.getRocket().getFlightLog();
    final BasePersonRocket<?, ?> rocket = job.getRocket();
    final FlightPlan flightPlan = job.getRocket().getFlightPlan();

    if (flightPlan.isLastRunMode() && (flightLog.isRunning() && ctx.getJobRunTime() > timeToAbort)
        || flightLog.isFailed()) {
      try {
        LOGGER.warn("ABORT ROCKET! rocket: {}", rocket.getClass());
        scheduler.interrupt(ctx.getJobDetail().getKey());
      } catch (SchedulerException e) {
        LOGGER.error("FAILED TO ABORT! {} : {}", rocket.getClass(), e.getMessage(), e);
      }
    } else {
      LOGGER.debug("Keep flying. rocket: {}", rocket.getClass());
    }
  }

  @Override
  public void run() {
    try {
      LOGGER.info("Run zombie killer!");
      final List<JobExecutionContext> currentlyExecuting = scheduler.getCurrentlyExecutingJobs();
      currentlyExecuting.stream().sequential().forEach(this::abortRunningJob);
    } catch (SchedulerException e) {
      LOGGER.warn("SCHEDULER ERROR! {}", e.getMessage(), e);
    }
  }

}
