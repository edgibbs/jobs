package gov.ca.cwds.data.persistence.cms.client;

import static gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils.freshDate;

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

@SuppressWarnings({"squid:S2160"})
public class RawSafetyAlert extends ClientReference implements NeutronJdbcReader<RawSafetyAlert> {

  private static final long serialVersionUID = 1L;

  public enum ColumnPosition {
    START, CLT_IDENTIFIER, SAL_THIRD_ID, SAL_ACTV_RNC, SAL_ACTV_DT, SAL_ACTV_GEC, SAL_ACTV_TXT,

    SAL_DACT_DT, SAL_DACT_GEC, SAL_DACT_TXT,

    SAL_LST_UPD_ID, SAL_LST_UPD_TS,

    SAL_IBMSNAP_LOGMARKER, SAL_IBMSNAP_OPERATION
  }

  // ================================
  // SAF_ALRT: (safety alerts)
  // ================================

  @Id
  @Column(name = "SAL_THIRD_ID")
  private String safetyAlertId;

  @Column(name = "SAL_ACTV_RNC")
  @Type(type = "short")
  private Short safetyAlertActivationReasonCode;

  @Column(name = "SAL_ACTV_DT")
  @Type(type = "date")
  private Date safetyAlertActivationDate;

  @Column(name = "SAL_ACTV_GEC")
  @Type(type = "short")
  private Short safetyAlertActivationCountyCode;

  @Column(name = "SAL_ACTV_TXT")
  private String safetyAlertActivationExplanation;

  @Column(name = "SAL_DACT_DT")
  @Type(type = "date")
  private Date safetyAlertDeactivationDate;

  @Column(name = "SAL_DACT_GEC")
  @Type(type = "short")
  private Short safetyAlertDeactivationCountyCode;

  @Column(name = "SAL_DACT_TXT")
  private String safetyAlertDeactivationExplanation;

  @Column(name = "SAL_LST_UPD_ID")
  private String safetyAlertLastUpdatedId;

  @Column(name = "SAL_LST_UPD_TS")
  @Type(type = "timestamp")
  private Date safetyAlertLastUpdatedTimestamp;

  @Enumerated(EnumType.STRING)
  @Column(name = "SAL_IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation safetyAlertLastUpdatedOperation;

  @Column(name = "SAL_IBMSNAP_LOGMARKER")
  @Type(type = "timestamp")
  private Date safetyAlertReplicationTimestamp;

  @Override
  public RawSafetyAlert read(ResultSet rs) throws SQLException {
    super.read(rs);

    safetyAlertId = rs.getString(ColumnPosition.SAL_THIRD_ID.ordinal());
    safetyAlertActivationCountyCode = rs.getShort(ColumnPosition.SAL_ACTV_GEC.ordinal());
    safetyAlertActivationDate = rs.getDate(ColumnPosition.SAL_ACTV_DT.ordinal());
    safetyAlertActivationExplanation = rs.getString(ColumnPosition.SAL_ACTV_TXT.ordinal());
    safetyAlertActivationReasonCode = rs.getShort(ColumnPosition.SAL_ACTV_RNC.ordinal());
    safetyAlertDeactivationCountyCode = rs.getShort(ColumnPosition.SAL_DACT_GEC.ordinal());
    safetyAlertDeactivationDate = rs.getDate(ColumnPosition.SAL_DACT_DT.ordinal());
    safetyAlertDeactivationExplanation = rs.getString(ColumnPosition.SAL_DACT_TXT.ordinal());
    safetyAlertLastUpdatedId = rs.getString(ColumnPosition.SAL_LST_UPD_ID.ordinal());
    safetyAlertLastUpdatedTimestamp = rs.getTimestamp(ColumnPosition.SAL_LST_UPD_TS.ordinal());

    safetyAlertLastUpdatedOperation = CmsReplicationOperation
        .strToRepOp(rs.getString(ColumnPosition.SAL_IBMSNAP_OPERATION.ordinal()));
    safetyAlertReplicationTimestamp =
        rs.getTimestamp(ColumnPosition.SAL_IBMSNAP_LOGMARKER.ordinal());

    return this;
  }

  @Override
  public Serializable getPrimaryKey() {
    return new VarargPrimaryKey(getCltId(), safetyAlertId);
  }

  public String getSafetyAlertId() {
    return safetyAlertId;
  }

  public void setSafetyAlertId(String safetyAlertId) {
    this.safetyAlertId = safetyAlertId;
  }

  public Short getSafetyAlertActivationReasonCode() {
    return safetyAlertActivationReasonCode;
  }

  public void setSafetyAlertActivationReasonCode(Short safetyAlertActivationReasonCode) {
    this.safetyAlertActivationReasonCode = safetyAlertActivationReasonCode;
  }

  public Date getSafetyAlertActivationDate() {
    return freshDate(safetyAlertActivationDate);
  }

  public void setSafetyAlertActivationDate(Date safetyAlertActivationDate) {
    this.safetyAlertActivationDate = freshDate(safetyAlertActivationDate);
  }

  public Short getSafetyAlertActivationCountyCode() {
    return safetyAlertActivationCountyCode;
  }

  public void setSafetyAlertActivationCountyCode(Short safetyAlertActivationCountyCode) {
    this.safetyAlertActivationCountyCode = safetyAlertActivationCountyCode;
  }

  public String getSafetyAlertActivationExplanation() {
    return safetyAlertActivationExplanation;
  }

  public void setSafetyAlertActivationExplanation(String safetyAlertActivationExplanation) {
    this.safetyAlertActivationExplanation = safetyAlertActivationExplanation;
  }

  public Date getSafetyAlertDeactivationDate() {
    return freshDate(safetyAlertDeactivationDate);
  }

  public void setSafetyAlertDeactivationDate(Date safetyAlertDeactivationDate) {
    this.safetyAlertDeactivationDate = freshDate(safetyAlertDeactivationDate);
  }

  public Short getSafetyAlertDeactivationCountyCode() {
    return safetyAlertDeactivationCountyCode;
  }

  public void setSafetyAlertDeactivationCountyCode(Short safetyAlertDeactivationCountyCode) {
    this.safetyAlertDeactivationCountyCode = safetyAlertDeactivationCountyCode;
  }

  public String getSafetyAlertDeactivationExplanation() {
    return safetyAlertDeactivationExplanation;
  }

  public void setSafetyAlertDeactivationExplanation(String safetyAlertDeactivationExplanation) {
    this.safetyAlertDeactivationExplanation = safetyAlertDeactivationExplanation;
  }

  public String getSafetyAlertLastUpdatedId() {
    return safetyAlertLastUpdatedId;
  }

  public void setSafetyAlertLastUpdatedId(String safetyAlertLastUpdatedId) {
    this.safetyAlertLastUpdatedId = safetyAlertLastUpdatedId;
  }

  public Date getSafetyAlertLastUpdatedTimestamp() {
    return freshDate(safetyAlertLastUpdatedTimestamp);
  }

  public void setSafetyAlertLastUpdatedTimestamp(Date safetyAlertLastUpdatedTimestamp) {
    this.safetyAlertLastUpdatedTimestamp = freshDate(safetyAlertLastUpdatedTimestamp);
  }

  public CmsReplicationOperation getSafetyAlertLastUpdatedOperation() {
    return safetyAlertLastUpdatedOperation;
  }

  public void setSafetyAlertLastUpdatedOperation(
      CmsReplicationOperation safetyAlertLastUpdatedOperation) {
    this.safetyAlertLastUpdatedOperation = safetyAlertLastUpdatedOperation;
  }

  public Date getSafetyAlertReplicationTimestamp() {
    return freshDate(safetyAlertReplicationTimestamp);
  }

  public void setSafetyAlertReplicationTimestamp(Date safetyAlertReplicationTimestamp) {
    this.safetyAlertReplicationTimestamp = freshDate(safetyAlertReplicationTimestamp);
  }

}
