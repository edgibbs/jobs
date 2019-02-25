package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.DbResetStatusDao;
import gov.ca.cwds.data.persistence.cms.DatabaseResetEntry;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;

public class SchemaResetRocketTest extends Goddard<DatabaseResetEntry, DatabaseResetEntry> {

  DbResetStatusDao dao;
  SchemaResetRocket target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    dao = new DbResetStatusDao(sessionFactory);

    target = new SchemaResetRocket(dao, mapper, lastRunFile, flightPlan, launchDirector);
    target.setPollPeriodInSeconds(2);
    target.setTimeoutSeconds(5);

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
    runKillThread(target, 3500L);
    final Date theDate = new Date();
    final Date lastRunDate = theDate;
    final Date actual = target.launch(lastRunDate);
    final Date expected = theDate;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronRuntimeException.class)
  public void refreshSchema_A$() throws Exception {
    runKillThread(target, 8500L);
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
    String actual = target.getDbSchema();
    String expected = "CWSNS1";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getSchemaRefreshTimeoutSeconds_A$() throws Exception {
    int actual = target.getSchemaRefreshTimeoutSeconds();
    int expected = 5;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSchemaRefreshTimeoutSeconds_A$int() throws Exception {
    int schemaRefreshTimeoutSeconds = 0;
    target.setSchemaRefreshTimeoutSeconds(schemaRefreshTimeoutSeconds);
  }

  @Test
  public void getPollPeriodInSeconds_A$() throws Exception {
    int actual = target.getPollPeriodInSeconds();
    int expected = 2;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setPollPeriodInSeconds_A$int() throws Exception {
    int pollPeriodInSeconds = 0;
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
    int actual = target.getTimeoutSeconds();
    int expected = 5;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setTimeoutSeconds_A$int() throws Exception {
    int timeoutSeconds = 0;
    target.setTimeoutSeconds(timeoutSeconds);
  }

}
