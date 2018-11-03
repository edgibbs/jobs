package gov.ca.cwds.jobs.schedule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;

import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.test.Mach1TestRocket;
import gov.ca.cwds.neutron.atom.AtomFlightPlanManager;
import gov.ca.cwds.neutron.atom.AtomLaunchPad;
import gov.ca.cwds.neutron.atom.AtomRocketFactory;
import gov.ca.cwds.neutron.enums.NeutronSchedulerConstants;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.launch.FlightPlanRegistry;
import gov.ca.cwds.neutron.launch.FlightRecorder;
import gov.ca.cwds.neutron.launch.LaunchDirector;
import gov.ca.cwds.neutron.launch.LaunchPad;
import gov.ca.cwds.neutron.launch.NeutronRocket;
import gov.ca.cwds.neutron.launch.RocketFactory;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;
import gov.ca.cwds.neutron.launch.ZombieKillerTimerTask;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;

public class LaunchDirectorTest extends Goddard {

  FlightRecorder jobHistory;
  RocketFactory rocketFactory;
  FlightPlanRegistry rocketOptions;
  TriggerKey key;
  Scheduler scheduler;
  LaunchPad launchPad;
  ZombieKillerTimerTask timerTask;

  LaunchDirector target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    jobHistory = mock(FlightRecorder.class);
    rocketFactory = mock(RocketFactory.class);
    rocketOptions = mock(FlightPlanRegistry.class);
    scheduler = mock(Scheduler.class);
    launchPad = mock(LaunchPad.class);
    timerTask = new ZombieKillerTimerTask(scheduler, "240000");

    key = new TriggerKey("el_trigger", NeutronSchedulerConstants.GRP_LST_CHG);
    target = new LaunchDirector(jobHistory, rocketFactory, rocketOptions, timerTask, "60000");
    target.setScheduler(scheduler);
    target.setFlightPlan(flightPlan);

    target.getLaunchPads().put(Mach1TestRocket.class, launchPad);
  }

  @Test
  public void type() throws Exception {
    assertThat(LaunchDirector.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void createJob_Args__Class__FlightPlan() throws Exception {
    Class<?> klass = Mach1TestRocket.class;
    BasePersonRocket actual = target.fuelRocket(klass, flightPlan);
    BasePersonRocket expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void createJob_Args__String__FlightPlan() throws Exception {
    String jobName = null;
    BasePersonRocket actual = target.fuelRocket(jobName, flightPlan);
    BasePersonRocket expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void runScheduledJob_Args__Class__FlightPlan() throws Exception {
    Class<?> klass = Mach1TestRocket.class;
    FlightLog actual = target.launch(klass, flightPlan);
    FlightLog expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void runScheduledJob_Args__Class__FlightPlan_T__NeutronException() throws Exception {
    Class<?> klass = Mach1TestRocket.class;
    try {
      target.launch(klass, flightPlan);
      fail("Expected exception was not thrown!");
    } catch (NeutronCheckedException e) {
    }
  }

  @Test(expected = NeutronCheckedException.class)
  public void runScheduledJob_Args__String__FlightPlan() throws Exception {
    String jobName = Mach1TestRocket.class.getName();
    FlightLog actual = target.launch(jobName, flightPlan);
    FlightLog expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void scheduleJob_Args__Class__DefaultFlightSchedule__FlightPlan() throws Exception {
    Class<?> klass = Mach1TestRocket.class;
    StandardFlightSchedule sched = StandardFlightSchedule.PEOPLE_SUMMARY;
    AtomLaunchPad actual = target.scheduleLaunch(sched, flightPlan);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void stopScheduler_Args__boolean() throws Exception {
    boolean waitForJobsToComplete = false;
    target.stopScheduler(waitForJobsToComplete);
  }

  @Test(expected = NeutronCheckedException.class)
  public void stopScheduler_Args__boolean__boom() throws Exception {
    doThrow(new SchedulerException()).when(scheduler).shutdown();
    doThrow(new SchedulerException()).when(scheduler).shutdown(any(Boolean.class));
    target.stopScheduler(false);
  }

  @Test
  public void startScheduler_Args__() throws Exception {
    target.startScheduler();
  }

  @Test(expected = NeutronCheckedException.class)
  public void startScheduler_Args__boom() throws Exception {
    doThrow(new SchedulerException()).when(scheduler).start();
    target.startScheduler();
  }

  @Test
  public void addExecutingJob_Args__TriggerKey__NeutronRocket() throws Exception {
    NeutronRocket rocket = mock(NeutronRocket.class);
    target.markRocketAsInFlight(key, rocket);
  }

  @Test
  public void removeExecutingJob_Args__TriggerKey() throws Exception {
    target.removeExecutingJob(key);
  }

  @Test
  public void getExecutingJobs_Args__() throws Exception {
    Map<TriggerKey, NeutronRocket> actual = target.getRocketsInFlight();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getRocketFactory_Args__() throws Exception {
    AtomRocketFactory actual = target.getRocketFactory();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getOpts_Args__() throws Exception {
    FlightPlan actual = target.getFlightPlan();
    // assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setOpts_Args__FlightPlan() throws Exception {
    target.setFlightPlan(flightPlan);
  }

  @Test
  public void getScheduler_Args__() throws Exception {
    Scheduler actual = target.getScheduler();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setScheduler_Args__Scheduler() throws Exception {
    Scheduler scheduler = mock(Scheduler.class);
    target.setScheduler(scheduler);
  }

  @Test
  public void isJobVetoed_Args__String() throws Exception {
    String className = Mach1TestRocket.class.getName();
    boolean actual = target.isLaunchVetoed(className);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void isJobVetoed_Args__boom() throws Exception {
    String className = "quibblescibblerazzerfrazzer";
    boolean actual = target.isLaunchVetoed(className);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getRocketOptions_Args__() throws Exception {
    AtomFlightPlanManager actual = target.getFlightPlanManger();
    assertThat(actual, is(notNullValue()));
  }

}
