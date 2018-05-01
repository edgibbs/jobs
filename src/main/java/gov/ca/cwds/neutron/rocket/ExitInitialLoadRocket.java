package gov.ca.cwds.neutron.rocket;

import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomFlightRecorder;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.flight.FlightSummary;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.launch.LaunchDirector;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;

/**
 * Exit the initial load cycle.
 * 
 * @author CWDS API Team
 */
public class ExitInitialLoadRocket
    extends BasePersonRocket<ReplicatedOtherAdultInPlacemtHome, ReplicatedOtherAdultInPlacemtHome> {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(ExitInitialLoadRocket.class);

  private transient LaunchDirector launchDirector;

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao random DAO for parent class
   * @param esDao ElasticSearch DAO
   * @param mapper Jackson ObjectMapper
   * @param launchDirector command launch director
   * @param flightPlan command line options
   */
  @Inject
  public ExitInitialLoadRocket(final ReplicatedOtherAdultInPlacemtHomeDao dao,
      @Named("elasticsearch.dao.people") final ElasticsearchDao esDao, final ObjectMapper mapper,
      LaunchDirector launchDirector, FlightPlan flightPlan) {
    super(dao, esDao, flightPlan.getLastRunLoc(), mapper, flightPlan);
    this.launchDirector = launchDirector;
  }

  protected void logError(StandardFlightSchedule sched, FlightSummary summary) {
    if (summary == null) {
      LOGGER.error("\n\n\t>>>>>>>>>>> NULL SUMMARY??  rocket: {}", sched.getRocketName());
    } else if (summary.getBulkError() > 0 || summary.getBulkAfter() == 0) {
      LOGGER.error("\n\n\t>>>>>>>>>>> ERRORS? NO RECORDS?\n ROCKET: {}, error: {}, bulk after: {}",
          sched.getRocketName(), summary.getBulkError(), summary.getBulkAfter());
    }
  }

  @Override
  public Date launch(Date lastRunDate) {
    nameThread("exit_initial_load");
    if (LaunchCommand.isInitialMode()) {
      LOGGER.info("EXIT INITIAL LOAD!");
      final AtomFlightRecorder flightRecorder = launchDirector.getFlightRecorder();

      try {
        for (StandardFlightSchedule sched : StandardFlightSchedule.getInitialLoadRockets(true,
            flightPlan.getExcludedRockets())) {
          final FlightSummary summary = flightRecorder.getFlightSummary(sched);
          LOGGER.info("ROCKET SUMMARY:\n{}", summary);
          logError(sched, summary);
        }

        LaunchCommand.getInstance().shutdown();
        //Swap Alias to new index
        final String index = flightPlan.getIndexName();
        final String alias = esDao.getConfig().getElasticsearchAlias();
        if (esDao.createOrSwapAlias(alias, index)) {
          LOGGER.info("Applied Alias {} to Index {} ", alias, index);
        }
      } catch (Exception e) {
        CheeseRay.checked(LOGGER, e, "ELASTICSEARCH INDEX MANAGEMENT ERROR! {}", e.getMessage());
      }
    }

    return lastRunDate;
  }

}
