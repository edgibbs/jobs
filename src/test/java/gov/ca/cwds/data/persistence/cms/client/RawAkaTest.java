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

import gov.ca.cwds.data.persistence.cms.client.RawAka.ColumnPosition;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class RawAkaTest extends Goddard<ReplicatedClient, RawClient> {

  static Timestamp ts;
  RawAka target;

  public static void prepResultSetGood(ResultSet rs) throws SQLException {
    when(rs.getString(ColumnPosition.CLT_IDENTIFIER.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.ONM_THIRD_ID.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.ONM_FIRST_NM.ordinal())).thenReturn("Homer");
    when(rs.getString(ColumnPosition.ONM_LAST_NM.ordinal())).thenReturn("Simpson");
    when(rs.getString(ColumnPosition.ONM_MIDDLE_NM.ordinal())).thenReturn("J");
    when(rs.getString(ColumnPosition.ONM_NMPRFX_DSC.ordinal())).thenReturn("mr");

    Short cd = 1311;
    when(rs.getShort(ColumnPosition.ONM_NAME_TPC.ordinal())).thenReturn(cd);
    when(rs.getString(ColumnPosition.ONM_SUFX_TLDSC.ordinal())).thenReturn("jr");
    when(rs.getString(ColumnPosition.ONM_LST_UPD_ID.ordinal())).thenReturn("0x5");

    final Date date = new Date();
    ts = new Timestamp(date.getTime());
    when(rs.getTimestamp(ColumnPosition.ONM_LST_UPD_TS.ordinal())).thenReturn(ts);
    when(rs.getString(ColumnPosition.ONM_IBMSNAP_OPERATION.ordinal())).thenReturn("U");
    when(rs.getTimestamp(ColumnPosition.ONM_IBMSNAP_LOGMARKER.ordinal()))
        .thenReturn(new Timestamp(new Date().getTime()));
  }

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
