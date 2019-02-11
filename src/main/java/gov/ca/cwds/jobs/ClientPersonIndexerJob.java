package gov.ca.cwds.jobs;

import java.io.IOException;
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
import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.atom.AtomRowMapper;
import gov.ca.cwds.neutron.atom.AtomValidateDocument;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.rocket.ClientSQLResource;
import gov.ca.cwds.neutron.rocket.InitialLoadJdbcRocket;
import gov.ca.cwds.neutron.rocket.PeopleSummaryLastChangeHandler;
import gov.ca.cwds.neutron.rocket.PeopleSummaryThreadHandler;
import gov.ca.cwds.neutron.rocket.PeopleSummaryThreadHandler.STEP;
import gov.ca.cwds.neutron.rocket.ReplicationLagRocket;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;
import gov.ca.cwds.neutron.util.transform.EntityNormalizer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;

/**
 * PEOPLE SUMMARY ROCKET! Let's light this candle!
 * 
 * <p>
 * Rocket to load Client person data from CMS into ElasticSearch.
 * </p>
 *
 * <p>
 * Important safety tip: sometimes rockets blow up during testing, sometimes on the launch pad. The
 * same holds true for rockets in the Neutron code base ... :-)
 * </p>
 *
 * @author CWDS API Team
 */
public class ClientPersonIndexerJob extends InitialLoadJdbcRocket<ReplicatedClient, RawClient>
    implements AtomRowMapper<RawClient>, AtomValidateDocument {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientPersonIndexerJob.class);

  private static Date lastEndTime = new Date();

  private AtomicInteger nextThreadNum = new AtomicInteger(0);

  protected transient ThreadLocal<PeopleSummaryThreadHandler> handler = new ThreadLocal<>();

  private boolean largeLoad = false;

  private boolean runMultiThread = false;

  private boolean multiThreadRetrieveDone = false;

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
    super(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
  }

  @Override
  public void close() throws IOException {
    deallocateThreadHandler(); // SNAP-877: free memory no matter what
    super.close();
  }

  @Override
  public Date launch(Date lastSuccessfulRunTime) throws NeutronCheckedException {
    final FlightLog fl = getFlightLog();
    Date ret = null;

    try {
      allocateThreadHandler();
      largeLoad = determineInitialLoad(lastSuccessfulRunTime) && isLargeDataSet();
      ret = super.launch(lastSuccessfulRunTime);

      // AR-325: replication metrics.
      lastEndTime = new Date();
      fl.setLastEndTime(lastEndTime.getTime());

      final Float lastReplicationSecs = ReplicationLagRocket.getLastReplicationSeconds();
      if (lastReplicationSecs != null && lastReplicationSecs != 0.0F) {
        fl.addOtherMetric(STEP.REPLICATION_TIME_SECS.name().toLowerCase(), lastReplicationSecs);
        fl.addOtherMetric("blue_line_secs", lastReplicationSecs); // blue = replication
        fl.addOtherMetric("blue_line_millis", lastReplicationSecs * 1000);
      }

    } finally {
      deallocateThreadHandler(); // SNAP-877: free memory no matter what
    }

    return ret;
  }

  @Override
  public List<ReplicatedClient> fetchLastRunResults(final Date lastRunDate,
      final Set<String> deletionResults) {
    allocateThreadHandler();
    return handler.get().fetchLastRunNormalizedResults(lastRunDate, deletionResults);
  }

  @Override
  public boolean useTransformThread() {
    return false;
  }

  @Override
  public RawClient extract(ResultSet rs) throws SQLException {
    return new RawClient().read(rs);
  }

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
    return getInitialLoadViewName();
  }

  @Override
  public String getJdbcOrderBy() {
    return " ORDER BY CLT_IDENTIFIER ";
  }

  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    String ret = null;
    final StringBuilder buf = new StringBuilder();

    // Return no records from the obsolete MQT.
    // Real work in PeopleSummaryThreadHandler.
    buf.append(
        "SELECT '1234567abc' AS CLT_IDENTIFIER FROM SYSIBM.SYSDUMMY1 X WHERE 1=2 AND '0' BETWEEN ':fromId' AND ':toId'")
        .append(getJdbcOrderBy()).append(" FOR READ ONLY WITH UR ");
    ret = buf.toString();
    LOGGER.trace("initial load: SQL:\n\n{}\n", ret);

    return ret;
  }

  /**
   * Despite IBM's research prowess in machine learning and artificial intelligence, the IQ of DB2's
   * optimizer rivals a simple paramecium.
   */
  @Override
  public String getPrepLastChangeSQL() {
    String ret = null;
    try {
      ret = NeutronDB2Utils.prepLastChangeSQL(ClientSQLResource.INS_CLI_LST_CHG,
          determineLastSuccessfulRunTime(), getFlightPlan().getOverrideLastEndTime());
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "ERROR BUILDING LAST CHANGE SQL! {}", e.getMessage());
    }

    LOGGER.info("last change: SQL:\n\n{}\n", ret);
    return ret;
  }

  @Override
  public String[] getPrepLastChangeSQLs() {
    final String[] ret = {getPrepLastChangeSQL()};
    return ret;
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
    return NeutronJdbcUtils.getCommonPartitionRanges1024(this);
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

  /**
   * The "extract" part of ETL. Single producer, chained consumers. This rocket normalizes
   * <strong>without</strong> the transform thread.
   */
  @Override
  protected void threadRetrieveByJdbc() {
    pullMultiThreadJdbc();
  }

  @Override
  public int nextThreadNumber() {
    return nextThreadNum.incrementAndGet();
  }

  @Override
  public ESOptionalCollection[] keepCollections() {
    return new ESOptionalCollection[] {ESOptionalCollection.AKA, ESOptionalCollection.SAFETY_ALERT};
  }

  // =========================
  // INITIAL LOAD HANDLERS:
  // =========================

  @Override
  public void handleStartRange(Pair<String, String> range) {
    multiThreadRetrieveDone = true;
    deallocateThreadHandler();
    allocateThreadHandler();
  }

  @Override
  public void handleFinishRange(Pair<String, String> range) {
    try {
      handler.get().handleFinishRange(range);
    } finally {
      // Deallocate the thread instance, no matter what.
      deallocateThreadHandler();
    }
  }

  @Override
  public void handleSecondaryJdbc(Connection con, Pair<String, String> range) throws SQLException {
    handler.get().handleSecondaryJdbc(con, range);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handleMainResults(final ResultSet rs, Connection con) throws SQLException {
    handler.get().handleMainResults(rs, con);
  }

  @Override
  public void handleJdbcDone(final Pair<String, String> range) {
    handler.get().handleJdbcDone(range);
  }

  // =========================
  // VALIDATION:
  // =========================

  @Override
  public boolean validateDocument(final ElasticSearchPerson person) throws NeutronCheckedException {
    boolean ret = true;

    if (flightPlan.isValidateAfterIndexing()) {
      try {
        final String clientId = person.getId();
        LOGGER.debug("Validate client: {}", clientId);

        // HACK: Initialize transaction. Fix DAO impl instead.
        grabTransaction();
        final ReplicatedClient client = getJobDao().find(clientId);

        ret = StringUtils.equals(client.getCommonFirstName(), person.getFirstName())
            && StringUtils.equals(client.getCommonLastName(), person.getLastName())
            && StringUtils.equals(client.getCommonMiddleName(), person.getMiddleName())
            && validateAddresses(client, person);
      } catch (Exception e) {
        LOGGER.warn("PEOPLE SUMMARY VALIDATION ERROR!", e);
        failValidation(); // fail optional validation without aborting the flight
        ret = false;
      }
    }

    return ret;
  }

  /**
   * Validate that addresses are found in Elasticsearch and vice versa.
   * 
   * @param client client address to check
   * @param person person document
   * @return true if addresses pass validation
   */
  public boolean validateAddresses(ReplicatedClient client, ElasticSearchPerson person) {
    final short residenceType = (short) 32;
    final String clientId = person.getId();
    final Map<String, ReplicatedAddress> repAddresses = client.getClientAddresses().stream()
        .filter(ca -> ca.getEffEndDt() == null && ca.getAddressType() != null
            && ca.getAddressType() == residenceType) // active residence only
        .flatMap(ca -> ca.getAddresses().stream())
        .collect(Collectors.toMap(ReplicatedAddress::getId, a -> a));
    final Map<String, ElasticSearchPersonAddress> docAddresses = person.getAddresses().stream()
        .collect(Collectors.toMap(ElasticSearchPersonAddress::getId, a -> a));

    for (ElasticSearchPersonAddress docAddr : docAddresses.values()) {
      if (docAddr.getLegacyDescriptor().getLegacyTableName().equals(LegacyTable.ADDRESS.getName())
          && !repAddresses.containsKey(docAddr.getAddressId())) {
        LOGGER.debug("DOC ADDRESS ID {} NOT FOUND IN DATABASE {}", docAddr.getAddressId(),
            clientId);
        return false;
      }
    }

    for (ReplicatedAddress repAddr : repAddresses.values()) {
      if (!docAddresses.containsKey(repAddr.getAddressId())) {
        LOGGER.debug("ADDRESS ID {} NOT FOUND IN DOCUMENT {}", repAddr.getAddressId(), clientId);
        return false;
      }
    }

    LOGGER.debug("set size: docAddresses: {}, repAddresses: {}, client addrs: {}, doc addrs: {}",
        docAddresses.size(), repAddresses.size(), client.getClientAddresses().size(),
        person.getAddresses().size());
    return true;
  }

  /**
   * Both modes. Construct an appropriate handler for this thread.
   */
  public void allocateThreadHandler() {
    deallocateThreadHandler();
    handler.set(getFlightPlan().isLastRunMode() ? new PeopleSummaryLastChangeHandler(this)
        : new PeopleSummaryThreadHandler(this));
  }

  /**
   * Both modes. Set this thread's handler to null.
   */
  public void deallocateThreadHandler() {
    if (handler != null && handler.get() != null) {
      handler.get().setRocket(null);
      handler.get().clear();
      handler.set(null);
    }
  }

  @Override
  public void startMultiThreadRetrieve() {
    runMultiThread = true;
    multiThreadRetrieveDone = false;
  }

  @Override
  public void doneMultiThreadRetrieve() {
    multiThreadRetrieveDone = true;
    doneRetrieve();
  }

  @Override
  public void doneRetrieve() {
    if (!runMultiThread || (runMultiThread && multiThreadRetrieveDone)) {
      super.doneRetrieve();
    }
  }

  @Override
  public String getEventType() {
    return "neutron_lc_client";
  }

  public static Date getLastEndTime() {
    return lastEndTime;
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
