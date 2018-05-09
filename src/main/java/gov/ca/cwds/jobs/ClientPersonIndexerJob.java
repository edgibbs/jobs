package gov.ca.cwds.jobs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
import gov.ca.cwds.jobs.util.jdbc.WorkSecondaryResults;
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
import gov.ca.cwds.neutron.rocket.PeopleSummaryThreadHandler;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;
import gov.ca.cwds.neutron.util.transform.EntityNormalizer;

/**
 * PEOPLE SUMMARY ROCKET!
 * 
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

  protected transient ThreadLocal<PeopleSummaryThreadHandler> handler = new ThreadLocal<>();

  private boolean largeLoad = false;

  /**
   * Construct batch rocket instance with all required dependencies.
   * 
   * @param dao Client DAO
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   * @param launchDirector global Launch Director
   */
  @Inject
  public ClientPersonIndexerJob(final ReplicatedClientDao dao,
      @Named("elasticsearch.dao.people-summary") final ElasticsearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, lastRunFile, mapper, flightPlan);
    this.launchDirector = launchDirector;
    allocateThreadHandler();
  }

  @Override
  public Date launch(Date lastSuccessfulRunTime) throws NeutronCheckedException {
    determineIndexName();
    largeLoad = determineInitialLoad(lastSuccessfulRunTime) && isLargeDataSet();
    return super.launch(lastSuccessfulRunTime);
  }

  @Override
  protected List<ReplicatedClient> fetchLastRunResults(final Date lastRunDate,
      final Set<String> deletionResults) {
    allocateThreadHandler();
    final PeopleSummaryThreadHandler theHandler = handler.get();
    final Pair<String, String> range = Pair.<String, String>of("a", "b");

    flightLog.markRangeStart(range);
    theHandler.handleStartRange(range);

    // Read from the view, old school.
    theHandler.addAll(super.fetchLastRunResults(lastRunDate, deletionResults));

    final Session session = getJobDao().grabSession();
    final Transaction txn = grabTransaction();
    final WorkSecondaryResults<ReplicatedClient> work = new WorkSecondaryResults<>(theHandler);

    try {
      NeutronJdbcUtils.doWork(session, work);
      session.clear();
      txn.commit();

      // Done reading data. Process data, like cleansing and normalizing.
      theHandler.handleJdbcDone(range);

      LOGGER.info("LAST CHANGE COMPLETED SUCCESSFULLY!");
      return getResults();
    } catch (Exception e) {
      fail();
      txn.rollback();
      throw CheeseRay.runtime(LOGGER, e, "ERROR EXECUTING LAST CHANGE SQL! {}", e.getMessage());
    } finally {
      handleFinishRange(range);
      flightLog.markRangeComplete(range);
      deallocateThreadHandler();
    }
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
      throw CheeseRay.runtime(LOGGER, e, "ERROR BUILDING LAST CHANGE SQL! {}", e.getMessage());
    }
  }

  @Override
  public void handleStartRange(Pair<String, String> range) {
    allocateThreadHandler();
  }

  @Override
  public void handleFinishRange(Pair<String, String> range) {
    deallocateThreadHandler();
  }

  @Override
  public void handleSecondaryJdbc(Connection con, Pair<String, String> range) throws SQLException {
    handler.get().handleSecondaryJdbc(con, range);
  }

  /**
   * Both modes. Construct a handler for this thread.
   */
  protected void allocateThreadHandler() {
    if (handler.get() == null) {
      handler.set(new PeopleSummaryThreadHandler(this));
    }
  }

  /**
   * Both modes. Set this thread's handler to null.
   */
  protected void deallocateThreadHandler() {
    if (handler.get() != null) {
      handler.set(null);
    }
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
  public void handleMainResults(final ResultSet rs) throws SQLException {
    handler.get().handleMainResults(rs);
  }

  @Override
  public void handleJdbcDone(final Pair<String, String> range) {
    handler.get().handleJdbcDone(range);
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
   * The "extract" part of ETL. Single producer, chained consumers. This rocket normalizes
   * <strong>without</strong> the transform thread.
   */
  @Override
  protected void threadRetrieveByJdbc() {
    pullMultiThreadJdbc();
  }

  @Override
  public boolean isInitialLoadJdbc() {
    return true;
  }

  public boolean isLargeLoad() {
    return largeLoad;
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

    LOGGER.debug("set size: docAddresses: {}, repAddresses: {}, client addrs: {}, doc addrs: {}",
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
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception unhandled launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(ClientPersonIndexerJob.class, args);
  }

}
