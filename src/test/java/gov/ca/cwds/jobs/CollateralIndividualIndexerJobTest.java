package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedCollateralIndividualDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedCollateralIndividual;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlanTest;

/**
 * @author CWDS API Team
 */
public class CollateralIndividualIndexerJobTest
    extends Goddard<ReplicatedCollateralIndividual, ReplicatedCollateralIndividual> {

  ReplicatedCollateralIndividualDao dao;
  CollateralIndividualIndexerJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    dao = new ReplicatedCollateralIndividualDao(sessionFactory);
    target = new CollateralIndividualIndexerJob(dao, esDao, lastRunFile, MAPPER, flightPlan,
        launchDirector);
    target.setFlightPlan(FlightPlanTest.makeGeneric());
  }

  @Test
  public void testType() throws Exception {
    assertThat(CollateralIndividualIndexerJob.class, notNullValue());
  }

  @Test
  public void testInstantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void type() throws Exception {
    assertThat(CollateralIndividualIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getPartitionRanges_Args__() throws Exception {
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    assertThat(actual, notNullValue());
  }

  @Test
  public void getPartitionRanges_RSQ() throws Exception {
    System.setProperty("DB_CMS_SCHEMA", "CWSRSQ");
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    assertThat(actual, notNullValue());
  }

  @Test
  public void getPartitionRanges_Linux() throws Exception {
    System.setProperty("DB_CMS_SCHEMA", "CWSRS1");

    when(meta.getDatabaseMajorVersion()).thenReturn(12);
    when(meta.getDatabaseMinorVersion()).thenReturn(1);
    when(meta.getDatabaseProductVersion()).thenReturn("DB2/LINUXX8664");

    final List<Pair<String, String>> actual = target.getPartitionRanges();
    assertThat(actual, notNullValue());
  }

  @Test
  public void main_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/dsmith/client_indexer_time.txt", "-S"};
    CollateralIndividualIndexerJob.main(args);
  }

  @Test
  public void getPrepLastChangeSQL_A$() throws Exception {
    String actual = target.getPrepLastChangeSQL();
    String expected =
        "INSERT INTO GT_ID (IDENTIFIER)\n SELECT DISTINCT R.IDENTIFIER \n FROM COLTRL_T R \n WHERE R.IBMSNAP_LOGMARKER > '2018-12-31 03:30:12.000'";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isViewNormalizer_A$() throws Exception {
    boolean actual = target.isViewNormalizer();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getDenormalizedClass_A$() throws Exception {
    Object actual = target.getDenormalizedClass();
    Object expected = ReplicatedCollateralIndividual.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void main_A$StringArray() throws Exception {
    String[] args = new String[] {};
    CollateralIndividualIndexerJob.main(args);
  }

  // @Test
  // public void testfindAllNamedQueryExists() throws Exception {
  // Query query = session.getNamedQuery(
  // "gov.ca.cwds.data.persistence.cms.rep.ReplicatedCollateralIndividual.findPartitionedBuckets");
  // assertThat(query, is(notNullValue()));
  // }

  // @Test
  // public void testfindAllUpdatedAfterNamedQueryExists() throws Exception {
  // Query query = session.getNamedQuery(
  // "gov.ca.cwds.data.persistence.cms.rep.ReplicatedCollateralIndividual.findAllUpdatedAfter");
  // assertThat(query, is(notNullValue()));
  // }

}
