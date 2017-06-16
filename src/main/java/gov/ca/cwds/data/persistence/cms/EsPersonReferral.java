package gov.ca.cwds.data.persistence.cms;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.NamedNativeQueries;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.annotations.Type;

import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPerson.ElasticSearchAccessLimitation;
import gov.ca.cwds.data.es.ElasticSearchPerson.ElasticSearchPersonAllegation;
import gov.ca.cwds.data.es.ElasticSearchPerson.ElasticSearchPersonReferral;
import gov.ca.cwds.data.es.ElasticSearchPerson.ElasticSearchPersonReporter;
import gov.ca.cwds.data.es.ElasticSearchPerson.ElasticSearchPersonSocialWorker;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.rest.api.domain.DomainChef;

/**
 * Entity bean for Materialized Query Table (MQT), ES_REFERRAL_HIST.
 * 
 * <p>
 * Implements {@link ApiGroupNormalizer} and converts to {@link ReplicatedPersonReferrals}.
 * </p>
 * 
 * @author CWDS API Team
 */
@Entity
@Table(name = "ES_REFERRAL_HIST")
@NamedNativeQueries({@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.EsPersonReferral.findAllUpdatedAfter",
    query = "SELECT r.* FROM {h-schema}ES_REFERRAL_HIST r WHERE r.CLIENT_ID IN ( "
        + "SELECT r1.CLIENT_ID FROM {h-schema}ES_REFERRAL_HIST r1 "
        + "WHERE r1.LAST_CHG > CAST(:after AS TIMESTAMP) " + ") ORDER BY CLIENT_ID FOR READ ONLY ",
    resultClass = EsPersonReferral.class, readOnly = true)})
public class EsPersonReferral
    implements PersistentObject, ApiGroupNormalizer<ReplicatedPersonReferrals> {

  private static final long serialVersionUID = -2265057057202257108L;

  private static final Logger LOGGER = LogManager.getLogger(EsPersonReferral.class);

  @Type(type = "timestamp")
  @Column(name = "LAST_CHG", updatable = false)
  private Date lastChange;

  // ================
  // KEYS:
  // ================

  @Id
  @Column(name = "CLIENT_ID")
  private String clientId;

  @Id
  @Column(name = "REFERRAL_ID")
  private String referralId;

  // ================
  // REFERRAL:
  // ================

  @Column(name = "START_DATE")
  @Type(type = "date")
  private Date startDate;

  @Column(name = "END_DATE")
  @Type(type = "date")
  private Date endDate;

  @Column(name = "REFERRAL_RESPONSE_TYPE")
  @Type(type = "integer")
  private Integer referralResponseType;

  @Column(name = "REFERRAL_COUNTY")
  @Type(type = "integer")
  private Integer county;

  // ==============
  // REPORTER:
  // ==============

  @Column(name = "REPORTER_ID")
  private String reporterId;

  @Column(name = "REPORTER_FIRST_NM")
  private String reporterFirstName;

  @Column(name = "REPORTER_LAST_NM")
  private String reporterLastName;

  // ==============
  // SOCIAL WORKER:
  // ==============

  @Column(name = "WORKER_ID")
  private String workerId;

  @Column(name = "WORKER_FIRST_NM")
  private String workerFirstName;

  @Column(name = "WORKER_LAST_NM")
  private String workerLastName;

  // =============
  // ALLEGATION:
  // =============

  @Column(name = "ALLEGATION_ID")
  private String allegationId;

  @Column(name = "ALLEGATION_DISPOSITION")
  @Type(type = "integer")
  private Integer allegationDisposition;

  @Column(name = "ALLEGATION_TYPE")
  @Type(type = "integer")
  private Integer allegationType;

  // =============
  // VICTIM:
  // =============

  @Column(name = "VICTIM_ID")
  private String victimId;

  @Column(name = "VICTIM_FIRST_NM")
  private String victimFirstName;

  @Column(name = "VICTIM_LAST_NM")
  private String victimLastName;

  // ==================
  // ACCESS LIMITATION:
  // ==================

  @Column(name = "LIMITED_ACCESS_CODE")
  private String limitedAccessCode;

  @Column(name = "LIMITED_ACCESS_DATE")
  @Type(type = "date")
  private Date limitedAccessDate;

  @Column(name = "LIMITED_ACCESS_DESCRIPTION")
  private String limitedAccessDescription;

  @Column(name = "LIMITED_ACCESS_GOVERNMENT_ENTITY")
  @Type(type = "integer")
  private Integer limitedAccessGovernmentEntityId;

  // =============
  // PERPETRATOR:
  // =============

  @Column(name = "PERPETRATOR_ID")
  private String perpetratorId;

  @Column(name = "PERPETRATOR_FIRST_NM")
  private String perpetratorFirstName;

  @Column(name = "PERPETRATOR_LAST_NM")
  private String perpetratorLastName;

  // =============
  // REDUCE:
  // =============

  @Override
  public Class<ReplicatedPersonReferrals> getNormalizationClass() {
    return ReplicatedPersonReferrals.class;
  }

  @Override
  public ReplicatedPersonReferrals normalize(Map<Object, ReplicatedPersonReferrals> map) {
    ReplicatedPersonReferrals referrals = map.get(this.clientId);
    if (referrals == null) {
      referrals = new ReplicatedPersonReferrals(this.clientId);
      map.put(this.clientId, referrals);
    }

    ElasticSearchPersonReferral referral = new ElasticSearchPersonReferral();

    referral.setId(this.referralId);
    referral.setLegacyId(this.referralId);
    referral.setStartDate(DomainChef.cookDate(this.startDate));
    referral.setEndDate(DomainChef.cookDate(this.endDate));
    referral.setCountyId(String.valueOf(this.county));
    referral
        .setCountyName(ElasticSearchPerson.getSystemCodes().getCodeShortDescription(this.county));
    referral.setResponseTimeId(String.valueOf(this.referralResponseType));
    referral.setResponseTime(
        ElasticSearchPerson.getSystemCodes().getCodeShortDescription(this.referralResponseType));
    referral.setLegacyLastUpdated(DomainChef.cookStrictTimestamp(this.lastChange));

    //
    // Reporter
    //
    ElasticSearchPersonReporter reporter = new ElasticSearchPersonReporter();
    reporter.setId(this.reporterId);
    reporter.setLegacyClientId(this.reporterId);
    reporter.setFirstName(this.reporterFirstName);
    reporter.setLastName(this.reporterLastName);
    referral.setReporter(reporter);

    //
    // Assigned Worker
    //
    ElasticSearchPersonSocialWorker assignedWorker = new ElasticSearchPersonSocialWorker();
    assignedWorker.setId(this.workerId);
    assignedWorker.setLegacyClientId(this.workerId);
    assignedWorker.setFirstName(this.workerFirstName);
    assignedWorker.setLastName(this.workerLastName);
    referral.setAssignedSocialWorker(assignedWorker);

    //
    // Access Limitation
    //
    ElasticSearchAccessLimitation accessLimit = new ElasticSearchAccessLimitation();
    accessLimit.setLimitedAccessCode(this.limitedAccessCode);
    accessLimit.setLimitedAccessDate(DomainChef.cookDate(this.limitedAccessDate));
    accessLimit.setLimitedAccessDescription(this.limitedAccessDescription);
    accessLimit
        .setLimitedAccessGovernmentEntityId(String.valueOf(this.limitedAccessGovernmentEntityId));
    accessLimit.setLimitedAccessGovernmentEntityName(ElasticSearchPerson.getSystemCodes()
        .getCodeShortDescription(this.limitedAccessGovernmentEntityId));
    referral.setAccessLimitation(accessLimit);

    //
    // A referral may have more than one allegations
    //
    ElasticSearchPersonAllegation allegation = new ElasticSearchPersonAllegation();
    allegation.setId(this.allegationId);
    allegation.setAllegationDescription(
        ElasticSearchPerson.getSystemCodes().getCodeShortDescription(this.allegationType));
    allegation.setDispositionId(String.valueOf(this.allegationDisposition));
    allegation.setDispositionDescription(
        ElasticSearchPerson.getSystemCodes().getCodeShortDescription(this.allegationDisposition));

    allegation.setPerpetratorId(this.perpetratorId);
    allegation.setPerpetratorLegacyClientId(this.perpetratorId);
    allegation.setPerpetratorFirstName(this.perpetratorFirstName);
    allegation.setPerpetratorLastName(this.perpetratorLastName);

    allegation.setVictimId(this.victimId);
    allegation.setVictimLegacyClientId(this.victimId);
    allegation.setVictimFirstName(this.victimFirstName);
    allegation.setVictimLastName(this.victimLastName);

    referrals.addReferral(referral, allegation);
    return referrals;
  }

  @Override
  public Object getNormalizationGroupKey() {
    return this.clientId;
  }

  @Override
  public Serializable getPrimaryKey() {
    return null;
  }

  /**
   * Getter for composite "last change", the latest time that any associated record was created or
   * updated.
   * 
   * @return last change date
   */
  public Date getLastChange() {
    return lastChange;
  }

  /**
   * Setter for composite "last change", the latest time that any associated record was created or
   * updated.
   * 
   * @param lastChange last change date
   */
  public void setLastChange(Date lastChange) {
    this.lastChange = lastChange;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getReferralId() {
    return referralId;
  }

  public void setReferralId(String referralId) {
    this.referralId = referralId;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Integer getReferralResponseType() {
    return referralResponseType;
  }

  public void setReferralResponseType(Integer referralResponseType) {
    this.referralResponseType = referralResponseType;
  }

  public Integer getCounty() {
    return county;
  }

  public void setCounty(Integer county) {
    this.county = county;
  }

  public String getReporterId() {
    return reporterId;
  }

  public void setReporterId(String reporterId) {
    this.reporterId = reporterId;
  }

  public String getReporterFirstName() {
    return reporterFirstName;
  }

  public void setReporterFirstName(String reporterFirstName) {
    this.reporterFirstName = reporterFirstName;
  }

  public String getReporterLastName() {
    return reporterLastName;
  }

  public void setReporterLastName(String reporterLastName) {
    this.reporterLastName = reporterLastName;
  }

  public String getWorkerId() {
    return workerId;
  }

  public void setWorkerId(String workerId) {
    this.workerId = workerId;
  }

  public String getWorkerFirstName() {
    return workerFirstName;
  }

  public void setWorkerFirstName(String workerFirstName) {
    this.workerFirstName = workerFirstName;
  }

  public String getWorkerLastName() {
    return workerLastName;
  }

  public void setWorkerLastName(String workerLastName) {
    this.workerLastName = workerLastName;
  }

  public String getAllegationId() {
    return allegationId;
  }

  public void setAllegationId(String allegationId) {
    this.allegationId = allegationId;
  }

  public Integer getAllegationDisposition() {
    return allegationDisposition;
  }

  public void setAllegationDisposition(Integer allegationDisposition) {
    this.allegationDisposition = allegationDisposition;
  }

  public Integer getAllegationType() {
    return allegationType;
  }

  public void setAllegationType(Integer allegationType) {
    this.allegationType = allegationType;
  }

  public String getVictimId() {
    return victimId;
  }

  public void setVictimId(String victimId) {
    this.victimId = victimId;
  }

  public String getVictimFirstName() {
    return victimFirstName;
  }

  public void setVictimFirstName(String victimFirstName) {
    this.victimFirstName = victimFirstName;
  }

  public String getVictimLastName() {
    return victimLastName;
  }

  public void setVictimLastName(String victimLastName) {
    this.victimLastName = victimLastName;
  }

  public String getPerpetratorId() {
    return perpetratorId;
  }

  public void setPerpetratorId(String perpetratorId) {
    this.perpetratorId = perpetratorId;
  }

  public String getPerpetratorFirstName() {
    return perpetratorFirstName;
  }

  public void setPerpetratorFirstName(String perpetratorFirstName) {
    this.perpetratorFirstName = perpetratorFirstName;
  }

  public String getPerpetratorLastName() {
    return perpetratorLastName;
  }

  public void setPerpetratorLastName(String perpetratorLastName) {
    this.perpetratorLastName = perpetratorLastName;
  }

  public String getLimitedAccessCode() {
    return limitedAccessCode;
  }

  public void setLimitedAccessCode(String limitedAccessCode) {
    this.limitedAccessCode = limitedAccessCode;
  }

  public Date getLimitedAccessDate() {
    return limitedAccessDate;
  }

  public void setLimitedAccessDate(Date limitedAccessDate) {
    this.limitedAccessDate = limitedAccessDate;
  }

  public String getLimitedAccessDescription() {
    return limitedAccessDescription;
  }

  public void setLimitedAccessDescription(String limitedAccessDescription) {
    this.limitedAccessDescription = limitedAccessDescription;
  }

  public Integer getLimitedAccessGovernmentEntityId() {
    return limitedAccessGovernmentEntityId;
  }

  public void setLimitedAccessGovernmentEntityId(Integer limitedAccessGovernmentEntityId) {
    this.limitedAccessGovernmentEntityId = limitedAccessGovernmentEntityId;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public final int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public final boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE, false);
  }

}
