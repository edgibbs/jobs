package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.jobs.Goddard;

public class VoxListenerRocketTest
    extends Goddard<ReplicatedOtherAdultInPlacemtHome, ReplicatedOtherAdultInPlacemtHome> {

  ReplicatedOtherAdultInPlacemtHomeDao dao;
  VoxListenerRocket target;

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();

    dao = new ReplicatedOtherAdultInPlacemtHomeDao(sessionFactory);
    target =
        new VoxListenerRocket(dao, esDao, mapper, launchDirector, flightPlan, launchDirector, 10);
  }

  @Test
  public void type() throws Exception {
    assertThat(VoxListenerRocket.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void launch_A$Date() throws Exception {
    final Date lastRunDate = new Date();
    final Date expected = new Date();
    final Date actual = target.launch(lastRunDate);
    assertThat(actual, is(greaterThanOrEqualTo(expected)));
  }

}
