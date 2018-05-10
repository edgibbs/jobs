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

  SUCCEEDED(),

  FAILED(),

  RUNNING(FAILED, SUCCEEDED),

  NOT_STARTED(RUNNING),

  ;

  private final boolean terminal;

  private final FlightStatus[] permittedNextSteps;

  private FlightStatus(FlightStatus... permittedNextSteps) {
    this.terminal = permittedNextSteps.length == 0;
    this.permittedNextSteps = permittedNextSteps;
  }

  public boolean isTerminal() {
    return terminal;
  }

  public boolean isStepPermitted(FlightStatus next) {
    return ArrayUtils.contains(permittedNextSteps, next);
  }

}
