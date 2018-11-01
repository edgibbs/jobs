package gov.ca.cwds.neutron.rocket;

import static gov.ca.cwds.neutron.enums.NeutronIntegerDefaults.FETCH_SIZE;
import static gov.ca.cwds.neutron.enums.NeutronIntegerDefaults.QUERY_TIMEOUT_IN_SECONDS;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.INSERT_CLIENT_DUMMY;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.INS_CLIENT_LAST_CHG;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.INS_CLIENT_RANGE;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.INS_PLACEMENT_CLIENT_FULL;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SELECT_ADDRESS;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SELECT_AKA;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SELECT_CASE;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SELECT_CLIENT;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SELECT_CSEC;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SELECT_ETHNICITY;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SELECT_SAFETY;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_CLIENT_ADDR;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_CLI_COUNTY;
import static gov.ca.cwds.neutron.rocket.ClientSQLResource.SEL_PLACEMENT_ADDR;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.data.persistence.cms.PlacementHomeAddress;
import gov.ca.cwds.data.persistence.cms.client.ClientReference;
import gov.ca.cwds.data.persistence.cms.client.NeutronJdbcReader;
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
import gov.ca.cwds.neutron.jetpack.CheeseRay;
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

  public static final int FULL_DENORMALIZED_SIZE =
      NeutronIntegerDefaults.FULL_DENORMALIZED_SIZE.value();

  protected static final int TFO = TYPE_FORWARD_ONLY;
  protected static final int CRO = CONCUR_READ_ONLY;

  private final ClientPersonIndexerJob rocket;

  protected boolean doneHandlerRetrieve = false;

  protected Set<String> deletionResults = new HashSet<>();

  /**
   * key = client id
   */
  protected final Map<String, PlacementHomeAddress> placementHomeAddresses = new HashMap<>(5011);

  /**
   * key = client id
   */
  protected Map<String, RawClient> rawClients = new HashMap<>(FULL_DENORMALIZED_SIZE);

  /**
   * key = client id
   */
  protected Map<String, ReplicatedClient> normalized = new HashMap<>(FULL_DENORMALIZED_SIZE);

  public PeopleSummaryThreadHandler(ClientPersonIndexerJob rocket) {
    this.rocket = rocket;
  }

  // =================================
  // Neutron, the next generation.
  // =================================

  protected void read(final PreparedStatement stmt, Consumer<ResultSet> consumer) {
    try {
      stmt.setMaxRows(0);
      stmt.setQueryTimeout(QUERY_TIMEOUT_IN_SECONDS.getValue());
      stmt.setFetchSize(FETCH_SIZE.getValue());

      // Close ResultSet for driver stability. Can't just close parent statement and session.
      try (final ResultSet rs = stmt.executeQuery()) {
        // DRS: see HyperCube.makeCmsSessionFactory() for magic DB2 settings.
        // Without those settings, rs.next() frequently throws an Exception, because IBM's DB2 JDBC
        // driver does NOT comply with JDBC type 4 standards!
        while (rocket.isRunning() && rs.next()) { // oh DB2 ...
          consumer.accept(rs);
        }
      } finally {
        // Auto-close result set.
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "SELECT FAILED! {}", e.getMessage(), e);
    }
  }

  protected <T extends ClientReference> void readAny(final ResultSet rs,
      NeutronJdbcReader<T> reader, BiConsumer<RawClient, T> organizer, String msg) {
    int counter = 0;
    RawClient c = null;
    T t;

    try {
      while (rocket.isRunning() && rs.next() && (t = reader.read(rs)) != null) {
        // Find associated raw client, if any, and link.
        c = rawClients.get(t.getCltId());
        organizer.accept(c, t); // NOT WORKING??
        CheeseRay.logEvery(LOGGER, 5000, ++counter, "Read", msg);
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ DATA! {}", e.getMessage(), e);
    }

    LOGGER.info("{} {} recs retrieved", msg, counter);
  }

  protected void readClient(final ResultSet rs) {
    // DOESN'T WORK. :-(
    // Isn't this **logically identical** to the boilerplate code below??
    // Java should evaluate the lambda expression for each record, just like an interface
    // implementation, but it doesn't. It evaluates the expression only once. Why??

    // try {
    // final NeutronJdbcReader<RawClient> reader = new RawClient().read(rs);
    // final BiConsumer<RawClient, RawClient> organizer =
    // (ignore, c) -> rawClients.put(c.getCltId(), c);
    // readAny(rs, reader, organizer, "client");
    // } catch (Exception e) {
    // throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ CLIENT! {}", e.getMessage(), e);
    // }

    int counter = 0;
    RawClient c = null;

    try {
      while (rocket.isRunning() && rs.next() && (c = new RawClient().read(rs)) != null) {
        rawClients.put(c.getCltId(), c);
        CheeseRay.logEvery(LOGGER, 5000, ++counter, "Read", "client");
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ CLIENT! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} client records.", counter);
  }

  protected void readClientAddress(final ResultSet rs) {
    int counter = 0;
    RawClient c = null;
    RawClientAddress cla = null;

    try {
      while (rocket.isRunning() && rs.next() && (cla = new RawClientAddress().read(rs)) != null) {
        c = rawClients.get(cla.getCltId());
        if (c != null) {
          c.addClientAddress(cla);
          CheeseRay.logEvery(LOGGER, 5000, ++counter, "Read", "client address");
        } else {
          LOGGER.warn("ORPHAN CLIENT ADDRESS! id: {}, client: {}", cla.getClaId(), cla.getCltId());
        }
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ CLIENT ADDRESS! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} client address records.", counter);
  }

  protected void readAddress(final ResultSet rs) {
    int counter = 0;
    RawAddress adr = null;
    RawClient c = null;

    try {
      while (rocket.isRunning() && rs.next() && (adr = new RawAddress().read(rs)) != null) {
        c = rawClients.get(adr.getCltId());
        if (c != null) {
          c.getClientAddress().get(adr.getClaId()).setAddress(adr);
          CheeseRay.logEvery(LOGGER, 5000, ++counter, "Read", "address");
        } else {
          LOGGER.warn("ORPHAN ADDRESS! id: {}, client: {}", adr.getAdrId(), adr.getCltId());
        }
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ ADDRESS! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} address records.", counter);
  }

  protected void readClientCounty(final ResultSet rs) {
    // Should work but doesn't.
    // readAny(rs, new RawClientCounty().read(rs), (c, cc) -> c.addClientCounty(cc),
    // "client county");

    int counter = 0;
    RawClient c = null;
    RawClientCounty cc = null;

    try {
      while (rocket.isRunning() && rs.next() && (cc = new RawClientCounty().read(rs)) != null) {
        c = rawClients.get(cc.getCltId());
        if (c != null) {
          c.addClientCounty(cc);
          CheeseRay.logEvery(LOGGER, 5000, ++counter, "Read", "client county");
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
    // Should work but doesn't.
    // readAny(rs, new RawAka().read(rs), (c, aka) -> c.addAka(aka), "aka");

    int counter = 0;
    RawClient c = null;
    RawAka aka = null;

    try {
      while (rocket.isRunning() && rs.next() && (aka = new RawAka().read(rs)) != null) {
        c = rawClients.get(aka.getCltId());
        if (c != null) {
          c.addAka(aka);
          CheeseRay.logEvery(LOGGER, 5000, ++counter, "Read", "aka");
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
    // Ditto.
    // readAny(rs, new RawCase().read(rs), (c, cas) -> c.addCase(cas), "case");

    int counter = 0;
    RawClient c = null;
    RawCase cas = null;

    try {
      while (rocket.isRunning() && rs.next() && (cas = new RawCase().read(rs)) != null) {
        c = rawClients.get(cas.getCltId());
        if (c != null) {
          c.addCase(cas);
          CheeseRay.logEvery(LOGGER, 5000, ++counter, "Read", "case");
        } else {
          LOGGER.warn("ORPHAN CASE! id: {}, client: {}", cas.getOpenCaseId(), cas.getCltId());
        }
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ CASE RECORD! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} case records.", counter);
  }

  protected void readCsec(final ResultSet rs) {
    // Ditto.
    // readAny(rs, new RawCsec().read(rs), (c, csec) -> c.addCsec(csec), "csec");

    int counter = 0;
    RawClient c = null;
    RawCsec csec = null;

    try {
      while (rocket.isRunning() && rs.next() && (csec = new RawCsec().read(rs)) != null) {
        c = rawClients.get(csec.getCltId());
        if (c != null) {
          c.addCsec(csec);
          CheeseRay.logEvery(LOGGER, 50, ++counter, "Read", "csec");
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
    // Ditto.
    // readAny(rs, new RawEthnicity().read(rs), (c, eth) -> c.addEthnicity(eth), "ethnicity");

    int counter = 0;
    RawClient c = null;
    RawEthnicity eth = null;

    try {
      while (rocket.isRunning() && rs.next() && (eth = new RawEthnicity().read(rs)) != null) {
        c = rawClients.get(eth.getCltId());
        if (c != null) {
          c.addEthnicity(eth);
          CheeseRay.logEvery(LOGGER, 5000, ++counter, "Read", "ethnicity");
        } else {
          LOGGER.warn("ORPHAN ETHNICITY! id: {}, client: {}", eth.getClientEthnicityId(),
              eth.getCltId());
        }
      }
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO READ ETHNICITY! {}", e.getMessage(), e);
    }

    LOGGER.info("Retrieved {} case records.", counter);
  }

  protected void readSafetyAlert(final ResultSet rs) {
    // Ditto.
    // readAny(rs, new RawSafetyAlert().read(rs), (c, saf) -> c.addSafetyAlert(saf), "safety
    // alert");

    int counter = 0;
    RawClient c = null;
    RawSafetyAlert saf = null;

    try {
      while (rocket.isRunning() && rs.next() && (saf = new RawSafetyAlert().read(rs)) != null) {
        c = rawClients.get(saf.getCltId());
        if (c != null) {
          c.addSafetyAlert(saf);
          CheeseRay.logEvery(LOGGER, 5000, ++counter, "Read", "safety");
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
    // readClient(rs); // No longer.
    final int cntrRetrieved = rawClients.size();

    LOGGER.info("handleMainResults(): commit");
    con.commit(); // free database resources

    final FlightLog flightLog = getRocket().getFlightLog();
    flightLog.addToDenormalized(cntrRetrieved);
  }

  protected void loadClientRange(final PreparedStatement stmtInsClient, Pair<String, String> range)
      throws SQLException {
    // Initial Load client ranges.
    try {
      stmtInsClient.setString(1, range.getLeft());
      stmtInsClient.setString(2, range.getRight());
    } catch (Exception e) {
      LOGGER.trace("FAILED TO SET RANGES. Last change mode?", e);
    }

    final int clientCount = stmtInsClient.executeUpdate();
    LOGGER.debug("client count: {}", clientCount);
  }

  protected boolean isInitialLoad() {
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Read placement home addresses per rule R-02294, Client Abstract Most Recent Address.
   * </p>
   */
  @Override
  public void handleSecondaryJdbc(Connection con, Pair<String, String> range) throws SQLException {
    LOGGER.debug("handleSecondaryJdbc(): begin");
    String sqlPlacementAddress;
    try {
      sqlPlacementAddress = NeutronDB2Utils.prepLastChangeSQL(SEL_PLACEMENT_ADDR,
          rocket.determineLastSuccessfulRunTime(), rocket.getFlightPlan().getOverrideLastEndTime());
      LOGGER.trace("SQL for Placement Address:\n{}", sqlPlacementAddress);
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "INVALID PLACEMENT ADDRESS SQL! {}", e.getMessage(), e);
    }

    try (
        final PreparedStatement stmtInsClient =
            con.prepareStatement(pickPrepDml(INS_CLIENT_RANGE, INS_CLIENT_LAST_CHG));
        final PreparedStatement stmtInsClientPlaceHome =
            con.prepareStatement(pickPrepDml(INS_PLACEMENT_CLIENT_FULL, INSERT_CLIENT_DUMMY));
        final PreparedStatement stmtSelPlacementAddress =
            con.prepareStatement(sqlPlacementAddress, TFO, CRO);
        final PreparedStatement stmtSelClient = con.prepareStatement(SELECT_CLIENT, TFO, CRO);
        final PreparedStatement stmtSelClientAddress =
            con.prepareStatement(SEL_CLIENT_ADDR, TFO, CRO);
        final PreparedStatement stmtSelClientCounty =
            con.prepareStatement(SEL_CLI_COUNTY, TFO, CRO);
        final PreparedStatement stmtSelAddress = con.prepareStatement(SELECT_ADDRESS, TFO, CRO);
        final PreparedStatement stmtSelAka = con.prepareStatement(SELECT_AKA, TFO, CRO);
        final PreparedStatement stmtSelCase = con.prepareStatement(SELECT_CASE, TFO, CRO);
        final PreparedStatement stmtSelCsec = con.prepareStatement(SELECT_CSEC, TFO, CRO);
        final PreparedStatement stmtSelEthnicity = con.prepareStatement(SELECT_ETHNICITY, TFO, CRO);
        final PreparedStatement stmtSelSafety = con.prepareStatement(SELECT_SAFETY, TFO, CRO)) {

      // Commit more often by re-inserting client id's into GT_ID.
      // Initial Load client ranges:
      loadClientRange(stmtInsClient, range);

      LOGGER.info("Read client");
      read(stmtSelClient, rs -> readClient(rs));

      LOGGER.info("Read client address");
      read(stmtSelClientAddress, rs -> readClientAddress(rs));

      LOGGER.info("Read address");
      read(stmtSelAddress, rs -> readAddress(rs));

      // SNAP-731: missing addresses.
      if (isInitialLoad()) {
        con.commit(); // clear temp tables.
        loadClientRange(stmtInsClient, range); // Insert client id's again.
      }

      LOGGER.info("Read client county");
      read(stmtSelClientCounty, rs -> readClientCounty(rs));

      LOGGER.info("Read aka");
      read(stmtSelAka, rs -> readAka(rs));

      LOGGER.info("Read case");
      read(stmtSelCase, rs -> readCase(rs));

      LOGGER.info("Read csec");
      read(stmtSelCsec, rs -> readCsec(rs));

      LOGGER.info("Read ethnicity");
      read(stmtSelEthnicity, rs -> readEthnicity(rs));

      LOGGER.info("Read safety alert");
      read(stmtSelSafety, rs -> readSafetyAlert(rs));
      con.commit(); // clear again

      LOGGER.info("Insert placement home clients");
      prepPlacementClients(stmtInsClient, range);
      prepPlacementClients(stmtInsClientPlaceHome, range);

      LOGGER.info("Read placement home address");
      readPlacementAddress(stmtSelPlacementAddress);
      con.commit(); // and clear again. Make DBA's happy.
    } catch (Exception e) {
      getRocket().getFlightLog().markRangeError(range); // Fail the BUCKET, NOT the WHOLE FLIGHT!
      try {
        con.rollback();
      } catch (Exception e2) {
        LOGGER.trace("NESTED ROLLBACK EXCEPTION!", e2);
      }
      throw CheeseRay.runtime(LOGGER, e, "SECONDARY JDBC FAILED! {}", e.getMessage(), e);
    }

    LOGGER.debug("handleSecondaryJdbc(): DONE");
  }

  protected void mapReplicatedClient(PlacementHomeAddress pha) {
    if (normalized.containsKey(pha.getClientId())) {
      final ReplicatedClient rc = normalized.get(pha.getClientId());
      rc.setActivePlacementHomeAddress(pha);
    } else {
      // WARNING: last chg: if the client wasn't picked up from the view, then it's not here.
      LOGGER.warn("Client id for placement home address not in normalized map! client id: {}",
          pha.getClientId());
    }
  }

  @Override
  public void handleJdbcDone(final Pair<String, String> range) {
    // LOGGER.info("client address count: {}", );
    final RawToEsConverter conv = new RawToEsConverter();
    this.rawClients.values().stream().map(r -> r.normalize(conv))
        .forEach(c -> normalized.put(c.getId(), c));
    rawClients.clear(); // free memory
    LOGGER.debug("handleJdbcDone: normalized.size(): {}", normalized.size());

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

  @Override
  public void handleFinishRange(Pair<String, String> range) {
    doneThreadRetrieve();
    clear();
    freeMemory();
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
    try (final Session session = getRocket().getJobDao().grabSession();
        final Connection con = NeutronJdbcUtils.prepConnection(session);
        final PreparedStatement stmtSelPlacementAddress =
            con.prepareStatement(ClientSQLResource.SEL_PLACEMENT_ADDR)) {
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
      handleFinishRange(range);
      rocket.getFlightLog().markRangeComplete(range);
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
    final String sql =
        getRocket().getFlightPlan().isLastRunMode()
            ? NeutronDB2Utils.prepLastChangeSQL(sqlLastChange,
                getRocket().determineLastSuccessfulRunTime(),
                getRocket().getFlightPlan().getOverrideLastEndTime())
            : sqlInitialLoad; // initial mode
    LOGGER.trace("Prep SQL: \n{}", sql);
    return sql;
  }

  public void addAll(Collection<ReplicatedClient> collection) {
    if (!collection.isEmpty()) {
      if (normalized.size() < collection.size()) {
        this.normalized = new HashMap<String, ReplicatedClient>(collection.size());
      }
      collection.stream().forEach(c -> normalized.put(c.getId(), c));
    }
  }

  /**
   * Release unneeded heap memory early and often.
   */
  protected void clear() {
    LOGGER.debug("clear containers");
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
        .map(e -> getRocket().normalizeSingle(e.getValue()))
        .forEach(n -> normalized.put(n.getId(), n));
  }

  protected void prepPlacementClients(final PreparedStatement stmt, final Pair<String, String> p)
      throws SQLException {
    stmt.setMaxRows(0);
    stmt.setQueryTimeout(QUERY_TIMEOUT_IN_SECONDS.getValue()); // SNAP-709
    stmt.setFetchSize(FETCH_SIZE.getValue());

    if (!getRocket().getFlightPlan().isLastRunMode()) {
      LOGGER.info("Prep Affected Clients: range: {} - {}", p.getLeft(), p.getRight());
      try {
        stmt.setString(1, p.getLeft());
        stmt.setString(2, p.getRight());
      } catch (Exception e) {
        LOGGER.trace("FAILED TO SET PARAMS. Last change mode?", e);
      }
    }

    final int countInsClient = stmt.executeUpdate();
    LOGGER.info("Placement home clients: {}", countInsClient);
  }

  protected void readPlacementAddress(final PreparedStatement stmt) throws SQLException {
    stmt.setMaxRows(0);
    stmt.setQueryTimeout(QUERY_TIMEOUT_IN_SECONDS.getValue()); // SNAP-709
    stmt.setFetchSize(FETCH_SIZE.getValue());

    int cntr = 0;
    PlacementHomeAddress pha;

    // SNAP-709: Connection is closed. ERRORCODE=-4470, SQLSTATE=08003.
    // According to the JDBC specification, you don't
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

}
