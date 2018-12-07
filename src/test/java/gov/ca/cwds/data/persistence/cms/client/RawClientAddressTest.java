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

import gov.ca.cwds.data.persistence.cms.VarargPrimaryKey;
import gov.ca.cwds.data.persistence.cms.client.ClientAddressReference.ColumnPosition;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class RawClientAddressTest extends Goddard<ReplicatedClient, RawClient> {

  static java.sql.Date sqlDate;
  static Timestamp ts;
  RawClientAddress target;

  @Override
  public void setup() throws Exception {
    super.setup();
    prepResultSetGood(rs);
    target = new RawClientAddress();
    target.read(rs);
  }

  public static void prepResultSetGood(ResultSet rs) throws SQLException {
    when(rs.getString(ColumnPosition.CLT_IDENTIFIER.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.CLA_LST_UPD_ID.ordinal())).thenReturn("0x5");
    when(rs.getString(ColumnPosition.CLA_IDENTIFIER.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.CLA_FKADDRS_T.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.CLA_FKCLIENT_T.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.CLA_FKREFERL_T.ordinal())).thenReturn("");
    when(rs.getString(ColumnPosition.CLA_HOMLES_IND.ordinal())).thenReturn("N");
    when(rs.getString(ColumnPosition.CLA_BK_INMT_ID.ordinal())).thenReturn("N");

    when(rs.getShort(ColumnPosition.CLA_ADDR_TPC.ordinal())).thenReturn((short) 32);

    Date date = new Date();
    ts = new Timestamp(date.getTime());
    when(rs.getTimestamp(ColumnPosition.CLA_LST_UPD_TS.ordinal())).thenReturn(ts);

    sqlDate = new java.sql.Date(date.getTime());
    when(rs.getDate(ColumnPosition.CLA_EFF_END_DT.ordinal())).thenReturn(sqlDate);
    when(rs.getDate(ColumnPosition.CLA_EFF_STRTDT.ordinal())).thenReturn(sqlDate);
  }

  @Test
  public void type() throws Exception {
    assertThat(RawClientAddress.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void read_A$ResultSet() throws Exception {
    RawClientAddress actual = target.read(rs);
    // RawClientAddress expected = null;
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
    target.setCltId(DEFAULT_CLIENT_ID);
    target.setClaId(DEFAULT_CLIENT_ID);
    Serializable actual = target.getPrimaryKey();
    Serializable expected = new VarargPrimaryKey("_" + DEFAULT_CLIENT_ID);
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getClaReplicationOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getClaReplicationOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClaReplicationOperation_A$CmsReplicationOperation() throws Exception {
    CmsReplicationOperation claReplicationOperation = CmsReplicationOperation.U;
    target.setClaReplicationOperation(claReplicationOperation);
  }

  @Test
  public void getClaReplicationDate_A$() throws Exception {
    Date actual = target.getClaReplicationDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClaReplicationDate_A$Date() throws Exception {
    Date claReplicationDate = mock(Date.class);
    target.setClaReplicationDate(claReplicationDate);
  }

  @Test
  public void getClaLastUpdatedId_A$() throws Exception {
    String actual = target.getClaLastUpdatedId();
    String expected = "0x5";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClaLastUpdatedId_A$String() throws Exception {
    String claLastUpdatedId = null;
    target.setClaLastUpdatedId(claLastUpdatedId);
  }

  @Test
  public void getClaLastUpdatedTime_A$() throws Exception {
    Date actual = target.getClaLastUpdatedTime();
    Date expected = ts;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClaLastUpdatedTime_A$Date() throws Exception {
    Date claLastUpdatedTime = mock(Date.class);
    target.setClaLastUpdatedTime(claLastUpdatedTime);
  }

  @Test
  public void getClaFkAddress_A$() throws Exception {
    String actual = target.getClaFkAddress();
    String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClaFkAddress_A$String() throws Exception {
    String claFkAddress = null;
    target.setClaFkAddress(claFkAddress);
  }

  @Test
  public void getClaFkClient_A$() throws Exception {
    String actual = target.getClaFkClient();
    String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClaFkClient_A$String() throws Exception {
    String claFkClient = null;
    target.setClaFkClient(claFkClient);
  }

  @Test
  public void getClaFkReferral_A$() throws Exception {
    String actual = target.getClaFkReferral();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClaFkReferral_A$String() throws Exception {
    String claFkReferral = null;
    target.setClaFkReferral(claFkReferral);
  }

  @Test
  public void getClaAddressType_A$() throws Exception {
    Short actual = target.getClaAddressType();
    Short expected = 32;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClaAddressType_A$Short() throws Exception {
    Short claAddressType = null;
    target.setClaAddressType(claAddressType);
  }

  @Test
  public void getClaHomelessInd_A$() throws Exception {
    String actual = target.getClaHomelessInd();
    String expected = "N";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClaHomelessInd_A$String() throws Exception {
    String claHomelessInd = null;
    target.setClaHomelessInd(claHomelessInd);
  }

  @Test
  public void getClaBkInmtId_A$() throws Exception {
    String actual = target.getClaBkInmtId();
    String expected = "N";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClaBkInmtId_A$String() throws Exception {
    String claBkInmtId = null;
    target.setClaBkInmtId(claBkInmtId);
  }

  @Test
  public void getClaEffectiveEndDate_A$() throws Exception {
    Date actual = target.getClaEffectiveEndDate();
    Date expected = sqlDate;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClaEffectiveEndDate_A$Date() throws Exception {
    Date date = mock(Date.class);
    target.setClaEffectiveEndDate(date);
  }

  @Test
  public void getClaEffectiveStartDate_A$() throws Exception {
    Date actual = target.getClaEffectiveStartDate();
    Date expected = sqlDate;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClaEffectiveStartDate_A$Date() throws Exception {
    Date date = mock(Date.class);
    target.setClaEffectiveStartDate(date);
  }

  @Test
  public void getAddress_A$() throws Exception {
    RawAddress actual = target.getAddress();
    RawAddress expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAddress_A$RawAddress() throws Exception {
    RawAddress address = mock(RawAddress.class);
    target.setAddress(address);
  }

}
