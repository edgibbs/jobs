package gov.ca.cwds.neutron.vox.jmx.cmd;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import gov.ca.cwds.neutron.exception.NeutronRuntimeException;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.launch.LaunchPad;
import gov.ca.cwds.neutron.vox.jmx.VoxJMXCommandClient;

/**
 * Queue a primary key to be run again by the target rocket in Last Change mode.
 * 
 * @author CWDS API Team
 * @see LaunchPad#rerunKey(String[])
 */
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
    final String rawKeys = getArgs().trim();

    try {
      if (StringUtils.isAllBlank(rawKeys)) {
        LOGGER.error("VOX RE-RUN: KEY CANNOT BE EMPTY!");
        throw new NeutronRuntimeException("RE-RUN: KEY CANNOT BE EMPTY!");
      }

      LOGGER.warn("VOX RE-RUN KEY {}", rawKeys);
      getMbean().rerunKey(rawKeys.split(",")); // LaunchPad MBean.
      return String.format("RE-RUN KEY! key: %s", rawKeys);
    } catch (Exception e) {
      LOGGER.error("VOX RE-RUN KEY: ERROR PARSING KEY! {}", getArgs(), e);
      return ExceptionUtils.getStackTrace(e);
    }
  }

}
