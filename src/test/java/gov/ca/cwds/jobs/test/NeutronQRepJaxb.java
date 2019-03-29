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

import mqcap.xsd.ObjectFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import mqcap.xsd.MsgType;

/**
 * JAXB facilitator class for DB2 Q replication XML messages.
 * 
 * @author CWDS API Team
 */
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
    final JAXBContext jc = JAXBContext.newInstance(MsgType.class.getPackage().getName());
    final Marshaller m = jc.createMarshaller();

    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    m.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "mqcap.xsd");
    m.marshal(new ObjectFactory().createMsg(msg), out);
  }

  public static void main(String[] args) throws Exception {
    final NeutronQRepJaxb inst = new NeutronQRepJaxb();
    final MsgType msg =
        inst.read(args.length > 0 && StringUtils.isNotBlank(args[0]) ? args[0].trim()
            : "/qrep/mqcap/test.xml");
    inst.write(msg, System.out);
  }

}
