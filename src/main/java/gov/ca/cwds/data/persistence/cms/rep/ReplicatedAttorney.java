package gov.ca.cwds.data.persistence.cms.rep;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.NamedNativeQuery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.BaseAttorney;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;

/**
 * {@link PersistentObject} representing an Attorney as a {@link CmsReplicatedEntity} in the
 * replicated schema.
 * 
 * @author CWDS API Team
 */
@NamedNativeQuery(name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedAttorney.findBucketRange",
    query = "SELECT x.* FROM {h-schema}ATTRNY_T x "
        + "WHERE x.IDENTIFIER BETWEEN :min_id AND :max_id FOR READ ONLY WITH UR",
    resultClass = ReplicatedAttorney.class, readOnly = true)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedAttorney.findAllUpdatedAfter",
    query = "select z.IDENTIFIER, z.CITY_NM, z.CWATRY_IND, z.FAX_NO, z.FIRST_NM, "
        + "z.GVR_ENTC, z.LANG_TPC, z.LAST_NM, z.MSG_EXT_NO, z.MSG_TEL_NO, "
        + "z.MID_INI_NM, z.NMPRFX_DSC, z.POSTIL_DSC, z.PRM_EXT_NO, z.PRM_TEL_NO, "
        + "z.STATE_C, z.STREET_NM, z.STREET_NO, z.SUFX_TLDSC, z.ZIP_NO, z.LST_UPD_ID, "
        + "z.LST_UPD_TS, z.BUSNSS_NM, z.ZIP_SFX_NO, z.END_DT, z.ARCASS_IND, z.EMAIL_ADDR "
        + ", z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER "
        + "from {h-schema}ATTRNY_T z WHERE z.IBMSNAP_LOGMARKER >= :after FOR READ ONLY WITH UR",
    resultClass = ReplicatedAttorney.class)
@NamedNativeQuery(name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedAttorney.findAllByBucket",
    query = "select z.IDENTIFIER, z.CITY_NM, z.CWATRY_IND, z.FAX_NO, z.FIRST_NM, "
        + "z.GVR_ENTC, z.LANG_TPC, z.LAST_NM, z.MSG_EXT_NO, z.MSG_TEL_NO, "
        + "z.MID_INI_NM, z.NMPRFX_DSC, z.POSTIL_DSC, z.PRM_EXT_NO, z.PRM_TEL_NO, "
        + "z.STATE_C, z.STREET_NM, z.STREET_NO, z.SUFX_TLDSC, z.ZIP_NO, z.LST_UPD_ID, "
        + "z.LST_UPD_TS, z.BUSNSS_NM, z.ZIP_SFX_NO, z.END_DT, z.ARCASS_IND, z.EMAIL_ADDR "
        + ", z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER "
        + "from ( select mod(y.rn, CAST(:total_buckets AS INTEGER)) + 1 as bucket, y.* "
        + "from ( select row_number() over (order by 1) as rn, x.* "
        + "from {h-schema}ATTRNY_T x ) y ) z where z.bucket = :bucket_num FOR READ ONLY WITH UR",
    resultClass = ReplicatedAttorney.class)
@Entity
@Table(name = "ATTRNY_T")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings({"squid:S2160", "serial"})
public class ReplicatedAttorney extends BaseAttorney
    implements CmsReplicatedEntity, ApiGroupNormalizer<ReplicatedAttorney> {

  /**
   * Generated version.
   */
  private static final long serialVersionUID = 6160989831851057517L;

  private EmbeddableCmsReplicatedEntity replicatedEntity = new EmbeddableCmsReplicatedEntity();

  // =======================
  // CmsReplicatedEntity:
  // =======================

  @Override
  public EmbeddableCmsReplicatedEntity getReplicatedEntity() {
    return replicatedEntity;
  }

  // =======================
  // ApiGroupNormalizer:
  // =======================

  @SuppressWarnings("unchecked")
  @Override
  public Class<ReplicatedAttorney> getNormalizationClass() {
    return (Class<ReplicatedAttorney>) this.getClass();
  }

  @Override
  public ReplicatedAttorney normalize(Map<Object, ReplicatedAttorney> map) {
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
        LegacyTable.ATTORNEY);
  }
}
