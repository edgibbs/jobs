package gov.ca.cwds.neutron.rocket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.data.persistence.cms.EsClientPerson;
import gov.ca.cwds.data.persistence.cms.PlacementHomeAddress;
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
 * {@link AtomLoadStepHandler} for the People Summary getRocket().
 * 
 * <p>
 * Loads {@link EsClientPerson} and {@link PlacementHomeAddress}, normalizes to
 * {@link ReplicatedClient}.
 * </p>
 * 
 * @author CWDS API Team
 */
@SuppressWarnings({"findsecbugs:SQL_INJECTION_JDBC"})
public class PeopleSummaryThreadHandler
    implements ApiMarker, AtomLoadStepHandler<ReplicatedClient> {

  private static final long serialVersionUID = 1L;

  protected static final Logger LOGGER = LoggerFactory.getLogger(PeopleSummaryThreadHandler.class);

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
  protected Map<String, ReplicatedClient> normalized = new HashMap<>(300119);

  public PeopleSummaryThreadHandler(ClientPersonIndexerJob rocket) {
    this.rocket = rocket;
  }

  @Override
  public void handleMainResults(ResultSet rs) throws SQLException {
    int cntr = 0;
    EsClientPerson m;
    Object lastId = new Object();
    final List<EsClientPerson> grpRecs = new ArrayList<>(50);
    final FlightLog flightLog = getRocket().getFlightLog();

    // NOTE: Assumes that records are sorted by group key.
    while (!rocket.isFailed() && rs.next() && (m = rocket.extract(rs)) != null) {
      CheeseRay.logEvery(LOGGER, ++cntr, "Retrieved", "recs");
      if (!lastId.equals(m.getNormalizationGroupKey()) && cntr > 1) {
        normalize(grpRecs);
        grpRecs.clear(); // Single thread, re-use memory.
      }

      grpRecs.add(m);
      lastId = m.getNormalizationGroupKey();
    }

    flightLog.addToDenormalized(cntr);
    LOGGER.info("Counts: normalized: {}, de-normalized: {}", normalized.size(), cntr);
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
    String sqlPlacementAddress;
    try {
      sqlPlacementAddress = NeutronDB2Utils.prepLastChangeSQL(
          ClientSQLResource.SELECT_PLACEMENT_ADDRESS, rocket.determineLastSuccessfulRunTime(),
          rocket.getFlightPlan().getOverrideLastEndTime());
      LOGGER.info("SQL for Placement Address:\n{}", sqlPlacementAddress);
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "INVALID PLACEMENT ADDRESS SQL! {}", e.getMessage(), e);
    }

    try (
        final PreparedStatement stmtInsClient =
            con.prepareStatement(pickPrepDml(ClientSQLResource.INSERT_CLIENT_FULL,
                ClientSQLResource.INSERT_CLIENT_LAST_CHG));
        final PreparedStatement stmtInsClientPlacementHome =
            con.prepareStatement(pickPrepDml(ClientSQLResource.INSERT_PLACEMENT_HOME_CLIENT_FULL,
                ClientSQLResource.INSERT_NEXT_BUNDLE));
        final PreparedStatement stmtSelPlacementAddress =
            con.prepareStatement(sqlPlacementAddress)) {
      prepAffectedClients(stmtInsClient, range);
      prepAffectedClients(stmtInsClientPlacementHome, range);
      readPlacementAddress(stmtSelPlacementAddress);
    } catch (Exception e) {
      rocket.fail();
      try {
        con.rollback();
      } catch (Exception e2) {
        LOGGER.trace("NESTED EXCEPTION", e2);
      }
      throw CheeseRay.runtime(LOGGER, e, "SECONDARY JDBC FAILED! {}", e.getMessage(), e);
    }
  }

  protected void mapReplicatedClient(PlacementHomeAddress pha) {
    if (normalized.containsKey(pha.getClientId())) {
      final ReplicatedClient rc = normalized.get(pha.getClientId());
      rc.setActivePlacementHomeAddress(pha);
    } else {
      // WARNING: last chg: if the client wasn't picked up from the view, then it's not here.
      LOGGER.warn("Placement home address not in normalized map! client id: {}", pha.getClientId());
    }
  }

  @Override
  public void handleJdbcDone(final Pair<String, String> range) {
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
  }

  @Override
  public List<ReplicatedClient> getResults() {
    return normalized.values().stream().collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   * 
   * DB2 doesn't deal well with large sets of keys. Split lists of changed keys
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
            con.prepareStatement(ClientSQLResource.SELECT_PLACEMENT_ADDRESS)) {
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

  protected String pickPrepDml(String sqlInitialLoad, String sqlLastChange)
      throws NeutronCheckedException {
    final String preparedSql =
        getRocket().getFlightPlan().isLastRunMode()
            ? NeutronDB2Utils.prepLastChangeSQL(sqlLastChange,
                getRocket().determineLastSuccessfulRunTime(),
                getRocket().getFlightPlan().getOverrideLastEndTime())
            : sqlInitialLoad; // initial mode
    LOGGER.info("Prep SQL: \n{}", preparedSql);
    return preparedSql;
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
    placementHomeAddresses.clear();
  }

  /**
   * Normalize records from MQT/view for same client id.
   * 
   * @param grpRecs recs for same client id
   */
  protected void normalize(final List<EsClientPerson> grpRecs) {
    grpRecs.stream().sorted((e1, e2) -> e1.compare(e1, e2)).sequential().sorted()
        .collect(Collectors.groupingBy(EsClientPerson::getNormalizationGroupKey)).entrySet()
        .stream().map(e -> getRocket().normalizeSingle(e.getValue()))
        .forEach(n -> normalized.put(n.getId(), n));
  }

  protected void prepAffectedClients(final PreparedStatement stmt, final Pair<String, String> p)
      throws SQLException {
    stmt.setMaxRows(0);
    stmt.setQueryTimeout(115); // SNAP-709: just shy of 2 minute timeout

    if (!getRocket().getFlightPlan().isLastRunMode()) {
      LOGGER.info("Prep Affected Clients: range: {} - {}", p.getLeft(), p.getRight());
      stmt.setString(1, p.getLeft());
      stmt.setString(2, p.getRight());
    }

    final int countInsClient = stmt.executeUpdate();
    LOGGER.info("affected clients: {}", countInsClient);
  }

  protected void readPlacementAddress(final PreparedStatement stmt) throws SQLException {
    stmt.setMaxRows(0);
    stmt.setQueryTimeout(115); // SNAP-709: just shy of 2 minute timeout
    stmt.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());
    int cntr = 0;
    PlacementHomeAddress pha;

    // SNAP-709: Connection is closed. ERRORCODE=-4470, SQLSTATE=08003.
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
