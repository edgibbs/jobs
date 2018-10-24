package gov.ca.cwds.data.persistence.cms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.utils.JsonUtils;

public class EsClientPersonTest extends Goddard<ReplicatedClient, EsClientPerson> {

  EsClientPerson target;

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new EsClientPerson();
    target.setCltId(DEFAULT_CLIENT_ID);
  }

  @Test
  public void type() throws Exception {
    assertThat(EsClientPerson.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void extract_A$ResultSet() throws Exception {
    EsClientPerson actual = EsClientPerson.extract(rs);

    final String json =
        "{\"lastChange\":1540309977457,\"cltAdjudicatedDelinquentIndicator\":\"abc1234567\",\"cltAdoptionStatusCode\":\"abc1234567\",\"cltAlienRegistrationNumber\":\"abc1234567\",\"cltBirthCity\":\"abc1234567\",\"cltBirthCountryCodeType\":0,\"cltBirthDate\":1540309977457,\"cltBirthFacilityName\":\"abc1234567\",\"cltBirthStateCodeType\":0,\"cltBirthplaceVerifiedIndicator\":\"abc1234567\",\"cltChildClientIndicatorVar\":\"abc1234567\",\"cltClientIndexNumber\":\"abc1234567\",\"cltCommentDescription\":\"abc1234567\",\"cltCommonFirstName\":\"abc1234567\",\"cltCommonLastName\":\"abc1234567\",\"cltCommonMiddleName\":\"abc1234567\",\"cltConfidentialityActionDate\":1540309977457,\"cltConfidentialityInEffectIndicator\":\"abc1234567\",\"cltCreationDate\":1540309977457,\"cltCurrCaChildrenServIndicator\":\"abc1234567\",\"cltCurrentlyOtherDescription\":\"abc1234567\",\"cltCurrentlyRegionalCenterIndicator\":\"abc1234567\",\"cltDeathDate\":1540309977457,\"cltDeathDateVerifiedIndicator\":\"abc1234567\",\"cltDeathPlace\":\"abc1234567\",\"cltDeathReasonText\":\"abc1234567\",\"cltDriverLicenseNumber\":\"abc1234567\",\"cltDriverLicenseStateCodeType\":0,\"cltEmailAddress\":\"abc1234567\",\"cltEstimatedDobCode\":\"abc1234567\",\"cltEthUnableToDetReasonCode\":\"abc1234567\",\"cltFatherParentalRightTermDate\":1540309977457,\"cltGenderCode\":\"abc1234567\",\"cltHealthSummaryText\":\"abc1234567\",\"cltHispUnableToDetReasonCode\":\"abc1234567\",\"cltHispanicOriginCode\":\"abc1234567\",\"cltId\":\"abc1234567\",\"cltImmigrationCountryCodeType\":0,\"cltImmigrationStatusType\":0,\"cltIncapacitatedParentCode\":\"abc1234567\",\"cltIndividualHealthCarePlanIndicator\":\"abc1234567\",\"cltLimitationOnScpHealthIndicator\":\"abc1234567\",\"cltLiterateCode\":\"abc1234567\",\"cltMaritalCohabitatnHstryIndicatorVar\":\"abc1234567\",\"cltMaritalStatusType\":0,\"cltMilitaryStatusCode\":\"abc1234567\",\"cltMotherParentalRightTermDate\":1540309977457,\"cltNamePrefixDescription\":\"abc1234567\",\"cltNameType\":0,\"cltOutstandingWarrantIndicator\":\"abc1234567\",\"cltPrevCaChildrenServIndicator\":\"abc1234567\",\"cltPrevOtherDescription\":\"abc1234567\",\"cltPrevRegionalCenterIndicator\":\"abc1234567\",\"cltPrimaryEthnicityType\":0,\"cltPrimaryLanguageType\":0,\"cltReligionType\":0,\"cltSecondaryLanguageType\":0,\"cltSensitiveHlthInfoOnFileIndicator\":\"abc1234567\",\"cltSensitivityIndicator\":\"abc1234567\",\"cltSoc158PlacementCode\":\"abc1234567\",\"cltSoc158SealedClientIndicator\":\"abc1234567\",\"cltSocialSecurityNumChangedCode\":\"abc1234567\",\"cltSocialSecurityNumber\":\"abc1234567\",\"cltSuffixTitleDescription\":\"abc1234567\",\"cltTribalAncestryClientIndicatorVar\":\"abc1234567\",\"cltTribalMembrshpVerifctnIndicatorVar\":\"abc1234567\",\"cltUnemployedParentCode\":\"abc1234567\",\"cltZippyCreatedIndicator\":\"abc1234567\",\"cltReplicationOperation\":\"I\",\"cltReplicationDate\":1540309977457,\"cltLastUpdatedId\":\"abc1234567\",\"cltLastUpdatedTime\":1540309977457,\"claReplicationOperation\":\"I\",\"claReplicationDate\":1540309977457,\"claLastUpdatedId\":\"abc1234567\",\"claLastUpdatedTime\":1540309977457,\"claId\":\"abc1234567\",\"claFkAddress\":\"abc1234567\",\"claFkClient\":\"abc1234567\",\"claFkReferral\":\"abc1234567\",\"claAddressType\":0,\"claHomelessInd\":\"abc1234567\",\"claBkInmtId\":\"abc1234567\",\"claEffectiveEndDate\":1540309977457,\"claEffectiveStartDate\":1540309977457,\"adrId\":\"abc1234567\",\"adrReplicationOperation\":\"I\",\"adrReplicationDate\":1540309977457,\"adrCity\":\"abc1234567\",\"adrEmergencyNumber\":0,\"adrEmergencyExtension\":0,\"adrFrgAdrtB\":\"abc1234567\",\"adrGovernmentEntityCd\":0,\"adrMessageNumber\":0,\"adrMessageExtension\":0,\"adrHeaderAddress\":\"abc1234567\",\"adrPrimaryNumber\":0,\"adrPrimaryExtension\":0,\"adrState\":0,\"adrStreetName\":\"abc1234567\",\"adrStreetNumber\":\"abc1234567\",\"adrZip\":\"abc1234567\",\"adrAddressDescription\":\"abc1234567\",\"adrZip4\":0,\"adrPostDirCd\":\"abc1234567\",\"adrPreDirCd\":\"abc1234567\",\"adrStreetSuffixCd\":0,\"adrUnitDesignationCd\":0,\"adrUnitNumber\":\"abc1234567\",\"adrLastUpdatedTime\":1540309977457,\"clientCountyId\":\"abc1234567\",\"clientCounty\":0,\"clientCountyRule\":null,\"clientEthnicityId\":\"abc1234567\",\"clientEthnicityCode\":0,\"safetyAlertId\":\"abc1234567\",\"safetyAlertActivationReasonCode\":0,\"safetyAlertActivationDate\":1540309977457,\"safetyAlertActivationCountyCode\":0,\"safetyAlertActivationExplanation\":\"abc1234567\",\"safetyAlertDeactivationDate\":1540309977457,\"safetyAlertDeactivationCountyCode\":0,\"safetyAlertDeactivationExplanation\":\"abc1234567\",\"safetyAlertLastUpdatedId\":\"abc1234567\",\"safetyAlertLastUpdatedTimestamp\":1540309977457,\"safetyAlertLastUpdatedOperation\":\"I\",\"safetyAlertReplicationTimestamp\":1540309977457,\"akaId\":\"abc1234567\",\"akaFirstName\":\"abc1234567\",\"akaLastName\":\"abc1234567\",\"akaMiddleName\":\"abc1234567\",\"akaNamePrefixDescription\":\"abc1234567\",\"akaNameType\":0,\"akaSuffixTitleDescription\":\"abc1234567\",\"akaLastUpdatedId\":\"abc1234567\",\"akaLastUpdatedTimestamp\":1540309977457,\"akaLastUpdatedOperation\":\"I\",\"akaReplicationTimestamp\":1540309977457}";
    final EsClientPerson expected = JsonUtils.from(json, EsClientPerson.class);

    // Generated at runtime
    expected.setLastChange(actual.getLastChange());
    expected.setCltLastUpdatedTime(actual.getCltLastUpdatedTime());
    expected.setClaLastUpdatedTime(actual.getClaLastUpdatedTime());
    expected.setAdrLastUpdatedTime(actual.getAdrLastUpdatedTime());
    expected.setAkaLastUpdatedTimestamp(actual.getAkaLastUpdatedTimestamp());
    expected.setSafetyAlertLastUpdatedTimestamp(actual.getSafetyAlertLastUpdatedTimestamp());

    expected.setAkaReplicationTimestamp(actual.getAkaReplicationTimestamp());
    expected.setCsecReplicationTimestamp(actual.getCsecReplicationTimestamp());

    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = SQLException.class)
  public void extract_A$ResultSet_T$SQLException() throws Exception {
    when(rs.next()).thenThrow(SQLException.class);
    when(rs.getString(any(String.class))).thenThrow(SQLException.class);
    EsClientPerson.extract(rs);
  }

  @Test
  public void makeReplicatedClient_A$() throws Exception {
    ReplicatedClient actual = target.makeReplicatedClient();
    // ReplicatedClient expected = null;
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getNormalizationClass_A$() throws Exception {
    Class<ReplicatedClient> actual = target.getNormalizationClass();
    Class<ReplicatedClient> expected = ReplicatedClient.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_A$Map() throws Exception {
    final Map<Object, ReplicatedClient> map = new HashMap<Object, ReplicatedClient>();
    final ReplicatedClient actual = target.normalize(map);

    // final String json =
    // "{\"activePlacementHomeAddress\":null,\"adjudicatedDelinquentIndicator\":\"\",\"adoptionStatusCode\":\"\","
    // +
    // "\"akas\":{},\"alienRegistrationNumber\":\"\",\"birthCity\":\"\",\"birthCountryCodeType\":null,"
    // +
    // "\"birthDate\":null,\"birthFacilityName\":\"\",\"birthStateCodeType\":null,\"birthplaceVerifiedIndicator\":\"\","
    // +
    // "\"childClientIndicatorVar\":\"\",\"clientAddresses\":[],\"clientCounties\":[],\"clientIndexNumber\":\"\","
    // + "\"clientRaces\":[],\"clientSafetyAlerts\":[],\"commentDescription\":\"\","
    // + "\"commonFirstName\":\"\",\"commonLastName\":\"\",\"commonMiddleName\":\"\","
    // + "\"confidentialityActionDate\":null,\"confidentialityInEffectIndicator\":\"\","
    // +
    // "\"creationDate\":null,\"csecs\":{},\"currCaChildrenServIndicator\":\"\",\"currentlyOtherDescription\":\"\",\"currentlyRegionalCenterIndicator\":\"\",\"deathDate\":null,\"deathDateVerifiedIndicator\":\"\",\"deathPlace\":\"\",\"deathReasonText\":\"\",\"driverLicenseNumber\":\"\",\"driverLicenseStateCodeType\":null,\"emailAddress\":\"\",\"estimatedDobCode\":\"\",\"ethUnableToDetReasonCode\":\"\",\"fatherParentalRightTermDate\":null,\"genderCode\":\"\",\"genderExpressionType\":null,\"genderIdentityType\":null,\"giNotListedDescription\":null,\"healthSummaryText\":\"\",\"hispUnableToDetReasonCode\":\"\",\"hispanicOriginCode\":\"\",\"id\":\"\",\"immigrationCountryCodeType\":null,\"immigrationStatusType\":null,\"incapacitatedParentCode\":\"\",\"individualHealthCarePlanIndicator\":\"\",\"lastUpdatedId\":null,\"lastUpdatedTime\":null,\"legacyDescriptor\":{},\"legacyId\":\"\",\"limitationOnScpHealthIndicator\":\"\",\"limitedAccessCode\":\"\",\"literateCode\":\"\",\"maritalCohabitatnHstryIndicatorVar\":\"\",\"maritalStatusType\":null,\"militaryStatusCode\":\"\",\"motherParentalRightTermDate\":null,\"namePrefixDescription\":\"\",\"nameType\":null,\"openCaseId\":null,\"openCaseResponsibleAgencyCode\":null,\"otherClientNames\":[],\"outstandingWarrantIndicator\":\"\",\"prevCaChildrenServIndicator\":\"\",\"prevOtherDescription\":\"\",\"prevRegionalCenterIndicator\":\"\",\"primaryEthnicityType\":null,\"primaryKey\":\"\",\"primaryLanguageType\":null,\"raceAndEthnicity\":{\"race_codes\":[],\"hispanic_codes\":[],\"unable_to_determine_code\":\"\",\"hispanic_origin_code\":\"\",\"hispanic_unable_to_determine_code\":\"\"},\"religionType\":null,\"replicatedEntity\":{\"replicationOperation\":null,\"replicationDate\":null},\"replicationDate\":null,\"replicationOperation\":null,\"safetyAlerts\":{},\"secondaryLanguageType\":null,\"sensitiveHlthInfoOnFileIndicator\":\"\",\"sensitivityIndicator\":\"\",\"sexualOrientationType\":null,\"soNotListedDescrption\":null,\"soUnableToDetermineCode\":\"\",\"soc158PlacementCode\":\"\",\"soc158SealedClientIndicator\":\"\",\"socialSecurityNumChangedCode\":\"\",\"socialSecurityNumber\":\"\",\"suffixTitleDescription\":\"\",\"tribalAncestryClientIndicatorVar\":\"\",\"tribalMembrshpVerifctnIndicatorVar\":\"\",\"unemployedParentCode\":\"\",\"zippyCreatedIndicator\":\"\"}";
    // final ReplicatedClient expected = JsonUtils.from(json, ReplicatedClient.class);
    // assertThat(actual, is(equalTo(expected)));

    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getPrimaryKey_A$() throws Exception {
    Serializable actual = target.getPrimaryKey();
    Serializable expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCltFatherParentalRightTermDate_A$() throws Exception {
    Date actual = target.getCltFatherParentalRightTermDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClaId_A$String() throws Exception {
    String claId = null;
    target.setClaId(claId);
  }

  @Test
  public void getClientCountyId_A$() throws Exception {
    String actual = target.getClientCountyId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getClientEthnicityId_A$() throws Exception {
    String actual = target.getClientEthnicityId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getClientEthnicityCode_A$() throws Exception {
    Short actual = target.getClientEthnicityCode();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getClientCountyRule_A$() throws Exception {
    String actual = target.getClientCountyRule();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClientCountyRule_A$String() throws Exception {
    String clientCountyRule = null;
    target.setClientCountyRule(clientCountyRule);
  }

  @Test
  public void setClientCountyId_A$String() throws Exception {
    String clientCountyId = null;
    target.setClientCountyId(clientCountyId);
  }

  @Test
  public void setClientEthnicityId_A$String() throws Exception {
    String clientEthnicityId = null;
    target.setClientEthnicityId(clientEthnicityId);
  }

  @Test
  public void setClientEthnicityCode_A$Short() throws Exception {
    Short clientEthnicityCode = null;
    target.setClientEthnicityCode(clientEthnicityCode);
  }

  @Test
  public void setAdrReplicationOperation_A$CmsReplicationOperation() throws Exception {
    final CmsReplicationOperation adrReplicationOperation = CmsReplicationOperation.U;
    target.setAdrReplicationOperation(adrReplicationOperation);
  }

  @Test
  public void getSafetyAlertId_A$() throws Exception {
    String actual = target.getSafetyAlertId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertId_A$String() throws Exception {
    String safetyAlertId = null;
    target.setSafetyAlertId(safetyAlertId);
  }

  @Test
  public void getSafetyAlertActivationReasonCode_A$() throws Exception {
    Short actual = target.getSafetyAlertActivationReasonCode();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertActivationReasonCode_A$Short() throws Exception {
    Short safetyAlertActivationReasonCode = null;
    target.setSafetyAlertActivationReasonCode(safetyAlertActivationReasonCode);
  }

  @Test
  public void getSafetyAlertActivationDate_A$() throws Exception {
    Date actual = target.getSafetyAlertActivationDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertActivationDate_A$Date() throws Exception {
    Date safetyAlertActivationDate = mock(Date.class);
    target.setSafetyAlertActivationDate(safetyAlertActivationDate);
  }

  @Test
  public void getSafetyAlertActivationCountyCode_A$() throws Exception {
    Short actual = target.getSafetyAlertActivationCountyCode();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertActivationCountyCode_A$Short() throws Exception {
    Short safetyAlertActivationCountyCode = null;
    target.setSafetyAlertActivationCountyCode(safetyAlertActivationCountyCode);
  }

  @Test
  public void getSafetyAlertActivationExplanation_A$() throws Exception {
    String actual = target.getSafetyAlertActivationExplanation();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertActivationExplanation_A$String() throws Exception {
    String safetyAlertActivationExplanation = null;
    target.setSafetyAlertActivationExplanation(safetyAlertActivationExplanation);
  }

  @Test
  public void getSafetyAlertDeactivationDate_A$() throws Exception {
    Date actual = target.getSafetyAlertDeactivationDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertDeactivationDate_A$Date() throws Exception {
    Date safetyAlertDeactivationDate = mock(Date.class);
    target.setSafetyAlertDeactivationDate(safetyAlertDeactivationDate);
  }

  @Test
  public void getSafetyAlertDeactivationCountyCode_A$() throws Exception {
    Short actual = target.getSafetyAlertDeactivationCountyCode();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertDeactivationCountyCode_A$Short() throws Exception {
    Short safetyAlertDeactivationCountyCode = null;
    target.setSafetyAlertDeactivationCountyCode(safetyAlertDeactivationCountyCode);
  }

  @Test
  public void getSafetyAlertDeactivationExplanation_A$() throws Exception {
    String actual = target.getSafetyAlertDeactivationExplanation();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertDeactivationExplanation_A$String() throws Exception {
    String safetyAlertDeactivationExplanation = null;
    target.setSafetyAlertDeactivationExplanation(safetyAlertDeactivationExplanation);
  }

  @Test
  public void getSafetyAlertLastUpdatedId_A$() throws Exception {
    String actual = target.getSafetyAlertLastUpdatedId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertLastUpdatedId_A$String() throws Exception {
    String safetyAlertLastUpdatedId = null;
    target.setSafetyAlertLastUpdatedId(safetyAlertLastUpdatedId);
  }

  @Test
  public void getSafetyAlertLastUpdatedTimestamp_A$() throws Exception {
    Date actual = target.getSafetyAlertLastUpdatedTimestamp();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertLastUpdatedTimestamp_A$Date() throws Exception {
    Date safetyAlertLastUpdatedTimestamp = mock(Date.class);
    target.setSafetyAlertLastUpdatedTimestamp(safetyAlertLastUpdatedTimestamp);
  }

  @Test
  public void getSafetyAlertLastUpdatedOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getSafetyAlertLastUpdatedOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertLastUpdatedOperation_A$CmsReplicationOperation() throws Exception {
    CmsReplicationOperation safetyAlertLastUpdatedOperation = CmsReplicationOperation.U;
    target.setSafetyAlertLastUpdatedOperation(safetyAlertLastUpdatedOperation);
  }

  @Test
  public void getSafetyAlertReplicationTimestamp_A$() throws Exception {
    Date actual = target.getSafetyAlertReplicationTimestamp();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertReplicationTimestamp_A$Date() throws Exception {
    Date safetyAlertReplicationTimestamp = mock(Date.class);
    target.setSafetyAlertReplicationTimestamp(safetyAlertReplicationTimestamp);
  }

  @Test
  public void getAkaId_A$() throws Exception {
    String actual = target.getAkaId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaId_A$String() throws Exception {
    String akaId = null;
    target.setAkaId(akaId);
  }

  @Test
  public void getAkaFirstName_A$() throws Exception {
    String actual = target.getAkaFirstName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaFirstName_A$String() throws Exception {
    String akaFirstName = null;
    target.setAkaFirstName(akaFirstName);
  }

  @Test
  public void getAkaLastName_A$() throws Exception {
    String actual = target.getAkaLastName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaLastName_A$String() throws Exception {
    String akaLastName = null;
    target.setAkaLastName(akaLastName);
  }

  @Test
  public void getAkaMiddleName_A$() throws Exception {
    String actual = target.getAkaMiddleName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaMiddleName_A$String() throws Exception {
    String akaMiddleName = null;
    target.setAkaMiddleName(akaMiddleName);
  }

  @Test
  public void getAkaNamePrefixDescription_A$() throws Exception {
    String actual = target.getAkaNamePrefixDescription();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaNamePrefixDescription_A$String() throws Exception {
    String akaNamePrefixDescription = null;
    target.setAkaNamePrefixDescription(akaNamePrefixDescription);
  }

  @Test
  public void getAkaNameType_A$() throws Exception {
    Short actual = target.getAkaNameType();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaNameType_A$Short() throws Exception {
    Short akaNameType = null;
    target.setAkaNameType(akaNameType);
  }

  @Test
  public void getAkaSuffixTitleDescription_A$() throws Exception {
    String actual = target.getAkaSuffixTitleDescription();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaSuffixTitleDescription_A$String() throws Exception {
    String akaSuffixTitleDescription = null;
    target.setAkaSuffixTitleDescription(akaSuffixTitleDescription);
  }

  @Test
  public void getAkaLastUpdatedId_A$() throws Exception {
    String actual = target.getAkaLastUpdatedId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaLastUpdatedId_A$String() throws Exception {
    String akaLastUpdatedId = null;
    target.setAkaLastUpdatedId(akaLastUpdatedId);
  }

  @Test
  public void getAkaLastUpdatedTimestamp_A$() throws Exception {
    Date actual = target.getAkaLastUpdatedTimestamp();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaLastUpdatedTimestamp_A$Date() throws Exception {
    Date akaLastUpdatedTimestamp = mock(Date.class);
    target.setAkaLastUpdatedTimestamp(akaLastUpdatedTimestamp);
  }

  @Test
  public void getAkaLastUpdatedOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getAkaLastUpdatedOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaLastUpdatedOperation_A$CmsReplicationOperation() throws Exception {
    final CmsReplicationOperation akaLastUpdatedOperation = CmsReplicationOperation.U;
    target.setAkaLastUpdatedOperation(akaLastUpdatedOperation);
  }

  @Test
  public void getAkaReplicationTimestamp_A$() throws Exception {
    Date actual = target.getAkaReplicationTimestamp();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaReplicationTimestamp_A$Date() throws Exception {
    Date akaReplicationTimestamp = mock(Date.class);
    target.setAkaReplicationTimestamp(akaReplicationTimestamp);
  }

  @Test
  public void compare_A$EsClientPerson$EsClientPerson() throws Exception {
    final EsClientPerson o1 = new EsClientPerson();
    o1.setCltId(DEFAULT_CLIENT_ID);

    final EsClientPerson o2 = new EsClientPerson();
    o2.setCltId(DEFAULT_CLIENT_ID);

    final int actual = target.compare(o1, o2);
    final int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void compareTo_A$EsClientPerson() throws Exception {
    EsClientPerson o = new EsClientPerson();
    o.setCltId(DEFAULT_CLIENT_ID);

    int actual = target.compareTo(o);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void hashCode_A$() throws Exception {
    int actual = target.hashCode();
    int expected = 0;
    assertThat(actual, is(not(expected)));
  }

  @Test
  public void equals_A$Object() throws Exception {
    Object obj = null;
    boolean actual = target.equals(obj);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

}
