package gov.ca.cwds.neutron.vox.jmx.cmd;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;
import gov.ca.cwds.neutron.vox.jmx.VoxJMXCommandClient;

public abstract class VoxCommandBaseTest<T extends VoxJMXCommandClient> extends Goddard {

  Class<T> type;
  T target;

  public VoxCommandBaseTest(Class<T> type) {
    this.type = type;
  }

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();

    target = type.newInstance();
    target.setMbean(mbean);
    target.setRocket(StandardFlightSchedule.REFERRAL.getRocketName());
  }

  @Test
  public void type() throws Exception {
    assertThat(VoxCmdPause.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void run_A$() throws Exception {
    final String actual = target.run();
    final String expected = "PAUSED ROCKET referral!";
    assertThat(actual, is(equalTo(expected)));
  }

}
