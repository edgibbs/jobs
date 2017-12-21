package gov.ca.cwds.generic.data.persistence.cms.rep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.BaseEducationProviderContact;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.generic.jobs.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;
import java.util.Date;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.annotations.Type;

/**
 * {@link PersistentObject} representing an Education Provider Contact as a
 * {@link CmsReplicatedEntity}.
 *
 * @author CWDS API Team
 */
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedEducationProviderContact.findBucketRange",
    query = "SELECT x.* FROM {h-schema}EDPRVCNT x "
        + "WHERE x.IDENTIFIER BETWEEN :min_id AND :max_id ORDER BY IDENTIFIER FOR READ ONLY WITH UR",
    resultClass = ReplicatedEducationProviderContact.class, readOnly = true)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedEducationProviderContact.findAllUpdatedAfter",
    query = "SELECT z.IDENTIFIER, z.PRICNTIND, z.PH_NUMBR, z.PH_EXTNO, z.FAX_NO, "
        + "z.FIRST_NME, z.MIDDLE_NM, z.LAST_NME, z.NM_PREFIX, z.SUFFX_TITL, "
        + "z.TITLDESC, z.EMAILADR, z.DOE_IND, z.LST_UPD_ID, z.LST_UPD_TS, z.FKED_PVDRT, "
        + "z.IBMSNAP_COMMITSEQ, z.IBMSNAP_INTENTSEQ, z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER "
        + "from {h-schema}EDPRVCNT z WHERE z.IBMSNAP_LOGMARKER >= :after FOR READ ONLY WITH UR ",
    resultClass = ReplicatedEducationProviderContact.class, readOnly = true)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedEducationProviderContact.findAllByBucket",
    query = "SELECT z.IDENTIFIER, z.PRICNTIND, z.PH_NUMBR, z.PH_EXTNO, z.FAX_NO, "
        + "z.FIRST_NME, z.MIDDLE_NM, z.LAST_NME, z.NM_PREFIX, z.SUFFX_TITL, "
        + "z.TITLDESC, z.EMAILADR, z.DOE_IND, z.LST_UPD_ID, z.LST_UPD_TS, z.FKED_PVDRT, "
        + "z.IBMSNAP_COMMITSEQ, z.IBMSNAP_INTENTSEQ, z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER "
        + "from ( select mod(y.rn, CAST(:total_buckets AS INTEGER)) + 1 as bucket, y.* "
        + "from ( select row_number() over (order by 1) as rn, x.* "
        + "from {h-schema}EDPRVCNT x ) y ) z where z.bucket = :bucket_num FOR READ ONLY WITH UR",
    resultClass = ReplicatedEducationProviderContact.class, readOnly = true)
@Entity
@Table(name = "EDPRVCNT")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicatedEducationProviderContact extends BaseEducationProviderContact
    implements CmsReplicatedEntity, ApiGroupNormalizer<ReplicatedEducationProviderContact> {

  /**
   * Default.
   */
  private static final long serialVersionUID = 1L;

  @Enumerated(EnumType.STRING)
  @Column(name = "IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation replicationOperation;

  @Type(type = "timestamp")
  @Column(name = "IBMSNAP_LOGMARKER", updatable = false)
  private Date replicationDate;

  // =======================
  // CmsReplicatedEntity:
  // =======================

  @Override
  public CmsReplicationOperation getReplicationOperation() {
    return replicationOperation;
  }

  @Override
  public void setReplicationOperation(CmsReplicationOperation replicationOperation) {
    this.replicationOperation = replicationOperation;
  }

  @Override
  public Date getReplicationDate() {
    return replicationDate;
  }

  @Override
  public void setReplicationDate(Date replicationDate) {
    this.replicationDate = replicationDate;
  }

  // =======================
  // ApiGroupNormalizer:
  // =======================

  @SuppressWarnings("unchecked")
  @Override
  public Class<ReplicatedEducationProviderContact> getNormalizationClass() {
    return (Class<ReplicatedEducationProviderContact>) this.getClass();
  }

  @Override
  public ReplicatedEducationProviderContact normalize(
      Map<Object, ReplicatedEducationProviderContact> map) {
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
        LegacyTable.EDUCATION_PROVIDER);
  }
}
