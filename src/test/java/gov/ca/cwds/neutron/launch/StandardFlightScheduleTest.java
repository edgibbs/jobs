package gov.ca.cwds.neutron.launch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.quartz.listeners.JobChainingJobListener;

import gov.ca.cwds.jobs.Goddard;

public class StandardFlightScheduleTest extends Goddard {

  StandardFlightSchedule target;

  @Override
  public void setup() throws Exception {
    super.setup();
    target = StandardFlightSchedule.CLIENT;
  }

  @Test
  public void type() throws Exception {
    assertThat(StandardFlightSchedule.class, notNullValue());
  }

  @Test
  public void buildInitialLoadJobChainListener_Args__() throws Exception {
    final JobChainingJobListener actual =
        StandardFlightSchedule.buildInitialLoadJobChainListener(true, new HashSet<>());
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getInitialLoadRockets_Args__() throws Exception {
    final List<StandardFlightSchedule> actual =
        StandardFlightSchedule.getInitialLoadRockets(true, new HashSet<>());

    final List<StandardFlightSchedule> expected = new ArrayList<>();
    expected.add(StandardFlightSchedule.LIGHT_THIS_CANDLE);
    expected.add(StandardFlightSchedule.RESET_PEOPLE_INDEX);
    expected.add(StandardFlightSchedule.RESET_PEOPLE_SUMMARY_INDEX);
    expected.add(StandardFlightSchedule.PEOPLE_SUMMARY);
    expected.add(StandardFlightSchedule.REPORTER_S);
    expected.add(StandardFlightSchedule.COLLATERAL_INDIVIDUAL_S);
    expected.add(StandardFlightSchedule.SERVICE_PROVIDER_S);
    expected.add(StandardFlightSchedule.SUBSTITUTE_CARE_PROVIDER_S);
    expected.add(StandardFlightSchedule.EDUCATION_PROVIDER_S);
    expected.add(StandardFlightSchedule.OTHER_ADULT_IN_HOME_S);
    expected.add(StandardFlightSchedule.OTHER_CHILD_IN_HOME_S);
    expected.add(StandardFlightSchedule.CLIENT);
    expected.add(StandardFlightSchedule.REPORTER);
    expected.add(StandardFlightSchedule.COLLATERAL_INDIVIDUAL);
    expected.add(StandardFlightSchedule.SERVICE_PROVIDER);
    expected.add(StandardFlightSchedule.SUBSTITUTE_CARE_PROVIDER);
    expected.add(StandardFlightSchedule.EDUCATION_PROVIDER);
    expected.add(StandardFlightSchedule.OTHER_ADULT_IN_HOME);
    expected.add(StandardFlightSchedule.OTHER_CHILD_IN_HOME);
    expected.add(StandardFlightSchedule.CASES);
    expected.add(StandardFlightSchedule.RELATIONSHIP);
    expected.add(StandardFlightSchedule.REFERRAL);
    expected.add(StandardFlightSchedule.INTAKE_SCREENING);
    expected.add(StandardFlightSchedule.EXIT_INITIAL_LOAD);

    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLastChangeRockets_Args__() throws Exception {
    final List<StandardFlightSchedule> actual =
        StandardFlightSchedule.getLastChangeRockets(true, new HashSet<>());

    final List<StandardFlightSchedule> expected = new ArrayList<>();
    expected.add(StandardFlightSchedule.PEOPLE_SUMMARY);
    expected.add(StandardFlightSchedule.REPORTER_S);
    expected.add(StandardFlightSchedule.COLLATERAL_INDIVIDUAL_S);
    expected.add(StandardFlightSchedule.SERVICE_PROVIDER_S);
    expected.add(StandardFlightSchedule.SUBSTITUTE_CARE_PROVIDER_S);
    expected.add(StandardFlightSchedule.EDUCATION_PROVIDER_S);
    expected.add(StandardFlightSchedule.OTHER_ADULT_IN_HOME_S);
    expected.add(StandardFlightSchedule.OTHER_CHILD_IN_HOME_S);
    expected.add(StandardFlightSchedule.CLIENT);
    expected.add(StandardFlightSchedule.REPORTER);
    expected.add(StandardFlightSchedule.COLLATERAL_INDIVIDUAL);
    expected.add(StandardFlightSchedule.SERVICE_PROVIDER);
    expected.add(StandardFlightSchedule.SUBSTITUTE_CARE_PROVIDER);
    expected.add(StandardFlightSchedule.EDUCATION_PROVIDER);
    expected.add(StandardFlightSchedule.OTHER_ADULT_IN_HOME);
    expected.add(StandardFlightSchedule.OTHER_CHILD_IN_HOME);
    expected.add(StandardFlightSchedule.CASES);
    expected.add(StandardFlightSchedule.RELATIONSHIP);
    expected.add(StandardFlightSchedule.REFERRAL);
    expected.add(StandardFlightSchedule.INTAKE_SCREENING);

    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getRocketName_Args__() throws Exception {
    final String actual = target.getRocketName();
    final String expected = "client";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isNewDocument_Args__() throws Exception {
    boolean actual = target.isNewDocument();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isNewDocument_Args__2() throws Exception {
    target = StandardFlightSchedule.RELATIONSHIP;
    boolean actual = target.isNewDocument();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getStartDelaySeconds_Args__() throws Exception {
    final int actual = target.getStartDelaySeconds();
    final int expected = 8;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getWaitPeriodSeconds_Args__() throws Exception {
    final int actual = target.getWaitPeriodSeconds();
    final int expected = 20;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLastRunPriority_Args__() throws Exception {
    final int actual = target.getLastRunPriority();
    final int expected = 1000;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getNestedElement_Args__() throws Exception {
    final StandardFlightSchedule target = StandardFlightSchedule.CLIENT;
    final String actual = target.getNestedElement();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void lookupByJobName_Args__String() throws Exception {
    final String key = StandardFlightSchedule.CASES.getRocketName();
    final StandardFlightSchedule actual = StandardFlightSchedule.lookupByRocketName(key);
    final StandardFlightSchedule expected = StandardFlightSchedule.CASES;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void lookupByClass_Args__Class() throws Exception {
    final Class<?> key = StandardFlightSchedule.CLIENT.getRocketClass();
    final StandardFlightSchedule actual = StandardFlightSchedule.lookupByRocketClass(key);
    final StandardFlightSchedule expected = StandardFlightSchedule.CLIENT;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadOrder_Args__() throws Exception {
    final int actual = target.getInitialLoadOrder();
    final int expected = 11;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isRunLastChange_Args__() throws Exception {
    boolean actual = target.isRunLastChange();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isRunLastChange_Args__2() throws Exception {
    target = StandardFlightSchedule.EXIT_INITIAL_LOAD;
    boolean actual = target.isRunLastChange();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isRunInitialLoad_Args__() throws Exception {
    boolean actual = target.isRunInitialLoad();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

}
