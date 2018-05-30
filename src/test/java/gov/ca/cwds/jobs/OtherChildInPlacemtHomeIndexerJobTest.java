package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedOtherChildInPlacemtHomeDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherChildInPlacemtHome;

/**
 * @author CWDS API Team
 */
@SuppressWarnings("javadoc")
public class OtherChildInPlacemtHomeIndexerJobTest
    extends Goddard<ReplicatedOtherChildInPlacemtHome, ReplicatedOtherChildInPlacemtHome> {

  ReplicatedOtherChildInPlacemtHomeDao dao;
  OtherChildInPlacemtHomeIndexerJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    dao = new ReplicatedOtherChildInPlacemtHomeDao(this.sessionFactory);
    target = new OtherChildInPlacemtHomeIndexerJob(dao, esDao, lastRunFile, MAPPER,
        flightPlan, launchDirector);
  }

  @Test
  public void testType() throws Exception {
    assertThat(OtherChildInPlacemtHomeIndexerJob.class, notNullValue());
  }

  @Test
  public void testInstantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void main_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/CWS-NS3/client_indexer_time.txt", "-S"};
    OtherChildInPlacemtHomeIndexerJob.main(args);
  }

}
