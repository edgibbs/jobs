package gov.ca.cwds.neutron.atom;

import gov.ca.cwds.neutron.flight.FlightLog;

@FunctionalInterface
public interface AtomMonitorNotifier {

  void notifyMonitor(final FlightLog fl, final String eventType);

}
