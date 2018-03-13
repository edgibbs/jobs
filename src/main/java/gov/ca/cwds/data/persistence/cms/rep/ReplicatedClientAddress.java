package gov.ca.cwds.data.persistence.cms.rep;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.BaseClientAddress;

/**
 * {@link PersistentObject} representing a Client Address in the replicated schema.
 * 
 * @author CWDS API Team
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "CL_ADDRT")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicatedClientAddress extends BaseClientAddress implements CmsReplicatedEntity {

  @OneToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "IDENTIFIER", referencedColumnName = "FKADDRS_T", insertable = false,
      updatable = false, unique = false)
  private Set<ReplicatedAddress> addresses = new LinkedHashSet<>();

  private EmbeddableCmsReplicatedEntity replicatedEntity = new EmbeddableCmsReplicatedEntity();

  /**
   * Default constructor
   * 
   * Required for Hibernate
   */
  public ReplicatedClientAddress() {
    super();
  }

  /**
   * Getter for addresses. Returns underlying member, not a deep copy.
   * 
   * @return Set of addresses
   */
  public Set<ReplicatedAddress> getAddresses() {
    return addresses;
  }

  /**
   * Setter for addresses.
   * 
   * @param addresses addresses to set
   */
  public void setAddresses(Set<ReplicatedAddress> addresses) {
    if (addresses != null) {
      this.addresses = addresses;
    } else {
      this.addresses = new LinkedHashSet<>();
    }
  }

  /**
   * Add an address.
   * 
   * @param address to add
   */
  public void addAddress(ReplicatedAddress address) {
    if (address != null) {
      addresses.add(address);
    }
  }

  /**
   * Is this client address record active?
   * 
   * <p>
   * Active = end date is not null OR end date exceeds current date/time.
   * </p>
   * 
   * @return true if active
   */
  public boolean isActive() {
    return getEffEndDt() == null || getEffEndDt().after(new Date());
  }

  @Override
  public String getLegacyId() {
    return this.getPrimaryKey();
  }

  @Override
  public ElasticSearchLegacyDescriptor getLegacyDescriptor() {
    return null;
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
