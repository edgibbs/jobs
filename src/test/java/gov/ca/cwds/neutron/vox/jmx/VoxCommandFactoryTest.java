package gov.ca.cwds.neutron.vox.jmx;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.vox.VoxCommandInstruction;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCmdLastRunStatus;

public class VoxCommandFactoryTest extends Goddard {

  @Test
  public void type() throws Exception {
    assertThat(VoxCommandFactory.class, notNullValue());
  }

  @Test
  public void build_A$VoxCommandType$VoxCommandInstruction() throws Exception {
    final VoxCommandType cmdType = VoxCommandType.DISABLE;
    final String[] args = new String[] {};
    final VoxCommandInstruction cmd = VoxCommandInstruction.parseCommandLine(args);
    final VoxJMXCommandClient actual = VoxCommandFactory.build(cmdType, cmd);
    final VoxJMXCommandClient expected = new VoxCmdLastRunStatus();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void build_A$VoxCommandType$VoxCommandInstruction_T$NeutronCheckedException()
      throws Exception {
    final VoxCommandType cmdType = null;
    final String[] args = new String[] {"-r", "fake_rocket", "-c", "fake_cmd"};
    final VoxCommandInstruction cmd = VoxCommandInstruction.parseCommandLine(args);
    VoxCommandFactory.build(cmdType, cmd);
  }

  @Test(expected = NeutronCheckedException.class)
  public void launch_A$StringArray_T$NeutronCheckedException() throws Exception {
    final String[] args = new String[] {};
    VoxCommandFactory.launch(args);
  }

  @Test(expected = Exception.class)
  public void main_A$StringArray_T$Exception() throws Exception {
    final String[] args = new String[] {};
    VoxCommandFactory.main(args);
  }

}
