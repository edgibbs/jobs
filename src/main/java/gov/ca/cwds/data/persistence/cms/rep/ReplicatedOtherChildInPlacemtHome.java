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
import gov.ca.cwds.data.persistence.cms.BaseOtherChildInPlacemtHome;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;

/**
 * {@link PersistentObject} representing an Other Child In Placement Home as a
 * {@link CmsReplicatedEntity}.
 * 
 * @author CWDS API Team
 */
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherChildInPlacemtHome.findBucketRange",
    query = "SELECT z.IDENTIFIER, z.BIRTH_DT, z.GENDER_CD, trim(z.OTHCHLD_NM) OTHCHLD_NM, "
        + "z.LST_UPD_ID, z.LST_UPD_TS, z.FKPLC_HM_T, z.YR_INC_AMT, z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER "
        + "FROM {h-schema}OTH_KIDT z WHERE z.IDENTIFIER BETWEEN :min_id AND :max_id "
        + "ORDER BY z.IDENTIFIER FOR READ ONLY WITH UR",
    resultClass = ReplicatedOtherChildInPlacemtHome.class, readOnly = true)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherChildInPlacemtHome.findAllUpdatedAfter",
    query = "SELECT z.IDENTIFIER, z.BIRTH_DT, z.GENDER_CD, trim(z.OTHCHLD_NM) OTHCHLD_NM, "
        + "z.LST_UPD_ID, z.LST_UPD_TS, z.FKPLC_HM_T, z.YR_INC_AMT, z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER "
        + "FROM {h-schema}OTH_KIDT z WHERE z.IBMSNAP_LOGMARKER >= :after FOR READ ONLY WITH UR ",
    resultClass = ReplicatedOtherChildInPlacemtHome.class)
@Entity
@Table(name = "OTH_KIDT")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicatedOtherChildInPlacemtHome extends BaseOtherChildInPlacemtHome
    implements CmsReplicatedEntity, ApiGroupNormalizer<ReplicatedOtherChildInPlacemtHome> {

  /**
   * Default.
   */
  private static final long serialVersionUID = 1L;

  private EmbeddableCmsReplicatedEntity replicatedEntity = new EmbeddableCmsReplicatedEntity();

  // =======================
  // ApiGroupNormalizer:
  // =======================

  @SuppressWarnings("unchecked")
  @Override
  public Class<ReplicatedOtherChildInPlacemtHome> getNormalizationClass() {
    return (Class<ReplicatedOtherChildInPlacemtHome>) this.getClass();
  }

  @Override
  public ReplicatedOtherChildInPlacemtHome normalize(
      Map<Object, ReplicatedOtherChildInPlacemtHome> map) {
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
        LegacyTable.CHILD_IN_PLACEMENT_HOME);
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
