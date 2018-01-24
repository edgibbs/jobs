package gov.ca.cwds.neutron.rocket;

import java.util.Date;

import javax.persistence.ParameterMode;

import org.hibernate.Session;
import org.hibernate.procedure.ProcedureCall;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.jobs.exception.NeutronException;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.jetpack.JobLogs;

/**
 * Refreshes a <strong>TEST</strong> transactional schema and its companion replicated schema.
 * 
 * @author CWDS API Team
 */
public class SchemaResetRocket
    extends BasePersonRocket<ReplicatedOtherAdultInPlacemtHome, ReplicatedOtherAdultInPlacemtHome> {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(SchemaResetRocket.class);

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao arbitrary DAO to fulfill interface
   * @param esDao ElasticSearch DAO for the target index
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   */
  @Inject
  public SchemaResetRocket(final ReplicatedOtherAdultInPlacemtHomeDao dao,
      final ElasticsearchDao esDao, final ObjectMapper mapper, FlightPlan flightPlan) {
    super(dao, esDao, flightPlan.getLastRunLoc(), mapper, flightPlan);
  }

  @Override
  public Date launch(Date lastRunDate) {
    LOGGER.warn("REFRESH TEST SCHEMA!!!");

    try {
      refreshSchema();
    } catch (Exception e) {
      JobLogs.checked(LOGGER, e, "SCHEMA REFRESH ERROR!! {}", e.getMessage());
    }

    return lastRunDate;
  }

  /**
   * Refresh a DB2 test schema by calling a stored procedure.
   * 
   * @throws NeutronException on database error
   */
  protected void refreshSchema() throws NeutronException {
    if (!isLargeDataSet()) {
      LOGGER.warn("\\n\\n\\n   REFRESH SCHEMA!!\\n\\n\\n");
      final Session session = getJobDao().getSessionFactory().getCurrentSession();
      getOrCreateTransaction(); // HACK
      final String schema = "CWSNS4"; // TESTING ONLY!!
      // (String) session.getSessionFactory().getProperties().get("hibernate.default_schema");

      final ProcedureCall proc = session.createStoredProcedureCall(schema + ".SPREFRSNS1");
      proc.registerStoredProcedureParameter("SCHEMANM", String.class, ParameterMode.IN);
      proc.registerStoredProcedureParameter("RETSTATUS", String.class, ParameterMode.OUT);
      proc.registerStoredProcedureParameter("RETMESSAG", String.class, ParameterMode.OUT);

      proc.setParameter("SCHEMANM", schema);
      proc.execute();

      final String returnStatus = (String) proc.getOutputParameterValue("RETSTATUS");
      final String returnMsg = (String) proc.getOutputParameterValue("RETMESSAG");
      LOGGER.info("refresh schema proc: status: {}, msg: {}", returnStatus, returnMsg);

      if (returnStatus.charAt(0) != '0') {
        JobLogs.runtime(LOGGER, "SCHEMA REFRESH ERROR! {}", returnMsg);
      }
    } else {
      LOGGER.warn("SAFETY! REFRESH PROHIBITED ON LARGE DATA SETS!");
    }
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(SchemaResetRocket.class, args);
  }

}
