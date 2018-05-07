package gov.ca.cwds.neutron.rocket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.data.persistence.cms.EsClientPerson;
import gov.ca.cwds.data.persistence.cms.PlacementHomeAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.jobs.ClientPersonIndexerJob;
import gov.ca.cwds.neutron.atom.AtomRangeHandler;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.jetpack.CheeseRay;

/**
 * {@link AtomRangeHandler} for the People Summary rocket.
 * 
 * <p>
 * Loads {@link EsClientPerson} and {@link PlacementHomeAddress}, normalizes to
 * {@link ReplicatedClient}.
 * </p>
 * 
 * @author CWDS API Team
 */
public class PeopleSummaryThreadHandler implements ApiMarker, AtomRangeHandler {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(PeopleSummaryThreadHandler.class);

  private final ClientPersonIndexerJob rocket;

  private final Map<String, PlacementHomeAddress> placementHomeAddresses; // key = client id

  private final Map<String, ReplicatedClient> normalized;

  public PeopleSummaryThreadHandler(ClientPersonIndexerJob rocket) {
    final boolean isLargeLoad = rocket.isLargeLoad();

    this.rocket = rocket;
    this.normalized = isLargeLoad ? new HashMap<>(20000) : new HashMap<>(5000);
    this.placementHomeAddresses = isLargeLoad ? new HashMap<>(2000) : new HashMap<>(200);
  }

  protected void clear() {
    normalized.clear();
    placementHomeAddresses.clear();
  }

  /**
   * Normalize all recs for same client id.
   * 
   * @param grpRecs recs for same client id
   */
  protected void normalize(final List<EsClientPerson> grpRecs) {
    grpRecs.stream().sorted((e1, e2) -> e1.compare(e1, e2)).sequential().sorted()
        .collect(Collectors.groupingBy(EsClientPerson::getNormalizationGroupKey)).entrySet()
        .stream().map(e -> rocket.normalizeSingle(e.getValue()))
        .forEach(n -> normalized.put(n.getId(), n));
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

  @Override
  public void handleMainResults(ResultSet rs) throws SQLException {
    int cntr = 0;
    EsClientPerson m;
    Object lastId = new Object();
    final List<EsClientPerson> grpRecs = new ArrayList<>();
    final FlightLog flightLog = rocket.getFlightLog();

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
    LOGGER.info("Normalized count: {}, de-normalized count: {}", normalized.size(), cntr);
  }

  @Override
  public void handleSecondaryJdbc(Connection con, Pair<String, String> range) throws SQLException {
    try (
        final PreparedStatement stmtInsClient =
            con.prepareStatement(ClientSQLResource.INSERT_PLACEHOME_CLIENT_FULL);
        final PreparedStatement stmtSelPlacementAddress =
            con.prepareStatement(ClientSQLResource.SELECT_PLACEMENT_ADDRESS)) {
      prepAffectedClients(stmtInsClient, range);
      readPlacementAddress(stmtSelPlacementAddress);
    } finally {
      // NOTE: AtomInitialLoad.pullRange() releases database resources.
    }
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

  @Override
  public void afterJdbc(final Pair<String, String> p) {
    final FlightLog flightLog = rocket.getFlightLog();
    // TODO: Merge placement home addresses HERE.

    // Send to Elasticsearch.
    normalized.values().stream().forEach(rocket::addToIndexQueue);
    flightLog.doneRetrieve();
  }

  @Override
  public void startRange(Pair<String, String> p) {
    clear();
  }

  @Override
  public void finishRange(Pair<String, String> p) {
    clear();
  }

}
