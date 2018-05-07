package gov.ca.cwds.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPerson.ESOptionalCollection;
import gov.ca.cwds.data.es.ElasticSearchPersonAddress;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.EsClientPerson;
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
import gov.ca.cwds.neutron.rocket.IndexResetPeopleSummaryRocket;
import gov.ca.cwds.neutron.rocket.InitialLoadJdbcRocket;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;
import gov.ca.cwds.neutron.util.transform.EntityNormalizer;

/**
 * PEOPLE SUMMARY!
 * <p>
 * Rocket to load Client person data from CMS into ElasticSearch.
 * </p>
 * 
 * @author CWDS API Team
 */
public class ClientPersonIndexerJob extends InitialLoadJdbcRocket<ReplicatedClient, EsClientPerson>
    implements AtomRowMapper<EsClientPerson>, AtomValidateDocument {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientPersonIndexerJob.class);

  private AtomLaunchDirector launchDirector;

  private AtomicInteger nextThreadNum = new AtomicInteger(0);

  /**
   * Construct batch rocket instance with all required dependencies.
   * 
   * @param dao Client DAO
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   */
  @Inject
  public ClientPersonIndexerJob(final ReplicatedClientDao dao,
      @Named("elasticsearch.dao.people-summary") final ElasticsearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, lastRunFile, mapper, flightPlan);
    this.launchDirector = launchDirector;
  }

  @Override
  public Date launch(Date lastSuccessfulRunTime) throws NeutronCheckedException {
    determineIndexName();
    return super.launch(lastSuccessfulRunTime);
  }

  @Override
  public boolean useTransformThread() {
    return false;
  }

  /**
   * Despite IBM's research prowess in machine learning and artificial intelligence, DB2's optimizer
   * can be as dumb as a stone.
   */
  @Override
  public String getPrepLastChangeSQL() {
    try {
      return NeutronDB2Utils.prepLastChangeSQL(ClientSQLResource.INSERT_CLIENT_LAST_CHG,
          determineLastSuccessfulRunTime(), getFlightPlan().getOverrideLastEndTime());
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "PEOPLE SUMMARY: ERROR BUILDING LAST CHANGE SQL! {}",
          e.getMessage());
    }
  }


  protected void prepAffectedClients(final PreparedStatement stmtInsClient,
      final Pair<String, String> p) throws SQLException {
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
  }

  @Override
  public EsClientPerson extract(ResultSet rs) throws SQLException {
    return EsClientPerson.extract(rs);
  }

  @Override
  public Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return EsClientPerson.class;
  }

  @Override
  public String getInitialLoadViewName() {
    return "MQT_CLIENT_ADDRESS";
  }

  @Override
  public String getMQTName() {
    return getInitialLoadViewName();
  }

  @Override
  public String getJdbcOrderBy() {
    return " ORDER BY X.CLT_IDENTIFIER ";
  }

  /**
   * Send all recs for same client id to the index queue.
   * 
   * @param grpRecs recs for same client id
   */
  protected void normalizeAndQueueIndex(final List<EsClientPerson> grpRecs) {
    grpRecs.stream().sorted((e1, e2) -> e1.compare(e1, e2)).sequential().sorted()
        .collect(Collectors.groupingBy(EsClientPerson::getNormalizationGroupKey)).entrySet()
        .stream().map(e -> normalizeSingle(e.getValue())).forEach(this::addToIndexQueue);
  }

  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    final StringBuilder buf = new StringBuilder();

    buf.append("SELECT x.* FROM ").append(dbSchemaName).append('.').append(getInitialLoadViewName())
        .append(" x WHERE X.CLT_IDENTIFIER BETWEEN ':fromId' AND ':toId' ");

    if (!getFlightPlan().isLoadSealedAndSensitive()) {
      buf.append(" AND x.CLT_SENSTV_IND = 'N' ");
    }

    buf.append(getJdbcOrderBy()).append(" FOR READ ONLY WITH UR ");
    return buf.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handleRangeResults(final ResultSet rs) throws SQLException {
    int cntr = 0;
    EsClientPerson m;
    Object lastId = new Object();
    final List<EsClientPerson> grpRecs = new ArrayList<>();

    // NOTE: Assumes that records are sorted by group key.
    while (!isFailed() && rs.next() && (m = extract(rs)) != null) {
      CheeseRay.logEvery(LOGGER, ++cntr, "Retrieved", "recs");
      if (!lastId.equals(m.getNormalizationGroupKey()) && cntr > 1) {

        // TODO: Fetch Placement Home addresses before normalizing.
        normalizeAndQueueIndex(grpRecs);
        grpRecs.clear(); // Single thread, re-use memory.
      }

      grpRecs.add(m);
      lastId = m.getNormalizationGroupKey();
    }
  }

  /**
   * <a href="https://osi-cwds.atlassian.net/browse/INT-1723">INT-1723</a>: Neutron to create
   * Elasticsearch Alias for people-summary index.
   */
  protected void determineIndexName() {
    // The Launch Director has a global registry of flight plans.
    if (launchDirector != null) {
      final FlightPlan resetIndexFlightPlan =
          launchDirector.getFlightPlanManger().getFlightPlan(IndexResetPeopleSummaryRocket.class);
      final String globalIndexName =
          LaunchCommand.getInstance().getCommonFlightPlan().getIndexName();

      if (resetIndexFlightPlan != null
          && StringUtils.isNotBlank(resetIndexFlightPlan.getIndexName())) {
        LOGGER.info("\n\nTake index name from IndexResetRocket flight plan!\n\n");
        flightPlan.setIndexName(resetIndexFlightPlan.getIndexName().trim());
      } else if (!StringUtils.isBlank(globalIndexName)) {
        LOGGER.info("\n\nTake index name from global flight plan!\n\n");
        flightPlan.setIndexName(globalIndexName.trim());
      }
    }
  }

  /**
   * Validate that addresses are found in ES and vice versa.
   * 
   * @param client client address to check
   * @param person person document
   * @return true if addresses pass validation
   */
  public boolean validateAddresses(ReplicatedClient client, ElasticSearchPerson person) {
    final String clientId = person.getId();
    final Map<String, ReplicatedAddress> repAddresses = client.getClientAddresses().stream()
        .filter(a -> a.getEffEndDt() == null).flatMap(ca -> ca.getAddresses().stream())
        .collect(Collectors.toMap(ReplicatedAddress::getId, a -> a));
    final Map<String, ElasticSearchPersonAddress> docAddresses = person.getAddresses().stream()
        .collect(Collectors.toMap(ElasticSearchPersonAddress::getId, a -> a));

    for (ElasticSearchPersonAddress docAddr : docAddresses.values()) {
      if (!repAddresses.containsKey(docAddr.getAddressId())) {
        LOGGER.warn("DOC ADDRESS ID {} NOT FOUND IN DATABASE {}", docAddr.getAddressId(), clientId);
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

    return true;
  }

  @Override
  public boolean validateDocument(final ElasticSearchPerson person) throws NeutronCheckedException {
    try {
      final String clientId = person.getId();
      LOGGER.info("Validate client: {}", clientId);

      // HACK: Initialize transaction. Fix DAO impl instead.
      grabTransaction();
      final ReplicatedClient client = getJobDao().find(clientId);

      return StringUtils.equals(client.getCommonFirstName(), person.getFirstName())
          && StringUtils.equals(client.getCommonLastName(), person.getLastName())
          && StringUtils.equals(client.getCommonMiddleName(), person.getMiddleName())
          && validateAddresses(client, person);
    } catch (Exception e) {
      LOGGER.error("PEOPLE SUMMARY VALIDATION ERROR!", e);
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

  @Override
  public void handleCustomJdbc(Connection con, Pair<String, String> range) throws SQLException {
    try (final PreparedStatement stmtInsClient =
        con.prepareStatement(ClientSQLResource.INSERT_CLIENT_FULL)) {
      prepAffectedClients(stmtInsClient, range);
      // readClients(stmtSelClient, mapClients);
    } finally {
      con.commit(); // release database resources
    }
  }

  @Override
  public boolean isInitialLoadJdbc() {
    return true;
  }

  @Override
  public List<Pair<String, String>> getPartitionRanges() throws NeutronCheckedException {
    return NeutronJdbcUtils.getCommonPartitionRanges64(this);
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
  public List<ReplicatedClient> normalize(List<EsClientPerson> recs) {
    return EntityNormalizer.<ReplicatedClient, EsClientPerson>normalizeList(recs);
  }

  @Override
  public int nextThreadNumber() {
    return nextThreadNum.incrementAndGet();
  }

  @Override
  public ESOptionalCollection[] keepCollections() {
    return new ESOptionalCollection[] {ESOptionalCollection.AKA, ESOptionalCollection.SAFETY_ALERT};
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception unhandled launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(ClientPersonIndexerJob.class, args);
  }

}
