//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2019.03.14 at 03:14:29 PM PDT
//


package mqcap.xsd;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>
 * Java class for transType complex type.
 * </p>
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * </p>
 * 
 * <pre>
 * &lt;complexType name="transType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="insertRow" type="{}singleValRowType"/&gt;
 *         &lt;element name="deleteRow" type="{}singleValRowType"/&gt;
 *         &lt;element name="updateRow" type="{}updateRowType"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="isLast" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="segmentNum" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
 *       &lt;attribute name="cmitLSN" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="cmitTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="authID" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="correlationID" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="planName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "transType", propOrder = {"insertRowOrDeleteRowOrUpdateRow"})
public class TransType {

  @XmlElementRefs({@XmlElementRef(name = "insertRow", type = JAXBElement.class, required = false),
      @XmlElementRef(name = "updateRow", type = JAXBElement.class, required = false),
      @XmlElementRef(name = "deleteRow", type = JAXBElement.class, required = false)})
  protected List<JAXBElement<?>> insertRowOrDeleteRowOrUpdateRow;
  @XmlAttribute(name = "isLast", required = true)
  protected boolean isLast;
  @XmlAttribute(name = "segmentNum", required = true)
  @XmlSchemaType(name = "positiveInteger")
  protected BigInteger segmentNum;
  @XmlAttribute(name = "cmitLSN", required = true)
  protected String cmitLSN;
  @XmlAttribute(name = "cmitTime", required = true)
  @XmlSchemaType(name = "dateTime")
  protected XMLGregorianCalendar cmitTime;
  @XmlAttribute(name = "authID")
  protected String authID;
  @XmlAttribute(name = "correlationID")
  protected String correlationID;
  @XmlAttribute(name = "planName")
  protected String planName;

  /**
   * Gets the value of the insertRowOrDeleteRowOrUpdateRow property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the insertRowOrDeleteRowOrUpdateRow property.
   * </p>
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * </p>
   * 
   * <pre>
   * getInsertRowOrDeleteRowOrUpdateRow().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link JAXBElement
   * }{@code <}{@link SingleValRowType }{@code >} {@link JAXBElement
   * }{@code <}{@link SingleValRowType }{@code >} {@link JAXBElement }{@code <}{@link UpdateRowType
   * }{@code >}
   * </p>
   * 
   * @return list of elements
   */
  public List<JAXBElement<?>> getInsertRowOrDeleteRowOrUpdateRow() {
    if (insertRowOrDeleteRowOrUpdateRow == null) {
      insertRowOrDeleteRowOrUpdateRow = new ArrayList<JAXBElement<?>>();
    }
    return this.insertRowOrDeleteRowOrUpdateRow;
  }

  /**
   * Gets the value of the isLast property.
   * 
   * @return value
   */
  public boolean isIsLast() {
    return isLast;
  }

  /**
   * Sets the value of the isLast property.
   * 
   * @param value value
   */
  public void setIsLast(boolean value) {
    this.isLast = value;
  }

  /**
   * Gets the value of the segmentNum property.
   * 
   * @return possible object is {@link BigInteger }
   * 
   */
  public BigInteger getSegmentNum() {
    return segmentNum;
  }

  /**
   * Sets the value of the segmentNum property.
   * 
   * @param value allowed object is {@link BigInteger }
   * 
   */
  public void setSegmentNum(BigInteger value) {
    this.segmentNum = value;
  }

  /**
   * Gets the value of the cmitLSN property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getCmitLSN() {
    return cmitLSN;
  }

  /**
   * Sets the value of the cmitLSN property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setCmitLSN(String value) {
    this.cmitLSN = value;
  }

  /**
   * Gets the value of the cmitTime property.
   * 
   * @return possible object is {@link XMLGregorianCalendar }
   * 
   */
  public XMLGregorianCalendar getCmitTime() {
    return cmitTime;
  }

  /**
   * Sets the value of the cmitTime property.
   * 
   * @param value allowed object is {@link XMLGregorianCalendar }
   * 
   */
  public void setCmitTime(XMLGregorianCalendar value) {
    this.cmitTime = value;
  }

  /**
   * Gets the value of the authID property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getAuthID() {
    return authID;
  }

  /**
   * Sets the value of the authID property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setAuthID(String value) {
    this.authID = value;
  }

  /**
   * Gets the value of the correlationID property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getCorrelationID() {
    return correlationID;
  }

  /**
   * Sets the value of the correlationID property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setCorrelationID(String value) {
    this.correlationID = value;
  }

  /**
   * Gets the value of the planName property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getPlanName() {
    return planName;
  }

  /**
   * Sets the value of the planName property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setPlanName(String value) {
    this.planName = value;
  }

}
