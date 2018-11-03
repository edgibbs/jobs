package gov.ca.cwds.data.persistence.cms.client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.rep.ReplicatedAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClientAddress;
import gov.ca.cwds.jobs.Goddard;

public class RawToEsConverterTest extends Goddard<ReplicatedClient, RawClient> {

  RawToEsConverter target;

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new RawToEsConverter();
  }

  @Test
  public void type() throws Exception {
    assertThat(RawToEsConverter.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void convert_A$RawClient() throws Exception {
    RawClient rawCli = new RawClient();
    ReplicatedClient actual = target.convert(rawCli);
    // ReplicatedClient expected = null;
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void convertCsec_A$ReplicatedClient$RawClient$RawCsec() throws Exception {
    ReplicatedClient rc = new ReplicatedClient();
    RawCsec rawCsec = new RawCsec();
    target.convertCsec(rc, rawCsec);
  }

  @Test
  public void convertCase_A$ReplicatedClient$RawClient$RawCase() throws Exception {
    ReplicatedClient rc = new ReplicatedClient();
    RawCase rawCase = mock(RawCase.class);
    target.convertCase(rc, rawCase);
  }

  @Test
  public void convertSafetyAlert_A$ReplicatedClient$RawClient$RawSafetyAlert() throws Exception {
    ReplicatedClient rc = new ReplicatedClient();
    RawSafetyAlert rawSafetyAlert = mock(RawSafetyAlert.class);
    target.convertSafetyAlert(rc, rawSafetyAlert);
  }

  @Test
  public void convertEthnicity_A$ReplicatedClient$RawClient$RawEthnicity() throws Exception {
    ReplicatedClient rc = new ReplicatedClient();
    RawClient rawCli = new RawClient();
    RawEthnicity rawEthnicity = mock(RawEthnicity.class);
    target.convertEthnicity(rc, rawEthnicity);
  }

  @Test
  public void convertAka_A$ReplicatedClient$RawClient$RawAka() throws Exception {
    ReplicatedClient rc = new ReplicatedClient();
    RawAka rawAka = mock(RawAka.class);
    target.convertAka(rc, rawAka);
  }

  @Test
  public void convertClientCounty_A$ReplicatedClient$RawClient$RawClientCounty() throws Exception {
    ReplicatedClient rc = new ReplicatedClient();
    RawClientCounty rawCounty = mock(RawClientCounty.class);
    target.convertClientCounty(rc, rawCounty);
  }

  @Test
  public void convertClientAddress_A$ReplicatedClient$RawClient$RawClientAddress()
      throws Exception {
    ReplicatedClient rc = new ReplicatedClient();
    RawClientAddress rawCliAdr = mock(RawClientAddress.class);
    target.convertClientAddress(rc, rawCliAdr);
  }

  @Test
  public void convertAddress_A$ReplicatedClient$ReplicatedClientAddress$RawClient$RawClientAddress$RawAddress()
      throws Exception {
    ReplicatedClient rc = new ReplicatedClient();
    ReplicatedClientAddress repCa = mock(ReplicatedClientAddress.class);
    RawClientAddress rawCa = mock(RawClientAddress.class);
    RawAddress rawAdr = mock(RawAddress.class);
    ReplicatedAddress actual = target.convertAddress(repCa, rawCa, rawAdr);
    // ReplicatedAddress expected = null;
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

}
