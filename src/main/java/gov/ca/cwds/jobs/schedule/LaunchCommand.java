package gov.ca.cwds.jobs.schedule;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weakref.jmx.Managed;

import com.google.inject.Inject;
import com.google.inject.Injector;

import gov.ca.cwds.neutron.atom.AtomCommandCenterConsole;
import gov.ca.cwds.neutron.atom.AtomFlightRecorder;
import gov.ca.cwds.neutron.atom.AtomHibernate;
import gov.ca.cwds.neutron.atom.AtomLaunchCommand;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.enums.NeutronDateTimeFormat;
import gov.ca.cwds.neutron.enums.NeutronSchedulerConstants;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.HyperCube;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.launch.LaunchCommandSettings;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.neutron.util.shrinkray.NeutronStringUtils;

/**
 * Launch rockets a la carte or on a schedule with Quartz. The master of ceremonies, AKA, Jimmy
 * Neutron.
 * 
 * @author CWDS API Team
 */
public class LaunchCommand implements AutoCloseable, AtomLaunchCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(LaunchCommand.class);

  protected static final List<String> DB_PROPERTY_LIST = Collections
      .unmodifiableList(asList("DB_NS_USER", "DB_NS_PASSWORD", "DB_NS_JDBC_URL", "DB_CMS_USER",
          "DB_CMS_PASSWORD", "DB_CMS_JDBC_URL", AtomHibernate.CURRENT_SCHEMA, "LAUNCH_DIR"));

  /**
   * Singleton instance. One launch director to rule them all!
   */
  private static LaunchCommand instance = new LaunchCommand();

  private static Function<FlightPlan, Injector> injectorMaker = HyperCube::buildInjectorFunctional;

  private static LaunchCommandSettings settings = new LaunchCommandSettings();

  private static FlightPlan standardFlightPlan;

  private FlightPlan commonFlightPlan;

  private boolean shutdownRequested;

  /**
   * <strong>HACK</strong>: make an interface for dependency injection. Add ability to swap DI
   * frameworks.
   */
  private Injector injector;

  private AtomFlightRecorder flightRecorder;

  private AtomLaunchDirector launchDirector;

  private AtomCommandCenterConsole cmdControlManager;

  private boolean fatalError;

  private LaunchCommand() {
    // no-op
  }

  @Inject
  public LaunchCommand(final AtomFlightRecorder flightRecorder,
      final AtomLaunchDirector launchDirector, final AtomCommandCenterConsole cmdControlManager) {
    this.flightRecorder = flightRecorder;
    this.launchDirector = launchDirector;
    this.cmdControlManager = cmdControlManager;
  }

  /**
   * <p>
   * <strong>MOVE</strong> this responsibility to another unit.
   * </p>
   * 
   * @param initialMode obvious
   * @param hoursInPast number of hours in past
   * @throws IOException on file read/write error
   */
  protected void resetTimestamps(boolean initialMode, int hoursInPast) throws IOException {
    final DateFormat fmt =
        new SimpleDateFormat(NeutronDateTimeFormat.LAST_RUN_DATE_FORMAT.getFormat());
    final Date now = new DateTime().minusHours(initialMode ? 876000 : hoursInPast).toDate();

    // OPTION: soft-code rockets to launch.
    for (StandardFlightSchedule sched : StandardFlightSchedule.values()) {
      final FlightPlan opts = new FlightPlan(commonFlightPlan);

      // Find the rocket's time file under the base directory:
      final StringBuilder buf = new StringBuilder();
      buf.append(Paths.get(opts.getBaseDirectory()).toString()).append(File.separatorChar)
          .append(sched.getRocketName()).append(".time");
      opts.setLastRunLoc(buf.toString());

      final File f = new File(opts.getLastRunLoc()); // NOSONAR
      final boolean fileExists = f.exists();

      if (!fileExists) {
        FileUtils.writeStringToFile(f, fmt.format(now));
      }
    }
  }

  /**
   * Return String output to JMX or other interface.
   * 
   * <p>
   * <strong>MOVE</strong> to another module.
   * </p>
   * 
   * @return readable output
   */
  @Managed(description = "Reset for initial load.")
  public String resetTimestampsForInitialLoad() {
    LOGGER.warn("RESET TIMESTAMPS FOR INITIAL LOAD!");
    try {
      resetTimestamps(true, 0);
    } catch (IOException e) {
      LOGGER.error("FAILED TO RESET TIMESTAMPS! {}", e.getMessage(), e);
      return CheeseRay.stackToString(e);
    }

    return "Reset timestamp files for initial load!";
  }

  @Managed(description = "Reset for last change.")
  public void resetTimestampsForLastChange(int hoursInPast) throws IOException {
    LOGGER.warn("RESET TIMESTAMPS FOR LAST CHANGE! hours in past: {}", hoursInPast);
    resetTimestamps(false, hoursInPast);
  }

  /**
   * <p>
   * <strong>MOVE</strong> this responsibility to another unit.
   * </p>
   * 
   * Find the rocket's time file under the base directory.
   * 
   * @param flightPlan base options
   * @param fmt reusable date format
   * @param now current datetime
   * @param sched this schedule
   * @throws IOException on file error
   */
  protected void handleSchedulerModeTimeFile(final FlightPlan flightPlan, final DateFormat fmt,
      final Date now, final StandardFlightSchedule sched) throws IOException {
    final StringBuilder buf = new StringBuilder();

    buf.append(Paths.get(flightPlan.getBaseDirectory()).toString()).append(File.separatorChar)
        .append(sched.getRocketName()).append(".time");
    final String timeFileLoc = buf.toString();
    flightPlan.setLastRunLoc(timeFileLoc);
    LOGGER.debug("base directory: {}, rocket name: {}, last run loc: {}",
        flightPlan.getBaseDirectory(), sched.getRocketName(), flightPlan.getLastRunLoc());

    // If timestamp file doesn't exist, create it.
    final File f = new File(timeFileLoc); // NOSONAR
    final boolean fileExists = f.exists();
    final boolean overrideLastRunTime = flightPlan.getOverrideLastRunStartTime() != null;

    if (!fileExists || settings.isInitialMode()) {
      FileUtils.writeStringToFile(f,
          fmt.format(overrideLastRunTime ? flightPlan.getOverrideLastRunStartTime() : now));
    }
  }

  @Override
  @Managed(description = "Stop the scheduler")
  public void stopScheduler(boolean waitForJobsToComplete) throws NeutronCheckedException {
    this.launchDirector.stopScheduler(waitForJobsToComplete);
  }

  @Override
  @Managed(description = "Start the scheduler")
  public void startScheduler() throws NeutronCheckedException {
    this.launchDirector.startScheduler();
  }

  protected void configureInitialMode(final Date now) {
    if (settings.isInitialMode()) {
      commonFlightPlan.setOverrideLastRunTime(now);
      commonFlightPlan.setLastRunMode(false);
      LOGGER.warn("\n\n\n\n>>>>>>> FULL, INITIAL LOAD! <<<<<<<\n\n\n\n");
    }
  }

  /**
   * Prepare launch pads and start the scheduler.
   * 
   * @throws NeutronCheckedException on initialization error
   */
  protected void initScheduler() throws NeutronCheckedException {
    try {
      // NOTE: make last change window configurable.
      final DateFormat fmt =
          new SimpleDateFormat(NeutronDateTimeFormat.LAST_RUN_DATE_FORMAT.getFormat());
      final Date now = settings.isInitialMode() ? fmt.parse("1917-10-31 10:11:12.000")
          : new DateTime().minusHours(NeutronSchedulerConstants.LAST_CHG_WINDOW_HOURS).toDate();

      configureInitialMode(now);
      final boolean loadPeopleIndex = commonFlightPlan.isLoadPeopleIndex();
      final Set<StandardFlightSchedule> excludeRockets = commonFlightPlan.getExcludedRockets();

      if (!excludeRockets.isEmpty()) {
        LOGGER.warn("\n\n *********** EXCLUDE ROCKETS!! {} ***********\n\n", excludeRockets);
      }

      // Turn off People rockets, if desired.
      // Prepare launch pads.
      for (StandardFlightSchedule sched : isInitialMode()
          ? StandardFlightSchedule.getInitialLoadRockets(loadPeopleIndex, excludeRockets)
          : StandardFlightSchedule.getLastChangeRockets(loadPeopleIndex, excludeRockets)) {
        final FlightPlan flightPlan = new FlightPlan(commonFlightPlan);
        handleSchedulerModeTimeFile(flightPlan, fmt, now, sched);
        launchDirector.scheduleLaunch(sched, flightPlan);
      }

      cmdControlManager.initCommandControl();

      // Cindy: "Let's light this candle!"
      if (!LaunchCommand.settings.isTestMode()) {
        LOGGER.warn("start scheduler ...");
        launchDirector.getScheduler().start();
      }

    } catch (IOException | SchedulerException | ParseException e) {
      try {
        launchDirector.getScheduler().shutdown(false);
      } catch (SchedulerException e2) {
        LOGGER.warn("SCHEDULER FALSE START! {}", e2.getMessage(), e2);
      }
      throw CheeseRay.checked(LOGGER, e, "INIT ERROR: {}", e.getMessage());
    }
  }

  /**
   * Load all rocket definitions and continue running after a rocket completes.
   * 
   * @return true if running in continuous mode
   */
  public static boolean isSchedulerMode() {
    return LaunchCommand.settings.isSchedulerMode();
  }

  /**
   * For unit tests where resources either may not close properly or where expensive resources
   * should be mocked.
   * 
   * @return whether in test mode
   */
  public static boolean isTestMode() {
    return LaunchCommand.settings.isTestMode();
  }

  /**
   * For unit tests where resources either may not close properly or where expensive resources
   * should be mocked.
   * 
   * @param mode whether in test mode
   */
  public static void setTestMode(boolean mode) {
    LaunchCommand.settings.setTestMode(mode);
  }

  public static boolean isInitialMode() {
    return LaunchCommand.settings.isInitialMode();
  }

  public FlightPlan getCommonFlightPlan() {
    return commonFlightPlan;
  }

  public void setCommonFlightPlan(FlightPlan startingOpts) {
    this.commonFlightPlan = startingOpts;
  }

  public AtomFlightRecorder getFlightRecorder() {
    return flightRecorder;
  }

  public void setFlightRecorder(AtomFlightRecorder jobHistory) {
    this.flightRecorder = jobHistory;
  }

  public AtomLaunchDirector getNeutronScheduler() {
    return launchDirector;
  }

  public void setLaunchDirector(AtomLaunchDirector launchScheduler) {
    this.launchDirector = launchScheduler;
  }

  public Scheduler getScheduler() {
    return launchDirector.getScheduler();
  }

  /**
   * One scheduler to rule them all. And in the multi-threading ... bind them? :-)
   * 
   * @return evil singleton instance
   */
  public static LaunchCommand getInstance() {
    return instance;
  }

  protected static FlightPlan parseCommandLine(String... args) throws NeutronCheckedException {
    try {
      return FlightPlan.parseCommandLine(args);
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "COMMAND LINE ERROR! {}", e.getMessage());
    }
  }

  /**
   * Populates list of System properties from corresponding Env Variables. Will not create/update
   * property if null.
   */
  public static void setSysPropsFromEnvVars() {
    for (String propName : DB_PROPERTY_LIST) {
      // Get from Env Variables by Prop Name.
      final String envVarValue = System.getenv(propName);
      if (envVarValue != null) {
        System.setProperty(propName, envVarValue);
      }
    }
  }

  /**
   * Neutron even constructs and inject dependencies into the LaunchCommand instance, though this
   * may be confusing.
   * 
   * @param flightPlan rocket flight plan
   * @return LaunchCommand instance with all dependencies injected
   * @throws NeutronCheckedException on general error
   */
  protected static LaunchCommand buildCommandCenter(final FlightPlan flightPlan)
      throws NeutronCheckedException {
    Injector injector;
    try {
      injector = injectorMaker.apply(flightPlan);
      instance = injector.getInstance(LaunchCommand.class);
      instance.commonFlightPlan = flightPlan;
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "LAUNCH COMMAND FAILURE! {}", e.getMessage());
    }

    return instance;
  }

  /**
   * <p>
   * <strong>Single launch mode</strong>: close resources and exit JVM.
   * </p>
   * <p>
   * <strong>Continuous launch mode</strong>: close resources and exit JVM only on fatal startup
   * error.
   * </p>
   */
  @Override
  public void close() throws Exception {
    if (!isTestMode() && (!isSchedulerMode() || fatalError || shutdownRequested)) {
      // Shutdown all remaining resources, even those not attached to this rocket.
      final int exitCode = this.fatalError ? -1 : 0;
      LOGGER.warn("\n>>>>>>>>>> SHUT DOWN LAUNCH COMMAND! Exit code: {}", exitCode);
      System.exit(exitCode); // NOSONAR
    }
  }

  @Override
  public void shutdown() throws NeutronCheckedException {
    LOGGER.info("\n>>>>>>>>>> SHUTDOWN REQUESTED!");
    try {
      this.shutdownRequested = true;
      launchDirector.stopScheduler(false);
      close();
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "FAILED TO SHUTDOWN!: {}", e.getMessage());
    }
  }

  /**
   * Run continuous, scheduler mode.
   * 
   * @return launch command instance with dependencies injected
   * @throws NeutronCheckedException on launch error
   */
  protected static synchronized LaunchCommand startSchedulerMode() throws NeutronCheckedException {
    LOGGER.info("STARTING LAUNCH COMMAND ...");

    // HACK: inject a mock scheduler instead.
    if (standardFlightPlan.isSimulateLaunch()) {
      return instance; // Test "main" methods
    }

    LaunchCommand.settings.setSchedulerMode(true);
    LaunchCommand.settings.setInitialMode(!standardFlightPlan.isLastRunMode());
    final Injector injector = injectorMaker.apply(standardFlightPlan);

    try (final LaunchCommand launchCommand = buildCommandCenter(standardFlightPlan)) {
      instance = injector.getInstance(LaunchCommand.class);
      instance.commonFlightPlan = new FlightPlan(standardFlightPlan);
      instance.injector = injector;

      instance.initScheduler();
      instance.fatalError = false; // Good to go
    } catch (Throwable e) { // NOSONAR
      // Intentionally catch a Throwable, not an Exception.
      // Forcibly close orphaned resources, if necessary, by system exit.
      instance.fatalError = true;
      throw CheeseRay.checked(LOGGER, e, "CRITICAL START ERROR!: {}", e.getMessage());
    }

    LOGGER.info("LAUNCH COMMAND STARTED!");
    return instance;
  }

  /**
   * Entry point for expendable, one shot rockets, typically for initial load. Not used in
   * continuous mode.
   * 
   * <p>
   * This method automatically closes the Hibernate session factory and ElasticSearch DAO and EXITs
   * the JVM.
   * </p>
   * 
   * @param klass rocket class
   * @param args command line arguments
   * @param <T> Person persistence type
   * @throws NeutronCheckedException rocket failure
   */
  public static <T extends BasePersonRocket<?, ?>> void launchOneWayTrip(final Class<T> klass,
      String... args) throws NeutronCheckedException {
    setSysPropsFromEnvVars();
    standardFlightPlan = parseCommandLine(args);

    final Optional<String> opt = NeutronStringUtils.filePath(standardFlightPlan.getLastRunLoc());
    System.setProperty("LAUNCH_DIR", opt.isPresent() ? opt.get() : "./jobrunner/");

    LaunchCommand.settings.setSchedulerMode(false);
    LaunchCommand.settings.setInitialMode(!standardFlightPlan.isLastRunMode());
    instance.fatalError = true; // Murphy was an optimist.

    if (standardFlightPlan.isSimulateLaunch()) {
      return; // Test "main" methods
    }

    try (final LaunchCommand launchCommand = buildCommandCenter(standardFlightPlan);
        final T rocket = HyperCube.newRocket(klass, args)) {
      rocket.setFlightPlan(standardFlightPlan);
      rocket.run();
      launchCommand.fatalError = false; // We made it. Almost.
    } catch (Throwable e) { // NOSONAR
      // Intentionally catch a Throwable, not an Exception.
      // Forcibly close orphaned resources, if necessary, by system exit.
      instance.fatalError = true;
      throw CheeseRay.runtime(LOGGER, e, "ROCKET EXPLODED!: {}", e.getMessage());
    }
  }

  public static Function<FlightPlan, Injector> getInjectorMaker() {
    return LaunchCommand.injectorMaker;
  }

  public static void setInjectorMaker(Function<FlightPlan, Injector> makeLaunchCommand) {
    LaunchCommand.injectorMaker = makeLaunchCommand;
  }

  public Injector getInjector() {
    return injector;
  }

  public void setInjector(Injector injector) {
    this.injector = injector;
  }

  public static LaunchCommandSettings getSettings() {
    return LaunchCommand.settings;
  }

  public static void setSettings(LaunchCommandSettings settings) {
    LaunchCommand.settings = settings;
  }

  public static FlightPlan getStandardFlightPlan() {
    return standardFlightPlan;
  }

  public static void setStandardFlightPlan(FlightPlan standardFlightPlan) {
    LaunchCommand.standardFlightPlan = standardFlightPlan;
  }

  /**
   * Customize flight configurations per rocket type.
   * <p>
   * OPTION: load an optional configuration file per rocket.
   * </p>
   * 
   * @param args command line
   * @throws Exception unhandled error
   */
  public static void main(String[] args) throws Exception {
    setSysPropsFromEnvVars();
    standardFlightPlan = parseCommandLine(args);
    final String baseDir = standardFlightPlan.getBaseDirectory();
    LaunchCommand.settings.setBaseDirectory(baseDir);
    startSchedulerMode();
  }

}
