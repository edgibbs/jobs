package gov.ca.cwds.neutron.launch;

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
 * This zombie killer only kills jobs in Last Change mode only, <strong>not Initial Load</strong>,
 * since the latter runs much longer than the former.
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
    final BasePersonRocket<?, ?> rocket = job.getRocket();
    final FlightPlan flightPlan = rocket.getFlightPlan();
    final Class<?> klass = rocket.getClass();

    final FlightLog flightLog = job.getRocket().getFlightLog();
    final long elapsed = System.currentTimeMillis() - flightLog.getStartTime();

    LOGGER.info("Check flight. rocket: {}, elapsed millis: {}, failed: {}, max time to abort: {}",
        klass, elapsed, flightLog.isFailed(), timeToAbort);
    if (flightPlan.isLastRunMode() && (elapsed > timeToAbort || flightLog.isFailed())) {
      try {
        LOGGER.warn("ABORT FLIGHT! rocket: {}, elapsed millis: {}, failed: {}", klass, elapsed,
            flightLog.isFailed());
        scheduler.interrupt(ctx.getJobDetail().getKey());
        LOGGER.warn("FLIGHT ABORTED! rocket: {}", klass);
      } catch (SchedulerException e) {
        LOGGER.error("FAILED TO ABORT! {} : {}", klass, e.getMessage(), e);
      }
    } else {
      LOGGER.info("Let her fly! rocket: {}, elapsed millis: {}, failed: {}, max time to abort: {}",
          klass, elapsed, flightLog.isFailed(), timeToAbort);
    }
  }

  @Override
  public void run() {
    try {
      LOGGER.info("Start zombie killer!");
      scheduler.getCurrentlyExecutingJobs().stream().sequential().forEach(this::abortRunningJob);
    } catch (SchedulerException e) {
      LOGGER.warn("SCHEDULER ERROR! {}", e.getMessage(), e);
    }
  }

}
