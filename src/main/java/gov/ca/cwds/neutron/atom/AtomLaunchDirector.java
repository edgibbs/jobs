package gov.ca.cwds.neutron.atom;

import java.util.Map;

import org.quartz.Scheduler;
import org.quartz.TriggerKey;

import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.launch.NeutronRocket;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;

/**
 * Manager for scheduled rocket flights.
 * 
 * @author CWDS API Team
 */
public interface AtomLaunchDirector {

  /**
   * Launch a registered rocket.
   * 
   * @param klass rocket class
   * @param flightPlan command line arguments
   * @return rocket flight progress
   * @throws NeutronCheckedException unexpected runtime error
   */
  FlightLog launch(Class<?> klass, FlightPlan flightPlan) throws NeutronCheckedException;

  /**
   * Launch a registered rocket. Convenient overload.
   * 
   * @param rocketClass rocket class name
   * @param flightPlan command line arguments
   * @return rocket flight progress
   * @throws NeutronCheckedException unexpected runtime error
   */
  FlightLog launch(String rocketClass, FlightPlan flightPlan) throws NeutronCheckedException;

  void prepareLaunchPads();

  void markRocketAsInFlight(TriggerKey key, NeutronRocket rocket);

  AtomLaunchPad scheduleLaunch(StandardFlightSchedule sched, FlightPlan flightPlan)
      throws NeutronCheckedException;

  boolean isLaunchVetoed(String className) throws NeutronCheckedException;

  void stopScheduler(boolean waitForJobsToComplete) throws NeutronCheckedException;

  void startScheduler() throws NeutronCheckedException;

  Scheduler getScheduler();

  Map<Class<?>, AtomLaunchPad> getLaunchPads();

  AtomFlightPlanManager getFlightPlanManger();

  AtomFlightRecorder getFlightRecorder();

}
