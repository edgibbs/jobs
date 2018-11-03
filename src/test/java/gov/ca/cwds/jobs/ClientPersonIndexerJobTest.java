package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPerson.ESOptionalCollection;
import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;

public class ClientPersonIndexerJobTest extends Goddard<ReplicatedClient, RawClient> {

  ReplicatedClientDao dao;
  ClientPersonIndexerJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    when(rs.next()).thenReturn(true, true, false);
    dao = new ReplicatedClientDao(sessionFactory);
    target =
        new ClientPersonIndexerJob(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
    target.allocateThreadHandler();
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

    // NOTE: the date is **dynamic** (current date/time).
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
    final RawClient actual = target.extract(rs);
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
    final Object expected = RawClient.class;
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
    final String expected = " ORDER BY CLT_IDENTIFIER ";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadQuery_A$String() throws Exception {
    final String dbSchemaName = "CWSRS4";
    final String actual = target.getInitialLoadQuery(dbSchemaName).replaceAll("  ", " ");
    final String expected =
        "SELECT '1234567abc' AS CLT_IDENTIFIER FROM SYSIBM.SYSDUMMY1 X WHERE 1=2 AND '0' BETWEEN ':fromId' AND ':toId' ORDER BY CLT_IDENTIFIER FOR READ ONLY WITH UR ";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void initialLoadProcessRangeResults_A$ResultSet() throws Exception {
    target.handleMainResults(rs, con);
  }

  @Test(expected = SQLException.class)
  public void initialLoadProcessRangeResults_A$ResultSet_T$SQLException() throws Exception {
    when(rs.next()).thenThrow(SQLException.class);
    doThrow(SQLException.class).when(con).commit();
    target.handleMainResults(rs, con);
  }

  @Test
  public void validateAddresses_A$ReplicatedClient$ElasticSearchPerson() throws Exception {
    final ElasticSearchPerson person = new ElasticSearchPerson();
    person.setLastName("Young");
    person.setFirstName("Angus");
    person.setMiddleName("McKinnon");
    person.setId(DEFAULT_CLIENT_ID);

    final ReplicatedClient rep = new ReplicatedClient();
    rep.setCommonLastName("Young");
    rep.setCommonFirstName("Angus");
    rep.setCommonMiddleName("McKinnon");
    rep.setBirthCity("Glasgow");

    dao = mock(ReplicatedClientDao.class);
    when(dao.find(any())).thenReturn(rep);

    final TestClientPersonIndexerJob target = new TestClientPersonIndexerJob(dao, esDao,
        lastRunFile, mapper, sessionFactory, null, flightPlan);
    target.setTxn(transaction);
    final boolean actual = target.validateAddresses(rep, person);
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void validateDocument_A$ElasticSearchPerson() throws Exception {
    final ElasticSearchPerson person = new ElasticSearchPerson();
    person.setId(DEFAULT_CLIENT_ID);
    person.setLastName("Young");
    person.setFirstName("Angus");
    person.setMiddleName("McKinnon");

    final ReplicatedClient rep = new ReplicatedClient();
    rep.setId(DEFAULT_CLIENT_ID);
    rep.setCommonLastName("Young");
    rep.setCommonFirstName("Angus");
    rep.setCommonMiddleName("McKinnon");
    rep.setBirthCity("Glasgow");

    dao = mock(ReplicatedClientDao.class);
    when(dao.find(any())).thenReturn(rep);

    final TestClientPersonIndexerJob target = new TestClientPersonIndexerJob(dao, esDao,
        lastRunFile, mapper, sessionFactory, null, flightPlan);
    target.setTxn(transaction);
    final boolean actual = target.validateDocument(person);
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void validateDocument_A$ElasticSearchPerson__explode() throws Exception {
    final ElasticSearchPerson person = new ElasticSearchPerson();
    person.setId(DEFAULT_CLIENT_ID);
    person.setLastName("Young");
    person.setFirstName("Angus");
    person.setMiddleName("McKinnon");

    final ReplicatedClient rep = new ReplicatedClient();
    rep.setCommonLastName("Young");
    rep.setCommonFirstName("Angus");
    rep.setCommonMiddleName("McKinnon");
    rep.setBirthCity("Glasgow");

    dao = mock(ReplicatedClientDao.class);
    when(dao.find(any())).thenThrow(SQLException.class);

    final TestClientPersonIndexerJob target = new TestClientPersonIndexerJob(dao, esDao,
        lastRunFile, mapper, sessionFactory, null, flightPlan);
    target.setTxn(transaction);

    final boolean actual = target.validateDocument(person);
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  @Ignore
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
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    final List<Pair<String, String>> expected = new ArrayList<>();
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
    final List<RawClient> recs = new ArrayList<>();
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

  @Test(expected = NeutronCheckedException.class)
  public void launch_A$Date() throws Exception {
    when(flightPlan.getOverrideLastRunStartTime()).thenThrow(IllegalStateException.class);
    Date lastSuccessfulRunTime = new Date();
    Date actual = target.launch(lastSuccessfulRunTime);
    Date expected = new Date();
    assertThat(expected, is(greaterThanOrEqualTo(actual)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void launch_A$Date_T$NeutronCheckedException() throws Exception {
    when(esDao.getConfig()).thenThrow(IllegalStateException.class);
    Date lastSuccessfulRunTime = new Date();
    target.launch(lastSuccessfulRunTime);
  }

  @Test
  public void fetchLastRunResults_A$Date$Set() throws Exception {
    Date lastRunDate = new Date();
    Set<String> deletionResults = new HashSet<>();

    List<ReplicatedClient> actual = target.fetchLastRunResults(lastRunDate, deletionResults);
    List<ReplicatedClient> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPrepLastChangeSQLs_A$() throws Exception {
    String[] actual = target.getPrepLastChangeSQLs();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void isLargeLoad_A$() throws Exception {
    boolean actual = target.isLargeLoad();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void handleStartRange_A$Pair() throws Exception {
    Pair<String, String> range = pair;
    target.handleStartRange(range);
  }

  @Test
  public void handleFinishRange_A$Pair() throws Exception {
    Pair<String, String> range = pair;
    target.handleFinishRange(range);
  }

  @Test
  public void handleSecondaryJdbc_A$Connection$Pair() throws Exception {
    Pair<String, String> range = pair;
    target.handleSecondaryJdbc(con, range);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void handleSecondaryJdbc_A$Connection$Pair_T$SQLException() throws Exception {
    when(con.prepareStatement(any(String.class))).thenThrow(SQLException.class);
    when(con.prepareStatement(any(String.class), any(Integer.class), any(Integer.class)))
        .thenThrow(SQLException.class);
    Pair<String, String> range = pair;
    target.handleSecondaryJdbc(con, range);
  }

  @Test
  public void handleMainResults_A$ResultSet$Connection() throws Exception {
    target.handleMainResults(rs, con);
  }

  @Test(expected = SQLException.class)
  public void handleMainResults_A$ResultSet$Connection_T$SQLException() throws Exception {
    doThrow(SQLException.class).when(con).commit();
    target.handleMainResults(rs, con);
  }

  @Test
  public void handleJdbcDone_A$Pair() throws Exception {
    Pair<String, String> range = pair;
    target.handleJdbcDone(range);
  }

  @Test
  public void allocateThreadHandler_A$() throws Exception {
    target.allocateThreadHandler();
  }

  @Test
  public void deallocateThreadHandler_A$() throws Exception {
    target.deallocateThreadHandler();
  }

  @Test
  public void startMultiThreadRetrieve_A$() throws Exception {
    target.startMultiThreadRetrieve();
  }

  @Test
  public void doneMultiThreadRetrieve_A$() throws Exception {
    target.doneMultiThreadRetrieve();
  }

  @Test
  public void doneRetrieve_A$() throws Exception {
    target.doneRetrieve();
  }

  @Test
  public void getRerunClients_A$() throws Exception {
    Deque<String> actual = target.getRerunClients();
    Deque<String> expected = new ConcurrentLinkedDeque<>();
    assertThat(actual.size(), is(equalTo(expected.size())));
  }

  @Test
  public void addRerunClient_A$String() throws Exception {
    String clientId = DEFAULT_CLIENT_ID;
    target.addRerunClient(clientId);
  }

}
