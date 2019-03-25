package gov.ca.cwds.neutron.rocket.cases;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.ca.cwds.data.std.ApiObjectIdentity;

/**
 * Convenient carrier bean for client/case keys.
 * 
 * @author CWDS API Team
 */
@SuppressWarnings({"squid:S2160"})
public class MinClientCase extends ApiObjectIdentity {

  private static final long serialVersionUID = 1L;

  private String clientId;
  private String caseId;

  public MinClientCase(String clientId, String caseId) {
    this.clientId = clientId;
    this.caseId = caseId;
  }

  public static MinClientCase extract(final ResultSet rs) throws SQLException {
    return new MinClientCase(rs.getString("CLIENT_ID"), rs.getString("CASE_ID"));
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getCaseId() {
    return caseId;
  }

  public void setCaseId(String referralId) {
    this.caseId = referralId;
  }


}
