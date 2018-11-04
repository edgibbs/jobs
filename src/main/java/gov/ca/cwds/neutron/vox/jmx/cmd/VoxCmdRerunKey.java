package gov.ca.cwds.neutron.vox.jmx.cmd;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import gov.ca.cwds.neutron.exception.NeutronRuntimeException;
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
      if (StringUtils.isAllBlank(key)) {
        LOGGER.error("RE-RUN: KEY CANNOT BE EMPTY!");
        throw new NeutronRuntimeException("RE-RUN: KEY CANNOT BE EMPTY!");
      }

      LOGGER.warn("RE-RUN KEY {}", key);
      getMbean().rerunKey(key);
      return String.format("RE-RUN KEY! key: %s", key);
    } catch (Exception e) {
      LOGGER.error("RE-RUN KEY: ERROR PARSING KEY! {}", getArgs(), e);
      return ExceptionUtils.getStackTrace(e);
    }
  }

}
