package gov.ca.cwds.neutron.vox.jmx.cmd;

import org.apache.commons.lang3.exception.ExceptionUtils;

import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.vox.jmx.VoxJMXCommandClient;

public class VoxCmdRerunKey extends VoxJMXCommandClient {

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxCmdRerunKey.class);

  public VoxCmdRerunKey() {
    super();
  }

  public VoxCmdRerunKey(String host, String port) {
    super(host, port);
  }

  @Override
  public String run() {
    final String key = getArgs().trim();

    try {
      LOGGER.warn("RE-RUN KEY {}", key);
      getMbean().rerunKey(key);
      return String.format("RE-RUN KEY! %s hours in past", key);
    } catch (Exception e) {
      LOGGER.error("RE-RUN KEY: ERROR PARSING! {}", getArgs(), e);
      return ExceptionUtils.getStackTrace(e);
    }
  }

}
