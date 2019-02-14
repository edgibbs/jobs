package gov.ca.cwds.neutron.flight;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.jobs.Goddard;

public class NeutronNewRelicNotifierTest extends Goddard {

  NeutronNewRelicNotifier target;

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();

    target = new NeutronNewRelicNotifier();
  }

  @Test
  public void type() throws Exception {
    assertThat(NeutronNewRelicNotifier.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void notifyMonitor_A$FlightLog$String() throws Exception {
    final FlightLog fl = new FlightLog();
    final String eventType = "nr_fun";
    target.notifyMonitor(fl, eventType);
  }

  @Test
  public void append_A$StringBuilder$MapEntry() throws Exception {
    final StringBuilder buf = new StringBuilder();
    Map.Entry e = mock(Map.Entry.class);
    target.append(buf, e);
  }

}
