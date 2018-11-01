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
import gov.ca.cwds.neutron.jetpack.CheeseRay;
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
    final Short s = rawCli.getCltPrimaryLanguageType();
    if (s != null && s.shortValue() != 0 && s.shortValue() != 1253) {
      LOGGER.trace("Not English");
    }

    rc.setPrimaryLanguageType(s);
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

    int counter = 0;
    final Collection<RawClientAddress> coll = rawCli.getClientAddress().values();
    LOGGER.trace("convert client address: count: {}", coll.size());
    for (RawClientAddress rca : coll) {
      CheeseRay.logEvery(LOGGER, 1000, ++counter, "Convert client address", "ca");
      convertClientAddress(rc, rawCli, rca);
    }

    counter = 0;
    LOGGER.trace("convert client county");
    for (RawClientCounty cc : rawCli.getClientCounty()) {
      CheeseRay.logEvery(LOGGER, 1000, ++counter, "Convert client county", "cc");
      convertClientCounty(rc, cc);
    }

    counter = 0;
    LOGGER.trace("convert aka");
    for (RawAka aka : rawCli.getAka()) {
      CheeseRay.logEvery(LOGGER, 1000, ++counter, "Convert aka", "aka");
      convertAka(rc, aka);
    }

    counter = 0;
    LOGGER.trace("convert ethnicity");
    for (RawEthnicity eth : rawCli.getEthnicity()) {
      CheeseRay.logEvery(LOGGER, 1000, ++counter, "Convert ethnicity", "eth");
      convertEthnicity(rc, eth);
    }

    counter = 0;
    LOGGER.trace("convert safety alert");
    for (RawSafetyAlert saf : rawCli.getSafetyAlert()) {
      CheeseRay.logEvery(LOGGER, 1000, ++counter, "Convert safety alert", "saf");
      convertSafetyAlert(rc, rawCli, saf);
    }

    LOGGER.trace("convert case");
    for (RawCase cas : rawCli.getCases()) {
      CheeseRay.logEvery(LOGGER, 1000, ++counter, "Convert case", "cas");
      convertCase(rc, cas);
    }

    // SNAP-729: Neutron Initial Load: restore CSEC.
    LOGGER.trace("convert CSEC");
    for (RawCsec csec : rawCli.getCsec()) {
      CheeseRay.logEvery(LOGGER, 1000, ++counter, "Convert CSEC", "csec");
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

  protected void convertSafetyAlert(ReplicatedClient rc, RawClient rawCli, RawSafetyAlert rawSaf) {
    if (StringUtils.isBlank(rawSaf.getSafetyAlertId())
        || CmsReplicationOperation.D == rawSaf.getSafetyAlertLastUpdatedOperation()) {
      return;
    }

    final ElasticSearchSafetyAlert alert = new ElasticSearchSafetyAlert();
    alert.setId(rawSaf.getSafetyAlertId());

    final ElasticSearchSafetyAlert.Activation activation =
        new ElasticSearchSafetyAlert.Activation();
    alert.setActivation(activation);

    activation.setActivationReasonDescription(SystemCodeCache.global()
        .getSystemCodeShortDescription(rawSaf.getSafetyAlertActivationReasonCode()));
    activation.setActivationReasonId(rawSaf.getSafetyAlertActivationReasonCode() != null
        ? rawSaf.getSafetyAlertActivationReasonCode().toString()
        : null);

    final ElasticSearchSystemCode activationCounty = new ElasticSearchSystemCode();
    activation.setActivationCounty(activationCounty);
    activationCounty.setDescription(SystemCodeCache.global()
        .getSystemCodeShortDescription(rawSaf.getSafetyAlertActivationCountyCode()));
    activationCounty.setId(rawSaf.getSafetyAlertActivationCountyCode() != null
        ? rawSaf.getSafetyAlertActivationCountyCode().toString()
        : null);

    activation.setActivationDate(DomainChef.cookDate(rawSaf.getSafetyAlertActivationDate()));
    activation.setActivationExplanation(rawSaf.getSafetyAlertActivationExplanation());

    final ElasticSearchSafetyAlert.Deactivation deactivation =
        new ElasticSearchSafetyAlert.Deactivation();
    alert.setDeactivation(deactivation);

    final ElasticSearchSystemCode deactivationCounty = new ElasticSearchSystemCode();
    deactivation.setDeactivationCounty(deactivationCounty);

    deactivationCounty.setDescription(SystemCodeCache.global()
        .getSystemCodeShortDescription(rawSaf.getSafetyAlertDeactivationCountyCode()));
    deactivationCounty.setId(rawSaf.getSafetyAlertDeactivationCountyCode() != null
        ? rawSaf.getSafetyAlertDeactivationCountyCode().toString()
        : null);

    deactivation.setDeactivationDate(DomainChef.cookDate(rawSaf.getSafetyAlertDeactivationDate()));
    deactivation.setDeactivationExplanation(rawSaf.getSafetyAlertDeactivationExplanation());

    alert.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(rawSaf.getSafetyAlertId(),
        rawSaf.getSafetyAlertLastUpdatedTimestamp(), LegacyTable.SAFETY_ALERT));

    rc.addSafetyAlert(alert);
  }

  protected void convertEthnicity(ReplicatedClient rc, RawEthnicity rawEthnicity) {
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

  protected void convertClientAddress(ReplicatedClient rc, RawClient rawCli,
      RawClientAddress rawCliAdr) {
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
      rca.addAddress(convertAddress(rca, rawCli, rawCliAdr, rawCliAdr.getAddress()));
    }
  }

  protected ReplicatedAddress convertAddress(ReplicatedClientAddress repCa, RawClient rawCli,
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
