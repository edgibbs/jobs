package gov.ca.cwds.neutron.atom;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightSummary;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;

public interface AtomFlightRecorder extends ApiMarker {

  Map<Class<?>, CircularFifoQueue<FlightLog>> getFlightLogHistory();

  void logFlight(Class<?> klazz, FlightLog flightLog);

  FlightLog getLastFlightLog(final Class<?> klazz);

  FlightLog getLastFlightLog(StandardFlightSchedule sched);

  List<FlightLog> getHistory(final Class<?> klazz);

  void summarizeFlight(StandardFlightSchedule flightSchedule, FlightLog flightLog);

  FlightSummary getFlightSummary(StandardFlightSchedule flightSchedule);

}
