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

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPerson.ESOptionalCollection;
import gov.ca.cwds.data.persistence.cms.EsClientPerson;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;

public class ClientPersonIndexerJobTest extends Goddard<ReplicatedClient, EsClientPerson> {

  ReplicatedClientDao dao;
  ClientPersonIndexerJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    when(rs.next()).thenReturn(true, true, false);
    dao = new ReplicatedClientDao(sessionFactory);
    target = new ClientPersonIndexerJob(dao, esDao, lastRunFile, mapper, flightPlan);
  }

  @Test
  public void type() throws Exception {
    assertThat(ClientPersonIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void useTransformThread_A$() throws Exception {
    final boolean actual = target.useTransformThread();
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPrepLastChangeSQL_A$() throws Exception {
    // final Date date = target.determineLastSuccessfulRunTime();
    // NeutronDateTimeFormat.LAST_RUN_DATE_FORMAT.formatter().format(datetime);
    // target.writeLastSuccessfulRunTime(datetime);
    // target.getFlightPlan().setOverrideLastRunTime(lastRunTime);
    final String actual = target.getPrepLastChangeSQL();
    // final String expected =
    // "\"INSERT INTO GT_ID (IDENTIFIER)\\nSELECT DISTINCT CLT.IDENTIFIER \\nFROM CLIENT_T clt
    // \\nWHERE CLT.IBMSNAP_LOGMARKER > '2018-12-31 03:21:12.000' \\nUNION SELECT DISTINCT
    // cla.FKCLIENT_T AS IDENTIFIER \\nFROM CL_ADDRT cla \\nWHERE CLA.IBMSNAP_LOGMARKER >
    // '2018-12-31 03:21:12.000' \\nUNION SELECT DISTINCT cla.FKCLIENT_T AS IDENTIFIER \\nFROM
    // CL_ADDRT cla \\nJOIN ADDRS_T adr ON cla.FKADDRS_T = adr.IDENTIFIER \\nWHERE
    // ADR.IBMSNAP_LOGMARKER > '2018-12-31 03:21:12.000' \\nUNION SELECT DISTINCT eth.ESTBLSH_ID AS
    // IDENTIFIER \\nFROM CLSCP_ET eth \\nWHERE ETH.ESTBLSH_CD = 'C' \\nAND ETH.IBMSNAP_LOGMARKER >
    // '2018-12-31 03:21:12.000' \"";
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void extract_A$ResultSet() throws Exception {
    final EsClientPerson actual = target.extract(rs);
    // EsClientPerson expected = null;
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = SQLException.class)
  public void extract_A$ResultSet_T$SQLException() throws Exception {
    when(rs.getString(any(String.class))).thenThrow(SQLException.class);
    target.extract(rs);
  }

  @Test
  public void getDenormalizedClass_A$() throws Exception {
    final Object actual = target.getDenormalizedClass();
    final Object expected = EsClientPerson.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadViewName_A$() throws Exception {
    final String actual = target.getInitialLoadViewName();
    final String expected = "MQT_CLIENT_ADDRESS";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getMQTName_A$() throws Exception {
    final String actual = target.getMQTName();
    final String expected = "MQT_CLIENT_ADDRESS";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJdbcOrderBy_A$() throws Exception {
    final String actual = target.getJdbcOrderBy();
    final String expected = " ORDER BY X.CLT_IDENTIFIER ";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalizeAndQueueIndex_A$List() throws Exception {
    final List<EsClientPerson> grpRecs = new ArrayList<EsClientPerson>();
    target.normalizeAndQueueIndex(grpRecs);
  }

  @Test
  public void getInitialLoadQuery_A$String() throws Exception {
    final String dbSchemaName = null;
    final String actual = target.getInitialLoadQuery(dbSchemaName);
    final String expected =
        "SELECT x.* FROM null.MQT_CLIENT_ADDRESS x WHERE X.CLT_IDENTIFIER BETWEEN ':fromId' AND ':toId'  AND x.CLT_SENSTV_IND = 'N'  ORDER BY X.CLT_IDENTIFIER  FOR READ ONLY WITH UR ";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void initialLoadProcessRangeResults_A$ResultSet() throws Exception {
    target.initialLoadProcessRangeResults(rs);
  }

  @Test(expected = SQLException.class)
  public void initialLoadProcessRangeResults_A$ResultSet_T$SQLException() throws Exception {
    when(rs.next()).thenThrow(SQLException.class);
    target.initialLoadProcessRangeResults(rs);
  }

  @Test
  public void validateAddresses_A$ReplicatedClient$ElasticSearchPerson() throws Exception {
    final ReplicatedClient client = new ReplicatedClient();
    final ElasticSearchPerson person = new ElasticSearchPerson();
    final boolean actual = target.validateAddresses(client, person);
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void validateDocument_A$ElasticSearchPerson() throws Exception {
    final ElasticSearchPerson person = new ElasticSearchPerson();
    person.setId(DEFAULT_CLIENT_ID);

    final ReplicatedClient rep = new ReplicatedClient();
    rep.setCommonLastName("Young");
    rep.setCommonFirstName("Angus");
    rep.setCommonMiddleName("McKinnon");
    rep.setBirthCity("Glasgow");

    dao = mock(ReplicatedClientDao.class);
    // TestClientIndexerJob target = new TestClientIndexerJob(dao, esDao, lastRunFile, mapper,
    // sessionFactory, flightRecorder, flightPlan);
    // target.setTxn(transaction);
    when(dao.find(any())).thenReturn(rep);

    boolean actual = target.validateDocument(person);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void threadRetrieveByJdbc_A$() throws Exception {
    target.threadRetrieveByJdbc();
  }

  @Test
  public void isInitialLoadJdbc_A$() throws Exception {
    final boolean actual = target.isInitialLoadJdbc();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPartitionRanges_A$() throws Exception {
    final List actual = target.getPartitionRanges();
    final List expected = new ArrayList<>();
    expected.add(pair);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void mustDeleteLimitedAccessRecords_A$() throws Exception {
    final boolean actual = target.mustDeleteLimitedAccessRecords();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_A$List() throws Exception {
    final List<EsClientPerson> recs = new ArrayList<EsClientPerson>();
    final List<ReplicatedClient> actual = target.normalize(recs);
    final List<ReplicatedClient> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void nextThreadNumber_A$() throws Exception {
    final int actual = target.nextThreadNumber();
    final int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void keepCollections_A$() throws Exception {
    final ESOptionalCollection[] actual = target.keepCollections();
    final ESOptionalCollection[] expected =
        {ESOptionalCollection.AKA, ESOptionalCollection.SAFETY_ALERT};
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void main_A$StringArray_T$Exception() throws Exception {
    final String[] args = new String[] {};
    ClientPersonIndexerJob.main(args);
  }

}
