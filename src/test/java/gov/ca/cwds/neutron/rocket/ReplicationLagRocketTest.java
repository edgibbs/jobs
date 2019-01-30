package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.DbResetStatusDao;
import gov.ca.cwds.data.persistence.cms.DatabaseResetEntry;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;

public class ReplicationLagRocketTest extends Goddard<DatabaseResetEntry, DatabaseResetEntry> {

  DbResetStatusDao dao;
  ReplicationLagRocket target;

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();

    dao = new DbResetStatusDao(sessionFactory);
    target = new ReplicationLagRocket(dao, MAPPER, lastRunFile, flightPlan, launchDirector);
  }

  @Test
  public void type() throws Exception {
    assertThat(ReplicationLagRocket.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void launch_A$Date() throws Exception {
    Date lastRunDate = new Date();
    Date actual = target.launch(lastRunDate);
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void measureReplicationLag_A$() throws Exception {
    target.measureReplicationLag();
  }

  @Test
  public void pull_A$ResultSet() throws Exception {
    Float actual = target.pull(rs);
    Float expected = 0.0F;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = SQLException.class)
  public void pull_A$ResultSet_T$SQLException() throws Exception {
    this.bombResultSet();
    target.pull(rs);
  }

  @Test
  public void getLastReplicationSeconds_A$() throws Exception {
    Float actual = ReplicationLagRocket.getLastReplicationSeconds();
    Float expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void main_A$StringArray() throws Exception {
    final String[] args = new String[] {};
    ReplicationLagRocket.main(args);
  }

  @Test(expected = Exception.class)
  public void main_A$StringArray_T$Exception() throws Exception {
    final String[] args = new String[] {};
    ReplicationLagRocket.main(args);
  }

}
