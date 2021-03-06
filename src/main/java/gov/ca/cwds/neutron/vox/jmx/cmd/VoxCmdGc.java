package gov.ca.cwds.neutron.vox.jmx.cmd;

import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.launch.LaunchPad;
import gov.ca.cwds.neutron.vox.jmx.VoxJMXCommandClient;

/**
 * Request garbage collection. Useful for finding memory leaks.
 * 
 * @author CWDS API Team
 * @see LaunchPad#gc()
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
    String ret = "REQUEST GARBAGE COLLECTION!";
    LOGGER.warn(ret);

    try {
      getMbean().gc(); // LaunchPad MBean.
    } catch (Exception e) {
      LOGGER.error("\n\n***** FAILED TO RUN GARBAGE COLLECTION! ***** {}", e.getMessage(), e);
      ret = CheeseRay.stackToString(e);
    }

    return ret;
  }

}
