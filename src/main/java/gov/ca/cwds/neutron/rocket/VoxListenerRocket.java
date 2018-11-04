package gov.ca.cwds.neutron.rocket;

import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.launch.LaunchDirector;

/**
 * Dummy rocket supports cross-job VOX commands, like shutdown,
 * 
 * <p>
 * Reasoning: if the People Summary rocket (class {@code ClientPersonIndexerJob} stalls, then global
 * VOX commands may fail in Launch Command, because the only rocket flying is
 * {@code ClientPersonIndexerJob}.
 * </p>
 * 
 * @author CWDS API Team
 */
public class VoxListenerRocket
    extends BasePersonRocket<ReplicatedOtherAdultInPlacemtHome, ReplicatedOtherAdultInPlacemtHome> {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxListenerRocket.class);

  private int iterations;

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao random DAO for parent class
   * @param esDao ElasticSearch DAO for People Summary index
   * @param mapper Jackson ObjectMapper
   * @param launchDirector1 command launch director
   * @param flightPlan command line options
   * @param launchDirector launch director
   * @param iterations TODO
   */
  @Inject
  public VoxListenerRocket(final ReplicatedOtherAdultInPlacemtHomeDao dao,
      @Named("elasticsearch.dao.people-summary") final ElasticsearchDao esDao,
      final ObjectMapper mapper, LaunchDirector launchDirector1, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector,
      @Named("vox.listener.rocket.iterations") Integer iterations) {
    super(dao, esDao, flightPlan.getLastRunLoc(), mapper, flightPlan, launchDirector);
    this.iterations = iterations != null && iterations > 0 ? iterations : 3000;
  }

  @Override
  public Date launch(Date lastRunDate) {
    LOGGER.info("Launch dummy rocket");
    nameThread("dummy_rocket");
    int counter = 0;

    try {
      // Listen for VOX commands.
      for (int i = 0; i < iterations; i++) {
        CheeseRay.logEvery(LOGGER, ++counter, "Dummy", "vox");
        Thread.sleep(100L);
      }
    } catch (Exception e) {
      LOGGER.error("DUMMY ROCKET BLEW UP!", e);
    }

    return lastRunDate;
  }

}
