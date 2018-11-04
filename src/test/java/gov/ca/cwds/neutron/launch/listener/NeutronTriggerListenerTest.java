package gov.ca.cwds.neutron.launch.listener;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerKey;

import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.test.TestIndexerJob;
import gov.ca.cwds.jobs.test.TestNormalizedEntityDao;
import gov.ca.cwds.neutron.enums.NeutronSchedulerConstants;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;
import gov.ca.cwds.neutron.launch.LaunchDirector;
import gov.ca.cwds.neutron.launch.NeutronRocket;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;

public class NeutronTriggerListenerTest extends Goddard {

  NeutronTriggerListener target;
  NeutronRocket job;
  LaunchDirector neutronScheduler;

  JobExecutionContext context_;
  JobDataMap jobDataMap;
  JobDetail jobDetail;

  TriggerKey triggerKey;
  Trigger trigger;

  TestNormalizedEntityDao dao;
  TestIndexerJob rocket;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    dao = new TestNormalizedEntityDao(sessionFactory);
    rocket = new TestIndexerJob(dao, esDao, lastRunFile, MAPPER, sessionFactory, flightRecorder);
    rocket.setFlightPlan(flightPlan);
    rocket.setFlightLog(flightRecord);
    job = new NeutronRocket(rocket, flightSchedule, flightRecorder);

    context_ = mock(JobExecutionContext.class);
    triggerKey = new TriggerKey("fakejob", NeutronSchedulerConstants.GRP_LST_CHG);
    trigger = mock(Trigger.class);
    jobDataMap = mock(JobDataMap.class);
    jobDetail = mock(JobDetail.class);
    neutronScheduler = mock(LaunchDirector.class);

    when(context_.getJobInstance()).thenReturn(job);
    when(context_.getJobDetail()).thenReturn(jobDetail);
    when(context_.getTrigger()).thenReturn(trigger);

    when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
    when(trigger.getKey()).thenReturn(triggerKey);
    when(jobDataMap.getString(any(String.class))).thenReturn(TestIndexerJob.class.getName());

    neutronScheduler.scheduleLaunch(StandardFlightSchedule.PEOPLE_SUMMARY, flightPlan);
    target = new NeutronTriggerListener(neutronScheduler);
  }

  @Test
  public void type() throws Exception {
    assertThat(NeutronTriggerListener.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getName_Args__() throws Exception {
    String actual = target.getName();
    String expected = "neutron_trigger_listener";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void triggerFired_Args__Trigger__JobExecutionContext() throws Exception {
    target.triggerFired(trigger, context_);
  }

  @Test
  public void vetoJobExecution_Args__Trigger__JobExecutionContext() throws Exception {
    boolean actual = target.vetoJobExecution(trigger, context_);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronRuntimeException.class)
  public void vetoJobExecution__boom() throws Exception {
    when(neutronScheduler.isLaunchVetoed(any(String.class)))
        .thenThrow(NeutronCheckedException.class);

    boolean actual = target.vetoJobExecution(trigger, context_);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void triggerMisfired_Args__Trigger() throws Exception {
    target.triggerMisfired(trigger);
  }

  @Test
  public void triggerComplete_Args__Trigger__JobExecutionContext__CompletedExecutionInstruction()
      throws Exception {
    CompletedExecutionInstruction triggerInstructionCode =
        CompletedExecutionInstruction.SET_TRIGGER_COMPLETE;
    target.triggerComplete(trigger, context_, triggerInstructionCode);
  }

}
