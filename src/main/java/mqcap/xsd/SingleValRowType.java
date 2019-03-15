//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.03.14 at 03:14:29 PM PDT 
//


package mqcap.xsd;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for singleValRowType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="singleValRowType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded">
 *         &lt;element name="col" type="{}singleValColType"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{}opAttrGroup"/>
 *       &lt;attGroup ref="{}commonAttrGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "singleValRowType", propOrder = {
    "col"
})
public class SingleValRowType {

    @XmlElement(required = true)
    protected List<SingleValColType> col;
    @XmlAttribute(name = "rowNum")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger rowNum;
    @XmlAttribute(name = "hasLOBCols")
    protected Boolean hasLOBCols;
    @XmlAttribute(name = "subName", required = true)
    protected String subName;
    @XmlAttribute(name = "srcOwner", required = true)
    protected String srcOwner;
    @XmlAttribute(name = "srcName", required = true)
    protected String srcName;
    @XmlAttribute(name = "intentSEQ")
    protected String intentSEQ;

    /**
     * Gets the value of the col property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the col property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCol().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SingleValColType }
     * 
     * 
     */
    public List<SingleValColType> getCol() {
        if (col == null) {
            col = new ArrayList<SingleValColType>();
        }
        return this.col;
    }

    /**
     * Gets the value of the rowNum property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRowNum() {
        return rowNum;
    }

    /**
     * Sets the value of the rowNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRowNum(BigInteger value) {
        this.rowNum = value;
    }

    /**
     * Gets the value of the hasLOBCols property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHasLOBCols() {
        if (hasLOBCols == null) {
            return false;
        } else {
            return hasLOBCols;
        }
    }

    /**
     * Sets the value of the hasLOBCols property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHasLOBCols(Boolean value) {
        this.hasLOBCols = value;
    }

    /**
     * Gets the value of the subName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubName() {
        return subName;
    }

    /**
     * Sets the value of the subName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubName(String value) {
        this.subName = value;
    }

    /**
     * Gets the value of the srcOwner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrcOwner() {
        return srcOwner;
    }

    /**
     * Sets the value of the srcOwner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrcOwner(String value) {
        this.srcOwner = value;
    }

    /**
     * Gets the value of the srcName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrcName() {
        return srcName;
    }

    /**
     * Sets the value of the srcName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrcName(String value) {
        this.srcName = value;
    }

    /**
     * Gets the value of the intentSEQ property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIntentSEQ() {
        return intentSEQ;
    }

    /**
     * Sets the value of the intentSEQ property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIntentSEQ(String value) {
        this.intentSEQ = value;
    }

}
