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
import gov.ca.cwds.data.persistence.cms.BaseOtherAdultInPlacemtHome;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;

/**
 * {@link PersistentObject} representing an Other Adult In Placement Home as a
 * {@link CmsReplicatedEntity}.
 * 
 * @author CWDS API Team
 */
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome.findBucketRange",
    query = "SELECT z.IDENTIFIER, z.BIRTH_DT, z.END_DT, z.GENDER_CD, trim(z.OTH_ADLTNM) OTH_ADLTNM, "
        + "z.START_DT, z.LST_UPD_ID, z.LST_UPD_TS, z.FKPLC_HM_T, trim(z.COMNT_DSC) COMNT_DSC, "
        + "z.OTH_ADL_CD, z.IDENTFD_DT, z.RESOST_IND, z.PASSBC_CD "
        + ", z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER FROM {h-schema}OTH_ADLT z "
        + "WHERE z.IDENTIFIER < :min_id AND z.IDENTIFIER <= :max_id ORDER BY z.IDENTIFIER FOR READ ONLY WITH UR",
    resultClass = ReplicatedOtherAdultInPlacemtHome.class, readOnly = true)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome.findAllUpdatedAfter",
    query = "SELECT z.IDENTIFIER, z.BIRTH_DT, z.END_DT, z.GENDER_CD, trim(z.OTH_ADLTNM) OTH_ADLTNM, "
        + "z.START_DT, z.LST_UPD_ID, z.LST_UPD_TS, z.FKPLC_HM_T, trim(z.COMNT_DSC) COMNT_DSC, "
        + "z.OTH_ADL_CD, z.IDENTFD_DT, z.RESOST_IND, z.PASSBC_CD "
        + ", z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER "
        + "FROM {h-schema}OTH_ADLT z WHERE z.IBMSNAP_LOGMARKER >= :after FOR READ ONLY WITH UR ",
    resultClass = ReplicatedOtherAdultInPlacemtHome.class, readOnly = true)
@Entity
@Table(name = "OTH_ADLT")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicatedOtherAdultInPlacemtHome extends BaseOtherAdultInPlacemtHome
    implements CmsReplicatedEntity, ApiGroupNormalizer<ReplicatedOtherAdultInPlacemtHome> {

  private static final long serialVersionUID = 1L;

  private EmbeddableCmsReplicatedEntity replicatedEntity = new EmbeddableCmsReplicatedEntity();

  // =======================
  // ApiGroupNormalizer:
  // =======================

  @SuppressWarnings("unchecked")
  @Override
  public Class<ReplicatedOtherAdultInPlacemtHome> getNormalizationClass() {
    return (Class<ReplicatedOtherAdultInPlacemtHome>) this.getClass();
  }

  @Override
  public ReplicatedOtherAdultInPlacemtHome normalize(
      Map<Object, ReplicatedOtherAdultInPlacemtHome> map) {
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
        LegacyTable.ADULT_IN_PLACEMENT_HOME);
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
