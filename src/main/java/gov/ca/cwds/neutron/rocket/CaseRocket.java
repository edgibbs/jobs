package gov.ca.cwds.neutron.rocket;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.dao.cms.ReplicatedPersonCasesDao;
import gov.ca.cwds.dao.cms.StaffPersonDao;
import gov.ca.cwds.data.es.ElasticSearchAccessLimitation;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPerson.ElasticSearchPersonSocialWorker;
import gov.ca.cwds.data.es.ElasticSearchPersonCase;
import gov.ca.cwds.data.es.ElasticSearchPersonChild;
import gov.ca.cwds.data.es.ElasticSearchPersonParent;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.EsCaseRelatedPerson;
import gov.ca.cwds.data.persistence.cms.EsPersonCase;
import gov.ca.cwds.data.persistence.cms.ReplicatedPersonCases;
import gov.ca.cwds.data.persistence.cms.StaffPerson;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.EmbeddableStaffWorker;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomRowMapper;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.rocket.cases.CaseSQLResource;
import gov.ca.cwds.neutron.rocket.cases.FocusChildParent;
import gov.ca.cwds.neutron.rocket.referral.ReferralJobRanges;
import gov.ca.cwds.neutron.util.NeutronThreadUtils;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.neutron.util.transform.EntityNormalizer;
import gov.ca.cwds.rest.api.domain.DomainChef;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;

/**
 * Rocket to index person cases from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class CaseRocket extends InitialLoadJdbcRocket<ReplicatedPersonCases, EsCaseRelatedPerson>
    implements AtomRowMapper<EsCaseRelatedPerson> {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(CaseRocket.class);

  private static final int HASH_SIZE_LARGE = 99881;

  private final AtomicInteger rowsReadCases = new AtomicInteger(0);

  private final AtomicInteger nextThreadNum = new AtomicInteger(0);

  private transient StaffPersonDao staffPersonDao;

  private transient ReplicatedClientDao clientDao;

  private Map<String, StaffPerson> staffWorkers = new HashMap<>();

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao DAO for {@link ReplicatedPersonCases}
   * @param esDao ElasticSearch DAO
   * @param clientDao client DAO
   * @param staffPersonDao staff worker DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   * @param launchDirector launch director
   */
  @Inject
  public CaseRocket(ReplicatedPersonCasesDao dao,
      @Named("elasticsearch.dao.people") ElasticsearchDao esDao, ReplicatedClientDao clientDao,
      StaffPersonDao staffPersonDao, @LastRunFile String lastRunFile, ObjectMapper mapper,
      FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);

    this.clientDao = clientDao;
    this.staffPersonDao = staffPersonDao;
  }

  // =====================
  // ROCKET SPECS:
  // =====================

  /**
   * This rocket normalizes <strong>without</strong> the transform thread.
   */
  @Override
  public boolean useTransformThread() {
    return false;
  }

  @Override
  public String getPrepLastChangeSQL() {
    try {
      return NeutronDB2Utils.prepLastChangeSQL(CaseSQLResource.PREP_AFFECTED_CLIENTS_LAST_CHG,
          determineLastSuccessfulRunTime(), getFlightPlan().getOverrideLastEndTime());
    } catch (NeutronCheckedException e) {
      throw CheeseRay.runtime(LOGGER, e, "CASES: ERROR BUILDING LAST CHANGE SQL: {}",
          e.getMessage());
    }
  }

  @Override
  public String getInitialLoadViewName() {
    return "VW_MQT_REFRL_ONLY";
  }

  @Override
  public boolean isInitialLoadJdbc() {
    return true;
  }

  @Override
  public List<Pair<String, String>> getPartitionRanges() throws NeutronCheckedException {
    return new ReferralJobRanges().getPartitionRanges(this); // Cases takes a long time ...
  }

  @Override
  public String getOptionalElementName() {
    return "cases";
  }

  /**
   * If sealed or sensitive data must NOT be loaded then any records indexed with sealed or
   * sensitive flag must be deleted.
   */
  @Override
  public boolean mustDeleteLimitedAccessRecords() {
    return !getFlightPlan().isLoadSealedAndSensitive();
  }

  @Override
  public String getJdbcOrderBy() {
    return ""; // sort manually, since DB2 might not optimize the sort.
  }

  /**
   * Roll your own SQL.
   * <p>
   * This approach requires sorted results. Either sort on the database side or here in the
   * application.
   * </p>
   */
  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    final StringBuilder buf = new StringBuilder();
    buf.append(CaseSQLResource.SELECT_CASES_FULL_EVERYTHING);

    if (!getFlightPlan().isLoadSealedAndSensitive()) {
      buf.append(" WHERE CAS.LMT_ACSSCD = 'N' ");
    }

    final String ret = buf.toString().trim();
    LOGGER.info("CASE SQL: {}", ret);
    return ret;
  }

  protected String buildAffectedClientsSQL() {
    return getFlightPlan().isLastRunMode() ? getPrepLastChangeSQL()
        : CaseSQLResource.PREP_AFFECTED_CLIENTS_FULL;
  }

  // =====================
  // NORMALIZATION:
  // =====================

  @Override
  protected UpdateRequest prepareUpsertRequest(ElasticSearchPerson esp, ReplicatedPersonCases p)
      throws NeutronCheckedException {
    return prepareUpdateRequest(esp, p, p.getCases(), true);
  }

  @Override
  public Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return EsPersonCase.class;
  }

  @Override
  public List<ReplicatedPersonCases> normalize(List<EsCaseRelatedPerson> recs) {
    return EntityNormalizer.<ReplicatedPersonCases, EsCaseRelatedPerson>normalizeList(recs);
  }

  // =====================
  // JDBC:
  // =====================

  protected void prepAffectedClients(final PreparedStatement stmtInsClient,
      final PreparedStatement stmtInsClientCase, final Pair<String, String> p) throws SQLException {
    LOGGER.info("Prep Affected Clients: range: {} - {}", p.getLeft(), p.getRight());
    stmtInsClient.setMaxRows(0);
    stmtInsClient.setQueryTimeout(0);

    if (!getFlightPlan().isLastRunMode()) {
      LOGGER.debug("INITIAL LOAD");
      stmtInsClient.setString(1, p.getLeft());
      stmtInsClient.setString(2, p.getRight());
    } else {
      LOGGER.debug("LAST RUN");
    }

    final int countInsClient = stmtInsClient.executeUpdate();
    LOGGER.info("affected clients: {}", countInsClient);

    final int countInsClientCases = stmtInsClientCase.executeUpdate();
    LOGGER.info("affected client/cases: {}", countInsClientCases);
  }

  protected void readCaseClients(final PreparedStatement stmt,
      final List<Pair<String, String>> list) throws SQLException {
    LOGGER.info("read Case Clients");
    stmt.setMaxRows(0);
    stmt.setQueryTimeout(0);
    stmt.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());

    try (final ResultSet rs = stmt.executeQuery()) {
      while (!isFailed() && rs.next()) {
        list.add(Pair.of(rs.getString("CLIENT_ID"), rs.getString("CASE_ID")));
      }
    } finally {
      // close result set
    }
  }

  protected void readFocusChildParents(final PreparedStatement stmt,
      final List<FocusChildParent> list) throws SQLException {
    LOGGER.info("read Focus Child Parents");
    stmt.setMaxRows(0);
    stmt.setQueryTimeout(0);
    stmt.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());

    int cntr = 0;
    FocusChildParent m;

    try (final ResultSet rs = stmt.executeQuery()) {
      while (!isFailed() && rs.next() && (m = FocusChildParent.extract(rs)) != null) {
        CheeseRay.logEvery(++cntr, "read", "focus child parent");
        list.add(m);
      }
    } finally {
      // close result set
    }
  }

  protected void readCases(final PreparedStatement stmtSelCase,
      final Map<String, EsCaseRelatedPerson> mapCases) throws SQLException {
    LOGGER.info("read Cases");
    stmtSelCase.setMaxRows(0);
    stmtSelCase.setQueryTimeout(0);
    stmtSelCase.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());

    int cntr = 0;
    EsCaseRelatedPerson m;
    final ResultSet rs = stmtSelCase.executeQuery(); // NOSONAR
    while (!isFailed() && rs.next()) {
      m = extractCase(rs);
      CheeseRay.logEvery(++cntr, "read", "case bundle");
      CheeseRay.logEvery(LOGGER, 10000, rowsReadCases.incrementAndGet(), "Total read", "cases");
      mapCases.put(m.getCaseId(), m);
    }
  }

  /**
   * Reads the current list of staff workers.
   * 
   * <p>
   * NEXT: Read staff worker deltas or cache with Hibernate.
   * </p>
   * 
   * @return complete list of potential case workers
   * @throws NeutronCheckedException on database error
   */
  protected Map<String, StaffPerson> readStaffWorkers() throws NeutronCheckedException {
    LOGGER.info("readStaffWorkers");
    try {
      return staffPersonDao.findAll().stream()
          .collect(Collectors.toMap(StaffPerson::getId, w -> w));
    } catch (Exception e) {
      fail();
      throw new NeutronCheckedException("ERROR READING STAFF WORKERS", e);
    }
  }

  /**
   * Pulls <strong>Client/Case/Relationship</strong>.
   * 
   * @param rs result set
   */
  @Override
  public EsCaseRelatedPerson extract(final ResultSet rs) throws SQLException {
    return null;
  }

  protected ReplicatedClient extractClient(ResultSet rs) throws SQLException {
    final ReplicatedClient ret = new ReplicatedClient();

    ret.setId(rs.getString("CLIENT_ID"));
    ret.setCommonFirstName(rs.getString("CLIENT_FIRST_NM"));
    ret.setCommonLastName(rs.getString("CLIENT_LAST_NM"));
    ret.setSensitivityIndicator(rs.getString("CLIENT_SENSITIVITY_IND"));
    ret.setLastUpdatedTime(rs.getTimestamp("CLIENT_LAST_UPDATED"));
    ret.setReplicationOperation(CmsReplicationOperation.valueOf(rs.getString("CLIENT_OPERATION")));
    ret.setReplicationDate(rs.getTimestamp("CLIENT_LOGMARKER"));

    return ret;
  }

  protected EsCaseRelatedPerson extractCase(ResultSet rs) throws SQLException {
    final EsCaseRelatedPerson ret = new EsCaseRelatedPerson();

    ret.setCaseId(rs.getString("CASE_ID"));
    ret.setFocusChildId(rs.getString("FOCUS_CHILD_ID"));
    ret.setStartDate(rs.getDate("START_DATE"));
    ret.setEndDate(rs.getDate("END_DATE"));
    ret.setCaseLastUpdated(rs.getTimestamp("CASE_LAST_UPDATED"));
    ret.setCounty(rs.getInt("COUNTY"));
    ret.setServiceComponent(rs.getInt("SERVICE_COMP"));

    //
    // Worker (staff):
    //
    final String workerId = ifNull(rs.getString("WORKER_ID"));
    if (StringUtils.isNotBlank(workerId) && staffWorkers.containsKey(workerId)) {
      final StaffPerson staffPerson = staffWorkers.get(workerId);
      final EmbeddableStaffWorker worker = ret.getWorker();
      worker.setWorkerId(workerId);
      worker.setWorkerFirstName(staffPerson.getFirstName());
      worker.setWorkerLastName(staffPerson.getLastName());
      worker.setWorkerLastUpdated(staffPerson.getLastUpdatedTime());
    }

    //
    // Access Limitation:
    //
    ret.setLimitedAccessCode(ifNull(rs.getString("LIMITED_ACCESS_CODE")));
    ret.setLimitedAccessDate(rs.getDate("LIMITED_ACCESS_DATE"));
    ret.setLimitedAccessDescription(ifNull(rs.getString("LIMITED_ACCESS_DESCRIPTION")));
    ret.setLimitedAccessGovernmentEntityId(rs.getInt("LIMITED_ACCESS_GOVERNMENT_ENT"));

    return ret;
  }

  protected Map<String, ReplicatedClient> readClients(final PreparedStatement stmtSelClient,
      final Map<String, ReplicatedClient> mapClients) throws NeutronCheckedException {
    try {
      stmtSelClient.setMaxRows(0);
      stmtSelClient.setQueryTimeout(0);
      stmtSelClient.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());

      LOGGER.info("read client/case keys");
      final ResultSet rs = stmtSelClient.executeQuery(); // NOSONAR

      ReplicatedClient rc;
      while (!isFailed() && rs.next()) {
        rc = extractClient(rs);
        mapClients.put(rc.getId(), rc);
      }

    } catch (Exception e) {
      fail();
      throw new NeutronCheckedException("ERROR READING CLIENTS", e);
    }

    return mapClients;
  }

  // =====================
  // ASSEMBLY:
  // =====================

  protected void collectCaseClients(final Map<String, Set<String>> mapCaseClients,
      final Pair<String, String> p) {
    // case => clients
    final String caseId = p.getRight();
    Set<String> clientCases = mapCaseClients.get(caseId);

    if (clientCases == null) {
      clientCases = new HashSet<>();
      mapCaseClients.put(caseId, clientCases);
    }

    clientCases.add(p.getLeft());
  }

  protected void collectThisClientCase(final Map<String, Set<String>> mapClientCases, String caseId,
      String clientId) {
    Set<String> clientCases = mapClientCases.get(clientId);
    if (clientCases == null) {
      clientCases = new HashSet<>();
      mapClientCases.put(clientId, clientCases);
    }

    clientCases.add(caseId);
  }

  protected void collectClientCases(final Map<String, Set<String>> mapClientCases,
      final Pair<String, String> p) {
    // client => cases
    collectThisClientCase(mapClientCases, p.getRight(), p.getLeft());
  }

  protected void collectFocusChildParents(
      final Map<String, Map<String, FocusChildParent>> mapFocusChildParents,
      final FocusChildParent rel) {
    // focus child => parents
    final String focusChildId = rel.getFocusClientId();
    Map<String, FocusChildParent> clientParents = mapFocusChildParents.get(focusChildId);
    if (clientParents == null) {
      clientParents = new HashMap<>();
      mapFocusChildParents.put(focusChildId, clientParents);
    }
    clientParents.put(rel.getParentClientId(), rel);
  }

  protected void addFocusChildren(final Map<String, EsCaseRelatedPerson> mapCases,
      final Map<String, ReplicatedClient> mapClients) {
    // Focus child:
    for (EsCaseRelatedPerson theCase : mapCases.values()) {
      final ReplicatedClient focusChild = mapClients.get(theCase.getFocusChildId());
      if (focusChild != null) {
        theCase.setFocusChildFirstName(focusChild.getFirstName());
        theCase.setFocusChildLastName(focusChild.getLastName());
        theCase.setFocusChildSensitivityIndicator(focusChild.getSensitivityIndicator());
        theCase.setFocusChildLastUpdated(focusChild.getLastUpdatedTime());
      }
    }
  }

  // =====================
  // REDUCE:
  // =====================

  protected void reduceCase(final ReplicatedPersonCases cases, EsCaseRelatedPerson rawCase,
      final Map<String, Map<String, FocusChildParent>> mapFocusChildParents) {
    if (rawCase != null) {
      final ElasticSearchPersonCase esPersonCase = new ElasticSearchPersonCase();
      final String caseId = rawCase.getCaseId();
      final String focusChildId = rawCase.getFocusChildId();

      //
      // Case:
      //
      esPersonCase.setId(caseId);
      esPersonCase.setLegacyId(caseId);
      esPersonCase
          .setLegacyLastUpdated(DomainChef.cookStrictTimestamp(rawCase.getCaseLastUpdated()));
      esPersonCase.setStartDate(DomainChef.cookDate(rawCase.getStartDate()));
      esPersonCase.setEndDate(DomainChef.cookDate(rawCase.getEndDate()));

      final Integer county = rawCase.getCounty();
      esPersonCase.setCountyId(county == null ? null : county.toString());
      esPersonCase.setCountyName(SystemCodeCache.global().getSystemCodeShortDescription(county));
      esPersonCase.setServiceComponentId(
          rawCase.getServiceComponent() == null ? null : rawCase.getServiceComponent().toString());
      esPersonCase.setServiceComponent(
          SystemCodeCache.global().getSystemCodeShortDescription(rawCase.getServiceComponent()));
      esPersonCase.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(
          rawCase.getCaseId(), rawCase.getCaseLastUpdated(), LegacyTable.CASE));

      //
      // Child:
      //
      final ElasticSearchPersonChild child = new ElasticSearchPersonChild();
      child.setId(focusChildId);
      child.setLegacyClientId(focusChildId);
      child
          .setLegacyLastUpdated(DomainChef.cookStrictTimestamp(rawCase.getFocusChildLastUpdated()));
      child.setFirstName(rawCase.getFocusChildFirstName());
      child.setLastName(rawCase.getFocusChildLastName());
      child.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(focusChildId,
          rawCase.getFocusChildLastUpdated(), LegacyTable.CLIENT));
      child.setSensitivityIndicator(rawCase.getFocusChildSensitivityIndicator());
      esPersonCase.setFocusChild(child);

      //
      // Assigned Worker:
      //
      final ElasticSearchPersonSocialWorker assignedWorker = new ElasticSearchPersonSocialWorker();
      assignedWorker.setId(rawCase.getWorker().getWorkerId());
      assignedWorker.setLegacyClientId(rawCase.getWorker().getWorkerId());
      assignedWorker.setLegacyLastUpdated(
          DomainChef.cookStrictTimestamp(rawCase.getWorker().getWorkerLastUpdated()));
      assignedWorker.setFirstName(rawCase.getWorker().getWorkerFirstName());
      assignedWorker.setLastName(rawCase.getWorker().getWorkerLastName());
      assignedWorker.setLegacyDescriptor(
          ElasticTransformer.createLegacyDescriptor(rawCase.getWorker().getWorkerId(),
              rawCase.getWorker().getWorkerLastUpdated(), LegacyTable.STAFF_PERSON));
      esPersonCase.setAssignedSocialWorker(assignedWorker);

      //
      // Access Limitation:
      //
      final ElasticSearchAccessLimitation accessLimit = new ElasticSearchAccessLimitation();
      accessLimit.setLimitedAccessCode(rawCase.getAccessLimitation().getLimitedAccessCode());
      accessLimit.setLimitedAccessDate(
          DomainChef.cookDate(rawCase.getAccessLimitation().getLimitedAccessDate()));
      accessLimit
          .setLimitedAccessDescription(rawCase.getAccessLimitation().getLimitedAccessDescription());
      accessLimit.setLimitedAccessGovernmentEntityId(
          rawCase.getAccessLimitation().getLimitedAccessGovernmentEntityId() == null ? null
              : rawCase.getAccessLimitation().getLimitedAccessGovernmentEntityId().toString());
      accessLimit.setLimitedAccessGovernmentEntityName(
          SystemCodeCache.global().getSystemCodeShortDescription(
              rawCase.getAccessLimitation().getLimitedAccessGovernmentEntityId()));
      esPersonCase.setAccessLimitation(accessLimit);

      //
      // A focus child may have more than one parent:
      //
      final Map<String, FocusChildParent> parents = mapFocusChildParents.get(focusChildId);
      if (parents != null && !parents.isEmpty()) {
        parents.values().stream().forEach(fcp -> {
          final ElasticSearchPersonParent parent = new ElasticSearchPersonParent();

          parent.setId(fcp.getParentClientId());
          parent.setLegacyClientId(fcp.getParentClientId());
          parent.setFirstName(fcp.getParentFirstName());
          parent.setLastName(fcp.getParentLastName());
          parent.setSensitivityIndicator(fcp.getParentSensitivity());

          parent.setLegacyLastUpdated(
              DomainChef.cookStrictTimestamp(rawCase.getFocusChildLastUpdated()));
          parent.setLegacySourceTable(LegacyTable.CLIENT.getName());
          parent.setRelationship(fcp.translateRelationshipToString());
          parent.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(
              fcp.getParentClientId(), rawCase.getFocusChildLastUpdated(), LegacyTable.CLIENT));
          cases.addCase(esPersonCase, parent);
        });
      } else {
        cases.addCase(esPersonCase, null);
      }
    }
  }

  protected ReplicatedPersonCases reduceClientCases(final String clientId,
      final Map<String, EsCaseRelatedPerson> mapCases,
      final Map<String, Set<String>> mapClientCases,
      final Map<String, Map<String, FocusChildParent>> mapFocusChildParents) {
    final ReplicatedPersonCases ret = new ReplicatedPersonCases(clientId);
    mapClientCases.get(clientId).stream()
        .forEach(k -> reduceCase(ret, mapCases.get(k), mapFocusChildParents));
    return ret;
  }

  protected int assemblePieces(final List<FocusChildParent> listFocusChildParents,
      List<Pair<String, String>> listCaseClients, final Map<String, EsCaseRelatedPerson> mapCases,
      final Map<String, ReplicatedClient> mapClients, final Map<String, Set<String>> mapClientCases)
      throws NeutronCheckedException {
    LOGGER.info("assemble pieces");
    int countNormalized = 0;

    try {
      final List<FocusChildParent> focusChildParents = listFocusChildParents.stream()
          .sorted((e1, e2) -> e1.getFocusClientId().compareTo(e2.getFocusClientId()))
          .collect(Collectors.toList());

      final Map<String, Set<String>> mapCaseClients = new HashMap<>(HASH_SIZE_LARGE);
      final Map<String, Map<String, FocusChildParent>> mapFocusChildParents =
          new HashMap<>(HASH_SIZE_LARGE);

      // Collect maps:
      for (Pair<String, String> p : listCaseClients) {
        collectCaseClients(mapCaseClients, p);
        collectClientCases(mapClientCases, p);
      }

      for (FocusChildParent ccr : focusChildParents) {
        collectFocusChildParents(mapFocusChildParents, ccr);
      }

      // Add focus child details to cases.
      addFocusChildren(mapCases, mapClients);

      // Boil down to JSON objects for Elasticsearch.
      final Map<String, ReplicatedPersonCases> mapReadyClientCases = mapClientCases.entrySet()
          .stream()
          .map(x -> reduceClientCases(x.getKey(), mapCases, mapClientCases, mapFocusChildParents))
          .collect(Collectors.toMap(ReplicatedPersonCases::getGroupId, r -> r));

      // Index into Elasticsearch!
      mapReadyClientCases.values().stream().forEach(this::addToIndexQueue);

      // Verify test data. Ignored in Perf/Prod.
      verify(mapReadyClientCases);
    } finally {
      // C'mon Jacoco ... cover the code ...
    }

    return countNormalized;
  }

  protected boolean verify(final Map<String, ReplicatedPersonCases> mapReadyClientCases)
      throws NeutronCheckedException {
    if (!isLargeDataSet()) {
      LOGGER.info("Validate test data ...");
      final List<Pair<String, String>> tests = new ArrayList<>();
      tests.add(Pair.of("Amber", "TMZGOO205B"));
      tests.add(Pair.of("Nina", "TBCF40g0D8"));
      tests.add(Pair.of("Lucy", "ASUREPK0Bu"));
      tests.add(Pair.of("Consuelo", "H2NRB2Y0AB"));

      try {
        catchYourBreath(); // Let bulk processor finish

        if (LOGGER.isInfoEnabled()) {
          for (Pair<String, String> p : tests) {
            LOGGER.info("TEST: name: {}\n{}", p.getLeft(), ElasticSearchPerson.MAPPER
                .writeValueAsString(mapReadyClientCases.get(p.getRight())));
          }
        }
      } catch (IOException e) {
        fail();
        throw CheeseRay.checked(LOGGER, e, "VALIDATION ERROR! {}", e.getMessage());
      }
    }

    return true;
  }

  // =====================
  // THREADS:
  // =====================

  /**
   * Read all records from a single partition (key range), sort results, and normalize.
   * 
   * <p>
   * Each call to this method should run in its own thread.
   * </p>
   * 
   * @param keyRange partition (key) range to read
   * @return number of client documents affected
   * @throws NeutronCheckedException on general error
   */
  protected int pullNextRange(final Pair<String, String> keyRange) throws NeutronCheckedException {
    final String threadName = "case_" + nextThreadNum.incrementAndGet() + "_" + keyRange.getLeft()
        + "_" + keyRange.getRight();
    nameThread(threadName);
    LOGGER.info("BEGIN: range: {} - {}", keyRange.getLeft(), keyRange.getRight());
    getFlightLog().markRangeStart(keyRange);

    final List<Pair<String, String>> listCaseClients = new ArrayList<>(HASH_SIZE_LARGE);
    final List<FocusChildParent> listFocusChildParents = new ArrayList<>(HASH_SIZE_LARGE);
    final Map<String, ReplicatedClient> mapClients = new HashMap<>(HASH_SIZE_LARGE); // Prime
    final Map<String, EsCaseRelatedPerson> mapCasesById = new HashMap<>(HASH_SIZE_LARGE); // Prime

    // Retrieve records.
    try (final Connection con = getConnection()) {
      final String schema = getDBSchemaName();
      NeutronJdbcUtils.enableBatchSettings(con);

      final String sqlAffectedClients = buildAffectedClientsSQL();
      LOGGER.info("Case History affected clients SQL:\n {}", sqlAffectedClients);

      try (final PreparedStatement stmtInsClient = con.prepareStatement(sqlAffectedClients);
          final PreparedStatement stmtInsClientCase =
              con.prepareStatement(CaseSQLResource.INSERT_CLIENT_CASE);
          final PreparedStatement stmtSelClient =
              con.prepareStatement(CaseSQLResource.SELECT_CLIENT);
          final PreparedStatement stmtSelCase = con.prepareStatement(CaseSQLResource.SELECT_CASE);
          final PreparedStatement stmtSelCaseClients =
              con.prepareStatement(CaseSQLResource.SELECT_CLIENT_CASE);
          final PreparedStatement stmtSelFocusChildParents =
              con.prepareStatement(CaseSQLResource.SELECT_FOCUS_CHILD_PARENTS)) {
        prepAffectedClients(stmtInsClient, stmtInsClientCase, keyRange);
        readClients(stmtSelClient, mapClients);
        readCases(stmtSelCase, mapCasesById);
        readCaseClients(stmtSelCaseClients, listCaseClients);
        readFocusChildParents(stmtSelFocusChildParents, listFocusChildParents);
      } finally {
        con.commit(); // release database resources
      }

    } catch (Exception e) {
      fail();
      throw CheeseRay.checked(LOGGER, e, "ERROR PULLING RANGE! {} - {}: {}", keyRange.getLeft(),
          keyRange.getRight(), e.getMessage());
    }

    // Process records.
    int recordsProcessed = 0;
    try {
      recordsProcessed = assemblePieces(listFocusChildParents, listCaseClients, mapCasesById,
          mapClients, new HashMap<>(HASH_SIZE_LARGE));
    } catch (Exception e) {
      fail();
      throw CheeseRay.checked(LOGGER, e, "ERROR ASSEMBLING RANGE! {} - {}: {}", keyRange.getLeft(),
          keyRange.getRight(), e.getMessage());
    } finally {
      getFlightLog().markRangeComplete(keyRange);
    }

    LOGGER.info("DONE: range: {} - {}", keyRange.getLeft(), keyRange.getRight());
    return recordsProcessed;
  }

  protected void runMultiThreadIndexing() {
    nameThread("case_master");
    LOGGER.info("BEGIN: main read thread");
    doneTransform(); // normalize in place **WITHOUT** the transform thread

    try {
      staffWorkers = readStaffWorkers();
      final List<Pair<String, String>> ranges = getPartitionRanges();
      LOGGER.info(">>>>>>>> # OF RANGES: {} <<<<<<<<", ranges);
      final List<ForkJoinTask<?>> tasks = new ArrayList<>(ranges.size());
      final ForkJoinPool threadPool =
          new ForkJoinPool(NeutronThreadUtils.calcReaderThreads(getFlightPlan()));

      // Queue execution.
      for (Pair<String, String> p : ranges) {
        // Pull each range independently on the next available thread.
        tasks.add(threadPool.submit(() -> pullNextRange(p)));
      }

      // Join threads. Don't return from method until they complete.
      for (ForkJoinTask<?> task : tasks) {
        task.get();
      }
    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(LOGGER, e, "ERROR IN CASES MAIN THREAD! {}", e.getMessage());
    } finally {
      doneRetrieve();
    }

    LOGGER.info("DONE: read {} ES case rows", this.rowsReadCases.get());
  }

  @Override
  public List<ReplicatedPersonCases> fetchLastRunResults(Date lastRunDate,
      Set<String> deletionResults) {
    doneTransform(); // normalize in place **WITHOUT** the transform thread
    try {
      staffWorkers = readStaffWorkers();
      pullNextRange(Pair.of("a", "b"));
    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(LOGGER, e, "ERROR FETCHING LAST RUN RESULTS! {}", e.getMessage());
    } finally {
      doneRetrieve();
    }
    return new ArrayList<>(); // Work already done
  }

  /**
   * Initial load only. The "extract" part of ETL. Processes key ranges in separate threads.
   * 
   * <p>
   * Note that this rocket normalizes <strong>without</strong> the transform thread.
   * </p>
   */
  @Override
  protected void threadRetrieveByJdbc() {
    runMultiThreadIndexing();
  }

  public ReplicatedClientDao getClientDao() {
    return clientDao;
  }

  public Map<String, StaffPerson> getStaffWorkers() {
    return staffWorkers;
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(CaseRocket.class, args);
  }

}
