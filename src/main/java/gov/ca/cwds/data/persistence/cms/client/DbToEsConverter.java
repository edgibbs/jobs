package gov.ca.cwds.data.persistence.cms.client;

import gov.ca.cwds.data.persistence.cms.rep.ReplicatedAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClientAddress;

public class DbToEsConverter {

  public ReplicatedClient convert(RawClient raw) {
    final ReplicatedClient ret = new ReplicatedClient();
    ret.setAdjudicatedDelinquentIndicator(raw.getCltAdjudicatedDelinquentIndicator());
    ret.setAdoptionStatusCode(raw.getCltAdoptionStatusCode());
    ret.setAlienRegistrationNumber(raw.getCltAlienRegistrationNumber());
    ret.setBirthCity(raw.getCltBirthCity());
    ret.setBirthCountryCodeType(raw.getCltBirthCountryCodeType());
    ret.setBirthDate(raw.getCltBirthDate());
    ret.setBirthFacilityName(raw.getCltBirthFacilityName());
    ret.setBirthplaceVerifiedIndicator(raw.getCltBirthplaceVerifiedIndicator());
    ret.setBirthStateCodeType(raw.getCltBirthStateCodeType());
    ret.setChildClientIndicatorVar(raw.getCltChildClientIndicatorVar());
    ret.setClientIndexNumber(raw.getCltClientIndexNumber());
    ret.setCommentDescription(raw.getCltCommentDescription());
    ret.setCommonFirstName(raw.getCltCommonFirstName());
    ret.setCommonLastName(raw.getCltCommonLastName());
    ret.setCommonMiddleName(raw.getCltCommonMiddleName());
    ret.setConfidentialityActionDate(raw.getCltConfidentialityActionDate());
    ret.setConfidentialityInEffectIndicator(raw.getCltConfidentialityInEffectIndicator());
    ret.setCreationDate(raw.getCltCreationDate());
    ret.setCurrCaChildrenServIndicator(raw.getCltCurrCaChildrenServIndicator());
    ret.setCurrentlyOtherDescription(raw.getCltCurrentlyOtherDescription());
    ret.setCurrentlyRegionalCenterIndicator(raw.getCltCurrentlyRegionalCenterIndicator());
    ret.setDeathDate(raw.getCltDeathDate());
    ret.setDeathDateVerifiedIndicator(raw.getCltDeathDateVerifiedIndicator());
    ret.setDeathPlace(raw.getCltDeathPlace());
    ret.setDeathReasonText(raw.getCltDeathReasonText());
    ret.setDriverLicenseNumber(raw.getCltDriverLicenseNumber());
    ret.setDriverLicenseStateCodeType(raw.getCltDriverLicenseStateCodeType());
    ret.setEmailAddress(raw.getCltEmailAddress());
    ret.setEstimatedDobCode(raw.getCltEstimatedDobCode());
    ret.setEthUnableToDetReasonCode(raw.getCltEthUnableToDetReasonCode());
    ret.setFatherParentalRightTermDate(raw.getCltFatherParentalRightTermDate());
    ret.setCommonFirstName(raw.getCltCommonFirstName());
    ret.setGenderCode(raw.getCltGenderCode());
    ret.setHealthSummaryText(raw.getCltHealthSummaryText());
    ret.setHispanicOriginCode(raw.getCltHispanicOriginCode());
    ret.setHispUnableToDetReasonCode(raw.getCltHispUnableToDetReasonCode());
    ret.setId(raw.getCltId());
    ret.setImmigrationCountryCodeType(raw.getCltImmigrationCountryCodeType());
    ret.setImmigrationStatusType(raw.getCltImmigrationStatusType());
    ret.setIncapacitatedParentCode(raw.getCltIncapacitatedParentCode());
    ret.setIndividualHealthCarePlanIndicator(raw.getCltIndividualHealthCarePlanIndicator());
    ret.setCommonLastName(raw.getCltCommonLastName());
    ret.setLimitationOnScpHealthIndicator(raw.getCltLimitationOnScpHealthIndicator());
    ret.setLiterateCode(raw.getCltLiterateCode());
    ret.setMaritalCohabitatnHstryIndicatorVar(raw.getCltMaritalCohabitatnHstryIndicatorVar());
    ret.setMaritalStatusType(raw.getCltMaritalStatusType());
    ret.setCommonMiddleName(raw.getCltCommonMiddleName());
    ret.setMilitaryStatusCode(raw.getCltMilitaryStatusCode());
    ret.setMotherParentalRightTermDate(raw.getCltMotherParentalRightTermDate());
    ret.setNamePrefixDescription(raw.getCltNamePrefixDescription());
    ret.setNameType(raw.getCltNameType());
    ret.setOutstandingWarrantIndicator(raw.getCltOutstandingWarrantIndicator());
    ret.setPrevCaChildrenServIndicator(raw.getCltPrevCaChildrenServIndicator());
    ret.setPrevOtherDescription(raw.getCltPrevOtherDescription());
    ret.setPrevRegionalCenterIndicator(raw.getCltPrevRegionalCenterIndicator());
    ret.setPrimaryEthnicityType(raw.getCltPrimaryEthnicityType());

    // Languages
    ret.setPrimaryLanguageType(raw.getCltPrimaryLanguageType());
    ret.setSecondaryLanguageType(raw.getCltSecondaryLanguageType());

    ret.setReligionType(raw.getCltReligionType());
    ret.setSensitiveHlthInfoOnFileIndicator(raw.getCltSensitiveHlthInfoOnFileIndicator());
    ret.setSensitivityIndicator(raw.getCltSensitivityIndicator());
    ret.setSoc158PlacementCode(raw.getCltSoc158PlacementCode());
    ret.setSoc158SealedClientIndicator(raw.getCltSoc158SealedClientIndicator());
    ret.setSocialSecurityNumber(raw.getCltSocialSecurityNumber());
    ret.setSocialSecurityNumChangedCode(raw.getCltSocialSecurityNumChangedCode());
    ret.setSuffixTitleDescription(raw.getCltSuffixTitleDescription());
    ret.setTribalAncestryClientIndicatorVar(raw.getCltTribalAncestryClientIndicatorVar());
    ret.setTribalMembrshpVerifctnIndicatorVar(raw.getCltTribalMembrshpVerifctnIndicatorVar());
    ret.setUnemployedParentCode(raw.getCltUnemployedParentCode());
    ret.setZippyCreatedIndicator(raw.getCltZippyCreatedIndicator());

    ret.setReplicationDate(raw.getCltReplicationDate());
    ret.setReplicationOperation(raw.getCltReplicationOperation());
    ret.setLastUpdatedTime(raw.getCltLastUpdatedTime());
    return ret;
  }

  protected void convertClientAddress(ReplicatedClient rc, RawClient rawClient,
      RawClientAddress raw) {
    final ReplicatedClientAddress rca = new ReplicatedClientAddress();
    rca.setId(raw.getClaId());
    rca.setAddressType(raw.getClaAddressType());
    rca.setBkInmtId(raw.getClaBkInmtId());
    rca.setEffEndDt(raw.getClaEffectiveEndDate());
    rca.setEffStartDt(raw.getClaEffectiveStartDate());
    rca.setFkAddress(raw.getClaFkAddress());
    rca.setFkClient(raw.getClaFkClient());
    rca.setFkReferral(raw.getClaFkReferral());
    rca.setHomelessInd(raw.getClaHomelessInd());

    rca.setReplicationDate(raw.getClaReplicationDate());
    rca.setReplicationOperation(raw.getClaReplicationOperation());
    rca.setLastUpdatedId(raw.getClaLastUpdatedId());
    rca.setLastUpdatedTime(raw.getClaLastUpdatedTime());
    rc.addClientAddress(rca);
  }

  protected void convertAddress(ReplicatedClient rc, ReplicatedClientAddress outCa,
      RawClient rawCli, RawClientAddress rawCa, RawAddress rawAdr) {
    final ReplicatedAddress adr = new ReplicatedAddress();
    adr.setId(rawAdr.getAdrId());
    adr.setAddressDescription(rawAdr.getAdrAddressDescription());
    adr.setCity(rawAdr.getAdrCity());

    adr.setFrgAdrtB(rawAdr.getAdrFrgAdrtB());
    adr.setGovernmentEntityCd(rawAdr.getAdrGovernmentEntityCd());
    adr.setHeaderAddress(rawAdr.getAdrHeaderAddress());

    adr.setPostDirCd(rawAdr.getAdrPostDirCd());
    adr.setPreDirCd(rawAdr.getAdrPreDirCd());

    // NOTE: no way to figure out phone type from "primary phone". Home? Work? Cell? dunno.
    adr.setPrimaryExtension(rawAdr.getAdrPrimaryExtension());
    adr.setPrimaryNumber(rawAdr.getAdrPrimaryNumber());

    adr.setEmergencyExtension(rawAdr.getAdrEmergencyExtension());
    adr.setEmergencyNumber(rawAdr.getAdrEmergencyNumber());

    // This is *likely* a cell phone but *not guaranteed*.
    adr.setMessageExtension(rawAdr.getAdrMessageExtension());
    adr.setMessageNumber(rawAdr.getAdrMessageNumber());

    adr.setState(rawAdr.getAdrState());
    adr.setStateCd(rawAdr.getAdrState());
    adr.setStreetName(rawAdr.getAdrStreetName());
    adr.setStreetNumber(rawAdr.getAdrStreetNumber());
    adr.setStreetSuffixCd(rawAdr.getAdrStreetSuffixCd());
    adr.setUnitDesignationCd(rawAdr.getAdrUnitDesignationCd());
    adr.setUnitNumber(rawAdr.getAdrUnitNumber());
    adr.setZip(rawAdr.getAdrZip());
    adr.setZip4(rawAdr.getAdrZip4());

    adr.setReplicationDate(rawAdr.getAdrReplicationDate());
    adr.setReplicationOperation(rawAdr.getAdrReplicationOperation());
    adr.setLastUpdatedId(rawCa.getClaLastUpdatedId());
    adr.setLastUpdatedTime(rawAdr.getAdrLastUpdatedTime());
    outCa.addAddress(adr);
  }


}
