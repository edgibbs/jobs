package gov.ca.cwds.neutron.vox.jmx.cmd;

import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.launch.LaunchPad;
import gov.ca.cwds.neutron.vox.jmx.VoxJMXCommandClient;

/**
 * Shutdown Launch Command in either mode.
 * 
 * <p>
 * NOTE: Still looks up MBean (LaunchPad instance) for a given rocket, despite globally shutting
 * down Launch Command.
 * </p>
 * 
 * @author CWDS API Team
 * @see LaunchPad#shutdown()
 */
public class VoxCmdShutdown extends VoxJMXCommandClient {

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxCmdShutdown.class);

  public VoxCmdShutdown() {
    super();
  }

  public VoxCmdShutdown(String host, String port) {
    super(host, port);
  }

  @Override
  public String run() {
    String ret = "SHUT DOWN!";
    LOGGER.warn("SHUTDOWN DOWN COMMAND CENTER!");

    try {
      getMbean().shutdown(); // LaunchPad MBean.
    } catch (NeutronCheckedException e) {
      LOGGER.error("\n\n***** FAILED TO SHUTDOWN DOWN COMMAND CENTER! ***** {}", e.getMessage(), e);
      ret = CheeseRay.stackToString(e);
    }

    return ret;
  }

}
