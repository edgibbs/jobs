package gov.ca.cwds.jobs.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.apache.commons.cli.Option;
import org.junit.Test;

import gov.ca.cwds.neutron.flight.NeutronCmdLineOption;
import gov.ca.cwds.neutron.flight.NeutronCmdLineParser;
import gov.ca.cwds.neutron.flight.NeutronLongCmdLineName;

public class CmdLineOptionTest {

  @Test
  public void type() throws Exception {
    assertThat(NeutronCmdLineOption.class, notNullValue());
  }

  @Test
  public void test_valueof() throws Exception {
    NeutronCmdLineOption actual = NeutronCmdLineOption.valueOf("BUCKET_RANGE");
    NeutronCmdLineOption expected = NeutronCmdLineOption.BUCKET_RANGE;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getOpt_Args__() throws Exception {
    Option actual = NeutronCmdLineOption.BUCKET_RANGE.getOpt();
    Option expected = NeutronCmdLineParser.makeOpt("r", NeutronLongCmdLineName.CMD_LINE_BUCKET_RANGE,
        "bucket range (-r 20-24)", false, 2, Integer.class, '-');
    assertThat(actual, is(equalTo(expected)));
  }

}
