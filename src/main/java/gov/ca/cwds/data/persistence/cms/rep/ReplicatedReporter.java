package gov.ca.cwds.data.persistence.cms.rep;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.NamedNativeQuery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.BaseReporter;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;

/**
 * {@link PersistentObject} representing a Reporter as {@link CmsReplicatedEntity}.
 * 
 * @author CWDS API Team
 */
//@formatter:off
@NamedNativeQuery(name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedReporter.findBucketRange",
    query = "SELECT TRIM(z.RPTR_BDGNO) RPTR_BDGNO, TRIM(z.RPTR_CTYNM) RPTR_CTYNM, "
        + "z.COL_RELC, z.CMM_MTHC, z.CNFWVR_IND, z.FDBACK_DOC, TRIM(z.RPTR_EMPNM) RPTR_EMPNM, "
        + "z.FEEDBCK_DT, z.FB_RQR_IND, TRIM(z.RPTR_FSTNM) RPTR_FSTNM, TRIM(z.RPTR_LSTNM) RPTR_LSTNM, "
        + "z.MNRPTR_IND, z.MSG_EXT_NO, z.MSG_TEL_NO, TRIM(z.MID_INI_NM) MID_INI_NM, "
        + "TRIM(z.NMPRFX_DSC) NMPRFX_DSC, z.PRM_TEL_NO, z.PRM_EXT_NO, z.STATE_C, "
        + "TRIM(z.RPTR_ST_NM) RPTR_ST_NM, TRIM(z.RPTR_ST_NO) RPTR_ST_NO, "
        + "TRIM(z.SUFX_TLDSC) SUFX_TLDSC, z.RPTR_ZIPNO, z.LST_UPD_ID, z.LST_UPD_TS, "
        + "z.FKREFERL_T, z.FKLAW_ENFT, z.ZIP_SFX_NO, z.CNTY_SPFCD "
        + ", z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER FROM {h-schema}REPTR_T z "
        + "WHERE z.FKREFERL_T > :min_id AND z.FKREFERL_T <= :max_id FOR READ ONLY WITH UR",
    resultClass = ReplicatedReporter.class, readOnly = true)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedReporter.findAllUpdatedAfter",
    query = 
        "SELECT TRIM(z.RPTR_BDGNO) RPTR_BDGNO, TRIM(z.RPTR_CTYNM) RPTR_CTYNM, "
        + "z.COL_RELC, z.CMM_MTHC, z.CNFWVR_IND, z.FDBACK_DOC, TRIM(z.RPTR_EMPNM) RPTR_EMPNM, "
        + "z.FEEDBCK_DT, z.FB_RQR_IND, TRIM(z.RPTR_FSTNM) RPTR_FSTNM, TRIM(z.RPTR_LSTNM) RPTR_LSTNM, "
        + "z.MNRPTR_IND, z.MSG_EXT_NO, z.MSG_TEL_NO, TRIM(z.MID_INI_NM) MID_INI_NM, "
        + "TRIM(z.NMPRFX_DSC) NMPRFX_DSC, z.PRM_TEL_NO, z.PRM_EXT_NO, z.STATE_C, "
        + "TRIM(z.RPTR_ST_NM) RPTR_ST_NM, TRIM(z.RPTR_ST_NO) RPTR_ST_NO, "
        + "TRIM(z.SUFX_TLDSC) SUFX_TLDSC, z.RPTR_ZIPNO, z.LST_UPD_ID, z.LST_UPD_TS, "
        + "z.FKREFERL_T, z.FKLAW_ENFT, z.ZIP_SFX_NO, z.CNTY_SPFCD "
        + ", z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER \n" 
        + "FROM {h-schema}GT_ID gt \n"
        + "JOIN {h-schema}REPTR_T z ON z.FKREFERL_T = gt.IDENTIFIER \n"
        + "WHERE (1=1 or z.IBMSNAP_LOGMARKER > :after) \n"
        + "FOR READ ONLY WITH UR ",
    resultClass = ReplicatedReporter.class)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedReporter.findAllUpdatedAfterWithUnlimitedAccess",
    query = 
        "SELECT TRIM(z.RPTR_BDGNO) RPTR_BDGNO, TRIM(z.RPTR_CTYNM) RPTR_CTYNM, "
        + "z.COL_RELC, z.CMM_MTHC, z.CNFWVR_IND, z.FDBACK_DOC, TRIM(z.RPTR_EMPNM) RPTR_EMPNM, "
        + "z.FEEDBCK_DT, z.FB_RQR_IND, TRIM(z.RPTR_FSTNM) RPTR_FSTNM, TRIM(z.RPTR_LSTNM) RPTR_LSTNM, "
        + "z.MNRPTR_IND, z.MSG_EXT_NO, z.MSG_TEL_NO, TRIM(z.MID_INI_NM) MID_INI_NM, "
        + "TRIM(z.NMPRFX_DSC) NMPRFX_DSC, z.PRM_TEL_NO, z.PRM_EXT_NO, z.STATE_C, "
        + "TRIM(z.RPTR_ST_NM) RPTR_ST_NM, TRIM(z.RPTR_ST_NO) RPTR_ST_NO, "
        + "TRIM(z.SUFX_TLDSC) SUFX_TLDSC, z.RPTR_ZIPNO, z.LST_UPD_ID, z.LST_UPD_TS, "
        + "z.FKREFERL_T, z.FKLAW_ENFT, z.ZIP_SFX_NO, z.CNTY_SPFCD "
        + ", z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER \n" 
        + "FROM {h-schema}GT_ID gt \n"
        + "JOIN {h-schema}REPTR_T z ON z.FKREFERL_T = gt.IDENTIFIER \n"
        + "WHERE (1=1 or z.IBMSNAP_LOGMARKER > :after) \n"
        + "FOR READ ONLY WITH UR ",
    resultClass = ReplicatedReporter.class)
//@formatter:on
@Entity
@Table(name = "REPTR_T")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings({"squid:S2160", "serial"})
public class ReplicatedReporter extends BaseReporter implements CmsReplicatedEntity,
    ApiGroupNormalizer<ReplicatedReporter>, EmbeddableCmsReplicatedEntityAware {

  private static final long serialVersionUID = 1L;

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
  public Class<ReplicatedReporter> getNormalizationClass() {
    return (Class<ReplicatedReporter>) this.getClass();
  }

  @Override
  public ReplicatedReporter normalize(Map<Object, ReplicatedReporter> map) {
    return null;
  }

  @Override
  public String getNormalizationGroupKey() {
    return this.getPrimaryKey();
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
    return getReferralId();
  }

  @Override
  public ElasticSearchLegacyDescriptor getLegacyDescriptor() {
    return ElasticTransformer.createLegacyDescriptor(getId(), getLastUpdatedTime(),
        LegacyTable.REPORTER);
  }
}
