package gov.ca.cwds.neutron.atom;

import gov.ca.cwds.neutron.exception.NeutronCheckedException;

/**
 * Start or stop the Quartz scheduler or shutdown Launch Command.
 * 
 * @author CWDS API Team
 */
public interface AtomLaunchCommand {

  void stopScheduler(boolean waitForJobsToComplete) throws NeutronCheckedException;

  void startScheduler() throws NeutronCheckedException;

  /**
   * shutdown Launch Command immediately.
   * 
   * @throws NeutronCheckedException
   */
  void shutdown() throws NeutronCheckedException;

}
