//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2019.03.14 at 03:14:29 PM PDT
//


package mqcap.xsd;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>
 * Java class for loadPhaseEnumType.
 * </p>
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * </p>
 * 
 * <pre>
 * &lt;simpleType name="loadPhaseEnumType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="none"/&gt;
 *     &lt;enumeration value="external"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "loadPhaseEnumType")
@XmlEnum
public enum LoadPhaseEnumType {

  @XmlEnumValue("none")
  NONE("none"), @XmlEnumValue("external")
  EXTERNAL("external");
  private final String value;

  LoadPhaseEnumType(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static LoadPhaseEnumType fromValue(String v) {
    for (LoadPhaseEnumType c : LoadPhaseEnumType.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }

}