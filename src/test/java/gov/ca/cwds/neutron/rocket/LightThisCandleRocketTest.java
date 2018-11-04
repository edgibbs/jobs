package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.launch.LaunchDirector;

public class LightThisCandleRocketTest
    extends Goddard<ReplicatedOtherAdultInPlacemtHome, ReplicatedOtherAdultInPlacemtHome> {

  ReplicatedOtherAdultInPlacemtHomeDao dao;
  LightThisCandleRocket target;

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();
    dao = new ReplicatedOtherAdultInPlacemtHomeDao(sessionFactory);
    target =
        new LightThisCandleRocket(dao, esDao, mapper, launchDirector, flightPlan, launchDirector);
  }

  @Test
  public void type() throws Exception {
    assertThat(LightThisCandleRocket.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void launch_A$Date() throws Exception {
    Date lastRunDate = new Date();
    Date actual = target.launch(lastRunDate);
    Date expected = new Date();
    assertThat(actual, is(lessThanOrEqualTo(expected)));
  }

  @Test
  public void getLaunchDirector1_A$() throws Exception {
    LaunchDirector actual = target.getLaunchDirector1();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setLaunchDirector1_A$LaunchDirector() throws Exception {
    target.setLaunchDirector1(launchDirector);
  }

}
