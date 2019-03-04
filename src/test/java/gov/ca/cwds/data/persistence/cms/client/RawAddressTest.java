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
import gov.ca.cwds.data.persistence.cms.client.RawAddress.ColumnPosition;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class RawAddressTest extends Goddard<ReplicatedClient, RawClient> {

  RawAddress target;

  public static void prepResultSetGood(ResultSet rs) throws SQLException {
    when(rs.getString(ColumnPosition.CLT_IDENTIFIER.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.CLA_IDENTIFIER.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.ADR_IDENTIFIER.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.ADR_STREET_NO.ordinal())).thenReturn("2111");
    when(rs.getString(ColumnPosition.ADR_STREET_NM.ordinal())).thenReturn("Via Roma");
    when(rs.getString(ColumnPosition.ADR_UNIT_NO.ordinal())).thenReturn("");
    when(rs.getString(ColumnPosition.ADR_CITY_NM.ordinal())).thenReturn("Campbell");
    when(rs.getString(ColumnPosition.ADR_ZIP_NO.ordinal())).thenReturn("95008");

    when(rs.getShort(ColumnPosition.ADR_GVR_ENTC.ordinal())).thenReturn((short) 1080);
    when(rs.getShort(ColumnPosition.ADR_ST_SFX_C.ordinal())).thenReturn((short) 0);
    when(rs.getShort(ColumnPosition.ADR_STATE_C.ordinal())).thenReturn((short) 1828);
    when(rs.getShort(ColumnPosition.ADR_UNT_DSGC.ordinal())).thenReturn((short) 2067);
    when(rs.getShort(ColumnPosition.ADR_ZIP_SFX_NO.ordinal())).thenReturn((short) 0);

    final Timestamp ts = new Timestamp(new Date().getTime());
    when(rs.getTimestamp(ColumnPosition.ADR_LST_UPD_TS.ordinal())).thenReturn(ts);

    when(rs.getInt(ColumnPosition.ADR_EMRG_EXTNO.ordinal())).thenReturn(1234);
    when(rs.getInt(ColumnPosition.ADR_MSG_EXT_NO.ordinal())).thenReturn(4567);
    when(rs.getInt(ColumnPosition.ADR_PRM_EXT_NO.ordinal())).thenReturn(789);

    when(rs.getLong(ColumnPosition.ADR_EMRG_TELNO.ordinal())).thenReturn(0L);
    when(rs.getLong(ColumnPosition.ADR_MSG_TEL_NO.ordinal())).thenReturn(0L);
    when(rs.getLong(ColumnPosition.ADR_PRM_TEL_NO.ordinal())).thenReturn(4083742790L);
  }

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new RawAddress();
  }

  @Test
  public void type() throws Exception {
    assertThat(RawAddress.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void read_A$ResultSet() throws Exception {
    RawAddressTest.prepResultSetGood(rs);
    RawAddress actual = target.read(rs);
    // RawAddress expected = null;
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
    target.setAdrId(DEFAULT_CLIENT_ID);
    Serializable actual = target.getPrimaryKey();
    Serializable expected = new VarargPrimaryKey(DEFAULT_CLIENT_ID, DEFAULT_CLIENT_ID);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAdrId_A$() throws Exception {
    String actual = target.getAdrId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrId_A$String() throws Exception {
    String adrId = null;
    target.setAdrId(adrId);
  }

  @Test
  public void getAdrCity_A$() throws Exception {
    String actual = target.getAdrCity();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrCity_A$String() throws Exception {
    String adrCity = null;
    target.setAdrCity(adrCity);
  }

  @Test
  public void getAdrEmergencyNumber_A$() throws Exception {
    Long actual = target.getAdrEmergencyNumber();
    Long expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrEmergencyNumber_A$Long() throws Exception {
    Long adrEmergencyNumber = null;
    target.setAdrEmergencyNumber(adrEmergencyNumber);
  }

  @Test
  public void getAdrEmergencyExtension_A$() throws Exception {
    Integer actual = target.getAdrEmergencyExtension();
    Integer expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrEmergencyExtension_A$Integer() throws Exception {
    Integer adrEmergencyExtension = null;
    target.setAdrEmergencyExtension(adrEmergencyExtension);
  }

  @Test
  public void getAdrFrgAdrtB_A$() throws Exception {
    String actual = target.getAdrFrgAdrtB();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrFrgAdrtB_A$String() throws Exception {
    String adrFrgAdrtB = null;
    target.setAdrFrgAdrtB(adrFrgAdrtB);
  }

  @Test
  public void getAdrGovernmentEntityCd_A$() throws Exception {
    Short actual = target.getAdrGovernmentEntityCd();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrGovernmentEntityCd_A$Short() throws Exception {
    Short adrGovernmentEntityCd = null;
    target.setAdrGovernmentEntityCd(adrGovernmentEntityCd);
  }

  @Test
  public void getAdrLastUpdatedTime_A$() throws Exception {
    Date actual = target.getAdrLastUpdatedTime();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrLastUpdatedTime_A$Date() throws Exception {
    Date adrLastUpdatedTime = mock(Date.class);
    target.setAdrLastUpdatedTime(adrLastUpdatedTime);
  }

  @Test
  public void getAdrMessageNumber_A$() throws Exception {
    Long actual = target.getAdrMessageNumber();
    Long expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrMessageNumber_A$Long() throws Exception {
    Long adrMessageNumber = null;
    target.setAdrMessageNumber(adrMessageNumber);
  }

  @Test
  public void getAdrMessageExtension_A$() throws Exception {
    Integer actual = target.getAdrMessageExtension();
    Integer expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrMessageExtension_A$Integer() throws Exception {
    Integer adrMessageExtension = null;
    target.setAdrMessageExtension(adrMessageExtension);
  }

  @Test
  public void getAdrHeaderAddress_A$() throws Exception {
    String actual = target.getAdrHeaderAddress();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrHeaderAddress_A$String() throws Exception {
    String adrHeaderAddress = null;
    target.setAdrHeaderAddress(adrHeaderAddress);
  }

  @Test
  public void getAdrPrimaryNumber_A$() throws Exception {
    Long actual = target.getAdrPrimaryNumber();
    Long expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrPrimaryNumber_A$Long() throws Exception {
    Long adrPrimaryNumber = null;
    target.setAdrPrimaryNumber(adrPrimaryNumber);
  }

  @Test
  public void getAdrPrimaryExtension_A$() throws Exception {
    Integer actual = target.getAdrPrimaryExtension();
    Integer expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAdrState_A$() throws Exception {
    Short actual = target.getAdrState();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAdrStreetName_A$() throws Exception {
    String actual = target.getAdrStreetName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAdrStreetNumber_A$() throws Exception {
    String actual = target.getAdrStreetNumber();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAdrZip_A$() throws Exception {
    String actual = target.getAdrZip();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAdrAddressDescription_A$() throws Exception {
    String actual = target.getAdrAddressDescription();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrAddressDescription_A$String() throws Exception {
    String adrAddressDescription = null;
    target.setAdrAddressDescription(adrAddressDescription);
  }

  @Test
  public void getAdrZip4_A$() throws Exception {
    Short actual = target.getAdrZip4();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAdrPostDirCd_A$() throws Exception {
    String actual = target.getAdrPostDirCd();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAdrPreDirCd_A$() throws Exception {
    String actual = target.getAdrPreDirCd();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAdrStreetSuffixCd_A$() throws Exception {
    Short actual = target.getAdrStreetSuffixCd();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAdrUnitDesignationCd_A$() throws Exception {
    Short actual = target.getAdrUnitDesignationCd();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAdrUnitNumber_A$() throws Exception {
    String actual = target.getAdrUnitNumber();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAdrReplicationOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getAdrReplicationOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrReplicationOperation_A$CmsReplicationOperation() throws Exception {
    CmsReplicationOperation adrReplicationOperation = CmsReplicationOperation.U;
    target.setAdrReplicationOperation(adrReplicationOperation);
  }

  @Test
  public void getAdrReplicationDate_A$() throws Exception {
    Date actual = target.getAdrReplicationDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAdrReplicationDate_A$Date() throws Exception {
    Date adrReplicationDate = mock(Date.class);
    target.setAdrReplicationDate(adrReplicationDate);
  }

  @Test
  public void setAdrPrimaryExtension_A$Integer() throws Exception {
    Integer adrPrimaryExtension = null;
    target.setAdrPrimaryExtension(adrPrimaryExtension);
  }

  @Test
  public void setAdrState_A$Short() throws Exception {
    Short adrState = null;
    target.setAdrState(adrState);
  }

  @Test
  public void setAdrStreetName_A$String() throws Exception {
    String adrStreetName = null;
    target.setAdrStreetName(adrStreetName);
  }

  @Test
  public void setAdrStreetNumber_A$String() throws Exception {
    String adrStreetNumber = null;
    target.setAdrStreetNumber(adrStreetNumber);
  }

  @Test
  public void setAdrZip_A$String() throws Exception {
    String adrZip = null;
    target.setAdrZip(adrZip);
  }

  @Test
  public void setAdrZip4_A$Short() throws Exception {
    Short adrZip4 = null;
    target.setAdrZip4(adrZip4);
  }

  @Test
  public void setAdrPostDirCd_A$String() throws Exception {
    String adrPostDirCd = null;
    target.setAdrPostDirCd(adrPostDirCd);
  }

  @Test
  public void setAdrPreDirCd_A$String() throws Exception {
    String adrPreDirCd = null;
    target.setAdrPreDirCd(adrPreDirCd);
  }

  @Test
  public void setAdrStreetSuffixCd_A$Short() throws Exception {
    Short adrStreetSuffixCd = null;
    target.setAdrStreetSuffixCd(adrStreetSuffixCd);
  }

  @Test
  public void setAdrUnitDesignationCd_A$Short() throws Exception {
    Short adrUnitDesignationCd = null;
    target.setAdrUnitDesignationCd(adrUnitDesignationCd);
  }

  @Test
  public void setAdrUnitNumber_A$String() throws Exception {
    String adrUnitNumber = null;
    target.setAdrUnitNumber(adrUnitNumber);
  }

  @Test
  public void hashCode_A$() throws Exception {
    int actual = target.hashCode();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

}
