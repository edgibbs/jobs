package gov.ca.cwds.neutron.flight;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.Calendar;

import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.EsClientPerson;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class NeutronWarningTrackerTest extends Goddard<ReplicatedClient, EsClientPerson> {

  NeutronWarningTracker target;

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new NeutronWarningTracker();
  }

  @Test
  public void type() throws Exception {
    assertThat(NeutronWarningTracker.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getStackTrace_A$() throws Exception {
    StackTraceElement[] actual = NeutronWarningTracker.getStackTrace();
    // StackTraceElement[] expected = null;
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getThreadId_A$() throws Exception {
    long actual = target.getThreadId();
    long expected = 0L;
    assertThat(actual, is(not(expected)));
  }

  @Test
  public void getStartTime_A$() throws Exception {
    final Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MINUTE, -1);

    long actual = target.getStartTime();
    long expected = cal.getTimeInMillis();
    assertThat(actual, is(greaterThanOrEqualTo(expected)));
  }

  @Test
  public void getStack_A$() throws Exception {
    StackTraceElement[] actual = target.getStack();
    // StackTraceElement[] expected = null;
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getId_A$() throws Exception {
    int actual = target.getId();
    int expected = 7;
    assertThat(actual, is(equalTo(expected)));
  }

}
