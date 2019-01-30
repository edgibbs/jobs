package gov.ca.cwds.neutron.vox.jmx.cmd;

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
public class VoxCmdGc extends VoxJMXCommandClient {

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxCmdGc.class);

  public VoxCmdGc() {
    super();
  }

  public VoxCmdGc(String host, String port) {
    super(host, port);
  }

  @Override
  public String run() {
    String ret = "RUN GARBAGE COLLECTION!";
    LOGGER.warn("RUN GARBAGE COLLECTION!");

    try {
      getMbean().gc(); // LaunchPad MBean.
    } catch (Exception e) {
      LOGGER.error("\n\n***** FAILED TO RUN GARBAGE COLLECTION! ***** {}", e.getMessage(), e);
      ret = CheeseRay.stackToString(e);
    }

    return ret;
  }

}
