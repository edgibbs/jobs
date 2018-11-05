package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.data.es.NeutronElasticSearchDao;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.test.TestDenormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntityDao;
import gov.ca.cwds.neutron.enums.NeutronDateTimeFormat;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.launch.FlightRecorder;

public class LastFlightRocketTest extends Goddard<TestNormalizedEntity, TestDenormalizedEntity> {

  private static class TestLastSuccessfulRunJob extends LastFlightRocket {

    NeutronElasticSearchDao esDao;

    FlightLog track = new FlightLog();

    FlightPlan flightPlan;

    public TestLastSuccessfulRunJob(String lastJobRunTimeFilename, NeutronElasticSearchDao esDao,
        FlightRecorder jobHistory, final FlightPlan opts) {
      super(lastJobRunTimeFilename, opts);
    }

    @Override
    public Date launch(Date lastSuccessfulRunTime) {
      return null;
    }

    @Override
    protected void finish() {}

    @Override
    public FlightLog getFlightLog() {
      return track;
    }

    @Override
    public NeutronElasticSearchDao getEsDao() {
      return esDao;
    }

    @Override
    public ObjectMapper getMapper() {
      return MAPPER;
    }

  }

  private static final String FIXED_DATETIME = "2017-06-14 14:09:47";

  TestNormalizedEntityDao dao;
  TestLastSuccessfulRunJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    target = new TestLastSuccessfulRunJob(lastRunFile, esDao, flightRecorder, flightPlan);

    try (BufferedWriter w = new BufferedWriter(new FileWriter(tempFile))) {
      w.write(FIXED_DATETIME);
    } catch (IOException e) {
    }
  }

  @Test
  public void type() throws Exception {
    assertThat(LastFlightRocket.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void run_Args__() throws Exception {
    target.run();
  }

  @Test
  public void determineLastSuccessfulRunTime_Args__() throws Exception {
    final Date actual = target.determineLastSuccessfulRunTime();
    final Date expected =
        NeutronDateTimeFormat.LAST_RUN_DATE_FORMAT.formatter().parse(FIXED_DATETIME);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void determineLastSuccessfulRunTime_error__() throws Exception {
    target.setLastRunTimeFilename("zugzug_oompa_loompa");
    final Date actual = target.determineLastSuccessfulRunTime();
    final Date expected =
        NeutronDateTimeFormat.LAST_RUN_DATE_FORMAT.formatter().parse(FIXED_DATETIME);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void writeLastSuccessfulRunTime_Args__Date() throws Exception {
    final FlightLog track = mock(FlightLog.class);
    when(track.isFailed()).thenReturn(false);
    target.track = track;

    target.setLastRunTimeFilename(tempFile.getAbsolutePath());

    final Date datetime = new Date();
    target.writeLastSuccessfulRunTime(datetime);
  }

  @Test(expected = NeutronCheckedException.class)
  public void writeLastSuccessfulRunTime_Args__bomb() throws Exception {
    final FlightLog track = mock(FlightLog.class);
    when(track.isFailed()).thenReturn(false);
    target.track = track;

    target.setLastRunTimeFilename("/does/not/exist/garbage");

    final Date datetime = new Date();
    target.writeLastSuccessfulRunTime(datetime);
  }

  @Test
  public void getLastJobRunTimeFilename_Args__() throws Exception {
    final String actual = target.getLastJobRunTimeFilename();
    final String expected = tempFile.getAbsolutePath();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void done_Args__() throws Exception {
    target.done();
  }

  @Test
  public void fail_Args__() throws Exception {
    target.fail();
  }

  @Test
  public void doneRetrieve_Args__() throws Exception {
    target.doneRetrieve();
  }

  @Test
  public void doneTransform_Args__() throws Exception {
    target.doneTransform();
  }

  @Test
  public void doneIndex_Args__() throws Exception {
    target.doneIndex();
  }

  @Test
  public void isRunning_Args__() throws Exception {
    final boolean actual = target.isRunning();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isFailed_Args__() throws Exception {
    final boolean actual = target.isFailed();
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isRetrieveDone_Args__() throws Exception {
    final boolean actual = target.isRetrieveDone();
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isTransformDone_Args__() throws Exception {
    final boolean actual = target.isTransformDone();
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isIndexDone_Args__() throws Exception {
    final boolean actual = target.isIndexDone();
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void calcLastRunDate_Args__Date__JobOptions() throws Exception {
    final Date lastSuccessfulRunTime = new Date();
    final Date actual = target.calcLastRunDate(lastSuccessfulRunTime, flightPlan);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void calcLastRunDate_Args__Date() throws Exception {
    final Date lastSuccessfulRunTime = new Date();

    final FlightPlan opts = new FlightPlan();
    opts.setOverrideLastRunTime(lastSuccessfulRunTime);
    target.setFlightPlan(opts);

    final Date actual = target.calcLastRunDate(lastSuccessfulRunTime);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getOpts_Args__() throws Exception {
    final FlightPlan opts = new FlightPlan();
    target.setFlightPlan(opts);

    final FlightPlan actual = target.getFlightPlan();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setOpts_Args__JobOptions() throws Exception {
    target.setFlightPlan(flightPlan);
  }

  @Test
  public void getLogger_Args__() throws Exception {
    final Logger actual = target.getLogger();
    assertThat(actual, is(notNullValue()));
  }

}
