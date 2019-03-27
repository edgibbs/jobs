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
 * Java class for dbServerTypeEnumType.
 * </p>
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * </p>
 * 
 * <pre>
 * &lt;simpleType name="dbServerTypeEnumType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="QDB2"/&gt;
 *     &lt;enumeration value="QDB2/6000"/&gt;
 *     &lt;enumeration value="QDB2/HPUX"/&gt;
 *     &lt;enumeration value="QDB2/NT"/&gt;
 *     &lt;enumeration value="QDB2/SUN"/&gt;
 *     &lt;enumeration value="QDB2/SUN64"/&gt;
 *     &lt;enumeration value="QDB2/SUNX86"/&gt;
 *     &lt;enumeration value="QDB2/SUNX8664"/&gt;
 *     &lt;enumeration value="QDB2/LINUX"/&gt;
 *     &lt;enumeration value="QDB2/Windows"/&gt;
 *     &lt;enumeration value="QDB2/AIX64"/&gt;
 *     &lt;enumeration value="QORACLE"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "dbServerTypeEnumType")
@XmlEnum
public enum DbServerTypeEnumType {

  @XmlEnumValue("QDB2")
  QDB_2("QDB2"), @XmlEnumValue("QDB2/6000")
  QDB_2_6000("QDB2/6000"), @XmlEnumValue("QDB2/HPUX")
  QDB_2_HPUX("QDB2/HPUX"), @XmlEnumValue("QDB2/NT")
  QDB_2_NT("QDB2/NT"), @XmlEnumValue("QDB2/SUN")
  QDB_2_SUN("QDB2/SUN"), @XmlEnumValue("QDB2/SUN64")
  QDB_2_SUN_64("QDB2/SUN64"), @XmlEnumValue("QDB2/SUNX86")
  QDB_2_SUNX_86("QDB2/SUNX86"), @XmlEnumValue("QDB2/SUNX8664")
  QDB_2_SUNX_8664("QDB2/SUNX8664"), @XmlEnumValue("QDB2/LINUX")
  QDB_2_LINUX("QDB2/LINUX"), @XmlEnumValue("QDB2/Windows")
  QDB_2_WINDOWS("QDB2/Windows"), @XmlEnumValue("QDB2/AIX64")
  QDB_2_AIX_64("QDB2/AIX64"), QORACLE("QORACLE");
  private final String value;

  DbServerTypeEnumType(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static DbServerTypeEnumType fromValue(String v) {
    for (DbServerTypeEnumType c : DbServerTypeEnumType.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }

}