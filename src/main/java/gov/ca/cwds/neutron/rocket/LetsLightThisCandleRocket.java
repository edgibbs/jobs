package gov.ca.cwds.neutron.rocket;

import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.launch.LaunchDirector;

/**
 * Dummy rocket begins the Quartz job scheduler cycle.
 * 
 * @author CWDS API Team
 */
public class LetsLightThisCandleRocket
    extends BasePersonRocket<ReplicatedOtherAdultInPlacemtHome, ReplicatedOtherAdultInPlacemtHome> {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER =
      new JetPackLogger(LetsLightThisCandleRocket.class);

  private transient LaunchDirector launchDirector;

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao random DAO for parent class
   * @param esDao ElasticSearch DAO for People Summary index
   * @param mapper Jackson ObjectMapper
   * @param launchDirector command launch director
   * @param flightPlan command line options
   */
  @Inject
  public LetsLightThisCandleRocket(final ReplicatedOtherAdultInPlacemtHomeDao dao,
      @Named("elasticsearch.dao.people-summary") final ElasticsearchDao esDao,
      final ObjectMapper mapper, LaunchDirector launchDirector, FlightPlan flightPlan) {
    super(dao, esDao, flightPlan.getLastRunLoc(), mapper, flightPlan);
    this.launchDirector = launchDirector;
  }

  @Override
  public Date launch(Date lastRunDate) {
    nameThread("start_initial_load");
    if (LaunchCommand.isInitialMode()) {
      LOGGER.warn("Lets light this candle!");
    }

    return lastRunDate;
  }

  public LaunchDirector getLaunchDirector() {
    return launchDirector;
  }

  public void setLaunchDirector(LaunchDirector launchDirector) {
    this.launchDirector = launchDirector;
  }

}
