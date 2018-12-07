package gov.ca.cwds.data.persistence.cms.client;

import static gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils.freshDate;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Type;

import gov.ca.cwds.data.persistence.cms.PlacementHomeAddress;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiGroupNormalizer;

public class RawClient extends ClientReference implements NeutronJdbcReader<RawClient>,
    ApiGroupNormalizer<ReplicatedClient>, Comparable<RawClient>, Comparator<RawClient> {

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

    this.cltSensitivityIndicator = trimToNull(rs.getString(ColPos.CLT_SENSTV_IND.ordinal()));
    this.cltSoc158SealedClientIndicator = trimToNull(rs.getString(ColPos.CLT_SOC158_IND.ordinal()));
    this.cltBirthDate = rs.getDate(ColPos.CLT_BIRTH_DT.ordinal());
    this.cltClientIndexNumber = trimToNull(rs.getString(ColPos.CLT_CL_INDX_NO.ordinal()));
    this.cltCommonFirstName = (rs.getString(ColPos.CLT_COM_FST_NM.ordinal()));
    this.cltCommonLastName = trimToNull(rs.getString(ColPos.CLT_COM_LST_NM.ordinal()));
    this.cltCommonMiddleName = trimToNull(rs.getString(ColPos.CLT_COM_MID_NM.ordinal()));
    this.cltCreationDate = rs.getDate(ColPos.CLT_CREATN_DT.ordinal());
    this.cltDeathDate = rs.getDate(ColPos.CLT_DEATH_DT.ordinal());
    this.cltDeathDateVerifiedIndicator = trimToNull(rs.getString(ColPos.CLT_DTH_DT_IND.ordinal()));
    this.cltDriverLicenseNumber = trimToNull(rs.getString(ColPos.CLT_DRV_LIC_NO.ordinal()));
    this.cltDriverLicenseStateCodeType = rs.getShort(ColPos.CLT_D_STATE_C.ordinal());
    this.cltEmailAddress = trimToNull(rs.getString(ColPos.CLT_EMAIL_ADDR.ordinal()));
    this.cltEthUnableToDetReasonCode = trimToNull(rs.getString(ColPos.CLT_ETH_UD_CD.ordinal()));
    this.cltGenderCode = trimToNull(rs.getString(ColPos.CLT_GENDER_CD.ordinal()));
    this.cltHispUnableToDetReasonCode = trimToNull(rs.getString(ColPos.CLT_HISP_UD_CD.ordinal()));
    this.cltHispanicOriginCode = trimToNull(rs.getString(ColPos.CLT_HISP_CD.ordinal()));
    this.cltImmigrationStatusType = rs.getShort(ColPos.CLT_IMGT_STC.ordinal());
    this.cltLiterateCode = trimToNull(rs.getString(ColPos.CLT_LITRATE_CD.ordinal()));
    this.cltMaritalStatusType = rs.getShort(ColPos.CLT_MRTL_STC.ordinal());
    this.cltMilitaryStatusCode = trimToNull(rs.getString(ColPos.CLT_MILT_STACD.ordinal()));
    this.cltNamePrefixDescription = trimToNull(rs.getString(ColPos.CLT_NMPRFX_DSC.ordinal()));
    this.cltNameType = rs.getShort(ColPos.CLT_NAME_TPC.ordinal());

    this.cltPrimaryEthnicityType = rs.getShort(ColPos.CLT_P_ETHNCTYC.ordinal());
    this.cltPrimaryLanguageType = rs.getShort(ColPos.CLT_P_LANG_TPC.ordinal());
    this.cltSecondaryLanguageType = rs.getShort(ColPos.CLT_S_LANG_TC.ordinal());
    this.cltReligionType = rs.getShort(ColPos.CLT_RLGN_TPC.ordinal());

    this.cltSensitiveHlthInfoOnFileIndicator =
        trimToNull(rs.getString(ColPos.CLT_SNTV_HLIND.ordinal()));
    this.cltSoc158PlacementCode = trimToNull(rs.getString(ColPos.CLT_SOCPLC_CD.ordinal()));
    this.cltSocialSecurityNumChangedCode =
        trimToNull(rs.getString(ColPos.CLT_SSN_CHG_CD.ordinal()));
    this.cltSocialSecurityNumber = trimToNull(rs.getString(ColPos.CLT_SS_NO.ordinal()));
    this.cltSuffixTitleDescription = trimToNull(rs.getString(ColPos.CLT_SUFX_TLDSC.ordinal()));
    this.cltTribalAncestryClientIndicatorVar =
        trimToNull(rs.getString(ColPos.CLT_TRBA_CLT_B.ordinal()));
    this.cltZippyCreatedIndicator = trimToNull(rs.getString(ColPos.CLT_ZIPPY_IND.ordinal()));

    this.cltLastUpdatedId = trimToNull(rs.getString(ColPos.CLT_LST_UPD_ID.ordinal()));
    this.cltLastUpdatedTime = rs.getTimestamp(ColPos.CLT_LST_UPD_TS.ordinal());

    this.setCltReplicationOperation(
        CmsReplicationOperation.strToRepOp(rs.getString(ColPos.CLT_IBMSNAP_OPERATION.ordinal())));
    this.setCltReplicationDate(rs.getDate(ColPos.CLT_IBMSNAP_LOGMARKER.ordinal()));

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

  @Override
  public ReplicatedClient normalize(Map<Object, ReplicatedClient> ignoreMe) {
    return normalize(new RawToEsConverter());
  }

  public ReplicatedClient normalize(RawToEsConverter conv) {
    return conv.convert(this);
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
    return freshDate(cltFatherParentalRightTermDate);
  }

  public void setCltFatherParentalRightTermDate(Date cltFatherParentalRightTermDate) {
    this.cltFatherParentalRightTermDate = freshDate(cltFatherParentalRightTermDate);
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
    this.cltBirthDate = freshDate(cltBirthDate);
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
    this.cltDeathDate = freshDate(cltDeathDate);
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
    this.cltMotherParentalRightTermDate = freshDate(cltMotherParentalRightTermDate);
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

  @Override
  public int compare(RawClient o1, RawClient o2) {
    return StringUtils.trimToEmpty(o1.getCltId()).compareTo(StringUtils.trimToEmpty(o2.getCltId()));
  }

  @Override
  public int compareTo(RawClient o) {
    return compare(this, o);
  }

}
