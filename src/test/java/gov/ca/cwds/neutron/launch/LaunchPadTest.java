package gov.ca.cwds.neutron.launch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;

import gov.ca.cwds.jobs.ClientIndexerJob;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.atom.AtomFlightRecorder;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.enums.NeutronSchedulerConstants;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.util.NeutronThreadUtils;

public class LaunchPadTest extends Goddard {

  StandardFlightSchedule sched;
  LaunchPad target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    when(launchDirector.getFlightRecorder()).thenReturn(flightRecorder);

    sched = StandardFlightSchedule.REPORTER;
    target = new LaunchPad(launchDirector, sched, flightPlan);
  }

  @Test
  public void type() throws Exception {
    assertThat(LaunchPad.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void run_Args__String() throws Exception {
    final String cmdLineArgs = null;
    final String actual = target.run(cmdLineArgs);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void schedule_Args__() throws Exception {
    target.schedule();
  }

  @Test(expected = NeutronCheckedException.class)
  public void schedule_Args___T__SchedulerException() throws Exception {
    when(scheduler.getJobDetail(any(JobKey.class))).thenThrow(SchedulerException.class);
    when(launchDirector.launch(any(Class.class), any(FlightPlan.class)))
        .thenThrow(SchedulerException.class);
    when(scheduler.checkExists(any(JobKey.class))).thenThrow(SchedulerException.class);
    target.schedule();
  }

  @Test
  public void unschedule_Args__() throws Exception {
    target.unschedule();
  }

  @Test(expected = NeutronCheckedException.class)
  public void unschedule_Args___T__SchedulerException() throws Exception {
    doThrow(SchedulerException.class).when(scheduler).pauseTrigger(any(TriggerKey.class));
    when(scheduler.unscheduleJob(any(TriggerKey.class))).thenThrow(SchedulerException.class);
    target.unschedule();
  }

  @Test
  public void status_Args__() throws Exception {
    final JobDetail jd = mock(JobDetail.class);
    when(scheduler.getJobDetail(any(JobKey.class))).thenReturn(jd);

    final JobDataMap jdm = new JobDataMap();
    jdm.put("job_class", "TestNeutronJob");
    jdm.put("cmd_line", "--invalid");

    final FlightLog track = new FlightLog();
    jdm.put("track", track);
    when(jd.getJobDataMap()).thenReturn(jdm);

    flightRecorder.logFlight(ClientIndexerJob.class, track);
    target.status();
  }

  @Test
  public void stop_Args__() throws Exception {
    target.stop();
  }

  @Test(expected = NeutronCheckedException.class)
  public void stop_Args___T__SchedulerException() throws Exception {
    doThrow(SchedulerException.class).when(scheduler).pauseTrigger(any(TriggerKey.class));
    when(scheduler.interrupt(any(JobKey.class))).thenThrow(SchedulerException.class);
    target.stop();
  }

  @Test
  public void history_Args__() throws Exception {
    launchDirector = new LaunchDirector(flightRecorder, rocketFactory, flightPlanManager);
    launchDirector.setScheduler(scheduler);
    final String actual = target.history();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void isVetoExecution_Args__() throws Exception {
    final boolean actual = target.isVetoExecution();
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setVetoExecution_Args__boolean() throws Exception {
    final boolean vetoExecution = false;
    target.setVetoExecution(vetoExecution);
  }

  @Test
  public void getJd_Args__() throws Exception {
    target.schedule();
    final JobDetail actual = target.getJd();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getOpts_Args__() throws Exception {
    final FlightPlan actual = target.getFlightPlan();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setOpts_Args__FlightPlan() throws Exception {
    final FlightPlan opts_ = mock(FlightPlan.class);
    target.setFlightPlan(opts_);
  }

  @Test
  public void getFlightPlan_Args__() throws Exception {
    final FlightPlan actual = target.getFlightPlan();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setFlightPlan_Args__FlightPlan() throws Exception {
    final FlightPlan opts_ = mock(FlightPlan.class);
    target.setFlightPlan(opts_);
  }

  @Test
  public void getFlightSchedule_Args__() throws Exception {
    final StandardFlightSchedule actual = target.getFlightSchedule();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getFlightRecorder_Args__() throws Exception {
    final AtomFlightRecorder actual = target.getFlightRecorder();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getJobName_Args__() throws Exception {
    final String actual = target.getRocketName();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getTriggerName_Args__() throws Exception {
    final String actual = target.getTriggerName();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getJobKey_Args__() throws Exception {
    final JobKey actual = target.getJobKey();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getLaunchScheduler_Args__() throws Exception {
    final AtomLaunchDirector actual = target.getLaunchDirector();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void run_A$String() throws Exception {
    final String cmdLine = null;
    final String actual = target.run(cmdLine);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void schedule_A$() throws Exception {
    target.schedule();
  }

  @Test
  public void unschedule_A$() throws Exception {
    target.unschedule();
  }

  @Test
  public void status_A$() throws Exception {
    final String actual = target.status();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void history_A$() throws Exception {
    final String actual = target.history();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void logs_A$() throws Exception {
    final TemporaryFolder myTestFolder = new TemporaryFolder();

    try {
      myTestFolder.create();

      final File folder = myTestFolder.newFolder("jobrunner", "rocketlog");
      final File logFile = new File(folder, "reporter.log");
      FileUtils.writeStringToFile(logFile, "We have a winner!\nLifetime supply of Twinkies!\n");

      final String location =
          myTestFolder.getRoot().getAbsolutePath() + File.separator + "jobrunner";
      flightPlan.setBaseDirectory(location);
      when(flightPlan.getBaseDirectory()).thenReturn(location);

      final String actual = target.logs();
      assertThat(actual, is(notNullValue()));
      NeutronThreadUtils.catchYourBreath();

    } catch (Exception e) {
      // Weird behavior with temporary folder.
      // java.io.UncheckedIOException: java.nio.channels.ClosedByInterruptException
      e.printStackTrace();
    } finally {
      myTestFolder.delete();
    }
  }

  @Test
  public void resetTimestamp_A$boolean$int() throws Exception {
    final boolean initialMode = false;
    final int hoursInPast = 0;
    target.resetTimestamp(initialMode, hoursInPast);
  }

  @Test
  public void waybackHours_A$int() throws Exception {
    final int hoursInPast = 0;
    target.waybackHours(hoursInPast);
  }

  @Test
  public void stop_A$() throws Exception {
    target.stop();
  }

  @Test
  public void pause_A$() throws Exception {
    target.pause();
  }

  @Test
  public void resume_A$() throws Exception {
    target.resume();
  }

  @Test
  public void threadShutdownLaunchCommand_A$() throws Exception {
    target.threadShutdownLaunchCommand();
  }

  @Test
  public void shutdown_A$() throws Exception {
    final String actual = target.shutdown();
    final String expected = "Requested shutdown!";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJd_A$() throws Exception {
    final JobDetail actual = target.getJd();
    final JobDetail expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getFlightSchedule_A$() throws Exception {
    final StandardFlightSchedule actual = target.getFlightSchedule();
    final StandardFlightSchedule expected = StandardFlightSchedule.REPORTER;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getFlightRecorder_A$() throws Exception {
    final AtomFlightRecorder actual = target.getFlightRecorder();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getRocketName_A$() throws Exception {
    final String actual = target.getRocketName();
    final String expected = StandardFlightSchedule.REPORTER.getRocketName();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getTriggerName_A$() throws Exception {
    final String actual = target.getTriggerName();
    final String expected = StandardFlightSchedule.REPORTER.getRocketName();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJobKey_A$() throws Exception {
    final JobKey actual = target.getJobKey();
    final JobKey expected = new JobKey(StandardFlightSchedule.REPORTER.getRocketName(),
        NeutronSchedulerConstants.GRP_LST_CHG);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLaunchDirector_A$() throws Exception {
    final AtomLaunchDirector actual = target.getLaunchDirector();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getTriggerKey_A$() throws Exception {
    final TriggerKey actual = target.getTriggerKey();
    final TriggerKey expected = new TriggerKey(StandardFlightSchedule.REPORTER.getRocketName(),
        NeutronSchedulerConstants.GRP_LST_CHG);
    assertThat(actual, is(equalTo(expected)));
  }

}
