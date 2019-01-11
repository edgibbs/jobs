package gov.ca.cwds.neutron.launch;

import java.util.Deque;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomFlightPlanManager;
import gov.ca.cwds.neutron.atom.AtomFlightRecorder;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.atom.AtomLaunchPad;
import gov.ca.cwds.neutron.atom.AtomRocketFactory;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.neutron.util.shrinkray.NeutronClassFinder;

/**
 * Singleton implementation of {@link AtomLaunchDirector}. Runs the Quartz scheduler, tracks
 * {@link LaunchPad}'s, and schedules rocket launches.
 * 
 * <p>
 * Schedules an instance of {@link ZombieKillerTimerTask} to run every
 * {@code "zombie.killer.checkEveryMillis"} and abort zombie flights running longer than
 * {@code "zombie.killer.killAtMillis"}.
 * </p>
 * 
 * @author CWDS API Team
 * @see AtomFlightRecorder
 */
@Singleton
public class LaunchDirector implements AtomLaunchDirector {

  private static final Logger LOGGER = LoggerFactory.getLogger(LaunchDirector.class);

  /**
   * Quartz scheduler. Could hide this implementation behind an interface.
   */
  private Scheduler scheduler;

  private final AtomFlightRecorder flightRecorder;

  private final AtomRocketFactory rocketFactory;

  private final AtomFlightPlanManager flightPlanManger;

  private FlightPlan flightPlan;

  /**
   * Schedule launch pads.
   */
  private final Map<Class<?>, AtomLaunchPad> launchPads = new ConcurrentHashMap<>();

  private Deque<String> dequeRerunIds = new ConcurrentLinkedDeque<>();

  /**
   * Might not be necessary. Listeners and running jobs should handle this, but we still need a
   * single place to track rockets in flight.
   * 
   * <p>
   * OPTION: Quartz scheduler can track this too. Obsolete implementation?
   * </p>
   * 
   * <p>
   * SNAP-820: memory leak, if rockets are not removed.
   * </p>
   */
  private final Map<TriggerKey, NeutronRocket> rocketsInFlight = new ConcurrentHashMap<>();

  private Timer abortFlightTimer;

  @Inject
  public LaunchDirector(final AtomFlightRecorder flightRecorder,
      final AtomRocketFactory rocketFactory, final AtomFlightPlanManager flightPlanManager,
      ZombieKillerTimerTask timerTask,
      @Named("zombie.killer.checkEveryMillis") String zombieKillerMillis,
      @Named("rerun.deque.ids") Deque<String> rerunIds) {
    this.flightRecorder = flightRecorder;
    this.rocketFactory = rocketFactory;
    this.flightPlanManger = flightPlanManager;

    // Schedule Zombie Killer in Last Change mode only.
    if (!LaunchCommand.isInitialMode()) {
      LOGGER.warn("Schedule Zombie Killer: zombieKillerMillis: {}", zombieKillerMillis);
      this.abortFlightTimer = new Timer("abort_rocket_timer", true);
      final int iZombieKillerMillis = Integer.parseInt(zombieKillerMillis);
      this.abortFlightTimer.scheduleAtFixedRate(timerTask, iZombieKillerMillis,
          iZombieKillerMillis);
    }

    if (rerunIds != null) {
      dequeRerunIds = rerunIds;
    }
  }

  /**
   * Prepare a registered rocket.
   * 
   * @param klass rocket class
   * @param flightPlan command line arguments
   * @return a fueled rocket
   * @throws NeutronCheckedException unexpected runtime error
   */
  @SuppressWarnings("rawtypes")
  public BasePersonRocket fuelRocket(final Class<?> klass, final FlightPlan flightPlan)
      throws NeutronCheckedException {
    return rocketFactory.fuelRocket(klass, flightPlan);
  }

  /**
   * Create a registered rocket.
   * 
   * @param rocketName batch rocket class
   * @param flightPlan command line arguments
   * @return a fueled rocket
   * @throws NeutronCheckedException unexpected runtime error
   */
  @SuppressWarnings("rawtypes")
  public BasePersonRocket fuelRocket(final String rocketName, final FlightPlan flightPlan)
      throws NeutronCheckedException {
    return rocketFactory.fuelRocket(rocketName, flightPlan);
  }

  @Override
  public void prepareLaunchPads() {
    // Not in use.
  }

  @Override
  public FlightLog launch(Class<?> klass, final FlightPlan flightPlan)
      throws NeutronCheckedException {
    try {
      LOGGER.info("LAUNCH SCHEDULED ROCKET! {}", klass.getName());
      flightPlan.setDequeRerunIds(dequeRerunIds); // key re-runs
      final BasePersonRocket<?, ?> rocket = fuelRocket(klass, flightPlan);
      rocket.run();
      return rocket.getFlightLog();
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "SCHEDULED LAUNCH FAILED!: {}", e.getMessage());
    }
  }

  @Override
  public FlightLog launch(String rocketName, final FlightPlan flightPlan)
      throws NeutronCheckedException {
    return launch(NeutronClassFinder.classForName(rocketName), flightPlan);
  }

  @Override
  public AtomLaunchPad scheduleLaunch(StandardFlightSchedule sched, FlightPlan flightPlan)
      throws NeutronCheckedException {
    flightPlan.setDequeRerunIds(dequeRerunIds); // key re-runs
    final LaunchPad pad = new LaunchPad(this, sched, flightPlan, dequeRerunIds);
    final Class<?> klass = sched.getRocketClass();
    launchPads.put(klass, pad);
    flightPlanManger.addFlightPlan(klass, flightPlan);
    pad.schedule();

    return pad;
  }

  @Override
  public void stopScheduler(boolean waitForJobsToComplete) throws NeutronCheckedException {
    LOGGER.warn("STOP SCHEDULER! wait for jobs to complete: {}", waitForJobsToComplete);
    try {
      getScheduler().shutdown(waitForJobsToComplete);
    } catch (SchedulerException e) {
      throw CheeseRay.checked(LOGGER, e, "FAILED TO STOP SCHEDULER! {}", e.getMessage());
    }
  }

  @Override
  public void startScheduler() throws NeutronCheckedException {
    LOGGER.warn("START SCHEDULER!");
    try {
      getScheduler().start();
    } catch (Exception e) {
      LOGGER.error("FAILED TO START SCHEDULER! {}", e.getMessage(), e);
      throw CheeseRay.checked(LOGGER, e, "FAILED TO START SCHEDULER! {}", e.getMessage());
    }
  }

  @Override
  public void markRocketAsInFlight(final TriggerKey key, NeutronRocket rocket) {
    // rocketsInFlight.put(key, rocket);
  }

  /**
   * QUESTION: is this needed?
   * 
   * @param key trigger key
   */
  public void removeExecutingJob(final TriggerKey key) {
    // if (rocketsInFlight.containsKey(key)) {
    // rocketsInFlight.remove(key);
    // }
  }

  public Map<TriggerKey, NeutronRocket> getRocketsInFlight() {
    return rocketsInFlight;
  }

  public AtomRocketFactory getRocketFactory() {
    return rocketFactory;
  }

  public FlightPlan getFlightPlan() {
    return flightPlan;
  }

  public void setFlightPlan(FlightPlan flightPlan) {
    this.flightPlan = flightPlan;
  }

  @Override
  public Scheduler getScheduler() {
    return scheduler;
  }

  public void setScheduler(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Override
  public Map<Class<?>, AtomLaunchPad> getLaunchPads() {
    return launchPads;
  }

  @Override
  public boolean isLaunchVetoed(String className) throws NeutronCheckedException {
    return this.getLaunchPads().get(NeutronClassFinder.classForName(className)).isVetoExecution();
  }

  @Override
  public AtomFlightPlanManager getFlightPlanManger() {
    return flightPlanManger;
  }

  @Override
  public AtomFlightRecorder getFlightRecorder() {
    return flightRecorder;
  }

  public Timer getAbortFlightTimer() {
    return abortFlightTimer;
  }

  public void setAbortFlightTimer(Timer abortFlightTimer) {
    this.abortFlightTimer = abortFlightTimer;
  }

}
