package gov.ca.cwds.neutron.vox;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;
import gov.ca.cwds.neutron.vox.jmx.VoxCommandType;
import gov.ca.cwds.neutron.vox.jmx.VoxJmxDefaults;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Parses Vox instructions from the command line.
 * 
 * @author CWDS API Team
 */
public final class VoxCommandInstruction implements ApiMarker {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxCommandInstruction.class);

  private String host;
  private String port;

  private String rocket;
  private String command;

  private String args;

  public VoxCommandInstruction() {
    // default
  }

  public VoxCommandInstruction(String rocket, String command) {
    this.rocket = rocket;
    this.command = command;
  }

  public static VoxCommandInstruction parseCommandLine(final String[] args) {
    LOGGER.info("PARSE COMMAND LINE");
    final VoxCommandInstruction ret = new VoxCommandInstruction();
    final OptionParser parser = new OptionParser("h:p:r:c:a:");
    final OptionSet options = parser.parse(args);

    final String host =
        options.has("h") ? (String) options.valueOf("h") : VoxJmxDefaults.DEFAULT_HOST;
    final String port =
        options.has("p") ? (String) options.valueOf("p") : VoxJmxDefaults.DEFAULT_PORT;
    final String rocket = options.has("r") ? (String) options.valueOf("r")
        : StandardFlightSchedule.PEOPLE_SUMMARY.getRocketName();
    final String cmd =
        options.has("c") ? (String) options.valueOf("c") : VoxCommandType.STATUS.getKey();
    final String arg = options.has("a") ? (String) options.valueOf("a") : "";

    ret.setHost(host);
    ret.setPort(port);
    ret.setRocket(rocket);
    ret.setCommand(cmd);
    ret.setArgs(arg);

    LOGGER.info("VOX COMMAND: host: {}, port: {}, rocket: {}, arg: {}", host, port, rocket, arg);
    return ret;
  }

  // =======================
  // ACCESSORS:
  // =======================

  public String getRocket() {
    return rocket;
  }

  public String getCommand() {
    return command;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public void setRocket(String rocket) {
    this.rocket = rocket;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getArgs() {
    return args;
  }

  public void setArgs(String args) {
    this.args = args;
  }

  // =======================
  // IDENTITY:
  // =======================

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE, true);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false);
  }

}
