package gov.ca.cwds.neutron.launch;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomFlightRecorder;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.neutron.util.NeutronThreadUtils;

/**
 * Neutron implementation of Quartz {@link InterruptableJob} for scheduled flights.
 * 
 * @author CWDS API Team
 * @see LaunchCommand
 */
@DisallowConcurrentExecution
public class NeutronRocket implements InterruptableJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronRocket.class);

  private static final AtomicInteger instanceCounter = new AtomicInteger(0);

  private final int instanceNumber = instanceCounter.incrementAndGet();

  /**
   * <strong>SNAP-820:</strong> must clear this variable for gc.
   */
  @SuppressWarnings("rawtypes")
  private BasePersonRocket rocket;

  private final AtomFlightRecorder flightRecorder;

  private final StandardFlightSchedule flightSchedule;

  // "volatile" shows changes immediately to other threads.
  private volatile FlightLog flightLog = new FlightLog();

  /**
   * Constructor.
   * 
   * @param <T> ES replicated Person persistence class
   * @param <M> MQT entity class, if any, or T
   * @param rocket launch me!
   * @param flightSchedule flight schedule
   * @param flightRecorder common flight recorder
   */
  public <T extends PersistentObject, M extends ApiGroupNormalizer<?>> NeutronRocket(
      final BasePersonRocket<T, M> rocket, final StandardFlightSchedule flightSchedule,
      final AtomFlightRecorder flightRecorder) {
    this.rocket = rocket;
    this.flightSchedule = flightSchedule;
    this.flightRecorder = flightRecorder;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    final JobDataMap map = context.getJobDetail().getJobDataMap();
    final String rocketName = context.getTrigger().getJobKey().getName();
    final String origThreadName = "" + Thread.currentThread().getName();

    NeutronThreadUtils.nameThread(rocketName, this);
    LOGGER.info("\n\t>>>> LAUNCH! {}, instance # {}\n", rocket.getClass().getName(),
        instanceNumber);

    try (final BasePersonRocket flight = rocket) {
      MDC.put("rocketLog", rocketName);
      flightLog = rocket.getFlightLog();
      flightLog.setRocketName(rocketName);
      flightLog.start();

      // Job parameter data:
      map.put("opts", flight.getFlightPlan());
      map.put("track", flightLog);
      context.setResult(flightLog);

      // SNAP-820: NEXT: launch rocket in interruptible thread.
      flight.run();
      flight.done();
      LOGGER.info("HAPPY LANDING! rocket: {}", rocketName);
    } catch (Exception e) {
      flightLog.fail();
      LOGGER.error("FAILURE TO LAUNCH! rocket: {}", rocketName, e);
      throw new JobExecutionException("FAILURE TO LAUNCH! rocket: " + rocketName, e);
    } finally {
      final BasePersonRocket gcRocket = rocket;
      this.rocket = null; // SNAP-820: potential memory leak
      flightRecorder.logFlight(flightSchedule.getRocketClass(), flightLog);
      flightRecorder.summarizeFlight(flightSchedule, flightLog);

      try {
        if (!flightLog.isInitialLoad() && !StringUtils.isBlank(gcRocket.getEventType())) {
          flightLog.notifyMonitor(gcRocket.getEventType());
        }
      } finally {
        LOGGER.info("FLIGHT SUMMARY: rocket: {}\n{}", rocketName, flightLog);
        MDC.remove("rocketLog"); // remove the logging context, no matter what happens
        NeutronThreadUtils.nameThread(origThreadName);
      }
    }
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    LOGGER.warn("ABORT FLIGHT! rocket: {}", getRocket().getClass());
    getFlightLog().fail();
  }

  public FlightLog getFlightLog() {
    return flightLog;
  }

  public void setFlightLog(FlightLog track) {
    this.flightLog = track;
  }

  public BasePersonRocket getRocket() {
    return rocket;
  }

  public int getInstanceNumber() {
    return instanceNumber;
  }

}
