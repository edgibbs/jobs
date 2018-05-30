package gov.ca.cwds.jobs;

import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.Table;

import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedAkaDao;
import gov.ca.cwds.dao.cms.ReplicatedOtherClientNameDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.ReplicatedAkas;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherClientName;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomRowMapper;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;
import gov.ca.cwds.neutron.util.transform.EntityNormalizer;

/**
 * Rocket to load Other Client Name from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class OtherClientNameIndexerJob
    extends BasePersonRocket<ReplicatedAkas, ReplicatedOtherClientName>
    implements AtomRowMapper<ReplicatedOtherClientName> {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(OtherClientNameIndexerJob.class);

  //@formatter:off
  private static final String INSERT_CLIENT_LAST_CHG =
      "INSERT INTO GT_ID (IDENTIFIER)\n" 
    + "SELECT CLT.IDENTIFIER AS CLIENT_ID\n"
    + "FROM OCL_NM_T ONM\n" 
    + "JOIN CLIENT_T CLT ON CLT.IDENTIFIER = ONM.FKCLIENT_T\n"
    + "WHERE ONM.IBMSNAP_LOGMARKER > 'XYZ' \n" 
    + "UNION ALL\n" 
    + "SELECT CLT.IDENTIFIER\n"
    + "FROM CLIENT_T CLT \n"
    + "WHERE CLT.IBMSNAP_LOGMARKER > 'XYZ' ";
  //@formatter:on

  private transient ReplicatedOtherClientNameDao denormDao;

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao Relationship View DAO
   * @param denormDao de-normalized DAO
   * @param esDao ElasticSearch DAO
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   * @param launchDirector launch director
   */
  @Inject
  public OtherClientNameIndexerJob(final ReplicatedAkaDao dao,
      final ReplicatedOtherClientNameDao denormDao,
      @Named("elasticsearch.dao.people") final ElasticsearchDao esDao, final ObjectMapper mapper,
      FlightPlan flightPlan, AtomLaunchDirector launchDirector) {
    super(dao, esDao, flightPlan.getLastRunLoc(), mapper, flightPlan, launchDirector);
    this.denormDao = denormDao;
  }

  /**
   * DB2's optimizer is not very bright.
   */
  @Override
  public String getPrepLastChangeSQL() {
    try {
      return NeutronDB2Utils.prepLastChangeSQL(INSERT_CLIENT_LAST_CHG,
          determineLastSuccessfulRunTime(), null);
    } catch (NeutronCheckedException e) {
      throw CheeseRay.runtime(LOGGER, e, "ERROR BUILDING LAST CHANGE SQL: {}", e.getMessage());
    }
  }

  @Override
  public ReplicatedOtherClientName extract(ResultSet rs) throws SQLException {
    return ReplicatedOtherClientName.mapRowToBean(rs);
  }

  @Override
  public Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return ReplicatedOtherClientName.class;
  }

  @Override
  public ReplicatedAkas normalizeSingle(final List<ReplicatedOtherClientName> recs) {
    return recs != null && !recs.isEmpty() ? normalize(recs).get(0) : null;
  }

  @Override
  public List<ReplicatedAkas> normalize(final List<ReplicatedOtherClientName> recs) {
    return EntityNormalizer.<ReplicatedAkas, ReplicatedOtherClientName>normalizeList(recs);
  }

  @Override
  public String getDriverTable() {
    String ret = null;
    final Table tbl = this.denormDao.getEntityClass().getDeclaredAnnotation(Table.class);
    if (tbl != null) {
      ret = tbl.name();
    }

    return ret;
  }

  @Override
  public String getOptionalElementName() {
    return "akas";
  }

  @Override
  protected UpdateRequest prepareUpsertRequest(ElasticSearchPerson esp, ReplicatedAkas p)
      throws NeutronCheckedException {
    return prepareUpdateRequest(esp, p, p.getAkas(), true);
  }

  @Override
  public String getInitialLoadViewName() {
    return "MQT_OTHER_CLIENT_NAME";
  }

  /**
   * Optional method to customize JDBC ORDER BY clause on initial load.
   * 
   * @return custom ORDER BY clause for JDBC query
   */
  @Override
  public String getJdbcOrderBy() {
    return " ORDER BY x.FKCLIENT_T ";
  }

  @Override
  public boolean isInitialLoadJdbc() {
    return true;
  }

  @Override
  public List<Pair<String, String>> getPartitionRanges() throws NeutronCheckedException {
    return NeutronJdbcUtils.getCommonPartitionRanges16(this);
  }

  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    final StringBuilder buf = new StringBuilder();
    buf.append("SELECT x.* FROM ").append(dbSchemaName).append('.').append(getInitialLoadViewName())
        .append(" x ");

    if (!getFlightPlan().isLoadSealedAndSensitive()) {
      buf.append(" WHERE x.CLIENT_SENSITIVITY_IND = 'N' ");
    }

    buf.append(getJdbcOrderBy()).append(" FOR READ ONLY WITH UR ");
    return buf.toString();
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(OtherClientNameIndexerJob.class, args);
  }

}
