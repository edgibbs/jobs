package gov.ca.cwds.neutron.launch;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weakref.jmx.Managed;

import com.google.inject.Inject;

import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomFlightRecorder;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.atom.AtomLaunchPad;
import gov.ca.cwds.neutron.enums.NeutronDateTimeFormat;
import gov.ca.cwds.neutron.enums.NeutronSchedulerConstants;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.vox.jmx.VoxLaunchPadMBean;

/**
 * Everything required to launch a rocket.
 * 
 * <p>
 * Exposes methods to JMX via Vox.
 * </p>
 * 
 * @author CWDS API Team
 */
public class LaunchPad implements VoxLaunchPadMBean, AtomLaunchPad {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(LaunchPad.class);

  private transient Scheduler scheduler;

  private transient AtomLaunchDirector launchDirector;
  private final AtomFlightRecorder flightRecorder;

  private final StandardFlightSchedule flightSchedule;
  private FlightPlan flightPlan;

  private final String rocketName;
  private final String triggerName;

  private final TriggerKey triggerKey;
  private volatile JobKey jobKey;
  private volatile JobDetail jd;

  private boolean vetoExecution;

  @Inject
  public LaunchPad(final AtomLaunchDirector director, StandardFlightSchedule sched,
      final FlightPlan flightPlan) {
    this.launchDirector = director;
    this.scheduler = director.getScheduler();
    this.flightRecorder = director.getFlightRecorder();

    this.flightSchedule = sched;
    this.flightPlan = flightPlan;

    this.rocketName = flightSchedule.getRocketName();
    this.jobKey = new JobKey(rocketName, NeutronSchedulerConstants.GRP_LST_CHG);
    this.triggerName = flightSchedule.getRocketName();
    triggerKey = new TriggerKey(triggerName, NeutronSchedulerConstants.GRP_LST_CHG);

    final FlightLog flightLog = new FlightLog();
    flightLog.setRocketName(sched.getRocketName());

    // Seed the flight log history.
    flightRecorder.logFlight(sched.getRocketClass(), flightLog);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Managed(description = "Launch rocket now and show results immediately."
      + "\nparam: rocket command line."
      + "\nexample: -c config/local.yaml -l /Users/dsmith/client_indexer_time.txt -s=true")
  public String run(String cmdLine) throws NeutronCheckedException {
    try {
      LOGGER.info("LAUNCH ONE-WAY TRIP! {}", flightSchedule.getRocketName());
      final FlightPlan plan =
          FlightPlan.parseCommandLine(StringUtils.isBlank(cmdLine) ? null : cmdLine.split("\\s+"));
      final FlightLog flightLog = this.launchDirector.launch(flightSchedule.getRocketClass(), plan);
      return flightLog.toJson();
    } catch (Exception e) {
      LOGGER.error("FAILED TO LAUNCH ON DEMAND! {}", e.getMessage(), e);
      return CheeseRay.stackToString(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Managed(description = "Schedule rocket launch")
  public void schedule() throws NeutronCheckedException {
    LOGGER.debug("ATTEMPT TO SCHEDULE LAUNCH! {}", rocketName);
    try {
      if (scheduler.checkExists(this.jobKey)) {
        LOGGER.warn("ROCKET ALREADY SCHEDULED! rocket: {}", rocketName);
        return;
      }

      final String rocketClass = flightSchedule.getRocketClass().getName();

      // Rocket detail.
      jd = newJob(NeutronRocket.class)
          .withIdentity(rocketName,
              LaunchCommand.isInitialMode() ? NeutronSchedulerConstants.GRP_FULL_LOAD
                  : NeutronSchedulerConstants.GRP_LST_CHG)
          .usingJobData(NeutronSchedulerConstants.ROCKET_CLASS, rocketClass).storeDurably().build();

      // Schedule triggers.
      if (!LaunchCommand.isInitialMode()) {
        scheduler.scheduleJob(jd,
            newTrigger().withIdentity(triggerName, NeutronSchedulerConstants.GRP_LST_CHG)
                .withPriority(flightSchedule.getLastRunPriority())
                .withSchedule(simpleSchedule()
                    .withIntervalInSeconds(flightSchedule.getWaitPeriodSeconds()).repeatForever())
                .startAt(DateTime.now().plusSeconds(flightSchedule.getStartDelaySeconds()).toDate())
                .build());
        LOGGER.info("Scheduled trigger {}", rocketName);
      } else {
        // HACK: cleaner way?
        if (flightSchedule.getInitialLoadOrder() == 0) {
          final Trigger trigger =
              newTrigger().withIdentity(rocketName, NeutronSchedulerConstants.GRP_FULL_LOAD)
                  .startAt(DateTime.now().toDate()).build();
          scheduler.scheduleJob(jd, trigger);
        } else {
          scheduler.addJob(jd, false, false);
        }
      }

    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "FAILURE TO LAUNCH! rocket: {}", rocketName);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Managed(description = "Unschedule rocket")
  public void unschedule() throws NeutronCheckedException {
    try {
      LOGGER.warn("UNSCHEDULE LAUNCH! {}", rocketName);
      scheduler.unscheduleJob(triggerKey);
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "UNSCHEDULED LAUNCH! rocket: {}", rocketName);
    }
  }

  // =======================
  // STATUS/HISTORY/LOG:
  // =======================

  @Managed(description = "Show rocket flight statistics across flights")
  @Override
  public String summary() {
    try {
      return flightRecorder.getFlightSummary(this.flightSchedule).toJson();
    } catch (Exception e) {
      LOGGER.error("UNABLE TO SHOW FLIGHT SUMMARY! {}", e.getMessage(), e);
      return CheeseRay.stackToString(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Managed(description = "Show rocket's last flight status")
  public String status() {
    LOGGER.warn("SHOW ROCKET STATUS! {}", rocketName);
    return flightRecorder.getLastFlightLog(this.flightSchedule.getRocketClass()).toJson();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Managed(description = "Show rocket's flight log history")
  public String history() {
    LOGGER.warn("SHOW ROCKET FLIGHT HISTORY! {}", rocketName);
    final StringBuilder buf = new StringBuilder();
    buf.append("{[").append(flightRecorder.getFlightLogHistory(this.flightSchedule.getRocketClass())
        .stream().map(FlightLog::toJson).collect(Collectors.joining(","))).append("]}");
    return buf.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Managed(description = "Show rocket's last flight log")
  public String logs() {
    LOGGER.warn("SHOW FLIGHT LOG! {}", rocketName);
    final StringBuilder buf = new StringBuilder();
    buf.append(this.getFlightPlan().getBaseDirectory()).append(File.separator).append("rocketlog")
        .append(File.separator).append(flightSchedule.getRocketName()).append(".log");

    final StringWriter sw = new StringWriter();
    final String elPath = buf.toString();
    final Path pathIn = Paths.get(elPath);
    LOGGER.info("log path: {}", elPath);

    try (final Stream<String> lines = Files.lines(pathIn);
        final PrintWriter w = new PrintWriter(sw)) {
      lines.sequential().forEach(w::println);
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "ERROR READING LOGS! {}", flightSchedule.getRocketName());
    }

    return sw.toString();
  }

  protected void resetTimestamp(boolean initialMode, int hoursInPast) throws IOException {
    final DateFormat fmt =
        new SimpleDateFormat(NeutronDateTimeFormat.LAST_RUN_DATE_FORMAT.getFormat());
    final Date now = new DateTime().minusHours(initialMode ? 876000 : hoursInPast).toDate();

    // Find the rocket's time file under the base directory:
    final StringBuilder buf = new StringBuilder();
    buf.append(flightPlan.getBaseDirectory()).append(File.separatorChar)
        .append(flightSchedule.getRocketName()).append(".time");

    final String timestampFileName = buf.toString();
    final File f = new File(timestampFileName);
    final boolean fileExists = f.exists();

    if (fileExists) {
      FileUtils.writeStringToFile(f, fmt.format(now));
    } else {
      LOGGER.warn("MISSING TIMESTAMP FILE?? {}", timestampFileName);
    }
  }

  @Override
  @Managed(description = "Move timestamp file back in time." + "\nparam: hours in past")
  public void waybackHours(int hoursInPast) {
    try {
      LOGGER.warn("WAYBACK MACHINE! RESET TIMESTAMP! {}", rocketName);
      resetTimestamp(false, hoursInPast);
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "ERROR RESETTING TIMESTAMP! {}",
          flightSchedule.getRocketName());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Managed(description = "Abort flying rocket")
  public void stop() throws NeutronCheckedException {
    try {
      LOGGER.warn("ABORT ROCKET IN FLIGHT! {}", rocketName);
      unschedule();
      final JobKey key = new JobKey(rocketName, NeutronSchedulerConstants.GRP_LST_CHG);
      scheduler.interrupt(key);
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "FAILED TO ABORT FLIGHT! rocket: {}", rocketName);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Managed(description = "Pause rocket flight")
  public void pause() throws NeutronCheckedException {
    try {
      LOGGER.warn("PAUSE ROCKET! {}", rocketName);
      scheduler.pauseTrigger(triggerKey);
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "FAILED TO PAUSE FLIGHT! rocket: {}", rocketName);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Managed(description = "Resume rocket flight")
  public void resume() throws NeutronCheckedException {
    try {
      LOGGER.warn("RESUME FLIGHT! {}", rocketName);
      scheduler.resumeTrigger(triggerKey);
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "FAILED TO RESUME FLIGHT! rocket: {}", rocketName);
    }
  }

  protected void threadShutdownLaunchCommand() {
    try {
      Thread.currentThread().setName("shutdown");
      LOGGER.warn("SHUTDOWN THREAD STARTING!");
      Thread.sleep(2000);
      LaunchCommand.getInstance().shutdown();
    } catch (InterruptedException | NeutronCheckedException e) {
      LOGGER.debug("Shutdown thread interrupted", e);
      Thread.currentThread().interrupt();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Managed(description = "Shutdown command center")
  public String shutdown() throws NeutronCheckedException {
    LOGGER.warn("Shutting down command center!");
    new Thread(this::threadShutdownLaunchCommand).start();
    return "Requested shutdown!";
  }

  @Override
  public boolean isVetoExecution() {
    return vetoExecution;
  }

  @Override
  public void setVetoExecution(boolean vetoExecution) {
    this.vetoExecution = vetoExecution;
  }

  @Override
  public JobDetail getJd() {
    return jd;
  }

  @Override
  public FlightPlan getFlightPlan() {
    return flightPlan;
  }

  @Override
  public void setFlightPlan(FlightPlan opts) {
    this.flightPlan = opts;
  }

  @Override
  public StandardFlightSchedule getFlightSchedule() {
    return flightSchedule;
  }

  @Override
  public AtomFlightRecorder getFlightRecorder() {
    return flightRecorder;
  }

  @Override
  public String getRocketName() {
    return rocketName;
  }

  @Override
  public String getTriggerName() {
    return triggerName;
  }

  @Override
  public JobKey getJobKey() {
    return jobKey;
  }

  public AtomLaunchDirector getLaunchDirector() {
    return launchDirector;
  }

  public TriggerKey getTriggerKey() {
    return triggerKey;
  }

}
