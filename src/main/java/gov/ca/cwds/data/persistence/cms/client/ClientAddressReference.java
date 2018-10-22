package gov.ca.cwds.data.persistence.cms.client;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Id;

public class ClientAddressReference extends ClientReference {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "CLA_IDENTIFIER")
  protected String claId;

  @Override
  public ClientReference read(ResultSet rs) throws SQLException {
    this.claId = ifNull(rs.getString("CLA_IDENTIFIER"));
    return super.read(rs);
  }

  @Override
  public Serializable getPrimaryKey() {
    return null;
  }

  public String getClaId() {
    return claId;
  }

  public void setClaId(String claId) {
    this.claId = claId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((getCltId() == null) ? 0 : getCltId().hashCode());
    result = prime * result + ((getClaId() == null) ? 0 : getClaId().hashCode());
    return result;
  }

}
