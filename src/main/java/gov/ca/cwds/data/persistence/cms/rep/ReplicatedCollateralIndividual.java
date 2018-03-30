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
import gov.ca.cwds.data.persistence.cms.BaseCollateralIndividual;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;

/**
 * {@link PersistentObject} representing a Collateral Individual as a {@link CmsReplicatedEntity}.
 * 
 * @author CWDS API Team
 */
//@formatter:off
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedCollateralIndividual.findBucketRange",
    query = "SELECT z.IDENTIFIER, TRIM(z.BADGE_NO) BADGE_NO, TRIM(z.CITY_NM) CITY_NM, TRIM(z.EMPLYR_NM) EMPLYR_NM, "
        + "z.FAX_NO, TRIM(z.FIRST_NM) FIRST_NM, z.FRG_ADRT_B, TRIM(z.LAST_NM) LAST_NM, "
        + "TRIM(z.MID_INI_NM) MID_INI_NM, TRIM(z.NMPRFX_DSC) NMPRFX_DSC, z.PRM_TEL_NO, z.PRM_EXT_NO, z.STATE_C, "
        + "TRIM(z.STREET_NM) STREET_NM, TRIM(z.STREET_NO) STREET_NO, TRIM(z.SUFX_TLDSC) SUFX_TLDSC, "
        + "z.ZIP_NO, z.LST_UPD_ID, z.LST_UPD_TS, z.ZIP_SFX_NO, TRIM(z.COMNT_DSC) COMNT_DSC, "
        + "z.GENDER_CD, z.BIRTH_DT, z.MRTL_STC, TRIM(z.EMAIL_ADDR) EMAIL_ADDR, "
        + "z.ESTBLSH_CD, z.ESTBLSH_ID, z.RESOST_IND, "
        + "IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER "
        + "FROM {h-schema}COLTRL_T z "
        + "WHERE z.IDENTIFIER >= :min_id AND z.IDENTIFIER <= :max_id "
        + "FOR READ ONLY WITH UR",
    resultClass = ReplicatedCollateralIndividual.class, readOnly = true)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedCollateralIndividual.findAllUpdatedAfter",
    query = "SELECT z.IDENTIFIER, TRIM(z.BADGE_NO) BADGE_NO, TRIM(z.CITY_NM) CITY_NM, TRIM(z.EMPLYR_NM) EMPLYR_NM, "
        + "z.FAX_NO, TRIM(z.FIRST_NM) FIRST_NM, z.FRG_ADRT_B, TRIM(z.LAST_NM) LAST_NM, "
        + "TRIM(z.MID_INI_NM) MID_INI_NM, TRIM(z.NMPRFX_DSC) NMPRFX_DSC, z.PRM_TEL_NO, z.PRM_EXT_NO, z.STATE_C, "
        + "TRIM(z.STREET_NM) STREET_NM, TRIM(z.STREET_NO) STREET_NO, TRIM(z.SUFX_TLDSC) SUFX_TLDSC, "
        + "z.ZIP_NO, z.LST_UPD_ID, z.LST_UPD_TS, z.ZIP_SFX_NO, TRIM(z.COMNT_DSC) COMNT_DSC, "
        + "z.GENDER_CD, z.BIRTH_DT, z.MRTL_STC, TRIM(z.EMAIL_ADDR) EMAIL_ADDR, "
        + "z.ESTBLSH_CD, z.ESTBLSH_ID, z.RESOST_IND, "
        + "IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER \n"
        + "FROM {h-schema}GT_ID gt \n"
        + "JOIN {h-schema}COLTRL_T z ON z.IDENTIFIER = gt.IDENTIFIER \n"
        + "WHERE (1=1 OR z.IBMSNAP_LOGMARKER >= :after) \n"
        + "FOR READ ONLY WITH UR",
    resultClass = ReplicatedCollateralIndividual.class)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedCollateralIndividual.findAllUpdatedAfterWithUnlimitedAccess",
    query = "SELECT z.IDENTIFIER, TRIM(z.BADGE_NO) BADGE_NO, TRIM(z.CITY_NM) CITY_NM, TRIM(z.EMPLYR_NM) EMPLYR_NM, "
        + "z.FAX_NO, TRIM(z.FIRST_NM) FIRST_NM, z.FRG_ADRT_B, TRIM(z.LAST_NM) LAST_NM, "
        + "TRIM(z.MID_INI_NM) MID_INI_NM, TRIM(z.NMPRFX_DSC) NMPRFX_DSC, z.PRM_TEL_NO, z.PRM_EXT_NO, z.STATE_C, "
        + "TRIM(z.STREET_NM) STREET_NM, TRIM(z.STREET_NO) STREET_NO, TRIM(z.SUFX_TLDSC) SUFX_TLDSC, "
        + "z.ZIP_NO, z.LST_UPD_ID, z.LST_UPD_TS, z.ZIP_SFX_NO, TRIM(z.COMNT_DSC) COMNT_DSC, "
        + "z.GENDER_CD, z.BIRTH_DT, z.MRTL_STC, TRIM(z.EMAIL_ADDR) EMAIL_ADDR, "
        + "z.ESTBLSH_CD, z.ESTBLSH_ID, z.RESOST_IND, "
        + "IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER \n"
        + "FROM {h-schema}GT_ID gt \n"
        + "JOIN {h-schema}COLTRL_T z ON z.IDENTIFIER = gt.IDENTIFIER \n"
        + "WHERE (1=1 OR z.IBMSNAP_LOGMARKER >= :after) \n"
        + "FOR READ ONLY WITH UR",
    resultClass = ReplicatedCollateralIndividual.class)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedCollateralIndividual.findPartitionedBuckets",
    query = "SELECT z.IDENTIFIER, TRIM(z.BADGE_NO) BADGE_NO, TRIM(z.CITY_NM) CITY_NM, TRIM(z.EMPLYR_NM) EMPLYR_NM, "
        + "z.FAX_NO, TRIM(z.FIRST_NM) FIRST_NM, z.FRG_ADRT_B, TRIM(z.LAST_NM) LAST_NM, "
        + "TRIM(z.MID_INI_NM) MID_INI_NM, TRIM(z.NMPRFX_DSC) NMPRFX_DSC, z.PRM_TEL_NO, z.PRM_EXT_NO, z.STATE_C, "
        + "TRIM(z.STREET_NM) STREET_NM, TRIM(z.STREET_NO) STREET_NO, TRIM(z.SUFX_TLDSC) SUFX_TLDSC, "
        + "z.ZIP_NO, z.LST_UPD_ID, z.LST_UPD_TS, z.ZIP_SFX_NO, TRIM(z.COMNT_DSC) COMNT_DSC, "
        + "z.GENDER_CD, z.BIRTH_DT, z.MRTL_STC, TRIM(z.EMAIL_ADDR) EMAIL_ADDR, "
        + "z.ESTBLSH_CD, z.ESTBLSH_ID, z.RESOST_IND, "
        + "IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER "
        + "FROM {h-schema}COLTRL_T z "
        + "WHERE z.IDENTIFIER >= :min_id AND z.IDENTIFIER <= :max_id "
        + "FOR READ ONLY WITH UR",
    resultClass = ReplicatedCollateralIndividual.class)
//@formatter:on
@Entity
@Table(name = "COLTRL_T")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicatedCollateralIndividual extends BaseCollateralIndividual
    implements CmsReplicatedEntity, ApiGroupNormalizer<ReplicatedCollateralIndividual> {

  private static final long serialVersionUID = 1L;

  private EmbeddableCmsReplicatedEntity replicatedEntity = new EmbeddableCmsReplicatedEntity();

  // =======================
  // ApiGroupNormalizer:
  // =======================

  @SuppressWarnings("unchecked")
  @Override
  public Class<ReplicatedCollateralIndividual> getNormalizationClass() {
    return (Class<ReplicatedCollateralIndividual>) this.getClass();
  }

  @Override
  public ReplicatedCollateralIndividual normalize(Map<Object, ReplicatedCollateralIndividual> map) {
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
        LegacyTable.COLLATERAL_INDIVIDUAL);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false);
  }

  @Override
  public EmbeddableCmsReplicatedEntity getReplicatedEntity() {
    return replicatedEntity;
  }

}
