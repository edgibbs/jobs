package gov.ca.cwds.data.es;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gov.ca.cwds.jobs.Goddard;

public class NeutronElasticSearchPersonTest extends Goddard {

  NeutronElasticSearchPerson target;

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new NeutronElasticSearchPerson();
  }

  @Test
  public void type() throws Exception {
    assertThat(NeutronElasticSearchPerson.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    NeutronElasticSearchPerson target = new NeutronElasticSearchPerson();
    assertThat(target, notNullValue());
  }

  @Test
  public void setAddresses_A$List() throws Exception {
    List<ElasticSearchPersonAddress> addresses = new ArrayList<>();
    target.setAddresses(addresses);
  }

}
