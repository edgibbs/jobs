package gov.ca.cwds.neutron.launch;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;

public class AbortFlightTimerTaskTest extends Goddard {

  AbortFlightTimerTask target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    target = new AbortFlightTimerTask(scheduler, "240000");
  }

  @Test
  public void type() throws Exception {
    assertThat(AbortFlightTimerTask.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void abortRunningJob_A$JobExecutionContext() throws Exception {
    final NeutronRocket job = mock(NeutronRocket.class);
    final FlightLog flightLog = mock(FlightLog.class);
    final BasePersonRocket<?, ?> rocket = mock(BasePersonRocket.class);
    final JobExecutionContext ctx = mock(JobExecutionContext.class);
    final FlightPlan flightPlan = mock(FlightPlan.class);
    final JobDetail jobDetail = mock(JobDetail.class);
    final JobKey jobKey = new org.quartz.JobKey("dead_job");

    when(ctx.getJobInstance()).thenReturn(job);
    when(job.getRocket()).thenReturn(rocket);
    when(rocket.getFlightLog()).thenReturn(flightLog);
    when(rocket.getFlightPlan()).thenReturn(flightPlan);
    when(flightPlan.isLastRunMode()).thenReturn(true);
    when(flightLog.isRunning()).thenReturn(true);
    when(flightLog.isFailed()).thenReturn(false);
    when(ctx.getJobRunTime()).thenReturn(100000L);
    when(ctx.getJobDetail()).thenReturn(jobDetail);
    when(jobDetail.getKey()).thenReturn(jobKey);

    final List<JobExecutionContext> runningJobs = new ArrayList<>();
    runningJobs.add(ctx);
    when(scheduler.getCurrentlyExecutingJobs()).thenReturn(runningJobs);

    target.abortRunningJob(ctx);
  }

  @Test
  public void run_A$() throws Exception {
    target.run();
  }

}
