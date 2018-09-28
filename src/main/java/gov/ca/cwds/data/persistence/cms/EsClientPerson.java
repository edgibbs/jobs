package gov.ca.cwds.data.persistence.cms;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.FlushModeType;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.annotations.Type;

import gov.ca.cwds.common.NameSuffixTranslator;
import gov.ca.cwds.data.es.ElasticSearchPersonAka;
import gov.ca.cwds.data.es.ElasticSearchSafetyAlert;
import gov.ca.cwds.data.es.ElasticSearchSystemCode;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.rocket.ClientSQLResource;
import gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.DomainChef;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;

/**
 * Entity bean for the People Summary index, CWS/CMS view VW_LST_CLIENT_ADDRESS.
 * 
 * <p>
 * <strong>NOTE</strong>: view VW_LST_CLIENT_ADDRESS is no longer in use. See
 * {@link ClientSQLResource}.
 * </p>
 * 
 * <p>
 * Implements {@link ApiGroupNormalizer} and converts to {@link ReplicatedClient}.
 * </p>
 * 
 * @author CWDS API Team
 * @see ClientSQLResource
 */
@Entity
@Table(name = "VW_LST_CLIENT_ADDRESS")
// @formatter:off
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.EsClientPerson.findAllUpdatedAfter",
    query = ClientSQLResource.SELECT_CLIENT_VIEW_LAST_CHANGE,
    resultClass = EsClientPerson.class, readOnly = true, cacheable=false, flushMode=FlushModeType.MANUAL)

@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.EsClientPerson.findAllUpdatedAfterWithUnlimitedAccess",
    query = ClientSQLResource.SELECT_CLIENT_VIEW_LAST_CHANGE,
    resultClass = EsClientPerson.class, readOnly = true, cacheable=false, flushMode=FlushModeType.MANUAL)

@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.EsClientPerson.findAllUpdatedAfterWithLimitedAccess",
    query = "SELECT " + ClientSQLResource.LAST_CHG_COLUMNS + "\n"
        + "FROM {h-schema}VW_LST_CLIENT_ADDRESS x \n"
        + "WHERE x.CLT_SENSTV_IND != 'N' \n "
        + "ORDER BY CLT_IDENTIFIER \n"
        + "FOR READ ONLY WITH UR ",
    resultClass = EsClientPerson.class, readOnly = true, cacheable=false, flushMode=FlushModeType.MANUAL)
// @formatter:on
public class EsClientPerson extends BaseEsClient
    implements Comparable<EsClientPerson>, Comparator<EsClientPerson> {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(EsClientPerson.class);

  // ================================
  // SAF_ALRT: (safety alerts)
  // ================================

  @Id
  @Column(name = "SAL_THIRD_ID")
  private String safetyAlertId;

  @Column(name = "SAL_ACTV_RNC")
  @Type(type = "short")
  private Short safetyAlertActivationReasonCode;

  @Column(name = "SAL_ACTV_DT")
  @Type(type = "date")
  private Date safetyAlertActivationDate;

  @Column(name = "SAL_ACTV_GEC")
  @Type(type = "short")
  private Short safetyAlertActivationCountyCode;

  @Column(name = "SAL_ACTV_TXT")
  private String safetyAlertActivationExplanation;

  @Column(name = "SAL_DACT_DT")
  @Type(type = "date")
  private Date safetyAlertDeactivationDate;

  @Column(name = "SAL_DACT_GEC")
  @Type(type = "short")
  private Short safetyAlertDeactivationCountyCode;

  @Column(name = "SAL_DACT_TXT")
  private String safetyAlertDeactivationExplanation;

  @Column(name = "SAL_LST_UPD_ID")
  private String safetyAlertLastUpdatedId;

  @Column(name = "SAL_LST_UPD_TS")
  @Type(type = "timestamp")
  private Date safetyAlertLastUpdatedTimestamp;

  @Enumerated(EnumType.STRING)
  @Column(name = "SAL_IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation safetyAlertLastUpdatedOperation;

  @Column(name = "SAL_IBMSNAP_LOGMARKER")
  @Type(type = "timestamp")
  private Date safetyAlertReplicationTimestamp;

  // =====================================
  // OCL_NM_T: (other client name / AKA)
  // =====================================

  @Id
  @Column(name = "ONM_THIRD_ID")
  private String akaId;

  @Column(name = "ONM_FIRST_NM")
  private String akaFirstName;

  @Column(name = "ONM_LAST_NM")
  private String akaLastName;

  @Column(name = "ONM_MIDDLE_NM")
  private String akaMiddleName;

  @Column(name = "ONM_NMPRFX_DSC")
  private String akaNamePrefixDescription;

  @Type(type = "short")
  @Column(name = "ONM_NAME_TPC")
  private Short akaNameType;

  @Column(name = "ONM_SUFX_TLDSC")
  private String akaSuffixTitleDescription;

  @Column(name = "ONM_LST_UPD_ID")
  private String akaLastUpdatedId;

  @Column(name = "ONM_LST_UPD_TS")
  @Type(type = "timestamp")
  private Date akaLastUpdatedTimestamp;

  @Enumerated(EnumType.STRING)
  @Column(name = "ONM_IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation akaLastUpdatedOperation;

  @Column(name = "ONM_IBMSNAP_LOGMARKER")
  @Type(type = "timestamp")
  private Date akaReplicationTimestamp;

  // ====================================
  // CASE_T: (is there an open case)
  // =====================================

  @Column(name = "CAS_IDENTIFIER")
  private String openCaseId;

  @Column(name = "CAS_RSP_AGY_CD")
  private String openCaseResponsibleAgencyCode;

  /**
   * Build an EsClient from the incoming ResultSet.
   * 
   * @param rs incoming tuple
   * @return a populated EsClient
   * @throws SQLException if unable to convert types or stream breaks, etc.
   */
  public static EsClientPerson extract(final ResultSet rs) throws SQLException {
    final EsClientPerson ret = new EsClientPerson();
    BaseEsClient.extract(ret, rs);

    //
    // Safety alert
    //
    ret.safetyAlertId = rs.getString("SAL_THIRD_ID");
    ret.safetyAlertActivationCountyCode = rs.getShort("SAL_ACTV_GEC");
    ret.safetyAlertActivationDate = rs.getDate("SAL_ACTV_DT");
    ret.safetyAlertActivationExplanation = rs.getString("SAL_ACTV_TXT");
    ret.safetyAlertActivationReasonCode = rs.getShort("SAL_ACTV_RNC");
    ret.safetyAlertDeactivationCountyCode = rs.getShort("SAL_DACT_GEC");
    ret.safetyAlertDeactivationDate = rs.getDate("SAL_DACT_DT");
    ret.safetyAlertDeactivationExplanation = rs.getString("SAL_DACT_TXT");
    ret.safetyAlertLastUpdatedId = rs.getString("SAL_LST_UPD_ID");
    ret.safetyAlertLastUpdatedTimestamp = rs.getTimestamp("SAL_LST_UPD_TS");
    ret.safetyAlertLastUpdatedOperation =
        CmsReplicationOperation.strToRepOp(rs.getString("SAL_IBMSNAP_OPERATION"));
    ret.safetyAlertReplicationTimestamp = rs.getTimestamp("SAL_IBMSNAP_LOGMARKER");

    //
    // Other name (AKA)
    //
    ret.akaId = rs.getString("ONM_THIRD_ID");
    ret.akaFirstName = rs.getString("ONM_FIRST_NM");
    ret.akaLastName = rs.getString("ONM_LAST_NM");
    ret.akaMiddleName = rs.getString("ONM_MIDDLE_NM");
    ret.akaNamePrefixDescription = rs.getString("ONM_NMPRFX_DSC");
    ret.akaNameType = rs.getShort("ONM_NAME_TPC");
    ret.akaSuffixTitleDescription = rs.getString("ONM_SUFX_TLDSC");
    ret.akaLastUpdatedId = rs.getString("ONM_LST_UPD_ID");
    ret.akaLastUpdatedTimestamp = rs.getTimestamp("ONM_LST_UPD_TS");
    ret.akaLastUpdatedOperation =
        CmsReplicationOperation.strToRepOp(rs.getString("ONM_IBMSNAP_OPERATION"));
    ret.akaReplicationTimestamp = rs.getTimestamp("ONM_IBMSNAP_LOGMARKER");

    //
    // is there an open case? (get its id)
    //
    ret.openCaseId = rs.getString("CAS_IDENTIFIER");

    // Was necessary during quarterly CWS/CMS DDL update.
    // Probably no longer needed.
    try {
      ret.openCaseResponsibleAgencyCode = rs.getString("CAS_RSP_AGY_CD");
    } catch (Exception e) {
      LOGGER.trace("COLUMN 'CAS_RSP_AGY_CD' NOT IN SCHEMA!", e);
    }

    //
    // Last change (overall)
    //
    ret.lastChange = rs.getTimestamp("LAST_CHG");

    return ret;
  }

  @Override
  protected ReplicatedClient makeReplicatedClient() {
    return new ReplicatedClient();
  }

  @Override
  public Class<ReplicatedClient> getNormalizationClass() {
    return ReplicatedClient.class;
  }

  @Override
  public ReplicatedClient normalize(Map<Object, ReplicatedClient> map) {
    ReplicatedClient ret = super.normalize(map);

    // Safety alerts
    ret.addSafetyAlert(createSafetyAlert());

    // AKA
    ret.addAka(createAka());

    // Open case id
    ret.setOpenCaseId(this.openCaseId);

    ret.setOpenCaseResponsibleAgencyCode(this.openCaseResponsibleAgencyCode);

    map.put(ret.getId(), ret);
    return ret;
  }

  /**
   * This view (i.e., materialized query table) doesn't have a proper unique key, but a combination
   * of several fields might come close.
   * <ul>
   * <li>"Cook": convert String parameter to strong type</li>
   * <li>"Uncook": convert strong type parameter to String</li>
   * </ul>
   */
  @Override
  public Serializable getPrimaryKey() {
    return null;
  }

  public Date getCltFatherParentalRightTermDate() {
    return NeutronDateUtils.freshDate(cltFatherParentalRightTermDate);
  }

  public void setClaId(String claId) {
    this.claId = claId;
  }

  public String getClientCountyId() {
    return clientCountyId;
  }

  public String getClientEthnicityId() {
    return clientEthnicityId;
  }

  public Short getClientEthnicityCode() {
    return clientEthnicityCode;
  }

  public String getClientCountyRule() {
    return clientCountyRule;
  }

  public void setClientCountyRule(String clientCountyRule) {
    this.clientCountyRule = clientCountyRule;
  }

  public void setClientCountyId(String clientCountyId) {
    this.clientCountyId = clientCountyId;
  }

  public void setClientEthnicityId(String clientEthnicityId) {
    this.clientEthnicityId = clientEthnicityId;
  }

  public void setClientEthnicityCode(Short clientEthnicityCode) {
    this.clientEthnicityCode = clientEthnicityCode;
  }

  public void setAdrReplicationOperation(CmsReplicationOperation adrReplicationOperation) {
    this.adrReplicationOperation = adrReplicationOperation;
  }

  public String getSafetyAlertId() {
    return safetyAlertId;
  }

  public void setSafetyAlertId(String safetyAlertId) {
    this.safetyAlertId = safetyAlertId;
  }

  public Short getSafetyAlertActivationReasonCode() {
    return safetyAlertActivationReasonCode;
  }

  public void setSafetyAlertActivationReasonCode(Short safetyAlertActivationReasonCode) {
    this.safetyAlertActivationReasonCode = safetyAlertActivationReasonCode;
  }

  public Date getSafetyAlertActivationDate() {
    return NeutronDateUtils.freshDate(safetyAlertActivationDate);
  }

  public void setSafetyAlertActivationDate(Date safetyAlertActivationDate) {
    this.safetyAlertActivationDate = NeutronDateUtils.freshDate(safetyAlertActivationDate);
  }

  public Short getSafetyAlertActivationCountyCode() {
    return safetyAlertActivationCountyCode;
  }

  public void setSafetyAlertActivationCountyCode(Short safetyAlertActivationCountyCode) {
    this.safetyAlertActivationCountyCode = safetyAlertActivationCountyCode;
  }

  public String getSafetyAlertActivationExplanation() {
    return safetyAlertActivationExplanation;
  }

  public void setSafetyAlertActivationExplanation(String safetyAlertActivationExplanation) {
    this.safetyAlertActivationExplanation = safetyAlertActivationExplanation;
  }

  public Date getSafetyAlertDeactivationDate() {
    return NeutronDateUtils.freshDate(safetyAlertDeactivationDate);
  }

  public void setSafetyAlertDeactivationDate(Date safetyAlertDeactivationDate) {
    this.safetyAlertDeactivationDate = NeutronDateUtils.freshDate(safetyAlertDeactivationDate);
  }

  public Short getSafetyAlertDeactivationCountyCode() {
    return safetyAlertDeactivationCountyCode;
  }

  public void setSafetyAlertDeactivationCountyCode(Short safetyAlertDeactivationCountyCode) {
    this.safetyAlertDeactivationCountyCode = safetyAlertDeactivationCountyCode;
  }

  public String getSafetyAlertDeactivationExplanation() {
    return safetyAlertDeactivationExplanation;
  }

  public void setSafetyAlertDeactivationExplanation(String safetyAlertDeactivationExplanation) {
    this.safetyAlertDeactivationExplanation = safetyAlertDeactivationExplanation;
  }

  public String getSafetyAlertLastUpdatedId() {
    return safetyAlertLastUpdatedId;
  }

  public void setSafetyAlertLastUpdatedId(String safetyAlertLastUpdatedId) {
    this.safetyAlertLastUpdatedId = safetyAlertLastUpdatedId;
  }

  public Date getSafetyAlertLastUpdatedTimestamp() {
    return NeutronDateUtils.freshDate(safetyAlertLastUpdatedTimestamp);
  }

  public void setSafetyAlertLastUpdatedTimestamp(Date safetyAlertLastUpdatedTimestamp) {
    this.safetyAlertLastUpdatedTimestamp =
        NeutronDateUtils.freshDate(safetyAlertLastUpdatedTimestamp);
  }

  public CmsReplicationOperation getSafetyAlertLastUpdatedOperation() {
    return safetyAlertLastUpdatedOperation;
  }

  public void setSafetyAlertLastUpdatedOperation(
      CmsReplicationOperation safetyAlertLastUpdatedOperation) {
    this.safetyAlertLastUpdatedOperation = safetyAlertLastUpdatedOperation;
  }

  public Date getSafetyAlertReplicationTimestamp() {
    return NeutronDateUtils.freshDate(safetyAlertReplicationTimestamp);
  }

  public void setSafetyAlertReplicationTimestamp(Date safetyAlertReplicationTimestamp) {
    this.safetyAlertReplicationTimestamp =
        NeutronDateUtils.freshDate(safetyAlertReplicationTimestamp);
  }

  public String getAkaId() {
    return akaId;
  }

  public void setAkaId(String akaId) {
    this.akaId = akaId;
  }

  public String getAkaFirstName() {
    return akaFirstName;
  }

  public void setAkaFirstName(String akaFirstName) {
    this.akaFirstName = akaFirstName;
  }

  public String getAkaLastName() {
    return akaLastName;
  }

  public void setAkaLastName(String akaLastName) {
    this.akaLastName = akaLastName;
  }

  public String getAkaMiddleName() {
    return akaMiddleName;
  }

  public void setAkaMiddleName(String akaMiddleName) {
    this.akaMiddleName = akaMiddleName;
  }

  public String getAkaNamePrefixDescription() {
    return akaNamePrefixDescription;
  }

  public void setAkaNamePrefixDescription(String akaNamePrefixDescription) {
    this.akaNamePrefixDescription = akaNamePrefixDescription;
  }

  public Short getAkaNameType() {
    return akaNameType;
  }

  public void setAkaNameType(Short akaNameType) {
    this.akaNameType = akaNameType;
  }

  public String getAkaSuffixTitleDescription() {
    return akaSuffixTitleDescription;
  }

  public void setAkaSuffixTitleDescription(String akaSuffixTitleDescription) {
    this.akaSuffixTitleDescription = akaSuffixTitleDescription;
  }

  public String getAkaLastUpdatedId() {
    return akaLastUpdatedId;
  }

  public void setAkaLastUpdatedId(String akaLastUpdatedId) {
    this.akaLastUpdatedId = akaLastUpdatedId;
  }

  public Date getAkaLastUpdatedTimestamp() {
    return NeutronDateUtils.freshDate(akaLastUpdatedTimestamp);
  }

  public void setAkaLastUpdatedTimestamp(Date akaLastUpdatedTimestamp) {
    this.akaLastUpdatedTimestamp = NeutronDateUtils.freshDate(akaLastUpdatedTimestamp);
  }

  public CmsReplicationOperation getAkaLastUpdatedOperation() {
    return akaLastUpdatedOperation;
  }

  public void setAkaLastUpdatedOperation(CmsReplicationOperation akaLastUpdatedOperation) {
    this.akaLastUpdatedOperation = akaLastUpdatedOperation;
  }

  public Date getAkaReplicationTimestamp() {
    return NeutronDateUtils.freshDate(akaReplicationTimestamp);
  }

  public void setAkaReplicationTimestamp(Date akaReplicationTimestamp) {
    this.akaReplicationTimestamp = NeutronDateUtils.freshDate(akaReplicationTimestamp);
  }

  private ElasticSearchSafetyAlert createSafetyAlert() {
    if (StringUtils.isBlank(this.safetyAlertId)
        || CmsReplicationOperation.D == this.safetyAlertLastUpdatedOperation) {
      return null;
    }

    final ElasticSearchSafetyAlert alert = new ElasticSearchSafetyAlert();
    alert.setId(this.safetyAlertId);

    final ElasticSearchSafetyAlert.Activation activation =
        new ElasticSearchSafetyAlert.Activation();
    alert.setActivation(activation);

    activation.setActivationReasonDescription(SystemCodeCache.global()
        .getSystemCodeShortDescription(this.safetyAlertActivationReasonCode));
    activation.setActivationReasonId(this.safetyAlertActivationReasonCode != null
        ? this.safetyAlertActivationReasonCode.toString()
        : null);

    final ElasticSearchSystemCode activationCounty = new ElasticSearchSystemCode();
    activation.setActivationCounty(activationCounty);
    activationCounty.setDescription(SystemCodeCache.global()
        .getSystemCodeShortDescription(this.safetyAlertActivationCountyCode));
    activationCounty.setId(this.safetyAlertActivationCountyCode != null
        ? this.safetyAlertActivationCountyCode.toString()
        : null);

    activation.setActivationDate(DomainChef.cookDate(this.safetyAlertActivationDate));
    activation.setActivationExplanation(this.safetyAlertActivationExplanation);

    final ElasticSearchSafetyAlert.Deactivation deactivation =
        new ElasticSearchSafetyAlert.Deactivation();
    alert.setDeactivation(deactivation);

    final ElasticSearchSystemCode deactivationCounty = new ElasticSearchSystemCode();
    deactivation.setDeactivationCounty(deactivationCounty);

    deactivationCounty.setDescription(SystemCodeCache.global()
        .getSystemCodeShortDescription(this.safetyAlertDeactivationCountyCode));
    deactivationCounty.setId(this.safetyAlertDeactivationCountyCode != null
        ? this.safetyAlertDeactivationCountyCode.toString()
        : null);

    deactivation.setDeactivationDate(DomainChef.cookDate(this.safetyAlertDeactivationDate));
    deactivation.setDeactivationExplanation(this.safetyAlertDeactivationExplanation);

    alert.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.safetyAlertId,
        this.safetyAlertLastUpdatedTimestamp, LegacyTable.SAFETY_ALERT));

    return alert;
  }

  private ElasticSearchPersonAka createAka() {
    if (StringUtils.isBlank(this.akaId)
        || CmsReplicationOperation.D == this.akaLastUpdatedOperation) {
      return null;
    }

    ElasticSearchPersonAka aka = new ElasticSearchPersonAka();
    aka.setId(this.akaId);

    if (StringUtils.isNotBlank(this.akaFirstName)) {
      aka.setFirstName(this.akaFirstName.trim());
    }

    if (StringUtils.isNotBlank(this.akaLastName)) {
      aka.setLastName(this.akaLastName.trim());
    }

    if (StringUtils.isNotBlank(this.akaMiddleName)) {
      aka.setMiddleName(this.akaMiddleName.trim());
    }

    if (StringUtils.isNotBlank(this.akaNamePrefixDescription)) {
      aka.setPrefix(this.akaNamePrefixDescription.trim());
    }

    if (StringUtils.isNotBlank(this.akaSuffixTitleDescription)) {
      aka.setSuffix(NameSuffixTranslator.translate(this.akaSuffixTitleDescription.trim()));
    }

    if (this.akaNameType != null && this.akaNameType.intValue() != 0) {
      aka.setNameType(SystemCodeCache.global().getSystemCodeShortDescription(this.akaNameType));
    }

    aka.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.akaId,
        this.akaLastUpdatedTimestamp, LegacyTable.ALIAS_OR_OTHER_CLIENT_NAME));

    return aka;
  }

  // =====================
  // IDENTITY:
  // =====================

  @Override
  public int compare(EsClientPerson o1, EsClientPerson o2) {
    return o1.getCltId().compareTo(o2.getCltId());
  }

  @Override
  public int compareTo(EsClientPerson o) {
    return compare(this, o);
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
