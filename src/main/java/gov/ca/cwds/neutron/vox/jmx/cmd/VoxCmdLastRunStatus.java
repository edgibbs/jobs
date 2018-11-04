package gov.ca.cwds.neutron.vox.jmx.cmd;

import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.vox.jmx.VoxJMXCommandClient;

public class VoxCmdLastRunStatus extends VoxJMXCommandClient {

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxCmdLastRunStatus.class);

  public VoxCmdLastRunStatus() {
    super();
  }

  public VoxCmdLastRunStatus(String host, String port) {
    super(host, port);
  }

  @Override
  public String run() {
    final String ret = getMbean().status(); // LaunchPad MBean.
    LOGGER.info("status: {}", ret);
    return ret;
  }

}
