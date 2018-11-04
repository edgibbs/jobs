package gov.ca.cwds.neutron.vox.jmx.cmd;

import org.apache.commons.lang3.exception.ExceptionUtils;

import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.vox.jmx.VoxJMXCommandClient;

public class VoxCmdResume extends VoxJMXCommandClient {

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxCmdResume.class);

  public VoxCmdResume() {
    super();
  }

  public VoxCmdResume(String host, String port) {
    super(host, port);
  }

  @Override
  public String run() {
    LOGGER.info("STOP {}!", getRocket());
    try {
      getMbean().stop();
      return String.format("STOPPED ROCKET %s!", getRocket());
    } catch (NeutronCheckedException e) {
      LOGGER.error("ERROR STOPPING {}!", getRocket(), e);
      return ExceptionUtils.getStackTrace(e);
    }
  }

}
