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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.client.RawClientTest;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.ClientPersonIndexerJob;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;

public class PeopleSummaryLastChangeHandlerTest extends Goddard<ReplicatedClient, RawClient> {

  PeopleSummaryLastChangeHandler target;
  ClientPersonIndexerJob rocket;
  ReplicatedClientDao dao;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    dao = new ReplicatedClientDao(sessionFactory);
    when(flightPlan.getLastRunLoc()).thenReturn(tempFile.getAbsolutePath());

    flightRecord.setLastChangeSince(new Date());

    rocket =
        new ClientPersonIndexerJob(dao, esDao, lastRunFile, MAPPER, flightPlan, launchDirector);
    rocket.setFlightLog(flightRecord);
    target = new PeopleSummaryLastChangeHandler(rocket);
  }

  @Test
  public void type() throws Exception {
    assertThat(PeopleSummaryLastChangeHandler.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void fetchLastRunNormalizedResults_A$Date$Set() throws Exception {
    final Date lastRunDate = new Date();
    final Set<String> deletionResults = mock(Set.class);
    final List<ReplicatedClient> actual =
        target.fetchLastRunNormalizedResults(lastRunDate, deletionResults);
    final List<ReplicatedClient> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronRuntimeException.class)
  public void fetchLastRunNormalizedResults_bomb() throws Exception {
    final Date lastRunDate = new Date();
    final Set<String> deletionResults = mock(Set.class);
    bombResultSet();
    final List<ReplicatedClient> actual =
        target.fetchLastRunNormalizedResults(lastRunDate, deletionResults);
    final List<ReplicatedClient> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void clearSession_A$Session() throws Exception {
    target.clearSession(session);
  }

  @Test
  public void handleSecondaryJdbc_A$Connection$Pair() throws Exception {
    final Pair<String, String> range = pair;
    target.handleSecondaryJdbc(con, range);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void handleSecondaryJdbc_A$Connection$Pair_T$SQLException() throws Exception {
    when(con.prepareStatement(any(String.class))).thenThrow(SQLException.class);
    when(con.prepareStatement(any(String.class), any(Integer.class), any(Integer.class)))
        .thenThrow(SQLException.class);

    final Pair<String, String> range = pair;
    target.handleSecondaryJdbc(con, range);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void handleSecondaryJdbc_bomb() throws Exception {
    when(con.prepareStatement(any(String.class))).thenThrow(SQLException.class);
    when(con.prepareStatement(any(String.class), any(Integer.class), any(Integer.class)))
        .thenThrow(SQLException.class);
    doThrow(SQLException.class).when(con).rollback();

    final Pair<String, String> range = pair;
    target.handleSecondaryJdbc(con, range);
  }

  @Test
  public void readClientKeys_A$ResultSet() throws Exception {
    RawClientTest.prepResultSetGood(rs);
    when(rs.next()).thenReturn(true).thenReturn(false);
    target.readClientKeys(rs);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void readClientKeys_bomb() throws Exception {
    bombResultSet();
    target.readClientKeys(rs);
  }

  @Test
  public void loadClientRange_A$Connection$PreparedStatement$Pair() throws Exception {
    PreparedStatement stmtInsClient = preparedStatement;
    Pair<String, String> range = pair;
    target.loadClientRange(con, stmtInsClient, range);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void loadClientRange_A$Connection$PreparedStatement$Pair_T$SQLException()
      throws Exception {
    PreparedStatement stmtInsClient = preparedStatement;
    Pair<String, String> range = pair;
    when(stmtInsClient.executeBatch()).thenThrow(SQLException.class);

    target.loadClientRange(con, stmtInsClient, range);
  }

  @Test
  public void insertNextKeyBundle_A$Connection$int$int() throws Exception {
    int start = 0;
    int end = 0;
    int actual = target.insertNextKeyBundle(con, start, end);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isInitialLoad_A$() throws Exception {
    boolean actual = target.isInitialLoad();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void handleFinishRange_A$Pair() throws Exception {
    Pair<String, String> range = pair;
    target.handleFinishRange(range);
  }

  @Test
  public void readNextRunId_A$ResultSet() throws Exception {
    when(rs.next()).thenReturn(true, false);
    when(rs.getInt(1)).thenReturn(1000);
    target.readNextRunId(rs);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void readNextRunId_bomb() throws Exception {
    when(rs.next()).thenReturn(true, false);
    when(rs.getInt(1)).thenReturn(1000);
    bombResultSet();
    target.readNextRunId(rs);
  }

  @Test
  public void calcReplicationDelay_A$() throws Exception {
    target.calcReplicationDelay();
  }

  @Test
  public void getEventType_A$() throws Exception {
    String actual = target.getEventType();
    String expected = "neutron_lc_client";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getRunId_A$() throws Exception {
    int actual = target.getRunId();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRunId_A$int() throws Exception {
    int runId = 0;
    target.setRunId(runId);
  }

}
