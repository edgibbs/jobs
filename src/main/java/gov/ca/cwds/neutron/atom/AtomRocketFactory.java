package gov.ca.cwds.neutron.atom;

import org.quartz.spi.JobFactory;

import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;

/**
 * Neutron implementation of Quartz {@link JobFactory}.
 * 
 * @author CWDS API Team
 */
public interface AtomRocketFactory extends JobFactory {

  /**
   * Prepare a registered rocket.
   * 
   * @param klass rocket class
   * @param flightPlan command line arguments
   * @return the rocket
   * @throws NeutronCheckedException unexpected runtime error
   */
  @SuppressWarnings("rawtypes")
  BasePersonRocket fuelRocket(final Class<?> klass, final FlightPlan flightPlan)
      throws NeutronCheckedException;

  /**
   * Prepare a registered rocket. Convenient overload of {@link #fuelRocket(Class, FlightPlan)}.
   * 
   * @param rocketName rocket class name
   * @param flightPlan flight plan from command line arguments
   * @return the rocket
   * @throws NeutronCheckedException unexpected runtime error
   */
  @SuppressWarnings("rawtypes")
  public BasePersonRocket fuelRocket(final String rocketName, final FlightPlan flightPlan)
      throws NeutronCheckedException;

}
