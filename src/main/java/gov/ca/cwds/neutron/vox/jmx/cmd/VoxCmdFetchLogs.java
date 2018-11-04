package gov.ca.cwds.neutron.vox.jmx.cmd;

import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.vox.jmx.VoxJMXCommandClient;

public class VoxCmdFetchLogs extends VoxJMXCommandClient {

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxCmdFetchLogs.class);

  public VoxCmdFetchLogs() {
    super();
  }

  public VoxCmdFetchLogs(String host, String port) {
    super(host, port);
  }

  @Override
  public String run() {
    LOGGER.warn("VOX: PULL LOGS FOR ROCKET {}", getRocket());
    final String logs = getMbean().logs(); // LaunchPad MBean.
    LOGGER.warn("VOX: LOGS FOR ROCKET {}\n\n{}\n\n", getRocket(), logs);
    return logs;
  }

}
