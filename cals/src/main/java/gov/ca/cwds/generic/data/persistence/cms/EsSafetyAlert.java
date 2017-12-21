package gov.ca.cwds.generic.data.persistence.cms;

import gov.ca.cwds.data.es.ElasticSearchCounty;
import gov.ca.cwds.data.es.ElasticSearchSafetyAlert;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.data.std.ApiObjectIdentity;
import gov.ca.cwds.generic.jobs.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.DomainChef;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.NamedNativeQueries;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.annotations.Type;

/**
 * Entity bean for view VW_LST_SAFETY_ALERT.
 * 
 * @author CWDS API Team
 */
@Entity
@Table(name = "VW_LST_SAFETY_ALERT")
@NamedNativeQueries({
    @NamedNativeQuery(name = "gov.ca.cwds.data.persistence.cms.EsSafetyAlert.findAllUpdatedAfter",
        query = "SELECT r.* FROM {h-schema}VW_LST_SAFETY_ALERT r WHERE r.CLIENT_ID IN ( "
            + "SELECT r1.CLIENT_ID FROM {h-schema}VW_LST_SAFETY_ALERT r1 "
            + "WHERE r1.LAST_CHANGED > :after "
            + ") ORDER BY CLIENT_ID, ALERT_ID FOR READ ONLY WITH UR ",
        resultClass = EsSafetyAlert.class, readOnly = true),

    @NamedNativeQuery(
        name = "gov.ca.cwds.data.persistence.cms.EsSafetyAlert.findAllUpdatedAfterWithUnlimitedAccess",
        query = "SELECT r.* FROM {h-schema}VW_LST_SAFETY_ALERT r WHERE r.CLIENT_ID IN ( "
            + "SELECT r1.CLIENT_ID FROM {h-schema}VW_LST_SAFETY_ALERT r1 "
            + "WHERE r1.LAST_CHANGED > :after "
            + ") AND r.CLIENT_SENSITIVITY_IND = 'N' ORDER BY CLIENT_ID, ALERT_ID FOR READ ONLY WITH UR ",
        resultClass = EsSafetyAlert.class, readOnly = true),

    @NamedNativeQuery(
        name = "gov.ca.cwds.data.persistence.cms.EsSafetyAlert.findAllUpdatedAfterWithLimitedAccess",
        query = "SELECT r.* FROM {h-schema}VW_LST_SAFETY_ALERT r WHERE r.CLIENT_ID IN ( "
            + "SELECT r1.CLIENT_ID FROM {h-schema}VW_LST_SAFETY_ALERT r1 "
            + "WHERE r1.LAST_CHANGED > :after "
            + ") AND r.CLIENT_SENSITIVITY_IND != 'N' ORDER BY CLIENT_ID, ALERT_ID FOR READ ONLY WITH UR ",
        resultClass = EsSafetyAlert.class, readOnly = true)})

public class EsSafetyAlert extends ApiObjectIdentity
    implements PersistentObject, ApiGroupNormalizer<ReplicatedSafetyAlerts> {

  private static final long serialVersionUID = -4358337986243075067L;

  @Type(type = "timestamp")
  @Column(name = "LAST_CHANGED", updatable = false)
  private Date lastChanged;

  @Id
  @Column(name = "CLIENT_ID")
  private String clientId;

  @Id
  @Column(name = "ALERT_ID")
  private String alertId;

  @Column(name = "ACTIVATION_REASON_CD")
  @Type(type = "int")
  private Integer activationReasonCode;

  @Column(name = "ACTIVATION_DATE")
  @Type(type = "date")
  private Date activationDate;

  @Column(name = "ACTIVATION_COUNTY_CD")
  @Type(type = "integer")
  private Integer activationCountyCode;

  @Column(name = "ACTIVATION_EXPLANATION")
  private String activationExplanation;

  @Column(name = "DEACTIVATION_DATE")
  @Type(type = "date")
  private Date deactivationDate;

  @Column(name = "DEACTIVATION_COUNTY_CD")
  @Type(type = "integer")
  private Integer deactivationCountyCode;

  @Column(name = "DEACTIVATION_EXPLANATION")
  private String deactivationExplanation;

  @Column(name = "LAST_UPDATED_ID")
  private String lastUpdatedId;

  @Column(name = "LAST_UPDATED_TS")
  @Type(type = "timestamp")
  private Date lastUpdatedTimestamp;

  @Column(name = "ALERT_IBMSNAP_OPERATION")
  private String lastUpdatedOperation;

  @Column(name = "ALERT_IBMSNAP_LOGMARKER")
  @Type(type = "timestamp")
  private Date replicationTimestamp;

  /**
   * No-argument constructor
   */
  public EsSafetyAlert() {
    // No-argument default constructor
  }

  @Override
  public Class<ReplicatedSafetyAlerts> getNormalizationClass() {
    return ReplicatedSafetyAlerts.class;
  }

  @Override
  public ReplicatedSafetyAlerts normalize(Map<Object, ReplicatedSafetyAlerts> map) {
    ReplicatedSafetyAlerts alerts = map.get(this.clientId);
    if (alerts == null) {
      alerts = new ReplicatedSafetyAlerts(this.clientId);
      map.put(this.clientId, alerts);
    }

    ElasticSearchSafetyAlert alert = new ElasticSearchSafetyAlert();
    alert.setId(this.alertId);

    ElasticSearchSafetyAlert.Activation activation = new ElasticSearchSafetyAlert.Activation();
    alert.setActivation(activation);

    activation.setActivationReasonDescription(
        SystemCodeCache.global().getSystemCodeShortDescription(this.activationReasonCode));
    activation.setActivationReasonId(
        this.activationReasonCode != null ? this.activationReasonCode.toString() : null);

    ElasticSearchCounty activationCounty = new ElasticSearchCounty();
    activation.setActivationCounty(activationCounty);
    activationCounty
        .setName(SystemCodeCache.global().getSystemCodeShortDescription(this.activationCountyCode));
    activationCounty
        .setId(this.activationCountyCode != null ? this.activationCountyCode.toString() : null);

    activation.setActivationDate(DomainChef.cookDate(this.activationDate));
    activation.setActivationExplanation(this.activationExplanation);


    ElasticSearchSafetyAlert.Deactivation deactivation =
        new ElasticSearchSafetyAlert.Deactivation();
    alert.setDeactivation(deactivation);

    ElasticSearchCounty deactivationCounty = new ElasticSearchCounty();
    deactivation.setDeactivationCounty(deactivationCounty);

    deactivationCounty.setName(
        SystemCodeCache.global().getSystemCodeShortDescription(this.deactivationCountyCode));
    deactivationCounty
        .setId(this.deactivationCountyCode != null ? this.deactivationCountyCode.toString() : null);

    deactivation.setDeactivationDate(DomainChef.cookDate(this.deactivationDate));
    deactivation.setDeactivationExplanation(this.deactivationExplanation);

    alert.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.alertId,
        this.lastUpdatedTimestamp, LegacyTable.SAFETY_ALERT));

    alerts.addSafetyAlert(alert);
    return alerts;
  }

  @Override
  public String getNormalizationGroupKey() {
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
  public Date getLastChanged() {
    return lastChanged;
  }

  /**
   * Setter for composite "last change", the latest time that any associated record was created or
   * updated.
   * 
   * @param lastChanged last change date
   */
  public void setLastChanged(Date lastChanged) {
    this.lastChanged = lastChanged;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getAlertId() {
    return alertId;
  }

  public void setAlertId(String alertId) {
    this.alertId = alertId;
  }

  public Integer getActivationReasonCode() {
    return activationReasonCode;
  }

  public void setActivationReasonCode(Integer activationReasonCode) {
    this.activationReasonCode = activationReasonCode;
  }

  public Date getActivationDate() {
    return activationDate;
  }

  public void setActivationDate(Date activationDate) {
    this.activationDate = activationDate;
  }

  public Integer getActivationCountyCode() {
    return activationCountyCode;
  }

  public void setActivationCountyCode(Integer activationCountyCode) {
    this.activationCountyCode = activationCountyCode;
  }

  public String getActivationExplanation() {
    return activationExplanation;
  }

  public void setActivationExplanation(String activationExplanation) {
    this.activationExplanation = activationExplanation;
  }

  public Date getDeactivationDate() {
    return deactivationDate;
  }

  public void setDeactivationDate(Date deactivationDate) {
    this.deactivationDate = deactivationDate;
  }

  public Integer getDeactivationCountyCode() {
    return deactivationCountyCode;
  }

  public void setDeactivationCountyCode(Integer deactivationCountyCode) {
    this.deactivationCountyCode = deactivationCountyCode;
  }

  public String getDeactivationExplanation() {
    return deactivationExplanation;
  }

  public void setDeactivationExplanation(String deactivationExplanation) {
    this.deactivationExplanation = deactivationExplanation;
  }

  public String getLastUpdatedId() {
    return lastUpdatedId;
  }

  public void setLastUpdatedId(String lastUpdatedId) {
    this.lastUpdatedId = lastUpdatedId;
  }

  public Date getLastUpdatedTimestamp() {
    return lastUpdatedTimestamp;
  }

  public void setLastUpdatedTimestamp(Date lastUpdatedTimestamp) {
    this.lastUpdatedTimestamp = lastUpdatedTimestamp;
  }

  public String getLastUpdatedOperation() {
    return lastUpdatedOperation;
  }

  public void setLastUpdatedOperation(String lastUpdatedOperation) {
    this.lastUpdatedOperation = lastUpdatedOperation;
  }

  public Date getReplicationTimestamp() {
    return replicationTimestamp;
  }

  public void setReplicationTimestamp(Date replicationTimestamp) {
    this.replicationTimestamp = replicationTimestamp;
  }
}
