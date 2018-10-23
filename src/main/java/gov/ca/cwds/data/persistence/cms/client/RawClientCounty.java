package gov.ca.cwds.data.persistence.cms.client;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;

import org.hibernate.annotations.Type;

import gov.ca.cwds.data.persistence.cms.VarargPrimaryKey;

public class RawClientCounty extends ClientReference implements NeutronJdbcReader<RawClientCounty> {

  private static final long serialVersionUID = 1L;

  // ================================
  // CLIENT_CNTY: (client county)
  // ================================

  @Type(type = "short")
  @Column(name = "CLC_GVR_ENTC")
  protected Short clientCounty;

  @Column(name = "CLC_CNTY_RULE")
  protected String clientCountyRule;

  @Override
  public RawClientCounty read(ResultSet rs) throws SQLException {
    super.read(rs);
    this.clientCounty = rs.getShort("CLC_GVR_ENTC");
    this.clientCountyRule = ifNull(rs.getString("CLC_CNTY_RULE"));
    return this;
  }

  @Override
  public Serializable getPrimaryKey() {
    return new VarargPrimaryKey(getCltId(), getClientCounty().toString());
  }

  public Short getClientCounty() {
    return clientCounty;
  }

  public void setClientCounty(Short clientCounty) {
    this.clientCounty = clientCounty;
  }

  public String getClientCountyRule() {
    return clientCountyRule;
  }

  public void setClientCountyRule(String clientCountyRule) {
    this.clientCountyRule = clientCountyRule;
  }

}
