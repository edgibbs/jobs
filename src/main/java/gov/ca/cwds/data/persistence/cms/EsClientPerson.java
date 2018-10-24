package gov.ca.cwds.data.persistence.cms;

import static gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils.freshDate;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.common.NameSuffixTranslator;
import gov.ca.cwds.data.es.ElasticSearchPersonAka;
import gov.ca.cwds.data.es.ElasticSearchPersonCsec;
import gov.ca.cwds.data.es.ElasticSearchSafetyAlert;
import gov.ca.cwds.data.es.ElasticSearchSystemCode;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.rocket.ClientSQLResource;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.DomainChef;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;
import gov.ca.cwds.utils.JsonUtils;

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

  private static final Logger LOGGER = LoggerFactory.getLogger(EsClientPerson.class);

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

  // ====================================
  // CSECHIST: (CSEC history)
  // =====================================

  @Column(name = "CSH_THIRD_ID")
  private String csecId;

  @Column(name = "CSH_CSEC_TPC")
  @Type(type = "short")
  private Short csecCodeId;

  @Column(name = "CSH_START_DT")
  @Type(type = "date")
  private Date csecStartDate;

  @Column(name = "CSH_END_DT")
  @Type(type = "date")
  private Date csecEndDate;

  @Column(name = "CSH_LST_UPD_ID")
  private String csecLastUpdatedId;

  @Column(name = "CSH_LST_UPD_TS")
  @Type(type = "timestamp")
  private Date csecLastUpdatedTimestamp;

  @Enumerated(EnumType.STRING)
  @Column(name = "CSH_IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation csecLastUpdatedOperation;

  @Column(name = "CSH_IBMSNAP_LOGMARKER")
  @Type(type = "timestamp")
  private Date csecReplicationTimestamp;

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
    ret.openCaseResponsibleAgencyCode = rs.getString("CAS_RSP_AGY_CD");

    //
    // CSEC history (CSEC)
    //
    ret.csecId = rs.getString("CSH_THIRD_ID");
    ret.csecCodeId = rs.getShort("CSH_CSEC_TPC");
    ret.csecStartDate = rs.getDate("CSH_START_DT");
    ret.csecEndDate = rs.getDate("CSH_END_DT");
    ret.csecLastUpdatedId = rs.getString("CSH_LST_UPD_ID");
    ret.csecLastUpdatedTimestamp = rs.getTimestamp("CSH_LST_UPD_TS");
    ret.csecLastUpdatedOperation =
        CmsReplicationOperation.strToRepOp(rs.getString("CSH_IBMSNAP_OPERATION"));
    ret.csecReplicationTimestamp = rs.getTimestamp("CSH_IBMSNAP_LOGMARKER");

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
    final ReplicatedClient ret = super.normalize(map);

    // Safety alerts
    ret.addSafetyAlert(createSafetyAlert());

    // AKA
    ret.addAka(createAka());

    // CSEC
    ret.addCsec(createCsec());

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
    return freshDate(cltFatherParentalRightTermDate);
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
    return freshDate(safetyAlertActivationDate);
  }

  public void setSafetyAlertActivationDate(Date safetyAlertActivationDate) {
    this.safetyAlertActivationDate = freshDate(safetyAlertActivationDate);
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
    return freshDate(safetyAlertDeactivationDate);
  }

  public void setSafetyAlertDeactivationDate(Date safetyAlertDeactivationDate) {
    this.safetyAlertDeactivationDate = freshDate(safetyAlertDeactivationDate);
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
    return freshDate(safetyAlertLastUpdatedTimestamp);
  }

  public void setSafetyAlertLastUpdatedTimestamp(Date safetyAlertLastUpdatedTimestamp) {
    this.safetyAlertLastUpdatedTimestamp = freshDate(safetyAlertLastUpdatedTimestamp);
  }

  public CmsReplicationOperation getSafetyAlertLastUpdatedOperation() {
    return safetyAlertLastUpdatedOperation;
  }

  public void setSafetyAlertLastUpdatedOperation(
      CmsReplicationOperation safetyAlertLastUpdatedOperation) {
    this.safetyAlertLastUpdatedOperation = safetyAlertLastUpdatedOperation;
  }

  public Date getSafetyAlertReplicationTimestamp() {
    return freshDate(safetyAlertReplicationTimestamp);
  }

  public void setSafetyAlertReplicationTimestamp(Date safetyAlertReplicationTimestamp) {
    this.safetyAlertReplicationTimestamp = freshDate(safetyAlertReplicationTimestamp);
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
    return freshDate(akaLastUpdatedTimestamp);
  }

  public void setAkaLastUpdatedTimestamp(Date akaLastUpdatedTimestamp) {
    this.akaLastUpdatedTimestamp = freshDate(akaLastUpdatedTimestamp);
  }

  public CmsReplicationOperation getAkaLastUpdatedOperation() {
    return akaLastUpdatedOperation;
  }

  public void setAkaLastUpdatedOperation(CmsReplicationOperation akaLastUpdatedOperation) {
    this.akaLastUpdatedOperation = akaLastUpdatedOperation;
  }

  public Date getAkaReplicationTimestamp() {
    return freshDate(akaReplicationTimestamp);
  }

  public void setAkaReplicationTimestamp(Date akaReplicationTimestamp) {
    this.akaReplicationTimestamp = freshDate(akaReplicationTimestamp);
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

    final ElasticSearchPersonAka aka = new ElasticSearchPersonAka();
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

  private ElasticSearchPersonCsec createCsec() {
    if (StringUtils.isBlank(this.csecId)
        || CmsReplicationOperation.D == this.csecLastUpdatedOperation) {
      return null;
    }

    final ElasticSearchPersonCsec csec = new ElasticSearchPersonCsec();
    csec.setId(this.csecId);

    csec.setStartDate(DomainChef.cookDate(this.csecStartDate));
    csec.setEndDate(DomainChef.cookDate(this.csecEndDate));

    if (this.csecCodeId != null && this.csecCodeId > 0) {
      csec.setCsecCodeId(this.csecCodeId.toString());
      csec.setCsecDesc(SystemCodeCache.global().getSystemCodeShortDescription(this.csecCodeId));
    }

    csec.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.csecId,
        this.csecLastUpdatedTimestamp, LegacyTable.CSEC_HISTORY));

    return csec;
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

  @Override
  public String toString() {
    String ret = "";
    try {
      ret = JsonUtils.to(this);
    } catch (Exception e) {
      LOGGER.warn("FAILED TO STREAM TO JSON!", e);
    }

    return ret;
  }

  public String getOpenCaseId() {
    return openCaseId;
  }

  public String getOpenCaseResponsibleAgencyCode() {
    return openCaseResponsibleAgencyCode;
  }

  public String getCsecId() {
    return csecId;
  }

  public Short getCsecCodeId() {
    return csecCodeId;
  }

  public Date getCsecStartDate() {
    return csecStartDate;
  }

  public Date getCsecEndDate() {
    return csecEndDate;
  }

  public String getCsecLastUpdatedId() {
    return csecLastUpdatedId;
  }

  public Date getCsecLastUpdatedTimestamp() {
    return csecLastUpdatedTimestamp;
  }

  public CmsReplicationOperation getCsecLastUpdatedOperation() {
    return csecLastUpdatedOperation;
  }

  public Date getCsecReplicationTimestamp() {
    return csecReplicationTimestamp;
  }

  public void setOpenCaseId(String openCaseId) {
    this.openCaseId = openCaseId;
  }

  public void setOpenCaseResponsibleAgencyCode(String openCaseResponsibleAgencyCode) {
    this.openCaseResponsibleAgencyCode = openCaseResponsibleAgencyCode;
  }

  public void setCsecId(String csecId) {
    this.csecId = csecId;
  }

  public void setCsecCodeId(Short csecCodeId) {
    this.csecCodeId = csecCodeId;
  }

  public void setCsecStartDate(Date csecStartDate) {
    this.csecStartDate = csecStartDate;
  }

  public void setCsecEndDate(Date csecEndDate) {
    this.csecEndDate = csecEndDate;
  }

  public void setCsecLastUpdatedId(String csecLastUpdatedId) {
    this.csecLastUpdatedId = csecLastUpdatedId;
  }

  public void setCsecLastUpdatedTimestamp(Date csecLastUpdatedTimestamp) {
    this.csecLastUpdatedTimestamp = csecLastUpdatedTimestamp;
  }

  public void setCsecLastUpdatedOperation(CmsReplicationOperation csecLastUpdatedOperation) {
    this.csecLastUpdatedOperation = csecLastUpdatedOperation;
  }

  public void setCsecReplicationTimestamp(Date csecReplicationTimestamp) {
    this.csecReplicationTimestamp = csecReplicationTimestamp;
  }

}
