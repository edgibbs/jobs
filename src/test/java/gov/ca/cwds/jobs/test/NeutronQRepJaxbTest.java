package gov.ca.cwds.jobs.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.PrintStream;

import org.junit.Test;

import gov.ca.cwds.jobs.Goddard;
import mqcap.xsd.MsgType;

public class NeutronQRepJaxbTest extends Goddard {

  NeutronQRepJaxb target;

  @Override
  public void setup() throws Exception {
    super.setup();

    target = new NeutronQRepJaxb();
  }

  @Test
  public void type() throws Exception {
    assertThat(NeutronQRepJaxb.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void read_A$String() throws Exception {
    final MsgType actual = target.read("/qrep/mqcap/test.xml");
    assertThat(actual.getTrans().getInsertRowOrDeleteRowOrUpdateRow().size(), is(equalTo(1)));
  }

  @Test
  public void read_A$String_T$JAXBException() throws Exception {
    String xmlLocation = "/Users/nobody/test.xml";

    try {
      target.read(xmlLocation);
      fail("Expected exception was not thrown!");
    } catch (Exception e) {
    }
  }

  @Test
  public void read_A$String_T$IOException() throws Exception {
    String xmlLocation = "/Users/nobody/test.xml";

    try {
      target.read(xmlLocation);
      fail("Expected exception was not thrown!");
    } catch (IOException e) {
    }
  }

  @Test
  public void write_A$MsgType$PrintStream() throws Exception {
    MsgType msg = mock(MsgType.class);
    PrintStream out = mock(PrintStream.class);
    target.write(msg, out);
  }

  // @Test
  // public void write_A$MsgType$PrintStream_T$JAXBException() throws Exception {
  // final MsgType msg = target.read("/qrep/mqcap/test.xml");
  // final PrintStream out = mock(PrintStream.class);
  //
  // doThrow(JAXBException.class).when(out).flush();
  // doThrow(JAXBException.class).when(out).println();
  // doThrow(JAXBException.class).when(out).println(any(Object.class));
  // doThrow(JAXBException.class).when(out).print(any(Object.class));
  //
  // try {
  // target.write(msg, out);
  // fail("Expected exception was not thrown!");
  // } catch (JAXBException e) {
  // }
  // }
  //
  // @Test
  // public void write_A$MsgType$PrintStream_T$IOException() throws Exception {
  // MsgType msg = mock(MsgType.class);
  // PrintStream out = mock(PrintStream.class);
  //
  // try {
  // target.write(msg, out);
  // fail("Expected exception was not thrown!");
  // } catch (IOException e) {
  // }
  // }

}
