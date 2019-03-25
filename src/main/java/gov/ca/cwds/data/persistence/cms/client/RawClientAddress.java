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

import org.hibernate.annotations.Type;

import gov.ca.cwds.data.persistence.cms.VarargPrimaryKey;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;

@SuppressWarnings({"squid:S2160"})
public class RawClientAddress extends ClientAddressReference
    implements NeutronJdbcReader<RawClientAddress> {

  private static final long serialVersionUID = 1L;

  private RawAddress address;

  // =======================
  // CL_ADDRT: (address)
  // =======================

  @Enumerated(EnumType.STRING)
  @Column(name = "CLA_IBMSNAP_OPERATION", updatable = false)
  protected CmsReplicationOperation claReplicationOperation;

  @Type(type = "timestamp")
  @Column(name = "CLA_IBMSNAP_LOGMARKER", updatable = false)
  protected Date claReplicationDate;

  @Column(name = "CLA_LST_UPD_ID")
  protected String claLastUpdatedId;

  @Type(type = "timestamp")
  @Column(name = "CLA_LST_UPD_TS")
  protected Date claLastUpdatedTime;

  @Column(name = "CLA_FKADDRS_T")
  protected String claFkAddress;

  @Column(name = "CLA_FKCLIENT_T")
  protected String claFkClient;

  @Column(name = "CLA_FKREFERL_T")
  protected String claFkReferral;

  @Type(type = "short")
  @Column(name = "CLA_ADDR_TPC")
  protected Short claAddressType;

  @Column(name = "CLA_HOMLES_IND")
  protected String claHomelessInd;

  @Column(name = "CLA_BK_INMT_ID")
  protected String claBkInmtId;

  @Type(type = "date")
  @Column(name = "CLA_EFF_END_DT")
  protected Date claEffectiveEndDate;

  @Type(type = "date")
  @Column(name = "CLA_EFF_STRTDT")
  protected Date claEffectiveStartDate;

  @Override
  public RawClientAddress read(ResultSet rs) throws SQLException {
    super.read(rs);

    this.claLastUpdatedId = trimToNull(rs.getString(ColumnPosition.CLA_LST_UPD_ID.ordinal()));
    this.claLastUpdatedTime = rs.getTimestamp(ColumnPosition.CLA_LST_UPD_TS.ordinal());
    this.claId = trimToNull(rs.getString(ColumnPosition.CLA_IDENTIFIER.ordinal()));
    this.claFkAddress = trimToNull(rs.getString(ColumnPosition.CLA_FKADDRS_T.ordinal()));
    this.claFkClient = trimToNull(rs.getString(ColumnPosition.CLA_FKCLIENT_T.ordinal()));
    this.claAddressType = rs.getShort(ColumnPosition.CLA_ADDR_TPC.ordinal());
    this.claEffectiveEndDate = rs.getDate(ColumnPosition.CLA_EFF_END_DT.ordinal());
    this.claEffectiveStartDate = rs.getDate(ColumnPosition.CLA_EFF_STRTDT.ordinal());

    this.setClaReplicationOperation(CmsReplicationOperation
        .strToRepOp(rs.getString(ColumnPosition.CLA_IBMSNAP_OPERATION.ordinal())));
    this.setClaReplicationDate(rs.getDate(ColumnPosition.CLA_IBMSNAP_LOGMARKER.ordinal()));

    return this;
  }

  @Override
  public Serializable getPrimaryKey() {
    return new VarargPrimaryKey(claFkClient, claId);
  }

  public CmsReplicationOperation getClaReplicationOperation() {
    return claReplicationOperation;
  }

  public void setClaReplicationOperation(CmsReplicationOperation claReplicationOperation) {
    this.claReplicationOperation = claReplicationOperation;
  }

  public Date getClaReplicationDate() {
    return freshDate(claReplicationDate);
  }

  public void setClaReplicationDate(Date claReplicationDate) {
    this.claReplicationDate = freshDate(claReplicationDate);
  }

  public String getClaLastUpdatedId() {
    return claLastUpdatedId;
  }

  public void setClaLastUpdatedId(String claLastUpdatedId) {
    this.claLastUpdatedId = claLastUpdatedId;
  }

  public Date getClaLastUpdatedTime() {
    return freshDate(claLastUpdatedTime);
  }

  public void setClaLastUpdatedTime(Date claLastUpdatedTime) {
    this.claLastUpdatedTime = freshDate(claLastUpdatedTime);
  }

  public String getClaFkAddress() {
    return claFkAddress;
  }

  public void setClaFkAddress(String claFkAddress) {
    this.claFkAddress = claFkAddress;
  }

  public String getClaFkClient() {
    return claFkClient;
  }

  public void setClaFkClient(String claFkClient) {
    this.claFkClient = claFkClient;
  }

  public String getClaFkReferral() {
    return claFkReferral;
  }

  public void setClaFkReferral(String claFkReferral) {
    this.claFkReferral = claFkReferral;
  }

  public Short getClaAddressType() {
    return claAddressType;
  }

  public void setClaAddressType(Short claAddressType) {
    this.claAddressType = claAddressType;
  }

  public String getClaHomelessInd() {
    return claHomelessInd;
  }

  public void setClaHomelessInd(String claHomelessInd) {
    this.claHomelessInd = claHomelessInd;
  }

  public String getClaBkInmtId() {
    return claBkInmtId;
  }

  public void setClaBkInmtId(String claBkInmtId) {
    this.claBkInmtId = claBkInmtId;
  }

  public Date getClaEffectiveEndDate() {
    return freshDate(claEffectiveEndDate);
  }

  public void setClaEffectiveEndDate(Date date) {
    this.claEffectiveEndDate = freshDate(date);
  }

  public Date getClaEffectiveStartDate() {
    return freshDate(claEffectiveStartDate);
  }

  public void setClaEffectiveStartDate(Date date) {
    this.claEffectiveStartDate = freshDate(date);
  }

  public RawAddress getAddress() {
    return address;
  }

  public void setAddress(RawAddress address) {
    this.address = address;
  }

}
