package gov.ca.cwds.data.persistence.cms.client;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

import gov.ca.cwds.data.persistence.cms.VarargPrimaryKey;

public class RawEthnicity extends ClientReference implements NeutronJdbcReader<RawEthnicity> {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "ETH_IDENTIFIER")
  protected String clientEthnicityId;

  @Type(type = "short")
  @Column(name = "ETHNICITY_CODE")
  protected Short clientEthnicityCode;

  @Override
  public RawEthnicity read(ResultSet rs) throws SQLException {
    super.read(rs);
    this.clientEthnicityCode = rs.getShort("ETHNICITY_CODE");
    return this;
  }

  @Override
  public Serializable getPrimaryKey() {
    return new VarargPrimaryKey(getCltId(), clientEthnicityId);
  }

}
