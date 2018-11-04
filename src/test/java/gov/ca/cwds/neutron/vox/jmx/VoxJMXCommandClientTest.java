package gov.ca.cwds.neutron.vox.jmx;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.BiFunction;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.vox.VoxCommandInstruction;
import gov.ca.cwds.neutron.vox.jmx.cmd.VoxCmdLastRunStatus;

public class VoxJMXCommandClientTest {

  String host;
  String port;
  String rocket;
  String cmd;

  JMXConnector jmxConnector;
  MBeanServerConnection mbeanServerConnection;
  BiFunction<String, String, JMXConnector> makeConnector = (host, port) -> jmxConnector;
  VoxJMXCommandClient target;

  @Before
  public void setup() throws Exception {
    host = "localhost";
    port = "1098";
    rocket = "client";
    cmd = "status";

    jmxConnector = mock(JMXConnector.class);
    mbeanServerConnection = mock(MBeanServerConnection.class);
    when(jmxConnector.getMBeanServerConnection()).thenReturn(mbeanServerConnection);
    target = new VoxCmdLastRunStatus(host, port);
    target.setHost(host);
    target.setPort(port);
    target.setRocket(rocket);

    target.setMakeConnector(makeConnector);
  }

  @Test
  public void type() throws Exception {
    assertThat(VoxJMXCommandClient.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void connect_Args__() throws Exception {
    target.connect();
  }

  @Test(expected = NeutronCheckedException.class)
  public void connect_Args___T__NeutronException() throws Exception {
    doThrow(new IllegalStateException()).when(jmxConnector).getMBeanServerConnection();
    target.connect();
  }

  @Test
  public void close_Args__() throws Exception {
    target.close();
  }

  @Test(expected = IllegalStateException.class)
  public void close_Args___T__Exception() throws Exception {
    doThrow(new IllegalStateException()).when(jmxConnector).close();
    target.setJmxConnector(jmxConnector);
    target.close();
  }

  @Test(expected = NeutronCheckedException.class)
  public void proxy_Args__String() throws Exception {
    Object actual = target.proxy(rocket);
    Object expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void proxy_Args__String_T__NeutronException() throws Exception {
    target.proxy(rocket);
  }

  @Test(expected = Exception.class)
  public void main_Args__StringArray() throws Exception {
    String[] args = new String[] {};
    VoxCommandFactory.main(args);
  }

  @Test
  public void getMakeConnector_Args__() throws Exception {
    final BiFunction<String, String, JMXConnector> actual = target.getMakeConnector();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setMakeConnector_Args__BiFunction() throws Exception {
    final BiFunction<String, String, JMXConnector> makeConnector = mock(BiFunction.class);
    target.setMakeConnector(makeConnector);
  }

  @Test
  public void parseCommandLine_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-h", host, "-p", port, "-r", rocket, "-c", cmd};
    final VoxCommandInstruction actual = VoxCommandInstruction.parseCommandLine(args);

    final VoxCommandInstruction expected = new VoxCommandInstruction();
    expected.setHost(host);
    expected.setPort(port);
    expected.setRocket(rocket);
    expected.setCommand(cmd);
    expected.setArgs("");

    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJmxConnector_Args__() throws Exception {
    JMXConnector actual = target.getJmxConnector();
    JMXConnector expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setJmxConnector_Args__JMXConnector() throws Exception {
    JMXConnector jmxConnector = mock(JMXConnector.class);
    target.setJmxConnector(jmxConnector);
  }

  @Test
  public void getMbeanServerConnection_Args__() throws Exception {
    MBeanServerConnection actual = target.getMbeanServerConnection();
    MBeanServerConnection expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setMbeanServerConnection_Args__MBeanServerConnection() throws Exception {
    MBeanServerConnection mbeanServerConnection = mock(MBeanServerConnection.class);
    target.setMbeanServerConnection(mbeanServerConnection);
  }

  @Test
  public void getHost_Args__() throws Exception {
    String actual = target.getHost();
    String expected = host;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPort_Args__() throws Exception {
    String actual = target.getPort();
    String expected = port;
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void launch_Args__Triple() throws Exception {
  // final Triple<String, String, String> triple = Triple.of(host, port, rocket);
  // VoxJMXClient.launch(triple);
  // }

  @Test
  public void isTestMode_Args__() throws Exception {
    boolean actual = VoxJMXCommandClient.isTestMode();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setTestMode_Args__boolean() throws Exception {
    boolean testMode = false;
    VoxJMXCommandClient.setTestMode(testMode);
  }

}
