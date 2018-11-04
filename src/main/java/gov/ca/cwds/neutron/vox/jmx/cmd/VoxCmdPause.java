package gov.ca.cwds.neutron.vox.jmx.cmd;

import org.apache.commons.lang3.exception.ExceptionUtils;

import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.vox.jmx.VoxJMXCommandClient;

public class VoxCmdPause extends VoxJMXCommandClient {

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxCmdPause.class);

  public VoxCmdPause() {
    super();
  }

  public VoxCmdPause(String host, String port) {
    super(host, port);
  }

  @Override
  public String run() {
    LOGGER.warn("PAUSE {}", getRocket());
    try {
      getMbean().pause(); // LaunchPad MBean.
      return String.format("PAUSED ROCKET %s!", getRocket());
    } catch (NeutronCheckedException e) {
      LOGGER.error("ERROR PAUSING {}!", getRocket(), e);
      return ExceptionUtils.getStackTrace(e);
    }
  }

}
