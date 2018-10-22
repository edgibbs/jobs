package gov.ca.cwds.neutron.util.transform;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class JobTransformUtilsTest {

  @Test
  public void type() throws Exception {
    assertThat(JobTransformUtils.class, notNullValue());
  }

  @Test
  public void test_ifNull__null() throws Exception {
    final String value = null;
    String actual = JobTransformUtils.ifNull(value);
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void test_ifNull__trim() throws Exception {
    final String value = " trim me ";
    final String actual = JobTransformUtils.ifNull(value);
    final String expected = "trim me";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void test_ifNull__whitespace() throws Exception {
    final String value = " \t\t\n ";
    final String actual = JobTransformUtils.ifNull(value);
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void test_ifNull__blank() throws Exception {
    final String value = "";
    final String actual = JobTransformUtils.ifNull(value);
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

}
