package gov.ca.cwds.jobs;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedRelationshipsDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.EsRelationship;
import gov.ca.cwds.data.persistence.cms.RelationshipTypeCache;
import gov.ca.cwds.data.persistence.cms.ReplicatedRelationships;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomRowMapper;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.rocket.InitialLoadJdbcRocket;
import gov.ca.cwds.neutron.util.NeutronThreadUtils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;
import gov.ca.cwds.neutron.util.transform.EntityNormalizer;

/**
 * Rocket loads family relationships from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class RelationshipIndexerJob
    extends InitialLoadJdbcRocket<ReplicatedRelationships, EsRelationship>
    implements AtomRowMapper<EsRelationship> {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(RelationshipIndexerJob.class);

  /**
   * SQL to insert last changed client list into global temporary table for use in views,
   * VW_LST_BI_DIR_RELATION and VW_LST_REL_CLN_RELT_CLIENT.
   */
  // @formatter:off
  static final String INSERT_RELATION_LAST_CHG = 
      "INSERT INTO GT_ID (IDENTIFIER)\n"
          + "WITH LAST_CHG AS (\n"
             + " SELECT DISTINCT CLNR.IDENTIFIER AS REL_ID\n"
             + " FROM CLN_RELT CLNR \n"
             + " WHERE CLNR.IBMSNAP_LOGMARKER > 'XYZ' \n"
           + " UNION \n"
             + " SELECT DISTINCT CLNR.IDENTIFIER AS REL_ID \n"
             + " FROM CLN_RELT CLNR\n"
             + " JOIN CLIENT_T CLNS ON CLNR.FKCLIENT_T = CLNS.IDENTIFIER\n"
             + " WHERE CLNS.IBMSNAP_LOGMARKER > 'XYZ' \n"
           + " UNION \n"
             + " SELECT DISTINCT CLNR.IDENTIFIER AS REL_ID\n"
             + " FROM CLN_RELT CLNR\n"
             + " JOIN CLIENT_T CLNP ON CLNR.FKCLIENT_0 = CLNP.IDENTIFIER\n"
             + " WHERE CLNP.IBMSNAP_LOGMARKER > 'XYZ' \n"
             + "),\n"
         + "CHG_CLIENTS AS (\n"
             + " SELECT DISTINCT CLNP.IDENTIFIER AS CLIENT_ID\n"
             + " FROM LAST_CHG LC\n"
             + " JOIN CLN_RELT CLNR ON CLNR.IDENTIFIER = LC.REL_ID\n"
             + " JOIN CLIENT_T CLNP ON CLNR.FKCLIENT_0 = CLNP.IDENTIFIER\n"
           + " UNION\n"
             + " SELECT DISTINCT CLNS.IDENTIFIER AS CLIENT_ID\n"
             + " FROM LAST_CHG LC\n"
             + " JOIN CLN_RELT CLNR ON CLNR.IDENTIFIER = LC.REL_ID\n"
             + " JOIN CLIENT_T CLNS ON CLNR.FKCLIENT_T = CLNS.IDENTIFIER\n"
             + ")\n"
        + "SELECT chg.CLIENT_ID FROM CHG_CLIENTS chg\n";  
  // @formatter:on

  private AtomicInteger nextThreadNum = new AtomicInteger(0);

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao Relationship View DAO
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run file
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   */
  @Inject
  public RelationshipIndexerJob(final ReplicatedRelationshipsDao dao,
      @Named("elasticsearch.dao.people") final ElasticsearchDao esDao,
      @LastRunFile String lastRunFile, final ObjectMapper mapper, FlightPlan flightPlan) {
    super(dao, esDao, lastRunFile, mapper, flightPlan);
    RelationshipTypeCache.getInstance().clearCache();
  }

  @Override
  public String getPrepLastChangeSQL() {
    try {
      return INSERT_RELATION_LAST_CHG.replaceAll("XYZ",
          NeutronJdbcUtils.makeTimestampStringLookBack(determineLastSuccessfulRunTime()));
    } catch (NeutronCheckedException e) {
      throw CheeseRay.runtime(LOGGER, e, "ERROR BUILDING LAST CHANGE SQL: {}", e.getMessage());
    }
  }

  @Override
  public String getInitialLoadViewName() {
    return "VW_MQT_BI_DIR_RELATION";
  }

  @Override
  public EsRelationship extract(ResultSet rs) throws SQLException {
    return EsRelationship.mapRow(rs);
  }

  @Override
  public String getJdbcOrderBy() {
    return " ORDER BY THIS_LEGACY_ID, RELATED_LEGACY_ID ";
  }

  @Override
  public Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return EsRelationship.class;
  }

  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    final StringBuilder buf = new StringBuilder();
    buf.append("SELECT x.REVERSE_RELATIONSHIP, x.THIS_LEGACY_ID, x.THIS_SENSITIVITY_IND, ")
        .append("x.THIS_FIRST_NAME, x.THIS_LAST_NAME, ")
        .append("x.THIS_LEGACY_LAST_UPDATED, x.THIS_LEGACY_LAST_UPDATED_ID, ")
        .append("x.REL_CODE, x.RELATED_LEGACY_ID, x.RELATED_SENSITIVITY_IND, ")
        .append("x.RELATED_LEGACY_LAST_UPDATED, x.RELATED_LEGACY_LAST_UPDATED_ID, ")
        .append("x.RELATED_FIRST_NAME, x.RELATED_LAST_NAME, ")
        .append("x.REL_IBMSNAP_LOGMARKER, x.REL_IBMSNAP_OPERATION, ")
        .append("x.THIS_IBMSNAP_LOGMARKER, x.THIS_IBMSNAP_OPERATION, ")
        .append("x.RELATED_IBMSNAP_LOGMARKER, x.RELATED_IBMSNAP_OPERATION, x.LAST_CHG")
        .append(" FROM ").append(dbSchemaName).append('.').append(getInitialLoadViewName())
        .append(" x WHERE x.THIS_LEGACY_ID BETWEEN ':fromId' AND ':toId' ");

    if (!getFlightPlan().isLoadSealedAndSensitive()) {
      buf.append(" AND x.THIS_SENSITIVITY_IND = 'N' AND x.RELATED_SENSITIVITY_IND = 'N' ");
    }

    buf.append(getJdbcOrderBy()).append(" FOR READ ONLY WITH UR ");
    return buf.toString();
  }

  /**
   * Send all records for the same group id to the index queue.
   * 
   * @param grpRecs records for same client id
   */
  protected void normalizeAndQueueIndex(final List<EsRelationship> grpRecs) {
    grpRecs.stream().sorted((e1, e2) -> e1.compare(e1, e2)).filter(EsRelationship::isActive)
        .sequential().sorted().collect(Collectors.groupingBy(EsRelationship::getThisLegacyId))
        .entrySet().stream().map(e -> normalizeSingle(e.getValue())).forEach(this::addToIndexQueue);
  }

  /**
   * Iterate results sets from {@link #pullRange(Pair, String)}.
   * 
   * @param rs result set
   * @throws SQLException on database error
   */
  @Override
  public void handleMainResults(final ResultSet rs) throws SQLException {
    int cntr = 0;
    EsRelationship m;
    Object lastId = new Object();
    final List<EsRelationship> grpRecs = new ArrayList<>();

    // NOTE: Assumes that records are sorted by group key.
    while (!isFailed() && rs.next() && (m = extract(rs)) != null) {
      CheeseRay.logEvery(LOGGER, ++cntr, "Retrieved", "recs");
      if (!lastId.equals(m.getNormalizationGroupKey()) && cntr > 1) {
        normalizeAndQueueIndex(grpRecs);
        grpRecs.clear(); // Single thread, re-use memory.
      }

      grpRecs.add(m);
      lastId = m.getNormalizationGroupKey();
    }
  }

  @Override
  public int nextThreadNumber() {
    return nextThreadNum.incrementAndGet();
  }

  /**
   * The "extract" part of ETL. Single producer, chained consumers. This rocket normalizes
   * <strong>without</strong> the transform thread.
   */
  @Override
  protected void threadRetrieveByJdbc() {
    nameThread("relationship_extract");
    LOGGER.info("BEGIN: main extract thread");
    doneTransform(); // No transform thread

    RelationshipTypeCache.getInstance().clearCache();
    try {
      final List<Pair<String, String>> ranges = getPartitionRanges();
      LOGGER.info(">>>>>>>> # OF RANGES: {} <<<<<<<<", ranges);
      final List<ForkJoinTask<?>> tasks = new ArrayList<>(ranges.size());
      final ForkJoinPool threadPool =
          new ForkJoinPool(NeutronThreadUtils.calcReaderThreads(getFlightPlan()));

      // Queue execution.
      for (Pair<String, String> p : ranges) {
        tasks.add(threadPool.submit(() -> pullRange(p, null)));
      }

      // Join threads. Don't return from method until they complete.
      for (ForkJoinTask<?> task : tasks) {
        task.get();
      }

    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(LOGGER, e, "RELATIONSHIP BATCH ERROR! {}", e.getMessage());
    } finally {
      doneRetrieve();
      RelationshipTypeCache.getInstance().clearCache();
    }

    LOGGER.info("DONE: relationship extract thread");
  }

  // =====================
  // FIXED SPECS:
  // =====================

  @Override
  public boolean useTransformThread() {
    return false;
  }

  @Override
  public boolean isInitialLoadJdbc() {
    return true;
  }

  @Override
  public List<Pair<String, String>> getPartitionRanges() throws NeutronCheckedException {
    return NeutronJdbcUtils.getCommonPartitionRanges64(this);
  }

  @Override
  public String getOptionalElementName() {
    return "relationships";
  }

  // =====================
  // TRANSFORM:
  // =====================

  @Override
  protected UpdateRequest prepareUpsertRequest(ElasticSearchPerson esp, ReplicatedRelationships p)
      throws NeutronCheckedException {
    return prepareUpdateRequest(esp, p, p.getRelations(), true);
  }

  @Override
  public ReplicatedRelationships normalizeSingle(List<EsRelationship> recs) {
    return !recs.isEmpty() ? normalize(recs).get(0) : null;
  }

  @Override
  public List<ReplicatedRelationships> normalize(List<EsRelationship> recs) {
    return EntityNormalizer.<ReplicatedRelationships, EsRelationship>normalizeList(recs);
  }

  @Override
  public synchronized void close() throws IOException {
    RelationshipTypeCache.getInstance().clearCache();
    super.close();
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(RelationshipIndexerJob.class, args);
  }

}
