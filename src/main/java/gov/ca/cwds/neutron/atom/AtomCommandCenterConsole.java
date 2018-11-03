package gov.ca.cwds.neutron.atom;

import gov.ca.cwds.neutron.exception.NeutronCheckedException;

@FunctionalInterface
public interface AtomCommandCenterConsole {

  void initCommandControl() throws NeutronCheckedException;

}
