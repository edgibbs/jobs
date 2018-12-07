package gov.ca.cwds.data.persistence.cms.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.PlacementHomeAddress;
import gov.ca.cwds.data.persistence.cms.client.ClientReference.ColPos;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.Goddard;

public class RawClientTest extends Goddard<RawClient, ApiGroupNormalizer<ReplicatedClient>> {

  static java.sql.Date sqlDate;
  RawClient target;

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new RawClient();

    prepResultSetGood(rs);
    target.read(rs);
  }

  public static void prepResultSetGood(ResultSet rs) throws SQLException {
    when(rs.getString(ColPos.CLT_IDENTIFIER.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColPos.CLT_SENSTV_IND.ordinal())).thenReturn("R");
    when(rs.getString(ColPos.CLT_SOC158_IND.ordinal())).thenReturn("Y");
    when(rs.getString(ColPos.CLT_CL_INDX_NO.ordinal())).thenReturn("12345");
    when(rs.getString(ColPos.CLT_COM_FST_NM.ordinal())).thenReturn("Baby");
    when(rs.getString(ColPos.CLT_COM_LST_NM.ordinal())).thenReturn("Doe");
    when(rs.getString(ColPos.CLT_COM_MID_NM.ordinal())).thenReturn("X");
    when(rs.getString(ColPos.CLT_DTH_DT_IND.ordinal())).thenReturn("N");
    when(rs.getString(ColPos.CLT_DRV_LIC_NO.ordinal())).thenReturn("");
    when(rs.getString(ColPos.CLT_EMAIL_ADDR.ordinal())).thenReturn("");
    when(rs.getString(ColPos.CLT_ETH_UD_CD.ordinal())).thenReturn("");
    when(rs.getString(ColPos.CLT_GENDER_CD.ordinal())).thenReturn("F");
    when(rs.getString(ColPos.CLT_HISP_UD_CD.ordinal())).thenReturn("");
    when(rs.getString(ColPos.CLT_HISP_CD.ordinal())).thenReturn("");
    when(rs.getString(ColPos.CLT_LITRATE_CD.ordinal())).thenReturn("");
    when(rs.getString(ColPos.CLT_MILT_STACD.ordinal())).thenReturn("");
    when(rs.getString(ColPos.CLT_NMPRFX_DSC.ordinal())).thenReturn("Ms");
    when(rs.getString(ColPos.CLT_SNTV_HLIND.ordinal())).thenReturn("");
    when(rs.getString(ColPos.CLT_SOCPLC_CD.ordinal())).thenReturn("");
    when(rs.getString(ColPos.CLT_SSN_CHG_CD.ordinal())).thenReturn("");
    when(rs.getString(ColPos.CLT_SS_NO.ordinal())).thenReturn("111223333");
    when(rs.getString(ColPos.CLT_SUFX_TLDSC.ordinal())).thenReturn("jr");
    when(rs.getString(ColPos.CLT_TRBA_CLT_B.ordinal())).thenReturn("");
    when(rs.getString(ColPos.CLT_ZIPPY_IND.ordinal())).thenReturn("N");
    when(rs.getString(ColPos.CLT_LST_UPD_ID.ordinal())).thenReturn("0x5");

    when(rs.getShort(ColPos.CLT_D_STATE_C.ordinal())).thenReturn((short) 1828);
    when(rs.getShort(ColPos.CLT_IMGT_STC.ordinal())).thenReturn((short) 1199);
    when(rs.getShort(ColPos.CLT_MRTL_STC.ordinal())).thenReturn((short) 1309);
    when(rs.getShort(ColPos.CLT_NAME_TPC.ordinal())).thenReturn((short) 1312);
    when(rs.getShort(ColPos.CLT_P_ETHNCTYC.ordinal())).thenReturn((short) 3163);
    when(rs.getShort(ColPos.CLT_P_LANG_TPC.ordinal())).thenReturn((short) 1253);
    when(rs.getShort(ColPos.CLT_S_LANG_TC.ordinal())).thenReturn((short) 1261);
    when(rs.getShort(ColPos.CLT_RLGN_TPC.ordinal())).thenReturn((short) 1566);

    Date date = new Date();
    when(rs.getTimestamp(ColPos.CLT_LST_UPD_TS.ordinal()))
        .thenReturn(new Timestamp(date.getTime()));

    sqlDate = new java.sql.Date(date.getTime());
    when(rs.getDate(ColPos.CLT_BIRTH_DT.ordinal())).thenReturn(sqlDate);
    when(rs.getDate(ColPos.CLT_CREATN_DT.ordinal())).thenReturn(sqlDate);
    when(rs.getDate(ColPos.CLT_DEATH_DT.ordinal())).thenReturn(sqlDate);
  }

  @Test
  public void type() throws Exception {
    assertThat(RawClient.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getPrimaryKey_A$() throws Exception {
    Serializable actual = target.getPrimaryKey();
    Serializable expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void read_A$ResultSet() throws Exception {
    RawClient actual = target.read(rs);
    // RawClient expected = new RawClient();
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = SQLException.class)
  public void read_A$ResultSet_T$SQLException() throws Exception {
    bombResultSet();
    target.read(rs);
  }

  @Test
  public void getNormalizationClass_A$() throws Exception {
    Class<ReplicatedClient> actual = target.getNormalizationClass();
    Class<ReplicatedClient> expected = ReplicatedClient.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getNormalizationGroupKey_A$() throws Exception {
    Serializable actual = target.getNormalizationGroupKey();
    Serializable expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_A$Map() throws Exception {
    Map<Object, ReplicatedClient> ignoreMe = new HashMap<Object, ReplicatedClient>();
    ReplicatedClient actual = target.normalize(ignoreMe);
    // ReplicatedClient expected = new ReplicatedClient();
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void normalize_A$RawToEsConverter() throws Exception {
    RawToEsConverter conv = mock(RawToEsConverter.class);
    ReplicatedClient actual = target.normalize(conv);
    ReplicatedClient expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltAdjudicatedDelinquentIndicator_A$() throws Exception {
    String actual = target.getCltAdjudicatedDelinquentIndicator();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltAdoptionStatusCode_A$() throws Exception {
    String actual = target.getCltAdoptionStatusCode();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltAlienRegistrationNumber_A$() throws Exception {
    String actual = target.getCltAlienRegistrationNumber();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltBirthCity_A$() throws Exception {
    String actual = target.getCltBirthCity();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltBirthCountryCodeType_A$() throws Exception {
    Short actual = target.getCltBirthCountryCodeType();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltBirthDate_A$() throws Exception {
    Date actual = target.getCltBirthDate();
    Date expected = sqlDate;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltBirthFacilityName_A$() throws Exception {
    String actual = target.getCltBirthFacilityName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltBirthStateCodeType_A$() throws Exception {
    Short actual = target.getCltBirthStateCodeType();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltBirthplaceVerifiedIndicator_A$() throws Exception {
    String actual = target.getCltBirthplaceVerifiedIndicator();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltBirthplaceVerifiedIndicator_A$String() throws Exception {
    String cltBirthplaceVerifiedIndicator = null;
    target.setCltBirthplaceVerifiedIndicator(cltBirthplaceVerifiedIndicator);
  }

  @Test
  public void getCltChildClientIndicatorVar_A$() throws Exception {
    String actual = target.getCltChildClientIndicatorVar();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltClientIndexNumber_A$() throws Exception {
    String actual = target.getCltClientIndexNumber();
    String expected = "12345";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltCommentDescription_A$() throws Exception {
    String actual = target.getCltCommentDescription();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltCommonFirstName_A$() throws Exception {
    String actual = target.getCltCommonFirstName();
    String expected = "Baby";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltCommonLastName_A$() throws Exception {
    String actual = target.getCltCommonLastName();
    String expected = "Doe";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltCommonMiddleName_A$() throws Exception {
    String actual = target.getCltCommonMiddleName();
    String expected = "X";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltConfidentialityActionDate_A$() throws Exception {
    Date actual = target.getCltConfidentialityActionDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltConfidentialityActionDate_A$Date() throws Exception {
    Date cltConfidentialityActionDate = mock(Date.class);
    target.setCltConfidentialityActionDate(cltConfidentialityActionDate);
  }

  @Test
  public void getCltConfidentialityInEffectIndicator_A$() throws Exception {
    String actual = target.getCltConfidentialityInEffectIndicator();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltConfidentialityInEffectIndicator_A$String() throws Exception {
    String cltConfidentialityInEffectIndicator = null;
    target.setCltConfidentialityInEffectIndicator(cltConfidentialityInEffectIndicator);
  }

  @Test
  public void getCltCreationDate_A$() throws Exception {
    Date actual = target.getCltCreationDate();
    // Date expected = null;
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setCltCreationDate_A$Date() throws Exception {
    Date cltCreationDate = mock(Date.class);
    target.setCltCreationDate(cltCreationDate);
  }

  @Test
  public void getCltCurrCaChildrenServIndicator_A$() throws Exception {
    String actual = target.getCltCurrCaChildrenServIndicator();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltCurrCaChildrenServIndicator_A$String() throws Exception {
    String cltCurrCaChildrenServIndicator = null;
    target.setCltCurrCaChildrenServIndicator(cltCurrCaChildrenServIndicator);
  }

  @Test
  public void getCltCurrentlyOtherDescription_A$() throws Exception {
    String actual = target.getCltCurrentlyOtherDescription();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltCurrentlyRegionalCenterIndicator_A$() throws Exception {
    String actual = target.getCltCurrentlyRegionalCenterIndicator();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltDeathDate_A$() throws Exception {
    Date actual = target.getCltDeathDate();
    Date expected = sqlDate;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltDeathDateVerifiedIndicator_A$() throws Exception {
    String actual = target.getCltDeathDateVerifiedIndicator();
    String expected = "N";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltDeathPlace_A$() throws Exception {
    String actual = target.getCltDeathPlace();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltDeathReasonText_A$() throws Exception {
    String actual = target.getCltDeathReasonText();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltDriverLicenseNumber_A$() throws Exception {
    String actual = target.getCltDriverLicenseNumber();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltDriverLicenseStateCodeType_A$() throws Exception {
    Short actual = target.getCltDriverLicenseStateCodeType();
    Short expected = 1828;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltEmailAddress_A$() throws Exception {
    String actual = target.getCltEmailAddress();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltEstimatedDobCode_A$() throws Exception {
    String actual = target.getCltEstimatedDobCode();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltEthUnableToDetReasonCode_A$() throws Exception {
    String actual = target.getCltEthUnableToDetReasonCode();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltGenderCode_A$() throws Exception {
    String actual = target.getCltGenderCode();
    String expected = "F";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltHealthSummaryText_A$() throws Exception {
    String actual = target.getCltHealthSummaryText();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltHealthSummaryText_A$String() throws Exception {
    String cltHealthSummaryText = null;
    target.setCltHealthSummaryText(cltHealthSummaryText);
  }

  @Test
  public void getCltHispUnableToDetReasonCode_A$() throws Exception {
    String actual = target.getCltHispUnableToDetReasonCode();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltHispUnableToDetReasonCode_A$String() throws Exception {
    String cltHispUnableToDetReasonCode = null;
    target.setCltHispUnableToDetReasonCode(cltHispUnableToDetReasonCode);
  }

  @Test
  public void getCltHispanicOriginCode_A$() throws Exception {
    String actual = target.getCltHispanicOriginCode();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltHispanicOriginCode_A$String() throws Exception {
    String cltHispanicOriginCode = null;
    target.setCltHispanicOriginCode(cltHispanicOriginCode);
  }

  @Test
  public void getCltImmigrationCountryCodeType_A$() throws Exception {
    Short actual = target.getCltImmigrationCountryCodeType();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltImmigrationCountryCodeType_A$Short() throws Exception {
    Short cltImmigrationCountryCodeType = null;
    target.setCltImmigrationCountryCodeType(cltImmigrationCountryCodeType);
  }

  @Test
  public void getCltImmigrationStatusType_A$() throws Exception {
    Short actual = target.getCltImmigrationStatusType();
    Short expected = 1199;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltIncapacitatedParentCode_A$() throws Exception {
    String actual = target.getCltIncapacitatedParentCode();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltIndividualHealthCarePlanIndicator_A$() throws Exception {
    String actual = target.getCltIndividualHealthCarePlanIndicator();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltLimitationOnScpHealthIndicator_A$() throws Exception {
    String actual = target.getCltLimitationOnScpHealthIndicator();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltLiterateCode_A$() throws Exception {
    String actual = target.getCltLiterateCode();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltMaritalCohabitatnHstryIndicatorVar_A$() throws Exception {
    String actual = target.getCltMaritalCohabitatnHstryIndicatorVar();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltMaritalStatusType_A$() throws Exception {
    Short actual = target.getCltMaritalStatusType();
    Short expected = 1309;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltMilitaryStatusCode_A$() throws Exception {
    String actual = target.getCltMilitaryStatusCode();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltMotherParentalRightTermDate_A$() throws Exception {
    Date actual = target.getCltMotherParentalRightTermDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltNamePrefixDescription_A$() throws Exception {
    String actual = target.getCltNamePrefixDescription();
    String expected = "Ms";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltNameType_A$() throws Exception {
    Short actual = target.getCltNameType();
    Short expected = 1312;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltOutstandingWarrantIndicator_A$() throws Exception {
    String actual = target.getCltOutstandingWarrantIndicator();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltPrevCaChildrenServIndicator_A$() throws Exception {
    String actual = target.getCltPrevCaChildrenServIndicator();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltPrevOtherDescription_A$() throws Exception {
    String actual = target.getCltPrevOtherDescription();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltPrevRegionalCenterIndicator_A$() throws Exception {
    String actual = target.getCltPrevRegionalCenterIndicator();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltPrevRegionalCenterIndicator_A$String() throws Exception {
    String cltPrevRegionalCenterIndicator = null;
    target.setCltPrevRegionalCenterIndicator(cltPrevRegionalCenterIndicator);
  }

  @Test
  public void getCltPrimaryEthnicityType_A$() throws Exception {
    Short actual = target.getCltPrimaryEthnicityType();
    Short expected = 3163;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltPrimaryEthnicityType_A$Short() throws Exception {
    Short cltPrimaryEthnicityType = null;
    target.setCltPrimaryEthnicityType(cltPrimaryEthnicityType);
  }

  @Test
  public void getCltPrimaryLanguageType_A$() throws Exception {
    Short actual = target.getCltPrimaryLanguageType();
    Short expected = 1253;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltPrimaryLanguageType_A$Short() throws Exception {
    Short cltPrimaryLanguageType = null;
    target.setCltPrimaryLanguageType(cltPrimaryLanguageType);
  }

  @Test
  public void getCltReligionType_A$() throws Exception {
    Short actual = target.getCltReligionType();
    Short expected = 1566;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltReligionType_A$Short() throws Exception {
    Short cltReligionType = null;
    target.setCltReligionType(cltReligionType);
  }

  @Test
  public void getCltSecondaryLanguageType_A$() throws Exception {
    Short actual = target.getCltSecondaryLanguageType();
    Short expected = 1261;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltSecondaryLanguageType_A$Short() throws Exception {
    Short cltSecondaryLanguageType = null;
    target.setCltSecondaryLanguageType(cltSecondaryLanguageType);
  }

  @Test
  public void getCltSensitiveHlthInfoOnFileIndicator_A$() throws Exception {
    String actual = target.getCltSensitiveHlthInfoOnFileIndicator();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltSensitiveHlthInfoOnFileIndicator_A$String() throws Exception {
    String cltSensitiveHlthInfoOnFileIndicator = null;
    target.setCltSensitiveHlthInfoOnFileIndicator(cltSensitiveHlthInfoOnFileIndicator);
  }

  @Test
  public void getCltSensitivityIndicator_A$() throws Exception {
    String actual = target.getCltSensitivityIndicator();
    String expected = "R";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltSensitivityIndicator_A$String() throws Exception {
    String cltSensitivityIndicator = null;
    target.setCltSensitivityIndicator(cltSensitivityIndicator);
  }

  @Test
  public void getCltSoc158PlacementCode_A$() throws Exception {
    String actual = target.getCltSoc158PlacementCode();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltSoc158PlacementCode_A$String() throws Exception {
    String cltSoc158PlacementCode = null;
    target.setCltSoc158PlacementCode(cltSoc158PlacementCode);
  }

  @Test
  public void getCltSoc158SealedClientIndicator_A$() throws Exception {
    String actual = target.getCltSoc158SealedClientIndicator();
    String expected = "Y";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltSoc158SealedClientIndicator_A$String() throws Exception {
    String cltSoc158SealedClientIndicator = null;
    target.setCltSoc158SealedClientIndicator(cltSoc158SealedClientIndicator);
  }

  @Test
  public void getCltSocialSecurityNumChangedCode_A$() throws Exception {
    String actual = target.getCltSocialSecurityNumChangedCode();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltSocialSecurityNumChangedCode_A$String() throws Exception {
    String cltSocialSecurityNumChangedCode = null;
    target.setCltSocialSecurityNumChangedCode(cltSocialSecurityNumChangedCode);
  }

  @Test
  public void getCltSocialSecurityNumber_A$() throws Exception {
    String actual = target.getCltSocialSecurityNumber();
    String expected = "111223333";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltSocialSecurityNumber_A$String() throws Exception {
    String cltSocialSecurityNumber = null;
    target.setCltSocialSecurityNumber(cltSocialSecurityNumber);
  }

  @Test
  public void getCltSuffixTitleDescription_A$() throws Exception {
    String actual = target.getCltSuffixTitleDescription();
    String expected = "jr";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltSuffixTitleDescription_A$String() throws Exception {
    String cltSuffixTitleDescription = null;
    target.setCltSuffixTitleDescription(cltSuffixTitleDescription);
  }

  @Test
  public void getCltTribalAncestryClientIndicatorVar_A$() throws Exception {
    String actual = target.getCltTribalAncestryClientIndicatorVar();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltTribalAncestryClientIndicatorVar_A$String() throws Exception {
    String cltTribalAncestryClientIndicatorVar = null;
    target.setCltTribalAncestryClientIndicatorVar(cltTribalAncestryClientIndicatorVar);
  }

  @Test
  public void getCltTribalMembrshpVerifctnIndicatorVar_A$() throws Exception {
    String actual = target.getCltTribalMembrshpVerifctnIndicatorVar();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltTribalMembrshpVerifctnIndicatorVar_A$String() throws Exception {
    String cltTribalMembrshpVerifctnIndicatorVar = null;
    target.setCltTribalMembrshpVerifctnIndicatorVar(cltTribalMembrshpVerifctnIndicatorVar);
  }

  @Test
  public void getCltUnemployedParentCode_A$() throws Exception {
    String actual = target.getCltUnemployedParentCode();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltUnemployedParentCode_A$String() throws Exception {
    String cltUnemployedParentCode = null;
    target.setCltUnemployedParentCode(cltUnemployedParentCode);
  }

  @Test
  public void getCltZippyCreatedIndicator_A$() throws Exception {
    String actual = target.getCltZippyCreatedIndicator();
    String expected = "N";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltZippyCreatedIndicator_A$String() throws Exception {
    String cltZippyCreatedIndicator = null;
    target.setCltZippyCreatedIndicator(cltZippyCreatedIndicator);
  }

  @Test
  public void getCltReplicationOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getCltReplicationOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltReplicationOperation_A$CmsReplicationOperation() throws Exception {
    CmsReplicationOperation cltReplicationOperation = CmsReplicationOperation.U;
    target.setCltReplicationOperation(cltReplicationOperation);
  }

  @Test
  public void getCltReplicationDate_A$() throws Exception {
    Date actual = target.getCltReplicationDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltReplicationDate_A$Date() throws Exception {
    Date cltReplicationDate = mock(Date.class);
    target.setCltReplicationDate(cltReplicationDate);
  }

  @Test
  public void getCltLastUpdatedId_A$() throws Exception {
    String actual = target.getCltLastUpdatedId();
    String expected = "0x5";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltLastUpdatedId_A$String() throws Exception {
    String cltLastUpdatedId = null;
    target.setCltLastUpdatedId(cltLastUpdatedId);
  }

  @Test
  public void getCltLastUpdatedTime_A$() throws Exception {
    Date actual = target.getCltLastUpdatedTime();
    Date expected = sqlDate;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltLastUpdatedTime_A$Date() throws Exception {
    Date cltLastUpdatedTime = mock(Date.class);
    target.setCltLastUpdatedTime(cltLastUpdatedTime);
  }

  @Test
  public void getCltFatherParentalRightTermDate_A$() throws Exception {
    Date actual = target.getCltFatherParentalRightTermDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCltFatherParentalRightTermDate_A$Date() throws Exception {
    Date cltFatherParentalRightTermDate = mock(Date.class);
    target.setCltFatherParentalRightTermDate(cltFatherParentalRightTermDate);
  }

  @Test
  public void setCltAdjudicatedDelinquentIndicator_A$String() throws Exception {
    String cltAdjudicatedDelinquentIndicator = null;
    target.setCltAdjudicatedDelinquentIndicator(cltAdjudicatedDelinquentIndicator);
  }

  @Test
  public void setCltAdoptionStatusCode_A$String() throws Exception {
    String cltAdoptionStatusCode = null;
    target.setCltAdoptionStatusCode(cltAdoptionStatusCode);
  }

  @Test
  public void setCltAlienRegistrationNumber_A$String() throws Exception {
    String cltAlienRegistrationNumber = null;
    target.setCltAlienRegistrationNumber(cltAlienRegistrationNumber);
  }

  @Test
  public void setCltBirthCity_A$String() throws Exception {
    String cltBirthCity = null;
    target.setCltBirthCity(cltBirthCity);
  }

  @Test
  public void setCltBirthCountryCodeType_A$Short() throws Exception {
    Short cltBirthCountryCodeType = null;
    target.setCltBirthCountryCodeType(cltBirthCountryCodeType);
  }

  @Test
  public void setCltBirthDate_A$Date() throws Exception {
    Date cltBirthDate = mock(Date.class);
    target.setCltBirthDate(cltBirthDate);
  }

  @Test
  public void setCltBirthFacilityName_A$String() throws Exception {
    String cltBirthFacilityName = null;
    target.setCltBirthFacilityName(cltBirthFacilityName);
  }

  @Test
  public void setCltBirthStateCodeType_A$Short() throws Exception {
    Short cltBirthStateCodeType = null;
    target.setCltBirthStateCodeType(cltBirthStateCodeType);
  }

  @Test
  public void setCltChildClientIndicatorVar_A$String() throws Exception {
    String cltChildClientIndicatorVar = null;
    target.setCltChildClientIndicatorVar(cltChildClientIndicatorVar);
  }

  @Test
  public void setCltClientIndexNumber_A$String() throws Exception {
    String cltClientIndexNumber = null;
    target.setCltClientIndexNumber(cltClientIndexNumber);
  }

  @Test
  public void setCltCommentDescription_A$String() throws Exception {
    String cltCommentDescription = null;
    target.setCltCommentDescription(cltCommentDescription);
  }

  @Test
  public void setCltCommonFirstName_A$String() throws Exception {
    String cltCommonFirstName = null;
    target.setCltCommonFirstName(cltCommonFirstName);
  }

  @Test
  public void setCltCommonLastName_A$String() throws Exception {
    String cltCommonLastName = null;
    target.setCltCommonLastName(cltCommonLastName);
  }

  @Test
  public void setCltCommonMiddleName_A$String() throws Exception {
    String cltCommonMiddleName = null;
    target.setCltCommonMiddleName(cltCommonMiddleName);
  }

  @Test
  public void setCltCurrentlyOtherDescription_A$String() throws Exception {
    String cltCurrentlyOtherDescription = null;
    target.setCltCurrentlyOtherDescription(cltCurrentlyOtherDescription);
  }

  @Test
  public void setCltCurrentlyRegionalCenterIndicator_A$String() throws Exception {
    String cltCurrentlyRegionalCenterIndicator = null;
    target.setCltCurrentlyRegionalCenterIndicator(cltCurrentlyRegionalCenterIndicator);
  }

  @Test
  public void setCltDeathDate_A$Date() throws Exception {
    Date cltDeathDate = mock(Date.class);
    target.setCltDeathDate(cltDeathDate);
  }

  @Test
  public void setCltDeathDateVerifiedIndicator_A$String() throws Exception {
    String cltDeathDateVerifiedIndicator = null;
    target.setCltDeathDateVerifiedIndicator(cltDeathDateVerifiedIndicator);
  }

  @Test
  public void setCltDeathPlace_A$String() throws Exception {
    String cltDeathPlace = null;
    target.setCltDeathPlace(cltDeathPlace);
  }

  @Test
  public void setCltDeathReasonText_A$String() throws Exception {
    String cltDeathReasonText = null;
    target.setCltDeathReasonText(cltDeathReasonText);
  }

  @Test
  public void setCltDriverLicenseNumber_A$String() throws Exception {
    String cltDriverLicenseNumber = null;
    target.setCltDriverLicenseNumber(cltDriverLicenseNumber);
  }

  @Test
  public void setCltDriverLicenseStateCodeType_A$Short() throws Exception {
    Short cltDriverLicenseStateCodeType = null;
    target.setCltDriverLicenseStateCodeType(cltDriverLicenseStateCodeType);
  }

  @Test
  public void setCltEmailAddress_A$String() throws Exception {
    String cltEmailAddress = null;
    target.setCltEmailAddress(cltEmailAddress);
  }

  @Test
  public void setCltEstimatedDobCode_A$String() throws Exception {
    String cltEstimatedDobCode = null;
    target.setCltEstimatedDobCode(cltEstimatedDobCode);
  }

  @Test
  public void setCltEthUnableToDetReasonCode_A$String() throws Exception {
    String cltEthUnableToDetReasonCode = null;
    target.setCltEthUnableToDetReasonCode(cltEthUnableToDetReasonCode);
  }

  @Test
  public void setCltGenderCode_A$String() throws Exception {
    String cltGenderCode = null;
    target.setCltGenderCode(cltGenderCode);
  }

  @Test
  public void setCltImmigrationStatusType_A$Short() throws Exception {
    Short cltImmigrationStatusType = null;
    target.setCltImmigrationStatusType(cltImmigrationStatusType);
  }

  @Test
  public void setCltIncapacitatedParentCode_A$String() throws Exception {
    String cltIncapacitatedParentCode = null;
    target.setCltIncapacitatedParentCode(cltIncapacitatedParentCode);
  }

  @Test
  public void setCltIndividualHealthCarePlanIndicator_A$String() throws Exception {
    String cltIndividualHealthCarePlanIndicator = null;
    target.setCltIndividualHealthCarePlanIndicator(cltIndividualHealthCarePlanIndicator);
  }

  @Test
  public void setCltLimitationOnScpHealthIndicator_A$String() throws Exception {
    String cltLimitationOnScpHealthIndicator = null;
    target.setCltLimitationOnScpHealthIndicator(cltLimitationOnScpHealthIndicator);
  }

  @Test
  public void setCltLiterateCode_A$String() throws Exception {
    String cltLiterateCode = null;
    target.setCltLiterateCode(cltLiterateCode);
  }

  @Test
  public void setCltMaritalCohabitatnHstryIndicatorVar_A$String() throws Exception {
    String cltMaritalCohabitatnHstryIndicatorVar = null;
    target.setCltMaritalCohabitatnHstryIndicatorVar(cltMaritalCohabitatnHstryIndicatorVar);
  }

  @Test
  public void setCltMaritalStatusType_A$Short() throws Exception {
    Short cltMaritalStatusType = null;
    target.setCltMaritalStatusType(cltMaritalStatusType);
  }

  @Test
  public void setCltMilitaryStatusCode_A$String() throws Exception {
    String cltMilitaryStatusCode = null;
    target.setCltMilitaryStatusCode(cltMilitaryStatusCode);
  }

  @Test
  public void setCltMotherParentalRightTermDate_A$Date() throws Exception {
    Date cltMotherParentalRightTermDate = mock(Date.class);
    target.setCltMotherParentalRightTermDate(cltMotherParentalRightTermDate);
  }

  @Test
  public void setCltNamePrefixDescription_A$String() throws Exception {
    String cltNamePrefixDescription = null;
    target.setCltNamePrefixDescription(cltNamePrefixDescription);
  }

  @Test
  public void setCltNameType_A$Short() throws Exception {
    Short cltNameType = null;
    target.setCltNameType(cltNameType);
  }

  @Test
  public void setCltOutstandingWarrantIndicator_A$String() throws Exception {
    String cltOutstandingWarrantIndicator = null;
    target.setCltOutstandingWarrantIndicator(cltOutstandingWarrantIndicator);
  }

  @Test
  public void setCltPrevCaChildrenServIndicator_A$String() throws Exception {
    String cltPrevCaChildrenServIndicator = null;
    target.setCltPrevCaChildrenServIndicator(cltPrevCaChildrenServIndicator);
  }

  @Test
  public void setCltPrevOtherDescription_A$String() throws Exception {
    String cltPrevOtherDescription = null;
    target.setCltPrevOtherDescription(cltPrevOtherDescription);
  }

  @Test
  public void addClientAddress_A$RawClientAddress() throws Exception {
    RawClientAddress cla = mock(RawClientAddress.class);
    target.addClientAddress(cla);
  }

  @Test
  public void addClientCounty_A$RawClientCounty() throws Exception {
    RawClientCounty c = mock(RawClientCounty.class);
    target.addClientCounty(c);
  }

  @Test
  public void addCsec_A$RawCsec() throws Exception {
    RawCsec c = mock(RawCsec.class);
    target.addCsec(c);
  }

  @Test
  public void addCase_A$RawCase() throws Exception {
    RawCase c = mock(RawCase.class);
    target.addCase(c);
  }

  @Test
  public void addAka_A$RawAka() throws Exception {
    RawAka c = mock(RawAka.class);
    target.addAka(c);
  }

  @Test
  public void addEthnicity_A$RawEthnicity() throws Exception {
    RawEthnicity c = mock(RawEthnicity.class);
    target.addEthnicity(c);
  }

  @Test
  public void addSafetyAlert_A$RawSafetyAlert() throws Exception {
    RawSafetyAlert c = mock(RawSafetyAlert.class);
    target.addSafetyAlert(c);
  }

  @Test
  public void getClientAddress_A$() throws Exception {
    Map<String, RawClientAddress> actual = target.getClientAddress();
    Map<String, RawClientAddress> expected = new HashMap<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getClientCounty_A$() throws Exception {
    List<RawClientCounty> actual = target.getClientCounty();
    List<RawClientCounty> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCsec_A$() throws Exception {
    List<RawCsec> actual = target.getCsec();
    List<RawCsec> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAka_A$() throws Exception {
    List<RawAka> actual = target.getAka();
    List<RawAka> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getEthnicity_A$() throws Exception {
    List<RawEthnicity> actual = target.getEthnicity();
    List<RawEthnicity> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getSafetyAlert_A$() throws Exception {
    List<RawSafetyAlert> actual = target.getSafetyAlert();
    List<RawSafetyAlert> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCases_A$() throws Exception {
    List<RawCase> actual = target.getCases();
    List<RawCase> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPlacementHomeAddress_A$() throws Exception {
    PlacementHomeAddress actual = target.getPlacementHomeAddress();
    PlacementHomeAddress expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setPlacementHomeAddress_A$PlacementHomeAddress() throws Exception {
    PlacementHomeAddress placementHomeAddress = mock(PlacementHomeAddress.class);
    target.setPlacementHomeAddress(placementHomeAddress);
  }

  @Test
  public void compare_A$RawClient$RawClient() throws Exception {
    RawClient o1 = new RawClient();
    RawClient o2 = new RawClient();

    int actual = target.compare(o1, o2);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void compareTo_A$RawClient() throws Exception {
    RawClient o = new RawClient();
    int actual = target.compareTo(o);
    int expected = 0;
    assertThat(actual, is(not(equalTo(expected))));
  }

}
