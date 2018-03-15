package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.DbResetStatusDao;
import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;

public class SchemaResetRocketTest extends Goddard {

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
  public void launch_A$Date() throws Exception {
    final Date theDate = new Date();
    final Date lastRunDate = theDate;
    final Date actual = target.launch(lastRunDate);
    final Date expected = theDate;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void refreshSchema_A$() throws Exception {
    target.refreshSchema();
  }

  @Test
  public void refreshSchema_A$_T$NeutronCheckedException() throws Exception {
    try {
      when(dao.getSessionFactory()).thenThrow(SQLException.class);
      target.refreshSchema();
      fail("Expected exception was not thrown!");
    } catch (NeutronCheckedException e) {
    }
  }

  @Test
  public void main_A$StringArray_T$Exception() throws Exception {
    String[] args = new String[] {};

    try {
      SchemaResetRocket.main(args);
      fail("Expected exception was not thrown!");
    } catch (Exception e) {
    }
  }

}
