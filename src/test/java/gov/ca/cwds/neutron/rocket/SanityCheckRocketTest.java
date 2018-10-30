package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.jobs.Goddard;

public class SanityCheckRocketTest
    extends Goddard<ReplicatedOtherAdultInPlacemtHome, ReplicatedOtherAdultInPlacemtHome> {

  ReplicatedOtherAdultInPlacemtHomeDao dao;
  SanityCheckRocket target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    dao = new ReplicatedOtherAdultInPlacemtHomeDao(this.sessionFactory);
    target = new SanityCheckRocket(dao, esDao, MAPPER, flightPlan, launchDirector, null);
  }

  @Test
  public void type() throws Exception {
    assertThat(SanityCheckRocket.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void executeJob_Args__Date() throws Exception {
    final Date lastSuccessfulRunTime = new Date();
    final Date actual = target.launch(lastSuccessfulRunTime);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void main_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/CWS-NS3/client_indexer_time.txt", "-S"};
    SanityCheckRocket.main(args);
  }

}
