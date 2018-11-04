package gov.ca.cwds.neutron.vox.jmx.cmd;

import org.apache.commons.lang3.exception.ExceptionUtils;

import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.launch.LaunchPad;
import gov.ca.cwds.neutron.vox.jmx.VoxJMXCommandClient;

/**
 * Stop (unschedule) target rocket in Last Change mode.
 * 
 * @author CWDS API Team
 * @see LaunchPad#stop()
 */
public class VoxCmdStop extends VoxJMXCommandClient {

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxCmdStop.class);

  public VoxCmdStop() {
    super();
  }

  public VoxCmdStop(String host, String port) {
    super(host, port);
  }

  @Override
  public String run() {
    LOGGER.info("STOP/UNSCHEDULE ROCKET {}!", getRocket());
    try {
      getMbean().stop(); // LaunchPad MBean.
      return String.format("STOP/UNSCHEDULE ROCKET %s!", getRocket());
    } catch (NeutronCheckedException e) {
      LOGGER.error("ERROR UNSCHEDULING ROCKET {}!", getRocket(), e);
      return ExceptionUtils.getStackTrace(e);
    }
  }

}
