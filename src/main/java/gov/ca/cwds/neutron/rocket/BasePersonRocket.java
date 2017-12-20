package gov.ca.cwds.neutron.rocket;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.persistence.FlushModeType;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.hibernate.CacheMode;
import org.hibernate.HibernateException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StringType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.BaseDaoImpl;
import gov.ca.cwds.data.DaoException;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.data.std.ApiPersonAware;
import gov.ca.cwds.jobs.component.BulkProcessorBuilder;
import gov.ca.cwds.jobs.exception.JobsException;
import gov.ca.cwds.jobs.exception.NeutronException;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.jobs.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.atom.AtomDocumentSecurity;
import gov.ca.cwds.neutron.atom.AtomInitialLoad;
import gov.ca.cwds.neutron.atom.AtomPersonDocPrep;
import gov.ca.cwds.neutron.atom.AtomTransform;
import gov.ca.cwds.neutron.atom.AtomValidateDocument;
import gov.ca.cwds.neutron.enums.NeutronColumn;
import gov.ca.cwds.neutron.enums.NeutronDateTimeFormat;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.jetpack.JobLogs;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;

/**
 * Base person rocket to documents from CMS into ElasticSearch.
 * 
 * <p>
 * This class implements {@link AutoCloseable} and automatically closes common resources, such as
 * {@link ElasticsearchDao} and Hibernate {@link SessionFactory}.
 * </p>
 * 
 * <p>
 * <strong>Auto mode ("smart" mode)</strong> takes the same parameters as last run and determines
 * whether the job has never been run. If the last run date is older than 50 years, then then assume
 * that the job is populating ElasticSearch for the first time and run all initial batch loads.
 * </p>
 * 
 * <h3>Command Line:</h3>
 * 
 * <pre>
 * {@code java gov.ca.cwds.jobs.ClientIndexerJob -c config/local.yaml -l /Users/CWS-NS3/client_indexer_time.txt}
 * </pre>
 * 
 * @author CWDS API Team
 * @param <T> ES replicated Person persistence class
 * @param <M> MQT entity class, if any, or T
 * @see FlightPlan
 */
public abstract class BasePersonRocket<T extends PersistentObject, M extends ApiGroupNormalizer<?>>
    extends LastFlightRocket implements AutoCloseable, AtomPersonDocPrep<T>, AtomInitialLoad<T, M>,
    AtomTransform<T, M>, AtomDocumentSecurity, AtomValidateDocument {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(BasePersonRocket.class);

  /**
   * Jackson ObjectMapper.
   */
  protected ObjectMapper mapper;

  private final BulkProcessorBuilder bulkProcessorBuilder;

  /**
   * Main DAO for the supported persistence class.
   */
  protected transient BaseDaoImpl<T> jobDao;

  /**
   * Elasticsearch client DAO.
   */
  protected transient ElasticsearchDao esDao;

  /**
   * Hibernate session factory.
   * 
   * <p>
   * OPTION: get this from Hibernate.
   * </p>
   */
  protected final SessionFactory sessionFactory;

  /**
   * Track this rocket's flight progress.
   */
  protected FlightLog flightLog = new FlightLog();

  /**
   * Queue of raw, denormalized records waiting to be normalized.
   * <p>
   * <strong>NOTE</strong>: some rockets normalize on their own, since the normalize/transform step
   * is inexpensive.
   * </p>
   * 
   * <p>
   * <strong>MOVE</strong> to another unit.
   * </p>
   */
  protected LinkedBlockingDeque<M> queueNormalize = new LinkedBlockingDeque<>(2000);

  /**
   * Queue of normalized records waiting to publish to Elasticsearch.
   * 
   * <p>
   * <strong>MOVE</strong> to another unit.
   * </p>
   * <p>
   * <strong>OPTION:</strong> size by environment (production size or small test data set).
   * </p>
   */
  protected LinkedBlockingDeque<T> queueIndex = new LinkedBlockingDeque<>(5000);

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param jobDao Person DAO, such as {@link ReplicatedClientDao}
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   */
  @Inject
  public BasePersonRocket(final BaseDaoImpl<T> jobDao, final ElasticsearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper, FlightPlan flightPlan) {
    super(lastRunFile, flightPlan);
    this.jobDao = jobDao;
    this.esDao = esDao;
    this.mapper = mapper;
    this.sessionFactory = jobDao.getSessionFactory();
    this.bulkProcessorBuilder = new BulkProcessorBuilder(esDao, flightLog);
    this.flightLog.setRocketName(getClass().getSimpleName());
  }

  /**
   * Build a delete request to remove the document from the index.
   * 
   * @param id primary key
   * @return bulk delete request
   */
  public DeleteRequest bulkDelete(final String id) {
    return new DeleteRequest(getFlightPlan().getIndexName(),
        esDao.getConfig().getElasticsearchDocType(), id);
  }

  /**
   * Adds a normalized object to the index queue and trap InterruptedException. Suitable for streams
   * and lambda.
   * 
   * @param norm normalized object to add to index queue
   */
  protected void addToIndexQueue(T norm) {
    try {
      JobLogs.logEvery(flightLog.markQueuedToIndex(), "index queue", "recs");
      queueIndex.putLast(norm);
    } catch (InterruptedException e) {
      fail();
      Thread.currentThread().interrupt();
      throw JobLogs.runtime(LOGGER, e, "INTERRUPTED! {}", e.getMessage());
    }
  }

  /**
   * Instantiate one Elasticsearch BulkProcessor per working thread.
   * 
   * @return an ES bulk processor
   */
  public BulkProcessor buildBulkProcessor() {
    return this.bulkProcessorBuilder.buildBulkProcessor();
  }

  /**
   * Publish a Person record to Elasticsearch with a bulk processor.
   * 
   * <p>
   * Child implementations may customize this method and generate different JSON for create/insert
   * and update to prevent overwriting data from other jobs.
   * </p>
   * 
   * @param bp {@link #buildBulkProcessor()} for this thread
   * @param t Person record to write
   * @throws IOException if unable to prepare request
   * @see #prepareUpsertRequest(ElasticSearchPerson, PersistentObject)
   */
  protected void prepareDocument(final BulkProcessor bp, T t) throws IOException {
    Arrays.stream(ElasticTransformer.buildElasticSearchPersons(t))
        .map(p -> prepareUpsertRequestNoChecked(p, t)).forEach(x -> { // NOSONAR
          ElasticTransformer.pushToBulkProcessor(flightLog, bp, x);
        });
  }

  /**
   * Prepare an "upsert" request <strong>without a checked exception</strong> and throw a runtime
   * {@link JobsException} on error.
   * 
   * <p>
   * This method's signature is easier to use in functional lambda and stream calls than method
   * signatures with checked exceptions.
   * </p>
   * 
   * @param esp person document object
   * @param t normalized entity
   * @return prepared upsert request
   */
  @SuppressWarnings("rawtypes")
  protected DocWriteRequest prepareUpsertRequestNoChecked(ElasticSearchPerson esp, T t) {
    DocWriteRequest<?> ret;
    try {
      if (isDelete(t)) {
        ret = bulkDelete((String) t.getPrimaryKey()); // WARNING: cannot assume String PK.
        getFlightLog().incrementBulkDeleted();
      } else {
        ret = prepareUpsertRequest(esp, t);
        getFlightLog().incrementBulkPrepared();
      }
    } catch (Exception e) {
      throw JobLogs.runtime(LOGGER, e, "ERROR BUILDING UPSERT!: PK: {}", t.getPrimaryKey()); // NOSONAR
    }

    return ret;
  }

  /**
   * Prepare sections of a document for update.
   * 
   * @param esp ES document, already prepared by
   *        {@link ElasticTransformer#buildElasticSearchPersonDoc(ApiPersonAware)}
   * @param t target ApiPersonAware instance
   * @return left = insert JSON, right = update JSON throws JsonProcessingException on JSON parse
   *         error
   * @throws NeutronException on Elasticsearch disconnect
   * @see ElasticTransformer#prepareUpsertRequest(AtomPersonDocPrep, String, String,
   *      ElasticSearchPerson, PersistentObject)
   */
  protected UpdateRequest prepareUpsertRequest(ElasticSearchPerson esp, T t)
      throws NeutronException {
    if (StringUtils.isNotBlank(getLegacySourceTable())) {
      esp.setLegacySourceTable(getLegacySourceTable());
    }

    return ElasticTransformer.<T>prepareUpsertRequest(this, getFlightPlan().getIndexName(),
        esDao.getConfig().getElasticsearchDocType(), esp, t);
  }

  protected void addThread(Runnable target, List<Thread> threads) {
    threads.add(new Thread(target));
  }

  protected void addThread(boolean make, Runnable target, List<Thread> threads) {
    if (make) {
      addThread(target, threads);
    }
  }

  /**
   * ENTRY POINT FOR INITIAL LOAD.
   * 
   * <p>
   * Run threads to extract, transform, and index.
   * </p>
   * 
   * @throws NeutronException bombed
   */
  protected void doInitialLoadJdbc() throws NeutronException {
    final List<Thread> threads = new ArrayList<>();

    try {
      nameThread("initial_load");
      LOGGER.info("INITIAL LOAD WITH JDBC!");
      addThread(true, this::threadIndex, threads);
      addThread(useTransformThread(), this::threadNormalize, threads);
      addThread(true, this::threadRetrieveByJdbc, threads);

      // Start 'em up.
      for (Thread t : threads) {
        t.start();
      }

      // Wait for 'em to finish.
      for (Thread t : threads) {
        t.join();
      }

      Thread.sleep(NeutronIntegerDefaults.SLEEP_MILLIS.getValue()); // WARN: threading practices
      LOGGER.info("PROGRESS TRACK: {}", () -> this.getFlightLog().toString());
    } catch (Exception e) {
      fail();
      Thread.currentThread().interrupt();
      throw JobLogs.checked(LOGGER, e, "JDBC EXCEPTION: {}", e);
    } finally {
      done();
      this.finish(); // OK for initial load.
    }

    LOGGER.info("DONE: JDBC initial load");
  }

  /**
   * The "extract" part of ETL. Single producer, chained consumers.
   */
  protected void threadRetrieveByJdbc() {
    nameThread("jdbc");
    LOGGER.info("BEGIN: jdbc thread");

    try (final Connection con = jobDao.getSessionFactory().getSessionFactoryOptions()
        .getServiceRegistry().getService(ConnectionProvider.class).getConnection()) {
      con.setSchema(getDBSchemaName());
      con.setAutoCommit(false);

      // Linux MQT lacks ORDER BY clause. Must sort manually.
      // Either detect platform or force ORDER BY clause.
      final String query = getInitialLoadQuery(getDBSchemaName());
      LOGGER.info("query: {}", query);

      // Enable parallelism for underlying database.
      NeutronDB2Utils.enableParallelism(con);

      M m;
      try (final Statement stmt = con.createStatement()) {
        stmt.setFetchSize(15000); // faster
        stmt.setMaxRows(0);
        stmt.setQueryTimeout(100000);
        final ResultSet rs = stmt.executeQuery(query); // NOSONAR

        int cntr = 0;
        while (isRunning() && rs.next() && (m = extract(rs)) != null) {
          JobLogs.logEvery(++cntr, "Retrieved", "recs");
          queueNormalize.putLast(m);
        }

        con.commit();
      }
    } catch (Exception e) {
      fail();
      throw JobLogs.runtime(LOGGER, e, "BATCH ERROR! {}", e.getMessage());
    } finally {
      doneRetrieve();
    }

    LOGGER.info("DONE: jdbc thread");
  }

  protected int normalizeLoop(final List<M> grpRecs, Object theLastId, int inCntr)
      throws InterruptedException {
    M m;
    T t;
    Object lastId = theLastId;
    int cntr = inCntr;
    ++cntr;

    while ((m = queueNormalize.pollFirst(NeutronIntegerDefaults.POLL_MILLIS.getValue(),
        TimeUnit.MILLISECONDS)) != null) {
      JobLogs.logEvery(++cntr, "Transformed", "recs");

      // NOTE: Assumes that records are sorted by group key.
      // End of group. Normalize these group records.
      if (!lastId.equals(m.getNormalizationGroupKey()) && cntr > 1
          && (t = normalizeSingle(grpRecs)) != null) {
        LOGGER.trace("queueIndex.putLast: id: {}", t.getPrimaryKey());
        queueIndex.putLast(t);
        grpRecs.clear(); // Single thread, re-use memory.
      }

      grpRecs.add(m);
      lastId = m.getNormalizationGroupKey();
    }

    // Last bundle.
    if (!grpRecs.isEmpty() && (t = normalizeSingle(grpRecs)) != null) {
      queueIndex.putLast(t);
      grpRecs.clear(); // Single thread, re-use memory.
    }

    return cntr;
  }

  /**
   * The "transform" part of ETL. Single-thread consumer, second stage of initial load. Convert
   * denormalized records to normalized ones and pass to the index queue.
   */
  protected void threadNormalize() {
    nameThread("normalize");
    LOGGER.info("BEGIN: normalize thread");

    int cntr = 0;
    Object lastId = new Object();
    final List<M> grpRecs = new ArrayList<>();

    try {
      while (isRunning() && !(isRetrieveDone() && queueNormalize.isEmpty())) {
        cntr = normalizeLoop(grpRecs, lastId, cntr);
      }
    } catch (Exception e) {
      fail();
      Thread.currentThread().interrupt();
      throw JobLogs.runtime(LOGGER, e, "Transformer: FATAL ERROR: {}", e.getMessage());
    } finally {
      doneTransform();
    }

    LOGGER.info("DONE: normalize thread");
  }

  /**
   * The "load" part of ETL. Read from the normalized record queue and index into ES.
   */
  protected void threadIndex() {
    nameThread("es_indexer");
    LOGGER.info("BEGIN: indexer thread");
    final BulkProcessor bp = buildBulkProcessor();
    int cntr = 0;

    try {
      while (!(isFailed() || (isRetrieveDone() && isTransformDone() && queueIndex.isEmpty()))) {
        cntr = bulkPrepare(bp, cntr);
      }

      // Catch stragglers.
      cntr = bulkPrepare(bp, cntr);
      LOGGER.debug("Flush ES bulk processor ... recs processed: {}", cntr);
      bp.flush();

      LOGGER.debug("Waiting to close ES bulk processor ...");
      bp.awaitClose(NeutronIntegerDefaults.WAIT_BULK_PROCESSOR.getValue(), TimeUnit.SECONDS);
      LOGGER.debug("Closed ES bulk processor");
    } catch (Exception e) {
      fail();
      Thread.currentThread().interrupt();
      throw JobLogs.runtime(LOGGER, e, "FATAL INDEXING ERROR: {}", e.getMessage());
    } finally {
      doneIndex();
    }

    LOGGER.info("DONE: indexer thread");
  }

  /**
   * Poll the index queue, track counts, and bulk prepare documents.
   * 
   * @param bp ES bulk processor
   * @param cntr record count
   * @throws IOException on IO error
   * @throws InterruptedException if thread interrupted
   * @return number of documents prepared
   */
  protected int bulkPrepare(final BulkProcessor bp, int cntr)
      throws IOException, InterruptedException {
    int i = cntr;
    T t;

    while (isRunning() && (t = queueIndex.pollFirst(NeutronIntegerDefaults.POLL_MILLIS.getValue(),
        TimeUnit.MILLISECONDS)) != null) {
      JobLogs.logEvery(++i, "Indexed", "recs to ES");
      prepareDocument(bp, t);
    }
    return i;
  }

  /**
   * Prepare a document and trap IOException.
   * 
   * @param bp bulk processor
   * @param p ApiPersonAware object
   */
  protected void prepareDocumentTrapException(BulkProcessor bp, T p) {
    try {
      prepareDocument(bp, p);
    } catch (Exception e) {
      fail();
      throw JobLogs.runtime(LOGGER, e, "IO EXCEPTION: {}", e.getMessage());
    }
  }

  /**
   * Forcibly remove sealed or sensitive documents.
   * 
   * @param deletionResults documents to remove from Elasticsearch
   * @param bp bulk processor
   */
  protected void deleteRestricted(final Set<String> deletionResults, final BulkProcessor bp) {
    if (!deletionResults.isEmpty()) {
      LOGGER.warn("Found {} people to delete, IDs: {}", deletionResults.size(), deletionResults);

      for (String deletionId : deletionResults) {
        bp.add(new DeleteRequest(getFlightPlan().getIndexName(),
            esDao.getConfig().getElasticsearchDocType(), deletionId));
      }

      flightLog.addToBulkDeleted(deletionResults.size());
    }
  }

  protected List<T> fetchLastRunResults(final Date lastRunDate, final Set<String> deletionResults) {
    return this.isViewNormalizer() ? extractLastRunRecsFromView(lastRunDate, deletionResults)
        : extractLastRunRecsFromTable(lastRunDate);
  }

  /**
   * <strong>ENTRY POINT FOR LAST RUN.</strong>
   *
   * <p>
   * Fetch all records for the next batch run, either by bucket or last successful run date. Pulls
   * either from an MQT via {@link #extractLastRunRecsFromView(Date, Set)}, if
   * {@link #isViewNormalizer()} is overridden, else from the base table directly via
   * {@link #extractLastRunRecsFromTable(Date)}.
   * </p>
   * 
   * @param lastRunDt last time the batch ran successfully.
   * @return List of results to process
   * @throws NeutronException oops!
   * @see gov.ca.cwds.neutron.rocket.LastFlightRocket#launch(java.util.Date)
   */
  protected Date doLastRun(Date lastRunDt) throws NeutronException {
    LOGGER.info("LAST RUN MODE!");

    try {
      final BulkProcessor bp = buildBulkProcessor();
      final Set<String> deletionResults = new HashSet<>();
      final List<T> results = fetchLastRunResults(lastRunDt, deletionResults);

      if (results != null && !results.isEmpty()) {
        LOGGER.info("Found {} people to index", results.size());
        results.stream().forEach(p -> { // NOSONAR
          getFlightLog().addAffectedDocumentId(p.getPrimaryKey().toString());
          prepareDocumentTrapException(bp, p);
        });
      }

      deleteRestricted(deletionResults, bp); // last run only
      awaitBulkProcessorClose(bp);
      validateDocuments();
      return new Date(getFlightLog().getStartTime());
    } catch (Exception e) {
      fail();
      throw JobLogs.checked(LOGGER, e, "General Exception: {}", e.getMessage());
    } finally {
      done();
    }
  }

  protected void sizeQueues(final Date lastRun) {
    // Configure queue sizes for last run or initial load.
    if (determineFlightMode(lastRun)) {
      queueNormalize = new LinkedBlockingDeque<>(2000);
      queueIndex = new LinkedBlockingDeque<>(5000);
    } else {
      queueNormalize = new LinkedBlockingDeque<>(50000);
      queueIndex = new LinkedBlockingDeque<>(125000);
    }
  }

  protected boolean determineFlightMode(final Date lastRun) {
    return this.getFlightPlan().determineInitialLoad(lastRun);
  }

  /**
   * Lambda runs a number of threads up to max processor cores. Queued jobs wait until a worker
   * thread is available.
   * 
   * <p>
   * Auto mode ("smart" mode) takes the same parameters as last run and determines whether the job
   * has never been run. If the last run date is older than 50 years, then then assume that the job
   * is populating ElasticSearch for the first time and run all initial batch loads.
   * </p>
   * 
   * {@inheritDoc}
   * 
   * @see gov.ca.cwds.neutron.rocket.LastFlightRocket#launch(java.util.Date)
   */
  @Override
  public Date launch(Date lastSuccessfulRunTime) throws NeutronException {
    LOGGER.info("RUNNING JOB: {}", getClass().getName());
    Date ret;

    try {
      // If index name is provided, use it, else take alias from ES config.
      final String indexNameOverride = getFlightPlan().getIndexName();
      final String effectiveIndexName = StringUtils.isBlank(indexNameOverride)
          ? esDao.getConfig().getElasticsearchAlias() : indexNameOverride;
      getFlightPlan().setIndexName(effectiveIndexName); // WARNING: probably a bad idea.
      final Date lastRun = calcLastRunDate(lastSuccessfulRunTime);

      sizeQueues(lastRun);
      if (determineFlightMode(lastRun)) {
        flightLog.setInitialLoad(true);
        refreshMQT();
        if (isInitialLoadJdbc()) {
          doInitialLoadJdbc();
        } else {
          extractHibernate();
        }
      } else {
        doLastRun(lastRun);
      }

      // CHECKSTYLE:OFF
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Updating last successful run time to {}",
            new SimpleDateFormat(NeutronDateTimeFormat.LAST_RUN_DATE_FORMAT.getFormat())
                .format(flightLog.getStartTime()));
      }
      // CHECKSTYLE:ON
      ret = new Date(flightLog.getStartTime());
    } catch (NeutronException | RuntimeException e) {
      fail();
      throw JobLogs.checked(LOGGER, e, "ROCKET EXPLODED! {}", e.getMessage());
    } finally {
      done();
      try {
        this.close();
      } catch (IOException io) {
        LOGGER.error("IOEXCEPTION ON CLOSE! {}", io.getMessage(), io);
      }
    }

    return ret;
  }

  /**
   * Pull records changed since the last successful run.
   * 
   * <p>
   * If this job defines a de-normalized view entity, then pull from that. Otherwise, pull from the
   * table entity.
   * </p>
   * 
   * @param lastRunTime last successful run date/time
   * @return List of normalized entities
   */
  @SuppressWarnings("unchecked")
  protected List<T> extractLastRunRecsFromTable(final Date lastRunTime) {
    LOGGER.info("LAST SUCCESSFUL RUN: {}", lastRunTime);
    final Class<?> entityClass = jobDao.getEntityClass();
    final String namedQueryName = entityClass.getName() + ".findAllUpdatedAfter";
    final Session session = jobDao.getSessionFactory().getCurrentSession();
    final Transaction txn = getOrCreateTransaction(); // Cheesy hack.

    try {
      final NativeQuery<T> q = session.getNamedNativeQuery(namedQueryName);
      q.setParameter(NeutronColumn.SQL_COLUMN_AFTER.getValue(),
          NeutronJdbcUtils.makeSimpleTimestampString(lastRunTime), StringType.INSTANCE);

      final ImmutableList.Builder<T> results = new ImmutableList.Builder<>();
      final List<T> recs = q.list();

      LOGGER.info("FOUND {} RECORDS", recs.size());
      results.addAll(recs);
      session.clear();
      txn.commit();
      return results.build();
    } catch (HibernateException h) {
      fail();
      LOGGER.error("EXTRACT ERROR! {}", h.getMessage(), h);
      txn.rollback();
      throw new DaoException(h);
    } finally {
      doneRetrieve();
    }
  }

  @SuppressWarnings("unchecked")
  protected void loadRecsForDeletion(final Class<?> entityClass, final Session session,
      final Date lastRunTime, Set<String> deletionResults) {
    LOGGER.warn("DELETE RESTRICTED RECORDS!");
    final String namedQueryNameForDeletion =
        entityClass.getName() + ".findAllUpdatedAfterWithLimitedAccess";
    final NativeQuery<M> q = session.getNamedNativeQuery(namedQueryNameForDeletion);
    q.setParameter(NeutronColumn.SQL_COLUMN_AFTER.getValue(),
        NeutronJdbcUtils.makeSimpleTimestampString(lastRunTime), StringType.INSTANCE);

    final List<M> deletionRecs = q.list();
    if (deletionRecs != null && !deletionRecs.isEmpty()) {
      for (M rec : deletionRecs) {
        // Assuming group key represents ID of client to delete. This is true for client,
        // referral history, case history jobs.
        Object groupKey = rec.getNormalizationGroupKey();
        if (groupKey != null) {
          deletionResults.add(groupKey.toString());
        }
      }
    }

    if (!deletionResults.isEmpty()) {
      LOGGER.warn("FOUND {} RECORDS FOR DELETION", deletionResults.size());
    }
  }

  /**
   * Pull from view for last run mode.
   * 
   * @param lastRunTime last successful run time
   * @param deletionResults records to remove
   * @return List of normalized entities
   */
  @SuppressWarnings("unchecked")
  protected List<T> extractLastRunRecsFromView(final Date lastRunTime,
      final Set<String> deletionResults) {
    LOGGER.info("PULL VIEW: last successful run: {}", lastRunTime);
    final Class<?> entityClass = getDenormalizedClass(); // view entity class
    final String namedQueryName =
        getFlightPlan().isLoadSealedAndSensitive() ? entityClass.getName() + ".findAllUpdatedAfter"
            : entityClass.getName() + ".findAllUpdatedAfterWithUnlimitedAccess";

    final Session session = jobDao.getSessionFactory().getCurrentSession();
    final Transaction txn = getOrCreateTransaction();
    Object lastId = new Object();

    try {
      // Insert into session temp table that drives a last change view.
      prepHibernateLastChange(session, lastRunTime);
      final NativeQuery<M> q = session.getNamedNativeQuery(namedQueryName);
      q.setParameter(NeutronColumn.SQL_COLUMN_AFTER.getValue(),
          NeutronJdbcUtils.makeSimpleTimestampString(lastRunTime), StringType.INSTANCE);

      final ImmutableList.Builder<T> results = new ImmutableList.Builder<>();
      final List<M> recs = q.list();
      LOGGER.info("FOUND {} RECORDS", recs.size());

      // Convert denormalized rows to normalized persistence objects.
      final List<M> groupRecs = new ArrayList<>();
      for (M m : recs) {
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
      session.clear();
      txn.commit();
      return results.build();
    } catch (Exception h) {
      fail();
      txn.rollback();
      throw JobLogs.runtime(LOGGER, h, "EXTRACT SQL ERROR!: {}", h.getMessage());
    } finally {
      doneRetrieve();
    }
  }

  @Override
  public synchronized void close() throws IOException {
    if (isRunning() && !LaunchCommand.isSchedulerMode()) {
      LOGGER.warn("CLOSING CONNECTIONS!!");

      if (this.esDao != null) {
        LOGGER.warn("CLOSING ES DAO");
        this.esDao.close();
      }

      if (this.sessionFactory != null) {
        LOGGER.warn("CLOSING SESSION FACTORY");
        this.sessionFactory.close();
      }

      catchYourBreath();
    }
  }

  @Override
  protected synchronized void finish() throws NeutronException {
    final String rocketName = this.getClass().getName();
    LOGGER.info("FINISH JOB! {}", rocketName);
    try {
      done();
      close();
    } catch (Exception e) {
      fail();
      throw JobLogs.checked(LOGGER, e, "ERROR LANDING ROCKET! {}, {}", rocketName, e.getMessage());
    }
    LOGGER.info("JOB FINISHED!");
  }

  /**
   * Divide work into buckets: pull a unique range of identifiers so that no bucket results overlap.
   * <p>
   * Where possible, prefer use {@link #threadRetrieveByJdbc()} or {@link #extractHibernate()}
   * instead.
   * </p>
   * 
   * @param minId start of identifier range
   * @param maxId end of identifier range
   * @return collection of entity results
   */
  @SuppressWarnings("unchecked")
  protected List<T> pullBucketRange(String minId, String maxId) {
    LOGGER.info("PULL BUCKET RANGE {} to {}", minId, maxId);
    final Pair<String, String> p = Pair.of(minId, maxId);
    getFlightLog().markRangeStart(p);

    final Class<?> entityClass =
        getDenormalizedClass() != null ? getDenormalizedClass() : getJobDao().getEntityClass();
    final String namedQueryName = entityClass.getName() + ".findBucketRange";
    final Session session = jobDao.getSessionFactory().getCurrentSession();
    final Transaction txn = getOrCreateTransaction();

    try {
      session.clear();
      session.setCacheMode(CacheMode.IGNORE);
      session.setDefaultReadOnly(true);
      session.setFlushMode(FlushModeType.COMMIT);

      final NativeQuery<T> q = session.getNamedNativeQuery(namedQueryName);
      q.setParameter("min_id", minId, StringType.INSTANCE)
          .setParameter("max_id", maxId, StringType.INSTANCE)
          .setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());

      // No reduction/normalization. Iterate, process, flush.
      final ScrollableResults results = q.scroll(ScrollMode.FORWARD_ONLY);
      final ImmutableList.Builder<T> ret = new ImmutableList.Builder<>();
      int cnt = 0;

      while (results.next()) {
        final Object[] row = results.get();
        for (Object obj : row) {
          ret.add((T) obj);
        }

        if (((++cnt) % NeutronIntegerDefaults.FETCH_SIZE.getValue()) == 0) {
          LOGGER.info("recs read: {}", cnt);
          session.flush(); // Flush every N records
        }
      }

      session.flush();
      results.close();
      txn.commit();
      getFlightLog().markRangeComplete(p);
      return ret.build();
    } catch (HibernateException e) {
      fail();
      LOGGER.error("ERROR PULLING BUCKET RANGE! {}-{}: {}", minId, maxId, e.getMessage(), e);
      txn.rollback();
      throw new DaoException(e);
    }
  }

  protected void awaitBulkProcessorClose(final BulkProcessor bp) {
    try {
      bp.awaitClose(NeutronIntegerDefaults.WAIT_BULK_PROCESSOR.getValue(), TimeUnit.SECONDS);
    } catch (Exception e2) {
      fail();
      throw new JobsException("ERROR CLOSING BULK PROCESSOR!", e2);
    } finally {
      doneRetrieve();
    }
  }

  /**
   * Pull replicated records from named query "findBucketRange".
   * 
   * <p>
   * Thread safety: ElasticsearchDao is thread-safe, but BulkProcessor is <strong>NOT</strong>.
   * Construct one BulkProcessor per thread.
   * </p>
   * 
   * @return number of records processed
   * @throws NeutronException on general error
   * @see #pullBucketRange(String, String)
   */
  protected int extractHibernate() throws NeutronException {
    LOGGER.info("INITIAL LOAD WITH HIBERNATE!");
    final List<Pair<String, String>> buckets = getPartitionRanges();

    for (Pair<String, String> b : buckets) {
      final List<T> results = pullBucketRange(b.getLeft(), b.getRight());

      if (results != null && !results.isEmpty()) {
        final BulkProcessor bp = buildBulkProcessor();
        results.stream().forEach(p -> { // NOSONAR
          prepareDocumentTrapException(bp, p);
        });

        awaitBulkProcessorClose(bp);
      }
    }

    return getFlightLog().getCurrentBulkPrepared();
  }

  @Override
  public BaseDaoImpl<T> getJobDao() {
    return jobDao;
  }

  @Override
  public FlightLog getFlightLog() {
    return flightLog;
  }

  @Override
  public ElasticsearchDao getEsDao() {
    return esDao;
  }

  /**
   * Only used for testing.
   * 
   * @return impl index queue
   */
  protected LinkedBlockingDeque<T> getQueueIndex() {
    return queueIndex;
  }

  /**
   * Only used for testing.
   * 
   * @param queueIndex index queue implementation
   */
  protected void setQueueIndex(LinkedBlockingDeque<T> queueIndex) {
    this.queueIndex = queueIndex;
  }

  /**
   * Only used for testing.
   * 
   * @param track progress tracker
   */
  public void setFlightLog(FlightLog track) {
    this.flightLog = track;
  }

  @Override
  public ObjectMapper getMapper() {
    return mapper;
  }

  @SuppressWarnings("javadoc")
  public void setMapper(ObjectMapper mapper) {
    this.mapper = mapper;
  }

}
