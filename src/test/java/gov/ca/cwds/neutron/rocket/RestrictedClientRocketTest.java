package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.persistence.cms.EsClientAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class RestrictedClientRocketTest extends Goddard<ReplicatedClient, EsClientAddress> {

  ReplicatedClientDao dao;
  RestrictedClientRocket target;

  @Override
  public void setup() throws Exception {
    super.setup();

    dao = new ReplicatedClientDao(sessionFactory);
    target = new RestrictedClientRocket(dao, esDao, lastRunFile, MAPPER, flightPlan, launchDirector);
  }

  @Test
  public void type() throws Exception {
    assertThat(RestrictedClientRocket.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getInitialLoadQuery_Args__String() throws Exception {
    final String dbSchemaName = "CWSRS1";
    final String actual = target.getInitialLoadQuery(dbSchemaName);
    final String expected =
        "SELECT x.* FROM CWSRS1.MQT_CLIENT_ADDRESS x WHERE x.clt_identifier BETWEEN ':fromId' AND ':toId'  AND x.CLT_SENSTV_IND in ('S','R')  ORDER BY X.CLT_IDENTIFIER  FOR READ ONLY WITH UR ";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void mustDeleteLimitedAccessRecords_Args__() throws Exception {
    final boolean actual = target.mustDeleteLimitedAccessRecords();
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void main_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/dsmith/client_indexer_time.txt", "-S"};
    RestrictedClientRocket.main(args);
  }

}
