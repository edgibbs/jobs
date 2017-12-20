package gov.ca.cwds.jobs;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.ReplicatedPersonCasesDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.EsChildPersonCase;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;

/**
 * Rocket to load case history from CMS into ElasticSearch for 'focus child' person.
 * 
 * @author CWDS API Team
 */
public class ChildCaseHistoryIndexerJob extends CaseHistoryIndexerJob {

  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(ChildCaseHistoryIndexerJob.class);

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao Case history view DAO
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   */
  @Inject
  public ChildCaseHistoryIndexerJob(final ReplicatedPersonCasesDao dao,
      final ElasticsearchDao esDao, @LastRunFile final String lastRunFile,
      final ObjectMapper mapper, FlightPlan flightPlan) {
    super(dao, esDao, lastRunFile, mapper, flightPlan);
  }

  @Override
  public EsChildPersonCase extract(ResultSet rs) throws SQLException {
    final String caseId = rs.getString("CASE_ID");
    String focusChildId = rs.getString("FOCUS_CHILD_ID");

    if (focusChildId == null) {
      LOGGER.warn("FOCUS_CHILD_ID is null for CASE_ID: {}", caseId); // NOSONAR
      return null;
    }

    final EsChildPersonCase ret = new EsChildPersonCase();

    //
    // Case:
    //
    ret.setCaseId(caseId);
    ret.setStartDate(rs.getDate("START_DATE"));
    ret.setEndDate(rs.getDate("END_DATE"));
    ret.setCaseLastUpdated(rs.getTimestamp("CASE_LAST_UPDATED"));
    ret.setCounty(rs.getInt("COUNTY"));
    ret.setServiceComponent(rs.getInt("SERVICE_COMP"));

    //
    // Child (client):
    //
    ret.setFocusChildId(focusChildId);
    ret.setFocusChildFirstName(ifNull(rs.getString("FOCUS_CHLD_FIRST_NM")));
    ret.setFocusChildLastName(ifNull(rs.getString("FOCUS_CHLD_LAST_NM")));
    ret.setFocusChildLastUpdated(rs.getTimestamp("FOCUS_CHILD_LAST_UPDATED"));
    ret.setFocusChildSensitivityIndicator(rs.getString("FOCUS_CHILD_SENSITIVITY_IND"));

    //
    // Parent:
    //
    ret.setParentId(ifNull(rs.getString("PARENT_ID")));
    ret.setParentFirstName(ifNull(rs.getString("PARENT_FIRST_NM")));
    ret.setParentLastName(ifNull(rs.getString("PARENT_LAST_NM")));
    ret.setParentRelationship(rs.getInt("PARENT_RELATIONSHIP"));
    ret.setParentLastUpdated(rs.getTimestamp("PARENT_LAST_UPDATED"));
    ret.setParentSourceTable(rs.getString("PARENT_SOURCE_TABLE"));
    ret.setParentSensitivityIndicator(rs.getString("PARENT_SENSITIVITY_IND"));

    //
    // Worker (staff):
    //
    ret.getWorker().setWorkerId(ifNull(rs.getString("WORKER_ID")));
    ret.getWorker().setWorkerFirstName(ifNull(rs.getString("WORKER_FIRST_NM")));
    ret.getWorker().setWorkerLastName(ifNull(rs.getString("WORKER_LAST_NM")));
    ret.getWorker().setWorkerLastUpdated(rs.getTimestamp("WORKER_LAST_UPDATED"));

    //
    // Access Limitation:
    //
    ret.setLimitedAccessCode(ifNull(rs.getString("LIMITED_ACCESS_CODE")));
    ret.setLimitedAccessDate(rs.getDate("LIMITED_ACCESS_DATE"));
    ret.setLimitedAccessDescription(ifNull(rs.getString("LIMITED_ACCESS_DESCRIPTION")));
    ret.setLimitedAccessGovernmentEntityId(rs.getInt("LIMITED_ACCESS_GOVERNMENT_ENT"));
    return ret;
  }

  @Override
  public Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return EsChildPersonCase.class;
  }

  @Override
  public String getInitialLoadViewName() {
    return "MQT_CASE_HIST";
  }

  @Override
  public String getJdbcOrderBy() {
    return " ORDER BY FOCUS_CHILD_ID, CASE_ID, PARENT_ID ";
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception unhandled launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(ChildCaseHistoryIndexerJob.class, args);
  }
}
