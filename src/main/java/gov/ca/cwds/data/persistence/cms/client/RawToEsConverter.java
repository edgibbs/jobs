package gov.ca.cwds.data.persistence.cms.client;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.common.NameSuffixTranslator;
import gov.ca.cwds.data.es.ElasticSearchPersonAka;
import gov.ca.cwds.data.es.ElasticSearchPersonCsec;
import gov.ca.cwds.data.es.ElasticSearchSafetyAlert;
import gov.ca.cwds.data.es.ElasticSearchSystemCode;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClientAddress;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.DomainChef;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;

/**
 * Convert "raw" (JDBC) object hierarchy to Elasticsearch ready beans.
 * 
 * @author CWDS API Team
 * @see RawClient
 * @see ReplicatedClient
 */
public class RawToEsConverter {

  protected static final Logger LOGGER = LoggerFactory.getLogger(RawToEsConverter.class);

  public ReplicatedClient convert(RawClient rawCli) {
    final ReplicatedClient rc = new ReplicatedClient();
    rc.setAdjudicatedDelinquentIndicator(rawCli.getCltAdjudicatedDelinquentIndicator());
    rc.setAdoptionStatusCode(rawCli.getCltAdoptionStatusCode());
    rc.setAlienRegistrationNumber(rawCli.getCltAlienRegistrationNumber());
    rc.setBirthCity(rawCli.getCltBirthCity());
    rc.setBirthCountryCodeType(rawCli.getCltBirthCountryCodeType());
    rc.setBirthDate(rawCli.getCltBirthDate());
    rc.setBirthFacilityName(rawCli.getCltBirthFacilityName());
    rc.setBirthplaceVerifiedIndicator(rawCli.getCltBirthplaceVerifiedIndicator());
    rc.setBirthStateCodeType(rawCli.getCltBirthStateCodeType());
    rc.setChildClientIndicatorVar(rawCli.getCltChildClientIndicatorVar());
    rc.setClientIndexNumber(rawCli.getCltClientIndexNumber());
    rc.setCommentDescription(rawCli.getCltCommentDescription());
    rc.setCommonFirstName(rawCli.getCltCommonFirstName());
    rc.setCommonLastName(rawCli.getCltCommonLastName());
    rc.setCommonMiddleName(rawCli.getCltCommonMiddleName());
    rc.setConfidentialityActionDate(rawCli.getCltConfidentialityActionDate());
    rc.setConfidentialityInEffectIndicator(rawCli.getCltConfidentialityInEffectIndicator());
    rc.setCreationDate(rawCli.getCltCreationDate());
    rc.setCurrCaChildrenServIndicator(rawCli.getCltCurrCaChildrenServIndicator());
    rc.setCurrentlyOtherDescription(rawCli.getCltCurrentlyOtherDescription());
    rc.setCurrentlyRegionalCenterIndicator(rawCli.getCltCurrentlyRegionalCenterIndicator());
    rc.setDeathDate(rawCli.getCltDeathDate());
    rc.setDeathDateVerifiedIndicator(rawCli.getCltDeathDateVerifiedIndicator());
    rc.setDeathPlace(rawCli.getCltDeathPlace());
    rc.setDeathReasonText(rawCli.getCltDeathReasonText());
    rc.setDriverLicenseNumber(rawCli.getCltDriverLicenseNumber());
    rc.setDriverLicenseStateCodeType(rawCli.getCltDriverLicenseStateCodeType());
    rc.setEmailAddress(rawCli.getCltEmailAddress());
    rc.setEstimatedDobCode(rawCli.getCltEstimatedDobCode());
    rc.setEthUnableToDetReasonCode(rawCli.getCltEthUnableToDetReasonCode());
    rc.setFatherParentalRightTermDate(rawCli.getCltFatherParentalRightTermDate());
    rc.setCommonFirstName(rawCli.getCltCommonFirstName());
    rc.setGenderCode(rawCli.getCltGenderCode());
    rc.setHealthSummaryText(rawCli.getCltHealthSummaryText());
    rc.setHispanicOriginCode(rawCli.getCltHispanicOriginCode());
    rc.setHispUnableToDetReasonCode(rawCli.getCltHispUnableToDetReasonCode());
    rc.setId(rawCli.getCltId());
    rc.setImmigrationCountryCodeType(rawCli.getCltImmigrationCountryCodeType());
    rc.setImmigrationStatusType(rawCli.getCltImmigrationStatusType());
    rc.setIncapacitatedParentCode(rawCli.getCltIncapacitatedParentCode());
    rc.setIndividualHealthCarePlanIndicator(rawCli.getCltIndividualHealthCarePlanIndicator());
    rc.setCommonLastName(rawCli.getCltCommonLastName());
    rc.setLimitationOnScpHealthIndicator(rawCli.getCltLimitationOnScpHealthIndicator());
    rc.setLiterateCode(rawCli.getCltLiterateCode());
    rc.setMaritalCohabitatnHstryIndicatorVar(rawCli.getCltMaritalCohabitatnHstryIndicatorVar());
    rc.setMaritalStatusType(rawCli.getCltMaritalStatusType());
    rc.setCommonMiddleName(rawCli.getCltCommonMiddleName());
    rc.setMilitaryStatusCode(rawCli.getCltMilitaryStatusCode());
    rc.setMotherParentalRightTermDate(rawCli.getCltMotherParentalRightTermDate());
    rc.setNamePrefixDescription(rawCli.getCltNamePrefixDescription());
    rc.setNameType(rawCli.getCltNameType());
    rc.setOutstandingWarrantIndicator(rawCli.getCltOutstandingWarrantIndicator());
    rc.setPrevCaChildrenServIndicator(rawCli.getCltPrevCaChildrenServIndicator());
    rc.setPrevOtherDescription(rawCli.getCltPrevOtherDescription());
    rc.setPrevRegionalCenterIndicator(rawCli.getCltPrevRegionalCenterIndicator());
    rc.setPrimaryEthnicityType(rawCli.getCltPrimaryEthnicityType());

    // Languages
    rc.setPrimaryLanguageType(rawCli.getCltPrimaryLanguageType());
    rc.setSecondaryLanguageType(rawCli.getCltSecondaryLanguageType());

    rc.setReligionType(rawCli.getCltReligionType());
    rc.setSensitiveHlthInfoOnFileIndicator(rawCli.getCltSensitiveHlthInfoOnFileIndicator());
    rc.setSensitivityIndicator(rawCli.getCltSensitivityIndicator());
    rc.setSoc158PlacementCode(rawCli.getCltSoc158PlacementCode());
    rc.setSoc158SealedClientIndicator(rawCli.getCltSoc158SealedClientIndicator());
    rc.setSocialSecurityNumber(rawCli.getCltSocialSecurityNumber());
    rc.setSocialSecurityNumChangedCode(rawCli.getCltSocialSecurityNumChangedCode());
    rc.setSuffixTitleDescription(rawCli.getCltSuffixTitleDescription());
    rc.setTribalAncestryClientIndicatorVar(rawCli.getCltTribalAncestryClientIndicatorVar());
    rc.setTribalMembrshpVerifctnIndicatorVar(rawCli.getCltTribalMembrshpVerifctnIndicatorVar());
    rc.setUnemployedParentCode(rawCli.getCltUnemployedParentCode());
    rc.setZippyCreatedIndicator(rawCli.getCltZippyCreatedIndicator());

    rc.setReplicationDate(rawCli.getCltReplicationDate());
    rc.setReplicationOperation(rawCli.getCltReplicationOperation());
    rc.setLastUpdatedTime(rawCli.getCltLastUpdatedTime());

    final Collection<RawClientAddress> coll = rawCli.getClientAddress().values();
    LOGGER.trace("convert client address");
    for (RawClientAddress rca : coll) {
      convertClientAddress(rc, rca);
    }

    LOGGER.trace("convert client county");
    for (RawClientCounty cc : rawCli.getClientCounty()) {
      convertClientCounty(rc, cc);
    }

    LOGGER.trace("convert aka");
    for (RawAka aka : rawCli.getAka()) {
      convertAka(rc, aka);
    }

    LOGGER.trace("convert ethnicity");
    for (RawEthnicity eth : rawCli.getEthnicity()) {
      convertEthnicity(rc, rawCli, eth);
    }

    LOGGER.trace("convert safety alert");
    for (RawSafetyAlert saf : rawCli.getSafetyAlert()) {
      convertSafetyAlert(rc, saf);
    }

    LOGGER.trace("convert case");
    for (RawCase cas : rawCli.getCases()) {
      convertCase(rc, cas);
    }

    // SNAP-729: Neutron Initial Load: restore CSEC.
    LOGGER.trace("convert CSEC");
    for (RawCsec csec : rawCli.getCsec()) {
      convertCsec(rc, csec);
    }

    return rc;
  }

  protected void convertCsec(ReplicatedClient rc, RawCsec rawCsec) {
    if (StringUtils.isBlank(rawCsec.getCsecId())
        || CmsReplicationOperation.D == rawCsec.getCsecLastUpdatedOperation()) {
      return;
    }

    final ElasticSearchPersonCsec csec = new ElasticSearchPersonCsec();
    csec.setId(rawCsec.getCsecId());

    csec.setStartDate(DomainChef.cookDate(rawCsec.getCsecStartDate()));
    csec.setEndDate(DomainChef.cookDate(rawCsec.getCsecEndDate()));

    if (rawCsec.getCsecCodeId() != null && rawCsec.getCsecCodeId() > 0) {
      csec.setCsecCodeId(rawCsec.getCsecCodeId().toString());
      csec.setCsecDesc(
          SystemCodeCache.global().getSystemCodeShortDescription(rawCsec.getCsecCodeId()));
    }

    csec.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(rawCsec.getCsecId(),
        rawCsec.getCsecLastUpdatedTimestamp(), LegacyTable.CSEC_HISTORY));

    rc.addCsec(csec);
  }

  protected void convertCase(ReplicatedClient rc, RawCase rawCase) {
    rc.setOpenCaseId(rawCase.getOpenCaseId());
    rc.setOpenCaseResponsibleAgencyCode(rawCase.getOpenCaseResponsibleAgencyCode());
  }

  protected void convertSafetyAlert(ReplicatedClient rc, RawSafetyAlert rawSafetyAlert) {
    if (StringUtils.isBlank(rawSafetyAlert.getSafetyAlertId())
        || CmsReplicationOperation.D == rawSafetyAlert.getSafetyAlertLastUpdatedOperation()) {
      return;
    }

    final ElasticSearchSafetyAlert alert = new ElasticSearchSafetyAlert();
    alert.setId(rawSafetyAlert.getSafetyAlertId());

    final ElasticSearchSafetyAlert.Activation activation =
        new ElasticSearchSafetyAlert.Activation();
    alert.setActivation(activation);

    activation.setActivationReasonDescription(SystemCodeCache.global()
        .getSystemCodeShortDescription(rawSafetyAlert.getSafetyAlertActivationReasonCode()));
    activation.setActivationReasonId(rawSafetyAlert.getSafetyAlertActivationReasonCode() != null
        ? rawSafetyAlert.getSafetyAlertActivationReasonCode().toString()
        : null);

    final ElasticSearchSystemCode activationCounty = new ElasticSearchSystemCode();
    activation.setActivationCounty(activationCounty);
    activationCounty.setDescription(SystemCodeCache.global()
        .getSystemCodeShortDescription(rawSafetyAlert.getSafetyAlertActivationCountyCode()));
    activationCounty.setId(rawSafetyAlert.getSafetyAlertActivationCountyCode() != null
        ? rawSafetyAlert.getSafetyAlertActivationCountyCode().toString()
        : null);

    activation
        .setActivationDate(DomainChef.cookDate(rawSafetyAlert.getSafetyAlertActivationDate()));
    activation.setActivationExplanation(rawSafetyAlert.getSafetyAlertActivationExplanation());

    final ElasticSearchSafetyAlert.Deactivation deactivation =
        new ElasticSearchSafetyAlert.Deactivation();
    alert.setDeactivation(deactivation);

    final ElasticSearchSystemCode deactivationCounty = new ElasticSearchSystemCode();
    deactivation.setDeactivationCounty(deactivationCounty);

    deactivationCounty.setDescription(SystemCodeCache.global()
        .getSystemCodeShortDescription(rawSafetyAlert.getSafetyAlertDeactivationCountyCode()));
    deactivationCounty.setId(rawSafetyAlert.getSafetyAlertDeactivationCountyCode() != null
        ? rawSafetyAlert.getSafetyAlertDeactivationCountyCode().toString()
        : null);

    deactivation
        .setDeactivationDate(DomainChef.cookDate(rawSafetyAlert.getSafetyAlertDeactivationDate()));
    deactivation.setDeactivationExplanation(rawSafetyAlert.getSafetyAlertDeactivationExplanation());

    alert.setLegacyDescriptor(
        ElasticTransformer.createLegacyDescriptor(rawSafetyAlert.getSafetyAlertId(),
            rawSafetyAlert.getSafetyAlertLastUpdatedTimestamp(), LegacyTable.SAFETY_ALERT));

    rc.addSafetyAlert(alert);
  }

  protected void convertEthnicity(ReplicatedClient rc, RawClient rawCli,
      RawEthnicity rawEthnicity) {
    rc.addClientRace(rawEthnicity.getClientEthnicityCode());
  }

  protected void convertAka(ReplicatedClient rc, RawAka rawAka) {
    if (StringUtils.isBlank(rawAka.getAkaId())
        || CmsReplicationOperation.D == rawAka.getAkaLastUpdatedOperation()) {
      return;
    }

    final ElasticSearchPersonAka aka = new ElasticSearchPersonAka();
    aka.setId(rawAka.getAkaId());

    if (StringUtils.isNotBlank(rawAka.getAkaFirstName())) {
      aka.setFirstName(rawAka.getAkaFirstName().trim());
    }

    if (StringUtils.isNotBlank(rawAka.getAkaLastName())) {
      aka.setLastName(rawAka.getAkaLastName().trim());
    }

    if (StringUtils.isNotBlank(rawAka.getAkaMiddleName())) {
      aka.setMiddleName(rawAka.getAkaMiddleName().trim());
    }

    if (StringUtils.isNotBlank(rawAka.getAkaNamePrefixDescription())) {
      aka.setPrefix(rawAka.getAkaNamePrefixDescription().trim());
    }

    if (StringUtils.isNotBlank(rawAka.getAkaSuffixTitleDescription())) {
      aka.setSuffix(NameSuffixTranslator.translate(rawAka.getAkaSuffixTitleDescription().trim()));
    }

    if (rawAka.getAkaNameType() != null && rawAka.getAkaNameType().intValue() != 0) {
      aka.setNameType(
          SystemCodeCache.global().getSystemCodeShortDescription(rawAka.getAkaNameType()));
    }

    aka.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(rawAka.getAkaId(),
        rawAka.getAkaLastUpdatedTimestamp(), LegacyTable.ALIAS_OR_OTHER_CLIENT_NAME));

    rc.addAka(aka);
  }

  protected void convertClientCounty(ReplicatedClient rc, RawClientCounty rawCounty) {
    rc.addClientCounty(rawCounty.getClientCounty());
  }

  protected void convertClientAddress(ReplicatedClient rc, RawClientAddress rawCliAdr) {
    final ReplicatedClientAddress rca = new ReplicatedClientAddress();
    rca.setId(rawCliAdr.getClaId());
    rca.setAddressType(rawCliAdr.getClaAddressType());
    rca.setBkInmtId(rawCliAdr.getClaBkInmtId());
    rca.setEffEndDt(rawCliAdr.getClaEffectiveEndDate());
    rca.setEffStartDt(rawCliAdr.getClaEffectiveStartDate());
    rca.setFkAddress(rawCliAdr.getClaFkAddress());
    rca.setFkClient(rawCliAdr.getClaFkClient());
    rca.setFkReferral(rawCliAdr.getClaFkReferral());
    rca.setHomelessInd(rawCliAdr.getClaHomelessInd());

    rca.setReplicationDate(rawCliAdr.getClaReplicationDate());
    rca.setReplicationOperation(rawCliAdr.getClaReplicationOperation());
    rca.setLastUpdatedId(rawCliAdr.getClaLastUpdatedId());
    rca.setLastUpdatedTime(rawCliAdr.getClaLastUpdatedTime());
    rc.addClientAddress(rca);

    if (rawCliAdr.getAddress() != null) {
      rca.addAddress(convertAddress(rc, rca, rawCliAdr, rawCliAdr.getAddress()));
    }
  }

  protected ReplicatedAddress convertAddress(ReplicatedClient rc, ReplicatedClientAddress repCa,
      RawClientAddress rawCa, RawAddress rawAdr) {
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

    return adr;
  }

}
