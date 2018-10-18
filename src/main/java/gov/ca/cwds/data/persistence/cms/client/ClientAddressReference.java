package gov.ca.cwds.data.persistence.cms.client;

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

}
