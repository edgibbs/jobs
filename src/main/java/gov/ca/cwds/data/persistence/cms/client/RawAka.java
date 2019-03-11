package gov.ca.cwds.data.persistence.cms.client;

import static gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils.freshDate;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

import gov.ca.cwds.data.persistence.cms.VarargPrimaryKey;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;

@SuppressWarnings({"squid:S1206"})
public class RawAka extends ClientReference implements NeutronJdbcReader<RawAka> {

  private static final long serialVersionUID = 1L;

  public enum ColumnPosition {
    START, CLT_IDENTIFIER, ONM_THIRD_ID, ONM_FIRST_NM, ONM_LAST_NM, ONM_MIDDLE_NM,

    ONM_NMPRFX_DSC, ONM_NAME_TPC, ONM_SUFX_TLDSC,

    ONM_LST_UPD_ID, ONM_LST_UPD_TS,

    ONM_IBMSNAP_OPERATION, ONM_IBMSNAP_LOGMARKER
  }

  // =====================================
  // OCL_NM_T: (other client name / AKA)
  // =====================================

  @Id
  @Column(name = "ONM_THIRD_ID")
  private String akaId;

  @Column(name = "ONM_FIRST_NM")
  private String akaFirstName;

  @Column(name = "ONM_LAST_NM")
  private String akaLastName;

  @Column(name = "ONM_MIDDLE_NM")
  private String akaMiddleName;

  @Column(name = "ONM_NMPRFX_DSC")
  private String akaNamePrefixDescription;

  @Type(type = "short")
  @Column(name = "ONM_NAME_TPC")
  private Short akaNameType;

  @Column(name = "ONM_SUFX_TLDSC")
  private String akaSuffixTitleDescription;

  @Column(name = "ONM_LST_UPD_ID")
  private String akaLastUpdatedId;

  @Column(name = "ONM_LST_UPD_TS")
  @Type(type = "timestamp")
  private Date akaLastUpdatedTimestamp;

  @Enumerated(EnumType.STRING)
  @Column(name = "ONM_IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation akaLastUpdatedOperation;

  @Column(name = "ONM_IBMSNAP_LOGMARKER")
  @Type(type = "timestamp")
  private Date akaReplicationTimestamp;

  @Override
  public RawAka read(ResultSet rs) throws SQLException {
    super.read(rs);

    akaId = rs.getString(ColumnPosition.ONM_THIRD_ID.ordinal());
    akaFirstName = trimToNull(rs.getString(ColumnPosition.ONM_FIRST_NM.ordinal()));
    akaLastName = trimToNull(rs.getString(ColumnPosition.ONM_LAST_NM.ordinal()));
    akaMiddleName = trimToNull(rs.getString(ColumnPosition.ONM_MIDDLE_NM.ordinal()));
    akaNamePrefixDescription = trimToNull(rs.getString(ColumnPosition.ONM_NMPRFX_DSC.ordinal()));
    akaNameType = rs.getShort(ColumnPosition.ONM_NAME_TPC.ordinal());
    akaSuffixTitleDescription = trimToNull(rs.getString(ColumnPosition.ONM_SUFX_TLDSC.ordinal()));
    akaLastUpdatedId = rs.getString(ColumnPosition.ONM_LST_UPD_ID.ordinal());
    akaLastUpdatedTimestamp = rs.getTimestamp(ColumnPosition.ONM_LST_UPD_TS.ordinal());

    akaLastUpdatedOperation = CmsReplicationOperation
        .strToRepOp(rs.getString(ColumnPosition.ONM_IBMSNAP_OPERATION.ordinal()));
    akaReplicationTimestamp = rs.getTimestamp(ColumnPosition.ONM_IBMSNAP_LOGMARKER.ordinal());

    return this;
  }

  @Override
  public Serializable getPrimaryKey() {
    return new VarargPrimaryKey(getCltId(), akaId);
  }

  public String getAkaId() {
    return akaId;
  }

  public void setAkaId(String akaId) {
    this.akaId = akaId;
  }

  public String getAkaFirstName() {
    return akaFirstName;
  }

  public void setAkaFirstName(String akaFirstName) {
    this.akaFirstName = akaFirstName;
  }

  public String getAkaLastName() {
    return akaLastName;
  }

  public void setAkaLastName(String akaLastName) {
    this.akaLastName = akaLastName;
  }

  public String getAkaMiddleName() {
    return akaMiddleName;
  }

  public void setAkaMiddleName(String akaMiddleName) {
    this.akaMiddleName = akaMiddleName;
  }

  public String getAkaNamePrefixDescription() {
    return akaNamePrefixDescription;
  }

  public void setAkaNamePrefixDescription(String akaNamePrefixDescription) {
    this.akaNamePrefixDescription = akaNamePrefixDescription;
  }

  public Short getAkaNameType() {
    return akaNameType;
  }

  public void setAkaNameType(Short akaNameType) {
    this.akaNameType = akaNameType;
  }

  public String getAkaSuffixTitleDescription() {
    return akaSuffixTitleDescription;
  }

  public void setAkaSuffixTitleDescription(String akaSuffixTitleDescription) {
    this.akaSuffixTitleDescription = akaSuffixTitleDescription;
  }

  public String getAkaLastUpdatedId() {
    return akaLastUpdatedId;
  }

  public void setAkaLastUpdatedId(String akaLastUpdatedId) {
    this.akaLastUpdatedId = akaLastUpdatedId;
  }

  public Date getAkaLastUpdatedTimestamp() {
    return freshDate(akaLastUpdatedTimestamp);
  }

  public void setAkaLastUpdatedTimestamp(Date akaLastUpdatedTimestamp) {
    this.akaLastUpdatedTimestamp = freshDate(akaLastUpdatedTimestamp);
  }

  public CmsReplicationOperation getAkaLastUpdatedOperation() {
    return akaLastUpdatedOperation;
  }

  public void setAkaLastUpdatedOperation(CmsReplicationOperation akaLastUpdatedOperation) {
    this.akaLastUpdatedOperation = akaLastUpdatedOperation;
  }

  public Date getAkaReplicationTimestamp() {
    return freshDate(akaReplicationTimestamp);
  }

  public void setAkaReplicationTimestamp(Date akaReplicationTimestamp) {
    this.akaReplicationTimestamp = freshDate(akaReplicationTimestamp);
  }

}
