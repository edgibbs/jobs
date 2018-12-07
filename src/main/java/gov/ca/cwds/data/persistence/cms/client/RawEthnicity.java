package gov.ca.cwds.data.persistence.cms.client;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

import gov.ca.cwds.data.persistence.cms.VarargPrimaryKey;

public class RawEthnicity extends ClientReference implements NeutronJdbcReader<RawEthnicity> {

  private static final long serialVersionUID = 1L;

  protected enum ColumnPosition {
    START, CLT_IDENTIFIER, ETH_IDENTIFIER, ETHNICITY_CODE
  }

  // ================================
  // CLSCP_ET: (race & ethnicity)
  // ================================

  @Id
  @Column(name = "ETH_IDENTIFIER")
  protected String clientEthnicityId;

  @Type(type = "short")
  @Column(name = "ETHNICITY_CODE")
  protected Short clientEthnicityCode;

  @Override
  public RawEthnicity read(ResultSet rs) throws SQLException {
    super.read(rs);

    clientEthnicityId = trimToNull(rs.getString(ColumnPosition.ETH_IDENTIFIER.ordinal()));
    clientEthnicityCode = rs.getShort(ColumnPosition.ETHNICITY_CODE.ordinal());

    return this;
  }

  @Override
  public Serializable getPrimaryKey() {
    return new VarargPrimaryKey(getCltId(), clientEthnicityId);
  }

  public String getClientEthnicityId() {
    return clientEthnicityId;
  }

  public void setClientEthnicityId(String clientEthnicityId) {
    this.clientEthnicityId = clientEthnicityId;
  }

  public Short getClientEthnicityCode() {
    return clientEthnicityCode;
  }

  public void setClientEthnicityCode(Short clientEthnicityCode) {
    this.clientEthnicityCode = clientEthnicityCode;
  }

}
