package gov.ca.cwds.data.es;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.EsClientPerson;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class SimpleElasticSearchSystemCodeTest extends Goddard<ReplicatedClient, EsClientPerson> {

  SimpleElasticSearchSystemCode target;

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new SimpleElasticSearchSystemCode();
  }

  @Test
  public void type() throws Exception {
    assertThat(SimpleElasticSearchSystemCode.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

}
