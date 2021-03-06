package gov.ca.cwds.neutron.rocket;

import static gov.ca.cwds.neutron.enums.NeutronIntegerDefaults.FETCH_SIZE;
import static gov.ca.cwds.neutron.enums.NeutronIntegerDefaults.LOG_EVERY;
import static gov.ca.cwds.neutron.enums.NeutronIntegerDefaults.QUERY_TIMEOUT_IN_SECONDS;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.INS_CLI_DUMMY;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.INS_CLI_LST_CHG;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.INS_CLI_RNG;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.INS_PLACE_CLI_FULL;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_ADDR;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_AKA;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_CASE;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_CLI;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_CLI_ADDR;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_CLI_COUNTY;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_CSEC;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_ETHNIC;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_PLACE_ADDR;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_SAFETY;
import static gov.ca.cwds.neutron.util.NeutronThreadUtils.freeMemory;
import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.data.persistence.cms.PlacementHomeAddress;
import gov.ca.cwds.data.persistence.cms.client.RawAddress;
import gov.ca.cwds.data.persistence.cms.client.RawAka;
import gov.ca.cwds.data.persistence.cms.client.RawCase;
import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.client.RawClientAddress;
import gov.ca.cwds.data.persistence.cms.client.RawClientCounty;
import gov.ca.cwds.data.persistence.cms.client.RawCsec;
import gov.ca.cwds.data.persistence.cms.client.RawEthnicity;
import gov.ca.cwds.data.persistence.cms.client.RawSafetyAlert;
import gov.ca.cwds.data.persistence.cms.client.RawToEsConverter;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.jobs.ClientPersonIndexerJob;
import gov.ca.cwds.neutron.atom.AtomLoadStepHandler;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Monitor;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

/**
 * {@link AtomLoadStepHandler} for People Summary index, initial load.
 * 
 * <p>
 * Loads {@link RawClient} and {@link PlacementHomeAddress}, normalizes to {@link ReplicatedClient}.
 * </p>
 * 
 * <p>
 * NOT thread safe! Each instance of this class should operate in its own thread.
 * </p>
 * 
 * @author CWDS API Team
 */
@SuppressWarnings({"findsecbugs:SQL_INJECTION_JDBC"})
public class PeopleSummaryThreadHandler
    implements ApiMarker, AtomLoadStepHandler<ReplicatedClient> {

  private static final long serialVersionUID = 1L;

  protected static final Logger LOGGER = LoggerFactory.getLogger(PeopleSummaryThreadHandler.class);

  public static final int LG_SZ = LOG_EVERY.getValue();

  public static final int FULL_LOAD_SIZE = NeutronIntegerDefaults.FULL_DENORMALIZED_SIZE.value();

  protected static final int TFO = TYPE_FORWARD_ONLY;
  protected static final int CRO = CONCUR_READ_ONLY;

  public enum STEP {

    START("Job start"),

    FIND_CHANGED_CLIENT("Find changed clients"),

    SET_CLIENT_KEY("Set client keys"),

    SET_PLACEMENT_KEY("Set placement home keys"),

    SEL_CLIENT("Pull client"),

    SEL_CLIENT_ADDRESS("Pull client address"),

    SEL_ADDRESS("Pull address"),

    SEL_CLIENT_COUNTY("Pull client county"),

    SEL_AKA("Pull AKA"),

    SEL_CASE("Pull case"),

    SEL_CSEC("Pull csec"),

    SEL_ETHNIC("Pull ethnicity"),

    SEL_SAFETY("Pull safety"),

    SEL_PLACEMENT_HOME("Pull placement home"),

    DONE_RETRIEVAL("Done pulling data"),

    BEAN_TO_JSON("Convert data to JSON"),

    INDEX_TO_ES("Wait on Elasticsearch indexing"),

    REPLICATION_TIME_MILLIS("Avg millis to replicate records"),

    REPLICATION_TIME_SECS("Avg seconds to replicate records"),

    REPLICATION_TIME_MAX_SECS("Max seconds to replicate records"),

    REPLICATION_TIME_MIN_SECS("Min seconds to replicate records"),

    DONE("Job done")

    ;

    private final String msg;

    private STEP(String msg) {
      this.msg = msg;
    }

    public String getMsg() {
      return msg;
    }
  }

  private ClientPersonIndexerJob rocket;

  protected boolean doneHandlerRetrieve = false;

  protected transient Set<String> deletionResults = new HashSet<>();

  /**
   * key = client id. Single thread, non-thread-safe containers OK.
   */
  protected transient Map<String, PlacementHomeAddress> placementHomeAddresses =
      new HashMap<>(5011);

  /**
   * key = client id. Single thread, non-thread-safe containers OK.
   */
  protected transient Map<String, RawClient> rawClients = new HashMap<>(FULL_LOAD_SIZE);

  /**
   * key = client id. Single thread, non-thread-safe containers OK.
   */
  protected transient Map<String, ReplicatedClient> normalized = new HashMap<>(FULL_LOAD_SIZE);

  public PeopleSummaryThreadHandler(ClientPersonIndexerJob rocket) {
    this.rocket = rocket;
  }

  // =================================
  // Neutron, the next generation.
  // =================================

  protected void step(STEP step) {
    LOGGER.debug(step.getMsg());
    rocket.getFlightLog().addTimingEvent(step.name().toLowerCase());
  }

  protected void read(final PreparedStatement stmt, Consumer<ResultSet> consumer) {
    LOGGER.trace("read(): begin");
    try {
      stmt.setMaxRows(0);
      stmt.setQueryTimeout(QUERY_TIMEOUT_IN_SECONDS.getValue());
      stmt.setFetchSize(FETCH_SIZE.getValue());

      // Close ResultSet for driver stability, despite the JDBC standard (i.e., close parent
      // Statement, not child ResultSet). The DB2 driver gets confused, if you just close parent
      // statement and session without closing the result set.
      try (final ResultSet rs = stmt.executeQuery()) {
        consumer.accept(rs);
      } finally {
        // Auto-close result set.
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "SELECT FAILED! {}", e.getMessage(), e);
    }

    LOGGER.trace("read(): done");
  }

  protected void readClient(final ResultSet rs) {
    LOGGER.trace("readClient(): begin");
    int counter = 0;
    RawClient c = null;

    try {
      while (rocket.isRunning() && rs.next() && (c = new RawClient().read(rs)) != null) {
        rawClients.put(c.getCltId(), c);
        CheeseRay.logEvery(LOGGER, LG_SZ, ++counter, "read", "client");
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ CLIENT! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} client records.", counter);
  }

  protected void readClientAddress(final ResultSet rs) {
    LOGGER.trace("readClientAddress(): begin");
    int counter = 0;
    RawClient c = null;
    RawClientAddress cla = null;

    try {
      while (rocket.isRunning() && rs.next() && (cla = new RawClientAddress().read(rs)) != null) {
        c = rawClients.get(cla.getCltId());
        if (c != null) {
          c.addClientAddress(cla);
          CheeseRay.logEvery(LOGGER, LG_SZ, ++counter, "read", "client address");
        } else {
          LOGGER.warn("ORPHAN CLIENT ADDRESS! id: {}, client: {}", cla.getClaId(), cla.getCltId());
        }
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ CLIENT ADDRESS! {}", e.getMessage(), e);
    }

    LOGGER.info("readClientAddress(): Retrieved {} client address records.", counter);
  }

  protected void readAddress(final ResultSet rs) {
    LOGGER.trace("readAddress(): begin");
    int counter = 0;
    RawAddress adr = null;
    RawClient c = null;

    try {
      while (rocket.isRunning() && rs.next() && (adr = new RawAddress().read(rs)) != null) {
        c = rawClients.get(adr.getCltId());
        if (c != null) {
          final Map<String, RawClientAddress> cla = c.getClientAddress();
          final String claKey = adr.getClaId();
          if (cla != null && StringUtils.isNotEmpty(claKey) && cla.containsKey(claKey)) {
            cla.get(claKey).setAddress(adr);
          } else {
            LOGGER.warn("ORPHAN ADDRESS! client: {}, client address: {}, address: {}",
                adr.getCltId(), claKey, adr.getAdrId());
          }
          CheeseRay.logEvery(LOGGER, LG_SZ, ++counter, "read", "address");
        } else {
          LOGGER.warn("NO CLIENT FOR ADDRESS! client: {}, id: {}", adr.getCltId(), adr.getAdrId());
        }
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ ADDRESS! {}", e.getMessage(), e);
    }

    LOGGER.info("readAddress(): retrieved {} address records.", counter);
  }

  protected void readClientCounty(final ResultSet rs) {
    LOGGER.trace("readClientCounty(): begin");
    int counter = 0;
    RawClient c = null;
    RawClientCounty cc = null;

    try {
      while (rocket.isRunning() && rs.next() && (cc = new RawClientCounty().read(rs)) != null) {
        c = rawClients.get(cc.getCltId());
        if (c != null) {
          c.addClientCounty(cc);
          CheeseRay.logEvery(LOGGER, LG_SZ, ++counter, "read", "client county");
        } else {
          LOGGER.warn("ORPHAN CLIENT COUNTY! id: {}, client: {}", cc.getCltId(), cc.getCltId());
        }
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ CLIENT COUNTY! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} client county records.", counter);
  }

  protected void readAka(final ResultSet rs) {
    LOGGER.trace("readAka(): begin");
    int counter = 0;
    RawClient c = null;
    RawAka aka = null;

    try {
      while (rocket.isRunning() && rs.next() && (aka = new RawAka().read(rs)) != null) {
        c = rawClients.get(aka.getCltId());
        if (c != null) {
          c.addAka(aka);
          CheeseRay.logEvery(LOGGER, LG_SZ, ++counter, "read", "aka");
        } else {
          LOGGER.warn("ORPHAN AKA! id: {}, client: {}", aka.getAkaId(), aka.getCltId());
        }
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ AKA! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} aka records.", counter);
  }

  protected void readCase(final ResultSet rs) {
    LOGGER.trace("readCase(): begin");
    int counter = 0;
    RawClient c = null;
    RawCase cas = null;

    try {
      while (rocket.isRunning() && rs.next() && (cas = new RawCase().read(rs)) != null) {
        c = rawClients.get(cas.getCltId());
        if (c != null) {
          c.addCase(cas);
          CheeseRay.logEvery(LOGGER, LG_SZ, ++counter, "read", "case");
        } else {
          LOGGER.warn("ORPHAN CASE! id: {}, client: {}", cas.getOpenCaseId(), cas.getCltId());
        }
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ CASE! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} case records.", counter);
  }

  protected void readCsec(final ResultSet rs) {
    LOGGER.trace("readCsec(): begin");
    int counter = 0;
    RawClient c = null;
    RawCsec csec = null;

    try {
      while (rocket.isRunning() && rs.next() && (csec = new RawCsec().read(rs)) != null) {
        c = rawClients.get(csec.getCltId());
        if (c != null) {
          c.addCsec(csec);
          CheeseRay.logEvery(LOGGER, 250, ++counter, "read", "csec");
        } else {
          LOGGER.warn("ORPHAN CSEC! id: {}, client: {}", csec.getCsecId(), csec.getCltId());
        }
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ CSEC! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} CSEC records.", counter);
  }

  protected void readEthnicity(final ResultSet rs) {
    LOGGER.trace("readEthnicity(): begin");
    int counter = 0;
    RawClient c = null;
    RawEthnicity eth = null;

    try {
      while (rocket.isRunning() && rs.next() && (eth = new RawEthnicity().read(rs)) != null) {
        c = rawClients.get(eth.getCltId());
        if (c != null) {
          c.addEthnicity(eth);
          CheeseRay.logEvery(LOGGER, LG_SZ, ++counter, "read", "ethnicity");
        } else {
          LOGGER.warn("ORPHAN ETHNICITY! id: {}, client: {}", eth.getClientEthnicityId(),
              eth.getCltId());
        }
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ ETHNICITY! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} ethnicity records.", counter);
  }

  protected void readSafetyAlert(final ResultSet rs) {
    LOGGER.trace("readSafetyAlert(): begin");
    int counter = 0;
    RawClient c = null;
    RawSafetyAlert saf = null;

    try {
      while (rocket.isRunning() && rs.next() && (saf = new RawSafetyAlert().read(rs)) != null) {
        c = rawClients.get(saf.getCltId());
        if (c != null) {
          c.addSafetyAlert(saf);
          CheeseRay.logEvery(LOGGER, LG_SZ, ++counter, "read", "safety");
        } else {
          LOGGER.warn("ORPHAN SAFETY ALERT! id: {}, client: {}", saf.getSafetyAlertId(),
              saf.getCltId());
        }
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ SAFETY ALERT! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} safety alert records.", counter);
  }

  /**
   * SNAP-715: Initial Load: ERRORCODE=-1224, SQLSTATE=55032.
   * 
   * <p>
   * NEW SCHOOL: Read data, commit frequently, THEN normalize. Takes more memory but reduces
   * database errors.
   * </p>
   * 
   * <p>
   * OLD SCHOOL: normalize in place to reduce memory but holds cursors open longer.
   * </p>
   * 
   * {@inheritDoc}
   */
  @Override
  public void handleMainResults(ResultSet rs, Connection con) throws SQLException {
    final int cntrRetrieved = rawClients.size();

    LOGGER.info("handleMainResults(): commit");
    con.commit(); // free database resources

    final FlightLog flightLog = rocket.getFlightLog();
    flightLog.addToDenormalized(cntrRetrieved);
  }

  protected int loadClientRange(Connection con, final PreparedStatement stmtInsClient,
      Pair<String, String> range) throws SQLException {
    LOGGER.trace("loadClientRange(): begin");
    con.commit(); // free db resources

    // Initial Load client ranges.
    try {
      stmtInsClient.setString(1, range.getLeft());
      stmtInsClient.setString(2, range.getRight());
    } catch (Exception e) {
      LOGGER.trace("loadClientRange(): FAILED TO SET RANGES. Last change mode?", e);
    }

    final int ret = stmtInsClient.executeUpdate();
    LOGGER.info("loadClientRange(): client count: {}", ret);
    return ret;
  }

  protected boolean isInitialLoad() {
    return true;
  }

  /**
   * SNAP-796: measure replication lag.
   */
  protected void addOtherTimings() {
    LOGGER.debug("Add replication timings");
    final FlightLog lastReplicationCheck =
        rocket.launchDirector.getFlightRecorder().getLastFlightLog(ReplicationLagRocket.class);
    if (lastReplicationCheck != null) {
      rocket.getFlightLog().addOtherMetrics(lastReplicationCheck);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Read placement home addresses per rule R-02294, Client Abstract Most Recent Address.
   * </p>
   * 
   * <p>
   * Commit more often by re-inserting client id's into GT_ID.
   * </p>
   */
  @Override
  public void handleSecondaryJdbc(Connection con, Pair<String, String> range) throws SQLException {
    LOGGER.trace("handleSecondaryJdbc(): begin");
    final FlightLog fl = rocket.getFlightLog();
    final FlightPlan fp = rocket.getFlightPlan();

    String sqlPlacementAddress;
    try {
      sqlPlacementAddress = NeutronDB2Utils.prepLastChangeSQL(SEL_PLACE_ADDR,
          rocket.determineLastSuccessfulRunTime(), rocket.getFlightPlan().getOverrideLastEndTime());
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "INVALID PLACEMENT ADDRESS SQL! {}", e.getMessage(), e);
    }

    try (
        final NeutronDB2Monitor mon =
            new NeutronDB2Monitor(con, fp.isDebug() && !fp.isLastRunMode());
        final PreparedStatement stmtInsClient =
            con.prepareStatement(pickPrepDml(INS_CLI_RNG, INS_CLI_LST_CHG));
        final PreparedStatement stmtInsClientPlaceHome =
            con.prepareStatement(pickPrepDml(INS_PLACE_CLI_FULL, INS_CLI_DUMMY));
        final PreparedStatement stmtSelPlacementAddress =
            con.prepareStatement(sqlPlacementAddress, TFO, CRO);
        final PreparedStatement stmtSelClient = con.prepareStatement(SEL_CLI, TFO, CRO);
        final PreparedStatement stmtSelCliAddr = con.prepareStatement(SEL_CLI_ADDR, TFO, CRO);
        final PreparedStatement stmtSelAddress = con.prepareStatement(SEL_ADDR, TFO, CRO);
        final PreparedStatement stmtSelCliCnty = con.prepareStatement(SEL_CLI_COUNTY, TFO, CRO);
        final PreparedStatement stmtSelAka = con.prepareStatement(SEL_AKA, TFO, CRO);
        final PreparedStatement stmtSelCase = con.prepareStatement(SEL_CASE, TFO, CRO);
        final PreparedStatement stmtSelCsec = con.prepareStatement(SEL_CSEC, TFO, CRO);
        final PreparedStatement stmtSelEthnicity = con.prepareStatement(SEL_ETHNIC, TFO, CRO);
        final PreparedStatement stmtSelSafety = con.prepareStatement(SEL_SAFETY, TFO, CRO)) {

      // Client keys for this bundle.
      step(STEP.SET_CLIENT_KEY);
      final int keyCount = loadClientRange(con, stmtInsClient, range);

      if (keyCount > 0) {
        step(STEP.SEL_CLIENT);
        LOGGER.trace("SEL_CLI: \n\n{}\n", SEL_CLI);
        read(stmtSelClient, rs -> readClient(rs));

        // SNAP-735: missing addresses.
        step(STEP.SEL_CLIENT_ADDRESS);
        LOGGER.trace("SEL_CLI_ADDR:\n\n{}\n", SEL_CLI_ADDR);
        read(stmtSelCliAddr, rs -> readClientAddress(rs));

        step(STEP.SEL_ADDRESS);
        LOGGER.trace("SEL_ADDR:\n\n{}\n", SEL_ADDR);
        read(stmtSelAddress, rs -> readAddress(rs));

        step(STEP.SEL_CLIENT_COUNTY);
        LOGGER.trace("SEL_CLI_COUNTY:\n\n{}\n", SEL_CLI_COUNTY);
        read(stmtSelCliCnty, rs -> readClientCounty(rs));

        step(STEP.SEL_AKA);
        LOGGER.trace("SEL_AKA:\n{}", SEL_AKA);
        read(stmtSelAka, rs -> readAka(rs));

        step(STEP.SEL_CASE);
        LOGGER.trace("SEL_CASE:\n{}", SEL_CASE);
        read(stmtSelCase, rs -> readCase(rs));

        step(STEP.SEL_CSEC);
        LOGGER.trace("SEL_CSEC:\n{}", SEL_CSEC);
        read(stmtSelCsec, rs -> readCsec(rs));

        step(STEP.SEL_ETHNIC);
        LOGGER.trace("SEL_ETHNIC:\n{}", SEL_ETHNIC);
        read(stmtSelEthnicity, rs -> readEthnicity(rs));

        step(STEP.SEL_SAFETY);
        LOGGER.trace("SEL_SAFETY:\n{}", SEL_SAFETY);
        read(stmtSelSafety, rs -> readSafetyAlert(rs));

        step(STEP.SET_PLACEMENT_KEY);
        prepPlacementClients(stmtInsClientPlaceHome, range);

        step(STEP.SEL_PLACEMENT_HOME);
        readPlacementAddress(stmtSelPlacementAddress);
      }

      con.commit(); // free db resources. Make DBA's happy.

      // AR-325: reinstate replicate metric rocket.
      // calcReplicationDelay(); // When ready
    } catch (Exception e) {
      LOGGER.error("handleSecondaryJdbc: BOOM!", e);

      try {
        if (isInitialLoad()) {
          fl.markRangeError(range); // FULL MODE: Fail the BUCKET, NOT the WHOLE FLIGHT!
        } else {
          rocket.fail(); // Last change: fail the whole job!
        }

        con.rollback();
      } catch (Exception e2) {
        LOGGER.debug("NESTED ROLLBACK EXCEPTION!", e2);
      }
      throw CheeseRay.runtime(LOGGER, e, "SECONDARY JDBC FAILED! {}", e.getMessage(), e);
    } finally {
      addOtherTimings();
    }

    step(STEP.DONE_RETRIEVAL);
    LOGGER.debug("handleSecondaryJdbc(): DONE");
  }

  protected void calcReplicationDelay() {
    // No-op in Initial Load.
  }

  protected void mapReplicatedClient(PlacementHomeAddress pha) {
    if (normalized.containsKey(pha.getClientId())) {
      final ReplicatedClient rc = normalized.get(pha.getClientId());
      rc.setActivePlacementHomeAddress(pha);
    } else {
      // WARNING: last chg: if the client wasn't picked up from the view, then it's not here.
      LOGGER.warn("Client id for placement home address NOT FOUND! client id: {}",
          pha.getClientId());
    }
  }

  @Override
  public void handleJdbcDone(final Pair<String, String> range) {
    final RawToEsConverter conv = new RawToEsConverter();
    this.rawClients.values().stream().map(r -> r.normalize(conv))
        .forEach(c -> normalized.put(c.getId(), c));
    rawClients.clear(); // free memory
    LOGGER.debug("handleJdbcDone: normalized: {}", normalized.size());

    // Merge placement home addresses.
    placementHomeAddresses.values().stream().forEach(this::mapReplicatedClient);

    // Send to Elasticsearch.
    normalized.values().stream().forEach(rocket::addToIndexQueue);
    LOGGER.debug("handleJdbcDone: FINISHED");
  }

  @Override
  public void handleStartRange(Pair<String, String> range) {
    rocket.getFlightLog().markRangeStart(range);
    rocket.doneTransform();
    clear();
  }

  /**
   * <p>
   * SNAP-820: manual gc needed for Initial Load, not continuous mode. Should seldom call gc
   * directly anyway.
   * </p>
   * {@inheritDoc}
   */
  @Override
  public void handleFinishRange(Pair<String, String> range) {
    doneThreadRetrieve();

    if (getRocket() != null && getRocket().getFlightPlan() != null
        && !getRocket().getFlightPlan().isLastRunMode()) {
      freeMemory();
    }
  }

  @Override
  public List<ReplicatedClient> getResults() {
    return normalized.values().stream().collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   * 
   * DB2 doesn't deal well with large sets of keys. Split lists of changed keys into smaller
   * portions.
   */
  @Override
  public List<ReplicatedClient> fetchLastRunNormalizedResults(Date lastRunDate,
      Set<String> deletionResults) {
    final Pair<String, String> range = Pair.<String, String>of("a", "b"); // dummy range
    handleStartRange(range);

    this.deletionResults = deletionResults;
    LOGGER.info("After last run fetch: count: {}", normalized.size());

    // Handle additional JDBC statements, if any.
    try (final Session session = rocket.getJobDao().grabSession();
        final Connection con = NeutronJdbcUtils.prepConnection(session);
        final PreparedStatement stmtSelPlacementAddress =
            con.prepareStatement(ClientSQLResource.SEL_PLACE_ADDR)) {
      try {
        readPlacementAddress(stmtSelPlacementAddress);
      } catch (Exception e) {
        LOGGER.error("ERROR READING PLACEMENT HOME ADDRESS!", e);
        throw e;
      }

      con.commit(); // Done reading data. Clear temp tables.

      // Merge placement homes and index into Elasticsearch.
      handleJdbcDone(range);
      final List<ReplicatedClient> ret = getResults();
      LOGGER.info("FETCHED {} LAST CHANGE RESULTS", ret.size());
      return ret;
    } catch (Exception e) {
      rocket.fail();
      throw CheeseRay.runtime(LOGGER, e, "ERROR EXECUTING LAST CHANGE SQL! {}", e.getMessage());
    } finally {
      handleFinishRange(range); // ALWAYS RELEASE MEMORY!!
      rocket.getFlightLog().markRangeComplete(range);
      clear();
      this.rocket = null; // Release reference to parent rocket.
    }
  }

  /**
   * Some SQL statements differ between last change and initial load.
   * 
   * @param sqlInitialLoad SQL for initial load
   * @param sqlLastChange SQL for last change
   * @return chosen SQL
   * @throws NeutronCheckedException on SQL preparation error
   */
  protected String pickPrepDml(String sqlInitialLoad, String sqlLastChange)
      throws NeutronCheckedException {
    final String sql = rocket.getFlightPlan().isLastRunMode()
        ? NeutronDB2Utils.prepLastChangeSQL(sqlLastChange, rocket.determineLastSuccessfulRunTime(),
            rocket.getFlightPlan().getOverrideLastEndTime())
        : sqlInitialLoad; // initial mode
    LOGGER.trace("Prep SQL: \n{}", sql);
    return sql;
  }

  public void addAll(Collection<ReplicatedClient> collection) {
    if (!collection.isEmpty()) {
      if (normalized.size() < collection.size()) {
        this.normalized = new HashMap<>(collection.size());
      }
      collection.stream().forEach(c -> normalized.put(c.getId(), c));
    }
  }

  /**
   * Release unneeded heap memory early and often.
   */
  public void clear() {
    LOGGER.trace("clear containers");
    normalized.clear();
    rawClients.clear();
    placementHomeAddresses.clear();
  }

  /**
   * Normalize records from MQT/view for same client id.
   * 
   * @param grpRecs recs for same client id
   * @deprecated EsClientPerson goes away
   */
  @Deprecated
  protected void normalize(final List<RawClient> grpRecs) {
    grpRecs.stream().sorted((e1, e2) -> e1.compare(e1, e2)).sequential().sorted()
        .collect(Collectors.groupingBy(RawClient::getNormalizationGroupKey)).entrySet().stream()
        .map(e -> rocket.normalizeSingle(e.getValue())).forEach(n -> normalized.put(n.getId(), n));
  }

  protected void prepPlacementClients(final PreparedStatement stmt, final Pair<String, String> p)
      throws SQLException {
    stmt.setMaxRows(0);
    stmt.setQueryTimeout(QUERY_TIMEOUT_IN_SECONDS.getValue()); // SNAP-709
    stmt.setFetchSize(FETCH_SIZE.getValue());

    if (!rocket.getFlightPlan().isLastRunMode()) {
      LOGGER.debug("Prep Affected Clients: range: {} - {}", p.getLeft(), p.getRight());
      try {
        stmt.setString(1, p.getLeft());
        stmt.setString(2, p.getRight());
      } catch (Exception e) {
        LOGGER.warn("FAILED TO SET PARAMS. Last change mode?", e);
      }
    }

    final int countInsClient = stmt.executeUpdate();
    LOGGER.info("Prepped placement home clients: {}", countInsClient);
  }

  protected void readPlacementAddress(final PreparedStatement stmt) throws SQLException {
    stmt.setMaxRows(0);
    stmt.setQueryTimeout(QUERY_TIMEOUT_IN_SECONDS.getValue()); // SNAP-709
    stmt.setFetchSize(FETCH_SIZE.getValue());

    int cntr = 0;
    PlacementHomeAddress pha;

    // SNAP-709: Connection is closed. ERRORCODE=-4470, SQLSTATE=08003.
    // According to the JDBC specification, you shouldn't close the ResultSet; closing its parent
    // statement should close the result set.
    try (final ResultSet rs = stmt.executeQuery()) {
      while (rocket.isRunning() && rs.next()) {
        CheeseRay.logEvery(LOGGER, ++cntr, "Placement homes fetched", "recs");
        pha = new PlacementHomeAddress(rs);
        placementHomeAddresses.put(pha.getClientId(), pha);
      }
    } finally {
      // Close result set.
    }

    LOGGER.info("Placement home count: {}", placementHomeAddresses.size());
  }

  public Map<String, ReplicatedClient> getNormalized() {
    return normalized;
  }

  public boolean isDoneHandlerRetrieve() {
    return doneHandlerRetrieve;
  }

  protected void doneThreadRetrieve() {
    this.doneHandlerRetrieve = true;
  }

  public ClientPersonIndexerJob getRocket() {
    return rocket;
  }

  @Override
  public String getEventType() {
    return "neutron_initial_load_client";
  }

  public void setRocket(ClientPersonIndexerJob rocket) {
    this.rocket = rocket;
  }

  public int getRunId() {
    return 0;
  }

  public Map<String, RawClient> getRawClients() {
    return rawClients;
  }

}
