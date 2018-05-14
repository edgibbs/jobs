package gov.ca.cwds.jobs.schedule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;

import com.google.inject.Injector;

import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.test.Mach1TestRocket;
import gov.ca.cwds.jobs.test.TestDenormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntity;
import gov.ca.cwds.neutron.atom.AtomCommandCenterConsole;
import gov.ca.cwds.neutron.atom.AtomFlightRecorder;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.enums.NeutronSchedulerConstants;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.launch.LaunchCommandSettings;
import gov.ca.cwds.neutron.launch.RocketFactory;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;

public class LaunchCommandTest extends Goddard<TestNormalizedEntity, TestDenormalizedEntity> {

  Injector injector;
  LaunchCommand target;
  TriggerKey key;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    flightPlan = new FlightPlan();
    flightPlan.setEsConfigLoc("config/local.yaml");

    final File fakeBaseDir = tempFolder.newFolder();
    flightPlan.setBaseDirectory(fakeBaseDir.getAbsolutePath());
    flightPlan.setLastRunLoc(lastRunFile);

    final AtomCommandCenterConsole ctrlMgr = mock(AtomCommandCenterConsole.class);
    target = new LaunchCommand(flightRecorder, launchDirector, ctrlMgr);
    target.setCommonFlightPlan(flightPlan);
    target.setLaunchDirector(launchDirector);

    Function<FlightPlan, Injector> makeLaunchCommand = mock(Function.class);
    injector = mock(Injector.class);
    when(makeLaunchCommand.apply(any(FlightPlan.class))).thenReturn(injector);
    when(injector.getInstance(LaunchCommand.class)).thenReturn(target);
    LaunchCommand.setInjectorMaker(makeLaunchCommand);
    LaunchCommand.setStandardFlightPlan(flightPlan);

    key = new TriggerKey("el_trigger", NeutronSchedulerConstants.GRP_LST_CHG);
    LaunchCommand.setTestMode(true);
  }

  @Test
  public void type() throws Exception {
    assertThat(LaunchCommand.class, notNullValue());
  }

  @Test
  public void resetTimestamps_Args__boolean__int() throws Exception {
    boolean initialMode = false;
    int hoursInPast = 0;
    target.resetTimestamps(initialMode, hoursInPast);
  }

  @Test
  public void resetTimestampsForInitialLoad_Args__() throws Exception {
    String actual = target.resetTimestampsForInitialLoad();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void resetTimestampsForLastChange_Args__int() throws Exception {
    int hoursInPast = 0;
    target.resetTimestampsForLastChange(hoursInPast);
  }

  @Test
  public void stopScheduler_Args__boolean() throws Exception {
    boolean waitForJobsToComplete = false;
    target.stopScheduler(waitForJobsToComplete);
  }

  @Test
  public void isSchedulerMode_Args__() throws Exception {
    boolean actual = LaunchCommand.isSchedulerMode();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isTestMode_Args__() throws Exception {
    LaunchCommand.getSettings().setTestMode(true);
    boolean actual = LaunchCommand.isTestMode();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setTestMode_Args__boolean() throws Exception {
    boolean mode = false;
    LaunchCommand.setTestMode(mode);
  }

  @Test
  public void isInitialMode_Args__() throws Exception {
    boolean actual = LaunchCommand.isInitialMode();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void getEsDao_Args__() throws Exception {
  // ElasticsearchDao actual = target.getEsDao();
  // assertThat(actual, is(notNullValue()));
  // }
  //
  // @Test
  // public void setEsDao_Args__ElasticsearchDao() throws Exception {
  // target.setEsDao(esDao);
  // }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void startScheduler_Args__() throws Exception {
    target.startScheduler();
  }

  @Test
  public void handleTimeFile_Args__FlightPlan__DateFormat__Date__DefaultFlightSchedule()
      throws Exception {
    final DateFormat fmt = new SimpleDateFormat("yyyy-mm-dd");
    final Date now = new Date();
    final StandardFlightSchedule sched = StandardFlightSchedule.CLIENT;
    target.handleSchedulerModeTimeFile(flightPlan, fmt, now, sched);
  }

  @Test
  public void configureInitialMode_Args__Date() throws Exception {
    final Date now = new Date();
    target.configureInitialMode(now);
  }

  @Test
  public void initScheduler_Args__Injector() throws Exception {
    final Injector injector = mock(Injector.class);
    when(injector.getInstance(RocketFactory.class)).thenReturn(rocketFactory);

    LaunchCommand.getSettings().setExposeJmx(false);
    target.setInjector(injector);
    target.initScheduler();
  }

  @Test
  public void getStartingOpts_Args__() throws Exception {
    final FlightPlan actual = target.getCommonFlightPlan();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setStartingOpts_Args__FlightPlan() throws Exception {
    target.setCommonFlightPlan(flightPlan);
  }

  @Test
  public void getFlightRecorder_Args__() throws Exception {
    final AtomFlightRecorder actual = target.getFlightRecorder();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setFlightRecorder_Args__FlightRecorder() throws Exception {
    target.setFlightRecorder(flightRecorder);
  }

  @Test
  public void getNeutronScheduler_Args__() throws Exception {
    final AtomLaunchDirector actual = target.getNeutronScheduler();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setNeutronScheduler_Args__LaunchScheduler() throws Exception {
    target.setLaunchDirector(launchDirector);
  }

  @Test
  public void getScheduler_Args__() throws Exception {
    final Scheduler actual = target.getScheduler();
  }

  @Test
  public void getInstance_Args__() throws Exception {
    final LaunchCommand actual = LaunchCommand.getInstance();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void runStandalone_Args__Class__StringArray() throws Exception {
    final Class<Mach1TestRocket> klass = Mach1TestRocket.class;
    final String[] args = new String[] {"-S", "-l", "/var/lib/jenkins/SuperRocket/LastRuntime.time",
        "-c", "config/local.yaml"};
    LaunchCommand.launchOneWayTrip(klass, args);
  }

  @Test
  public void runStandalone_again() throws Exception {
    final Class<Mach1TestRocket> klass = Mach1TestRocket.class;
    final String[] args = new String[] {"-S", "-l", "/var/lib/jenkins/SuperRocket/LastRuntime.time",
        "-c", "config/local.yaml"};
    LaunchCommand.launchOneWayTrip(klass, args);
  }

  @Test
  @Ignore
  public void runStandalone_Args2__Class__StringArray() throws Exception {
    final Class<Mach1TestRocket> klass = Mach1TestRocket.class;
    final String[] args = new String[] {"-l", "/var/lib/jenkins/SuperRocket/LastRuntime.time", "-c",
        "config/local.yaml"};
    LaunchCommand.launchOneWayTrip(klass, args);
  }

  @Test
  public void main_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-S", "-b", "/var/lib/jenkins", "-c", "config/local.yaml"};
    LaunchCommand.main(args);
  }

  @Test
  public void handleTimeFile_Args__FlightPlan__DateFormat__Date__StandardFlightSchedule()
      throws Exception {
    final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    final Date now = new Date();
    final StandardFlightSchedule sched = StandardFlightSchedule.REFERRAL;
    target.handleSchedulerModeTimeFile(flightPlan, fmt, now, sched);
  }

  @Test
  public void initScheduler_Args__() throws Exception {
    LaunchCommand.getSettings().setExposeJmx(false);
    target.initScheduler();
  }

  @Test
  public void setLaunchScheduler_Args__LaunchScheduler() throws Exception {
    target.setLaunchDirector(launchDirector);
  }

  @Test
  public void parseCommandLine_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-b", "/var/lib/jenkins/", "-F"};
    final FlightPlan actual = LaunchCommand.parseCommandLine(args);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void buildCommandCenter_Args__FlightPlan() throws Exception {
    final FlightPlan standardFlightPlan = flightPlan;
    LaunchCommand actual = LaunchCommand.buildCommandCenter(standardFlightPlan);
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = NeutronCheckedException.class)
  public void buildCommandCenter_Args__FlightPlan_T__NeutronException() throws Exception {
    when(injector.getInstance(any(Class.class))).thenThrow(new IllegalStateException());
    LaunchCommand.buildCommandCenter(flightPlan);
  }

  @Test
  public void close_Args__() throws Exception {
    target.close();
  }

  @Test
  public void startSchedulerMode_Args__() throws Exception {
    LaunchCommand.getSettings().setExposeJmx(false);
    final LaunchCommand actual = LaunchCommand.startSchedulerMode();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getInjectorMaker_Args__() throws Exception {
    final Function<FlightPlan, Injector> actual = LaunchCommand.getInjectorMaker();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setInjectorMaker_Args__Function() throws Exception {
    final Function<FlightPlan, Injector> makeLaunchCommand = mock(Function.class);
    LaunchCommand.setInjectorMaker(makeLaunchCommand);
  }

  @Test
  public void getInjector_Args__() throws Exception {
    final Injector actual = target.getInjector();
    final Injector expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setInjector_Args__Injector() throws Exception {
    final Injector injector = mock(Injector.class);
    target.setInjector(injector);
  }

  @Test
  public void getSettings_Args__() throws Exception {
    final LaunchCommandSettings actual = LaunchCommand.getSettings();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getStandardFlightPlan_Args__() throws Exception {
    final FlightPlan actual = LaunchCommand.getStandardFlightPlan();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setStandardFlightPlan_Args__FlightPlan() throws Exception {
    final FlightPlan plan = mock(FlightPlan.class);
    LaunchCommand.setStandardFlightPlan(plan);
  }

  // @Test
  // @Ignore
  // public void createJob_Args__Class__StringArray() throws Exception {
  // Class<?> klass = TestIndexerJob.class;
  // String[] args = new String[] {"-c", "config/local.yaml", "-b", "/var/lib/jenkins/", "-F"}

  // BasePersonIndexerJob actual = target.createJob(klass, args);
  // assertThat(actual, is(notNullValue()));
  // }

  // @Test(expected = Exception.class)
  // public void close_Args___T__Exception() throws Exception {
  // target.close();
  // }

}
