package gov.ca.cwds.jobs.test;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import mqcap.xsd.MsgType;
import mqcap.xsd.TransType;

public class NeutronQRepJaxb {

  public NeutronQRepJaxb() {

  }

  public static void main(String[] args) throws Exception {
    final JAXBContext jc = JAXBContext.newInstance(TransType.class.getPackage().getName());
    final Unmarshaller unmarshaller = jc.createUnmarshaller();
    final Marshaller m = jc.createMarshaller();

    final File xml = new File("/Users/dsmith/cws_legacy/db2/q_rep/repl/q/test.xml");
    final MsgType config = (MsgType) JAXBIntrospector.getValue(unmarshaller.unmarshal(xml));

    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    m.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION,
        "/Users/dsmith/cws_legacy/db2/q_rep/repl/q/mqcap.xsd");
    m.marshal(config, System.out);
  }

}
