package gov.ca.cwds.neutron.vox.jmx.cmd;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.neutron.vox.jmx.VoxLaunchPadMBean;

public class VoxCommandFlightHistoryTest {

  static final class TestVoxCommandFlightHistory extends VoxCommandFlightHistory {

    VoxLaunchPadMBean mbean;

    @Override
    public VoxLaunchPadMBean getMbean() {
      return mbean;
    }

    @Override
    public void setMbean(VoxLaunchPadMBean mbean) {
      this.mbean = mbean;
    }

  }

  VoxLaunchPadMBean mbean;
  VoxCommandFlightHistory target;

  @Before
  public void setup() throws Exception {
    mbean = mock(VoxLaunchPadMBean.class);
    target = new TestVoxCommandFlightHistory();
    target.setMbean(mbean);
  }

  @Test
  public void type() throws Exception {
    assertThat(VoxCommandFlightHistory.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void run_Args__() throws Exception {
    when(mbean.history()).thenReturn("some history");
    final String actual = target.run();
    assertThat(actual, is(notNullValue()));
  }

}
