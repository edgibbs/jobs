package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.apache.commons.cli.Options;
import org.junit.Test;

public class JobOptionsTest {

  private static final JobOptions makeGeneric() {
    return new JobOptions("config/local.yaml", null, false, 1, 5, 10, 1, " ", "9999999999");
  }

  @Test
  public void type() throws Exception {
    assertThat(JobOptions.class, notNullValue());
  }

  @Test
  public void getEsConfigLoc_Args__() throws Exception {
    JobOptions target = makeGeneric();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    String actual = target.getEsConfigLoc();
    // then
    // e.g. : verify(mocked).called();
    String expected = "config/local.yaml";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLastRunLoc_Args__() throws Exception {
    JobOptions target = makeGeneric();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    String actual = target.getLastRunLoc();
    // then
    // e.g. : verify(mocked).called();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isLastRunMode_Args__() throws Exception {
    JobOptions target = makeGeneric();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    boolean actual = target.isLastRunMode();
    // then
    // e.g. : verify(mocked).called();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getStartBucket_Args__() throws Exception {
    JobOptions target = makeGeneric();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    long actual = target.getStartBucket();
    // then
    // e.g. : verify(mocked).called();
    long expected = 1L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getEndBucket_Args__() throws Exception {
    JobOptions target = makeGeneric();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    long actual = target.getEndBucket();
    // then
    // e.g. : verify(mocked).called();
    long expected = 5L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getTotalBuckets_Args__() throws Exception {
    JobOptions target = makeGeneric();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    long actual = target.getTotalBuckets();
    // then
    // e.g. : verify(mocked).called();
    long expected = 10L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getThreadCount_Args__() throws Exception {

    JobOptions target = makeGeneric();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    long actual = target.getThreadCount();
    // then
    // e.g. : verify(mocked).called();
    long expected = 1L;
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void makeOpt_Args__String__String__String() throws Exception {
  //
  // // given
  // String shortOpt = null;
  // String longOpt = null;
  // String description = null;
  // // e.g. : given(mocked.called()).willReturn(1);
  // // when
  // Option actual = JobOptions.makeOpt(shortOpt, longOpt, description);
  // // then
  // // e.g. : verify(mocked).called();
  // Option expected = null;
  // assertThat(actual, is(equalTo(expected)));
  // }

  // @Test
  // public void makeOpt_Args__String__String__String__boolean__int__Class__char() throws Exception
  // {
  //
  // // given
  // String shortOpt = null;
  // String longOpt = null;
  // String description = null;
  // boolean required = false;
  // int argc = 0;
  // Class<?> type = mock(Class.class);
  // char sep = ' ';
  // // e.g. : given(mocked.called()).willReturn(1);
  // // when
  // Option actual = JobOptions.makeOpt(shortOpt, longOpt, description, required, argc, type, sep);
  // // then
  // // e.g. : verify(mocked).called();
  // Option expected = null;
  // assertThat(actual, is(equalTo(expected)));
  // }

  @Test
  public void buildCmdLineOptions_Args__() throws Exception {

    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    Options actual = JobOptions.buildCmdLineOptions();
    // then
    // e.g. : verify(mocked).called();
    Options expected = null;
    assertThat(actual.getOptions().size(), is(equalTo(5)));
  }

  @Test
  public void printUsage_Args__() throws Exception {

    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    JobOptions.printUsage();
    // then
    // e.g. : verify(mocked).called();
  }

  @Test(expected = JobsException.class)
  public void parseCommandLine_Args__T__no_args() throws Exception {

    // given
    String[] args = new String[] {};
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    JobOptions actual = JobOptions.parseCommandLine(args);
    // then
    // e.g. : verify(mocked).called();
    JobOptions expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void parseCommandLine_Args__StringArray_T__JobsException() throws Exception {

    // given
    String[] args = new String[] {};
    // e.g. : given(mocked.called()).willReturn(1);
    try {
      // when
      JobOptions.parseCommandLine(args);
      fail("Expected exception was not thrown!");
    } catch (JobsException e) {
      // then
    }
  }

}
