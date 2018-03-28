package gov.ca.cwds.jobs.schedule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightSummary;
import gov.ca.cwds.neutron.launch.FlightRecorder;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;

public class FlightRecorderTest {

  FlightRecorder target;

  @Before
  public void setup() throws Exception {
    target = new FlightRecorder();
  }

  @Test
  public void type() throws Exception {
    assertThat(FlightRecorder.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void addTrack_Args__Class__FlightLog() throws Exception {
    final Class<?> klazz = StandardFlightSchedule.INTAKE_SCREENING.getRocketClass();
    final FlightLog flightLog = new FlightLog();
    target.logFlight(klazz, flightLog);
  }

  @Test
  public void getLastTrack_Args__Class() throws Exception {
    final Class<?> klazz = StandardFlightSchedule.INTAKE_SCREENING.getRocketClass();
    final FlightLog actual = target.getLastFlightLog(klazz);
    FlightLog expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getHistory_Args__Class() throws Exception {
    final Class<?> klazz = StandardFlightSchedule.INTAKE_SCREENING.getRocketClass();
    List<FlightLog> actual = target.getFlightLogHistory(klazz);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void summarizeFlight_Args__DefaultFlightSchedule__FlightLog() throws Exception {
    final StandardFlightSchedule flightSchedule = StandardFlightSchedule.INTAKE_SCREENING;
    final FlightLog flightLog = new FlightLog();
    target.summarizeFlight(flightSchedule, flightLog);
  }

  @Test
  public void getFlightSummary_Args__DefaultFlightSchedule() throws Exception {
    final StandardFlightSchedule flightSchedule = StandardFlightSchedule.INTAKE_SCREENING;
    final FlightSummary actual = target.getFlightSummary(flightSchedule);
    FlightSummary expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

}
