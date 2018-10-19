package gov.ca.cwds.data.persistence.cms.client;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Id;

import gov.ca.cwds.data.persistence.cms.CmsPersistentObject;

/**
 * Designates a direct child class of Client, one step removed with a foreign key to its parent.
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
    // setLastUpdatedId(ifNull(rs.getString("LST_UPD_ID")));
    // setLastUpdatedTime(rs.getDate("LST_UPD_TS"));
    return this;
  }

  public String getCltId() {
    return cltId;
  }

  public void setCltId(String cltId) {
    this.cltId = cltId;
  }

}
