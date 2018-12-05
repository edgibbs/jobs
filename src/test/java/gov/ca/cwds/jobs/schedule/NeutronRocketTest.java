package gov.ca.cwds.jobs.schedule;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Trigger;

import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.test.TestIndexerJob;
import gov.ca.cwds.jobs.test.TestNormalizedEntityDao;
import gov.ca.cwds.neutron.component.Rocket;
import gov.ca.cwds.neutron.enums.NeutronSchedulerConstants;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.launch.NeutronRocket;

public class NeutronRocketTest extends Goddard {

  NeutronRocket target;
  TestNormalizedEntityDao dao;
  TestIndexerJob rocket;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    dao = new TestNormalizedEntityDao(sessionFactory);
    rocket = new TestIndexerJob(dao, esDao, lastRunFile, MAPPER, sessionFactory, flightRecorder);
    rocket.setFlightPlan(flightPlan);
    rocket.init(this.tempFile.getAbsolutePath(), flightPlan);

    rocket.setFlightLog(flightRecord);
    target = new NeutronRocket(rocket, flightSchedule, flightRecorder);
  }

  @Test
  public void type() throws Exception {
    assertThat(NeutronRocket.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test(expected = JobExecutionException.class)
  public void execute_Args__JobExecutionContext_T__JobException() throws Exception {
    final JobExecutionContext context_ = mock(JobExecutionContext.class);
    final JobDetail jd = mock(JobDetail.class);
    final Trigger trg = mock(Trigger.class);

    final JobKey jobKey = new JobKey("crap", NeutronSchedulerConstants.GRP_LST_CHG);
    final JobDataMap jdm = new JobDataMap();
    jdm.put("job_class", "crap");
    jdm.put("cmd_line", "--invalid");

    when(context_.getJobDetail()).thenReturn(jd);
    when(context_.getTrigger()).thenReturn(trg);
    when(jd.getJobDataMap()).thenReturn(jdm);
    when(trg.getJobKey()).thenReturn(jobKey);

    doThrow(new IllegalArgumentException("uh oh")).when(context_).setResult(any(Object.class));
    target.execute(context_);
  }

  @Test
  public void execute__blow() throws Exception {
    final JobExecutionContext context_ = mock(JobExecutionContext.class);
    final JobDetail jd = mock(JobDetail.class);
    final Trigger trg = mock(Trigger.class);

    final JobKey jobKey = new JobKey("crap", NeutronSchedulerConstants.GRP_LST_CHG);
    final JobDataMap jdm = new JobDataMap();
    jdm.put("job_class", "crap");
    jdm.put("cmd_line", "--invalid");

    when(context_.getJobDetail()).thenReturn(jd);
    when(context_.getTrigger()).thenReturn(trg);
    when(jd.getJobDataMap()).thenReturn(jdm);
    when(trg.getJobKey()).thenReturn(jobKey);

    target.execute(context_);
  }

  @Test
  public void interrupt_Args__() throws Exception {
    target.interrupt();
  }

  @Test
  public void getTrack_Args__() throws Exception {
    FlightLog actual = target.getFlightLog();
    FlightLog expected = new FlightLog();
    expected.setStartTime(actual.getStartTime()); // Manual

    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setTrack_Args__JobProgressTrack() throws Exception {
    FlightLog track = mock(FlightLog.class);
    target.setFlightLog(track);
  }

  @Test
  public void getRocket() throws Exception {
    Rocket actual = target.getRocket();
    assertThat(actual, is(notNullValue()));
  }

}
