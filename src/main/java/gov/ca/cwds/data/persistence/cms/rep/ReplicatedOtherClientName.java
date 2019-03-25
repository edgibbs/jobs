package gov.ca.cwds.data.persistence.cms.rep;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NamedNativeQuery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;
import gov.ca.cwds.data.es.ElasticSearchPersonAka;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.BaseOtherClientName;
import gov.ca.cwds.data.persistence.cms.ReplicatedAkas;
import gov.ca.cwds.data.persistence.cms.client.NeutronJdbcReader;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;

/**
 * {@link PersistentObject} representing an Other Client Name as a {@link CmsReplicatedEntity}.
 * 
 * @author CWDS API Team
 */
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherClientName.findAllUpdatedAfter",
    query = "SELECT r.* FROM {h-schema}VW_LST_OTHER_CLIENT_NAME r WHERE r.THIRD_ID IN ( "
        + "SELECT r1.THIRD_ID FROM {h-schema}VW_LST_OTHER_CLIENT_NAME r1 "
        + "WHERE r1.LAST_CHG > :after " + ") ORDER BY FKCLIENT_T FOR READ ONLY WITH UR ",
    resultClass = ReplicatedOtherClientName.class)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherClientName.findAllUpdatedAfterWithUnlimitedAccess",
    query = "SELECT r.* FROM {h-schema}VW_LST_OTHER_CLIENT_NAME r WHERE r.THIRD_ID IN ( "
        + "SELECT r1.THIRD_ID FROM {h-schema}VW_LST_OTHER_CLIENT_NAME r1 "
        + "WHERE r1.LAST_CHG > :after "
        + ") AND r.CLIENT_SENSITIVITY_IND = 'N' ORDER BY FKCLIENT_T FOR READ ONLY WITH UR ",
    resultClass = ReplicatedOtherClientName.class)
@Entity
@Table(name = "VW_LST_OTHER_CLIENT_NAME")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings({"squid:S2160", "serial"})
public class ReplicatedOtherClientName extends BaseOtherClientName implements CmsReplicatedEntity,
    ApiGroupNormalizer<ReplicatedAkas>, NeutronJdbcReader<ReplicatedOtherClientName> {

  /**
   * Default serialization.
   */
  private static final long serialVersionUID = 1L;

  @Column(name = "CLIENT_SENSITIVITY_IND", updatable = false)
  private String clientSensitivityIndicator;

  private EmbeddableCmsReplicatedEntity replicatedEntity = new EmbeddableCmsReplicatedEntity();

  /**
   * Build a ReplicatedOtherClientName from an incoming ResultSet.
   * 
   * @param rs incoming tuple
   * @return a populated EsRelationship
   * @throws SQLException if unable to convert types or stream breaks, etc.
   */
  public static ReplicatedOtherClientName mapRowToBean(ResultSet rs) throws SQLException {
    ReplicatedOtherClientName ret = new ReplicatedOtherClientName();

    ret.setClientId(rs.getString("FKCLIENT_T"));
    ret.setThirdId(rs.getString("THIRD_ID"));

    ret.firstName = ifNull(rs.getString("FIRST_NM"));
    ret.middleName = ifNull(rs.getString("MIDDLE_NM"));
    ret.lastName = ifNull(rs.getString("LAST_NM"));
    ret.nameType = rs.getShort("NAME_TPC");
    ret.namePrefixDescription = ifNull(rs.getString("NMPRFX_DSC"));
    ret.suffixTitleDescription = ifNull(rs.getString("SUFX_TLDSC"));

    ret.setLastUpdatedId(rs.getString("LST_UPD_ID"));
    ret.setLastUpdatedTime(rs.getDate("LST_UPD_TS"));

    return ret;
  }

  // =======================
  // ApiGroupNormalizer:
  // =======================

  @Override
  public Class<ReplicatedAkas> getNormalizationClass() {
    return ReplicatedAkas.class;
  }

  @Override
  public Serializable getNormalizationGroupKey() {
    return this.getPrimaryKey();
  }

  @Override
  public ReplicatedAkas normalize(Map<Object, ReplicatedAkas> map) {
    final boolean isClientAdded = map.containsKey(this.clientId);
    final ReplicatedAkas ret =
        isClientAdded ? map.get(this.clientId) : new ReplicatedAkas(this.clientId);

    final gov.ca.cwds.data.es.ElasticSearchPersonAka aka = new ElasticSearchPersonAka();
    ret.addAka(aka);

    if (StringUtils.isNotBlank(this.firstName)) {
      aka.setFirstName(this.firstName.trim());
    }

    if (StringUtils.isNotBlank(this.lastName)) {
      aka.setLastName(this.lastName.trim());
    }

    if (StringUtils.isNotBlank(this.middleName)) {
      aka.setMiddleName(this.middleName.trim());
    }

    if (StringUtils.isNotBlank(this.namePrefixDescription)) {
      aka.setPrefix(this.namePrefixDescription.trim());
    }

    if (StringUtils.isNotBlank(this.suffixTitleDescription)) {
      aka.setSuffix(this.suffixTitleDescription.trim());
    }

    if (this.nameType != null && this.nameType.intValue() != 0) {
      aka.setNameType(SystemCodeCache.global().getSystemCodeShortDescription(this.nameType));
    }

    aka.setLegacyDescriptor(getLegacyDescriptor());

    map.put(ret.getId(), ret);
    return ret;
  }

  // =======================
  // ApiLegacyAware:
  // =======================

  @Override
  public String getLegacyId() {
    return getId();
  }

  @Override
  public String getId() {
    return getThirdId();
  }

  @Override
  public ElasticSearchLegacyDescriptor getLegacyDescriptor() {
    return ElasticTransformer.createLegacyDescriptor(getId(), getLastUpdatedTime(),
        LegacyTable.ALIAS_OR_OTHER_CLIENT_NAME);
  }

  // =======================
  // SETTERS:
  // =======================

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  public void setNamePrefixDescription(String namePrefixDescription) {
    this.namePrefixDescription = namePrefixDescription;
  }

  public void setNameType(Short nameType) {
    this.nameType = nameType;
  }

  public void setSuffixTitleDescription(String suffixTitleDescription) {
    this.suffixTitleDescription = suffixTitleDescription;
  }

  public String getClientSensitivityIndicator() {
    return clientSensitivityIndicator;
  }

  public void setClientSensitivityIndicator(String clientSensitivityIndicator) {
    this.clientSensitivityIndicator = clientSensitivityIndicator;
  }

  @Override
  public EmbeddableCmsReplicatedEntity getReplicatedEntity() {
    return replicatedEntity;
  }

  @Override
  public ReplicatedOtherClientName read(ResultSet rs) throws SQLException {
    return ReplicatedOtherClientName.mapRowToBean(rs);
  }

}
