package gov.ca.cwds.data.persistence.cms.client;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Id;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiObjectIdentity;

/**
 * Designates a direct child class of Client, that has a foreign key to its parent.
 * 
 * @author CWDS API Team
 */
@SuppressWarnings({"squid:S1206"})
public abstract class ClientReference extends ApiObjectIdentity implements PersistentObject {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "CLT_IDENTIFIER")
  protected String cltId;

  public ClientReference read(ResultSet rs) throws SQLException {
    this.cltId = ifNull(rs.getString("CLT_IDENTIFIER"));
    return this;
  }

  public String getCltId() {
    return cltId;
  }

  public void setCltId(String cltId) {
    this.cltId = cltId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((getCltId() == null) ? 0 : getCltId().hashCode());
    return result;
  }

}
