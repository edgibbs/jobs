package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.SessionFactory;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.dao.cms.ReplicatedPersonCasesDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.cms.EsChildPersonCase;

public class ChildCaseHistoryIndexerJobTest {

  @Test
  public void type() throws Exception {
    assertThat(ChildCaseHistoryIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    ReplicatedPersonCasesDao clientDao = null;
    ElasticsearchDao elasticsearchDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    ChildCaseHistoryIndexerJob target = new ChildCaseHistoryIndexerJob(clientDao, elasticsearchDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    assertThat(target, notNullValue());
  }

  @Test
  public void extract_Args__ResultSet() throws Exception {
    ReplicatedPersonCasesDao clientDao = null;
    ElasticsearchDao elasticsearchDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    ChildCaseHistoryIndexerJob target = new ChildCaseHistoryIndexerJob(clientDao, elasticsearchDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    // given
    ResultSet rs = mock(ResultSet.class);
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    EsChildPersonCase actual = target.extract(rs);
    // then
    // e.g. : verify(mocked).called();
    EsChildPersonCase expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  public void extract_Args__ResultSet_T__SQLException() throws Exception {
    ReplicatedPersonCasesDao clientDao = null;
    ElasticsearchDao elasticsearchDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    ChildCaseHistoryIndexerJob target = new ChildCaseHistoryIndexerJob(clientDao, elasticsearchDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    // given
    ResultSet rs = mock(ResultSet.class);
    // e.g. : given(mocked.called()).willReturn(1);
    try {
      // when
      target.extract(rs);
      fail("Expected exception was not thrown!");
    } catch (SQLException e) {
      // then
    }
  }

  @Test
  public void getDenormalizedClass_Args__() throws Exception {
    ReplicatedPersonCasesDao clientDao = null;
    ElasticsearchDao elasticsearchDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    ChildCaseHistoryIndexerJob target = new ChildCaseHistoryIndexerJob(clientDao, elasticsearchDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    Object actual = target.getDenormalizedClass();
    // then
    // e.g. : verify(mocked).called();
    Object expected = EsChildPersonCase.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getViewName_Args__() throws Exception {
    ReplicatedPersonCasesDao clientDao = null;
    ElasticsearchDao elasticsearchDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    ChildCaseHistoryIndexerJob target = new ChildCaseHistoryIndexerJob(clientDao, elasticsearchDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    String actual = target.getInitialLoadViewName();
    // then
    // e.g. : verify(mocked).called();
    String expected = "MQT_CASE_HIST";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJdbcOrderBy_Args__() throws Exception {
    ReplicatedPersonCasesDao clientDao = null;
    ElasticsearchDao elasticsearchDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    ChildCaseHistoryIndexerJob target = new ChildCaseHistoryIndexerJob(clientDao, elasticsearchDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    String actual = target.getJdbcOrderBy();
    // then
    // e.g. : verify(mocked).called();
    String expected = " ORDER BY FOCUS_CHILD_ID, CASE_ID, PARENT_ID ";
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  public void main_Args__StringArray() throws Exception {
    // given
    String[] args = new String[] {};
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    ChildCaseHistoryIndexerJob.main(args);
    // then
    // e.g. : verify(mocked).called();
  }

}
