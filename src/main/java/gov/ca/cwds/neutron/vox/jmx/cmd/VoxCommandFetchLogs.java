package gov.ca.cwds.neutron.vox.jmx.cmd;

import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.vox.jmx.VoxJMXCommandClient;

public class VoxCommandFetchLogs extends VoxJMXCommandClient {

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxCommandFetchLogs.class);

  public VoxCommandFetchLogs() {
    super();
  }

  public VoxCommandFetchLogs(String host, String port) {
    super(host, port);
  }

  @Override
  public String run() {
    LOGGER.warn("PULL LOGS FOR ROCKET {}", getRocket());
    final String logs = getMbean().logs();
    LOGGER.warn("LOGS FOR ROCKET {}\n\n{}\n\n", getRocket(), logs);
    return logs;
  }

}
