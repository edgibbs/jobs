//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2019.03.14 at 03:14:29 PM PDT
//


package mqcap.xsd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>
 * Java class for loadDoneType complex type.
 * </p>
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * </p>
 * 
 * <pre>
 * &lt;complexType name="loadDoneType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="subName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "loadDoneType")
public class LoadDoneType {

  @XmlAttribute(name = "subName", required = true)
  protected String subName;

  /**
   * Gets the value of the subName property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getSubName() {
    return subName;
  }

  /**
   * Sets the value of the subName property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setSubName(String value) {
    this.subName = value;
  }

}