package gov.ca.cwds.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.update.UpdateRequest;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ibm.db2.jcc.DB2SystemMonitor;

import gov.ca.cwds.dao.cms.ReplicatedPersonReferralsDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.EsPersonReferral;
import gov.ca.cwds.data.persistence.cms.ReplicatedPersonReferrals;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomRowMapper;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.neutron.rocket.referral.MinClientReferral;
import gov.ca.cwds.neutron.rocket.referral.ReferralJobRanges;
import gov.ca.cwds.neutron.util.NeutronThreadUtils;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.util.transform.EntityNormalizer;

/**
 * Rocket to index person referrals from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class ReferralHistoryIndexerJob
    extends BasePersonRocket<ReplicatedPersonReferrals, EsPersonReferral>
    implements AtomRowMapper<EsPersonReferral> {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ReferralHistoryIndexerJob.class);

  //@formatter:off
  protected static final String INSERT_CLIENT_FULL =
      "INSERT INTO GT_REFR_CLT (FKREFERL_T, FKCLIENT_T, SENSTV_IND)"
          + "\nSELECT rc.FKREFERL_T, rc.FKCLIENT_T, c.SENSTV_IND"
          + "\nFROM REFR_CLT rc"
          + "\nJOIN CLIENT_T c on c.IDENTIFIER = rc.FKCLIENT_T"
          + "\nWHERE rc.FKCLIENT_T BETWEEN ? AND ?"
          + "\n  AND  c.IBMSNAP_OPERATION IN ('I','U') "; // don't update a deleted Client document
  //@formatter:on

  /**
   * Filter <strong>deleted</strong> Client, Referral, Referral/Client, and Allegation.
   */
  //@formatter:off
  protected static final String INSERT_REFERRAL_LAST_CHG = 
      "INSERT INTO GT_ID (IDENTIFIER)\n"
      + " WITH step1 AS (\n"
      + "     SELECT ALG.FKREFERL_T AS REFERRAL_ID\n"
      + "     FROM ALLGTN_T ALG \n"
      + "     WHERE ALG.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
      + " ), \n"
      + " step2 AS (\n"
      + "     SELECT ALG.FKREFERL_T AS REFERRAL_ID \n"
      + "     FROM CLIENT_T C \n"
      + "     JOIN ALLGTN_T ALG ON (C.IDENTIFIER = ALG.FKCLIENT_0 OR C.IDENTIFIER = ALG.FKCLIENT_T)\n"
      + "     WHERE C.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
      + " ),\n"
      + " step3 AS (\n"
      + "     SELECT RCT.FKREFERL_T AS REFERRAL_ID \n"
      + "     FROM REFR_CLT RCT \n"
      + "     WHERE RCT.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
      + " ), \n"
      + " step4 AS (\n"
      + "     SELECT RFL.IDENTIFIER AS REFERRAL_ID \n"
      + "     FROM REFERL_T RFL \n"
      + "     WHERE RFL.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
      + " ), \n"
      + " step5 AS (\n"
      + "     SELECT RPT.FKREFERL_T AS REFERRAL_ID \n"
      + "     FROM REPTR_T RPT \n"
      + "     WHERE RPT.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
      + " ), \n"
      + " hoard AS (\n"
      + "           SELECT DISTINCT s1.REFERRAL_ID FROM STEP1 s1 \n"
      + "     UNION SELECT DISTINCT s2.REFERRAL_ID FROM STEP2 s2 \n"
      + "     UNION SELECT DISTINCT s3.REFERRAL_ID FROM STEP3 s3 \n"
      + "     UNION SELECT DISTINCT s4.REFERRAL_ID FROM STEP4 s4 \n"
      + "     UNION SELECT DISTINCT s5.REFERRAL_ID FROM STEP5 s5 \n"
      + " ) \n"
      + " SELECT DISTINCT rc2.FKREFERL_T \n"
      + " FROM hoard h \n"
      + " JOIN REFR_CLT rc1 ON rc1.FKREFERL_T = h.REFERRAL_ID \n"
      + " JOIN REFR_CLT rc2 ON rc2.FKCLIENT_T = rc1.FKCLIENT_T ";
  //@formatter:on

  //@formatter:off
  protected static final String SELECT_CLIENT =
        "SELECT rc.FKCLIENT_T, rc.FKREFERL_T, rc.SENSTV_IND, "
      + "c.IBMSNAP_OPERATION AS CLT_IBMSNAP_OPERATION \n" 
      + "FROM GT_REFR_CLT RC \n"
      + "JOIN CLIENT_T C ON C.IDENTIFIER = RC.FKCLIENT_T \n"
      + "WITH UR ";
  //@formatter:on

  //@formatter:off
  protected static final String SELECT_ALLEGATION = 
        "SELECT \n"
      + " RC.FKREFERL_T         AS REFERRAL_ID,\n" 
      + " ALG.IDENTIFIER        AS ALLEGATION_ID,\n"
      + " ALG.ALG_DSPC          AS ALLEGATION_DISPOSITION,\n"
      + " ALG.ALG_TPC           AS ALLEGATION_TYPE,\n"
      + " ALG.LST_UPD_TS        AS ALLEGATION_LAST_UPDATED,\n"
      + " CLP.IDENTIFIER        AS PERPETRATOR_ID,\n"
      + " CLP.SENSTV_IND        AS PERPETRATOR_SENSITIVITY_IND,\n"
      + " TRIM(CLP.COM_FST_NM)  AS PERPETRATOR_FIRST_NM,\n"
      + " TRIM(CLP.COM_LST_NM)  AS PERPETRATOR_LAST_NM,\n"
      + " CLP.LST_UPD_TS        AS PERPETRATOR_LAST_UPDATED,\n"
      + " CLV.IDENTIFIER        AS VICTIM_ID,\n"
      + " CLV.SENSTV_IND        AS VICTIM_SENSITIVITY_IND,\n"
      + " TRIM(CLV.COM_FST_NM)  AS VICTIM_FIRST_NM,\n"
      + " TRIM(CLV.COM_LST_NM)  AS VICTIM_LAST_NM,\n"
      + " CLV.LST_UPD_TS        AS VICTIM_LAST_UPDATED,\n" 
      + " CURRENT TIMESTAMP     AS LAST_CHG, \n"
      + " ALG.IBMSNAP_OPERATION AS ALG_IBMSNAP_OPERATION \n"
      + "FROM (SELECT DISTINCT rc1.FKREFERL_T FROM GT_REFR_CLT rc1) RC \n"
      + "JOIN ALLGTN_T       ALG  ON ALG.FKREFERL_T = RC.FKREFERL_T \n"
      + "JOIN CLIENT_T       CLV  ON CLV.IDENTIFIER = ALG.FKCLIENT_T \n"
      + "LEFT JOIN CLIENT_T  CLP  ON CLP.IDENTIFIER = ALG.FKCLIENT_0 \n"
      + "WITH UR ";
  //@formatter:on

  //@formatter:off
  protected static final String SELECT_REFERRAL = 
      "SELECT "
      + " RFL.IDENTIFIER        AS REFERRAL_ID,\n"
      + " RFL.REF_RCV_DT        AS START_DATE,\n" 
      + " RFL.REFCLSR_DT        AS END_DATE,\n"
      + " RFL.RFR_RSPC          AS REFERRAL_RESPONSE_TYPE,\n"
      + " RFL.LMT_ACSSCD        AS LIMITED_ACCESS_CODE,\n"
      + " RFL.LMT_ACS_DT        AS LIMITED_ACCESS_DATE,\n"
      + " TRIM(RFL.LMT_ACSDSC)  AS LIMITED_ACCESS_DESCRIPTION,\n"
      + " RFL.L_GVR_ENTC        AS LIMITED_ACCESS_GOVERNMENT_ENT,\n"
      + " RFL.LST_UPD_TS        AS REFERRAL_LAST_UPDATED,\n"
      + " TRIM(RPT.FKREFERL_T)  AS REPORTER_ID,\n"
      + " TRIM(RPT.RPTR_FSTNM)  AS REPORTER_FIRST_NM,\n"
      + " TRIM(RPT.RPTR_LSTNM)  AS REPORTER_LAST_NM,\n"
      + " RPT.LST_UPD_TS        AS REPORTER_LAST_UPDATED,\n"
      + " STP.IDENTIFIER        AS WORKER_ID,\n" 
      + " TRIM(STP.FIRST_NM)    AS WORKER_FIRST_NM,\n"
      + " TRIM(STP.LAST_NM)     AS WORKER_LAST_NM,\n"
      + " STP.LST_UPD_TS        AS WORKER_LAST_UPDATED,\n"
      + " RFL.GVR_ENTC          AS REFERRAL_COUNTY,\n" 
      + " CURRENT TIMESTAMP     AS LAST_CHG, \n"
      + " RFL.IBMSNAP_OPERATION AS RFL_IBMSNAP_OPERATION, \n"
      + " RPT.IBMSNAP_OPERATION AS RPT_IBMSNAP_OPERATION \n"
      + "FROM (SELECT DISTINCT rc1.FKREFERL_T FROM GT_REFR_CLT rc1) RC \n"
      + "JOIN REFERL_T          RFL  ON RFL.IDENTIFIER = RC.FKREFERL_T \n"
      + "LEFT JOIN REPTR_T      RPT  ON RPT.FKREFERL_T = RFL.IDENTIFIER \n"
      + "LEFT JOIN STFPERST     STP  ON RFL.FKSTFPERST = STP.IDENTIFIER ";
   //@formatter:on

  /**
   * Allocate memory once for each thread and reuse per key range.
   * 
   * <p>
   * Use thread local variables <strong>sparingly</strong> because they stick to the thread. This
   * Neutron rocket reuses threads for performance, since thread creation is expensive.
   * </p>
   */
  protected transient ThreadLocal<List<EsPersonReferral>> allocAllegations = new ThreadLocal<>();

  protected transient ThreadLocal<Map<String, EsPersonReferral>> allocReferrals =
      new ThreadLocal<>();

  protected transient ThreadLocal<List<MinClientReferral>> allocClientReferralKeys =
      new ThreadLocal<>();

  protected transient ThreadLocal<List<EsPersonReferral>> allocReadyToNorm = new ThreadLocal<>();

  protected final AtomicInteger rowsReadReferrals = new AtomicInteger(0);

  protected final AtomicInteger rowsReadAllegations = new AtomicInteger(0);

  protected final AtomicInteger nextThreadNum = new AtomicInteger(0);

  private boolean monitorDb2;

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao DAO for {@link ReplicatedPersonReferrals}
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   */
  @Inject
  public ReferralHistoryIndexerJob(ReplicatedPersonReferralsDao dao,
      @Named("elasticsearch.dao.people") ElasticsearchDao esDao, @LastRunFile String lastRunFile,
      ObjectMapper mapper, FlightPlan flightPlan) {
    super(dao, esDao, lastRunFile, mapper, flightPlan);
  }

  @Override
  public Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    EsPersonReferral.setOpts(getFlightPlan()); // WARNING: change for continuous mode
    return EsPersonReferral.class;
  }

  @Override
  public String getInitialLoadViewName() {
    return "VW_MQT_REFRL_ONLY";
  }

  @Override
  public String getJdbcOrderBy() {
    return ""; // sort manually since DB2 might not optimize the sort.
  }

  /**
   * Roll your own SQL.
   */
  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    final StringBuilder buf = new StringBuilder();
    buf.append(SELECT_REFERRAL);

    if (!getFlightPlan().isLoadSealedAndSensitive()) {
      buf.append(" WHERE RFL.LMT_ACSSCD = 'N' ");
    }

    buf.append(getJdbcOrderBy()).append(" FOR READ ONLY WITH UR ");
    final String ret = buf.toString().replaceAll("\\s+", " ").trim();
    LOGGER.info("REFERRAL SQL: {}", ret);
    return ret;
  }

  /**
   * Synchronize grabbing connections from the connection pool to prevent deadlocks in C3P0.
   * 
   * @return a connection
   * @throws SQLException on database error
   */
  protected synchronized Connection getConnection() throws SQLException {
    return jobDao.getSessionFactory().getSessionFactoryOptions().getServiceRegistry()
        .getService(ConnectionProvider.class).getConnection();
  }

  /**
   * Initial mode only. Allocate memory once per thread and reuse it.
   * 
   * <p>
   * NEXT: calculate container sizes by bundle size.
   * </p>
   */
  protected void allocateThreadMemory() {
    if (allocAllegations.get() == null) {
      allocAllegations.set(new ArrayList<>(150000));
      allocReadyToNorm.set(new ArrayList<>(150000));
      allocReferrals.set(new HashMap<>(99881)); // Prime
      allocClientReferralKeys.set(new ArrayList<>(12000));
    }
  }

  protected void readClients(final PreparedStatement stmtInsClient,
      final PreparedStatement stmtSelClient, final List<MinClientReferral> listClientReferralKeys,
      final Pair<String, String> p) throws SQLException {
    // Prepare client list.
    stmtInsClient.setMaxRows(0);
    stmtInsClient.setQueryTimeout(0);
    stmtInsClient.setString(1, p.getLeft());
    stmtInsClient.setString(2, p.getRight());

    final int cntInsClientReferral = stmtInsClient.executeUpdate();
    LOGGER.info("bundle client/referrals: {}", cntInsClientReferral);

    // Prepare retrieval.
    stmtSelClient.setMaxRows(0);
    stmtSelClient.setQueryTimeout(0);
    stmtSelClient.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());

    LOGGER.info("pull client referral keys");
    final ResultSet rs = stmtSelClient.executeQuery(); // NOSONAR
    while (!isFailed() && rs.next()) {
      listClientReferralKeys.add(MinClientReferral.extract(rs));
    }
  }

  protected void readReferrals(final PreparedStatement stmtSelReferral,
      final Map<String, EsPersonReferral> mapReferrals) throws SQLException {
    stmtSelReferral.setMaxRows(0);
    stmtSelReferral.setQueryTimeout(0);
    stmtSelReferral.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());

    int cntr = 0;
    EsPersonReferral m;
    LOGGER.info("pull referrals");
    final ResultSet rs = stmtSelReferral.executeQuery(); // NOSONAR
    while (!isFailed() && rs.next() && (m = EsPersonReferral.extractReferral(rs)) != null) {
      CheeseRay.logEvery(++cntr, "read", "bundle referral");
      CheeseRay.logEvery(LOGGER, 10000, rowsReadReferrals.incrementAndGet(), "Total read",
          "referrals");
      if (m.getReferralClientReplicationOperation() != CmsReplicationOperation.D) {
        mapReferrals.put(m.getReferralId(), m);
      }
    }
  }

  protected void readAllegations(final PreparedStatement stmtSelAllegation,
      final List<EsPersonReferral> listAllegations) throws SQLException {
    stmtSelAllegation.setMaxRows(0);
    stmtSelAllegation.setQueryTimeout(0);
    stmtSelAllegation.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());

    int cntr = 0;
    EsPersonReferral m;
    LOGGER.info("pull allegations");
    final ResultSet rs = stmtSelAllegation.executeQuery(); // NOSONAR
    while (!isFailed() && rs.next() && (m = EsPersonReferral.extractAllegation(rs)) != null) {
      CheeseRay.logEvery(++cntr, "read", "bundle allegation");
      CheeseRay.logEvery(LOGGER, 15000, rowsReadAllegations.incrementAndGet(), "Total read",
          "allegations");
      listAllegations.add(m);
    }
  }

  @Override
  public List<ReplicatedPersonReferrals> normalize(List<EsPersonReferral> recs) {
    return EntityNormalizer.<ReplicatedPersonReferrals, EsPersonReferral>normalizeList(recs);
  }

  protected int normalizeClientReferrals(int cntr, MinClientReferral rc1, final String clientId,
      final Map<String, EsPersonReferral> mapReferrals,
      final List<EsPersonReferral> listReadyToNorm,
      final Map<String, List<EsPersonReferral>> mapAllegationByReferral) {
    int ret = cntr;
    final String referralId = rc1.getReferralId();
    final EsPersonReferral denormReferral = mapReferrals.get(referralId);
    final boolean goodToGo = denormReferral != null
        && denormReferral.getReferralReplicationOperation() != CmsReplicationOperation.D;

    // Sealed and sensitive may be excluded.
    if (goodToGo) {
      // Loop allegations for this referral:
      if (mapAllegationByReferral.containsKey(referralId)) {
        for (EsPersonReferral alg : mapAllegationByReferral.get(referralId)) {
          alg.mergeClientReferralInfo(clientId, denormReferral);
          listReadyToNorm.add(alg);
        }
      } else {
        listReadyToNorm.add(denormReferral);
      }
    }

    // #152932457: Overwrite deleted referrals.
    final ReplicatedPersonReferrals repl =
        goodToGo ? normalizeSingle(listReadyToNorm) : new ReplicatedPersonReferrals(clientId);
    ++ret;
    repl.setClientId(clientId);
    addToIndexQueue(repl);

    return ret;
  }

  protected int normalizeQueryResults(final Map<String, EsPersonReferral> mapReferrals,
      final List<EsPersonReferral> listReadyToNorm,
      final Map<String, List<MinClientReferral>> mapReferralByClient,
      final Map<String, List<EsPersonReferral>> mapAllegationByReferral) {
    LOGGER.debug("Normalize all: START");
    int countNormalized = 0;

    for (Map.Entry<String, List<MinClientReferral>> rc : mapReferralByClient.entrySet()) {
      final String clientId = rc.getKey();
      // Loop referrals for this client only.
      if (StringUtils.isNotBlank(clientId)) {
        listReadyToNorm.clear(); // next client id
        for (MinClientReferral rc1 : rc.getValue()) {
          countNormalized = normalizeClientReferrals(countNormalized, rc1, clientId, mapReferrals,
              listReadyToNorm, mapAllegationByReferral);
        }
      }
    }

    LOGGER.debug("Normalize all: END");
    return countNormalized;
  }

  protected DB2SystemMonitor buildMonitor(final Connection con) {
    DB2SystemMonitor ret = null;
    if (monitorDb2) {
      ret = NeutronDB2Utils.monitorStart(con);
    }
    return ret;
  }

  protected void monitorStopAndReport(DB2SystemMonitor monitor) throws SQLException {
    if (monitor != null) {
      NeutronDB2Utils.monitorStopAndReport(monitor);
    }
  }

  /**
   * Release heap objects early and often. Good time to <strong>request</strong> garbage collection,
   * not demand it. Java GC runs in a dedicated thread anyway.
   * <p>
   * SonarQube disagrees.
   * </p>
   * The catch: when many threads run, parallel GC may not get sufficient CPU cycles, until heap
   * memory is exhausted. Yes, this is a good place to drop a hint to GC that it
   * <strong>might</strong> want to clean up memory.
   * 
   * @param listAllegations EsPersonReferral
   * @param mapReferrals k=id, v=EsPersonReferral
   * @param listClientReferralKeys client/referral id pairs
   * @param listReadyToNorm EsPersonReferral
   */
  protected void cleanUpMemory(final List<EsPersonReferral> listAllegations,
      Map<String, EsPersonReferral> mapReferrals, List<MinClientReferral> listClientReferralKeys,
      List<EsPersonReferral> listReadyToNorm) {
    releaseLocalMemory(listAllegations, mapReferrals, listClientReferralKeys, listReadyToNorm);
    System.gc(); // NOSONAR
  }

  /**
   * Pour referrals, allegations, and client/referral keys into the caldron and brew into a
   * referrals element per client.
   * 
   * @param listAllegations bundle allegations
   * @param mapReferrals k=referral id, v=EsPersonReferral
   * @param listClientReferralKeys client/referral key pairs
   * @param listReadyToNorm denormalized records
   * @return normalized record count
   */
  protected int mapReduce(final List<EsPersonReferral> listAllegations,
      final Map<String, EsPersonReferral> mapReferrals,
      final List<MinClientReferral> listClientReferralKeys,
      final List<EsPersonReferral> listReadyToNorm) {
    int countNormalized = 0;
    try {
      final Map<String, List<MinClientReferral>> mapReferralByClient =
          listClientReferralKeys.stream().filter(MinClientReferral::isActive)
              .sorted((e1, e2) -> e1.getClientId().compareTo(e2.getClientId()))
              .collect(Collectors.groupingBy(MinClientReferral::getClientId));
      listClientReferralKeys.clear(); // release objects for gc

      final Map<String, List<EsPersonReferral>> mapAllegationByReferral = listAllegations.stream()
          .sorted((e1, e2) -> e1.getReferralId().compareTo(e2.getReferralId()))
          .collect(Collectors.groupingBy(EsPersonReferral::getReferralId));
      listAllegations.clear(); // release objects for gc

      // For each client group:
      countNormalized = normalizeQueryResults(mapReferrals, listReadyToNorm, mapReferralByClient,
          mapAllegationByReferral);
    } finally {
      cleanUpMemory(listAllegations, mapReferrals, listClientReferralKeys, listReadyToNorm);
    }
    return countNormalized;
  }

  protected String getClientSeedQuery() {
    return INSERT_CLIENT_FULL;
  }

  /**
   * Read all records from a single partition (key range) in buckets. Then sort results and
   * normalize.
   * 
   * <p>
   * Each call to this method should run in its own thread.
   * </p>
   * 
   * @param p partition (key) range to read
   * @return number of client documents affected
   * @throws NeutronCheckedException general error
   */
  protected int pullNextRange(final Pair<String, String> p) throws NeutronCheckedException {
    final String threadName =
        "extract_" + nextThreadNum.incrementAndGet() + "_" + p.getLeft() + "_" + p.getRight();
    nameThread(threadName);
    getFlightLog().markRangeStart(p);

    allocateThreadMemory(); // allocate thread local memory, if not done prior.
    final List<EsPersonReferral> listAllegations = allocAllegations.get();
    final Map<String, EsPersonReferral> mapReferrals = allocReferrals.get();
    final List<MinClientReferral> listClientReferralKeys = allocClientReferralKeys.get();
    final List<EsPersonReferral> listReadyToNorm = allocReadyToNorm.get();

    // Clear collections, free memory before starting.
    releaseLocalMemory(listAllegations, mapReferrals, listClientReferralKeys, listReadyToNorm);

    try (final Connection con = getConnection()) {
      con.setSchema(getDBSchemaName());
      con.setAutoCommit(false);
      NeutronDB2Utils.enableParallelism(con);

      final DB2SystemMonitor monitor = NeutronDB2Utils.monitorStart(con);
      final String schema = getDBSchemaName();

      try (final PreparedStatement stmtInsClient = con.prepareStatement(getClientSeedQuery());
          final PreparedStatement stmtSelClient = con.prepareStatement(SELECT_CLIENT);
          final PreparedStatement stmtSelReferral =
              con.prepareStatement(getInitialLoadQuery(schema));
          final PreparedStatement stmtSelAllegation = con.prepareStatement(SELECT_ALLEGATION)) {

        readClients(stmtInsClient, stmtSelClient, listClientReferralKeys, p);
        readReferrals(stmtSelReferral, mapReferrals);
        readAllegations(stmtSelAllegation, listAllegations);

        NeutronDB2Utils.monitorStopAndReport(monitor);
        con.commit();
      }
    } catch (SQLException e) {
      fail();
      throw CheeseRay.checked(LOGGER, e, "ERROR HANDLING RANGE {} - {}: {}", p.getLeft(),
          p.getRight(), e.getMessage());
    }

    int cntr = mapReduce(listAllegations, mapReferrals, listClientReferralKeys, listReadyToNorm);
    getFlightLog().markRangeComplete(p);
    return cntr;
  }

  /**
   * Initial load only. The "extract" part of ETL. Runs partition/key ranges in separate threads.
   * 
   * <p>
   * Note that this rocket normalizes <strong>without</strong> the transform thread.
   * </p>
   */
  @Override
  protected void threadRetrieveByJdbc() {
    nameThread("read_main");
    LOGGER.info("BEGIN: main read thread");

    // WARNING: static setter is OK in *standalone* rocket but NOT wise in continuous mode.
    EsPersonReferral.setOpts(getFlightPlan());
    doneTransform(); // normalize in place **WITHOUT** the transform thread

    try {
      final List<Pair<String, String>> ranges = getPartitionRanges();
      LOGGER.info(">>>>>>>> # OF RANGES: {} <<<<<<<<", ranges);
      final List<ForkJoinTask<?>> tasks = new ArrayList<>(ranges.size());
      final ForkJoinPool threadPool =
          new ForkJoinPool(NeutronThreadUtils.calcReaderThreads(getFlightPlan()));

      // Queue execution.
      for (Pair<String, String> p : ranges) {
        // Pull each range **independently** on the next available thread.
        tasks.add(threadPool.submit(() -> pullNextRange(p)));
      }

      // Join threads. Don't return from method until they complete.
      for (ForkJoinTask<?> task : tasks) {
        task.get();
      }
    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(LOGGER, e, "REFERRALS: ERROR IN THREADED RETRIEVAL! {}",
          e.getMessage());
    } finally {
      doneRetrieve();
    }

    LOGGER.info("DONE: retrieved {} ES referral rows", this.rowsReadReferrals.get());
  }

  /**
   * This rocket normalizes <strong>without</strong> the transform thread.
   */
  @Override
  public boolean useTransformThread() {
    return false;
  }

  /**
   * DB2's optimizer is not very bright.
   */
  @Override
  public String getPrepLastChangeSQL() {
    try {
      return NeutronDB2Utils.prepLastChangeSQL(INSERT_REFERRAL_LAST_CHG,
          determineLastSuccessfulRunTime(), getFlightPlan().getOverrideLastEndTime());
    } catch (NeutronCheckedException e) {
      throw CheeseRay.runtime(LOGGER, e, "ERROR BUILDING LAST CHANGE SQL: {}", e.getMessage());
    }
  }

  @Override
  public boolean isInitialLoadJdbc() {
    return true;
  }

  /**
   * Referrals is an <strong>enormous</strong> task and fetches partition ranges from a file instead
   * of a bloated Java class.
   * 
   * @see ReferralJobRanges
   */
  @Override
  public List<Pair<String, String>> getPartitionRanges() throws NeutronCheckedException {
    return new ReferralJobRanges().getPartitionRanges(this);
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
  public String getOptionalElementName() {
    return "referrals";
  }

  @Override
  protected UpdateRequest prepareUpsertRequest(ElasticSearchPerson esp, ReplicatedPersonReferrals p)
      throws NeutronCheckedException {
    return prepareUpdateRequest(esp, p, p.getReferrals(), true);
  }

  @Override
  public EsPersonReferral extract(final ResultSet rs) throws SQLException {
    return new EsPersonReferral(rs);
  }

  protected void releaseLocalMemory(final List<EsPersonReferral> listAllegations,
      final Map<String, EsPersonReferral> mapReferrals,
      final List<MinClientReferral> listClientReferralKeys,
      final List<EsPersonReferral> listReadyToNorm) {
    listAllegations.clear();
    listClientReferralKeys.clear();
    listReadyToNorm.clear();
    mapReferrals.clear();
  }

  public boolean isMonitorDb2() {
    return monitorDb2;
  }

  public void setMonitorDb2(boolean monitorDb2) {
    this.monitorDb2 = monitorDb2;
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(ReferralHistoryIndexerJob.class, args);
  }

}
