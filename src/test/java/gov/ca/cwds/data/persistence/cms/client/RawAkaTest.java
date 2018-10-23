package gov.ca.cwds.data.persistence.cms.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.EsClientPerson;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class RawAkaTest extends Goddard<ReplicatedClient, EsClientPerson> {

  RawAka target;

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new RawAka();
  }

  @Test
  public void type() throws Exception {
    assertThat(RawAka.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void read_A$ResultSet() throws Exception {
    RawAka actual = target.read(rs);
    // RawAka expected = null;
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = SQLException.class)
  public void read_A$ResultSet_T$SQLException() throws Exception {
    when(rs.next()).thenThrow(SQLException.class);
    when(rs.getString(any(String.class))).thenThrow(SQLException.class);
    target.read(rs);
  }

  @Test
  public void getPrimaryKey_A$() throws Exception {
    Serializable actual = target.getPrimaryKey();
    // Serializable expected = null;
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getAkaId_A$() throws Exception {
    String actual = target.getAkaId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaId_A$String() throws Exception {
    String akaId = null;
    target.setAkaId(akaId);
  }

  @Test
  public void getAkaFirstName_A$() throws Exception {
    String actual = target.getAkaFirstName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaFirstName_A$String() throws Exception {
    String akaFirstName = null;
    target.setAkaFirstName(akaFirstName);
  }

  @Test
  public void getAkaLastName_A$() throws Exception {
    String actual = target.getAkaLastName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaLastName_A$String() throws Exception {
    String akaLastName = null;
    target.setAkaLastName(akaLastName);
  }

  @Test
  public void getAkaMiddleName_A$() throws Exception {
    String actual = target.getAkaMiddleName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaMiddleName_A$String() throws Exception {
    String akaMiddleName = null;
    target.setAkaMiddleName(akaMiddleName);
  }

  @Test
  public void getAkaNamePrefixDescription_A$() throws Exception {
    String actual = target.getAkaNamePrefixDescription();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaNamePrefixDescription_A$String() throws Exception {
    String akaNamePrefixDescription = null;
    target.setAkaNamePrefixDescription(akaNamePrefixDescription);
  }

  @Test
  public void getAkaNameType_A$() throws Exception {
    Short actual = target.getAkaNameType();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaNameType_A$Short() throws Exception {
    Short akaNameType = null;
    target.setAkaNameType(akaNameType);
  }

  @Test
  public void getAkaSuffixTitleDescription_A$() throws Exception {
    String actual = target.getAkaSuffixTitleDescription();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaSuffixTitleDescription_A$String() throws Exception {
    String akaSuffixTitleDescription = null;
    target.setAkaSuffixTitleDescription(akaSuffixTitleDescription);
  }

  @Test
  public void getAkaLastUpdatedId_A$() throws Exception {
    String actual = target.getAkaLastUpdatedId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaLastUpdatedId_A$String() throws Exception {
    String akaLastUpdatedId = null;
    target.setAkaLastUpdatedId(akaLastUpdatedId);
  }

  @Test
  public void getAkaLastUpdatedTimestamp_A$() throws Exception {
    Date actual = target.getAkaLastUpdatedTimestamp();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaLastUpdatedTimestamp_A$Date() throws Exception {
    Date akaLastUpdatedTimestamp = mock(Date.class);
    target.setAkaLastUpdatedTimestamp(akaLastUpdatedTimestamp);
  }

  @Test
  public void getAkaLastUpdatedOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getAkaLastUpdatedOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaLastUpdatedOperation_A$CmsReplicationOperation() throws Exception {
    CmsReplicationOperation akaLastUpdatedOperation = CmsReplicationOperation.U;
    target.setAkaLastUpdatedOperation(akaLastUpdatedOperation);
  }

  @Test
  public void getAkaReplicationTimestamp_A$() throws Exception {
    Date actual = target.getAkaReplicationTimestamp();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAkaReplicationTimestamp_A$Date() throws Exception {
    Date akaReplicationTimestamp = new Date();
    target.setAkaReplicationTimestamp(akaReplicationTimestamp);
  }

}
