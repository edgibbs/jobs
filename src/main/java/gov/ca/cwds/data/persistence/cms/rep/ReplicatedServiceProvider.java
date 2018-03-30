package gov.ca.cwds.data.persistence.cms.rep;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.NamedNativeQuery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.BaseServiceProvider;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;

/**
 * {@link PersistentObject} representing a Service Provider as a {@link CmsReplicatedEntity}.
 * 
 * @author CWDS API Team
 */
//@formatter:off
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedServiceProvider.findBucketRange",
    query = "SELECT z.IDENTIFIER, trim(z.AGENCY_NM) AGENCY_NM, trim(z.CITY_NM) CITY_NM, "
        + "z.FAX_NO, trim(z.FIRST_NM) FIRST_NM, trim(z.LAST_NM) LAST_NM, "
        + "trim(z.NMPRFX_DSC) as NMPRFX_DSC, z.PHONE_NO, z.TEL_EXT_NO, "
        + "trim(z.PSTITL_DSC) as PSTITL_DSC, z.SVCPVDRC, z.STATE_C, trim(z.STREET_NM) STREET_NM, "
        + "trim(z.STREET_NO) STREET_NO, trim(z.SUFX_TLDSC) SUFX_TLDSC, z.ZIP_NM, "
        + "z.LST_UPD_ID, z.LST_UPD_TS, z.ZIP_SFX_NO, z.ARCASS_IND, trim(z.EMAIL_ADDR) EMAIL_ADDR"
        + ", z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER FROM {h-schema}SVC_PVRT z "
        + "WHERE z.IDENTIFIER BETWEEN :min_id AND :max_id "
        + "FOR READ ONLY WITH UR",
    resultClass = ReplicatedServiceProvider.class, readOnly = true)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedServiceProvider.findAllUpdatedAfter",
    query = "SELECT z.IDENTIFIER, trim(z.AGENCY_NM) AGENCY_NM, trim(z.CITY_NM) CITY_NM, "
        + "z.FAX_NO, trim(z.FIRST_NM) FIRST_NM, trim(z.LAST_NM) LAST_NM, "
        + "trim(z.NMPRFX_DSC) as NMPRFX_DSC, z.PHONE_NO, z.TEL_EXT_NO, "
        + "trim(z.PSTITL_DSC) as PSTITL_DSC, z.SVCPVDRC, z.STATE_C, trim(z.STREET_NM) STREET_NM, "
        + "trim(z.STREET_NO) STREET_NO, trim(z.SUFX_TLDSC) SUFX_TLDSC, z.ZIP_NM, "
        + "z.LST_UPD_ID, z.LST_UPD_TS, z.ZIP_SFX_NO, z.ARCASS_IND, trim(z.EMAIL_ADDR) EMAIL_ADDR"
        + ", z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER "
        + "from {h-schema}SVC_PVRT z "
        + "WHERE z.IBMSNAP_LOGMARKER >= :after "
        + "FOR READ ONLY WITH UR ",
    resultClass = ReplicatedServiceProvider.class)
//@formatter:on
@Entity
@Table(name = "SVC_PVRT")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicatedServiceProvider extends BaseServiceProvider
    implements CmsReplicatedEntity, ApiGroupNormalizer<ReplicatedServiceProvider> {

  private static final long serialVersionUID = 1L;

  private EmbeddableCmsReplicatedEntity replicatedEntity = new EmbeddableCmsReplicatedEntity();

  // =======================
  // ApiGroupNormalizer:
  // =======================

  @SuppressWarnings("unchecked")
  @Override
  public Class<ReplicatedServiceProvider> getNormalizationClass() {
    return (Class<ReplicatedServiceProvider>) this.getClass();
  }

  @Override
  public ReplicatedServiceProvider normalize(Map<Object, ReplicatedServiceProvider> map) {
    return null;
  }

  @Override
  public String getNormalizationGroupKey() {
    return this.getId();
  }

  // =======================
  // ApiLegacyAware:
  // =======================

  @Override
  public String getLegacyId() {
    return getId();
  }

  @Override
  public ElasticSearchLegacyDescriptor getLegacyDescriptor() {
    return ElasticTransformer.createLegacyDescriptor(getId(), getLastUpdatedTime(),
        LegacyTable.SERVICE_PROVIDER);
  }

  @Override
  public EmbeddableCmsReplicatedEntity getReplicatedEntity() {
    return replicatedEntity;
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
