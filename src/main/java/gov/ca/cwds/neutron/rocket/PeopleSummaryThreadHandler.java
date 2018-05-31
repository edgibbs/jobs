package gov.ca.cwds.neutron.rocket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
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
 * {@link AtomLoadStepHandler} for the People Summary rocket.
 * 
 * <p>
 * Loads {@link EsClientPerson} and {@link PlacementHomeAddress}, normalizes to
 * {@link ReplicatedClient}.
 * </p>
 * 
 * @author CWDS API Team
 */
public class PeopleSummaryThreadHandler
    implements ApiMarker, AtomLoadStepHandler<ReplicatedClient> {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(PeopleSummaryThreadHandler.class);

  private final ClientPersonIndexerJob rocket;

  private boolean doneHandlerRetrieve = false;

  /**
   * key = client id
   */
  private final Map<String, PlacementHomeAddress> placementHomeAddresses;

  /**
   * key = client id
   */
  private final Map<String, ReplicatedClient> normalized;

  public PeopleSummaryThreadHandler(ClientPersonIndexerJob rocket) {
    final boolean isLargeLoad = rocket.isLargeLoad();

    this.rocket = rocket;
    this.normalized = isLargeLoad ? new LinkedHashMap<>(150011) : new LinkedHashMap<>(20011);
    this.placementHomeAddresses = isLargeLoad ? new HashMap<>(5011) : new HashMap<>(2003);
  }

  @Override
  public void handleMainResults(ResultSet rs) throws SQLException {
    int cntr = 0;
    EsClientPerson m;
    Object lastId = new Object();
    final List<EsClientPerson> grpRecs = new ArrayList<>();
    final FlightLog flightLog = rocket.getFlightLog();

    // NOTE: Assumes that records are sorted by group key.
    while (!rocket.isFailed() && rs.next() && (m = rocket.extract(rs)) != null) {
      CheeseRay.logEvery(LOGGER, 5000, ++cntr, "Retrieved", "recs");
      if (!lastId.equals(m.getNormalizationGroupKey()) && cntr > 1) {
        normalize(grpRecs);
        grpRecs.clear(); // Single thread, re-use memory.
      }

      grpRecs.add(m);
      lastId = m.getNormalizationGroupKey();
    }

    flightLog.addToDenormalized(cntr);
    LOGGER.info("Normalized count: {}, de-normalized count: {}", normalized.size(), cntr);
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
    try (
        final PreparedStatement stmtInsClient1 =
            con.prepareStatement(pickPrepDml(ClientSQLResource.INSERT_CLIENT_FULL,
                ClientSQLResource.INSERT_CLIENT_LAST_CHG));
        final PreparedStatement stmtInsClient2 =
            con.prepareStatement(pickPrepDml(ClientSQLResource.INSERT_PLACEMENT_HOME_CLIENT_FULL,
                ClientSQLResource.INSERT_PLACEMENT_HOME_CLIENT_LAST_CHG));
        final PreparedStatement stmtSelPlacementAddress =
            con.prepareStatement(ClientSQLResource.SELECT_PLACEMENT_ADDRESS)) {
      prepAffectedClients(stmtInsClient1, range);
      prepAffectedClients(stmtInsClient2, range);
      readPlacementAddress(stmtSelPlacementAddress);
    } catch (Exception e) {
      con.rollback();
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
    LOGGER.info("\nhandleJdbcDone: normalized.size(): {}\n", normalized.size());

    // Merge placement home addresses.
    placementHomeAddresses.values().stream().forEachOrdered(this::mapReplicatedClient);

    // Send to Elasticsearch.
    normalized.values().stream().forEach(rocket::addToIndexQueue);
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

  @Override
  public List<ReplicatedClient> fetchLastRunNormalizedResults(Date lastRunDate,
      Set<String> deletionResults) {
    final Pair<String, String> range = Pair.<String, String>of("a", "b"); // dummy range
    handleStartRange(range);

    // Read from the view, old school.
    addAll(rocket.extractLastRunRecsFromView(lastRunDate, deletionResults));
    LOGGER.info("After view: count: {}", normalized.size());

    // Handle additional JDBC statements, if any.
    try (Connection con = NeutronJdbcUtils.prepConnection(rocket.getJobDao().getSessionFactory())) {
      try {
        con.commit(); // clean up first
        handleSecondaryJdbc(con, range);
        con.commit(); // clear temp table
      } catch (Exception e) {
        con.rollback();
        throw e;
      }

      // Done reading data. Process data, like cleansing and normalizing.
      handleJdbcDone(range);
      final List<ReplicatedClient> ret = getResults();
      LOGGER.info("FETCHED LAST CHANGE RESULTS: count: {}", ret.size());
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
    final String preparedSql = rocket.getFlightPlan().isLastRunMode()
        ? NeutronDB2Utils.prepLastChangeSQL(sqlLastChange, rocket.determineLastSuccessfulRunTime(),
            rocket.getFlightPlan().getOverrideLastEndTime())
        : sqlInitialLoad; // initial mode
    LOGGER.info("Prep SQL: \n{}", preparedSql);
    return preparedSql;
  }

  public void addAll(Collection<ReplicatedClient> collection) {
    if (!collection.isEmpty()) {
      collection.stream().forEach(c -> normalized.put(c.getId(), c));
    }
  }

  protected void clear() {
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
        .stream().map(e -> rocket.normalizeSingle(e.getValue()))
        .forEach(n -> normalized.put(n.getId(), n));
    LOGGER.trace("normalized.size: {}", normalized.size());
  }

  protected void prepAffectedClients(final PreparedStatement stmtInsClient,
      final Pair<String, String> p) throws SQLException {
    stmtInsClient.setMaxRows(0);
    stmtInsClient.setQueryTimeout(0);

    if (!rocket.getFlightPlan().isLastRunMode()) {
      LOGGER.info("Prep Affected Clients: range: {} - {}", p.getLeft(), p.getRight());
      stmtInsClient.setString(1, p.getLeft());
      stmtInsClient.setString(2, p.getRight());
    }

    final int countInsClient = stmtInsClient.executeUpdate();
    LOGGER.info("affected clients: {}", countInsClient);
  }

  protected void readPlacementAddress(final PreparedStatement stmt) throws SQLException {
    LOGGER.info("read placement home address");
    stmt.setMaxRows(0);
    stmt.setQueryTimeout(0);
    stmt.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());

    PlacementHomeAddress pha;
    final ResultSet rs = stmt.executeQuery(); // NOSONAR
    while (!rocket.isFailed() && rs.next()) {
      pha = new PlacementHomeAddress(rs);
      placementHomeAddresses.put(pha.getClientId(), pha);
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

}