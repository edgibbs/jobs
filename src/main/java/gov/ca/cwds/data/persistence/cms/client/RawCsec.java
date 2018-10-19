package gov.ca.cwds.data.persistence.cms.client;

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

public class RawCsec extends ClientReference implements NeutronJdbcReader<RawCsec> {

  private static final long serialVersionUID = 1L;

  @Column(name = "CSH_THIRD_ID")
  private String csecId;

  @Column(name = "CSH_CSEC_TPC")
  @Type(type = "short")
  private Short csecCodeId;

  @Column(name = "CSH_START_DT")
  @Type(type = "date")
  private Date csecStartDate;

  @Column(name = "CSH_END_DT")
  @Type(type = "date")
  private Date csecEndDate;

  @Column(name = "CSH_LST_UPD_ID")
  private String csecLastUpdatedId;

  @Column(name = "CSH_LST_UPD_TS")
  @Type(type = "timestamp")
  private Date csecLastUpdatedTimestamp;

  @Enumerated(EnumType.STRING)
  @Column(name = "CSH_IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation csecLastUpdatedOperation;

  @Column(name = "CSH_IBMSNAP_LOGMARKER")
  @Type(type = "timestamp")
  private Date csecReplicationTimestamp;

  @Override
  public RawCsec read(ResultSet rs) throws SQLException {
    super.read(rs);
    csecId = rs.getString("CSH_THIRD_ID");
    csecCodeId = rs.getShort("CSH_CSEC_TPC");
    csecStartDate = rs.getDate("CSH_START_DT");
    csecEndDate = rs.getDate("CSH_END_DT");
    csecLastUpdatedId = rs.getString("CSH_LST_UPD_ID");
    csecLastUpdatedTimestamp = rs.getTimestamp("CSH_LST_UPD_TS");
    csecLastUpdatedOperation =
        CmsReplicationOperation.strToRepOp(rs.getString("CSH_IBMSNAP_OPERATION"));
    csecReplicationTimestamp = rs.getTimestamp("CSH_IBMSNAP_LOGMARKER");
    return this;
  }

  @Override
  public Serializable getPrimaryKey() {
    return new VarargPrimaryKey(getCltId(), csecId);
  }

}
