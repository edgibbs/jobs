package gov.ca.cwds.neutron.flight;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.enums.FlightStatus;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;

public class FlightSummaryTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(FlightSummaryTest.class);
  FlightSummary target;

  @Before
  public void setup() throws Exception {
    target = new FlightSummary(StandardFlightSchedule.CLIENT);
  }

  @Test
  public void type() throws Exception {
    assertThat(FlightSummary.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void accumulate_Args__FlightLog() throws Exception {
    final FlightLog flightLog = new FlightLog();
    flightLog.addAffectedDocumentId(Goddard.DEFAULT_CLIENT_ID);
    flightLog.addToQueuedToIndex(35);
    flightLog.addToBulkAfter(60);
    flightLog.addToBulkError(22);
    flightLog.addToBulkPrepared(36);
    flightLog.addToNormalized(45);
    flightLog.addToBulkDeleted(4);
    flightLog.markQueuedToIndex();
    flightLog.markQueuedToIndex();
    flightLog.markQueuedToIndex();
    flightLog.markQueuedToIndex();
    flightLog.trackBulkError();
    flightLog.trackBulkError();
    flightLog.trackBulkError();
    flightLog.incrementBulkDeleted();
    flightLog.incrementBulkDeleted();
    flightLog.incrementBulkDeleted();
    flightLog.incrementBulkDeleted();
    flightLog.done();
    target.accumulate(flightLog);
    LOGGER.info("summary: {}", target);
    assertThat(target.getBulkPrepared(), is(not(0)));
  }

  @Test
  public void getStatus_Args__() throws Exception {
    Map<FlightStatus, Integer> actual = target.getStatus();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setStatus_Args__EnumMap() throws Exception {
    EnumMap<FlightStatus, Integer> status = mock(EnumMap.class);
    target.setStatus(status);
  }

  @Test
  public void getTotalRuns_Args__() throws Exception {
    int actual = target.getTotalRuns();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setTotalRuns_Args__int() throws Exception {
    int totalRuns = 0;
    target.setTotalRuns(totalRuns);
  }

  @Test
  public void getRecsSentToIndexQueue_Args__() throws Exception {
    int actual = target.getRecsSentToIndexQueue();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsSentToIndexQueue_Args__int() throws Exception {
    int recsSentToIndexQueue = 0;
    target.setRecsSentToIndexQueue(recsSentToIndexQueue);
  }

  @Test
  public void getRecsSentToBulkProcessor_Args__() throws Exception {
    int actual = target.getRecsSentToBulkProcessor();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsSentToBulkProcessor_Args__int() throws Exception {
    int recsSentToBulkProcessor = 0;
    target.setRecsSentToBulkProcessor(recsSentToBulkProcessor);
  }

  @Test
  public void getRowsNormalized_Args__() throws Exception {
    int actual = target.getRowsNormalized();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRowsNormalized_Args__int() throws Exception {
    int rowsNormalized = 0;
    target.setRowsNormalized(rowsNormalized);
  }

  @Test
  public void getRecsBulkPrepared_Args__() throws Exception {
    int actual = target.getBulkPrepared();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsBulkPrepared_Args__int() throws Exception {
    int recsBulkPrepared = 0;
    target.setBulkPrepared(recsBulkPrepared);
  }

  @Test
  public void getRecsBulkDeleted_Args__() throws Exception {
    int actual = target.getBulkDeleted();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsBulkDeleted_Args__int() throws Exception {
    int recsBulkDeleted = 0;
    target.setBulkDeleted(recsBulkDeleted);
  }

  @Test
  public void getRecsBulkBefore_Args__() throws Exception {
    int actual = target.getBulkBefore();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsBulkBefore_Args__int() throws Exception {
    int recsBulkBefore = 0;
    target.setBulkBefore(recsBulkBefore);
  }

  @Test
  public void getRecsBulkAfter_Args__() throws Exception {
    int actual = target.getBulkAfter();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsBulkAfter_Args__int() throws Exception {
    int recsBulkAfter = 0;
    target.setBulkAfter(recsBulkAfter);
  }

  @Test
  public void getRecsBulkError_Args__() throws Exception {
    int actual = target.getBulkError();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsBulkError_Args__int() throws Exception {
    int recsBulkError = 0;
    target.setBulkError(recsBulkError);
  }

  @Test
  public void getFirstStart_Args__() throws Exception {
    Date actual = target.getFirstStart();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setFirstStart_Args__Date() throws Exception {
    Date firstStart = new Date();
    target.setFirstStart(firstStart);
  }

  @Test
  public void getLastEnd_Args__() throws Exception {
    Date actual = target.getLastEnd();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setLastEnd_Args__Date() throws Exception {
    Date lastEnd = new Date();
    target.setLastEnd(lastEnd);
  }

  @Test
  public void accumulate_A$FlightLog() throws Exception {
    FlightLog flightLog = new FlightLog();
    target.accumulate(flightLog);
  }

  @Test
  public void getStatus_A$() throws Exception {
    Map<FlightStatus, Integer> actual = target.getStatus();
    Map<FlightStatus, Integer> expected = new HashMap<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setStatus_A$EnumMap() throws Exception {
    EnumMap<FlightStatus, Integer> status = mock(EnumMap.class);
    target.setStatus(status);
  }

  @Test
  public void getTotalRuns_A$() throws Exception {
    int actual = target.getTotalRuns();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setTotalRuns_A$int() throws Exception {
    int totalRuns = 0;
    target.setTotalRuns(totalRuns);
  }

  @Test
  public void getRecsSentToIndexQueue_A$() throws Exception {
    int actual = target.getRecsSentToIndexQueue();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsSentToIndexQueue_A$int() throws Exception {
    int recsSentToIndexQueue = 0;
    target.setRecsSentToIndexQueue(recsSentToIndexQueue);
  }

  @Test
  public void getRecsSentToBulkProcessor_A$() throws Exception {
    int actual = target.getRecsSentToBulkProcessor();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsSentToBulkProcessor_A$int() throws Exception {
    int recsSentToBulkProcessor = 0;
    target.setRecsSentToBulkProcessor(recsSentToBulkProcessor);
  }

  @Test
  public void getRowsNormalized_A$() throws Exception {
    int actual = target.getRowsNormalized();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRowsNormalized_A$int() throws Exception {
    int rowsNormalized = 0;
    target.setRowsNormalized(rowsNormalized);
  }

  @Test
  public void getBulkPrepared_A$() throws Exception {
    int actual = target.getBulkPrepared();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setBulkPrepared_A$int() throws Exception {
    int recsBulkPrepared = 0;
    target.setBulkPrepared(recsBulkPrepared);
  }

  @Test
  public void getBulkDeleted_A$() throws Exception {
    int actual = target.getBulkDeleted();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setBulkDeleted_A$int() throws Exception {
    int recsBulkDeleted = 0;
    target.setBulkDeleted(recsBulkDeleted);
  }

  @Test
  public void getBulkBefore_A$() throws Exception {
    int actual = target.getBulkBefore();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setBulkBefore_A$int() throws Exception {
    int recsBulkBefore = 0;
    target.setBulkBefore(recsBulkBefore);
  }

  @Test
  public void getBulkAfter_A$() throws Exception {
    int actual = target.getBulkAfter();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setBulkAfter_A$int() throws Exception {
    int recsBulkAfter = 0;
    target.setBulkAfter(recsBulkAfter);
  }

  @Test
  public void getBulkError_A$() throws Exception {
    int actual = target.getBulkError();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setBulkError_A$int() throws Exception {
    int recsBulkError = 0;
    target.setBulkError(recsBulkError);
  }

  @Test
  public void getFirstStart_A$() throws Exception {
    Date actual = target.getFirstStart();
    Date expected = new Date();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setFirstStart_A$Date() throws Exception {
    Date firstStart = mock(Date.class);
    target.setFirstStart(firstStart);
  }

  @Test
  public void getLastEnd_A$() throws Exception {
    Date actual = target.getLastEnd();
    Date expected = new Date();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLastEnd_A$Date() throws Exception {
    Date lastEnd = mock(Date.class);
    target.setLastEnd(lastEnd);
  }

  @Test
  public void toString_A$() throws Exception {
    String actual = target.toString();
    String expected =
        "FlightSummary [\n\trocketName=client\n\tfirstStart=Tue Mar 27 10:51:18 PDT 2018\n\tlastEnd=Tue Mar 27 10:51:18 PDT 2018\n\tstatus={}\n\ttotalRuns=0\n\trecsSentToIndexQueue=0\n\trecsSentToBulkProcessor=0\n\trowsNormalized=0\n\trecsBulkPrepared=0\n\trecsBulkDeleted=0\n\trecsBulkBefore=0\n\trecsBulkAfter=0\n\trecsBulkError=0\n]";
    assertTrue(actual.startsWith(expected.substring(0, 20)));
  }

  @Test
  public void toJson_A$() throws Exception {
    String actual = target.toJson();
    String expected =
        ",\"status_history\":{},\"total_runs\":0,\"sent_to_index_queue\":0,\"sent_to_elasticsearch\":0,\"rows_normalized\":0,\"bulk_prepared\":0,\"bulk_deleted\":0,\"bulk_before\":0,\"bulk_after\":0,\"bulk_error\":0,\"validation_errors\":false}";
    assertTrue(actual.contains(expected));
  }

  @Test
  public void isValidationErrors_A$() throws Exception {
    final int actual = target.getValidationErrors();
    final int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setValidationErrors_A$boolean() throws Exception {
    final int validationErrors = 1;
    target.setValidationErrors(validationErrors);
  }

}
