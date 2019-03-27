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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>
 * Java class for rowOpType complex type.
 * </p>
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * </p>
 * 
 * <pre>
 * &lt;complexType name="rowOpType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="insertRow" type="{}singleValRowType"/&gt;
 *         &lt;element name="deleteRow" type="{}singleValRowType"/&gt;
 *         &lt;element name="updateRow" type="{}updateRowType"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="isLast" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
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
@XmlType(name = "rowOpType", propOrder = {"insertRow", "deleteRow", "updateRow"})
public class RowOpType {

  protected SingleValRowType insertRow;
  protected SingleValRowType deleteRow;
  protected UpdateRowType updateRow;
  @XmlAttribute(name = "isLast")
  protected Boolean isLast;
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
   * Gets the value of the insertRow property.
   * 
   * @return possible object is {@link SingleValRowType }
   * 
   */
  public SingleValRowType getInsertRow() {
    return insertRow;
  }

  /**
   * Sets the value of the insertRow property.
   * 
   * @param value allowed object is {@link SingleValRowType }
   * 
   */
  public void setInsertRow(SingleValRowType value) {
    this.insertRow = value;
  }

  /**
   * Gets the value of the deleteRow property.
   * 
   * @return possible object is {@link SingleValRowType }
   * 
   */
  public SingleValRowType getDeleteRow() {
    return deleteRow;
  }

  /**
   * Sets the value of the deleteRow property.
   * 
   * @param value allowed object is {@link SingleValRowType }
   * 
   */
  public void setDeleteRow(SingleValRowType value) {
    this.deleteRow = value;
  }

  /**
   * Gets the value of the updateRow property.
   * 
   * @return possible object is {@link UpdateRowType }
   * 
   */
  public UpdateRowType getUpdateRow() {
    return updateRow;
  }

  /**
   * Sets the value of the updateRow property.
   * 
   * @param value allowed object is {@link UpdateRowType }
   * 
   */
  public void setUpdateRow(UpdateRowType value) {
    this.updateRow = value;
  }

  /**
   * Gets the value of the isLast property.
   * 
   * @return possible object is {@link Boolean }
   * 
   */
  public Boolean isIsLast() {
    return isLast;
  }

  /**
   * Sets the value of the isLast property.
   * 
   * @param value allowed object is {@link Boolean }
   * 
   */
  public void setIsLast(Boolean value) {
    this.isLast = value;
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