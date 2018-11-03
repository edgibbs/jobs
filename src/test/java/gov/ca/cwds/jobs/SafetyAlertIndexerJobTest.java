package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.update.UpdateRequest;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.ObjectMapperUtils;
import gov.ca.cwds.dao.cms.ReplicatedSafetyAlertsDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.persistence.cms.EsSafetyAlert;
import gov.ca.cwds.data.persistence.cms.ReplicatedSafetyAlerts;
import gov.ca.cwds.jobs.test.SimpleTestSystemCodeCache;

public class SafetyAlertIndexerJobTest extends Goddard {

  private static final ObjectMapper mapper = ObjectMapperUtils.createObjectMapper();

  ReplicatedSafetyAlertsDao dao;
  SafetyAlertIndexerJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    when(sessionFactory.getCurrentSession()).thenReturn(session);
    when(session.beginTransaction()).thenReturn(transaction);
    when(flightPlan.isLoadSealedAndSensitive()).thenReturn(false);
    when(esDao.getConfig()).thenReturn(esConfig);
    when(esConfig.getElasticsearchAlias()).thenReturn("people");
    when(esConfig.getElasticsearchDocType()).thenReturn("person");

    dao = new ReplicatedSafetyAlertsDao(sessionFactory);
    SimpleTestSystemCodeCache.init();

    target = new SafetyAlertIndexerJob(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
  }

  @Test
  public void type() throws Exception {
    assertThat(SafetyAlertIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getDenormalizedClass_Args__() throws Exception {
    Object actual = target.getDenormalizedClass();
    Object expected = EsSafetyAlert.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadViewName_Args__() throws Exception {
    String actual = target.getInitialLoadViewName();
    String expected = "VW_LST_SAFETY_ALERT";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJdbcOrderBy_Args__() throws Exception {
    String actual = target.getJdbcOrderBy().trim();
    String expected = "ORDER BY CLIENT_ID";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadQuery_Args__String() throws Exception {
    String dbSchemaName = "CWSINT";
    String actual = target.getInitialLoadQuery(dbSchemaName).replaceAll("  ", " ").trim();
    String expected =
        "SELECT x.* FROM CWSINT.VW_LST_SAFETY_ALERT x WHERE x.CLIENT_SENSITIVITY_IND = 'N' ORDER BY CLIENT_ID FOR READ ONLY WITH UR";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadQuery_Args__sealed() throws Exception {
    // when(flightPlan.isLoadSealedAndSensitive()).thenReturn(true);
    String dbSchemaName = "CWSINT";
    String actual = target.getInitialLoadQuery(dbSchemaName).replaceAll("  ", " ").trim();
    String expected =
        "SELECT x.* FROM CWSINT.VW_LST_SAFETY_ALERT x WHERE x.CLIENT_SENSITIVITY_IND = 'N' ORDER BY CLIENT_ID FOR READ ONLY WITH UR";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void mustDeleteLimitedAccessRecords_Args__() throws Exception {
    boolean actual = target.mustDeleteLimitedAccessRecords();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalizeSingle_Args__List() throws Exception {
    List<EsSafetyAlert> recs = new ArrayList<EsSafetyAlert>();
    ReplicatedSafetyAlerts actual = target.normalizeSingle(recs);
    ReplicatedSafetyAlerts expected = new ReplicatedSafetyAlerts();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_Args__List() throws Exception {
    List<EsSafetyAlert> recs = new ArrayList<EsSafetyAlert>();
    List<ReplicatedSafetyAlerts> actual = target.normalize(recs);
    List<ReplicatedSafetyAlerts> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void prepareUpsertRequest_Args__ElasticSearchPerson__ReplicatedSafetyAlerts()
      throws Exception {
    ElasticSearchPerson esp = new ElasticSearchPerson();
    ReplicatedSafetyAlerts safetyAlerts = new ReplicatedSafetyAlerts();
    UpdateRequest actual = target.prepareUpsertRequest(esp, safetyAlerts);
    UpdateRequest expected = new UpdateRequest();
    assertThat(actual, notNullValue());
  }

  @Test
  public void extract_Args__ResultSet() throws Exception {
    final EsSafetyAlert actual = target.extract(rs);
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = SQLException.class)
  public void extract_Args__ResultSet_T__SQLException() throws Exception {
    when(rs.next()).thenThrow(new SQLException());
    when(rs.getString(any(String.class))).thenThrow(new SQLException());
    target.extract(rs);
  }

  @Test
  public void main_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/CWS-NS3/client_indexer_time.txt", "-S"};
    SafetyAlertIndexerJob.main(args);
  }

  @Test
  public void isInitialLoadJdbc_Args__() throws Exception {
    boolean actual = target.isInitialLoadJdbc();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPartitionRanges_Args__() throws Exception {
    final javax.persistence.Query q = mock(javax.persistence.Query.class);
    when(em.createNativeQuery(any(String.class), any(Class.class))).thenReturn(q);
    when(q.setParameter(any(String.class), any(String.class))).thenReturn(q);

    List actual = target.getPartitionRanges();
    assertThat(actual.size(), is(1));
  }

  @Test
  public void getOptionalElementName_Args__() throws Exception {
    String actual = target.getOptionalElementName();
    String expected = "safety_alerts";
    assertThat(actual, is(equalTo(expected)));
  }

}
