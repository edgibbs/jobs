package gov.ca.cwds.neutron.vox.jmx.cmd;

import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.vox.jmx.VoxJMXCommandClient;

public class VoxCmdFlightHistory extends VoxJMXCommandClient {

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxCmdFlightHistory.class);

  public VoxCmdFlightHistory() {
    super();
  }

  public VoxCmdFlightHistory(String host, String port) {
    super(host, port);
  }

  @Override
  public String run() {
    final String ret = getMbean().history(); // LaunchPad MBean.
    LOGGER.warn("VOX: Pull rocket flight history: {}", ret);
    return ret;
  }

}
