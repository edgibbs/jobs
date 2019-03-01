package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.flight.FlightSummary;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;

public class ExitInitialLoadRocketTest
    extends Goddard<ReplicatedOtherAdultInPlacemtHome, ReplicatedOtherAdultInPlacemtHome> {

  ReplicatedOtherAdultInPlacemtHomeDao dao;
  ExitInitialLoadRocket target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    when(esDao.createOrSwapAlias(any(String.class), any(String.class))).thenReturn(true);
    System.out.println("esDao.createOrSwapAlias: " + esDao.createOrSwapAlias("alias", "index"));

    when(LaunchCommand.getInstance().getCommonFlightPlan().getIndexName())
        .thenReturn("people-summary_2019.02.25.13.26.35");
    dao = new ReplicatedOtherAdultInPlacemtHomeDao(sessionFactory);
    target =
        new ExitInitialLoadRocket(dao, esDao, mapper, launchDirector, flightPlan, launchDirector);
  }

  @Test
  public void type() throws Exception {
    assertThat(ExitInitialLoadRocket.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void executeJob_Args__Date() throws Exception {
    Date lastRunDate = new Date();
    Date actual = target.launch(lastRunDate);
    Date expected = lastRunDate;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void logError_Args__StandardFlightSchedule__FlightSummary() throws Exception {
    StandardFlightSchedule sched = StandardFlightSchedule.CASES;
    FlightSummary summary = mock(FlightSummary.class);
    target.logError(sched, summary);
  }

}
