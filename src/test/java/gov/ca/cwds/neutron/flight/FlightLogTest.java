package gov.ca.cwds.neutron.flight;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.enums.FlightStatus;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;
import gov.ca.cwds.rest.api.domain.DomainChef;

public class FlightLogTest extends Goddard<ReplicatedClient, RawClient> {

  FlightLog target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    target = new FlightLog(StandardFlightSchedule.PEOPLE_SUMMARY.getRocketName());
    target.setInitialLoad(false);

    target.addAffectedDocumentId(DEFAULT_CLIENT_ID);
    target.addOtherMetric("nr_fun", "supercalifragilisticexpialadocious");
    target.addTimingEvent("potty_break");
  }

  @Test
  public void type() throws Exception {
    assertThat(FlightLog.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void trackQueuedToIndex_Args__() throws Exception {
    int actual = target.markQueuedToIndex();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void trackNormalized_Args__() throws Exception {
    int actual = target.incrementNormalized();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void trackBulkDeleted_Args__() throws Exception {
    int actual = target.incrementBulkDeleted();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void trackBulkPrepared_Args__() throws Exception {
    int actual = target.incrementBulkPrepared();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void trackBulkError_Args__() throws Exception {
    int actual = target.trackBulkError();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void trackRangeStart_Args__Pair() throws Exception {
    Pair<String, String> pair = Pair.of("1", "2");
    target.markRangeStart(pair);
  }

  @Test
  public void trackRangeComplete_Args__Pair() throws Exception {
    Pair<String, String> pair = Pair.of("1", "2");
    target.markRangeComplete(pair);
  }

  @Test
  public void start_Args__() throws Exception {
    target.start();
  }

  @Test
  public void fail_Args__() throws Exception {
    target.fail();
  }

  @Test
  public void done_Args__() throws Exception {
    target.done();
  }

  @Test
  public void hashCode_Args__() throws Exception {
    int actual = target.hashCode();
    assertThat(actual, not(equalTo(0)));
  }

  @Test
  public void equals_Args__Object() throws Exception {
    Object obj = null;
    boolean actual = target.equals(obj);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadRangesStarted_Args__() throws Exception {
    List<Pair<String, String>> actual = target.getInitialLoadRangesStarted();
    List<Pair<String, String>> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadRangesCompleted_Args__() throws Exception {
    List<Pair<String, String>> actual = target.getInitialLoadRangesCompleted();
    List<Pair<String, String>> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void toString_Args__initial_load() throws Exception {
    target.setLastChangeSince(DomainChef.uncookTimestampString("2017-12-25-08.32.05.123"));
    target.done();
    target.setInitialLoad(true);
    String actual = target.toString();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void toString_Args__last_chg() throws Exception {
    target.setLastChangeSince(DomainChef.uncookTimestampString("2017-12-25-08.32.05.123"));
    target.done();
    target.setInitialLoad(false);
    String actual = target.toString();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void isInitialLoad_Args__() throws Exception {
    boolean actual = target.isInitialLoad();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setInitialLoad_Args__boolean() throws Exception {
    boolean initialLoad = false;
    target.setInitialLoad(initialLoad);
  }

  @Test
  public void getLastChangeSince_Args__() throws Exception {
    Date actual = target.getLastChangeSince();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLastChangeSince_Args__Date() throws Exception {
    final Date lastChangeSince = new Date();
    target.setLastChangeSince(lastChangeSince);
  }

  @Test
  public void toString_Args__() throws Exception {
    final String actual = target.toString();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void addAffectedDocumentId_Args__String() throws Exception {
    final String docId = "abc1234567";
    target.addAffectedDocumentId(docId);
  }

  @Test
  public void getJobName_Args__() throws Exception {
    final String actual = target.getRocketName();
    final String expected = StandardFlightSchedule.PEOPLE_SUMMARY.getRocketName();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setJobName_Args__String() throws Exception {
    String jobName = null;
    target.setRocketName(jobName);
  }

  @Test
  public void getStartTime_Args__() throws Exception {
    long actual = target.getStartTime();
    long expected = 0L;
    assertThat(actual, is(not(expected)));
  }

  @Test
  public void getEndTime_Args__() throws Exception {
    long actual = target.getEndTime();
    long expected = 0L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getStatus_Args__() throws Exception {
    FlightStatus actual = target.getStatus();
    FlightStatus expected = FlightStatus.NOT_STARTED;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAffectedDocuments_Args__() throws Exception {
    String[] actual = target.getAffectedDocumentIds();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void isRunning_A$() throws Exception {
    boolean actual = target.isRunning();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isFailed_A$() throws Exception {
    boolean actual = target.isFailed();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isRetrieveDone_A$() throws Exception {
    boolean actual = target.isRetrieveDone();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isTransformDone_A$() throws Exception {
    boolean actual = target.isTransformDone();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isIndexDone_A$() throws Exception {
    boolean actual = target.isIndexDone();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void start_A$() throws Exception {
    target.start();
  }

  @Test
  public void fail_A$() throws Exception {
    target.fail();
  }

  @Test
  public void done_A$() throws Exception {
    target.done();
  }

  @Test
  public void doneIndex_A$() throws Exception {
    target.doneIndex();
  }

  @Test
  public void doneRetrieve_A$() throws Exception {
    target.doneRetrieve();
  }

  @Test
  public void doneTransform_A$() throws Exception {
    target.doneTransform();
  }

  @Test
  public void toString_A$() throws Exception {
    final String actual = target.toString();
    final String expected =
        "\n[\n    FLIGHT STATUS: NOT_STARTED:\tnull\n\n    LAST CHANGE:\n\tchanged since:          null\n\n    RUN TIME:\n\tstart:                  Tue Mar 27 14:11:30 PDT 2018\n\n    RECORDS RETRIEVED:\n\tdenormalized:           0\n\tnormalized:             0\n\n    ELASTICSEARCH:\n\tto bulk:                0\n\tbulk prepared:          0\n\tbulk deleted:           0\n\tbulk before:            0\n\tbulk after:             0\n\tbulk errors:            0\n]";
    assertTrue(actual.startsWith(expected.substring(0, 30)));
  }

  @Test
  public void toJson_A$() throws Exception {
    target.setStartTime(1522185484650L);
    final String actual = target.toJson().trim();
    final String expected =
        "{\"rocket_name\":\"people_summary\",\"fatal_error\":false,\"done_retrieve\":false,\"done_transform\":false,\"done_index\":false,\"done_flight\":false,\"validation_errors\":false,\"initial_load\":false,\"last_change_since\":null,\"status\":\"NOT_STARTED\",\"initial_load_ranges_started\":[],\"initial_load_ranges_completed\":[],\"initial_load_range_status\":{},\"affected_document_ids\":[],\"running\":true,\"failed\":false,\"retrieve_done\":false,\"transform_done\":false,\"index_done\":false,\"warnings\":[],\"to_index_queue\":0,\"normalized\":0,\"bulk_deleted\":0,\"bulk_prepared\":0,\"bulk_error\":0,\"bulk_after\":0,\"denormalized\":0,\"start_time\":\"2018-03-27T21:18:04.650Z\",\"end_time\":null}";
    System.out.println(actual);
    assertTrue(actual.startsWith(expected.substring(0, 30)));
  }

  @Test
  public void hashCode_A$() throws Exception {
    final int actual = target.hashCode();
    final int expected = 0;
    assertThat(actual, is(not(equalTo(expected))));
  }

  @Test
  public void equals_A$Object() throws Exception {
    final Object obj = null;
    boolean actual = target.equals(obj);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadRangesStarted_A$() throws Exception {
    final List<Pair<String, String>> actual = target.getInitialLoadRangesStarted();
    final List<Pair<String, String>> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadRangesCompleted_A$() throws Exception {
    final List<Pair<String, String>> actual = target.getInitialLoadRangesCompleted();
    final List<Pair<String, String>> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCurrentQueuedToIndex_A$() throws Exception {
    int actual = target.getCurrentQueuedToIndex();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCurrentNormalized_A$() throws Exception {
    int actual = target.getCurrentNormalized();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCurrentBulkDeleted_A$() throws Exception {
    int actual = target.getCurrentBulkDeleted();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCurrentBulkPrepared_A$() throws Exception {
    int actual = target.getCurrentBulkPrepared();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCurrentBulkError_A$() throws Exception {
    int actual = target.getCurrentBulkError();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCurrentBulkAfter_A$() throws Exception {
    int actual = target.getCurrentBulkAfter();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void addToQueuedToIndex_A$int() throws Exception {
    int addMe = 0;
    int actual = target.addToQueuedToIndex(addMe);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void addToNormalized_A$int() throws Exception {
    int addMe = 0;
    int actual = target.addToNormalized(addMe);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void addToBulkDeleted_A$int() throws Exception {
    int addMe = 0;
    int actual = target.addToBulkDeleted(addMe);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void addToBulkPrepared_A$int() throws Exception {
    int addMe = 0;
    int actual = target.addToBulkPrepared(addMe);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void addToBulkError_A$int() throws Exception {
    int addMe = 0;
    int actual = target.addToBulkError(addMe);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void addToBulkAfter_A$int() throws Exception {
    int addMe = 0;
    int actual = target.addToBulkAfter(addMe);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void addToBulkBefore_A$int() throws Exception {
    int addMe = 0;
    int actual = target.addToBulkBefore(addMe);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void markQueuedToIndex_A$() throws Exception {
    int actual = target.markQueuedToIndex();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void incrementNormalized_A$() throws Exception {
    int actual = target.incrementNormalized();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void incrementBulkDeleted_A$() throws Exception {
    int actual = target.incrementBulkDeleted();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void incrementBulkPrepared_A$() throws Exception {
    int actual = target.incrementBulkPrepared();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void trackBulkError_A$() throws Exception {
    int actual = target.trackBulkError();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void markRangeStart_A$Pair() throws Exception {
    target.markRangeStart(pair);
  }

  @Test
  public void markRangeComplete_A$Pair() throws Exception {
    target.markRangeComplete(pair);
  }

  @Test
  public void isInitialLoad_A$() throws Exception {
    boolean actual = target.isInitialLoad();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setInitialLoad_A$boolean() throws Exception {
    boolean initialLoad = false;
    target.setInitialLoad(initialLoad);
  }

  @Test
  public void getLastChangeSince_A$() throws Exception {
    Date actual = target.getLastChangeSince();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLastChangeSince_A$Date() throws Exception {
    Date lastChangeSince = mock(Date.class);
    target.setLastChangeSince(lastChangeSince);
  }

  @Test
  public void addAffectedDocumentId_A$String() throws Exception {
    String docId = DEFAULT_CLIENT_ID;
    target.addAffectedDocumentId(docId);
  }

  @Test
  public void getRocketName_A$() throws Exception {
    String actual = target.getRocketName();
    String expected = StandardFlightSchedule.PEOPLE_SUMMARY.getRocketName();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRocketName_A$String() throws Exception {
    String jobName = null;
    target.setRocketName(jobName);
  }

  @Test
  public void getStartTime_A$() throws Exception {
    long actual = target.getStartTime();
    assertThat(actual, is(not(0)));
  }

  @Test
  public void getEndTime_A$() throws Exception {
    long actual = target.getEndTime();
    long expected = 0L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getStatus_A$() throws Exception {
    final FlightStatus actual = target.getStatus();
    final FlightStatus expected = FlightStatus.NOT_STARTED;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAffectedDocumentIds_A$() throws Exception {
    final String[] actual = target.getAffectedDocumentIds();
    final String[] expected = {DEFAULT_CLIENT_ID};
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isValidationErrors_A$() throws Exception {
    boolean actual = target.isValidationErrors();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void failValidation_A$() throws Exception {
    target.failValidation();
  }

  @Test
  public void getFailedRanges_A$() throws Exception {
    target.setRangeStatus(pair, FlightStatus.FAILED);
    final List<Pair<String, String>> actual = target.getFailedRanges();
    final List<Pair<String, String>> expected = new ArrayList<>();
    expected.add(pair);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void fail_A$String() throws Exception {
    String reason = null;
    target.fail(reason);
  }

  @Test
  public void addToDenormalized_A$int() throws Exception {
    int addMe = 0;
    int actual = target.addToDenormalized(addMe);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void markStartChangePoll_A$() throws Exception {
    target.markStartChangePoll();
  }

  @Test
  public void markStartDataPoll_A$() throws Exception {
    target.markStartDataPoll();
  }

  @Test
  public void markEndDataPoll_A$() throws Exception {
    target.markEndDataPoll();
  }

  @Test
  public void incrementDenormalized_A$() throws Exception {
    int actual = target.incrementDenormalized();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRangeStatus_A$Pair$FlightStatus() throws Exception {
    Pair<String, String> pair = mock(Pair.class);
    FlightStatus flightStatus = FlightStatus.RUNNING;
    target.setRangeStatus(pair, flightStatus);
  }

  @Test
  public void markRangeSuccess_A$Pair() throws Exception {
    Pair<String, String> pair = mock(Pair.class);
    target.markRangeSuccess(pair);
  }

  @Test
  public void markRangeError_A$Pair() throws Exception {
    Pair<String, String> pair = mock(Pair.class);
    target.markRangeError(pair);
  }

  @Test
  public void filterStatus_A$FlightStatus$FlightStatusArray() throws Exception {
    FlightStatus actual_ = FlightStatus.RUNNING;
    FlightStatus[] scanFor = new FlightStatus[] {};
    boolean actual = target.filterStatus(actual_, scanFor);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void filterRanges_A$FlightStatusArray() throws Exception {
    FlightStatus[] statuses = new FlightStatus[] {};
    List actual = target.filterRanges(statuses);
    List expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void buildImmutableList_A$FlightStatusArray() throws Exception {
    FlightStatus[] statuses = new FlightStatus[] {};
    List actual = target.buildImmutableList(statuses);
    List expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCurrentDenormalized_A$() throws Exception {
    int actual = target.getCurrentDenormalized();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getWarnings_A$() throws Exception {
    List<String> actual = target.getWarnings();
    List<String> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getStartTimeAsDate_A$() throws Exception {
    Date actual = target.getStartTimeAsDate();
    Date expected = new Date();
    assertThat(actual, is(greaterThanOrEqualTo(expected)));
  }

  @Test
  public void getEndTimeAsDate_A$() throws Exception {
    Date actual = target.getEndTimeAsDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isGlobalError_A$() throws Exception {
    boolean actual = FlightLog.isGlobalError();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isGlobalErrorFlag_A$() throws Exception {
    boolean actual = FlightLog.isGlobalErrorFlag();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isFatalError_A$() throws Exception {
    boolean actual = target.isFatalError();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isDoneRetrieve_A$() throws Exception {
    boolean actual = target.isDoneRetrieve();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isDoneTransform_A$() throws Exception {
    boolean actual = target.isDoneTransform();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isDoneIndex_A$() throws Exception {
    boolean actual = target.isDoneIndex();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isDoneFlight_A$() throws Exception {
    boolean actual = target.isDoneFlight();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getRecsSentToIndexQueue_A$() throws Exception {
    AtomicInteger actual = target.getRecsSentToIndexQueue();
    AtomicInteger expected = new AtomicInteger(0);
    assertThat(actual.get(), is(equalTo(expected.get())));
  }

  @Test
  public void getRecsSentToBulkProcessor_A$() throws Exception {
    AtomicInteger actual = target.getRecsSentToBulkProcessor();
    AtomicInteger expected = new AtomicInteger(0);
    assertThat(actual.get(), is(equalTo(expected.get())));
  }

  @Test
  public void getRowsNormalized_A$() throws Exception {
    AtomicInteger actual = target.getRowsNormalized();
    AtomicInteger expected = new AtomicInteger(0);
    assertThat(actual.get(), is(equalTo(expected.get())));
  }

  @Test
  public void getRowsDenormalized_A$() throws Exception {
    AtomicInteger actual = target.getRowsDenormalized();
    AtomicInteger expected = new AtomicInteger(0);
    assertThat(actual.get(), is(equalTo(expected.get())));
  }

  @Test
  public void getRecsBulkPrepared_A$() throws Exception {
    AtomicInteger actual = target.getRecsBulkPrepared();
    AtomicInteger expected = new AtomicInteger(0);
    assertThat(actual.get(), is(equalTo(expected.get())));
  }

  @Test
  public void getRecsBulkDeleted_A$() throws Exception {
    AtomicInteger actual = target.getRecsBulkDeleted();
    AtomicInteger expected = new AtomicInteger(0);
    assertThat(actual.get(), is(equalTo(expected.get())));
  }

  @Test
  public void getRecsBulkBefore_A$() throws Exception {
    AtomicInteger actual = target.getRecsBulkBefore();
    AtomicInteger expected = new AtomicInteger(0);
    assertThat(actual.get(), is(equalTo(expected.get())));
  }

  @Test
  public void getRecsBulkAfter_A$() throws Exception {
    AtomicInteger actual = target.getRecsBulkAfter();
    AtomicInteger expected = new AtomicInteger(0);
    assertThat(actual.get(), is(equalTo(expected.get())));
  }

  @Test
  public void getRecsBulkError_A$() throws Exception {
    AtomicInteger actual = target.getRecsBulkError();
    AtomicInteger expected = new AtomicInteger(0);
    assertThat(actual.get(), is(equalTo(expected.get())));
  }

  @Test
  public void getInitialLoadRangeStatus_A$() throws Exception {
    final Map<Pair<String, String>, FlightStatus> actual = target.getInitialLoadRangeStatus();
    final Map<Pair<String, String>, FlightStatus> expected = new HashMap<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setStartTime_A$long() throws Exception {
    long startTime = 0L;
    target.setStartTime(startTime);
  }

  @Test
  public void getFailureCause_A$() throws Exception {
    String actual = target.getFailureCause();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setFailureCause_A$String() throws Exception {
    String failureCause = null;
    target.setFailureCause(failureCause);
  }

  @Test
  public void getTimeStartPoll_A$() throws Exception {
    long actual = target.getTimeStartPoll();
    long expected = 0L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setTimeStartPoll_A$long() throws Exception {
    long timeStartPoll = 0L;
    target.setTimeStartPoll(timeStartPoll);
  }

  @Test
  public void getTimeStartPull_A$() throws Exception {
    long actual = target.getTimeStartPull();
    long expected = 0L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setTimeStartPull_A$long() throws Exception {
    long timeStartPull = 0L;
    target.setTimeStartPull(timeStartPull);
  }

  @Test
  public void getTimeEndPull_A$() throws Exception {
    long actual = target.getTimeEndPull();
    long expected = 0L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setTimeEndPull_A$long() throws Exception {
    long timeEndPull = 0L;
    target.setTimeEndPull(timeEndPull);
  }

  @Test
  public void addWarning_A$String() throws Exception {
    String warning = null;
    target.addWarning(warning);
  }

  @Test
  public void addTimingEvents_A$FlightLog() throws Exception {
    FlightLog fl = mock(FlightLog.class);
    target.addTimingEvents(fl);
  }

  @Test
  public void addOtherMetrics_A$FlightLog() throws Exception {
    FlightLog fl = mock(FlightLog.class);
    target.addOtherMetrics(fl);
  }

  @Test
  public void addTimingEvent_A$String() throws Exception {
    String event = null;
    target.addTimingEvent(event);
  }

  @Test
  public void addTimingEvent_A$String$long() throws Exception {
    String event = null;
    long val = 0L;
    target.addTimingEvent(event, val);
  }

  @Test
  public void addOtherMetric_A$String$Object() throws Exception {
    String event = null;
    Object val = null;
    target.addOtherMetric(event, val);
  }

  @Test
  public void getTimings_A$() throws Exception {
    Map<String, Long> actual = target.getTimings();
    Map<String, Long> expected = new HashMap<>();
    expected.put("potty_break", new Date().getTime());
    assertThat(actual.get("potty_break"), is(greaterThanOrEqualTo(expected.get("potty_break"))));
  }

  @Test
  public void notifyMonitor_A$String() throws Exception {
    String eventType = null;
    target.notifyMonitor(eventType);
  }

  @Test
  public void getOtherMetrics_A$() throws Exception {
    Map<String, Object> actual = target.getOtherMetrics();
    Map<String, Object> expected = new HashMap<>();
    expected.put("nr_fun", "supercalifragilisticexpialadocious");
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLastEndTime_A$() throws Exception {
    long actual = target.getLastEndTime();
    long expected = 0L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLastEndTime_A$long() throws Exception {
    long lastEndTime = 0L;
    target.setLastEndTime(lastEndTime);
  }

  @Test
  public void getEsrefreshintervalsecs_A$() throws Exception {
    final int actual = FlightLog.getEsrefreshintervalsecs();
    final int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

}

