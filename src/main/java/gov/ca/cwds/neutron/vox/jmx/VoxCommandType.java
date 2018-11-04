package gov.ca.cwds.neutron.vox.jmx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCmdFetchLogs;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCmdFlightHistory;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCmdLastRunStatus;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCmdPause;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCmdRerunKey;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCmdResume;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCmdShutdown;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCmdStop;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCmdWayBack;

/**
 * Registered VOX command actions.
 * 
 * @author CWDS API Team
 */
public enum VoxCommandType {

  STATUS(VoxCmdLastRunStatus.class, "status"),

  SHUTDOWN(VoxCmdShutdown.class, "shutdown"),

  HISTORY(VoxCmdFlightHistory.class, "history"),

  LOGS(VoxCmdFetchLogs.class, "logs"),

  DISABLE(VoxCmdLastRunStatus.class, "disable"),

  ENABLE(VoxCmdLastRunStatus.class, "enable"),

  STOP(VoxCmdStop.class, "stop"),

  RESUME(VoxCmdResume.class, "resume"),

  PAUSE(VoxCmdPause.class, "pause"),

  WAYBACK(VoxCmdWayBack.class, "wayback"),

  RERUN(VoxCmdRerunKey.class, "rerun")

  ;

  private final Class<? extends VoxCommandAction> klass;
  private final String key;

  private static final Map<String, VoxCommandType> typeMap;

  static {
    final Map<String, VoxCommandType> types;
    types = new HashMap<>(VoxCommandType.values().length);
    for (VoxCommandType e : VoxCommandType.values()) {
      types.put(e.key, e);
    }

    typeMap = Collections.unmodifiableMap(types);
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
