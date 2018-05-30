package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.util.Date;

import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;

public class IndexResetPeopleRocketTest
    extends Goddard<ReplicatedOtherAdultInPlacemtHome, ReplicatedOtherAdultInPlacemtHome> {

  ReplicatedOtherAdultInPlacemtHomeDao dao;
  IndexResetRocket target;

  @Override
  public void setup() throws Exception {
    super.setup();

    flightPlan.setDropIndex(true);
    dao = new ReplicatedOtherAdultInPlacemtHomeDao(sessionFactory);
    target = new IndexResetPeopleRocket(dao, esDao, mapper, flightPlan, launchDirector);
  }

  @Test
  public void type() throws Exception {
    assertThat(IndexResetRocket.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void executeJob_Args__Date() throws Exception {
    final Date lastRunDate = new Date();
    final Date actual = target.launch(lastRunDate);
    final Date expected = lastRunDate;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronRuntimeException.class)
  public void executeJob__explode() throws Exception {
    flightPlan.setDropIndex(true);
    doThrow(new IllegalStateException()).when(esDao).deleteIndex(any(String.class));
    doThrow(new IllegalStateException()).when(esDao).getConfig();

    final Date lastRunDate = new Date();
    final Date actual = target.launch(lastRunDate);
    final Date expected = lastRunDate;
    assertThat(actual, is(equalTo(expected)));
  }

}
