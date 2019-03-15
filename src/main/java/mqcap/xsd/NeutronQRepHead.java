package mqcap.xsd;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NeutronQRepHead {

  private MsgType message;

  @XmlElementWrapper(name = "msg")
  @XmlElement(name = "trans")
  public MsgType getMessage() {
    return message;
  }

}
