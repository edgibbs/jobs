package gov.ca.cwds.neutron.util.transform;

import static gov.ca.cwds.data.persistence.cms.CmsPersistentObject.CMS_ID_LEN;

import gov.ca.cwds.data.es.ElasticSearchPersonCsec;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.dao.ApiClientCaseAware;
import gov.ca.cwds.dao.ApiClientCountyAware;
import gov.ca.cwds.dao.ApiClientRaceAndEthnicityAware;
import gov.ca.cwds.dao.ApiClientSafetyAlertsAware;
import gov.ca.cwds.dao.ApiLegacyAware;
import gov.ca.cwds.dao.ApiMultipleClientAddressAware;
import gov.ca.cwds.dao.ApiMultiplePersonAware;
import gov.ca.cwds.dao.ApiOtherClientNamesAware;
import gov.ca.cwds.dao.ApiScreeningAware;
import gov.ca.cwds.data.ApiTypedIdentifier;
import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPerson.ESOptionalCollection;
import gov.ca.cwds.data.es.ElasticSearchPersonAddress;
import gov.ca.cwds.data.es.ElasticSearchPersonAka;
import gov.ca.cwds.data.es.ElasticSearchPersonLanguage;
import gov.ca.cwds.data.es.ElasticSearchPersonPhone;
import gov.ca.cwds.data.es.ElasticSearchPersonScreening;
import gov.ca.cwds.data.es.ElasticSearchRaceAndEthnicity;
import gov.ca.cwds.data.es.ElasticSearchSafetyAlert;
import gov.ca.cwds.data.es.ElasticSearchSystemCode;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.CmsKeyIdGenerator;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicatedEntity;
import gov.ca.cwds.data.std.ApiAddressAware;
import gov.ca.cwds.data.std.ApiLanguageAware;
import gov.ca.cwds.data.std.ApiMultipleLanguagesAware;
import gov.ca.cwds.data.std.ApiMultiplePhonesAware;
import gov.ca.cwds.data.std.ApiPersonAware;
import gov.ca.cwds.data.std.ApiPhoneAware;
import gov.ca.cwds.neutron.atom.AtomPersonDocPrep;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.rest.api.domain.DomainChef;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;

/**
 * Methods to transform {@link ApiPersonAware} into {@link ElasticSearchPerson}.
 * 
 * @author CWDS API Team
 */
public final class ElasticTransformer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticTransformer.class);

  private static ObjectMapper mapper;

  private ElasticTransformer() {
    // Static methods, don't instantiate.
  }

  /**
   * Serialize object to JSON.
   * 
   * @param obj object to serialize
   * @return JSON for this screening
   */
  public static String jsonify(Object obj) {
    String ret = "";
    try {
      ret = mapper.writeValueAsString(obj);
    } catch (Exception e) { // NOSONAR
      // HACK: shouldn't swallow the exception, but don't blow up a whole batch for one bad record.
      // Functional lambda doesn't allow checked exceptions, only runtime exceptions.
      // But then SonarQube complains about switching from checked to runtime.
      // Damned if you do, damned if you don't.
      LOGGER.warn("ERROR SERIALIZING OBJECT {} TO JSON", obj);
    }

    return ret;
  }

  public static void pushToBulkProcessor(final FlightLog flightLog, final BulkProcessor bp,
      final DocWriteRequest<?> t) {
    CheeseRay.logEvery(flightLog.incrementBulkPrepared(), "add to ES bulk", "index doc");
    bp.add(t);
  }

  protected static String determineId(final ApiLegacyAware l, final ElasticSearchPerson esp) {
    String id;
    final String legacyId = StringUtils.trimToEmpty(l.getLegacyId());
    final boolean hasLegacyId = legacyId.length() == CMS_ID_LEN;

    if (hasLegacyId) {
      id = legacyId;
      esp.setLegacyId(id);
    } else {
      id = esp.getId();
    }

    return id;
  }

  protected static String determineId(CmsReplicatedEntity l, final ElasticSearchPerson esp) {
    final String id = l.getPrimaryKey().toString();
    esp.setLegacyId(id);
    return id;
  }

  /**
   * Prepare sections of a document for update. Elasticsearch automatically updates the provided
   * sections. Some rockets only write sub-documents, such as screenings or allegations, from a new
   * data source, like Intake PostgreSQL, but should NOT overwrite document details from legacy.
   * 
   * <p>
   * Default handler just serializes the whole ElasticSearchPerson instance to JSON and returns the
   * same JSON for both insert and update. Child classes should override this method and null out
   * any fields that should not be updated.
   * </p>
   * 
   * @param <T> normalized persistent type
   * @param docPrep document handler
   * @param alias ES index alias
   * @param docType ES document type
   * @param esp ES document, already prepared by
   *        {@link ElasticTransformer#buildElasticSearchPersonDoc(ApiPersonAware)}
   * @param t target ApiPersonAware instance
   * @return left = insert JSON, right = update JSON throws JsonProcessingException on JSON parse
   *         error
   * @throws NeutronCheckedException on Elasticsearch disconnect or JSON parse error
   */
  public static <T extends PersistentObject> UpdateRequest prepareUpsertRequest(
      AtomPersonDocPrep<T> docPrep, String alias, String docType, final ElasticSearchPerson esp,
      T t) throws NeutronCheckedException {
    String id = esp.getId();

    // Set id and legacy id.
    if (t instanceof ApiLegacyAware) {
      final ApiLegacyAware l = (ApiLegacyAware) t;
      final String tempId = StringUtils.isNotBlank(l.getLegacyId()) ? l.getLegacyId().trim() : null;
      final boolean hasLegacyId = tempId != null && tempId.length() == CMS_ID_LEN;

      if (hasLegacyId) {
        id = l.getLegacyId();
        esp.setLegacyId(id);
      } else {
        id = esp.getId();
      }
    } else if (t instanceof CmsReplicatedEntity) {
      esp.setLegacyId(t.getPrimaryKey().toString());
    }

    esp.setSource(""); // Clear out obsolete "source" element.
    esp.setSourceJson(""); // Ditto.

    // Child classes may override these methods as needed. left = update, right = insert.
    Pair<String, String> json;
    try {
      json = ElasticTransformer.prepareUpsertJson(docPrep, esp, t, docPrep.getOptionalElementName(),
          docPrep.getOptionalCollection(esp, t), docPrep.keepCollections());
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "ERROR PREPARING UPSERT: {}", e.getMessage());
    }

    // "Upsert": update if doc exists, insert if it does not.
    return new UpdateRequest(alias, docType, id).doc(json.getLeft(), XContentType.JSON)
        .upsert(new IndexRequest(alias, docType, id).source(json.getRight(), XContentType.JSON));
  }

  /**
   * Prepare "upsert" JSON (update and insert). Child classes do not normally override this method.
   * 
   * @param <T> normalized persistent type
   * @param docPrep optional handling to set collections before serializing JSON
   * @param esp ES document, already prepared by
   *        {@link ElasticTransformer#buildElasticSearchPersonDoc(ApiPersonAware)}
   * @param t target ApiPersonAware instance
   * @param elementName target ES element for update
   * @param list list of ES child objects
   * @param keep ES sections to keep
   * @return Pair of JSON, left = update, right = insert
   * @throws JsonProcessingException on JSON processing error
   */
  public static <T extends PersistentObject> Pair<String, String> prepareUpsertJson(
      AtomPersonDocPrep<T> docPrep, ElasticSearchPerson esp, T t, String elementName,
      List<? extends ApiTypedIdentifier<String>> list, ESOptionalCollection... keep)
      throws JsonProcessingException {

    // Child classes: Set optional collections before serializing the insert JSON.
    prepareInsertCollections(docPrep, esp, t, list, keep);
    final String insertJson = mapper.writeValueAsString(esp);

    String updateJson;
    if (StringUtils.isNotBlank(elementName)) {
      final StringBuilder buf = new StringBuilder();
      buf.append("{\"").append(elementName).append("\":[");

      if (list != null && !list.isEmpty()) {
        buf.append(list.stream().map(ElasticTransformer::jsonify).sorted(String::compareTo)
            .collect(Collectors.joining(",")));
      }

      buf.append("]}");
      updateJson = buf.toString();
    } else {
      updateJson = insertJson;
    }

    return Pair.of(updateJson, insertJson);
  }

  /**
   * Set optional ES person collections to null so that they are not overwritten by accident. Child
   * classes do not normally override this method.
   * 
   * @param <T> normalized persistent type
   * @param docPrep optional handling to set collections before serializing JSON
   * @param esp ES document, already prepared by
   *        {@link #buildElasticSearchPersonDoc(ApiPersonAware)}
   * @param t target ApiPersonAware instance
   * @param list list of ES child objects
   * @param keep ES sections to keep
   */
  public static <T extends PersistentObject> void prepareInsertCollections(
      AtomPersonDocPrep<T> docPrep, ElasticSearchPerson esp, T t,
      List<? extends ApiTypedIdentifier<String>> list, ESOptionalCollection... keep) {

    // Clear out optional collections for updates.
    esp.clearOptionalCollections(keep);

    // Child classes: Set optional collections before serializing the insert JSON.
    docPrep.setInsertCollections(esp, t, list);
  }

  /**
   * Handle both {@link ApiMultiplePersonAware} and {@link ApiPersonAware} implementations of type
   * T.
   * 
   * @param p instance of type T
   * @return array of person documents
   * @throws JsonProcessingException on parse error
   */
  public static ElasticSearchPerson[] buildElasticSearchPersons(final PersistentObject p)
      throws JsonProcessingException {
    ElasticSearchPerson[] ret;

    if (p instanceof ApiMultiplePersonAware) {
      final ApiPersonAware[] persons = ((ApiMultiplePersonAware) p).getPersons();
      ret = new ElasticSearchPerson[persons.length];
      int i = 0;
      for (ApiPersonAware px : persons) {
        ret[i++] = ElasticTransformer.buildElasticSearchPersonDoc(px);
      }
    } else {
      ret = new ElasticSearchPerson[] {buildElasticSearchPerson((ApiPersonAware) p)};
    }

    return ret;
  }

  /**
   * Produce an ElasticSearchPerson suitable as an Elasticsearch person document.
   * 
   * @param p ApiPersonAware persistence object
   * @return populated ElasticSearchPerson
   * @throws JsonProcessingException if unable to serialize JSON
   */
  public static ElasticSearchPerson buildElasticSearchPerson(ApiPersonAware p)
      throws JsonProcessingException {
    return ElasticTransformer.buildElasticSearchPersonDoc(p);
  }

  /**
   * Create legacy descriptor.
   * 
   * <p>
   * Legacy ID should always be 10 characters long, otherwise we can not parse it to get UI ID. For
   * example, LegacyTable.STFPERST is usually 3 characters long.
   * </p>
   * 
   * @param legacyId Legacy ID
   * @param legacyLastUpdated Legacy last updated time stamp
   * @param legacyTable Legacy table
   * @return Legacy descriptor
   */
  public static ElasticSearchLegacyDescriptor createLegacyDescriptor(String legacyId,
      Date legacyLastUpdated, LegacyTable legacyTable) {
    final ElasticSearchLegacyDescriptor ret = new ElasticSearchLegacyDescriptor();

    if (!StringUtils.isBlank(legacyId)) {
      final String cleanLegacyId = legacyId.trim(); // data errors in CHAR(10) keys.
      ret.setLegacyId(cleanLegacyId);
      ret.setLegacyLastUpdated(DomainChef.cookStrictTimestamp(legacyLastUpdated));

      if (cleanLegacyId.length() == CMS_ID_LEN) {
        ret.setLegacyUiId(CmsKeyIdGenerator.getUIIdentifierFromKey(cleanLegacyId));
      } else {
        ret.setLegacyUiId(cleanLegacyId);
      }

      if (legacyTable != null) {
        ret.setLegacyTableName(legacyTable.getName());
        ret.setLegacyTableDescription(legacyTable.getDescription());
      }
    }

    return ret;
  }

  protected static List<ElasticSearchPersonLanguage> buildLanguage(ApiPersonAware p) {
    List<ElasticSearchPersonLanguage> ret = null;

    if (p instanceof ApiMultipleLanguagesAware) {
      final ApiMultipleLanguagesAware mlx = (ApiMultipleLanguagesAware) p;
      ret = new ArrayList<>(mlx.getLanguages().length);
      for (ApiLanguageAware lx : mlx.getLanguages()) {
        final Integer languageId = lx.getLanguageSysId();
        ret.add(new ElasticSearchPersonLanguage(languageId.toString(),
            SystemCodeCache.global().getSystemCodeShortDescription(languageId), lx.getPrimary()));
      }
    } else if (p instanceof ApiLanguageAware) {
      ret = new ArrayList<>();
      final ApiLanguageAware lx = (ApiLanguageAware) p;
      final Integer languageId = lx.getLanguageSysId();
      ret.add(new ElasticSearchPersonLanguage(languageId.toString(),
          SystemCodeCache.global().getSystemCodeShortDescription(languageId), lx.getPrimary()));
    }

    return ret;
  }

  protected static List<ElasticSearchPersonPhone> buildPhone(ApiPersonAware p) {
    List<ElasticSearchPersonPhone> ret = null;
    if (p instanceof ApiMultiplePhonesAware) {
      final Map<String, ElasticSearchPersonPhone> uniquePhones = new HashMap<>();
      final ApiMultiplePhonesAware mphx = (ApiMultiplePhonesAware) p;
      for (ApiPhoneAware phx : mphx.getPhones()) {
        uniquePhones.putIfAbsent(phx.getPhoneNumber(), new ElasticSearchPersonPhone(phx));
      }
      ret = new ArrayList<>(uniquePhones.values());
    } else if (p instanceof ApiPhoneAware) {
      ret = new ArrayList<>();
      ret.add(new ElasticSearchPersonPhone((ApiPhoneAware) p));
    }

    return ret;
  }

  protected static List<ElasticSearchPersonAddress> buildAddress(ApiPersonAware p) {
    List<ElasticSearchPersonAddress> ret = null;

    if (p instanceof ApiMultipleClientAddressAware) {
      ret = ((ApiMultipleClientAddressAware) p).getElasticSearchPersonAddresses();
    } else if (p instanceof ApiAddressAware) {
      ret = new ArrayList<>();
      final ElasticSearchPersonAddress esAddress =
          new ElasticSearchPersonAddress((ApiAddressAware) p);
      if (p instanceof ApiLegacyAware) {
        esAddress.setLegacyDescriptor(((ApiLegacyAware) p).getLegacyDescriptor());
      }
      ret.add(esAddress);
    }

    return ret;
  }

  protected static List<ElasticSearchPersonScreening> buildScreening(ApiPersonAware p) {
    List<ElasticSearchPersonScreening> ret = null;
    if (p instanceof ApiScreeningAware) {
      final ApiScreeningAware screener = ((ApiScreeningAware) p);
      ret = new ArrayList<>(screener.getEsScreenings().length);
      for (ElasticSearchPersonScreening scr : screener.getEsScreenings()) {
        ret.add(scr);
      }
    }
    return ret;
  }

  protected static ElasticSearchLegacyDescriptor buildLegacyDescriptor(ApiPersonAware p) {
    ElasticSearchLegacyDescriptor ret = null;
    if (p instanceof ApiLegacyAware) {
      ret = ((ApiLegacyAware) p).getLegacyDescriptor();
    }
    return ret;
  }

  protected static ElasticSearchRaceAndEthnicity buildRaceEthnicity(ApiPersonAware p) {
    ElasticSearchRaceAndEthnicity ret = null;
    if (p instanceof ApiClientRaceAndEthnicityAware) {
      final ApiClientRaceAndEthnicityAware raceAware = (ApiClientRaceAndEthnicityAware) p;
      ret = raceAware.getRaceAndEthnicity();
    }
    return ret;
  }

  protected static List<ElasticSearchSystemCode> buildClientCounties(ApiPersonAware p) {
    List<ElasticSearchSystemCode> ret = new ArrayList<>();
    if (p instanceof ApiClientCountyAware) {
      final ApiClientCountyAware countyAware = (ApiClientCountyAware) p;
      ret = countyAware.getClientCounties();
    }
    return ret;
  }

  protected static List<ElasticSearchSafetyAlert> buildSafetyAlerts(ApiPersonAware p) {
    List<ElasticSearchSafetyAlert> ret = null;
    if (p instanceof ApiClientSafetyAlertsAware) {
      final ApiClientSafetyAlertsAware alertsAware = (ApiClientSafetyAlertsAware) p;
      final List<ElasticSearchSafetyAlert> safetyAlerts = alertsAware.getClientSafetyAlerts();
      if (safetyAlerts != null && !safetyAlerts.isEmpty()) {
        ret = safetyAlerts;
      }
    }
    return ret;
  }

  protected static List<ElasticSearchPersonAka> buildAkas(ApiPersonAware p) {
    List<ElasticSearchPersonAka> ret = null;
    if (p instanceof ApiOtherClientNamesAware) {
      final ApiOtherClientNamesAware akasAware = (ApiOtherClientNamesAware) p;
      final List<ElasticSearchPersonAka> clientAkas = akasAware.getOtherClientNames();
      if (clientAkas != null && !clientAkas.isEmpty()) {
        ret = clientAkas;
      }
    }
    return ret;
  }

  protected static List<ElasticSearchPersonCsec> buildCsecs(ApiPersonAware p) {
    List<ElasticSearchPersonCsec> ret = null;
    if (p instanceof ReplicatedClient) {
      ReplicatedClient replicatedClient = (ReplicatedClient) p;
      final List<ElasticSearchPersonCsec> clientCsecs = replicatedClient.getCsecs();
      if (clientCsecs != null && !clientCsecs.isEmpty()) {
        ret = clientCsecs;
      }
    }
    return ret;
  }

  protected static String buildOpenCaseId(ApiPersonAware p) {
    String ret = null;
    if (p instanceof ApiClientCaseAware) {
      final ApiClientCaseAware caseAware = (ApiClientCaseAware) p;
      ret = caseAware.getOpenCaseId();
    }
    return ret;
  }

  protected static String buildOpenCaseResponsibleAgencyCode(ApiPersonAware p) {
    String ret = null;
    if (p instanceof ApiClientCaseAware) {
      final ApiClientCaseAware caseAware = (ApiClientCaseAware) p;
      ret = caseAware.getOpenCaseResponsibleAgencyCode();
    }
    return ret;
  }

  /**
   * Produce an ElasticSearchPerson objects suitable for an Elasticsearch person document.
   * 
   * @param p ApiPersonAware persistence object
   * @return populated ElasticSearchPerson
   * @throws JsonProcessingException if unable to serialize JSON
   */
  public static ElasticSearchPerson buildElasticSearchPersonDoc(ApiPersonAware p)
      throws JsonProcessingException {
    return buildElasticSearchPersonDoc(mapper, p);
  }

  /**
   * Produce an ElasticSearchPerson objects suitable for an Elasticsearch person document.
   * 
   * @param mapper Jackson ObjectMapper
   * @param p ApiPersonAware persistence object
   * @return populated ElasticSearchPerson
   * @throws JsonProcessingException if unable to serialize JSON
   */
  public static ElasticSearchPerson buildElasticSearchPersonDoc(final ObjectMapper mapper,
      ApiPersonAware p) throws JsonProcessingException {
    ElasticSearchPerson ret;
    Serializable primaryKey = p.getPrimaryKey();

    if (primaryKey == null) {
      LOGGER.debug("NULL PRIMARY KEY: {}", p);
      primaryKey = "MISSING_ID";
    }

    // Write persistence object to Elasticsearch Person document.
    ret = new ElasticSearchPerson(primaryKey.toString(), // id
        p.getFirstName(), // first name
        p.getLastName(), // last name
        p.getMiddleName(), // middle name
        p.getNameSuffix(), // name suffix
        p.getGender(), // gender
        DomainChef.cookDate(p.getBirthDate()), // birth date
        p.getSsn(), // SSN
        p.getClass().getName(), // type
        mapper.writeValueAsString(p), // source
        null, // omit highlights
        buildAddress(p), buildPhone(p), buildLanguage(p), buildScreening(p));

    // Legacy descriptor
    ret.setLegacyDescriptor(buildLegacyDescriptor(p));

    // Sealed and sensitive.
    ret.setSensitivityIndicator(p.getSensitivityIndicator());

    // Set client counties
    final List<ElasticSearchSystemCode> clientCounties = buildClientCounties(p);
    ret.setClientCounties(clientCounties);
    // this is only added for backward compatibility
    if (!clientCounties.isEmpty()) {
      ret.setClientCounty(clientCounties.get(0));
    }

    // Set race/ethnicity
    ret.setCleintRace(buildRaceEthnicity(p));

    // Index number
    ret.setIndexNumber(
        StringUtils.isBlank(p.getClientIndexNumber()) ? null : p.getClientIndexNumber());

    // AKAs
    ret.setAkas(buildAkas(p));

    // CSECs
    ret.setCsecs(buildCsecs(p));

    // Safety alerts
    ret.setSafetyAlerts(buildSafetyAlerts(p));

    // Open case id
    ret.setOpenCaseId(buildOpenCaseId(p));

    // Open case responsible agency code
    ret.setOpenCaseResponsibleAgencyCode(buildOpenCaseResponsibleAgencyCode(p));

    // Death date
    ret.setDateOfDeath(DomainChef.cookDate(p.getDeathDate()));

    // Update time stamp
    ret.setIndexUpdateTime(DomainChef.cookStrictTimestamp(new Date()));

    return ret;
  }

  public static ObjectMapper getMapper() { // NOSONAR
    return mapper;
  }

  public static synchronized void setMapper(final ObjectMapper mapper) {
    if (ElasticTransformer.mapper == null) {
      ElasticTransformer.mapper = mapper;
    }
  }

}
