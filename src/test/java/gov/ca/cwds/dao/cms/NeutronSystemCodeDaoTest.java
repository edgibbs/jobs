package gov.ca.cwds.dao.cms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.SystemCode;
import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class NeutronSystemCodeDaoTest extends Goddard<ReplicatedClient, RawClient> {

  NeutronSystemCodeDao target;

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new NeutronSystemCodeDao(sessionFactory);
  }

  @Test
  public void type() throws Exception {
    assertThat(NeutronSystemCodeDao.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void findByForeignKeyMetaTable_A$String() throws Exception {
    String foreignKeyMetaTable = "GVR_ENTC";
    SystemCode[] actual = target.findByForeignKeyMetaTable(foreignKeyMetaTable);
    SystemCode[] expected = new SystemCode[0];
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void findBySystemCodeId_A$Number() throws Exception {
    Number systemCodeId = 1106;
    SystemCode actual = target.findBySystemCodeId(systemCodeId);
    SystemCode expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

}
