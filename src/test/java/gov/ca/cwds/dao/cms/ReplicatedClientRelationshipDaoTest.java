package gov.ca.cwds.dao.cms;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import gov.ca.cwds.jobs.Goddard;

public class ReplicatedClientRelationshipDaoTest extends Goddard {

  ReplicatedClientRelationshipDao target;

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new ReplicatedClientRelationshipDao(sessionFactory);
  }

  @Test
  public void type() throws Exception {
    assertThat(ReplicatedClientRelationshipDao.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

}
