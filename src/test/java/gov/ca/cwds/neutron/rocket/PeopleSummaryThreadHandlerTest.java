package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.persistence.cms.EsClientPerson;
import gov.ca.cwds.data.persistence.cms.PlacementHomeAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.ClientPersonIndexerJob;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;

public class PeopleSummaryThreadHandlerTest extends Goddard<ReplicatedClient, EsClientPerson> {

  PeopleSummaryThreadHandler target;
  ClientPersonIndexerJob rocket;
  ReplicatedClientDao dao;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    when(rs.next()).thenReturn(true, true, false);
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
    target.handleMainResults(rs, con);
  }

  @Test
  public void handleSecondaryJdbc_A$Connection$Pair() throws Exception {
    Pair<String, String> range = pair;
    target.handleSecondaryJdbc(con, range);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void handleSecondaryJdbc_A$Connection$Pair_T$SQLException() throws Exception {
    doThrow(new SQLException()).when(con).commit();
    doThrow(new SQLException()).when(rs).getString(any());
    target.handleSecondaryJdbc(con, pair);
  }

  @Test
  public void mapReplicatedClient_A$PlacementHomeAddress() throws Exception {
    PlacementHomeAddress pha = mock(PlacementHomeAddress.class);
    target.mapReplicatedClient(pha);
  }

  @Test
  public void handleJdbcDone_A$Pair() throws Exception {
    Pair<String, String> range = pair;
    target.handleJdbcDone(range);
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
  public void getResults_A$() throws Exception {
    List<ReplicatedClient> actual = target.getResults();
    List<ReplicatedClient> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  @Ignore
  public void fetchLastRunNormalizedResults_A$Date$Set() throws Exception {
    Date lastRunDate = new SimpleDateFormat("yyyy-mm-dd").parse("10-31-2017");
    final Set<String> deletionResults = new HashSet<>();
    List<ReplicatedClient> actual =
        target.fetchLastRunNormalizedResults(lastRunDate, deletionResults);
    List<ReplicatedClient> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void pickPrepDml_A$String$String() throws Exception {
    String sqlInitialLoad = null;
    String sqlLastChange = null;
    String actual = target.pickPrepDml(sqlInitialLoad, sqlLastChange);
    String expected = null;
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
    List<EsClientPerson> grpRecs = new ArrayList<EsClientPerson>();
    target.normalize(grpRecs);
  }

  @Test
  public void prepAffectedClients_A$PreparedStatement$Pair() throws Exception {
    Pair<String, String> p = pair;
    target.prepPlacementClients(preparedStatement, p);
  }

  @Test(expected = SQLException.class)
  public void prepAffectedClients_A$PreparedStatement$Pair_T$SQLException() throws Exception {
    when(rs.next()).thenThrow(SQLException.class);
    when(preparedStatement.executeUpdate()).thenThrow(SQLException.class);

    Pair<String, String> p = pair;
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
    boolean actual = target.isDoneHandlerRetrieve();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void doneRetrieve_A$() throws Exception {
    target.doneThreadRetrieve();
  }

}
