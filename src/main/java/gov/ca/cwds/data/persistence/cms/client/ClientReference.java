package gov.ca.cwds.data.persistence.cms.client;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Id;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiObjectIdentity;
import gov.ca.cwds.neutron.rocket.ClientSQLResource;

/**
 * Designates a direct child class of Client, that has a foreign key to its parent.
 * 
 * @author CWDS API Team
 */
@SuppressWarnings({"squid:S2160", "squid:S1206", "serial"})
public abstract class ClientReference extends ApiObjectIdentity implements PersistentObject {

  private static final long serialVersionUID = 1L;

  /**
   * Column position for performance.
   * 
   * @author CWDS API Team
   * @see ClientSQLResource#SEL_CLI
   */
  protected enum ColPos {
    START, CLT_IDENTIFIER, CLT_LST_UPD_ID, CLT_LST_UPD_TS,

    CLT_BIRTH_DT, CLT_CL_INDX_NO, CLT_COM_FST_NM, CLT_COM_LST_NM, CLT_COM_MID_NM,

    CLT_EMAIL_ADDR, CLT_GENDER_CD, CLT_HISP_UD_CD, CLT_HISP_CD, CLT_MRTL_STC, CLT_NMPRFX_DSC,

    CLT_NAME_TPC, CLT_P_ETHNCTYC, CLT_P_LANG_TPC, CLT_RLGN_TPC, CLT_S_LANG_TC, CLT_SENSTV_IND,

    CLT_SS_NO, CLT_SUFX_TLDSC, CLT_IBMSNAP_LOGMARKER, CLT_IBMSNAP_OPERATION, CLT_ADDED_TS
  }

  @Id
  @Column(name = "CLT_IDENTIFIER")
  protected String cltId;

  public ClientReference read(ResultSet rs) throws SQLException {
    this.cltId = trimToNull(rs.getString(ColPos.CLT_IDENTIFIER.ordinal()));
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
    return 31 * ((getCltId() == null) ? 0 : getCltId().hashCode());
  }

}
