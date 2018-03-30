package gov.ca.cwds.data.persistence.cms;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.NamedNativeQuery;

import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.persistence.cms.rep.SimpleReplicatedClient;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.rocket.ClientSQLResource;
import gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils;

/**
 * Entity bean for view VW_LST_CLIENT_ADDRESS.
 * 
 * <p>
 * Implements {@link ApiGroupNormalizer} and converts to {@link ReplicatedClient}.
 * </p>
 * 
 * #145240149: find ALL client/address records affected by changes.
 *
 * REFRESH TABLE CWSRSQ.ES_REL_CLN_RELT_CLIENT ;
 * 
 * @author CWDS API Team
 */
@Entity
@Table(name = "VW_LST_CLIENT_ADDRESS")
//@formatter:off
@NamedNativeQuery(name = "gov.ca.cwds.data.persistence.cms.EsClientAddress.findAllUpdatedAfter",
    query = "SELECT " + ClientSQLResource.LAST_CHG_COLUMNS + "\n"
        + "FROM {h-schema}VW_LST_CLIENT_ADDRESS x \n"
        + "WHERE (1=1 OR x.LAST_CHG > :after) \n"
        + "ORDER BY CLT_IDENTIFIER, CLA_IDENTIFIER, ADR_IDENTIFIER \n"
        + "FOR READ ONLY WITH UR ",
    resultClass = EsClientAddress.class, readOnly = true)

@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.EsClientAddress.findAllUpdatedAfterWithUnlimitedAccess",
        query = "SELECT " + ClientSQLResource.LAST_CHG_COLUMNS + "\n"
        + "FROM {h-schema}VW_LST_CLIENT_ADDRESS x \n"
        + "WHERE (1=1 OR x.LAST_CHG > :after) \n"
        + "ORDER BY CLT_IDENTIFIER, CLA_IDENTIFIER, ADR_IDENTIFIER \n"
        + "FOR READ ONLY WITH UR",
    resultClass = EsClientAddress.class, readOnly = true)

@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.EsClientAddress.findAllUpdatedAfterWithLimitedAccess",
        query = "SELECT " + ClientSQLResource.LAST_CHG_COLUMNS + "\n"
        + "FROM {h-schema}VW_LST_CLIENT_ADDRESS x \n"
        + "WHERE (1=1 OR x.LAST_CHG > :after) \n"
        + "AND x.CLT_SENSTV_IND != 'N' \n"
        + "ORDER BY CLT_IDENTIFIER, CLA_IDENTIFIER, ADR_IDENTIFIER \n"
        + "FOR READ ONLY WITH UR ",
    resultClass = EsClientAddress.class, readOnly = true)
//@formatter:on
public class EsClientAddress extends BaseEsClient
    implements Comparable<EsClientAddress>, Comparator<EsClientAddress> {

  private static final long serialVersionUID = 1L;

  /**
   * Build an EsClientAddress from the incoming ResultSet.
   * 
   * @param rs incoming tuple
   * @return a populated EsClientAddress
   * @throws SQLException if unable to convert types or stream breaks, etc.
   */
  public static EsClientAddress extract(final ResultSet rs) throws SQLException {
    final EsClientAddress ret = new EsClientAddress();
    BaseEsClient.extract(ret, rs);
    return ret;
  }

  @Override
  protected ReplicatedClient makeReplicatedClient() {
    return new SimpleReplicatedClient();
  }

  @Override
  public Class<ReplicatedClient> getNormalizationClass() {
    return ReplicatedClient.class;
  }

  public Date getCltFatherParentalRightTermDate() {
    return NeutronDateUtils.freshDate(cltFatherParentalRightTermDate);
  }

  public void setClaId(String claId) {
    this.claId = claId;
  }

  public void setAdrReplicationOperation(CmsReplicationOperation adrReplicationOperation) {
    this.adrReplicationOperation = adrReplicationOperation;
  }

  @Override
  public String getNormalizationGroupKey() {
    return this.cltId;
  }

  /**
   * This view (i.e., materialized query table) doesn't have a proper unique key, but a combination
   * of several fields might come close.
   * <ul>
   * <li>"Cook": convert String parameter to strong type</li>
   * <li>"Uncook": convert strong type parameter to String</li>
   * </ul>
   */
  @Override
  public Serializable getPrimaryKey() {
    return null;
  }

  // =====================
  // IDENTITY:
  // =====================

  @Override
  public int compare(EsClientAddress o1, EsClientAddress o2) {
    int comp = o1.getCltId().compareTo(o2.getCltId());
    if (comp == 0 && o1.getClaId() != null && o2.getClaId() != null) {
      comp = o1.getClaId().compareTo(o2.getClaId());
    }
    if (comp == 0 && o1.getAdrId() != null && o2.getAdrId() != null) {
      comp = o1.getAdrId().compareTo(o2.getAdrId());
    }
    if (comp == 0 && o1.clientCountyId != null && o2.clientCountyId != null) {
      comp = o1.clientCountyId.compareTo(o2.clientCountyId);
    }
    if (comp == 0 && o1.clientEthnicityId != null && o2.clientEthnicityId != null) {
      comp = o1.clientEthnicityId.compareTo(o2.clientEthnicityId);
    }

    return comp;
  }

  @Override
  public int compareTo(EsClientAddress o) {
    return compare(this, o);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false);
  }

}
