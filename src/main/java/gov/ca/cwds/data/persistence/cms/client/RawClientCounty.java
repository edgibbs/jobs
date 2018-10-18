package gov.ca.cwds.data.persistence.cms.client;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

import gov.ca.cwds.data.persistence.PersistentObject;

public class RawClientCounty implements PersistentObject, NeutronJdbcReader<RawClientCounty> {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "CLC_CLIENT_ID")
  protected String clientCountyId;

  @Type(type = "short")
  @Column(name = "CLC_GVR_ENTC")
  protected Short clientCounty;

  @Column(name = "CLC_CNTY_RULE")
  protected String clientCountyRule;

  @Override
  public RawClientCounty read(ResultSet rs) throws SQLException {
    this.clientCounty = rs.getShort("CLC_GVR_ENTC");
    this.clientCountyId = ifNull(rs.getString("CLC_CLIENT_ID"));
    return this;
  }

  @Override
  public Serializable getPrimaryKey() {
    return clientCountyId;
  }

  public Short getClientCounty() {
    return clientCounty;
  }

  public void setClientCounty(Short clientCounty) {
    this.clientCounty = clientCounty;
  }

  public String getClientCountyId() {
    return clientCountyId;
  }

  public void setClientCountyId(String clientCountyId) {
    this.clientCountyId = clientCountyId;
  }

  public String getClientCountyRule() {
    return clientCountyRule;
  }

  public void setClientCountyRule(String clientCountyRule) {
    this.clientCountyRule = clientCountyRule;
  }

}
