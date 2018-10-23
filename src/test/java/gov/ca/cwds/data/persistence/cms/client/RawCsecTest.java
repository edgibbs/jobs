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

public class RawCsecTest extends Goddard<ReplicatedClient, EsClientPerson> {

  RawCsec target;

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new RawCsec();
  }

  @Test
  public void type() throws Exception {
    assertThat(RawCsec.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void read_A$ResultSet() throws Exception {
    RawCsec actual = target.read(rs);
    // RawCsec expected = null;
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
  public void getCsecId_A$() throws Exception {
    String actual = target.getCsecId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCsecId_A$String() throws Exception {
    String csecId = null;
    target.setCsecId(csecId);
  }

  @Test
  public void getCsecCodeId_A$() throws Exception {
    Short actual = target.getCsecCodeId();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCsecCodeId_A$Short() throws Exception {
    Short csecCodeId = null;
    target.setCsecCodeId(csecCodeId);
  }

  @Test
  public void getCsecStartDate_A$() throws Exception {
    Date actual = target.getCsecStartDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCsecStartDate_A$Date() throws Exception {
    Date csecStartDate = mock(Date.class);
    target.setCsecStartDate(csecStartDate);
  }

  @Test
  public void getCsecEndDate_A$() throws Exception {
    Date actual = target.getCsecEndDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCsecEndDate_A$Date() throws Exception {
    Date csecEndDate = mock(Date.class);
    target.setCsecEndDate(csecEndDate);
  }

  @Test
  public void getCsecLastUpdatedId_A$() throws Exception {
    String actual = target.getCsecLastUpdatedId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCsecLastUpdatedId_A$String() throws Exception {
    String csecLastUpdatedId = null;
    target.setCsecLastUpdatedId(csecLastUpdatedId);
  }

  @Test
  public void getCsecLastUpdatedTimestamp_A$() throws Exception {
    Date actual = target.getCsecLastUpdatedTimestamp();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCsecLastUpdatedTimestamp_A$Date() throws Exception {
    Date csecLastUpdatedTimestamp = mock(Date.class);
    target.setCsecLastUpdatedTimestamp(csecLastUpdatedTimestamp);
  }

  @Test
  public void getCsecLastUpdatedOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getCsecLastUpdatedOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCsecLastUpdatedOperation_A$CmsReplicationOperation() throws Exception {
    CmsReplicationOperation csecLastUpdatedOperation = CmsReplicationOperation.U;
    target.setCsecLastUpdatedOperation(csecLastUpdatedOperation);
  }

  @Test
  public void getCsecReplicationTimestamp_A$() throws Exception {
    Date actual = target.getCsecReplicationTimestamp();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCsecReplicationTimestamp_A$Date() throws Exception {
    Date csecReplicationTimestamp = mock(Date.class);
    target.setCsecReplicationTimestamp(csecReplicationTimestamp);
  }

}
