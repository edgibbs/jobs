package gov.ca.cwds.jobs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPersonAddress;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.EsClientAddress;
import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.atom.AtomRowMapper;
import gov.ca.cwds.neutron.atom.AtomValidateDocument;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.rocket.ClientSQLResource;
import gov.ca.cwds.neutron.rocket.InitialLoadJdbcRocket;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;
import gov.ca.cwds.neutron.util.transform.EntityNormalizer;

/**
 * Rocket to load Clients from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class ClientIndexerJob extends InitialLoadJdbcRocket<ReplicatedClient, RawClient>
    implements AtomRowMapper<RawClient>, AtomValidateDocument {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientIndexerJob.class);

  private AtomicInteger nextThreadNum = new AtomicInteger(0);

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao Client DAO
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   * @param launchDirector launch director
   */
  @Inject
  public ClientIndexerJob(final ReplicatedClientDao dao,
      @Named("elasticsearch.dao.people") final ElasticsearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
    // if (flightPlan.isLegacyPeopleMapping()) {
    // EsClientAddress.setLegacyPeopleMapping(true);
    // }
  }

  @Override
  public void init(String lastGoodRunTimeFilename, FlightPlan flightPlan) {
    super.init(lastGoodRunTimeFilename, flightPlan);
    EsClientAddress.setLegacyPeopleMapping(flightPlan.isLegacyPeopleMapping());
  }

  // =======================
  // FIXED ROCKET SPECS:
  // =======================

  @Override
  public boolean useTransformThread() {
    return false;
  }

  @Override
  public String getPrepLastChangeSQL() {
    try {
      final String sql = NeutronDB2Utils.prepLastChangeSQL(ClientSQLResource.INS_CLIENT_LAST_CHG,
          determineLastSuccessfulRunTime(), getFlightPlan().getOverrideLastEndTime());
      LOGGER.info("LAST CHANGE SQL: {}", sql);
      return sql;
    } catch (NeutronCheckedException e) {
      throw CheeseRay.runtime(LOGGER, e, "ERROR BUILDING LAST CHANGE SQL: {}", e.getMessage());
    }
  }

  // =======================
  // ROCKET SPECS:
  // =======================

  @Override
  public Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return RawClient.class;
  }

  @Override
  public String getInitialLoadViewName() {
    return "MQT_CLIENT_ADDRESS";
  }

  @Override
  public String getMQTName() {
    return "REFRESH_ALL_MQTS"; // for Refresh MQT stored procedure
  }

  @Override
  public String getJdbcOrderBy() {
    return " ORDER BY X.CLT_IDENTIFIER ";
  }

  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    final StringBuilder buf = new StringBuilder();

    buf.append("SELECT x.* FROM ").append(dbSchemaName).append('.').append(getInitialLoadViewName())
        .append(" x WHERE x.clt_identifier BETWEEN ':fromId' AND ':toId' ");

    if (!getFlightPlan().isLoadSealedAndSensitive()) {
      buf.append(" AND x.CLT_SENSTV_IND = 'N' ");
    }

    buf.append(getJdbcOrderBy()).append(" FOR READ ONLY WITH UR ");
    final String sql = buf.toString();
    LOGGER.info("CLIENT INITIAL LOAD SQL: {}", sql);
    return sql;
  }

  @Override
  public boolean isInitialLoadJdbc() {
    return true;
  }

  @Override
  public List<Pair<String, String>> getPartitionRanges() throws NeutronCheckedException {
    return NeutronJdbcUtils.getCommonPartitionRanges64(this);
  }

  // =======================
  // WORK METHODS:
  // =======================

  @Override
  public RawClient extract(ResultSet rs) throws SQLException {
    return new RawClient().read(rs);
  }

  /**
   * Send all records for same client id to the index queue.
   * 
   * @param grpRecs records for same client id
   */
  protected void normalizeAndQueueIndex(final List<RawClient> grpRecs) {
    grpRecs.stream().sorted((e1, e2) -> e1.compare(e1, e2)).sequential()
        .collect(Collectors.groupingBy(RawClient::getNormalizationGroupKey)).entrySet().stream()
        .map(e -> normalizeSingle(e.getValue())).forEach(this::addToIndexQueue);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handleMainResults(final ResultSet rs, Connection con) throws SQLException {
    int cntr = 0;
    RawClient m;
    Object lastId = new Object();
    final List<RawClient> grpRecs = new ArrayList<>();

    // NOTE: Assumes that records are sorted by group key.
    while (!isFailed() && rs.next() && (m = extract(rs)) != null) {
      CheeseRay.logEvery(LOGGER, ++cntr, "Retrieved", "recs");
      if (!lastId.equals(m.getNormalizationGroupKey()) && cntr > 1) {
        normalizeAndQueueIndex(grpRecs);
        grpRecs.clear(); // Single thread, re-use memory.
      }

      grpRecs.add(m);
      lastId = m.getNormalizationGroupKey();
    }

    // Catch the last group.
    if (!grpRecs.isEmpty()) {
      normalizeAndQueueIndex(grpRecs);
    }
  }

  /**
   * Validate that addresses are found in ES and vice versa.
   * 
   * @param client client address to check
   * @param person person document
   * @return true if addresses pass validation
   */
  public boolean validateAddresses(final ReplicatedClient client,
      final ElasticSearchPerson person) {
    try {
      final String clientId = person.getId();
      final Map<String, ReplicatedAddress> repAddresses = client.getClientAddresses().stream()
          .filter(a -> a.getEffEndDt() == null).flatMap(ca -> ca.getAddresses().stream())
          .collect(Collectors.toMap(ReplicatedAddress::getId, a -> a));

      final Map<String, ElasticSearchPersonAddress> docAddresses = person.getAddresses().stream()
          .collect(Collectors.toMap(ElasticSearchPersonAddress::getId, a -> a));

      for (ElasticSearchPersonAddress docAddr : docAddresses.values()) {
        if (!repAddresses.containsKey(docAddr.getAddressId())) {
          LOGGER.warn("DOC ADDRESS ID {} NOT FOUND IN DATABASE {}", docAddr.getAddressId(),
              clientId);
          return false;
        }
      }

      for (ReplicatedAddress repAddr : repAddresses.values()) {
        if (!docAddresses.containsKey(repAddr.getAddressId())) {
          LOGGER.warn("ADDRESS ID {} NOT FOUND IN DOCUMENT {}", repAddr.getAddressId(), clientId);
          return false;
        }
      }

      LOGGER.info("set size: docAddresses: {}, repAddresses: {}, client addrs: {}, doc addrs: {}",
          docAddresses.size(), repAddresses.size(), client.getClientAddresses().size(),
          person.getAddresses().size());
    } catch (Exception e) {
      LOGGER.error("ERROR VALIDATING!", e);
      return false;
    }

    return true;
  }

  @Override
  public boolean validateDocument(final ElasticSearchPerson person) throws NeutronCheckedException {
    final String clientId = person.getId();
    LOGGER.info("Validate client: {}", clientId);

    try {
      // HACK: Initialize transaction. Fix DAO implementation instead.
      grabTransaction();
      final ReplicatedClient client = getJobDao().find(clientId);

      return client.getCommonFirstName().equals(person.getFirstName())
          && client.getCommonLastName().equals(person.getLastName())
          && client.getCommonMiddleName().equals(person.getMiddleName())
          && validateAddresses(client, person);
    } catch (Exception e) {
      LOGGER.error("CLIENT VALIDATION ERROR!", e);
      return false;
    }
  }

  /**
   * The "extract" part of ETL. Single producer, chained consumers. This rocket normalizes
   * <strong>without</strong> the transform thread.
   */
  @Override
  protected void threadRetrieveByJdbc() {
    pullMultiThreadJdbc();
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
  public List<ReplicatedClient> normalize(List<RawClient> recs) {
    return EntityNormalizer.<ReplicatedClient, RawClient>normalizeList(recs);
  }

  @Override
  public int nextThreadNumber() {
    return nextThreadNum.incrementAndGet();
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception unhandled launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(ClientIndexerJob.class, args);
  }

}
