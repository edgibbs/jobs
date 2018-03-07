package gov.ca.cwds.neutron.rocket;

import java.util.Date;

import javax.persistence.ParameterMode;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.procedure.ProcedureCall;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;

/**
 * Refreshes a <strong>TEST</strong> transactional schema and its companion, replicated schema.
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
   * @param mapper Jackson ObjectMapper
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param flightPlan command line options
   */
  @Inject
  public SchemaResetRocket(final ReplicatedOtherAdultInPlacemtHomeDao dao,
      final ObjectMapper mapper, @LastRunFile String lastRunFile, FlightPlan flightPlan) {
    super(dao, null, lastRunFile, mapper, flightPlan);
    LOGGER.warn("CONSTRUCTOR");
  }

  @Override
  public Date launch(Date lastRunDate) {
    LOGGER.warn("RESET TEST SCHEMA!!!");

    try {
      refreshSchema();
    } catch (Exception e) {
      CheeseRay.checked(LOGGER, e, "SCHEMA RESET ERROR!! {}", e.getMessage());
    }

    return lastRunDate;
  }

  /**
   * Refresh a DB2 test schema by calling a stored procedure.
   * 
   * @throws NeutronCheckedException on database error
   */
  protected void refreshSchema() throws NeutronCheckedException {
    if (!isLargeDataSet()) {
      LOGGER.warn("\n\n\n   ********** RESET SCHEMA!! ********** \n\n\n");
      final Session session = getJobDao().getSessionFactory().getCurrentSession();
      getOrCreateTransaction(); // HACK

      // Target the NS schema, not RS.
      final String targetTransactionalSchema =
          ((String) session.getSessionFactory().getProperties().get("hibernate.default_schema"))
              .replaceFirst("CWSRS", "CWSNS").replaceAll("\"", "");
      LOGGER.info("CALL SCHEMA RESET: target schema: {}", targetTransactionalSchema);

      final ProcedureCall proc = session.createStoredProcedureCall("CWSTMP.SPREFDBS");
      proc.registerStoredProcedureParameter("SCHEMANM", String.class, ParameterMode.IN);
      proc.registerStoredProcedureParameter("RETSTATUS", String.class, ParameterMode.OUT);
      proc.registerStoredProcedureParameter("RETMESSAG", String.class, ParameterMode.OUT);

      proc.setParameter("SCHEMANM", targetTransactionalSchema);
      proc.execute();

      final String returnStatus = (String) proc.getOutputParameterValue("RETSTATUS");
      final String returnMsg = (String) proc.getOutputParameterValue("RETMESSAG");
      LOGGER.info("refresh schema proc: status: {}, msg: {}", returnStatus, returnMsg);

      if (StringUtils.isNotBlank(returnStatus) && returnStatus.charAt(0) != '0') {
        CheeseRay.runtime(LOGGER, "SCHEMA RESET ERROR! {}", returnMsg);
      } else {
        LOGGER.warn("SCHEMA RESET STARTED!!!");
      }
    } else {
      LOGGER.warn("SAFETY! RESET PROHIBITED ON LARGE DATA SETS!");
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
