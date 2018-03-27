package gov.ca.cwds.neutron.launch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import com.google.inject.Singleton;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.atom.AtomFlightRecorder;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightSummary;

@Singleton
public class FlightRecorder implements ApiMarker, AtomFlightRecorder, Serializable {

  private static final long serialVersionUID = 1L;

  private static final int KEEP_LAST_FLIGHTS = 200;

  private final Map<Class<?>, CircularFifoQueue<FlightLog>> flightHistory =
      new ConcurrentHashMap<>();

  private final Map<Class<?>, FlightLog> lastFlightLogs = new ConcurrentHashMap<>();

  private final Map<StandardFlightSchedule, FlightSummary> flightSummaries =
      Collections.synchronizedMap(new EnumMap<>(StandardFlightSchedule.class));

  @Override
  public Map<Class<?>, CircularFifoQueue<FlightLog>> getFlightLogHistory() {
    return flightHistory;
  }

  @Override
  public void logFlight(Class<?> klazz, FlightLog flightLog) {
    lastFlightLogs.put(klazz, flightLog);

    if (!flightHistory.containsKey(klazz)) {
      flightHistory.put(klazz, new CircularFifoQueue<>(KEEP_LAST_FLIGHTS));
    }
    flightHistory.get(klazz).add(flightLog);
  }

  @Override
  public FlightLog getLastFlightLog(final Class<?> klazz) {
    return lastFlightLogs.get(klazz);
  }

  @Override
  public FlightLog getLastFlightLog(StandardFlightSchedule sched) {
    return getLastFlightLog(sched.getRocketClass());
  }

  @Override
  public List<FlightLog> getHistory(final Class<?> klazz) {
    return flightHistory.containsKey(klazz) ? new ArrayList<>(flightHistory.get(klazz))
        : new ArrayList<>();
  }

  @Override
  public synchronized FlightSummary summarizeFlight(StandardFlightSchedule flightSchedule,
      FlightLog flightLog) {
    FlightSummary summary = flightSummaries.get(flightSchedule);
    if (summary == null) {
      summary = new FlightSummary(flightSchedule);
      flightSummaries.put(flightSchedule, summary);
    }

    summary.accumulate(flightLog);
    return summary;
  }

  @Override
  public FlightSummary getFlightSummary(StandardFlightSchedule flightSchedule) {
    return flightSummaries.get(flightSchedule);
  }

}
