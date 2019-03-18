package gov.ca.cwds.jobs.test;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.IOUtils;

import mqcap.xsd.MsgType;
import mqcap.xsd.TransType;

public class NeutronQRepJaxb {

  public NeutronQRepJaxb() {}

  public MsgType read(String xmlLocation) throws JAXBException, IOException {
    MsgType ret = null;
    final JAXBContext jc = JAXBContext.newInstance(MsgType.class.getPackage().getName());
    try (final Reader r =
        new StringReader(IOUtils.resourceToString(xmlLocation, Charset.defaultCharset()));) {
      ret = (MsgType) JAXBIntrospector.getValue(jc.createUnmarshaller().unmarshal(r));
    } finally {
      // auto-close reader
    }

    return ret;
  }

  public void write(MsgType msg, PrintStream out) throws JAXBException, IOException {
    final JAXBContext jc = JAXBContext.newInstance(TransType.class.getPackage().getName());
    final Marshaller m = jc.createMarshaller();

    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    m.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "/qrep/mqcap/mqcap.xsd");
    m.marshal(msg, out);
  }

  public static void main(String[] args) throws Exception {
    final NeutronQRepJaxb inst = new NeutronQRepJaxb();
    final MsgType msg = inst.read("/qrep/mqcap/test.xml");
    inst.write(msg, System.out);
  }

}
