package gov.ca.cwds.neutron.launch;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import gov.ca.cwds.neutron.atom.AtomFlightRecorder;
import gov.ca.cwds.neutron.atom.AtomRocketFactory;
import gov.ca.cwds.neutron.enums.NeutronSchedulerConstants;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.neutron.util.shrinkray.NeutronClassFinder;

/**
 * Neutron implementation of Quartz JobFactory.
 * 
 * @author CWDS API Team
 */
@Singleton
public class RocketFactory implements AtomRocketFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(RocketFactory.class);

  /**
   * HACK: use a DI abstraction instead.
   */
  private Injector injector;

  private FlightPlanRegistry flightPlanRegistry;

  private final FlightPlan baseFlightPlan;

  private final AtomFlightRecorder flightRecorder;

  private Deque<String> dequeRerunIds = new ConcurrentLinkedDeque<>();

  @Inject
  public RocketFactory(final Injector injector, final FlightPlan baseFlightPlan,
      final FlightPlanRegistry flightPlanRegistry, final FlightRecorder flightRecorder,
      @Named("rerun.deque.ids") Deque<String> rerunIds) {
    this.injector = injector;
    this.baseFlightPlan = baseFlightPlan;
    this.flightPlanRegistry = flightPlanRegistry;
    this.flightRecorder = flightRecorder;

    if (rerunIds != null) {
      this.dequeRerunIds = rerunIds;
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public BasePersonRocket fuelRocket(Class<?> klass, final FlightPlan flightPlan)
      throws NeutronCheckedException {
    try {
      LOGGER.info("READY SCHEDULED ROCKET: {}", klass.getName());

      // Any keys requested to be re-run?
      if (!dequeRerunIds.isEmpty()) {
        flightPlan.setDequeRerunIds(dequeRerunIds);
      }

      final BasePersonRocket ret = (BasePersonRocket<?, ?>) injector.getInstance(klass);
      ret.init(flightPlan.getLastRunLoc(), flightPlan);
      return ret;
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "FAILED TO PREPARE ROCKET!: {}", e.getMessage());
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public BasePersonRocket fuelRocket(String rocketName, final FlightPlan flightPlan)
      throws NeutronCheckedException {
    return fuelRocket(NeutronClassFinder.classForName(rocketName), flightPlan);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
    final JobDetail jd = bundle.getJobDetail();
    Class<?> klazz;

    try {
      klazz = NeutronClassFinder
          .classForName(jd.getJobDataMap().getString(NeutronSchedulerConstants.ROCKET_CLASS));
    } catch (NeutronCheckedException e) {
      throw new SchedulerException("NO SUCH ROCKET CLASS!", e);
    }

    LOGGER.info("LAUNCH! rocket class: {}", klazz);
    NeutronRocket ret;
    try {
      final FlightPlan flightPlan = flightPlanRegistry.getFlightPlan(klazz);
      ret = new NeutronRocket(fuelRocket(klazz, flightPlan),
          StandardFlightSchedule.lookupByRocketClass(klazz), flightRecorder);
    } catch (NeutronCheckedException e) {
      throw new SchedulerException("NO ROCKET SETTINGS! klazz=" + klazz, e);
    }

    return ret;
  }

  public FlightPlan getBaseFlightPlan() {
    return baseFlightPlan;
  }

  public FlightPlanRegistry getFlightPlanRegistry() {
    return flightPlanRegistry;
  }

  public Injector getInjector() {
    return injector;
  }

  public void setInjector(Injector injector) {
    this.injector = injector;
  }

  public void setFlightPlanRegistry(FlightPlanRegistry flightPlanRegistry) {
    this.flightPlanRegistry = flightPlanRegistry;
  }

  public static Logger getLogger() {
    return LOGGER;
  }

  public Deque<String> getDequeRerunIds() {
    return dequeRerunIds;
  }

  public void setDequeRerunIds(Deque<String> dequeRerunIds) {
    this.dequeRerunIds = dequeRerunIds;
  }

}
