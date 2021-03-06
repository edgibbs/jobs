package gov.ca.cwds.neutron.launch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import com.google.inject.Singleton;

import gov.ca.cwds.neutron.atom.AtomFlightRecorder;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightSummary;

@Singleton
public class FlightRecorder implements AtomFlightRecorder {

  private static final long serialVersionUID = 1L;

  private static final int KEEP_LAST_FLIGHTS = 25;

  /**
   * Keep the last {@link #KEEP_LAST_FLIGHTS} flight logs by rocket class.
   */
  private final Map<Class<?>, CircularFifoQueue<FlightLog>> flightLogHistory =
      new ConcurrentHashMap<>();

  /**
   * Keep the <strong>last flight logs</strong> by rocket class.
   */
  private final Map<Class<?>, FlightLog> lastFlightLogs = new ConcurrentHashMap<>();

  /**
   * Last flight summary by rocket.
   */
  private final Map<StandardFlightSchedule, FlightSummary> flightSummaries =
      Collections.synchronizedMap(new EnumMap<>(StandardFlightSchedule.class));

  @Override
  public Map<Class<?>, CircularFifoQueue<FlightLog>> getFlightLogHistory() {
    return flightLogHistory;
  }

  @Override
  public synchronized void logFlight(Class<?> klazz, FlightLog flightLog) {
    lastFlightLogs.put(klazz, flightLog);

    if (!flightLogHistory.containsKey(klazz)) {
      flightLogHistory.put(klazz, new CircularFifoQueue<>(KEEP_LAST_FLIGHTS));
    }
    flightLogHistory.get(klazz).add(flightLog);
  }

  @Override
  public synchronized FlightLog getLastFlightLog(final Class<?> klazz) {
    return lastFlightLogs.get(klazz);
  }

  @Override
  public synchronized FlightLog getLastFlightLog(StandardFlightSchedule sched) {
    return getLastFlightLog(sched.getRocketClass());
  }

  @Override
  public synchronized List<FlightLog> getFlightLogHistory(final Class<?> klazz) {
    return flightLogHistory.containsKey(klazz) ? new ArrayList<>(flightLogHistory.get(klazz))
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
  public synchronized FlightSummary getFlightSummary(StandardFlightSchedule flightSchedule) {
    return flightSummaries.get(flightSchedule);
  }

}
