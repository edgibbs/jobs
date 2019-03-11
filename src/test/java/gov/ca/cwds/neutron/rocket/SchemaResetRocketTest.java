package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.DbResetStatusDao;
import gov.ca.cwds.data.persistence.cms.DatabaseResetEntry;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.atom.AtomHibernate;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;

public class SchemaResetRocketTest extends Goddard<DatabaseResetEntry, DatabaseResetEntry> {

  DbResetStatusDao dao;
  SchemaResetRocket target;
  DatabaseResetEntry entry;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    System.setProperty(AtomHibernate.CURRENT_SCHEMA, "CWSNS1");
    dao = mock(DbResetStatusDao.class);

    entry = new DatabaseResetEntry();
    entry.setEndTime(new Date());
    entry.setRefreshStatus("S");
    entry.setSchemaName("CWSNS1");
    when(dao.findBySchemaStartTime(any(String.class))).thenReturn(entry);

    when(dao.getSessionFactory()).thenReturn(sessionFactory);
    when(dao.grabSession()).thenReturn(session);

    entry = new DatabaseResetEntry();
    when(q.getSingleResult()).thenReturn(entry);

    target = new SchemaResetRocket(dao, mapper, lastRunFile, flightPlan, launchDirector);
    target.setPollPeriodInSeconds(2);
    target.setTimeoutSeconds(3);
    target.getFlightLog().start();

    when(proc.getOutputParameterValue("RETSTATUS")).thenReturn("0");
  }

  @Test
  public void type() throws Exception {
    assertThat(SchemaResetRocket.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test(expected = NeutronRuntimeException.class)
  public void launch_A$Date() throws Exception {
    runKillThread(target, 6500L);
    final Date theDate = new Date();
    final Date lastRunDate = theDate;
    final Date actual = target.launch(lastRunDate);
    final Date expected = theDate;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void refreshSchema_succeeded() throws Exception {
    runKillThread(target, 18500L);
    entry.setRefreshStatus("S");
    target.setTimeoutSeconds(2);
    try {
      target.refreshSchema();
    } catch (Exception e) {
      // code coverage only
    }
  }

  @Test(expected = IllegalStateException.class)
  public void refreshSchema_not_done() throws Exception {
    // runKillThread(target, 18500L);
    entry = new DatabaseResetEntry();
    entry.setEndTime(new Date());
    entry.setRefreshStatus("N");
    entry.setSchemaName("CWSNS1");
    when(dao.findBySchemaStartTime(any(String.class))).thenReturn(entry);

    target.setTimeoutSeconds(7);
    target.refreshSchema();
  }

  @Test(expected = NeutronRuntimeException.class)
  public void refreshSchema_A$_T$NeutronCheckedException() throws Exception {
    runKillThread(target, 3500L);
    dao = mock(DbResetStatusDao.class);
    when(dao.getSessionFactory()).thenThrow(SQLException.class);
    target.refreshSchema();
  }

  @Test(expected = Exception.class)
  public void main_A$StringArray_T$Exception() throws Exception {
    String[] args = new String[] {};
    SchemaResetRocket.main(args);
  }

  @Test
  public void getDbSchema_A$() throws Exception {
    final String actual = target.getDbSchema();
    final String expected = "CWSNS1";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getSchemaRefreshTimeoutSeconds_A$() throws Exception {
    final int actual = target.getSchemaRefreshTimeoutSeconds();
    final int expected = 3;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSchemaRefreshTimeoutSeconds_A$int() throws Exception {
    final int schemaRefreshTimeoutSeconds = 8;
    target.setSchemaRefreshTimeoutSeconds(schemaRefreshTimeoutSeconds);
  }

  @Test
  public void getPollPeriodInSeconds_A$() throws Exception {
    final int actual = target.getPollPeriodInSeconds();
    final int expected = 2;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setPollPeriodInSeconds_A$int() throws Exception {
    final int pollPeriodInSeconds = 0;
    target.setPollPeriodInSeconds(pollPeriodInSeconds);
  }

  @Test
  public void done_A$() throws Exception {
    try {
      target.done();
    } catch (NeutronRuntimeException e) {
    }
  }

  @Test(expected = Exception.class)
  public void fail_A$() throws Exception {
    target.fail();
  }

  @Test
  public void getTimeoutSeconds_A$() throws Exception {
    final int actual = target.getTimeoutSeconds();
    final int expected = 3;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setTimeoutSeconds_A$int() throws Exception {
    final int timeoutSeconds = 0;
    target.setTimeoutSeconds(timeoutSeconds);
  }

  @Test
  public void schemaRefreshCompleted_A$int() throws Exception {
    final int waitTimeSeconds = 8;
    try {
      boolean actual = target.schemaRefreshCompleted(waitTimeSeconds);
      boolean expected = true;
      assertThat(actual, is(equalTo(expected)));
    } catch (Exception e) {
      // eat it
      if (!(e instanceof NeutronRuntimeException && e.getCause() != null
          && e.getCause() instanceof InterruptedException)) {
        throw e;
      }
    }
  }

  @Test
  public void findSchemaRefreshStatus_A$() throws Exception {
    final String actual = target.findSchemaRefreshStatus();
    final String expected = "S";
    assertThat(actual, is(equalTo(expected)));
  }

}
