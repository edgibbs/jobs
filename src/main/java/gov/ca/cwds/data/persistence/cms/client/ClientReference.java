package gov.ca.cwds.data.persistence.cms.client;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.EqualsBuilder;

import gov.ca.cwds.data.persistence.cms.CmsPersistentObject;
import gov.ca.cwds.data.std.ApiObjectIdentity;

/**
 * Designates a direct, one step removed, child class of Client, that has a foreign key to its
 * parent.
 * 
 * @author CWDS API Team
 */
public abstract class ClientReference extends CmsPersistentObject {

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
    result = prime * result + ((cltId == null) ? 0 : cltId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false, ApiObjectIdentity.class);
  }

}
