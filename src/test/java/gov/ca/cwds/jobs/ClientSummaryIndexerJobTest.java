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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPersonAddress;
import gov.ca.cwds.data.persistence.cms.EsClientPerson;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClientAddress;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;

/**
 * 
 * @author CWDS API Team
 */
public class ClientSummaryIndexerJobTest extends Goddard<ReplicatedClient, EsClientPerson> {

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
    assertThat(ClientIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void extract_Args__ResultSet() throws Exception {
    when(rs.getString("CLT_IBMSNAP_OPERATION")).thenReturn("I");
    final EsClientPerson actual = target.extract(rs);
    final EsClientPerson expected = new EsClientPerson();
    final short s = (short) 0;
    // expected.setCltBirthCountryCodeType(s);
    // expected.setCltBirthStateCodeType(s);
    // expected.setCltDriverLicenseStateCodeType(s);
    expected.setCltImmigrationCountryCodeType(s);
    // expected.setCltMaritalStatusType(s);
    // expected.setCltNameType(s);
    expected.setCltPrimaryEthnicityType(s);
    expected.setCltPrimaryLanguageType(s);
    expected.setCltSecondaryLanguageType(s);
    expected.setCltReligionType(s);
    expected.setClaAddressType(s);
    expected.setClaAddressType(s);
    expected.setClaAddressType(s);
    expected.setAdrEmergencyExtension(0);
    // expected.setAdrPrimaryExtension(0);
    // expected.setAdrState(s);
    // expected.setAdrZip4(s);
    // expected.setAdrUnitDesignationCd(s);
    // expected.setAdrPostDirCd(0);
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, notNullValue());
  }

  @Test
  public void getDenormalizedClass_Args__() throws Exception {
    Object actual = target.getDenormalizedClass();
    Object expected = EsClientPerson.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getViewName_Args__() throws Exception {
    String actual = target.getInitialLoadViewName();
    String expected = "MQT_CLIENT_ADDRESS";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_Args__List() throws Exception {
    List<EsClientPerson> recs = new ArrayList<EsClientPerson>();
    List<ReplicatedClient> actual = target.normalize(recs);
    List<ReplicatedClient> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadViewName_Args__() throws Exception {
    String actual = target.getInitialLoadViewName();
    String expected = "MQT_CLIENT_ADDRESS";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJdbcOrderBy_Args__() throws Exception {
    final String actual = target.getJdbcOrderBy().trim().toUpperCase();
    final String expected = "ORDER BY X.CLT_IDENTIFIER";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadQuery_Args__String() throws Exception {
    final String actual =
        target.getInitialLoadQuery("CWSINT").trim().replace("\\s{2,}", " ").replaceAll("  ", " ");
    final String expected =
        "SELECT x.* FROM CWSINT.MQT_CLIENT_ADDRESS x WHERE X.CLT_IDENTIFIER BETWEEN ':fromId' AND ':toId' AND x.CLT_SENSTV_IND = 'N' ORDER BY X.CLT_IDENTIFIER FOR READ ONLY WITH UR";
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void normalizeAndQueueIndex() throws Exception {
  // List<EsClientPerson> grpRecs = new ArrayList<EsClientPerson>();
  // target.normalizeAndQueueIndex(grpRecs);
  // }

  // @Test
  // public void threadExtractJdbc_Args__() throws Exception {
  // target.threadRetrieveByJdbc();
  // }

  // @Test
  // public void pullRange_Args__Pair() throws Exception {
  // final Pair<String, String> p = pair;
  // target.pullRange(p, null);
  // }

  @Test(expected = NeutronRuntimeException.class)
  public void pullRange_Args__Pair__Exception() throws Exception {
    when(con.createStatement()).thenThrow(SQLException.class);
    final Pair<String, String> p = pair;

    TestClientIndexerJob target = new TestClientIndexerJob(dao, esDao, lastRunFile, mapper,
        sessionFactory, flightRecorder, flightPlan, launchDirector);
    target.setTxn(transaction);
    target.pullRange(p, null);
  }

  @Test
  public void getPartitionRanges_Args() throws Exception {
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    final List<Pair<String, String>> expected = new ArrayList<>();
    expected.add(pair);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPartitionRanges_RSQ() throws Exception {
    System.setProperty("DB_CMS_SCHEMA", "CWSRSQ");
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    assertThat(actual.size(), is(equalTo(1025)));
  }

  @Test
  public void mustDeleteLimitedAccessRecords_Args__() throws Exception {
    boolean actual = target.mustDeleteLimitedAccessRecords();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void mustDeleteLimitedAccessRecords_Args__2() throws Exception {
    when(flightPlan.isLoadSealedAndSensitive()).thenReturn(true);
    boolean actual = target.mustDeleteLimitedAccessRecords();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void useTransformThread_Args__() throws Exception {
    boolean actual = target.useTransformThread();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPrepLastChangeSQL_Args__() throws Exception {
    String actual = target.getPrepLastChangeSQL();
    assertThat(actual, is(notNullValue()));
  }

  // @Test
  // public void normalizeAndQueueIndex_Args__List() throws Exception {
  // List<EsClientPerson> grpRecs = new ArrayList<EsClientPerson>();
  // target.normalizeAndQueueIndex(grpRecs);
  // }

  @Test
  public void iterateRangeResults_Args__ResultSet() throws Exception {
    target.handleMainResults(rs);
  }

  @Test
  public void validateDocument_Args__ElasticSearchPerson() throws Exception {
    final ElasticSearchPerson person = new ElasticSearchPerson();
    person.setId(DEFAULT_CLIENT_ID);

    final ReplicatedClient rep = new ReplicatedClient();
    rep.setCommonLastName("Young");
    rep.setCommonFirstName("Angus");
    rep.setCommonMiddleName("McKinnon");
    rep.setBirthCity("Glasgow");

    dao = mock(ReplicatedClientDao.class);
    TestClientIndexerJob target = new TestClientIndexerJob(dao, esDao, lastRunFile, mapper,
        sessionFactory, flightRecorder, flightPlan, launchDirector);
    target.setTxn(transaction);
    when(dao.find(any())).thenReturn(rep);

    boolean actual = target.validateDocument(person);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void threadRetrieveByJdbc_Args__() throws Exception {
  // target.threadRetrieveByJdbc();
  // }

  @Test
  public void isInitialLoadJdbc_Args__() throws Exception {
    boolean actual = target.isInitialLoadJdbc();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPartitionRanges_Args__() throws Exception {
    final List actual = target.getPartitionRanges();
    final List expected = new ArrayList<>();
    expected.add(pair);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void main_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/mylittlepony/client_indexer_time.txt", "-S"};
    ClientIndexerJob.main(args);
  }

  @Test
  public void getMQTName_Args__() throws Exception {
    final String actual = target.getMQTName();
    final String expected = "MQT_CLIENT_ADDRESS";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void handleRangeResults_Args__ResultSet() throws Exception {
    target.handleMainResults(rs);
  }

  @Test
  public void handleRangeResults_Args__ResultSet__2() throws Exception {
    target.handleMainResults(rs);
  }

  @Test
  public void validateAddresses_Args__ReplicatedClient__ElasticSearchPerson() throws Exception {
    ReplicatedClient client = new ReplicatedClient();
    ElasticSearchPerson person = new ElasticSearchPerson();

    ReplicatedAddress repAddr = new ReplicatedAddress();
    repAddr.setId(DEFAULT_CLIENT_ID);
    repAddr.setCity("Provo");
    repAddr.setZip("80604");

    ReplicatedClientAddress ca = new ReplicatedClientAddress();
    ca.addAddress(repAddr);
    client.addClientAddress(ca);

    ElasticSearchPersonAddress espAddr = new ElasticSearchPersonAddress();
    espAddr.setId(DEFAULT_CLIENT_ID);
    espAddr.setCity("Provo");
    espAddr.setZip("80604");
    esp.getAddresses().add(espAddr);

    boolean actual = target.validateAddresses(client, person);
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void validateAddresses_Args__ReplicatedClient__ElasticSearchPerson__2() throws Exception {
    ReplicatedClient client = new ReplicatedClient();
    ElasticSearchPerson person = new ElasticSearchPerson();

    ReplicatedAddress repAddr = new ReplicatedAddress();
    repAddr.setId(DEFAULT_CLIENT_ID);
    repAddr.setCity("Provo");
    repAddr.setZip("80604");

    ReplicatedClientAddress ca = new ReplicatedClientAddress();
    ca.addAddress(repAddr);
    client.addClientAddress(ca);

    boolean actual = target.validateAddresses(client, person);
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void validateAddresses_Args__ReplicatedClient__ElasticSearchPerson__3() throws Exception {
    ReplicatedClient client = new ReplicatedClient();
    ElasticSearchPerson person = new ElasticSearchPerson();

    boolean actual = target.validateAddresses(client, person);
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void nextThreadNumber_Args__() throws Exception {
    int actual = target.nextThreadNumber();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

}
