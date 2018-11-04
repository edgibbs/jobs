package gov.ca.cwds.neutron.vox.jmx.cmd;

import org.apache.commons.lang3.exception.ExceptionUtils;

import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.vox.jmx.VoxJMXCommandClient;

public class VoxCmdWayBack extends VoxJMXCommandClient {

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxCmdWayBack.class);

  public VoxCmdWayBack() {
    super();
  }

  public VoxCmdWayBack(String host, String port) {
    super(host, port);
  }

  @Override
  public String run() {
    final String input = getArgs().trim();

    try {
      final int hours = Integer.parseInt(input);
      getMbean().waybackHours(hours); // LaunchPad MBean.
      return String.format("WAY BACK MACHINE! %s hours in past", hours);
    } catch (Exception e) {
      LOGGER.error("WAY BACK MACHINE: ERROR PARSING! {}", input, e);
      return ExceptionUtils.getStackTrace(e);
    }
  }

}
