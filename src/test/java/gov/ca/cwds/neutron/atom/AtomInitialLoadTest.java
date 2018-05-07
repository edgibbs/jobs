package gov.ca.cwds.neutron.atom;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.test.Mach1TestRocket;
import gov.ca.cwds.jobs.test.TestDenormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntity;
import gov.ca.cwds.neutron.flight.FlightPlan;

public class AtomInitialLoadTest extends Goddard<TestDenormalizedEntity, TestDenormalizedEntity> {

  Mach1TestRocket target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    flightPlan = new FlightPlan();
    flightPlan.setRefreshMqt(true);
    flightPlan.setLastRunMode(false);
    flightPlan.setThreadCount(1);
    flightPlan.setRangeGiven(true);

    target = mach1Rocket;
    target.setFlightPlan(flightPlan);
  }

  @Test
  public void type() throws Exception {
    assertThat(AtomInitialLoad.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void limitRange_Args__List() throws Exception {
    final List<Pair<String, String>> allKeyPairs = new ArrayList();
    allKeyPairs.add(pair);

    final List<Pair<String, String>> actual = target.limitRange(allKeyPairs);
    final List<Pair<String, String>> expected = allKeyPairs;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isInitialLoadJdbc_Args__() throws Exception {
    boolean actual = target.isInitialLoadJdbc();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadViewName_Args__() throws Exception {
    final String actual = target.getInitialLoadViewName();
    final String expected = "VW_NUTTIN";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadQuery_Args__String() throws Exception {
    String dbSchemaName = "CWSRS1";
    String actual = target.getInitialLoadQuery(dbSchemaName);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void isDelete_Args__Object() throws Exception {
    TestNormalizedEntity t = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    boolean actual = target.isDelete(t);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getOrCreateTransaction_Args__() throws Exception {
    final Transaction actual = target.grabTransaction();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void nextThreadNumber_Args__() throws Exception {
    int actual = target.nextThreadNumber();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void handleRangeResults_Args__ResultSet() throws Exception {
    target.handleRangeResults(rs);
  }

  @Test
  public void pullRange_Args__Pair() throws Exception {
    Pair<String, String> p = pair;
    target.pullRange(p);
  }

  @Test
  public void getPartitionRanges_Args__() throws Exception {
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    final List<Pair<String, String>> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void bigRetrieveByJdbc_Args__() throws Exception {
    target.pullMultiThreadJdbc();
  }

  @Test
  public void getMQTName_Args__() throws Exception {
    String actual = target.getMQTName();
    String expected = "VW_NUTTIN";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void refreshMQT_Args__() throws Exception {
    when(proc.getOutputParameterValue(any(String.class))).thenReturn("0");
    target.refreshMQT();
  }

  @Test
  public void initialLoadProcessRangeResults_Args__ResultSet() throws Exception {
    target.handleRangeResults(rs);
  }

  @Test
  public void pullMultiThreadJdbc_Args__() throws Exception {
    target.pullMultiThreadJdbc();
  }

}
