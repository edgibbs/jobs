package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.persistence.cms.PlacementHomeAddress;
import gov.ca.cwds.data.persistence.cms.PlacementHomeAddressTest;
import gov.ca.cwds.data.persistence.cms.client.RawAddressTest;
import gov.ca.cwds.data.persistence.cms.client.RawAka;
import gov.ca.cwds.data.persistence.cms.client.RawAkaTest;
import gov.ca.cwds.data.persistence.cms.client.RawCase;
import gov.ca.cwds.data.persistence.cms.client.RawCaseTest;
import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.client.RawClientAddress;
import gov.ca.cwds.data.persistence.cms.client.RawClientAddressTest;
import gov.ca.cwds.data.persistence.cms.client.RawClientCounty;
import gov.ca.cwds.data.persistence.cms.client.RawClientCountyTest;
import gov.ca.cwds.data.persistence.cms.client.RawClientTest;
import gov.ca.cwds.data.persistence.cms.client.RawCsec;
import gov.ca.cwds.data.persistence.cms.client.RawCsecTest;
import gov.ca.cwds.data.persistence.cms.client.RawEthnicity;
import gov.ca.cwds.data.persistence.cms.client.RawEthnicityTest;
import gov.ca.cwds.data.persistence.cms.client.RawSafetyAlert;
import gov.ca.cwds.data.persistence.cms.client.RawSafetyAlertTest;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.ClientPersonIndexerJob;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;
import gov.ca.cwds.neutron.rocket.PeopleSummaryThreadHandler.STEP;

public class PeopleSummaryThreadHandlerTest extends Goddard<ReplicatedClient, RawClient> {

  PeopleSummaryThreadHandler target;
  ClientPersonIndexerJob rocket;
  ReplicatedClientDao dao;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    when(rs.next()).thenReturn(true).thenReturn(false);
    dao = new ReplicatedClientDao(sessionFactory);
    rocket =
        new ClientPersonIndexerJob(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
    rocket.allocateThreadHandler();
    target = new PeopleSummaryThreadHandler(rocket);
  }

  @Test
  public void type() throws Exception {
    assertThat(PeopleSummaryThreadHandler.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void handleMainResults_A$ResultSet() throws Exception {
    target.handleMainResults(rs, con);
  }

  @Test(expected = SQLException.class)
  public void handleMainResults_A$ResultSet_T$SQLException() throws Exception {
    when(rs.getString(any(String.class))).thenThrow(SQLException.class);
    doThrow(new SQLException()).when(con).commit();
    target.handleMainResults(rs, con);
  }

  @Test
  public void handleSecondaryJdbc_A$Connection$Pair() throws Exception {
    final Pair<String, String> range = pair;
    target.handleSecondaryJdbc(con, range);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void handleSecondaryJdbc_A$Connection$Pair_T$SQLException() throws Exception {
    doThrow(new SQLException()).when(con).commit();
    doThrow(new SQLException()).when(rs).getString(any());
    target.handleSecondaryJdbc(con, pair);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void handleSecondaryJdbc_bomb() throws Exception {
    doThrow(new SQLException()).when(con).commit();
    doThrow(new SQLException()).when(con).rollback();
    doThrow(new SQLException()).when(rs).getString(any());
    target.handleSecondaryJdbc(con, pair);
  }

  @Test
  public void handleJdbcDone_A$Pair() throws Exception {
    final Pair<String, String> range = pair;
    target.handleJdbcDone(range);
  }

  @Test
  public void handleStartRange_A$Pair() throws Exception {
    final Pair<String, String> range = pair;
    target.handleStartRange(range);
  }

  @Test
  public void handleFinishRange_A$Pair() throws Exception {
    final Pair<String, String> range = pair;
    target.handleFinishRange(range);
  }

  @Test
  public void getResults_A$() throws Exception {
    List<ReplicatedClient> actual = target.getResults();
    List<ReplicatedClient> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronRuntimeException.class)
  public void fetchLastRunNormalizedResults_bomb() throws Exception {
    final Date lastRunDate = new SimpleDateFormat("yyyy-mm-dd").parse("10-31-2017");
    final Set<String> deletionResults = new HashSet<>();

    bombResultSet();
    final List<ReplicatedClient> actual =
        target.fetchLastRunNormalizedResults(lastRunDate, deletionResults);
    final List<ReplicatedClient> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void pickPrepDml_A$String$String() throws Exception {
    String sqlInitialLoad = "SELECT x.IDENTIFIER FROM GT_ID x";
    String sqlLastChange = "SELECT x.IDENTIFIER FROM GT_ID x";
    String actual = target.pickPrepDml(sqlInitialLoad, sqlLastChange);
    String expected = "SELECT x.IDENTIFIER FROM GT_ID x";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void pickPrepDml_A$String$String_T$NeutronCheckedException() throws Exception {
    when(rs.next()).thenThrow(NeutronCheckedException.class);
    when(preparedStatement.executeUpdate()).thenThrow(NeutronCheckedException.class);
    rocket = mock(ClientPersonIndexerJob.class);
    when(rocket.getFlightPlan()).thenThrow(NeutronCheckedException.class);
    target = new PeopleSummaryThreadHandler(rocket);
    String sqlInitialLoad = null;
    String sqlLastChange = null;
    target.pickPrepDml(sqlInitialLoad, sqlLastChange);
  }

  @Test
  public void addAll_A$Collection() throws Exception {
    Collection<ReplicatedClient> collection = mock(Collection.class);
    target.addAll(collection);
  }

  @Test
  public void clear_A$() throws Exception {
    target.clear();
  }

  @Test
  public void normalize_A$List() throws Exception {
    final List<RawClient> grpRecs = new ArrayList<>();
    target.normalize(grpRecs);
  }

  @Test
  public void prepAffectedClients_A$PreparedStatement$Pair() throws Exception {
    final Pair<String, String> p = pair;
    target.prepPlacementClients(preparedStatement, p);
  }

  @Test(expected = SQLException.class)
  public void prepAffectedClients_A$PreparedStatement$Pair_T$SQLException() throws Exception {
    when(rs.next()).thenThrow(SQLException.class);
    when(preparedStatement.executeUpdate()).thenThrow(SQLException.class);
    final Pair<String, String> p = pair;
    target.prepPlacementClients(preparedStatement, p);
  }

  @Test
  public void readPlacementAddress_A$PreparedStatement() throws Exception {
    target.readPlacementAddress(preparedStatement);
  }

  @Test(expected = SQLException.class)
  public void readPlacementAddress_A$PreparedStatement_T$SQLException() throws Exception {
    when(rs.next()).thenThrow(SQLException.class);
    target.readPlacementAddress(preparedStatement);
  }

  @Test
  public void getNormalized_A$() throws Exception {
    Map<String, ReplicatedClient> actual = target.getNormalized();
    Map<String, ReplicatedClient> expected = new HashMap<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isDoneHandlerRetrieve_A$() throws Exception {
    final boolean actual = target.isDoneHandlerRetrieve();
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void doneRetrieve_A$() throws Exception {
    target.doneThreadRetrieve();
  }

  @Test
  public void read_A$PreparedStatement$Consumer() throws Exception {
    rocket = mock(ClientPersonIndexerJob.class);
    when(rocket.isRunning()).thenReturn(true);
    rocket.allocateThreadHandler();
    target = new PeopleSummaryThreadHandler(rocket);
    Consumer<ResultSet> consumer = mock(Consumer.class);
    target.read(preparedStatement, consumer);
  }

  @Test
  public void readClient_A$ResultSet() throws Exception {
    RawClientTest.prepResultSetGood(rs);
    when(rs.next()).thenReturn(true).thenReturn(false);
    target.readClient(rs);
    System.out.println(target.getRawClients());
  }

  @Test(expected = NeutronRuntimeException.class)
  public void readClient_bomb() throws Exception {
    RawClientTest.prepResultSetGood(rs);
    when(rs.next()).thenReturn(true).thenReturn(false);
    bombResultSet();
    target.readClient(rs);
  }

  @Test
  public void readClientAddress_A$ResultSet() throws Exception {
    readClient_A$ResultSet();
    RawClientAddressTest.prepResultSetGood(rs);
    when(rs.next()).thenReturn(true).thenReturn(false);
    target.readClientAddress(rs);
  }

  @Test
  public void readClientAddress_orphan() throws Exception {
    readClient_A$ResultSet();
    RawClientAddressTest.prepResultSetGood(rs);
    when(rs.next()).thenReturn(true).thenReturn(false);
    when(rs.getString(RawClientAddress.ColumnPosition.CLT_IDENTIFIER.ordinal()))
        .thenReturn("7654321xyz");
    target.readClientAddress(rs);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void readClientAddress_bomb() throws Exception {
    readClient_A$ResultSet();
    RawClientAddressTest.prepResultSetGood(rs);
    when(rs.next()).thenReturn(true).thenReturn(false);
    bombResultSet();
    target.readClientAddress(rs);
  }

  @Test
  public void readAddress_A$ResultSet() throws Exception {
    readClientAddress_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawAddressTest.prepResultSetGood(rs);
    target.readAddress(rs);
  }

  @Test
  public void readAddress_orphan_client() throws Exception {
    readClientAddress_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawAddressTest.prepResultSetGood(rs);
    when(rs.getString(RawClientAddress.ColumnPosition.CLT_IDENTIFIER.ordinal()))
        .thenReturn("7654321xyz");
    target.readAddress(rs);
  }

  @Test
  public void readAddress_orphan_client_address() throws Exception {
    readClientAddress_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawAddressTest.prepResultSetGood(rs);
    when(rs.getString(RawClientAddress.ColumnPosition.CLA_IDENTIFIER.ordinal()))
        .thenReturn("7654321xyz");
    target.readAddress(rs);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void readAddress_bomb() throws Exception {
    readClientAddress_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawAddressTest.prepResultSetGood(rs);
    bombResultSet();
    target.readAddress(rs);
  }

  @Test
  public void readClientCounty_A$ResultSet() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawClientCountyTest.prepResultSetGood(rs);
    target.readClientCounty(rs);
  }

  @Test
  public void readClientCounty_orphan() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawClientCountyTest.prepResultSetGood(rs);
    when(rs.getString(RawClientCounty.ColumnPosition.CLT_IDENTIFIER.ordinal()))
        .thenReturn("7654321xyz");
    target.readClientCounty(rs);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void readClientCounty_bomb() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawClientCountyTest.prepResultSetGood(rs);
    bombResultSet();
    target.readClientCounty(rs);
  }

  @Test
  public void readAka_A$ResultSet() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawAkaTest.prepResultSetGood(rs);
    target.readAka(rs);
  }

  @Test
  public void readAka_orphan() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawAkaTest.prepResultSetGood(rs);
    when(rs.getString(RawAka.ColumnPosition.CLT_IDENTIFIER.ordinal())).thenReturn("7654321xyz");
    target.readAka(rs);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void readAka_bomb() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawAkaTest.prepResultSetGood(rs);
    bombResultSet();
    target.readAka(rs);
  }

  @Test
  public void readCase_A$ResultSet() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawCaseTest.prepResultSetGood(rs);
    target.readCase(rs);
  }

  @Test
  public void readCase_orphan() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawCaseTest.prepResultSetGood(rs);
    when(rs.getString(RawCase.ColumnPosition.CLT_IDENTIFIER.ordinal())).thenReturn("7654321xyz");
    target.readCase(rs);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void readCase_bomb() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawCaseTest.prepResultSetGood(rs);
    bombResultSet();
    target.readCase(rs);
  }

  @Test
  public void readCsec_A$ResultSet() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawCsecTest.prepResultSetGood(rs);
    target.readCsec(rs);
  }

  @Test
  public void readCsec_orphan() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawCsecTest.prepResultSetGood(rs);
    when(rs.getString(RawCsec.ColumnPosition.CLT_IDENTIFIER.ordinal())).thenReturn("7654321xyz");
    target.readCsec(rs);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void readCsec_bomb() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawCsecTest.prepResultSetGood(rs);
    bombResultSet();
    target.readCsec(rs);
  }

  @Test
  public void readEthnicity_A$ResultSet() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawEthnicityTest.prepResultSetGood(rs);
    target.readEthnicity(rs);
  }

  @Test
  public void readEthnicity_orphan() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawEthnicityTest.prepResultSetGood(rs);
    when(rs.getString(RawEthnicity.ColumnPosition.CLT_IDENTIFIER.ordinal()))
        .thenReturn("7654321xyz");
    target.readEthnicity(rs);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void readEthnicity_bomb() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawEthnicityTest.prepResultSetGood(rs);
    bombResultSet();
    target.readEthnicity(rs);
  }

  @Test
  public void readSafetyAlert_A$ResultSet() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawSafetyAlertTest.prepResultSetGood(rs);
    target.readSafetyAlert(rs);
  }

  @Test
  public void readSafetyAlert_orphan() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawSafetyAlertTest.prepResultSetGood(rs);
    when(rs.getString(RawSafetyAlert.ColumnPosition.CLT_IDENTIFIER.ordinal()))
        .thenReturn("7654321xyz");
    target.readSafetyAlert(rs);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void readSafetyAlert_bomb() throws Exception {
    readClient_A$ResultSet();
    when(rs.next()).thenReturn(true).thenReturn(false);
    RawSafetyAlertTest.prepResultSetGood(rs);
    bombResultSet();
    target.readSafetyAlert(rs);
  }

  @Test
  public void mapReplicatedClient_A$PlacementHomeAddress() throws Exception {
    readClient_A$ResultSet();
    handleJdbcDone_A$Pair();
    PlacementHomeAddressTest.prepResultSetGood(rs);
    final PlacementHomeAddress pha = new PlacementHomeAddress(rs);
    target.mapReplicatedClient(pha);
  }

  @Test
  public void handleMainResults_A$ResultSet$Connection() throws Exception {
    target.handleMainResults(rs, con);
  }

  @Test(expected = SQLException.class)
  public void handleMainResults_A$ResultSet$Connection_T$SQLException() throws Exception {
    when(rs.next()).thenThrow(SQLException.class);
    when(rs.getString(any(String.class))).thenThrow(SQLException.class);
    doThrow(SQLException.class).when(con).commit();
    target.handleMainResults(rs, con);
  }

  @Test
  public void loadClientRange_A$PreparedStatement$Pair() throws Exception {
    final PreparedStatement stmtInsClient = mock(PreparedStatement.class);
    final Pair<String, String> range = pair;
    target.loadClientRange(con, stmtInsClient, range);
  }

  @Test(expected = SQLException.class)
  public void loadClientRange_A$PreparedStatement$Pair_T$SQLException() throws Exception {
    doThrow(SQLException.class).when(preparedStatement).setString(any(Integer.class),
        any(String.class));
    when(preparedStatement.executeUpdate()).thenThrow(SQLException.class);
    final Pair<String, String> range = pair;
    target.loadClientRange(con, preparedStatement, range);
  }

  @Test
  public void prepPlacementClients_A$PreparedStatement$Pair() throws Exception {
    final PreparedStatement stmt = mock(PreparedStatement.class);
    final Pair<String, String> p = pair;
    target.prepPlacementClients(stmt, p);
  }

  @Test(expected = SQLException.class)
  public void prepPlacementClients_A$PreparedStatement$Pair_T$SQLException() throws Exception {
    when(rs.next()).thenThrow(SQLException.class);
    when(rs.getString(any(String.class))).thenThrow(SQLException.class);
    doThrow(SQLException.class).when(preparedStatement).setString(any(Integer.class),
        any(String.class));
    when(preparedStatement.executeUpdate()).thenThrow(SQLException.class);
    doThrow(SQLException.class).when(con).commit();
    final Pair<String, String> p = pair;
    target.prepPlacementClients(preparedStatement, p);
  }

  @Test
  public void doneThreadRetrieve_A$() throws Exception {
    target.doneThreadRetrieve();
  }

  @Test
  public void getRocket_A$() throws Exception {
    ClientPersonIndexerJob actual = target.getRocket();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void step_A$Object() throws Exception {
    target.step(STEP.SEL_AKA);
  }

  @Test
  public void loadClientRange_A$Connection$PreparedStatement$Pair() throws Exception {
    final PreparedStatement stmtInsClient = preparedStatement;
    final Pair<String, String> range = pair;
    int actual = target.loadClientRange(con, stmtInsClient, range);
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = SQLException.class)
  public void loadClientRange_A$Connection$PreparedStatement$Pair_T$SQLException()
      throws Exception {
    final PreparedStatement stmtInsClient = preparedStatement;
    final Pair<String, String> range = pair;
    bombResultSet();

    target.loadClientRange(con, stmtInsClient, range);
  }

  @Test
  public void isInitialLoad_A$() throws Exception {
    final boolean actual = target.isInitialLoad();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void addOtherTimings_A$() throws Exception {
    target.addOtherTimings();
  }

  @Test
  public void calcReplicationDelay_A$() throws Exception {
    target.calcReplicationDelay();
  }

  @Test
  public void getEventType_A$() throws Exception {
    String actual = target.getEventType();
    String expected = "neutron_initial_load_client";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRocket_A$ClientPersonIndexerJob() throws Exception {
    ClientPersonIndexerJob rocket_ = mock(ClientPersonIndexerJob.class);
    target.setRocket(rocket_);
  }

}
