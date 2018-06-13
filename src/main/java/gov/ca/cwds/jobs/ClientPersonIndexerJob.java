package gov.ca.cwds.jobs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StringType;
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
import gov.ca.cwds.neutron.enums.NeutronColumn;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.rocket.ClientSQLResource;
import gov.ca.cwds.neutron.rocket.InitialLoadJdbcRocket;
import gov.ca.cwds.neutron.rocket.PeopleSummaryThreadHandler;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;
import gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils;
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
public class ClientPersonIndexerJob extends InitialLoadJdbcRocket<ReplicatedClient, EsClientPerson>
    implements AtomRowMapper<EsClientPerson>, AtomValidateDocument {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientPersonIndexerJob.class);

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
  public Date launch(Date lastSuccessfulRunTime) throws NeutronCheckedException {
    allocateThreadHandler();
    largeLoad = determineInitialLoad(lastSuccessfulRunTime) && isLargeDataSet();
    return super.launch(lastSuccessfulRunTime);
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
      buf.append(" AND x.CLT_SENSTV_IND = 'ReplicatedClient' ");
    }

    buf.append(getJdbcOrderBy()).append(" FOR READ ONLY WITH UR ");
    return buf.toString();
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
  public String[] getPrepLastChangeSQLs() {
    final String[] ret = {getPrepLastChangeSQL()};
    return ret;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ReplicatedClient> extractLastRunRecsFromView(final Date lastRunTime,
      final Set<String> deletionResults) {
    LOGGER.info("PULL VIEW: last successful run: {}", lastRunTime);
    final Class<?> entityClass = getDenormalizedClass(); // view entity class
    final String namedQueryName =
        flightPlan.isLoadSealedAndSensitive() ? entityClass.getName() + ".findAllUpdatedAfter"
            : entityClass.getName() + ".findAllUpdatedAfterWithUnlimitedAccess";

    Transaction txn = null;
    List<EsClientPerson> recs = null;
    int count = 0;

    try (final Session session = jobDao.grabSession()) {
      txn = grabTransaction();

      // #1: INSERT all keys into GT_REFR_CLT, get total inserted.
      // #2: SELECT next N keys into GT_ID
      // #3: Pull from view, pull placement homes
      // #4: DELETE FROM GT_ID
      // #5: Repeat from step #2 until all keys are processed.

      // ClientSQLResource.INSERT_NEXT_BUNDLE

      // Insert into session temp table that drives the last change view.
      prepHibernateLastChange(session, lastRunTime, getPrepLastChangeSQLs());
      final NativeQuery<EsClientPerson> q = session.getNamedNativeQuery(namedQueryName);
      NeutronJdbcUtils.readOnlyQuery(q);
      q.setParameter(NeutronColumn.SQL_COLUMN_AFTER.getValue(),
          NeutronDateUtils.makeTimestampStringLookBack(lastRunTime), StringType.INSTANCE);

      // Iterate, process, flush.
      try {
        recs = q.list(); // read from a modestly sized key bundle
        LOGGER.info("FOUND {} RECORDS", recs.size());
        session.flush();
        session.clear();
        txn.commit(); // only on last SELECT
      } catch (Exception h) {
        fail();
        if (txn.getStatus().canRollback()) {
          txn.rollback();
        }
        throw CheeseRay.runtime(LOGGER, h, "EXTRACT SQL ERROR!: {}", h.getMessage());
      }

      // Release database resources. Don't hold on to the connection or transaction.
      LOGGER.info("PULL VIEW: DATA RETRIEVAL DONE");
      Object lastId = new Object();
      final List<ReplicatedClient> results = new ArrayList<>(recs.size()); // Size appropriately

      // Convert denormalized rows to normalized persistence objects.
      final List<EsClientPerson> groupRecs = new ArrayList<>(50);
      for (EsClientPerson m : recs) {
        if (!lastId.equals(m.getNormalizationGroupKey()) && !groupRecs.isEmpty()) {
          results.add(normalizeSingle(groupRecs));
          groupRecs.clear();
        }

        groupRecs.add(m);
        lastId = m.getNormalizationGroupKey();
        if (lastId == null) {
          // Could be a data error (invalid data in db).
          LOGGER.warn("NULL Normalization Group Key: {}", m);
          lastId = new Object();
        }
      }

      if (!groupRecs.isEmpty()) {
        results.add(normalizeSingle(groupRecs));
      }

      if (mustDeleteLimitedAccessRecords()) {
        loadRecsForDeletion(entityClass, session, lastRunTime, deletionResults);
      }

      groupRecs.clear();
      return results;
    } catch (Exception h) {
      fail();
      throw CheeseRay.runtime(LOGGER, h, "EXTRACT SQL ERROR!: {}", h.getMessage());
    } finally {
      doneRetrieve(); // Override in multi-thread mode to avoid killing the indexer thread
    }
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
    return NeutronJdbcUtils.getCommonPartitionRanges512(this);
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
    handler.get().handleFinishRange(range);
    deallocateThreadHandler();
  }

  @Override
  public void handleSecondaryJdbc(Connection con, Pair<String, String> range) throws SQLException {
    handler.get().handleSecondaryJdbc(con, range);
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

  // =========================
  // VALIDATION:
  // =========================

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
      LOGGER.warn("PEOPLE SUMMARY VALIDATION ERROR!", e);
      failValidation(); // fail optional validation without aborting the flight
      return false;
    }
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
   * Both modes. Construct a handler for this thread.
   */
  public void allocateThreadHandler() {
    if (handler.get() == null) {
      handler.set(new PeopleSummaryThreadHandler(this));
    }
  }

  /**
   * Both modes. Set this thread's handler to null.
   */
  public void deallocateThreadHandler() {
    if (handler.get() != null) {
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
