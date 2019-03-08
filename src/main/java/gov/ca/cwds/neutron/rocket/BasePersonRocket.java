package gov.ca.cwds.neutron.rocket;

import static gov.ca.cwds.neutron.enums.NeutronIntegerDefaults.FETCH_SIZE;
import static gov.ca.cwds.neutron.enums.NeutronIntegerDefaults.QUERY_TIMEOUT_IN_SECONDS;

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
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomDocumentSecurity;
import gov.ca.cwds.neutron.atom.AtomInitialLoad;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.atom.AtomPersonDocPrep;
import gov.ca.cwds.neutron.atom.AtomTransform;
import gov.ca.cwds.neutron.atom.AtomValidateDocument;
import gov.ca.cwds.neutron.component.HoverCar;
import gov.ca.cwds.neutron.component.NeutronBulkProcessorBuilder;
import gov.ca.cwds.neutron.enums.NeutronColumn;
import gov.ca.cwds.neutron.enums.NeutronDateTimeFormat;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;
import gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;

/**
 * Base class to index person documents from CMS into ElasticSearch.
 * 
 * <p>
 * This class implements {@link AutoCloseable} and automatically closes common resources, such as
 * {@link ElasticsearchDao} and Hibernate {@link SessionFactory}.
 * </p>
 * 
 * <p>
 * <strong>Auto mode ("smart" mode)</strong> takes the same parameters as last run and determines
 * whether the rocket has never been run. If the last run date is older than 25 years, then then
 * assume that the rocket is populating ElasticSearch for the first time and run all initial batch
 * loads.
 * </p>
 * 
 * <h3>Command Line:</h3>
 * 
 * <pre>
 * {@code java gov.ca.cwds.jobs.ClientIndexerJob -c config/local.yaml -l /Users/voldemort/people_summary.time}
 * </pre>
 * 
 * @author CWDS API Team
 * @param <N> normalized entity, ES replicated Person persistence class
 * @param <D> de-normalized entity, MQT entity class, if any, or type N again
 * @see FlightPlan
 */
public abstract class BasePersonRocket<N extends PersistentObject, D extends ApiGroupNormalizer<?>>
    extends LastFlightRocket implements AutoCloseable, AtomPersonDocPrep<N>, AtomInitialLoad<N, D>,
    AtomTransform<N, D>, AtomDocumentSecurity, AtomValidateDocument {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(BasePersonRocket.class);

  /**
   * Jackson ObjectMapper.
   */
  protected ObjectMapper mapper;

  private transient NeutronBulkProcessorBuilder bulkProcessorBuilder;

  /**
   * Main DAO for the supported persistence class.
   */
  protected transient BaseDaoImpl<N> jobDao;

  /**
   * Elasticsearch client DAO.
   */
  protected transient ElasticsearchDao esDao;

  /**
   * Primary Hibernate session factory. Rockets could potentially read from multiple datasources.
   */
  protected final SessionFactory sessionFactory;

  /**
   * Track this rocket's flight progress.
   */
  protected FlightLog flightLog = new FlightLog();

  protected transient AtomLaunchDirector launchDirector;

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
  protected LinkedBlockingDeque<D> queueNormalize = new LinkedBlockingDeque<>(5000);

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
  protected Queue<N> queueIndex = new ConcurrentLinkedQueue<>();

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param jobDao Person DAO, such as {@link ReplicatedClientDao}
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   * @param launchDirector launch director
   */
  @Inject
  public BasePersonRocket(final BaseDaoImpl<N> jobDao, final ElasticsearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(lastRunFile, flightPlan);
    this.jobDao = jobDao;
    this.esDao = esDao;
    this.mapper = mapper;
    this.sessionFactory = jobDao.getSessionFactory();
    this.bulkProcessorBuilder = new HoverCar(esDao, flightLog);
    this.flightLog.setRocketName(getClass().getSimpleName());
    this.launchDirector = launchDirector;
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
  public void addToIndexQueue(N norm) {
    try {
      CheeseRay.logEvery(flightLog.markQueuedToIndex(), "index queue", "recs");
      queueIndex.add(norm); // unbounded
    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(LOGGER, e, "INTERRUPTED! {}", e.getMessage());
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
  protected void prepareDocument(final BulkProcessor bp, N t) throws IOException {
    // SNAP-820: separate Jackson from ES client to diagnose hang.

    LOGGER.trace("PREP doc id: {}", t.getPrimaryKey());
    final List<?> ready = Arrays.stream(ElasticTransformer.buildElasticSearchPersons(t))
        .map(p -> prepareUpsertRequestNoChecked(p, t)).collect(Collectors.toList());

    LOGGER.trace("SEND doc id: {}", t.getPrimaryKey());
    ready.stream().sequential().forEach(x -> {
      ElasticTransformer.pushToBulkProcessor(flightLog, bp, (DocWriteRequest<?>) x);
    });

    // Arrays.stream(ElasticTransformer.buildElasticSearchPersons(t))
    // .map(p -> prepareUpsertRequestNoChecked(p, t)).forEach(x -> { // NOSONAR
    // ElasticTransformer.pushToBulkProcessor(flightLog, bp, x);
    // });
  }

  /**
   * Prepare an "upsert" request <strong>without a checked exception</strong> and throw a runtime
   * {@link NeutronRuntimeException} on error.
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
  protected DocWriteRequest prepareUpsertRequestNoChecked(ElasticSearchPerson esp, N t) {
    DocWriteRequest<?> ret;
    final FlightLog fl = getFlightLog();
    try {
      if (isDelete(t)) {
        ret = bulkDelete(t.getPrimaryKey().toString());
        fl.incrementBulkDeleted();
      } else {
        ret = prepareUpsertRequest(esp, t);
      }
    } catch (Exception e) {
      fl.addWarning("FAILED TO BUILD UPSERT FOR PK " + t.getPrimaryKey());
      throw CheeseRay.runtime(LOGGER, e, "ERROR BUILDING UPSERT!: PK: {}", t.getPrimaryKey());
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
   * @throws NeutronCheckedException on Elasticsearch disconnect
   * @see ElasticTransformer#prepareUpsertRequest(AtomPersonDocPrep, String, String,
   *      ElasticSearchPerson, PersistentObject)
   */
  protected UpdateRequest prepareUpsertRequest(ElasticSearchPerson esp, N t)
      throws NeutronCheckedException {
    LOGGER.trace("prep upsert: id: {}", t.getPrimaryKey());
    if (StringUtils.isNotBlank(getLegacySourceTable())) {
      esp.setLegacySourceTable(getLegacySourceTable());
    }

    return ElasticTransformer.<N>prepareUpsertRequest(this, getFlightPlan().getIndexName(),
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
   * <strong>ENTRY POINT FOR INITIAL LOAD</strong>
   * 
   * <p>
   * Run threads to extract, transform, and index.
   * </p>
   * 
   * @throws NeutronCheckedException bombed
   */
  @SuppressWarnings("unchecked")
  protected void doInitialLoadJdbc() throws NeutronCheckedException {
    final List<Thread> threads = new ArrayList<>();

    try {
      LOGGER.info("INITIAL LOAD WITH JDBC!");
      nameThread("initial_load");
      addThread(true, this::threadIndex, threads);
      addThread(useTransformThread(), this::threadNormalize, threads);
      addThread(true, this::threadRetrieveByJdbc, threads);

      // Start threads.
      for (Thread t : threads) {
        t.start();
      }

      // Wait for threads to finish.
      for (Thread t : threads) {
        t.join();
      }

      // WARN: threading practices. Prefer condition check or timed lock.
      Thread.sleep(NeutronIntegerDefaults.SLEEP_MILLIS.getValue());
      LOGGER.info("PROGRESS TRACK: {}", () -> this.getFlightLog().toString());
    } catch (Exception e) {
      fail();
      throw CheeseRay.checked(LOGGER, e, "JDBC EXCEPTION: {}", e);
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

    // Close the connection automatically.
    Connection con = null;
    try (final Session session = jobDao.grabSession()) {
      con = NeutronJdbcUtils.prepConnection(session);

      // Linux MQT lacks ORDER BY clause. Must sort manually.
      // Either detect platform or force ORDER BY clause.
      final String query = getInitialLoadQuery(getDBSchemaName());
      LOGGER.info("query: {}", query);

      // Enable parallelism for underlying database.
      NeutronJdbcUtils.enableBatchSettings(con);

      D m;
      try (final Statement stmt = con.createStatement()) {
        stmt.setFetchSize(FETCH_SIZE.getValue()); // faster
        stmt.setMaxRows(0);
        stmt.setQueryTimeout(QUERY_TIMEOUT_IN_SECONDS.getValue());

        // SNAP-709: Connection is closed. ERRORCODE=-4470, SQLSTATE=08003.
        int cntr = 0;
        try (final ResultSet rs = stmt.executeQuery(query)) {
          while (isRunning() && rs.next() && (m = extract(rs)) != null) {
            CheeseRay.logEvery(++cntr, "Retrieved", "recs");
            final boolean addedToQueue = queueNormalize.offer(m,
                NeutronIntegerDefaults.POLL_MILLIS.getValue(), TimeUnit.MILLISECONDS);
            LOGGER.trace("addedToQueue: {}", addedToQueue);
          }
        } finally {
          // Automatically close the result set.
        }

        con.commit();
      }
    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(LOGGER, e, "BATCH ERROR! {}", e.getMessage());
    } finally {
      doneRetrieve();
      LOGGER.info("DONE: jdbc thread");
    }
  }

  protected int normalizeLoop(final List<D> grpRecs, Object theLastId, int inCntr)
      throws InterruptedException {
    D m;
    N t;
    Object lastId = theLastId;
    int cntr = inCntr;
    ++cntr;

    while ((m = queueNormalize.pollFirst(NeutronIntegerDefaults.POLL_MILLIS.getValue(),
        TimeUnit.MILLISECONDS)) != null) {
      CheeseRay.logEvery(++cntr, "Transformed", "recs");

      // NOTE: Assumes that records are sorted by group key.
      // End of group. Normalize these group records.
      if (!lastId.equals(m.getNormalizationGroupKey()) && cntr > 1
          && (t = normalizeSingle(grpRecs)) != null) {

        // Unbounded:
        queueIndex.add(t);
        grpRecs.clear(); // Single thread, re-use memory.
      }

      grpRecs.add(m);
      lastId = m.getNormalizationGroupKey();
    }

    // Last bundle.
    if (!grpRecs.isEmpty() && (t = normalizeSingle(grpRecs)) != null) {
      queueIndex.add(t); // unbounded
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
    final List<D> grpRecs = new ArrayList<>();

    try {
      while (isRunning() && !(isRetrieveDone() && queueNormalize.isEmpty())) {
        cntr = normalizeLoop(grpRecs, lastId, cntr);
      }
    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(LOGGER, e, "TRANSFORMER: FATAL ERROR: {}", e.getMessage());
    } finally {
      doneTransform();
    }

    LOGGER.info("DONE: normalize thread");
  }

  /**
   * The "load" part of ETL. Read from the normalized record queue and index into ES.
   */
  protected void threadIndex() {
    nameThread("indexer");
    LOGGER.info("BEGIN: indexer thread");
    final BulkProcessor bp = buildBulkProcessor();
    int cntr = 0;

    try {
      while (!(!isRunning() || (isRetrieveDone() && isTransformDone() && queueIndex.isEmpty()))) {
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
      throw CheeseRay.runtime(LOGGER, e, "FATAL INDEXING ERROR: {}", e.getMessage());
    } finally {
      doneIndex();
    }

    LOGGER.info("STOP indexer thread");
  }

  /**
   * Super lame but sometimes effective approach to thread management, especially when
   * thread/connection pools warm up or other resources initialize.
   * 
   * <p>
   * More efficient/elegant to use {@link CyclicBarrier}, {@link CountDownLatch}, {@link Phaser}, or
   * even a raw Condition.
   * </p>
   * 
   * @throws InterruptedException on thread interruption
   */
  protected void waitOnQueue() throws InterruptedException {
    if (isRunning()) {
      final int sleepForMillis = NeutronIntegerDefaults.SLEEP_MILLIS.getValue();
      LOGGER.trace("thread {}: bulkPrepare: queue empty, waiting for data: sleep for {} millis",
          Thread.currentThread().getName(), sleepForMillis);
      Thread.sleep(sleepForMillis);
    }
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
    LOGGER.trace("Indexer thread: bulkPrepare");
    int i = cntr;
    N t; // Normalized type

    while (isRunning() && (t = queueIndex.poll()) != null) {
      CheeseRay.logEvery(++i, "Indexed", "recs to ES");
      prepareDocument(bp, t);
    }

    waitOnQueue();
    return i;
  }

  /**
   * Prepare a document and trap IOException.
   * 
   * @param bp bulk processor
   * @param p ApiPersonAware object
   */
  protected void prepareDocumentTrapException(BulkProcessor bp, N p) {
    try {
      prepareDocument(bp, p);
    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(LOGGER, e, "ERROR PREPARING DOCUMENT! {}", e.getMessage());
    }
  }

  /**
   * If not running in sealed/sensitive mode, forcibly remove sealed or sensitive documents.
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

  public List<N> fetchLastRunResults(final Date lastRunDate, final Set<String> deletionResults) {
    return isViewNormalizer() ? extractLastRunRecsFromView(lastRunDate, deletionResults)
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
   * @throws NeutronCheckedException oops!
   * @see gov.ca.cwds.neutron.rocket.LastFlightRocket#launch(java.util.Date)
   */
  protected Date doLastRun(Date lastRunDt) throws NeutronCheckedException {
    LOGGER.info("LAST RUN MODE!");
    final FlightLog fl = getFlightLog();

    try {
      final BulkProcessor bp = buildBulkProcessor();
      final Set<String> deletionResults = new HashSet<>();
      final List<N> results = fetchLastRunResults(lastRunDt, deletionResults);

      if (results != null && !results.isEmpty()) {
        LOGGER.info("Found {} persons to index", results.size());
        final NeutronCounter cntr1 = new NeutronCounter();
        final NeutronCounter cntr2 = new NeutronCounter();
        final int nLogEvery = flightPlan.isLastRunMode() ? 50 : 2000;

        // SNAP-820: People Summary job stalls here under CPU load or ES load.
        results.stream().sequential().forEach(p -> {
          final String id = p.getPrimaryKey().toString();
          CheeseRay.logEvery(LOGGER, nLogEvery, cntr1.incrementAndGet(), "track doc", "prep", id);
          fl.addAffectedDocumentId(id);

          CheeseRay.logEvery(LOGGER, nLogEvery, cntr2.incrementAndGet(), "prep doc", "prep", id);
          prepareDocumentTrapException(bp, p);
        });

        // SNAP-820: People Summary job never reaches this line after stall.
        LOGGER.info("Indexed {} persons", results.size());
      } else {
        LOGGER.info("NO PERSON CHANGES FOUND");
      }

      LOGGER.debug("Delete restricted, if any");
      deleteRestricted(deletionResults, bp); // last run only

      LOGGER.debug("Awaiting bulk processor ...");
      awaitBulkProcessorClose(bp);
      LOGGER.debug("Bulk processor done");

      LOGGER.debug("Validate documents");
      validateDocuments();
      LOGGER.debug("Validated documents");

      return new Date(fl.getStartTime());
    } catch (Exception e) {
      fail();
      throw CheeseRay.checked(LOGGER, e, "GENERAL EXCEPTION: {}", e.getMessage());
    } finally {
      done();
    }
  }

  /**
   * Configure queue sizes for last run or initial load.
   * 
   * @param lastRun last successful run time
   */
  protected void sizeQueues(final Date lastRun) {
    // Default implementation is no-op.
  }

  protected boolean determineInitialLoad(final Date lastRun) {
    return getFlightPlan().determineInitialLoad(lastRun);
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
   * Lambda runs a number of threads up to max processor cores. Queued jobs wait until a worker
   * thread is available.
   * 
   * <p>
   * Auto mode ("smart" mode) takes the same parameters as last run and determines whether the
   * rocket has never been run. If the last run date is older than 50 years, then then assume that
   * the rocket is populating ElasticSearch for the first time and run all initial batch loads.
   * </p>
   * 
   * {@inheritDoc}
   * 
   * @see gov.ca.cwds.neutron.rocket.LastFlightRocket#launch(java.util.Date)
   */
  @Override
  public Date launch(Date lastSuccessfulRunTime) throws NeutronCheckedException {
    LOGGER.info("LAUNCH ROCKET! {}", getClass().getName());
    Date ret;

    try {
      determineIndexName();
      final Date lastRun = calcLastRunDate(lastSuccessfulRunTime);
      LOGGER.info("last run date/time: {}", lastRun);

      sizeQueues(lastRun);
      if (determineInitialLoad(lastRun)) {
        // Initial mode:
        flightLog.setInitialLoad(true);
        if (isInitialLoadJdbc()) {
          doInitialLoadJdbc();
        } else {
          extractHibernate();
        }
      } else {
        // Last run mode:
        // INT-1723: Neutron to create Elasticsearch Alias for people-summary index
        // If index name is provided, use it, else take alias from ES config.
        final FlightPlan fp = getFlightPlan();
        final String indexNameOverride = fp.getIndexName();
        final String effectiveIndexName =
            StringUtils.isBlank(indexNameOverride) ? esDao.getConfig().getElasticsearchAlias()
                : indexNameOverride;
        fp.setIndexName(effectiveIndexName);
        doLastRun(lastRun);
      }

      // CHECKSTYLE:OFF
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Updating last successful run time to {}",
            new SimpleDateFormat(NeutronDateTimeFormat.FMT_LAST_RUN_DATE.getFormat())
                .format(flightLog.getStartTime()));
      }
      // CHECKSTYLE:ON
      ret = new Date(flightLog.getStartTime());
    } catch (Exception e) {
      fail();
      throw CheeseRay.checked(LOGGER, e, "ROCKET EXPLODED! {}", e.getMessage());
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
   * If this rocket defines a denormalized view entity, then pull from that. Otherwise, pull from
   * the table entity.
   * </p>
   * 
   * @param lastRunTime last successful run date/time
   * @return List of normalized entities
   */
  @SuppressWarnings("unchecked")
  protected List<N> extractLastRunRecsFromTable(final Date lastRunTime) {
    LOGGER.info("LAST SUCCESSFUL RUN: {}", lastRunTime);
    final Class<?> entityClass = jobDao.getEntityClass();
    final String namedQueryName = entityClass.getName() + ".findAllUpdatedAfter";
    final Session session = jobDao.getSessionFactory().getCurrentSession();
    final Transaction txn = grabTransaction(); // Cheesy hack.

    try {
      final NativeQuery<N> q = session.getNamedNativeQuery(namedQueryName);
      NeutronJdbcUtils.readOnlyQuery(q);
      q.setParameter(NeutronColumn.SQL_COLUMN_AFTER.getValue(),
          NeutronDateUtils.makeTimestampStringLookBack(lastRunTime), StringType.INSTANCE);

      final ImmutableList.Builder<N> results = new ImmutableList.Builder<>();
      final List<N> recs = q.list();

      LOGGER.info("FOUND {} RECORDS", recs.size());
      results.addAll(recs);
      session.clear();
      txn.commit();
      return results.build();
    } catch (HibernateException h) {
      fail();
      LOGGER.error("EXTRACT ERROR! {}", h.getMessage(), h);
      if (txn != null && txn.getStatus().canRollback()) {
        txn.rollback();
      }
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
    final NativeQuery<D> q = session.getNamedNativeQuery(namedQueryNameForDeletion);
    NeutronJdbcUtils.readOnlyQuery(q);
    q.setParameter(NeutronColumn.SQL_COLUMN_AFTER.getValue(),
        NeutronDateUtils.makeTimestampStringLookBack(lastRunTime), StringType.INSTANCE);

    final List<D> deletionRecs = q.list();
    if (deletionRecs != null && !deletionRecs.isEmpty()) {
      for (D rec : deletionRecs) {
        // Assuming group key represents ID of client to delete.
        // True for client, referral history, case history jobs.
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
   * Pull from view for last run mode with Hibernate, normalize, and delete sensitive records
   * (optional).
   * 
   * @param lastRunTime last successful run time
   * @param deletionResults records to remove
   * @return List of normalized entities
   */
  @SuppressWarnings("unchecked")
  protected List<N> extractLastRunRecsFromView(final Date lastRunTime,
      final Set<String> deletionResults) {
    LOGGER.info("PULL VIEW: last successful run: {}", lastRunTime);
    final Class<?> entityClass = getDenormalizedClass(); // view entity class
    final String namedQueryName =
        flightPlan.isLoadSealedAndSensitive() ? entityClass.getName() + ".findAllUpdatedAfter"
            : entityClass.getName() + ".findAllUpdatedAfterWithUnlimitedAccess";

    Transaction txn = null;

    try (final Session session = jobDao.grabSession()) {
      txn = grabTransaction();
      // Insert into session temp table that drives a last change view.
      runInsertAllLastChangeKeys(session, lastRunTime, getPrepLastChangeSQLs());
      final NativeQuery<D> q = session.getNamedNativeQuery(namedQueryName);
      NeutronJdbcUtils.readOnlyQuery(q);
      q.setParameter(NeutronColumn.SQL_COLUMN_AFTER.getValue(),
          NeutronDateUtils.makeTimestampStringLookBack(lastRunTime), StringType.INSTANCE);

      // Iterate, process, flush.
      List<D> recs = new ArrayList<>();
      try {
        recs = q.list();
        LOGGER.info("FOUND {} RECORDS", recs.size());
        session.flush();
        session.clear();
        txn.commit();
      } catch (Exception h) {
        fail();
        if (txn.getStatus().canRollback()) {
          try {
            txn.rollback();
          } catch (Exception e2) {
            LOGGER.error("NESTED EXCEPTION", e2);
          }
        }
        throw CheeseRay.runtime(LOGGER, h, "EXTRACT SQL ERROR!: {}", h.getMessage());
      }

      // Release database resources. Don't hold on to the connection or transaction.
      LOGGER.info("PULL VIEW: DATA RETRIEVAL DONE");
      Object lastId = new Object();
      final List<N> results = new ArrayList<>(recs.size()); // Size appropriately

      // Convert denormalized rows to normalized persistence objects.
      final List<D> groupRecs = new ArrayList<>(50);
      for (D m : recs) {
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
  public void close() throws IOException {
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

      catchYourBreath(); // a lock would be better
    }
  }

  @Override
  protected void finish() throws NeutronCheckedException {
    final String rocketName = this.getClass().getName();
    LOGGER.info("FINISH FLIGHT! {}", rocketName);
    try {
      done();
      close();
    } catch (Exception e) {
      fail();
      throw CheeseRay.checked(LOGGER, e, "ERROR LANDING ROCKET! {}, {}", rocketName,
          e.getMessage());
    }
    LOGGER.info("FLIGHT FINISHED!");
  }

  /**
   * Divide work into buckets: pull a unique range of identifiers so that no bucket results overlap.
   * 
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
  protected List<N> pullBucketRange(String minId, String maxId) {
    LOGGER.info("PULL BUCKET RANGE {} to {}", minId, maxId);
    final Pair<String, String> p = Pair.of(minId, maxId);
    getFlightLog().markRangeStart(p);

    final Class<?> entityClass =
        getDenormalizedClass() != null ? getDenormalizedClass() : getJobDao().getEntityClass();
    final String namedQueryName = entityClass.getName() + ".findBucketRange";
    final Session session = jobDao.getSessionFactory().getCurrentSession();
    final Transaction txn = grabTransaction();

    try {
      session.clear();
      session.setCacheMode(CacheMode.IGNORE);
      session.setDefaultReadOnly(true);
      session.setFlushMode(FlushModeType.COMMIT);

      final NativeQuery<N> q = session.getNamedNativeQuery(namedQueryName);
      NeutronJdbcUtils.readOnlyQuery(q);
      q.setParameter("min_id", minId, StringType.INSTANCE)
          .setParameter("max_id", maxId, StringType.INSTANCE)
          .setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());

      // No reduction/normalization. Iterate, process, flush.
      final ScrollableResults results = q.scroll(ScrollMode.FORWARD_ONLY);
      final ImmutableList.Builder<N> ret = new ImmutableList.Builder<>();
      int cnt = 0;

      while (results.next()) {
        final Object[] row = results.get();
        for (Object obj : row) {
          ret.add((N) obj);
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
      if (txn.getStatus().canRollback()) {
        try {
          txn.rollback();
        } catch (Exception e2) {
          LOGGER.error("NESTED EXCEPTION", e2);
        }
      }
      throw new DaoException(e);
    }
  }

  protected void awaitBulkProcessorClose(final BulkProcessor bp) {
    try {
      bp.awaitClose(NeutronIntegerDefaults.WAIT_BULK_PROCESSOR.getValue(), TimeUnit.SECONDS);
    } catch (Exception e2) {
      fail();
      throw new NeutronRuntimeException("ERROR CLOSING BULK PROCESSOR!", e2);
    } finally {
      doneIndex();
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
   * @throws NeutronCheckedException on general error
   * @see #pullBucketRange(String, String)
   */
  protected int extractHibernate() throws NeutronCheckedException {
    LOGGER.info("INITIAL LOAD WITH HIBERNATE!");
    final List<Pair<String, String>> buckets = getPartitionRanges();

    for (Pair<String, String> b : buckets) {
      final List<N> results = pullBucketRange(b.getLeft(), b.getRight());

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
  public BaseDaoImpl<N> getJobDao() {
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
   * Used for testing.
   * 
   * @return index queue implementation
   */
  protected Queue<N> getQueueIndex() {
    return queueIndex;
  }

  /**
   * Used for testing.
   * 
   * @param queueIndex index queue implementation
   */
  protected void setQueueIndex(ConcurrentLinkedDeque<N> queueIndex) {
    this.queueIndex = queueIndex;
  }

  /**
   * Mostly used for testing.
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

  public void setMapper(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public AtomLaunchDirector getLaunchDirector() {
    return launchDirector;
  }

}
