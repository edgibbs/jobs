package gov.ca.cwds.data.persistence.cms.rep;

import java.util.Date;
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
import gov.ca.cwds.data.persistence.cms.BaseClientRelationship;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;

/**
 * {@link PersistentObject} representing an Attorney as a {@link CmsReplicatedEntity} in the
 * replicated schema.
 * 
 * @author CWDS API Team
 */
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedClientRelationship.findBucketRange",
    query = "SELECT x.* FROM {h-schema}CLN_RELT x "
        + "WHERE x.IDENTIFIER BETWEEN :min_id AND :max_id FOR READ ONLY WITH UR",
    resultClass = ReplicatedClientRelationship.class, readOnly = true)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedClientRelationship.findAllUpdatedAfter",
    query = "SELECT z.ABSENT_CD, z.CLNTRELC, z.END_DT, z.FKCLIENT_0, z.FKCLIENT_T, z.IDENTIFIER, z.SAME_HM_CD, z.START_DT "
        + ", z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER "
        + "from {h-schema}CLN_RELT z WHERE z.IBMSNAP_LOGMARKER >= :after FOR READ ONLY WITH UR",
    resultClass = ReplicatedClientRelationship.class)
@Entity
@Table(name = "CLN_RELT")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicatedClientRelationship extends BaseClientRelationship
    implements CmsReplicatedEntity, ApiGroupNormalizer<ReplicatedClientRelationship> {

  /**
   * Generated version.
   */
  private static final long serialVersionUID = 1L;

  private EmbeddableCmsReplicatedEntity replicatedEntity = new EmbeddableCmsReplicatedEntity();

  public ReplicatedClientRelationship() {
    super();
  }

  public ReplicatedClientRelationship(String absentParentCode, Short clientRelationshipType,
      Date endDate, String secondaryClientId, String primaryClientId, String id,
      String sameHomeCode, Date startDate) {
    super(absentParentCode, clientRelationshipType, endDate, secondaryClientId, primaryClientId, id,
        sameHomeCode, startDate);
  }

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
  public Class<ReplicatedClientRelationship> getNormalizationClass() {
    return (Class<ReplicatedClientRelationship>) this.getClass();
  }

  @Override
  public ReplicatedClientRelationship normalize(Map<Object, ReplicatedClientRelationship> map) {
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
        LegacyTable.CLIENT_RELATIONSHIP);
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
