package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    rocket =
        new ClientPersonIndexerJob(dao, esDao, lastRunFile, MAPPER, flightPlan, launchDirector);
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

    final Pair<String, String> range = pair;
    target.handleSecondaryJdbc(con, range);
  }

}

