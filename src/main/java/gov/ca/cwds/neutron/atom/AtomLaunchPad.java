package gov.ca.cwds.neutron.atom;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;

public interface AtomLaunchPad extends ApiMarker {

  /**
   * Action: launch immediately and wait synchronously for results.
   * 
   * @param cmdLine command line
   * @return JXM output
   * @throws NeutronCheckedException on error
   */
  String run(String cmdLine) throws NeutronCheckedException;

  // ==============
  // COMMANDS:
  // ==============

  void schedule() throws NeutronCheckedException;

  void unschedule() throws NeutronCheckedException;

  /**
   * Show last flight status.
   * 
   * @return last flight status
   */
  String status();

  /**
   * Roll back timestamp file the number of hours specified.
   * 
   * @param hoursInPast hours to roll back timestamp file
   */
  void waybackHours(int hoursInPast);

  /**
   * Re-run record identifier during next run of rocket's last change mode. Use to re-run clients or
   * reporters that may have been missed.
   * 
   * @param ids identifiers to re-run
   */
  void rerunIds(String... ids);

  /**
   * Show history of last N flights.
   * 
   * @return flight history
   */
  String history();

  String summary();

  /**
   * Display this rocket's logs.
   * 
   * @return logs
   */
  String logs();

  /**
   * Abort a rocket <strong>in flight</strong>.
   * 
   * @throws NeutronCheckedException general error
   */
  void stop() throws NeutronCheckedException;

  /**
   * Pause a rocket's schedule.
   * 
   * @throws NeutronCheckedException general error
   */
  void pause() throws NeutronCheckedException;

  /**
   * Resume a rocket's schedule.
   * 
   * @throws NeutronCheckedException general error
   */
  void resume() throws NeutronCheckedException;

  /**
   * Abort all rockets, shutdown command center, and exit JVM.
   * 
   * @return readable result string
   * @throws NeutronCheckedException general error
   */
  String shutdown() throws NeutronCheckedException;

  boolean isVetoExecution();

  void setVetoExecution(boolean vetoExecution);

  // ==================
  // ACCESSORS:
  // ==================

  FlightPlan getFlightPlan();

  void setFlightPlan(FlightPlan flightPlan);


  StandardFlightSchedule getFlightSchedule();

  AtomFlightRecorder getFlightRecorder();


}
