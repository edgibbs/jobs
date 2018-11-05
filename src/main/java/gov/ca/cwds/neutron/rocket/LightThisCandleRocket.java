package gov.ca.cwds.neutron.rocket;

import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.es.NeutronElasticSearchDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.launch.LaunchDirector;

/**
 * Dummy rocket begins the Quartz job scheduler cycle.
 * 
 * @author CWDS API Team
 */
public class LightThisCandleRocket
    extends BasePersonRocket<ReplicatedOtherAdultInPlacemtHome, ReplicatedOtherAdultInPlacemtHome> {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(LightThisCandleRocket.class);

  private transient LaunchDirector launchDirector1;

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao random DAO for parent class
   * @param esDao ElasticSearch DAO for People Summary index
   * @param mapper Jackson ObjectMapper
   * @param launchDirector1 command launch director
   * @param flightPlan command line options
   * @param launchDirector launch director
   */
  @Inject
  public LightThisCandleRocket(final ReplicatedOtherAdultInPlacemtHomeDao dao,
      @Named("elasticsearch.dao.people-summary") final NeutronElasticSearchDao esDao,
      final ObjectMapper mapper, LaunchDirector launchDirector1, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, flightPlan.getLastRunLoc(), mapper, flightPlan, launchDirector);
    this.launchDirector1 = launchDirector1;
  }

  @Override
  public Date launch(Date lastRunDate) {
    nameThread("start_initial_load");
    if (LaunchCommand.isInitialMode()) {
      LOGGER.warn("Let's light this candle!");
    }

    return lastRunDate;
  }

  public LaunchDirector getLaunchDirector1() {
    return launchDirector1;
  }

  public void setLaunchDirector1(LaunchDirector launchDirector1) {
    this.launchDirector1 = launchDirector1;
  }

}
