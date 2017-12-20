package gov.ca.cwds.neutron.vox.jmx;

import java.io.IOException;
import java.util.function.BiFunction;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import gov.ca.cwds.jobs.exception.NeutronException;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.jetpack.JobLogs;
import gov.ca.cwds.neutron.vox.VoxCommandInstruction;

/**
 * Base class of JMX command actions.
 * 
 * @author CWDS API Team
 */
public abstract class VoxJMXCommandClient implements AutoCloseable, VoxCommandAction {

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxJMXCommandClient.class);

  private static boolean testMode;

  private BiFunction<String, String, JMXConnector> makeConnector = (theHost, thePort) -> {
    try {
      return JMXConnectorFactory.connect(new JMXServiceURL(
          "service:jmx:rmi:///jndi/rmi://" + theHost + ":" + thePort + "/jmxrmi"));
    } catch (IOException e) {
      throw JobLogs.runtime(LOGGER, e, "FAILED TO CONNECT VIA JMX! {}", e.getMessage());
    }
  };

  private String host;
  private String port;
  private String rocket;

  private VoxLaunchPadMBean mbean;
  private JMXConnector jmxConnector;
  private MBeanServerConnection mbeanServerConnection;

  public VoxJMXCommandClient() {
    // default ctor
  }

  public VoxJMXCommandClient(final String host, final String port) {
    this.host = host;
    this.port = port;
  }

  public final void connect() throws NeutronException {
    try {
      jmxConnector = makeConnector.apply(host, port);
      mbeanServerConnection = jmxConnector.getMBeanServerConnection();
      LOGGER.info("mbean count: {}", mbeanServerConnection.getMBeanCount());
      this.mbean = proxy(rocket);
    } catch (Exception e) {
      throw JobLogs.checked(LOGGER, e, "JMX DIDN'T CONNECT!! {}", e.getMessage());
    }
  }

  @Override
  public void close() throws Exception {
    if (jmxConnector != null) {
      jmxConnector.close();
    }
  }

  public final VoxLaunchPadMBean proxy(String rocketName) throws NeutronException {
    try {
      final ObjectName mbeanName = new ObjectName("Neutron:rocket=" + rocketName);
      return MBeanServerInvocationHandler.newProxyInstance(mbeanServerConnection, mbeanName,
          VoxLaunchPadMBean.class, true);
    } catch (Exception e) {
      throw JobLogs.checked(LOGGER, e, "FAILED TO GET MBEAN! {}", e.getMessage());
    }
  }

  public final void launch(final VoxCommandInstruction cmd) throws NeutronException {
    try {
      LOGGER.info("CONNECT JMX...");
      this.setHost(cmd.getHost());
      this.setPort(cmd.getPort());
      this.setRocket(cmd.getRocket());

      this.connect();
      this.run();
    } catch (Exception e) {
      throw JobLogs.checked(LOGGER, e, "JMX ERROR! host: {}, port: {}, rocket: {}", cmd.getHost(),
          cmd.getPort(), cmd.getRocket());
    }
  }

  public BiFunction<String, String, JMXConnector> getMakeConnector() {
    return makeConnector;
  }

  public void setMakeConnector(BiFunction<String, String, JMXConnector> makeConnector) {
    this.makeConnector = makeConnector;
  }

  public JMXConnector getJmxConnector() {
    return jmxConnector;
  }

  public void setJmxConnector(JMXConnector jmxConnector) {
    this.jmxConnector = jmxConnector;
  }

  public MBeanServerConnection getMbeanServerConnection() {
    return mbeanServerConnection;
  }

  public void setMbeanServerConnection(MBeanServerConnection mbeanServerConnection) {
    this.mbeanServerConnection = mbeanServerConnection;
  }

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }

  public static boolean isTestMode() {
    return testMode;
  }

  public static void setTestMode(boolean testMode) {
    VoxJMXCommandClient.testMode = testMode;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public VoxLaunchPadMBean getMbean() {
    return mbean;
  }

  public void setMbean(VoxLaunchPadMBean mbean) {
    this.mbean = mbean;
  }

  public String getRocket() {
    return rocket;
  }

  public void setRocket(String rocket) {
    this.rocket = rocket;
  }

}
