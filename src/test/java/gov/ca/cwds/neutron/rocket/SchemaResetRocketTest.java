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
import org.junit.Ignore;
import org.junit.Test;

import gov.ca.cwds.dao.cms.DbResetStatusDao;
import gov.ca.cwds.data.persistence.cms.DatabaseResetEntry;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;

public class SchemaResetRocketTest extends Goddard<DatabaseResetEntry, DatabaseResetEntry> {

  DbResetStatusDao dao;
  SchemaResetRocket target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    dao = mock(DbResetStatusDao.class);
    when(dao.getSessionFactory()).thenReturn(sessionFactory);
    target = new SchemaResetRocket(dao, mapper, lastRunFile, flightPlan);
  }

  @Test
  public void type() throws Exception {
    assertThat(SchemaResetRocket.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  @Ignore
  public void launch_A$Date() throws Exception {
    final Date theDate = new Date();
    final Date lastRunDate = theDate;
    final Date actual = target.launch(lastRunDate);
    final Date expected = theDate;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  @Ignore
  public void refreshSchema_A$() throws Exception {
    target.refreshSchema();
  }

  @Test(expected = NeutronCheckedException.class)
  public void refreshSchema_A$_T$NeutronCheckedException() throws Exception {
    when(dao.getSessionFactory()).thenThrow(SQLException.class);
    target.refreshSchema();
  }

  @Test(expected = Exception.class)
  public void main_A$StringArray_T$Exception() throws Exception {
    String[] args = new String[] {};
    SchemaResetRocket.main(args);
  }

}
