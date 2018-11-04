package gov.ca.cwds.neutron.launch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.TriggerFiredBundle;

import com.google.inject.Injector;

import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.test.Mach1TestRocket;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;

public class RocketFactoryTest extends Goddard {

  RocketFactory target;
  Injector injector;
  FlightPlan opts;

  JobDetail jd;
  JobDataMap jobDataMap;
  TriggerFiredBundle bundle = mock(TriggerFiredBundle.class);
  Scheduler scheduler = mock(Scheduler.class);
  BasePersonRocket rocket;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    injector = mock(Injector.class);
    jobDataMap = mock(JobDataMap.class);
    jd = mock(JobDetail.class);
    bundle = mock(TriggerFiredBundle.class);
    scheduler = mock(Scheduler.class);

    when(bundle.getJobDetail()).thenReturn(jd);
    when(jd.getJobDataMap()).thenReturn(jobDataMap);
    when(jobDataMap.getString(any())).thenReturn(Mach1TestRocket.class.getName());

    rocket = mach1Rocket;
    when(injector.getInstance(any(Class.class))).thenReturn(rocket);

    target = new RocketFactory(injector, opts, flightPlanRegistry, flightRecorder, null);
  }

  @Test
  public void type() throws Exception {
    assertThat(RocketFactory.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void createJob_Args__Class__FlightPlan() throws Exception {
    Class<?> klass = Mach1TestRocket.class;
    FlightPlan opts_ = mock(FlightPlan.class);
    BasePersonRocket actual = target.fuelRocket(klass, opts_);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void createJob_Args__String__FlightPlan() throws Exception {
    String jobName = Mach1TestRocket.class.getName();
    FlightPlan opts_ = mock(FlightPlan.class);
    BasePersonRocket actual = target.fuelRocket(jobName, opts_);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void newJob_Args__TriggerFiredBundle__Scheduler() throws Exception {
    flightPlanRegistry.addFlightPlan(Mach1TestRocket.class, flightPlan);
    target.setFlightPlanRegistry(flightPlanRegistry);
    Job actual = target.newJob(bundle, scheduler);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void newJob_Args__TriggerFiredBundle__Scheduler_T__SchedulerException() throws Exception {
    try {
      target.newJob(bundle, scheduler);
      fail("Expected exception was not thrown!");
    } catch (SchedulerException e) {
    }
  }

  @Test
  public void getBaseOpts_Args__() throws Exception {
    FlightPlan actual = target.getBaseFlightPlan();
    // assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getRocketOptions_Args__() throws Exception {
    FlightPlanRegistry actual = target.getFlightPlanRegistry();
    assertThat(actual, is(notNullValue()));
  }

}
