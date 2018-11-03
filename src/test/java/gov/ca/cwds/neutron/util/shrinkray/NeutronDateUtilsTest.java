package gov.ca.cwds.neutron.util.shrinkray;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.jobs.Goddard;

public class NeutronDateUtilsTest extends Goddard {

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();
  }

  @Test
  public void type() throws Exception {
    assertThat(NeutronDateUtils.class, notNullValue());
  }

  @Test
  public void freshDate_A$Date() throws Exception {
    Date incoming = makeDate();
    Date actual = NeutronDateUtils.freshDate(incoming);
    Date expected = makeDate();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void lookBack_A$Date() throws Exception {
    Date lastRunTime = makeDate();
    Date actual = NeutronDateUtils.lookBack(lastRunTime);
    Date expected = makeDate();
    assertThat(actual, is(lessThanOrEqualTo(expected)));
  }

  @Test
  public void uncookTimestampString_A$String() throws Exception {
    String timestamp = null;
    Date actual = NeutronDateUtils.uncookTimestampString(timestamp);
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void makeTimestampString_A$Date() throws Exception {
    Date date = makeDate();
    String actual = NeutronDateUtils.makeTimestampString(date);
    String expected = "TIMESTAMP('2018-10-31 00:00:00.000')";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void makeSimpleTimestampString_A$Date() throws Exception {
    Date date = makeDate();
    String actual = NeutronDateUtils.makeSimpleTimestampString(date);
    String expected = "2018-10-31 00:00:00.000";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void makeSimpleDateString_A$Date() throws Exception {
    Date date = makeDate();
    String actual = NeutronDateUtils.makeSimpleDateString(date);
    String expected = "2018-10-31";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void makeTimestampStringLookBack_A$Date() throws Exception {
    Date date = makeDate();
    String actual = NeutronDateUtils.makeTimestampStringLookBack(date);
    String expected = "2018-10-30 23:49:00.000";
    assertThat(actual, is(equalTo(expected)));
  }

  protected Date makeDate() throws ParseException {
    return new SimpleDateFormat("yyyy-MM-dd").parse("2018-10-31");
  }

}
