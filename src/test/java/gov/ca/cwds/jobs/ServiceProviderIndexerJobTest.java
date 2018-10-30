package gov.ca.cwds.jobs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.dao.cms.ReplicatedServiceProviderDao;
import gov.ca.cwds.data.es.NeutronElasticSearchDao;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.flight.FlightPlanTest;
import gov.ca.cwds.neutron.launch.FlightRecorder;

/**
 * @author CWDS API Team
 */
public class ServiceProviderIndexerJobTest extends Goddard {

  private static final class TestServiceProviderIndexerJob extends ServiceProviderIndexerJob {

    public TestServiceProviderIndexerJob(ReplicatedServiceProviderDao dao,
        NeutronElasticSearchDao esDao, String lastJobRunTimeFilename, ObjectMapper mapper,
        SessionFactory sessionFactory, FlightRecorder jobHistory, FlightPlan opts,
        AtomLaunchDirector launchDirector) {
      super(dao, esDao, lastJobRunTimeFilename, mapper, opts, launchDirector);
    }

  }

  ReplicatedServiceProviderDao dao;
  ServiceProviderIndexerJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    dao = new ReplicatedServiceProviderDao(sessionFactory);
    target =
        new ServiceProviderIndexerJob(dao, esDao, lastRunFile, MAPPER, flightPlan, launchDirector);
    target.setFlightPlan(FlightPlanTest.makeGeneric());
  }

  @After
  public void teardown() {
    // session.getTransaction().rollback();
  }

  @Test
  public void testType() throws Exception {
    assertThat(ServiceProviderIndexerJob.class, notNullValue());
  }

  @Test
  public void testInstantiation() throws Exception {
    target =
        new ServiceProviderIndexerJob(dao, esDao, lastRunFile, MAPPER, flightPlan, launchDirector);
    assertThat(target, notNullValue());
  }

  // @Test
  // public void testfindAllUpdatedAfterNamedQueryExists() throws Exception {
  // Query query = session.getNamedQuery(
  // "gov.ca.cwds.data.persistence.cms.rep.ReplicatedServiceProvider.findAllUpdatedAfter");
  // assertThat(query, is(notNullValue()));
  // }

  @Test
  public void type() throws Exception {
    assertThat(ServiceProviderIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getPartitionRanges_Args__() throws Exception {
    target =
        new ServiceProviderIndexerJob(dao, esDao, lastRunFile, MAPPER, flightPlan, launchDirector);
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getPartitionRanges_RSQ() throws Exception {
    System.setProperty("DB_CMS_SCHEMA", "CWSRSQ");
    target =
        new ServiceProviderIndexerJob(dao, esDao, lastRunFile, MAPPER, flightPlan, launchDirector);
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void main_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/CWS-NS3/client_indexer_time.txt", "-S"};
    ServiceProviderIndexerJob.main(args);
  }

}
