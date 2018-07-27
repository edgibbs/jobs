package gov.ca.cwds.neutron.vox.jmx;

import java.util.HashMap;
import java.util.Map;

import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCommandFetchLogs;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCommandFlightHistory;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCommandLastRunStatus;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCommandPause;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCommandResume;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCommandShutdown;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCommandStop;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCommandWayBack;

/**
 * Enum of VOX command actions.
 * 
 * @author CWDS API Team
 */
public enum VoxCommandType {

  STATUS(VoxCommandLastRunStatus.class, "status"),

  SHUTDOWN(VoxCommandShutdown.class, "shutdown"),

  HISTORY(VoxCommandFlightHistory.class, "history"),

  LOGS(VoxCommandFetchLogs.class, "logs"),

  DISABLE(VoxCommandLastRunStatus.class, "disable"),

  ENABLE(VoxCommandLastRunStatus.class, "enable"),

  STOP(VoxCommandStop.class, "stop"),

  RESUME(VoxCommandResume.class, "resume"),

  PAUSE(VoxCommandPause.class, "pause"),

  WAYBACK(VoxCommandWayBack.class, "wayback")

  ;

  private final Class<? extends VoxCommandAction> klass;
  private final String key;

  private static final Map<String, VoxCommandType> typeMap;

  static {
    typeMap = new HashMap<>();
    for (VoxCommandType e : VoxCommandType.values()) {
      typeMap.put(e.key, e);
    }
  }

  private VoxCommandType(final Class<? extends VoxCommandAction> klass, String key) {
    this.klass = klass;
    this.key = key;
  }

  public static VoxCommandType lookup(String key) {
    return VoxCommandType.typeMap.get(key);
  }

  public Class<? extends VoxCommandAction> getKlass() {
    return klass;
  }

  public String getKey() {
    return key;
  }

}
