package gov.ca.cwds.neutron.atom;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.data.es.NeutronElasticSearchDao;
import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;

public class AtomSharedTest extends Goddard<ReplicatedClient, RawClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AtomSharedTest.class);

  private static class TestAtomShared implements AtomShared {

    private final FlightLog track = new FlightLog();

    @Override
    public FlightLog getFlightLog() {
      return track;
    }

    @Override
    public NeutronElasticSearchDao getEsDao() {
      return null;
    }

    @Override
    public Logger getLogger() {
      return LOGGER;
    }

    @Override
    public FlightPlan getFlightPlan() {
      return null;
    }

    @Override
    public ObjectMapper getMapper() {
      return MAPPER;
    }

  }

  AtomShared target;

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();
    target = new TestAtomShared();
  }

  @Test
  public void type() throws Exception {
    assertThat(AtomShared.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void nameThread_Args__String() throws Exception {
    String title = "wedgie";
    target.nameThread(title);
  }

}
