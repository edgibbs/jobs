package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedReporterDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedReporter;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedSubstituteCareProvider;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;

/**
 * Test for {@link ReporterIndexerJob}.
 * 
 * @author CWDS API Team
 */
public class ReporterIndexerJobTest
    extends Goddard<ReplicatedSubstituteCareProvider, ReplicatedSubstituteCareProvider> {

  private ReplicatedReporterDao dao;
  private ReporterIndexerJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    dao = new ReplicatedReporterDao(sessionFactory);
    target = new ReporterIndexerJob(dao, esDao, lastRunFile, MAPPER, flightPlan);
  }

  @Test
  public void testType() throws Exception {
    assertThat(ReporterIndexerJob.class, notNullValue());
  }

  @Test
  public void testInstantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void type() throws Exception {
    assertThat(ReporterIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getIdColumn_Args__() throws Exception {
    final String actual = target.getIdColumn();
    final String expected = "FKREFERL_T";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPartitionRanges_Args() throws Exception {
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    final List<Pair<String, String>> expected = new ArrayList<>();
    expected.add(Pair.of("aaaaaaaaaa", "9999999999"));
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPartitionRanges_RSQ() throws Exception {
    System.setProperty("DB_CMS_SCHEMA", "CWSRSQ");
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    assertThat(actual.size(), is(equalTo(64)));
  }

  @Test
  public void main_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/dsmith/client_indexer_time.txt", "-S"};
    ReporterIndexerJob.main(args);
  }

  @Test
  public void getPrepLastChangeSQL_A$() throws Exception {
    String actual = target.getPrepLastChangeSQL();
    String expected =
        "INSERT INTO GT_ID (IDENTIFIER)\n SELECT DISTINCT R.FKREFERL_T\n FROM REPTR_T R \n WHERE R.IBMSNAP_LOGMARKER > '2018-12-31 03:21:12.000'";
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
    Object expected = ReplicatedReporter.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getIdColumn_A$() throws Exception {
    String actual = target.getIdColumn();
    String expected = "FKREFERL_T";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPartitionRanges_A$() throws Exception {
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    final List<Pair<String, String>> expected = new ArrayList<>();
    expected.add(pair);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void main_A$StringArray() throws Exception {
    String[] args = new String[] {};
    ReporterIndexerJob.main(args);
  }

}
