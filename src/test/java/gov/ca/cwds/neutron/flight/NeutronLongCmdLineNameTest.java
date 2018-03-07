package gov.ca.cwds.neutron.flight;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class NeutronLongCmdLineNameTest {

  @Test
  public void type() throws Exception {
    assertThat(NeutronLongCmdLineName.class, notNullValue());
  }

}
