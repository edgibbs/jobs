package gov.ca.cwds.neutron.vox;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.launch.LaunchCommandSettings;

public class XRaySpexTest extends Goddard<ReplicatedClient, RawClient> {

  XRaySpex target;

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new XRaySpex(launchCommandSettings, launchDirector, injector);
  }

  @Test
  public void type() throws Exception {
    assertThat(XRaySpex.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void initCommandControl_A$() throws Exception {
    target.initCommandControl();
  }

  @Test
  public void exposeREST_A$() throws Exception {
    target.exposeREST();
  }

  @Test(expected = NeutronCheckedException.class)
  public void exposeJMX_A$() throws Exception {
    target.exposeJMX();
  }

  @Test
  public void getSettings_A$() throws Exception {
    LaunchCommandSettings actual = target.getSettings();
    LaunchCommandSettings expected = launchCommandSettings;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJettyServer_A$() throws Exception {
    Thread actual = target.getJettyServer();
    Thread expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

}
