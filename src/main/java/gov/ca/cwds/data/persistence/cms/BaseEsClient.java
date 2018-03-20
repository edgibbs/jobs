package gov.ca.cwds.data.persistence.cms;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Type;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClientAddress;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils;

@MappedSuperclass
public abstract class BaseEsClient
    implements PersistentObject, ApiGroupNormalizer<ReplicatedClient> {

  private static final long serialVersionUID = 1L;

  @Type(type = "timestamp")
  @Column(name = "LAST_CHG", updatable = false)
  protected Date lastChange;

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

  @Id
  @Column(name = "CLT_IDENTIFIER")
  protected String cltId;

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

  // =======================
  // CL_ADDRT: (address)
  // =======================

  @Enumerated(EnumType.STRING)
  @Column(name = "CLA_IBMSNAP_OPERATION", updatable = false)
  protected CmsReplicationOperation claReplicationOperation;

  @Type(type = "timestamp")
  @Column(name = "CLA_IBMSNAP_LOGMARKER", updatable = false)
  protected Date claReplicationDate;

  @Column(name = "CLA_LST_UPD_ID")
  protected String claLastUpdatedId;

  @Type(type = "timestamp")
  @Column(name = "CLA_LST_UPD_TS")
  protected Date claLastUpdatedTime;

  @Id
  @Column(name = "CLA_IDENTIFIER")
  protected String claId;

  @Column(name = "CLA_FKADDRS_T")
  protected String claFkAddress;

  @Column(name = "CLA_FKCLIENT_T")
  protected String claFkClient;

  @Column(name = "CLA_FKREFERL_T")
  protected String claFkReferral;

  @Type(type = "short")
  @Column(name = "CLA_ADDR_TPC")
  protected Short claAddressType;

  @Column(name = "CLA_HOMLES_IND")
  protected String claHomelessInd;

  @Column(name = "CLA_BK_INMT_ID")
  protected String claBkInmtId;

  @Type(type = "date")
  @Column(name = "CLA_EFF_END_DT")
  protected Date claEffectiveEndDate;

  @Type(type = "date")
  @Column(name = "CLA_EFF_STRTDT")
  protected Date claEffectiveStartDate;

  // =======================
  // ADDRS_T: (address)
  // =======================

  @Id
  @Column(name = "ADR_IDENTIFIER")
  protected String adrId;

  @Enumerated(EnumType.STRING)
  @Column(name = "ADR_IBMSNAP_OPERATION", updatable = false)
  protected CmsReplicationOperation adrReplicationOperation;

  @Type(type = "timestamp")
  @Column(name = "ADR_IBMSNAP_LOGMARKER", updatable = false)
  protected Date adrReplicationDate;

  @Column(name = "ADR_CITY_NM")
  protected String adrCity;

  @Column(name = "ADR_EMRG_TELNO")
  protected Long adrEmergencyNumber;

  @Type(type = "integer")
  @Column(name = "ADR_EMRG_EXTNO")
  protected Integer adrEmergencyExtension;

  @Column(name = "ADR_FRG_ADRT_B")
  protected String adrFrgAdrtB;

  @Type(type = "short")
  @Column(name = "ADR_GVR_ENTC")
  protected Short adrGovernmentEntityCd;

  @Column(name = "ADR_MSG_TEL_NO")
  protected Long adrMessageNumber;

  @Type(type = "integer")
  @Column(name = "ADR_MSG_EXT_NO")
  protected Integer adrMessageExtension;

  @Column(name = "ADR_HEADER_ADR")
  protected String adrHeaderAddress;

  @Column(name = "ADR_PRM_TEL_NO")
  protected Long adrPrimaryNumber;

  @Type(type = "integer")
  @Column(name = "ADR_PRM_EXT_NO")
  protected Integer adrPrimaryExtension;

  @Type(type = "short")
  @Column(name = "ADR_STATE_C")
  protected Short adrState;

  @Column(name = "ADR_STREET_NM")
  @ColumnTransformer(read = "trim(ADR_STREET_NM)")
  protected String adrStreetName;

  @Column(name = "ADR_STREET_NO")
  protected String adrStreetNumber;

  @Column(name = "ADR_ZIP_NO")
  protected String adrZip;

  @Column(name = "ADR_ADDR_DSC")
  protected String adrAddressDescription;

  @Type(type = "short")
  @Column(name = "ADR_ZIP_SFX_NO")
  protected Short adrZip4;

  @Column(name = "ADR_POSTDIR_CD")
  protected String adrPostDirCd;

  @Column(name = "ADR_PREDIR_CD")
  protected String adrPreDirCd;

  @Type(type = "short")
  @Column(name = "ADR_ST_SFX_C")
  protected Short adrStreetSuffixCd;

  @Type(type = "short")
  @Column(name = "ADR_UNT_DSGC")
  protected Short adrUnitDesignationCd;

  @Column(name = "ADR_UNIT_NO")
  protected String adrUnitNumber;

  // ================================
  // CLIENT_CNTY: (client county)
  // ================================

  @Id
  @Column(name = "CLC_CLIENT_ID")
  protected String clientCountyId;

  @Type(type = "short")
  @Column(name = "CLC_GVR_ENTC")
  protected Short clientCounty;

  @Column(name = "CLC_CNTY_RULE")
  protected String clientCountyRule;

  // ================================
  // CLSCP_ET: (race & ethnicity)
  // ================================

  @Id
  @Column(name = "ETH_IDENTIFIER")
  protected String clientEthnicityId;

  @Type(type = "short")
  @Column(name = "ETHNICITY_CODE")
  protected Short clientEthnicityCode;

  public static void extract(final BaseEsClient ret, final ResultSet rs) throws SQLException {
    ret.cltSensitivityIndicator = ifNull(rs.getString("CLT_SENSTV_IND"));
    ret.cltSoc158SealedClientIndicator = ifNull(rs.getString("CLT_SOC158_IND"));
    ret.cltAdjudicatedDelinquentIndicator = ifNull(rs.getString("CLT_ADJDEL_IND"));
    ret.cltAdoptionStatusCode = rs.getString("CLT_ADPTN_STCD");
    ret.cltAlienRegistrationNumber = ifNull(rs.getString("CLT_ALN_REG_NO"));
    ret.cltBirthCity = ifNull(rs.getString("CLT_BIRTH_CITY"));
    ret.cltBirthCountryCodeType = rs.getShort("CLT_B_CNTRY_C");
    ret.cltBirthDate = rs.getDate("CLT_BIRTH_DT");
    ret.cltBirthFacilityName = ifNull(rs.getString("CLT_BR_FAC_NM"));
    ret.cltBirthStateCodeType = rs.getShort("CLT_B_STATE_C");
    ret.cltBirthplaceVerifiedIndicator = ifNull(rs.getString("CLT_BP_VER_IND"));
    ret.cltChildClientIndicatorVar = ifNull(rs.getString("CLT_CHLD_CLT_B"));
    ret.cltClientIndexNumber = ifNull(rs.getString("CLT_CL_INDX_NO"));
    ret.cltCommentDescription = ifNull(rs.getString("CLT_COMMNT_DSC"));
    ret.cltCommonFirstName = (rs.getString("CLT_COM_FST_NM"));
    ret.cltCommonLastName = ifNull(rs.getString("CLT_COM_LST_NM"));
    ret.cltCommonMiddleName = ifNull(rs.getString("CLT_COM_MID_NM"));
    ret.cltConfidentialityActionDate = rs.getDate("CLT_CONF_ACTDT");
    ret.cltConfidentialityInEffectIndicator = ifNull(rs.getString("CLT_CONF_EFIND"));
    ret.cltCreationDate = rs.getDate("CLT_CREATN_DT");
    ret.cltCurrCaChildrenServIndicator = ifNull(rs.getString("CLT_CURRCA_IND"));
    ret.cltCurrentlyOtherDescription = rs.getString("CLT_COTH_DESC");
    ret.cltCurrentlyRegionalCenterIndicator = ifNull(rs.getString("CLT_CURREG_IND"));
    ret.cltDeathDate = rs.getDate("CLT_DEATH_DT");
    ret.cltDeathDateVerifiedIndicator = ifNull(rs.getString("CLT_DTH_DT_IND"));
    ret.cltDeathPlace = ifNull(rs.getString("CLT_DEATH_PLC"));
    ret.cltDeathReasonText = ifNull(rs.getString("CLT_DTH_RN_TXT"));
    ret.cltDriverLicenseNumber = ifNull(rs.getString("CLT_DRV_LIC_NO"));
    ret.cltDriverLicenseStateCodeType = rs.getShort("CLT_D_STATE_C");
    ret.cltEmailAddress = ifNull(rs.getString("CLT_EMAIL_ADDR"));
    ret.cltEstimatedDobCode = ifNull(rs.getString("CLT_EST_DOB_CD"));
    ret.cltEthUnableToDetReasonCode = ifNull(rs.getString("CLT_ETH_UD_CD"));
    ret.cltFatherParentalRightTermDate = rs.getDate("CLT_FTERM_DT");
    ret.cltGenderCode = ifNull(rs.getString("CLT_GENDER_CD"));
    ret.cltHealthSummaryText = ifNull(rs.getString("CLT_HEALTH_TXT"));
    ret.cltHispUnableToDetReasonCode = ifNull(rs.getString("CLT_HISP_UD_CD"));
    ret.cltHispanicOriginCode = ifNull(rs.getString("CLT_HISP_CD"));
    ret.cltId = ifNull(rs.getString("CLT_IDENTIFIER"));
    ret.cltImmigrationCountryCodeType = rs.getShort("CLT_I_CNTRY_C");
    ret.cltImmigrationStatusType = rs.getShort("CLT_IMGT_STC");
    ret.cltIncapacitatedParentCode = ifNull(rs.getString("CLT_INCAPC_CD"));
    ret.cltIndividualHealthCarePlanIndicator = ifNull(rs.getString("CLT_HCARE_IND"));
    ret.cltLimitationOnScpHealthIndicator = ifNull(rs.getString("CLT_LIMIT_IND"));
    ret.cltLiterateCode = ifNull(rs.getString("CLT_LITRATE_CD"));
    ret.cltMaritalCohabitatnHstryIndicatorVar = ifNull(rs.getString("CLT_MAR_HIST_B"));
    ret.cltMaritalStatusType = rs.getShort("CLT_MRTL_STC");
    ret.cltMilitaryStatusCode = ifNull(rs.getString("CLT_MILT_STACD"));
    ret.cltMotherParentalRightTermDate = rs.getDate("CLT_MTERM_DT");
    ret.cltNamePrefixDescription = ifNull(rs.getString("CLT_NMPRFX_DSC"));
    ret.cltNameType = rs.getShort("CLT_NAME_TPC");
    ret.cltOutstandingWarrantIndicator = ifNull(rs.getString("CLT_OUTWRT_IND"));
    ret.cltPrevCaChildrenServIndicator = ifNull(rs.getString("CLT_PREVCA_IND"));
    ret.cltPrevOtherDescription = ifNull(rs.getString("CLT_POTH_DESC"));
    ret.cltPrevRegionalCenterIndicator = ifNull(rs.getString("CLT_PREREG_IND"));
    ret.cltPrimaryEthnicityType = rs.getShort("CLT_P_ETHNCTYC");

    ret.clientEthnicityCode = rs.getShort("ETHNICITY_CODE");
    ret.clientCounty = rs.getShort("CLC_GVR_ENTC");

    ret.clientCountyId = ifNull(rs.getString("CLC_CLIENT_ID"));
    ret.clientEthnicityId = ifNull(rs.getString("ETHNICITY_CODE"));

    // Languages
    ret.cltPrimaryLanguageType = rs.getShort("CLT_P_LANG_TPC");
    ret.cltSecondaryLanguageType = rs.getShort("CLT_S_LANG_TC");

    ret.cltReligionType = rs.getShort("CLT_RLGN_TPC");
    ret.cltSensitiveHlthInfoOnFileIndicator = ifNull(rs.getString("CLT_SNTV_HLIND"));
    ret.cltSoc158PlacementCode = ifNull(rs.getString("CLT_SOCPLC_CD"));
    ret.cltSocialSecurityNumChangedCode = ifNull(rs.getString("CLT_SSN_CHG_CD"));
    ret.cltSocialSecurityNumber = ifNull(rs.getString("CLT_SS_NO"));
    ret.cltSuffixTitleDescription = ifNull(rs.getString("CLT_SUFX_TLDSC"));
    ret.cltTribalAncestryClientIndicatorVar = ifNull(rs.getString("CLT_TRBA_CLT_B"));
    ret.cltTribalMembrshpVerifctnIndicatorVar = ifNull(rs.getString("CLT_TR_MBVRT_B"));
    ret.cltUnemployedParentCode = ifNull(rs.getString("CLT_UNEMPLY_CD"));
    ret.cltZippyCreatedIndicator = ifNull(rs.getString("CLT_ZIPPY_IND"));
    ret.cltLastUpdatedId = ifNull(rs.getString("CLT_LST_UPD_ID"));
    ret.cltLastUpdatedTime = rs.getTimestamp("CLT_LST_UPD_TS");

    ret.setCltReplicationOperation(
        CmsReplicationOperation.strToRepOp(rs.getString("CLT_IBMSNAP_OPERATION")));
    ret.setCltReplicationDate(rs.getDate("CLT_IBMSNAP_LOGMARKER"));

    ret.claLastUpdatedId = ifNull(rs.getString("CLA_LST_UPD_ID"));
    ret.claLastUpdatedTime = rs.getTimestamp("CLA_LST_UPD_TS");
    ret.claId = ifNull(rs.getString("CLA_IDENTIFIER"));
    ret.claFkAddress = ifNull(rs.getString("CLA_FKADDRS_T"));
    ret.claFkClient = ifNull(rs.getString("CLA_FKCLIENT_T"));
    ret.claFkReferral = ifNull(rs.getString("CLA_FKREFERL_T"));
    ret.claAddressType = rs.getShort("CLA_ADDR_TPC");
    ret.claHomelessInd = ifNull(rs.getString("CLA_HOMLES_IND"));
    ret.claBkInmtId = ifNull(rs.getString("CLA_BK_INMT_ID"));
    ret.claEffectiveEndDate = rs.getDate("CLA_EFF_END_DT");
    ret.claEffectiveStartDate = rs.getDate("CLA_EFF_STRTDT");

    ret.setClaReplicationOperation(
        CmsReplicationOperation.strToRepOp(rs.getString("CLA_IBMSNAP_OPERATION")));
    ret.setClaReplicationDate(rs.getDate("CLA_IBMSNAP_LOGMARKER"));

    ret.adrId = ifNull(rs.getString("ADR_IDENTIFIER"));
    ret.adrCity = ifNull(rs.getString("ADR_CITY_NM"));
    ret.adrEmergencyNumber = rs.getLong("ADR_EMRG_TELNO");
    ret.adrEmergencyExtension = rs.getInt("ADR_EMRG_EXTNO");
    ret.adrFrgAdrtB = ifNull(rs.getString("ADR_FRG_ADRT_B"));
    ret.adrGovernmentEntityCd = rs.getShort("ADR_GVR_ENTC");
    ret.adrMessageNumber = rs.getLong("ADR_MSG_TEL_NO");
    ret.adrMessageExtension = rs.getInt("ADR_MSG_EXT_NO");
    ret.adrHeaderAddress = ifNull(rs.getString("ADR_HEADER_ADR"));
    ret.adrPrimaryNumber = rs.getLong("ADR_PRM_TEL_NO");
    ret.adrPrimaryExtension = rs.getInt("ADR_PRM_EXT_NO");
    ret.adrState = rs.getShort("ADR_STATE_C");
    ret.adrStreetName = ifNull(rs.getString("ADR_STREET_NM"));
    ret.adrStreetNumber = ifNull(rs.getString("ADR_STREET_NO"));
    ret.adrZip = ifNull(rs.getString("ADR_ZIP_NO"));
    ret.adrAddressDescription = ifNull(rs.getString("ADR_ADDR_DSC"));
    ret.adrZip4 = rs.getShort("ADR_ZIP_SFX_NO");
    ret.adrPostDirCd = ifNull(rs.getString("ADR_POSTDIR_CD"));
    ret.adrPreDirCd = ifNull(rs.getString("ADR_PREDIR_CD"));
    ret.adrStreetSuffixCd = rs.getShort("ADR_ST_SFX_C");
    ret.adrUnitDesignationCd = rs.getShort("ADR_UNT_DSGC");
    ret.adrUnitNumber = ifNull(rs.getString("ADR_UNIT_NO"));

    ret.adrReplicationOperation =
        CmsReplicationOperation.strToRepOp(rs.getString("ADR_IBMSNAP_OPERATION"));
    ret.adrReplicationDate = rs.getDate("ADR_IBMSNAP_LOGMARKER");
  }

  @Override
  public ReplicatedClient normalize(Map<Object, ReplicatedClient> map) {
    final boolean isClientAdded = map.containsKey(this.cltId);
    final ReplicatedClient ret = isClientAdded ? map.get(this.cltId) : new ReplicatedClient();

    if (!isClientAdded) {
      // Populate core client attributes.
      ret.adjudicatedDelinquentIndicator = getCltAdjudicatedDelinquentIndicator();
      ret.adoptionStatusCode = getCltAdoptionStatusCode();
      ret.alienRegistrationNumber = getCltAlienRegistrationNumber();
      ret.birthCity = getCltBirthCity();
      ret.birthCountryCodeType = getCltBirthCountryCodeType();
      ret.birthDate = getCltBirthDate();
      ret.birthFacilityName = getCltBirthFacilityName();
      ret.birthplaceVerifiedIndicator = getCltBirthplaceVerifiedIndicator();
      ret.birthStateCodeType = getCltBirthStateCodeType();
      ret.childClientIndicatorVar = getCltChildClientIndicatorVar();
      ret.clientIndexNumber = getCltClientIndexNumber();
      ret.commentDescription = getCltCommentDescription();
      ret.commonFirstName = getCltCommonFirstName();
      ret.commonLastName = getCltCommonLastName();
      ret.commonMiddleName = getCltCommonMiddleName();
      ret.confidentialityActionDate = getCltConfidentialityActionDate();
      ret.confidentialityInEffectIndicator = getCltConfidentialityInEffectIndicator();
      ret.creationDate = getCltCreationDate();
      ret.currCaChildrenServIndicator = getCltCurrCaChildrenServIndicator();
      ret.currentlyOtherDescription = getCltCurrentlyOtherDescription();
      ret.currentlyRegionalCenterIndicator = getCltCurrentlyRegionalCenterIndicator();
      ret.deathDate = getCltDeathDate();
      ret.deathDateVerifiedIndicator = getCltDeathDateVerifiedIndicator();
      ret.deathPlace = getCltDeathPlace();
      ret.deathReasonText = getCltDeathReasonText();
      ret.driverLicenseNumber = getCltDriverLicenseNumber();
      ret.driverLicenseStateCodeType = getCltDriverLicenseStateCodeType();
      ret.emailAddress = getCltEmailAddress();
      ret.estimatedDobCode = getCltEstimatedDobCode();
      ret.ethUnableToDetReasonCode = getCltEthUnableToDetReasonCode();
      ret.fatherParentalRightTermDate = cltFatherParentalRightTermDate;
      ret.commonFirstName = getCltCommonFirstName();
      ret.genderCode = getCltGenderCode();
      ret.healthSummaryText = getCltHealthSummaryText();
      ret.hispanicOriginCode = getCltHispanicOriginCode();
      ret.hispUnableToDetReasonCode = getCltHispUnableToDetReasonCode();
      ret.id = getCltId();
      ret.immigrationCountryCodeType = getCltImmigrationCountryCodeType();
      ret.immigrationStatusType = getCltImmigrationStatusType();
      ret.incapacitatedParentCode = getCltIncapacitatedParentCode();
      ret.individualHealthCarePlanIndicator = getCltIndividualHealthCarePlanIndicator();
      ret.commonLastName = getCltCommonLastName();
      ret.limitationOnScpHealthIndicator = getCltLimitationOnScpHealthIndicator();
      ret.literateCode = getCltLiterateCode();
      ret.maritalCohabitatnHstryIndicatorVar = getCltMaritalCohabitatnHstryIndicatorVar();
      ret.maritalStatusType = getCltMaritalStatusType();
      ret.commonMiddleName = getCltCommonMiddleName();
      ret.militaryStatusCode = getCltMilitaryStatusCode();
      ret.motherParentalRightTermDate = getCltMotherParentalRightTermDate();
      ret.namePrefixDescription = getCltNamePrefixDescription();
      ret.nameType = getCltNameType();
      ret.outstandingWarrantIndicator = getCltOutstandingWarrantIndicator();
      ret.prevCaChildrenServIndicator = getCltPrevCaChildrenServIndicator();
      ret.prevOtherDescription = getCltPrevOtherDescription();
      ret.prevRegionalCenterIndicator = getCltPrevRegionalCenterIndicator();
      ret.primaryEthnicityType = getCltPrimaryEthnicityType();

      // Languages
      ret.primaryLanguageType = getCltPrimaryLanguageType();
      ret.secondaryLanguageType = getCltSecondaryLanguageType();

      ret.religionType = getCltReligionType();
      ret.sensitiveHlthInfoOnFileIndicator = getCltSensitiveHlthInfoOnFileIndicator();
      ret.sensitivityIndicator = getCltSensitivityIndicator();
      ret.soc158PlacementCode = getCltSoc158PlacementCode();
      ret.soc158SealedClientIndicator = getCltSoc158SealedClientIndicator();
      ret.socialSecurityNumber = getCltSocialSecurityNumber();
      ret.socialSecurityNumChangedCode = getCltSocialSecurityNumChangedCode();
      ret.suffixTitleDescription = getCltSuffixTitleDescription();
      ret.tribalAncestryClientIndicatorVar = getCltTribalAncestryClientIndicatorVar();
      ret.tribalMembrshpVerifctnIndicatorVar = getCltTribalMembrshpVerifctnIndicatorVar();
      ret.unemployedParentCode = getCltUnemployedParentCode();
      ret.zippyCreatedIndicator = getCltZippyCreatedIndicator();

      ret.setReplicationDate(getCltReplicationDate());
      ret.setReplicationOperation(getCltReplicationOperation());
      ret.setLastUpdatedTime(getCltLastUpdatedTime());
    }

    // Client Address:
    if (StringUtils.isNotBlank(getClaId())
        && CmsReplicationOperation.D != getClaReplicationOperation()) {
      final ReplicatedClientAddress rca = new ReplicatedClientAddress();
      rca.setId(getClaId());
      rca.setAddressType(getClaAddressType());
      rca.setBkInmtId(getClaBkInmtId());
      rca.setEffEndDt(getClaEffectiveEndDate());
      rca.setEffStartDt(getClaEffectiveStartDate());
      rca.setFkAddress(getClaFkAddress());
      rca.setFkClient(getClaFkClient());
      rca.setFkReferral(getClaFkReferral());
      rca.setHomelessInd(getClaHomelessInd());

      rca.setReplicationDate(getClaReplicationDate());
      rca.setReplicationOperation(getClaReplicationOperation());
      rca.setLastUpdatedId(getClaLastUpdatedId());
      rca.setLastUpdatedTime(getClaLastUpdatedTime());
      ret.addClientAddress(rca);

      // Address proper:
      if (StringUtils.isNotBlank(getAdrId())
          && CmsReplicationOperation.D != getAdrReplicationOperation()) {
        final ReplicatedAddress adr = new ReplicatedAddress();
        adr.setId(getAdrId());
        adr.setAddressDescription(getAdrAddressDescription());
        adr.setCity(getAdrCity());

        adr.setFrgAdrtB(getAdrFrgAdrtB());
        adr.setGovernmentEntityCd(getAdrGovernmentEntityCd());
        adr.setHeaderAddress(getAdrHeaderAddress());

        adr.setPostDirCd(getAdrPostDirCd());
        adr.setPreDirCd(getAdrPreDirCd());

        // NOTE: no way to figure out phone type from "primary phone". Home? Work? Cell? dunno.
        adr.setPrimaryExtension(getAdrPrimaryExtension());
        adr.setPrimaryNumber(getAdrPrimaryNumber());

        adr.setEmergencyExtension(getAdrEmergencyExtension());
        adr.setEmergencyNumber(getAdrEmergencyNumber());

        // This is *likely* a cell phone but *not guaranteed*.
        adr.setMessageExtension(getAdrMessageExtension());
        adr.setMessageNumber(getAdrMessageNumber());

        adr.setState(getAdrState());
        adr.setStateCd(getAdrState());
        adr.setStreetName(getAdrStreetName());
        adr.setStreetNumber(getAdrStreetNumber());
        adr.setStreetSuffixCd(getAdrStreetSuffixCd());
        adr.setUnitDesignationCd(getAdrUnitDesignationCd());
        adr.setUnitNumber(getAdrUnitNumber());
        adr.setZip(getAdrZip());
        adr.setZip4(getAdrZip4());

        adr.setReplicationDate(getAdrReplicationDate());
        adr.setReplicationOperation(getAdrReplicationOperation());
        adr.setLastUpdatedId(getClaLastUpdatedId());
        adr.setLastUpdatedTime(getClaLastUpdatedTime());
        rca.addAddress(adr);
      }
    }

    // Client races
    ret.addClientRace(this.clientEthnicityCode);

    // Add client county
    ret.addClientCounty(getClientCounty());

    map.put(ret.getId(), ret);
    return ret;
  }

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
    return NeutronDateUtils.freshDate(cltBirthDate);
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
    return NeutronDateUtils.freshDate(cltConfidentialityActionDate);
  }

  public void setCltConfidentialityActionDate(Date cltConfidentialityActionDate) {
    this.cltConfidentialityActionDate = NeutronDateUtils.freshDate(cltConfidentialityActionDate);
  }

  public String getCltConfidentialityInEffectIndicator() {
    return cltConfidentialityInEffectIndicator;
  }

  public void setCltConfidentialityInEffectIndicator(String cltConfidentialityInEffectIndicator) {
    this.cltConfidentialityInEffectIndicator = cltConfidentialityInEffectIndicator;
  }

  public Date getCltCreationDate() {
    return NeutronDateUtils.freshDate(cltCreationDate);
  }

  public void setCltCreationDate(Date cltCreationDate) {
    this.cltCreationDate = NeutronDateUtils.freshDate(cltCreationDate);
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
    return NeutronDateUtils.freshDate(cltDeathDate);
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

  public String getCltId() {
    return cltId;
  }

  public void setCltId(String cltId) {
    this.cltId = cltId;
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
    return NeutronDateUtils.freshDate(cltMotherParentalRightTermDate);
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
    return NeutronDateUtils.freshDate(cltReplicationDate);
  }

  public void setCltReplicationDate(Date cltReplicationDate) {
    this.cltReplicationDate = NeutronDateUtils.freshDate(cltReplicationDate);
  }

  public String getCltLastUpdatedId() {
    return cltLastUpdatedId;
  }

  public void setCltLastUpdatedId(String cltLastUpdatedId) {
    this.cltLastUpdatedId = cltLastUpdatedId;
  }

  public Date getCltLastUpdatedTime() {
    return NeutronDateUtils.freshDate(cltLastUpdatedTime);
  }

  public void setCltLastUpdatedTime(Date cltLastUpdatedTime) {
    this.cltLastUpdatedTime = NeutronDateUtils.freshDate(cltLastUpdatedTime);
  }

  public CmsReplicationOperation getClaReplicationOperation() {
    return claReplicationOperation;
  }

  public void setClaReplicationOperation(CmsReplicationOperation claReplicationOperation) {
    this.claReplicationOperation = claReplicationOperation;
  }

  public Date getClaReplicationDate() {
    return NeutronDateUtils.freshDate(claReplicationDate);
  }

  public void setClaReplicationDate(Date claReplicationDate) {
    this.claReplicationDate = NeutronDateUtils.freshDate(claReplicationDate);
  }

  public String getClaLastUpdatedId() {
    return claLastUpdatedId;
  }

  public void setClaLastUpdatedId(String claLastUpdatedId) {
    this.claLastUpdatedId = claLastUpdatedId;
  }

  public Date getClaLastUpdatedTime() {
    return NeutronDateUtils.freshDate(claLastUpdatedTime);
  }

  public void setClaLastUpdatedTime(Date claLastUpdatedTime) {
    this.claLastUpdatedTime = NeutronDateUtils.freshDate(claLastUpdatedTime);
  }

  public String getClaFkAddress() {
    return claFkAddress;
  }

  public void setClaFkAddress(String claFkAddress) {
    this.claFkAddress = claFkAddress;
  }

  public String getClaFkClient() {
    return claFkClient;
  }

  public void setClaFkClient(String claFkClient) {
    this.claFkClient = claFkClient;
  }

  public String getClaFkReferral() {
    return claFkReferral;
  }

  public void setClaFkReferral(String claFkReferral) {
    this.claFkReferral = claFkReferral;
  }

  public Short getClaAddressType() {
    return claAddressType;
  }

  public void setClaAddressType(Short claAddressType) {
    this.claAddressType = claAddressType;
  }

  public String getClaHomelessInd() {
    return claHomelessInd;
  }

  public void setClaHomelessInd(String claHomelessInd) {
    this.claHomelessInd = claHomelessInd;
  }

  public String getClaBkInmtId() {
    return claBkInmtId;
  }

  public void setClaBkInmtId(String claBkInmtId) {
    this.claBkInmtId = claBkInmtId;
  }

  public Date getClaEffectiveEndDate() {
    return NeutronDateUtils.freshDate(claEffectiveEndDate);
  }

  public void setClaEffectiveEndDate(Date date) {
    this.claEffectiveEndDate = NeutronDateUtils.freshDate(date);
  }

  public Date getClaEffectiveStartDate() {
    return NeutronDateUtils.freshDate(claEffectiveStartDate);
  }

  public void setClaEffectiveStartDate(Date date) {
    this.claEffectiveStartDate = NeutronDateUtils.freshDate(date);
  }

  public String getAdrId() {
    return adrId;
  }

  public void setAdrId(String adrId) {
    this.adrId = adrId;
  }

  public String getAdrCity() {
    return adrCity;
  }

  public void setAdrCity(String adrCity) {
    this.adrCity = adrCity;
  }

  public Long getAdrEmergencyNumber() {
    return adrEmergencyNumber;
  }

  public void setAdrEmergencyNumber(Long adrEmergencyNumber) {
    this.adrEmergencyNumber = adrEmergencyNumber;
  }

  public Integer getAdrEmergencyExtension() {
    return adrEmergencyExtension;
  }

  public void setAdrEmergencyExtension(Integer adrEmergencyExtension) {
    this.adrEmergencyExtension = adrEmergencyExtension;
  }

  public String getAdrFrgAdrtB() {
    return adrFrgAdrtB;
  }

  public void setAdrFrgAdrtB(String adrFrgAdrtB) {
    this.adrFrgAdrtB = adrFrgAdrtB;
  }

  public Short getAdrGovernmentEntityCd() {
    return adrGovernmentEntityCd;
  }

  public void setAdrGovernmentEntityCd(Short adrGovernmentEntityCd) {
    this.adrGovernmentEntityCd = adrGovernmentEntityCd;
  }

  public Long getAdrMessageNumber() {
    return adrMessageNumber;
  }

  public void setAdrMessageNumber(Long adrMessageNumber) {
    this.adrMessageNumber = adrMessageNumber;
  }

  public Integer getAdrMessageExtension() {
    return adrMessageExtension;
  }

  public void setAdrMessageExtension(Integer adrMessageExtension) {
    this.adrMessageExtension = adrMessageExtension;
  }

  public String getAdrHeaderAddress() {
    return adrHeaderAddress;
  }

  public void setAdrHeaderAddress(String adrHeaderAddress) {
    this.adrHeaderAddress = adrHeaderAddress;
  }

  public Long getAdrPrimaryNumber() {
    return adrPrimaryNumber;
  }

  public void setAdrPrimaryNumber(Long adrPrimaryNumber) {
    this.adrPrimaryNumber = adrPrimaryNumber;
  }

  public Integer getAdrPrimaryExtension() {
    return adrPrimaryExtension;
  }

  public Short getAdrState() {
    return adrState;
  }

  public String getAdrStreetName() {
    return adrStreetName;
  }

  public String getAdrStreetNumber() {
    return adrStreetNumber;
  }

  public String getAdrZip() {
    return adrZip;
  }

  public String getAdrAddressDescription() {
    return adrAddressDescription;
  }

  public void setAdrAddressDescription(String adrAddressDescription) {
    this.adrAddressDescription = adrAddressDescription;
  }

  public Short getAdrZip4() {
    return adrZip4;
  }

  public String getAdrPostDirCd() {
    return adrPostDirCd;
  }

  public String getAdrPreDirCd() {
    return adrPreDirCd;
  }

  public Short getAdrStreetSuffixCd() {
    return adrStreetSuffixCd;
  }

  public Short getAdrUnitDesignationCd() {
    return adrUnitDesignationCd;
  }

  public String getAdrUnitNumber() {
    return adrUnitNumber;
  }

  public String getClaId() {
    return claId;
  }

  public Short getClientCounty() {
    return clientCounty;
  }

  public void setClientCounty(Short clientCounty) {
    this.clientCounty = clientCounty;
  }

  public CmsReplicationOperation getAdrReplicationOperation() {
    return adrReplicationOperation;
  }

  public Date getAdrReplicationDate() {
    return NeutronDateUtils.freshDate(adrReplicationDate);
  }

  public Date getLastChange() {
    return NeutronDateUtils.freshDate(lastChange);
  }

  public void setLastChange(Date lastChange) {
    this.lastChange = NeutronDateUtils.freshDate(lastChange);
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
    this.cltBirthDate = NeutronDateUtils.freshDate(cltBirthDate);
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
    this.cltDeathDate = NeutronDateUtils.freshDate(cltDeathDate);
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

  public void setCltFatherParentalRightTermDate(Date cltFatherParentalRightTermDate) {
    this.cltFatherParentalRightTermDate =
        NeutronDateUtils.freshDate(cltFatherParentalRightTermDate);
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
    this.cltMotherParentalRightTermDate =
        NeutronDateUtils.freshDate(cltMotherParentalRightTermDate);
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

  public void setAdrReplicationDate(Date adrReplicationDate) {
    this.adrReplicationDate = NeutronDateUtils.freshDate(adrReplicationDate);
  }

  public void setAdrPrimaryExtension(Integer adrPrimaryExtension) {
    this.adrPrimaryExtension = adrPrimaryExtension;
  }

  public void setAdrState(Short adrState) {
    this.adrState = adrState;
  }

  public void setAdrStreetName(String adrStreetName) {
    this.adrStreetName = adrStreetName;
  }

  public void setAdrStreetNumber(String adrStreetNumber) {
    this.adrStreetNumber = adrStreetNumber;
  }

  public void setAdrZip(String adrZip) {
    this.adrZip = adrZip;
  }

  public void setAdrZip4(Short adrZip4) {
    this.adrZip4 = adrZip4;
  }

  public void setAdrPostDirCd(String adrPostDirCd) {
    this.adrPostDirCd = adrPostDirCd;
  }

  public void setAdrPreDirCd(String adrPreDirCd) {
    this.adrPreDirCd = adrPreDirCd;
  }

  public void setAdrStreetSuffixCd(Short adrStreetSuffixCd) {
    this.adrStreetSuffixCd = adrStreetSuffixCd;
  }

  public void setAdrUnitDesignationCd(Short adrUnitDesignationCd) {
    this.adrUnitDesignationCd = adrUnitDesignationCd;
  }

  public void setAdrUnitNumber(String adrUnitNumber) {
    this.adrUnitNumber = adrUnitNumber;
  }

  @Override
  public String getNormalizationGroupKey() {
    return this.cltId;
  }

}
