package gov.ca.cwds.jobs;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.update.UpdateRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedSafetyAlertsDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.EsSafetyAlert;
import gov.ca.cwds.data.persistence.cms.ReplicatedSafetyAlerts;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomRowMapper;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.rocket.InitialLoadJdbcRocket;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;
import gov.ca.cwds.neutron.util.transform.EntityNormalizer;

/**
 * Rocket to load safety alerts from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class SafetyAlertIndexerJob
    extends InitialLoadJdbcRocket<ReplicatedSafetyAlerts, EsSafetyAlert>
    implements AtomRowMapper<EsSafetyAlert> {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   * 
   * @param dao DAO
   * @param esDao ES SAO
   * @param lastRunFile Last runtime file
   * @param mapper Object mapper
   * @param flightPlan command line opts
   * @param launchDirector launch director
   */
  @Inject
  public SafetyAlertIndexerJob(ReplicatedSafetyAlertsDao dao,
      @Named("elasticsearch.dao.people") ElasticsearchDao esDao, @LastRunFile String lastRunFile,
      ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
  }

  @Override
  public Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return EsSafetyAlert.class;
  }

  @Override
  public String getInitialLoadViewName() {
    return "VW_LST_SAFETY_ALERT";
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
  public ReplicatedSafetyAlerts normalizeSingle(List<EsSafetyAlert> recs) {
    return recs != null && !recs.isEmpty() ? normalize(recs).get(0) : new ReplicatedSafetyAlerts();
  }

  @Override
  public List<ReplicatedSafetyAlerts> normalize(List<EsSafetyAlert> recs) {
    return EntityNormalizer.<ReplicatedSafetyAlerts, EsSafetyAlert>normalizeList(recs);
  }

  @Override
  public String getOptionalElementName() {
    return "safety_alerts";
  }

  @Override
  protected UpdateRequest prepareUpsertRequest(ElasticSearchPerson esp, ReplicatedSafetyAlerts p)
      throws NeutronCheckedException {
    return prepareUpdateRequest(esp, p, p.getSafetyAlerts(), true);
  }

  @Override
  public EsSafetyAlert extract(final ResultSet rs) throws SQLException {
    final EsSafetyAlert ret = new EsSafetyAlert();

    ret.setClientId(ifNull(rs.getString("CLIENT_ID")));
    ret.setAlertId(ifNull(rs.getString("ALERT_ID")));
    ret.setActivationCountyCode(rs.getInt("ACTIVATION_COUNTY_CD"));
    ret.setActivationDate(rs.getDate("ACTIVATION_DATE"));
    ret.setActivationExplanation(ifNull(rs.getString("ACTIVATION_EXPLANATION")));
    ret.setActivationReasonCode(rs.getInt("ACTIVATION_REASON_CD"));
    ret.setDeactivationCountyCode(rs.getInt("DEACTIVATION_COUNTY_CD"));
    ret.setDeactivationDate(rs.getDate("DEACTIVATION_DATE"));
    ret.setDeactivationExplanation(ifNull(rs.getString("DEACTIVATION_EXPLANATION")));
    ret.setLastUpdatedId(ifNull(rs.getString("LAST_UPDATED_ID")));
    ret.setLastUpdatedTimestamp(rs.getTimestamp("LAST_UPDATED_TS"));
    ret.setLastUpdatedOperation(ifNull(rs.getString("ALERT_IBMSNAP_OPERATION")));
    ret.setReplicationTimestamp(rs.getTimestamp("ALERT_IBMSNAP_LOGMARKER"));
    ret.setLastChanged(rs.getTimestamp("LAST_CHANGED"));

    return ret;
  }

  @Override
  public boolean isInitialLoadJdbc() {
    return true;
  }

  @Override
  public List<Pair<String, String>> getPartitionRanges() throws NeutronCheckedException {
    return NeutronJdbcUtils.getCommonPartitionRanges16(this);
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(SafetyAlertIndexerJob.class, args);
  }

}
