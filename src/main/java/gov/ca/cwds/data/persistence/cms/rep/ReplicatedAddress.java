package gov.ca.cwds.data.persistence.cms.rep;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;
import gov.ca.cwds.data.persistence.cms.BaseAddress;
import gov.ca.cwds.data.persistence.cms.CmsPersistentObject;
import gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;

/**
 * {@link CmsPersistentObject} representing an Address in the replicated schema.
 * 
 * @author CWDS API Team
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "ADDRS_T")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicatedAddress extends BaseAddress implements CmsReplicatedEntity {

  private EmbeddableCmsReplicatedEntity replicatedEntity = new EmbeddableCmsReplicatedEntity();

  public ReplicatedAddress() {
    super();
  }

  @Override
  public EmbeddableCmsReplicatedEntity getReplicatedEntity() {
    return replicatedEntity;
  }

  @Override
  public String getLegacyId() {
    return getId();
  }

  @Override
  public ElasticSearchLegacyDescriptor getLegacyDescriptor() {
    return ElasticTransformer.createLegacyDescriptor(getId(), getLastUpdatedTime(),
        LegacyTable.ADDRESS);
  }

  @Override
  public CmsReplicationOperation getReplicationOperation() {
    return replicatedEntity.getReplicationOperation();
  }

  @Override
  public Date getReplicationDate() {
    return NeutronDateUtils.freshDate(replicatedEntity.getReplicationDate());
  }

  @Override
  public void setReplicationOperation(CmsReplicationOperation replicationOperation) {
    this.replicatedEntity.setReplicationOperation(replicationOperation);
  }

  @Override
  public void setReplicationDate(Date replicationDate) {
    this.replicatedEntity.setReplicationDate(NeutronDateUtils.freshDate(replicationDate));
  }

  // @Override
  // public int hashCode() {
  // return HashCodeBuilder.reflectionHashCode(this, false);
  // }
  //
  // @Override
  // public boolean equals(Object obj) {
  // return EqualsBuilder.reflectionEquals(this, obj, false);
  // }

}
