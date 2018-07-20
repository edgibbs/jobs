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
 * <p>
 * This zombie killer only kills jobs in Last Change mode, <strong>not Initial Load</strong>, since
 * the latter runs much longer than the former.
 * </p>
 * 
 * @author CWDS API Team
 */
public class ZombieKillerTimerTask extends TimerTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZombieKillerTimerTask.class);

  private final Scheduler scheduler;

  private final int timeToAbort;

  @Inject
  public ZombieKillerTimerTask(Scheduler scheduler,
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
    final FlightPlan flightPlan = rocket.getFlightPlan();
    final Class<?> klass = rocket.getClass();

    if ((flightPlan.isLastRunMode() && (flightLog.isRunning() && ctx.getJobRunTime() > timeToAbort))
        || flightLog.isFailed()) {
      try {
        LOGGER.warn("ABORT ROCKET! rocket: {}", klass);
        scheduler.interrupt(ctx.getJobDetail().getKey());
      } catch (SchedulerException e) {
        LOGGER.error("FAILED TO ABORT! {} : {}", klass, e.getMessage(), e);
      }
    } else {
      LOGGER.info("Keep flying. rocket: {}", klass);
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
