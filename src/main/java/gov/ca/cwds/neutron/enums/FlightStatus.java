package gov.ca.cwds.neutron.enums;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Rocket flight status.
 * 
 * <p>
 * All flights start in {@link #NOT_STARTED}, switch to {@link #RUNNING} when started, and end with
 * a terminal status, either {@link #SUCCEEDED} or {@link #FAILED}.
 * </p>
 * 
 * @author CWDS API Team
 */
public enum FlightStatus {

  /**
   * Terminal step. The flight landed successfully.
   */
  SUCCEEDED(),

  /**
   * Terminal step. The flight blew up.
   */
  FAILED(),

  /**
   * Flight in progress. Could succeed or fail.
   */
  RUNNING(FAILED, SUCCEEDED),

  /**
   * Initial state.
   */
  NOT_STARTED(RUNNING),

  ;

  private final boolean terminal;

  private final FlightStatus[] permittedNextSteps;

  /**
   * Ctor. Each status takes optional possible next statuses. If no further status is possible, then
   * the status is terminal.
   * 
   * @param permittedNextSteps possible next flight statuses
   */
  private FlightStatus(FlightStatus... permittedNextSteps) {
    this.terminal = permittedNextSteps.length == 0;
    this.permittedNextSteps = permittedNextSteps;
  }

  /**
   * Is this state a terminal, leaf step.
   * 
   * @return true if this state a terminal, leaf step.
   */
  public boolean isTerminal() {
    return terminal;
  }

  /**
   * Test a status to see whether you proceed to it from the current status.
   * 
   * @param next status to test
   * @return true if a caller may proceed to the specified status from this one
   */
  public boolean isStepPermitted(FlightStatus next) {
    return ArrayUtils.contains(permittedNextSteps, next);
  }

}
