package gov.ca.cwds.dao.cms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class ReplicatedClientDaoTest extends Goddard {

  ReplicatedClientDao target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    target = new ReplicatedClientDao(sessionFactory);
  }

  @Test
  public void type() throws Exception {
    assertThat(ReplicatedClientDao.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void findByTemp_A$() throws Exception {
    List<ReplicatedClient> actual = target.findByTemp();
    List<ReplicatedClient> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void makeNamedQueryName_A$String() throws Exception {
    String suffix = "findByTemp";
    String actual = target.makeNamedQueryName(suffix);
    String expected = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient.findByTemp";
    assertThat(actual, is(equalTo(expected)));
  }

}
