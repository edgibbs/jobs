package gov.ca.cwds.data.persistence.cms.client;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Type;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;

public class RawAddress implements PersistentObject, NeutronJdbcReader<RawAddress> {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "ADR_IDENTIFIER")
  protected String adrId;

  @Enumerated(EnumType.STRING)
  @Column(name = "ADR_IBMSNAP_OPERATION", updatable = false)
  protected CmsReplicationOperation adrReplicationOperation;

  @Type(type = "timestamp")
  @Column(name = "ADR_IBMSNAP_LOGMARKER", updatable = false)
  protected Date adrReplicationDate;

  @Column(name = "ADR_CITY_NM")
  protected String adrCity;

  @Column(name = "ADR_EMRG_TELNO")
  protected Long adrEmergencyNumber;

  @Type(type = "integer")
  @Column(name = "ADR_EMRG_EXTNO")
  protected Integer adrEmergencyExtension;

  @Column(name = "ADR_FRG_ADRT_B")
  protected String adrFrgAdrtB;

  @Type(type = "short")
  @Column(name = "ADR_GVR_ENTC")
  protected Short adrGovernmentEntityCd;

  @Column(name = "ADR_MSG_TEL_NO")
  protected Long adrMessageNumber;

  @Type(type = "integer")
  @Column(name = "ADR_MSG_EXT_NO")
  protected Integer adrMessageExtension;

  @Column(name = "ADR_HEADER_ADR")
  protected String adrHeaderAddress;

  @Column(name = "ADR_PRM_TEL_NO")
  protected Long adrPrimaryNumber;

  @Type(type = "integer")
  @Column(name = "ADR_PRM_EXT_NO")
  protected Integer adrPrimaryExtension;

  @Type(type = "short")
  @Column(name = "ADR_STATE_C")
  protected Short adrState;

  @Column(name = "ADR_STREET_NM")
  @ColumnTransformer(read = "trim(ADR_STREET_NM)")
  protected String adrStreetName;

  @Column(name = "ADR_STREET_NO")
  protected String adrStreetNumber;

  @Column(name = "ADR_ZIP_NO")
  protected String adrZip;

  @Column(name = "ADR_ADDR_DSC")
  protected String adrAddressDescription;

  @Type(type = "short")
  @Column(name = "ADR_ZIP_SFX_NO")
  protected Short adrZip4;

  @Column(name = "ADR_POSTDIR_CD")
  protected String adrPostDirCd;

  @Column(name = "ADR_PREDIR_CD")
  protected String adrPreDirCd;

  @Type(type = "short")
  @Column(name = "ADR_ST_SFX_C")
  protected Short adrStreetSuffixCd;

  @Type(type = "short")
  @Column(name = "ADR_UNT_DSGC")
  protected Short adrUnitDesignationCd;

  @Column(name = "ADR_UNIT_NO")
  protected String adrUnitNumber;

  @Column(name = "ADR_LST_UPD_TS")
  protected Date adrLastUpdatedTime;

  @Override
  public RawAddress read(ResultSet rs) throws SQLException {
    this.adrId = ifNull(rs.getString("ADR_IDENTIFIER"));
    this.adrCity = ifNull(rs.getString("ADR_CITY_NM"));
    this.adrEmergencyNumber = rs.getLong("ADR_EMRG_TELNO");
    this.adrEmergencyExtension = rs.getInt("ADR_EMRG_EXTNO");
    this.adrFrgAdrtB = ifNull(rs.getString("ADR_FRG_ADRT_B"));
    this.adrGovernmentEntityCd = rs.getShort("ADR_GVR_ENTC");
    this.adrMessageNumber = rs.getLong("ADR_MSG_TEL_NO");
    this.adrMessageExtension = rs.getInt("ADR_MSG_EXT_NO");
    this.adrHeaderAddress = ifNull(rs.getString("ADR_HEADER_ADR"));
    this.adrPrimaryNumber = rs.getLong("ADR_PRM_TEL_NO");
    this.adrPrimaryExtension = rs.getInt("ADR_PRM_EXT_NO");
    this.adrState = rs.getShort("ADR_STATE_C");
    this.adrStreetName = ifNull(rs.getString("ADR_STREET_NM"));
    this.adrStreetNumber = ifNull(rs.getString("ADR_STREET_NO"));
    this.adrZip = ifNull(rs.getString("ADR_ZIP_NO"));
    this.adrAddressDescription = ifNull(rs.getString("ADR_ADDR_DSC"));
    this.adrZip4 = rs.getShort("ADR_ZIP_SFX_NO");
    this.adrPostDirCd = ifNull(rs.getString("ADR_POSTDIR_CD"));
    this.adrPreDirCd = ifNull(rs.getString("ADR_PREDIR_CD"));
    this.adrStreetSuffixCd = rs.getShort("ADR_ST_SFX_C");
    this.adrUnitDesignationCd = rs.getShort("ADR_UNT_DSGC");
    this.adrUnitNumber = ifNull(rs.getString("ADR_UNIT_NO"));
    this.adrLastUpdatedTime = rs.getTimestamp("ADR_LST_UPD_TS");

    this.adrReplicationOperation =
        CmsReplicationOperation.strToRepOp(rs.getString("ADR_IBMSNAP_OPERATION"));
    this.adrReplicationDate = rs.getDate("ADR_IBMSNAP_LOGMARKER");
    return this;
  }

  @Override
  public Serializable getPrimaryKey() {
    return this.getAdrId();
  }

  public String getAdrId() {
    return adrId;
  }

  public void setAdrId(String adrId) {
    this.adrId = adrId;
  }

  public String getAdrCity() {
    return adrCity;
  }

  public void setAdrCity(String adrCity) {
    this.adrCity = adrCity;
  }

  public Long getAdrEmergencyNumber() {
    return adrEmergencyNumber;
  }

  public void setAdrEmergencyNumber(Long adrEmergencyNumber) {
    this.adrEmergencyNumber = adrEmergencyNumber;
  }

  public Integer getAdrEmergencyExtension() {
    return adrEmergencyExtension;
  }

  public void setAdrEmergencyExtension(Integer adrEmergencyExtension) {
    this.adrEmergencyExtension = adrEmergencyExtension;
  }

  public String getAdrFrgAdrtB() {
    return adrFrgAdrtB;
  }

  public void setAdrFrgAdrtB(String adrFrgAdrtB) {
    this.adrFrgAdrtB = adrFrgAdrtB;
  }

  public Short getAdrGovernmentEntityCd() {
    return adrGovernmentEntityCd;
  }

  public void setAdrGovernmentEntityCd(Short adrGovernmentEntityCd) {
    this.adrGovernmentEntityCd = adrGovernmentEntityCd;
  }

  public Date getAdrLastUpdatedTime() {
    return adrLastUpdatedTime;
  }

  public void setAdrLastUpdatedTime(Date adrLastUpdatedTime) {
    this.adrLastUpdatedTime = adrLastUpdatedTime;
  }

  public Long getAdrMessageNumber() {
    return adrMessageNumber;
  }

  public void setAdrMessageNumber(Long adrMessageNumber) {
    this.adrMessageNumber = adrMessageNumber;
  }

  public Integer getAdrMessageExtension() {
    return adrMessageExtension;
  }

  public void setAdrMessageExtension(Integer adrMessageExtension) {
    this.adrMessageExtension = adrMessageExtension;
  }

  public String getAdrHeaderAddress() {
    return adrHeaderAddress;
  }

  public void setAdrHeaderAddress(String adrHeaderAddress) {
    this.adrHeaderAddress = adrHeaderAddress;
  }

  public Long getAdrPrimaryNumber() {
    return adrPrimaryNumber;
  }

  public void setAdrPrimaryNumber(Long adrPrimaryNumber) {
    this.adrPrimaryNumber = adrPrimaryNumber;
  }

  public Integer getAdrPrimaryExtension() {
    return adrPrimaryExtension;
  }

  public Short getAdrState() {
    return adrState;
  }

  public String getAdrStreetName() {
    return adrStreetName;
  }

  public String getAdrStreetNumber() {
    return adrStreetNumber;
  }

  public String getAdrZip() {
    return adrZip;
  }

  public String getAdrAddressDescription() {
    return adrAddressDescription;
  }

  public void setAdrAddressDescription(String adrAddressDescription) {
    this.adrAddressDescription = adrAddressDescription;
  }

  public Short getAdrZip4() {
    return adrZip4;
  }

  public String getAdrPostDirCd() {
    return adrPostDirCd;
  }

  public String getAdrPreDirCd() {
    return adrPreDirCd;
  }

  public Short getAdrStreetSuffixCd() {
    return adrStreetSuffixCd;
  }

  public Short getAdrUnitDesignationCd() {
    return adrUnitDesignationCd;
  }

  public String getAdrUnitNumber() {
    return adrUnitNumber;
  }

}
