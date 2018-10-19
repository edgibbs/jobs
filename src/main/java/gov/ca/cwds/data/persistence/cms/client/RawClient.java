package gov.ca.cwds.data.persistence.cms.client;

import static gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils.freshDate;
import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Type;

import gov.ca.cwds.data.persistence.cms.PlacementHomeAddress;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiGroupNormalizer;

public class RawClient extends ClientReference
    implements NeutronJdbcReader<RawClient>, ApiGroupNormalizer<ReplicatedClient> {

  private static final long serialVersionUID = 1L;

  private Map<String, RawClientAddress> clientAddress = new LinkedHashMap<>();

  private List<RawClientCounty> clientCounty = new ArrayList<>();

  private List<RawCsec> csec = new ArrayList<>();

  private List<RawAka> aka = new ArrayList<>();

  private List<RawEthnicity> ethnicity = new ArrayList<>();

  private List<RawSafetyAlert> safetyAlert = new ArrayList<>();

  private List<RawCase> cases = new ArrayList<>();

  private PlacementHomeAddress placementHomeAddress;

  // ==============================
  // CLIENT_T: (client)
  // ==============================

  @Column(name = "CLT_ADJDEL_IND")
  protected String cltAdjudicatedDelinquentIndicator;

  @Column(name = "CLT_ADPTN_STCD")
  protected String cltAdoptionStatusCode;

  @Column(name = "CLT_ALN_REG_NO")
  protected String cltAlienRegistrationNumber;

  @Column(name = "CLT_BIRTH_CITY")
  protected String cltBirthCity;

  @Type(type = "short")
  @Column(name = "CLT_B_CNTRY_C")
  protected Short cltBirthCountryCodeType;

  @Type(type = "date")
  @Column(name = "CLT_BIRTH_DT")
  protected Date cltBirthDate;

  @Column(name = "CLT_BR_FAC_NM")
  protected String cltBirthFacilityName;

  @Type(type = "short")
  @Column(name = "CLT_B_STATE_C")
  protected Short cltBirthStateCodeType;

  @Column(name = "CLT_BP_VER_IND")
  protected String cltBirthplaceVerifiedIndicator;

  @Column(name = "CLT_CHLD_CLT_B")
  protected String cltChildClientIndicatorVar;

  @Column(name = "CLT_CL_INDX_NO")
  protected String cltClientIndexNumber;

  @Column(name = "CLT_COMMNT_DSC")
  protected String cltCommentDescription;

  @Column(name = "CLT_COM_FST_NM")
  protected String cltCommonFirstName;

  @Column(name = "CLT_COM_LST_NM")
  protected String cltCommonLastName;

  @Column(name = "CLT_COM_MID_NM")
  protected String cltCommonMiddleName;

  @Type(type = "date")
  @Column(name = "CLT_CONF_ACTDT")
  protected Date cltConfidentialityActionDate;

  @Column(name = "CLT_CONF_EFIND")
  protected String cltConfidentialityInEffectIndicator;

  @Type(type = "date")
  @Column(name = "CLT_CREATN_DT")
  protected Date cltCreationDate;

  @Column(name = "CLT_CURRCA_IND")
  protected String cltCurrCaChildrenServIndicator;

  @Column(name = "CLT_COTH_DESC")
  protected String cltCurrentlyOtherDescription;

  @Column(name = "CLT_CURREG_IND")
  protected String cltCurrentlyRegionalCenterIndicator;

  @Type(type = "date")
  @Column(name = "CLT_DEATH_DT")
  protected Date cltDeathDate;

  @Column(name = "CLT_DTH_DT_IND")
  protected String cltDeathDateVerifiedIndicator;

  @Column(name = "CLT_DEATH_PLC")
  protected String cltDeathPlace;

  @Column(name = "CLT_DTH_RN_TXT")
  protected String cltDeathReasonText;

  @Column(name = "CLT_DRV_LIC_NO")
  protected String cltDriverLicenseNumber;

  @Type(type = "short")
  @Column(name = "CLT_D_STATE_C")
  protected Short cltDriverLicenseStateCodeType;

  @Column(name = "CLT_EMAIL_ADDR")
  @ColumnTransformer(read = "trim(CLT_EMAIL_ADDR)")
  protected String cltEmailAddress;

  @Column(name = "CLT_EST_DOB_CD")
  protected String cltEstimatedDobCode;

  @Column(name = "CLT_ETH_UD_CD")
  protected String cltEthUnableToDetReasonCode;

  @Type(type = "date")
  @Column(name = "CLT_FTERM_DT")
  protected Date cltFatherParentalRightTermDate;

  @Column(name = "CLT_GENDER_CD")
  protected String cltGenderCode;

  @Column(name = "CLT_HEALTH_TXT")
  protected String cltHealthSummaryText;

  @Column(name = "CLT_HISP_UD_CD")
  protected String cltHispUnableToDetReasonCode;

  @Column(name = "CLT_HISP_CD")
  protected String cltHispanicOriginCode;

  @Type(type = "short")
  @Column(name = "CLT_I_CNTRY_C")
  protected Short cltImmigrationCountryCodeType;

  @Type(type = "short")
  @Column(name = "CLT_IMGT_STC")
  protected Short cltImmigrationStatusType;

  @Column(name = "CLT_INCAPC_CD")
  protected String cltIncapacitatedParentCode;

  @Column(name = "CLT_HCARE_IND")
  protected String cltIndividualHealthCarePlanIndicator;

  @Column(name = "CLT_LIMIT_IND")
  protected String cltLimitationOnScpHealthIndicator;

  @Column(name = "CLT_LITRATE_CD")
  protected String cltLiterateCode;

  @Column(name = "CLT_MAR_HIST_B")
  protected String cltMaritalCohabitatnHstryIndicatorVar;

  @Type(type = "short")
  @Column(name = "CLT_MRTL_STC")
  protected Short cltMaritalStatusType;

  @Column(name = "CLT_MILT_STACD")
  protected String cltMilitaryStatusCode;

  @Type(type = "date")
  @Column(name = "CLT_MTERM_DT")
  protected Date cltMotherParentalRightTermDate;

  @Column(name = "CLT_NMPRFX_DSC")
  protected String cltNamePrefixDescription;

  @Type(type = "short")
  @Column(name = "CLT_NAME_TPC")
  protected Short cltNameType;

  @Column(name = "CLT_OUTWRT_IND")
  protected String cltOutstandingWarrantIndicator;

  @Column(name = "CLT_PREVCA_IND")
  protected String cltPrevCaChildrenServIndicator;

  @Column(name = "CLT_POTH_DESC")
  protected String cltPrevOtherDescription;

  @Column(name = "CLT_PREREG_IND")
  protected String cltPrevRegionalCenterIndicator;

  @Type(type = "short")
  @Column(name = "CLT_P_ETHNCTYC")
  protected Short cltPrimaryEthnicityType;

  @Type(type = "short")
  @Column(name = "CLT_P_LANG_TPC")
  protected Short cltPrimaryLanguageType;

  @Type(type = "short")
  @Column(name = "CLT_RLGN_TPC")
  protected Short cltReligionType;

  @Type(type = "short")
  @Column(name = "CLT_S_LANG_TC")
  protected Short cltSecondaryLanguageType;

  @Column(name = "CLT_SNTV_HLIND")
  protected String cltSensitiveHlthInfoOnFileIndicator;

  @Column(name = "CLT_SENSTV_IND")
  protected String cltSensitivityIndicator;

  @Column(name = "CLT_SOCPLC_CD")
  protected String cltSoc158PlacementCode;

  @Column(name = "CLT_SOC158_IND")
  protected String cltSoc158SealedClientIndicator;

  @Column(name = "CLT_SSN_CHG_CD")
  protected String cltSocialSecurityNumChangedCode;

  @Column(name = "CLT_SS_NO")
  protected String cltSocialSecurityNumber;

  @Column(name = "CLT_SUFX_TLDSC")
  protected String cltSuffixTitleDescription;

  @Column(name = "CLT_TRBA_CLT_B")
  protected String cltTribalAncestryClientIndicatorVar;

  @Column(name = "CLT_TR_MBVRT_B")
  protected String cltTribalMembrshpVerifctnIndicatorVar;

  @Column(name = "CLT_UNEMPLY_CD")
  protected String cltUnemployedParentCode;

  @Column(name = "CLT_ZIPPY_IND")
  protected String cltZippyCreatedIndicator;

  @Enumerated(EnumType.STRING)
  @Column(name = "CLT_IBMSNAP_OPERATION", updatable = false)
  protected CmsReplicationOperation cltReplicationOperation;

  @Type(type = "timestamp")
  @Column(name = "CLT_IBMSNAP_LOGMARKER", updatable = false)
  protected Date cltReplicationDate;

  @Column(name = "CLT_LST_UPD_ID")
  protected String cltLastUpdatedId;

  @Type(type = "timestamp")
  @Column(name = "CLT_LST_UPD_TS")
  protected Date cltLastUpdatedTime;

  @Override
  public Serializable getPrimaryKey() {
    return this.cltId;
  }

  @Override
  public RawClient read(final ResultSet rs) throws SQLException {
    super.read(rs);
    this.cltSensitivityIndicator = ifNull(rs.getString("CLT_SENSTV_IND"));
    this.cltSoc158SealedClientIndicator = ifNull(rs.getString("CLT_SOC158_IND"));
    this.cltAdjudicatedDelinquentIndicator = ifNull(rs.getString("CLT_ADJDEL_IND"));
    this.cltAdoptionStatusCode = rs.getString("CLT_ADPTN_STCD");
    this.cltAlienRegistrationNumber = ifNull(rs.getString("CLT_ALN_REG_NO"));
    this.cltBirthCity = ifNull(rs.getString("CLT_BIRTH_CITY"));
    this.cltBirthCountryCodeType = rs.getShort("CLT_B_CNTRY_C");
    this.cltBirthDate = rs.getDate("CLT_BIRTH_DT");
    this.cltBirthFacilityName = ifNull(rs.getString("CLT_BR_FAC_NM"));
    this.cltBirthStateCodeType = rs.getShort("CLT_B_STATE_C");
    this.cltBirthplaceVerifiedIndicator = ifNull(rs.getString("CLT_BP_VER_IND"));
    this.cltChildClientIndicatorVar = ifNull(rs.getString("CLT_CHLD_CLT_B"));
    this.cltClientIndexNumber = ifNull(rs.getString("CLT_CL_INDX_NO"));
    this.cltCommentDescription = ifNull(rs.getString("CLT_COMMNT_DSC"));
    this.cltCommonFirstName = (rs.getString("CLT_COM_FST_NM"));
    this.cltCommonLastName = ifNull(rs.getString("CLT_COM_LST_NM"));
    this.cltCommonMiddleName = ifNull(rs.getString("CLT_COM_MID_NM"));
    this.cltConfidentialityActionDate = rs.getDate("CLT_CONF_ACTDT");
    this.cltConfidentialityInEffectIndicator = ifNull(rs.getString("CLT_CONF_EFIND"));
    this.cltCreationDate = rs.getDate("CLT_CREATN_DT");
    this.cltCurrCaChildrenServIndicator = ifNull(rs.getString("CLT_CURRCA_IND"));
    this.cltCurrentlyOtherDescription = rs.getString("CLT_COTH_DESC");
    this.cltCurrentlyRegionalCenterIndicator = ifNull(rs.getString("CLT_CURREG_IND"));
    this.cltDeathDate = rs.getDate("CLT_DEATH_DT");
    this.cltDeathDateVerifiedIndicator = ifNull(rs.getString("CLT_DTH_DT_IND"));
    this.cltDeathPlace = ifNull(rs.getString("CLT_DEATH_PLC"));
    this.cltDeathReasonText = ifNull(rs.getString("CLT_DTH_RN_TXT"));
    this.cltDriverLicenseNumber = ifNull(rs.getString("CLT_DRV_LIC_NO"));
    this.cltDriverLicenseStateCodeType = rs.getShort("CLT_D_STATE_C");
    this.cltEmailAddress = ifNull(rs.getString("CLT_EMAIL_ADDR"));
    this.cltEstimatedDobCode = ifNull(rs.getString("CLT_EST_DOB_CD"));
    this.cltEthUnableToDetReasonCode = ifNull(rs.getString("CLT_ETH_UD_CD"));
    this.cltFatherParentalRightTermDate = rs.getDate("CLT_FTERM_DT");
    this.cltGenderCode = ifNull(rs.getString("CLT_GENDER_CD"));
    this.cltHealthSummaryText = ifNull(rs.getString("CLT_HEALTH_TXT"));
    this.cltHispUnableToDetReasonCode = ifNull(rs.getString("CLT_HISP_UD_CD"));
    this.cltHispanicOriginCode = ifNull(rs.getString("CLT_HISP_CD"));
    this.cltImmigrationCountryCodeType = rs.getShort("CLT_I_CNTRY_C");
    this.cltImmigrationStatusType = rs.getShort("CLT_IMGT_STC");
    this.cltIncapacitatedParentCode = ifNull(rs.getString("CLT_INCAPC_CD"));
    this.cltIndividualHealthCarePlanIndicator = ifNull(rs.getString("CLT_HCARE_IND"));
    this.cltLimitationOnScpHealthIndicator = ifNull(rs.getString("CLT_LIMIT_IND"));
    this.cltLiterateCode = ifNull(rs.getString("CLT_LITRATE_CD"));
    this.cltMaritalCohabitatnHstryIndicatorVar = ifNull(rs.getString("CLT_MAR_HIST_B"));
    this.cltMaritalStatusType = rs.getShort("CLT_MRTL_STC");
    this.cltMilitaryStatusCode = ifNull(rs.getString("CLT_MILT_STACD"));
    this.cltMotherParentalRightTermDate = rs.getDate("CLT_MTERM_DT");
    this.cltNamePrefixDescription = ifNull(rs.getString("CLT_NMPRFX_DSC"));
    this.cltNameType = rs.getShort("CLT_NAME_TPC");
    this.cltOutstandingWarrantIndicator = ifNull(rs.getString("CLT_OUTWRT_IND"));
    this.cltPrevCaChildrenServIndicator = ifNull(rs.getString("CLT_PREVCA_IND"));
    this.cltPrevOtherDescription = ifNull(rs.getString("CLT_POTH_DESC"));
    this.cltPrevRegionalCenterIndicator = ifNull(rs.getString("CLT_PREREG_IND"));
    this.cltPrimaryEthnicityType = rs.getShort("CLT_P_ETHNCTYC");

    return this;
  }

  @Override
  public Class<ReplicatedClient> getNormalizationClass() {
    return ReplicatedClient.class;
  }

  @Override
  public Serializable getNormalizationGroupKey() {
    return this.getCltId();
  }

  protected ReplicatedClient convertClient() {
    final ReplicatedClient ret = new ReplicatedClient();

    return ret;
  }

  @Override
  public ReplicatedClient normalize(Map<Object, ReplicatedClient> ignoreMe) {
    final ReplicatedClient ret = new ReplicatedClient();
    ret.setAdjudicatedDelinquentIndicator(getCltAdjudicatedDelinquentIndicator());
    ret.setAdoptionStatusCode(getCltAdoptionStatusCode());
    ret.setAlienRegistrationNumber(getCltAlienRegistrationNumber());
    ret.setBirthCity(getCltBirthCity());
    ret.setBirthCountryCodeType(getCltBirthCountryCodeType());
    ret.setBirthDate(getCltBirthDate());
    ret.setBirthFacilityName(getCltBirthFacilityName());
    ret.setBirthplaceVerifiedIndicator(getCltBirthplaceVerifiedIndicator());
    ret.setBirthStateCodeType(getCltBirthStateCodeType());
    ret.setChildClientIndicatorVar(getCltChildClientIndicatorVar());
    ret.setClientIndexNumber(getCltClientIndexNumber());
    ret.setCommentDescription(getCltCommentDescription());
    ret.setCommonFirstName(getCltCommonFirstName());
    ret.setCommonLastName(getCltCommonLastName());
    ret.setCommonMiddleName(getCltCommonMiddleName());
    ret.setConfidentialityActionDate(getCltConfidentialityActionDate());
    ret.setConfidentialityInEffectIndicator(getCltConfidentialityInEffectIndicator());
    ret.setCreationDate(getCltCreationDate());
    ret.setCurrCaChildrenServIndicator(getCltCurrCaChildrenServIndicator());
    ret.setCurrentlyOtherDescription(getCltCurrentlyOtherDescription());
    ret.setCurrentlyRegionalCenterIndicator(getCltCurrentlyRegionalCenterIndicator());
    ret.setDeathDate(getCltDeathDate());
    ret.setDeathDateVerifiedIndicator(getCltDeathDateVerifiedIndicator());
    ret.setDeathPlace(getCltDeathPlace());
    ret.setDeathReasonText(getCltDeathReasonText());
    ret.setDriverLicenseNumber(getCltDriverLicenseNumber());
    ret.setDriverLicenseStateCodeType(getCltDriverLicenseStateCodeType());
    ret.setEmailAddress(getCltEmailAddress());
    ret.setEstimatedDobCode(getCltEstimatedDobCode());
    ret.setEthUnableToDetReasonCode(getCltEthUnableToDetReasonCode());
    ret.setFatherParentalRightTermDate(getCltFatherParentalRightTermDate());
    ret.setCommonFirstName(getCltCommonFirstName());
    ret.setGenderCode(getCltGenderCode());
    ret.setHealthSummaryText(getCltHealthSummaryText());
    ret.setHispanicOriginCode(getCltHispanicOriginCode());
    ret.setHispUnableToDetReasonCode(getCltHispUnableToDetReasonCode());
    ret.setId(getCltId());
    ret.setImmigrationCountryCodeType(getCltImmigrationCountryCodeType());
    ret.setImmigrationStatusType(getCltImmigrationStatusType());
    ret.setIncapacitatedParentCode(getCltIncapacitatedParentCode());
    ret.setIndividualHealthCarePlanIndicator(getCltIndividualHealthCarePlanIndicator());
    ret.setCommonLastName(getCltCommonLastName());
    ret.setLimitationOnScpHealthIndicator(getCltLimitationOnScpHealthIndicator());
    ret.setLiterateCode(getCltLiterateCode());
    ret.setMaritalCohabitatnHstryIndicatorVar(getCltMaritalCohabitatnHstryIndicatorVar());
    ret.setMaritalStatusType(getCltMaritalStatusType());
    ret.setCommonMiddleName(getCltCommonMiddleName());
    ret.setMilitaryStatusCode(getCltMilitaryStatusCode());
    ret.setMotherParentalRightTermDate(getCltMotherParentalRightTermDate());
    ret.setNamePrefixDescription(getCltNamePrefixDescription());
    ret.setNameType(getCltNameType());
    ret.setOutstandingWarrantIndicator(getCltOutstandingWarrantIndicator());
    ret.setPrevCaChildrenServIndicator(getCltPrevCaChildrenServIndicator());
    ret.setPrevOtherDescription(getCltPrevOtherDescription());
    ret.setPrevRegionalCenterIndicator(getCltPrevRegionalCenterIndicator());
    ret.setPrimaryEthnicityType(getCltPrimaryEthnicityType());

    // Languages
    ret.setPrimaryLanguageType(getCltPrimaryLanguageType());
    ret.setSecondaryLanguageType(getCltSecondaryLanguageType());

    ret.setReligionType(getCltReligionType());
    ret.setSensitiveHlthInfoOnFileIndicator(getCltSensitiveHlthInfoOnFileIndicator());
    ret.setSensitivityIndicator(getCltSensitivityIndicator());
    ret.setSoc158PlacementCode(getCltSoc158PlacementCode());
    ret.setSoc158SealedClientIndicator(getCltSoc158SealedClientIndicator());
    ret.setSocialSecurityNumber(getCltSocialSecurityNumber());
    ret.setSocialSecurityNumChangedCode(getCltSocialSecurityNumChangedCode());
    ret.setSuffixTitleDescription(getCltSuffixTitleDescription());
    ret.setTribalAncestryClientIndicatorVar(getCltTribalAncestryClientIndicatorVar());
    ret.setTribalMembrshpVerifctnIndicatorVar(getCltTribalMembrshpVerifctnIndicatorVar());
    ret.setUnemployedParentCode(getCltUnemployedParentCode());
    ret.setZippyCreatedIndicator(getCltZippyCreatedIndicator());

    ret.setReplicationDate(getCltReplicationDate());
    ret.setReplicationOperation(getCltReplicationOperation());
    ret.setLastUpdatedTime(getCltLastUpdatedTime());
    return ret;
  }

  // ==============================
  // ACCESSORS:
  // ==============================

  public String getCltAdjudicatedDelinquentIndicator() {
    return cltAdjudicatedDelinquentIndicator;
  }

  public String getCltAdoptionStatusCode() {
    return cltAdoptionStatusCode;
  }

  public String getCltAlienRegistrationNumber() {
    return cltAlienRegistrationNumber;
  }

  public String getCltBirthCity() {
    return cltBirthCity;
  }

  public Short getCltBirthCountryCodeType() {
    return cltBirthCountryCodeType;
  }

  public Date getCltBirthDate() {
    return freshDate(cltBirthDate);
  }

  public String getCltBirthFacilityName() {
    return cltBirthFacilityName;
  }

  public Short getCltBirthStateCodeType() {
    return cltBirthStateCodeType;
  }

  public String getCltBirthplaceVerifiedIndicator() {
    return cltBirthplaceVerifiedIndicator;
  }

  public void setCltBirthplaceVerifiedIndicator(String cltBirthplaceVerifiedIndicator) {
    this.cltBirthplaceVerifiedIndicator = cltBirthplaceVerifiedIndicator;
  }

  public String getCltChildClientIndicatorVar() {
    return cltChildClientIndicatorVar;
  }

  public String getCltClientIndexNumber() {
    return cltClientIndexNumber;
  }

  public String getCltCommentDescription() {
    return cltCommentDescription;
  }

  public String getCltCommonFirstName() {
    return cltCommonFirstName;
  }

  public String getCltCommonLastName() {
    return cltCommonLastName;
  }

  public String getCltCommonMiddleName() {
    return cltCommonMiddleName;
  }

  public Date getCltConfidentialityActionDate() {
    return freshDate(cltConfidentialityActionDate);
  }

  public void setCltConfidentialityActionDate(Date cltConfidentialityActionDate) {
    this.cltConfidentialityActionDate = freshDate(cltConfidentialityActionDate);
  }

  public String getCltConfidentialityInEffectIndicator() {
    return cltConfidentialityInEffectIndicator;
  }

  public void setCltConfidentialityInEffectIndicator(String cltConfidentialityInEffectIndicator) {
    this.cltConfidentialityInEffectIndicator = cltConfidentialityInEffectIndicator;
  }

  public Date getCltCreationDate() {
    return freshDate(cltCreationDate);
  }

  public void setCltCreationDate(Date cltCreationDate) {
    this.cltCreationDate = freshDate(cltCreationDate);
  }

  public String getCltCurrCaChildrenServIndicator() {
    return cltCurrCaChildrenServIndicator;
  }

  public void setCltCurrCaChildrenServIndicator(String cltCurrCaChildrenServIndicator) {
    this.cltCurrCaChildrenServIndicator = cltCurrCaChildrenServIndicator;
  }

  public String getCltCurrentlyOtherDescription() {
    return cltCurrentlyOtherDescription;
  }

  public String getCltCurrentlyRegionalCenterIndicator() {
    return cltCurrentlyRegionalCenterIndicator;
  }

  public Date getCltDeathDate() {
    return freshDate(cltDeathDate);
  }

  public String getCltDeathDateVerifiedIndicator() {
    return cltDeathDateVerifiedIndicator;
  }

  public String getCltDeathPlace() {
    return cltDeathPlace;
  }

  public String getCltDeathReasonText() {
    return cltDeathReasonText;
  }

  public String getCltDriverLicenseNumber() {
    return cltDriverLicenseNumber;
  }

  public Short getCltDriverLicenseStateCodeType() {
    return cltDriverLicenseStateCodeType;
  }

  public String getCltEmailAddress() {
    return cltEmailAddress;
  }

  public String getCltEstimatedDobCode() {
    return cltEstimatedDobCode;
  }

  public String getCltEthUnableToDetReasonCode() {
    return cltEthUnableToDetReasonCode;
  }

  public String getCltGenderCode() {
    return cltGenderCode;
  }

  public String getCltHealthSummaryText() {
    return cltHealthSummaryText;
  }

  public void setCltHealthSummaryText(String cltHealthSummaryText) {
    this.cltHealthSummaryText = cltHealthSummaryText;
  }

  public String getCltHispUnableToDetReasonCode() {
    return cltHispUnableToDetReasonCode;
  }

  public void setCltHispUnableToDetReasonCode(String cltHispUnableToDetReasonCode) {
    this.cltHispUnableToDetReasonCode = cltHispUnableToDetReasonCode;
  }

  public String getCltHispanicOriginCode() {
    return cltHispanicOriginCode;
  }

  public void setCltHispanicOriginCode(String cltHispanicOriginCode) {
    this.cltHispanicOriginCode = cltHispanicOriginCode;
  }

  public Short getCltImmigrationCountryCodeType() {
    return cltImmigrationCountryCodeType;
  }

  public void setCltImmigrationCountryCodeType(Short cltImmigrationCountryCodeType) {
    this.cltImmigrationCountryCodeType = cltImmigrationCountryCodeType;
  }

  public Short getCltImmigrationStatusType() {
    return cltImmigrationStatusType;
  }

  public String getCltIncapacitatedParentCode() {
    return cltIncapacitatedParentCode;
  }

  public String getCltIndividualHealthCarePlanIndicator() {
    return cltIndividualHealthCarePlanIndicator;
  }

  public String getCltLimitationOnScpHealthIndicator() {
    return cltLimitationOnScpHealthIndicator;
  }

  public String getCltLiterateCode() {
    return cltLiterateCode;
  }

  public String getCltMaritalCohabitatnHstryIndicatorVar() {
    return cltMaritalCohabitatnHstryIndicatorVar;
  }

  public Short getCltMaritalStatusType() {
    return cltMaritalStatusType;
  }

  public String getCltMilitaryStatusCode() {
    return cltMilitaryStatusCode;
  }

  public Date getCltMotherParentalRightTermDate() {
    return freshDate(cltMotherParentalRightTermDate);
  }

  public String getCltNamePrefixDescription() {
    return cltNamePrefixDescription;
  }

  public Short getCltNameType() {
    return cltNameType;
  }

  public String getCltOutstandingWarrantIndicator() {
    return cltOutstandingWarrantIndicator;
  }

  public String getCltPrevCaChildrenServIndicator() {
    return cltPrevCaChildrenServIndicator;
  }

  public String getCltPrevOtherDescription() {
    return cltPrevOtherDescription;
  }

  public String getCltPrevRegionalCenterIndicator() {
    return cltPrevRegionalCenterIndicator;
  }

  public void setCltPrevRegionalCenterIndicator(String cltPrevRegionalCenterIndicator) {
    this.cltPrevRegionalCenterIndicator = cltPrevRegionalCenterIndicator;
  }

  public Short getCltPrimaryEthnicityType() {
    return cltPrimaryEthnicityType;
  }

  public void setCltPrimaryEthnicityType(Short cltPrimaryEthnicityType) {
    this.cltPrimaryEthnicityType = cltPrimaryEthnicityType;
  }

  public Short getCltPrimaryLanguageType() {
    return cltPrimaryLanguageType;
  }

  public void setCltPrimaryLanguageType(Short cltPrimaryLanguageType) {
    this.cltPrimaryLanguageType = cltPrimaryLanguageType;
  }

  public Short getCltReligionType() {
    return cltReligionType;
  }

  public void setCltReligionType(Short cltReligionType) {
    this.cltReligionType = cltReligionType;
  }

  public Short getCltSecondaryLanguageType() {
    return cltSecondaryLanguageType;
  }

  public void setCltSecondaryLanguageType(Short cltSecondaryLanguageType) {
    this.cltSecondaryLanguageType = cltSecondaryLanguageType;
  }

  public String getCltSensitiveHlthInfoOnFileIndicator() {
    return cltSensitiveHlthInfoOnFileIndicator;
  }

  public void setCltSensitiveHlthInfoOnFileIndicator(String cltSensitiveHlthInfoOnFileIndicator) {
    this.cltSensitiveHlthInfoOnFileIndicator = cltSensitiveHlthInfoOnFileIndicator;
  }

  public String getCltSensitivityIndicator() {
    return cltSensitivityIndicator;
  }

  public void setCltSensitivityIndicator(String cltSensitivityIndicator) {
    this.cltSensitivityIndicator = cltSensitivityIndicator;
  }

  public String getCltSoc158PlacementCode() {
    return cltSoc158PlacementCode;
  }

  public void setCltSoc158PlacementCode(String cltSoc158PlacementCode) {
    this.cltSoc158PlacementCode = cltSoc158PlacementCode;
  }

  public String getCltSoc158SealedClientIndicator() {
    return cltSoc158SealedClientIndicator;
  }

  public void setCltSoc158SealedClientIndicator(String cltSoc158SealedClientIndicator) {
    this.cltSoc158SealedClientIndicator = cltSoc158SealedClientIndicator;
  }

  public String getCltSocialSecurityNumChangedCode() {
    return cltSocialSecurityNumChangedCode;
  }

  public void setCltSocialSecurityNumChangedCode(String cltSocialSecurityNumChangedCode) {
    this.cltSocialSecurityNumChangedCode = cltSocialSecurityNumChangedCode;
  }

  public String getCltSocialSecurityNumber() {
    return cltSocialSecurityNumber;
  }

  public void setCltSocialSecurityNumber(String cltSocialSecurityNumber) {
    this.cltSocialSecurityNumber = cltSocialSecurityNumber;
  }

  public String getCltSuffixTitleDescription() {
    return cltSuffixTitleDescription;
  }

  public void setCltSuffixTitleDescription(String cltSuffixTitleDescription) {
    this.cltSuffixTitleDescription = cltSuffixTitleDescription;
  }

  public String getCltTribalAncestryClientIndicatorVar() {
    return cltTribalAncestryClientIndicatorVar;
  }

  public void setCltTribalAncestryClientIndicatorVar(String cltTribalAncestryClientIndicatorVar) {
    this.cltTribalAncestryClientIndicatorVar = cltTribalAncestryClientIndicatorVar;
  }

  public String getCltTribalMembrshpVerifctnIndicatorVar() {
    return cltTribalMembrshpVerifctnIndicatorVar;
  }

  public void setCltTribalMembrshpVerifctnIndicatorVar(
      String cltTribalMembrshpVerifctnIndicatorVar) {
    this.cltTribalMembrshpVerifctnIndicatorVar = cltTribalMembrshpVerifctnIndicatorVar;
  }

  public String getCltUnemployedParentCode() {
    return cltUnemployedParentCode;
  }

  public void setCltUnemployedParentCode(String cltUnemployedParentCode) {
    this.cltUnemployedParentCode = cltUnemployedParentCode;
  }

  public String getCltZippyCreatedIndicator() {
    return cltZippyCreatedIndicator;
  }

  public void setCltZippyCreatedIndicator(String cltZippyCreatedIndicator) {
    this.cltZippyCreatedIndicator = cltZippyCreatedIndicator;
  }

  public CmsReplicationOperation getCltReplicationOperation() {
    return cltReplicationOperation;
  }

  public void setCltReplicationOperation(CmsReplicationOperation cltReplicationOperation) {
    this.cltReplicationOperation = cltReplicationOperation;
  }

  public Date getCltReplicationDate() {
    return freshDate(cltReplicationDate);
  }

  public void setCltReplicationDate(Date cltReplicationDate) {
    this.cltReplicationDate = freshDate(cltReplicationDate);
  }

  public String getCltLastUpdatedId() {
    return cltLastUpdatedId;
  }

  public void setCltLastUpdatedId(String cltLastUpdatedId) {
    this.cltLastUpdatedId = cltLastUpdatedId;
  }

  public Date getCltLastUpdatedTime() {
    return freshDate(cltLastUpdatedTime);
  }

  public void setCltLastUpdatedTime(Date cltLastUpdatedTime) {
    this.cltLastUpdatedTime = freshDate(cltLastUpdatedTime);
  }

  public Date getCltFatherParentalRightTermDate() {
    return cltFatherParentalRightTermDate;
  }

  public void setCltFatherParentalRightTermDate(Date cltFatherParentalRightTermDate) {
    this.cltFatherParentalRightTermDate = cltFatherParentalRightTermDate;
  }

  public void setCltAdjudicatedDelinquentIndicator(String cltAdjudicatedDelinquentIndicator) {
    this.cltAdjudicatedDelinquentIndicator = cltAdjudicatedDelinquentIndicator;
  }

  public void setCltAdoptionStatusCode(String cltAdoptionStatusCode) {
    this.cltAdoptionStatusCode = cltAdoptionStatusCode;
  }

  public void setCltAlienRegistrationNumber(String cltAlienRegistrationNumber) {
    this.cltAlienRegistrationNumber = cltAlienRegistrationNumber;
  }

  public void setCltBirthCity(String cltBirthCity) {
    this.cltBirthCity = cltBirthCity;
  }

  public void setCltBirthCountryCodeType(Short cltBirthCountryCodeType) {
    this.cltBirthCountryCodeType = cltBirthCountryCodeType;
  }

  public void setCltBirthDate(Date cltBirthDate) {
    this.cltBirthDate = cltBirthDate;
  }

  public void setCltBirthFacilityName(String cltBirthFacilityName) {
    this.cltBirthFacilityName = cltBirthFacilityName;
  }

  public void setCltBirthStateCodeType(Short cltBirthStateCodeType) {
    this.cltBirthStateCodeType = cltBirthStateCodeType;
  }

  public void setCltChildClientIndicatorVar(String cltChildClientIndicatorVar) {
    this.cltChildClientIndicatorVar = cltChildClientIndicatorVar;
  }

  public void setCltClientIndexNumber(String cltClientIndexNumber) {
    this.cltClientIndexNumber = cltClientIndexNumber;
  }

  public void setCltCommentDescription(String cltCommentDescription) {
    this.cltCommentDescription = cltCommentDescription;
  }

  public void setCltCommonFirstName(String cltCommonFirstName) {
    this.cltCommonFirstName = cltCommonFirstName;
  }

  public void setCltCommonLastName(String cltCommonLastName) {
    this.cltCommonLastName = cltCommonLastName;
  }

  public void setCltCommonMiddleName(String cltCommonMiddleName) {
    this.cltCommonMiddleName = cltCommonMiddleName;
  }

  public void setCltCurrentlyOtherDescription(String cltCurrentlyOtherDescription) {
    this.cltCurrentlyOtherDescription = cltCurrentlyOtherDescription;
  }

  public void setCltCurrentlyRegionalCenterIndicator(String cltCurrentlyRegionalCenterIndicator) {
    this.cltCurrentlyRegionalCenterIndicator = cltCurrentlyRegionalCenterIndicator;
  }

  public void setCltDeathDate(Date cltDeathDate) {
    this.cltDeathDate = cltDeathDate;
  }

  public void setCltDeathDateVerifiedIndicator(String cltDeathDateVerifiedIndicator) {
    this.cltDeathDateVerifiedIndicator = cltDeathDateVerifiedIndicator;
  }

  public void setCltDeathPlace(String cltDeathPlace) {
    this.cltDeathPlace = cltDeathPlace;
  }

  public void setCltDeathReasonText(String cltDeathReasonText) {
    this.cltDeathReasonText = cltDeathReasonText;
  }

  public void setCltDriverLicenseNumber(String cltDriverLicenseNumber) {
    this.cltDriverLicenseNumber = cltDriverLicenseNumber;
  }

  public void setCltDriverLicenseStateCodeType(Short cltDriverLicenseStateCodeType) {
    this.cltDriverLicenseStateCodeType = cltDriverLicenseStateCodeType;
  }

  public void setCltEmailAddress(String cltEmailAddress) {
    this.cltEmailAddress = cltEmailAddress;
  }

  public void setCltEstimatedDobCode(String cltEstimatedDobCode) {
    this.cltEstimatedDobCode = cltEstimatedDobCode;
  }

  public void setCltEthUnableToDetReasonCode(String cltEthUnableToDetReasonCode) {
    this.cltEthUnableToDetReasonCode = cltEthUnableToDetReasonCode;
  }

  public void setCltGenderCode(String cltGenderCode) {
    this.cltGenderCode = cltGenderCode;
  }

  public void setCltImmigrationStatusType(Short cltImmigrationStatusType) {
    this.cltImmigrationStatusType = cltImmigrationStatusType;
  }

  public void setCltIncapacitatedParentCode(String cltIncapacitatedParentCode) {
    this.cltIncapacitatedParentCode = cltIncapacitatedParentCode;
  }

  public void setCltIndividualHealthCarePlanIndicator(String cltIndividualHealthCarePlanIndicator) {
    this.cltIndividualHealthCarePlanIndicator = cltIndividualHealthCarePlanIndicator;
  }

  public void setCltLimitationOnScpHealthIndicator(String cltLimitationOnScpHealthIndicator) {
    this.cltLimitationOnScpHealthIndicator = cltLimitationOnScpHealthIndicator;
  }

  public void setCltLiterateCode(String cltLiterateCode) {
    this.cltLiterateCode = cltLiterateCode;
  }

  public void setCltMaritalCohabitatnHstryIndicatorVar(
      String cltMaritalCohabitatnHstryIndicatorVar) {
    this.cltMaritalCohabitatnHstryIndicatorVar = cltMaritalCohabitatnHstryIndicatorVar;
  }

  public void setCltMaritalStatusType(Short cltMaritalStatusType) {
    this.cltMaritalStatusType = cltMaritalStatusType;
  }

  public void setCltMilitaryStatusCode(String cltMilitaryStatusCode) {
    this.cltMilitaryStatusCode = cltMilitaryStatusCode;
  }

  public void setCltMotherParentalRightTermDate(Date cltMotherParentalRightTermDate) {
    this.cltMotherParentalRightTermDate = cltMotherParentalRightTermDate;
  }

  public void setCltNamePrefixDescription(String cltNamePrefixDescription) {
    this.cltNamePrefixDescription = cltNamePrefixDescription;
  }

  public void setCltNameType(Short cltNameType) {
    this.cltNameType = cltNameType;
  }

  public void setCltOutstandingWarrantIndicator(String cltOutstandingWarrantIndicator) {
    this.cltOutstandingWarrantIndicator = cltOutstandingWarrantIndicator;
  }

  public void setCltPrevCaChildrenServIndicator(String cltPrevCaChildrenServIndicator) {
    this.cltPrevCaChildrenServIndicator = cltPrevCaChildrenServIndicator;
  }

  public void setCltPrevOtherDescription(String cltPrevOtherDescription) {
    this.cltPrevOtherDescription = cltPrevOtherDescription;
  }

  public void addClientAddress(RawClientAddress cla) {
    clientAddress.put(cla.getClaId(), cla);
  }

  public void addClientCounty(RawClientCounty c) {
    clientCounty.add(c);
  }

  public void addCsec(RawCsec c) {
    csec.add(c);
  }

  public void addCase(RawCase c) {
    cases.add(c);
  }

  public void addAka(RawAka c) {
    aka.add(c);
  }

  public void addEthnicity(RawEthnicity c) {
    ethnicity.add(c);
  }

  public void addSafetyAlert(RawSafetyAlert c) {
    safetyAlert.add(c);
  }

  public Map<String, RawClientAddress> getClientAddress() {
    return clientAddress;
  }

  public List<RawClientCounty> getClientCounty() {
    return clientCounty;
  }

  public List<RawCsec> getCsec() {
    return csec;
  }

  public List<RawAka> getAka() {
    return aka;
  }

  public List<RawEthnicity> getEthnicity() {
    return ethnicity;
  }

  public List<RawSafetyAlert> getSafetyAlert() {
    return safetyAlert;
  }

  public List<RawCase> getCases() {
    return cases;
  }

  public PlacementHomeAddress getPlacementHomeAddress() {
    return placementHomeAddress;
  }

  public void setPlacementHomeAddress(PlacementHomeAddress placementHomeAddress) {
    this.placementHomeAddress = placementHomeAddress;
  }

}
