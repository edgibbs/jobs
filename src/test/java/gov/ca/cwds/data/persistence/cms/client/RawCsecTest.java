package gov.ca.cwds.data.persistence.cms.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.client.RawCsec.ColumnPosition;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class RawCsecTest extends Goddard<ReplicatedClient, RawClient> {

  RawCsec target;

  public static void prepResultSetGood(ResultSet rs) throws SQLException {
    when(rs.getString(ColumnPosition.CLT_IDENTIFIER.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.CSH_THIRD_ID.ordinal())).thenReturn("1234567xyz");
    when(rs.getString(ColumnPosition.CSH_START_DT.ordinal())).thenReturn("C");

    Short cd = 1311;
    when(rs.getShort(ColumnPosition.CSH_CSEC_TPC.ordinal())).thenReturn(cd);
    when(rs.getString(ColumnPosition.CSH_END_DT.ordinal())).thenReturn("jr");
    when(rs.getString(ColumnPosition.CSH_LST_UPD_ID.ordinal())).thenReturn("0x5");

    final Date date = new Date();
    final Timestamp ts = new Timestamp(date.getTime());
    when(rs.getTimestamp(ColumnPosition.CSH_LST_UPD_TS.ordinal())).thenReturn(ts);
    when(rs.getString(ColumnPosition.CSH_IBMSNAP_OPERATION.ordinal())).thenReturn("U");
    when(rs.getTimestamp(ColumnPosition.CSH_IBMSNAP_LOGMARKER.ordinal()))
        .thenReturn(new Timestamp(new Date().getTime()));
  }

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
    bombResultSet();
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
