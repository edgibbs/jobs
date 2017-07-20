package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.hibernate.SessionFactory;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.dao.cms.ReplicatedClientR1Dao;
import gov.ca.cwds.data.es.ElasticsearchDao;

/**
 * 
 * @author CWDS API Team
 */
@SuppressWarnings("javadoc")
public class ClientIndexerJobTest {

  @Test
  public void type() throws Exception {
    assertThat(ClientIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    ReplicatedClientR1Dao clientDao = null;
    ElasticsearchDao elasticsearchDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    ClientIndexerJob target = new ClientIndexerJob(clientDao, elasticsearchDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    assertThat(target, notNullValue());
  }

  @Test(expected = JobsException.class)
  public void main_Args$StringArray() throws Exception {
    // given
    final String[] args = new String[] {};
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    ClientIndexerJob.main(args);
    // then
    // e.g. : verify(mocked).called();
  }

  @Test(expected = JobsException.class)
  public void main_Args__StringArray__t_je() throws Exception {
    // given
    String[] args = new String[] {};
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    ClientIndexerJob.main(args);
    // then
    // e.g. : verify(mocked).called();
  }

  @Test(expected = JobsException.class)
  public void main_Args__bucket_range() throws Exception {
    // given
    final String[] args = new String[] {"-c", "config/local.yaml", "-r", "21-22", "-b", "500"};
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    ClientIndexerJob.main(args);
    // then
    // e.g. : verify(mocked).called();
  }

  @Test(expected = JobsException.class)
  public void main_Args__bucket_range_not_digit() throws Exception {
    // given
    final String[] args = new String[] {"-c", "config/local.yaml", "-r", "abc-xyz", "-b", "500"};
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    ClientIndexerJob.main(args);
    // then
    // e.g. : verify(mocked).called();
  }

}
