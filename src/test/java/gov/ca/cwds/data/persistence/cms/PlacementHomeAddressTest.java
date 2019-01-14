package gov.ca.cwds.data.persistence.cms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.PlacementHomeAddress.ColumnPosition;
import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClientAddress;
import gov.ca.cwds.jobs.Goddard;

public class PlacementHomeAddressTest extends Goddard<ReplicatedClient, RawClient> {

  PlacementHomeAddress target;

  public static void prepResultSetGood(ResultSet rs) throws SQLException {
    when(rs.getString(ColumnPosition.CLIENT_ID.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.PE_THIRD_ID.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.PH_ID.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.OHP_ID.ordinal())).thenReturn(DEFAULT_CLIENT_ID);

    // when(rs.getShort(ColumnPosition.PH_GVR_ENTC.ordinal())).thenReturn((short) 1111);
    // when(rs.getShort(ColumnPosition.PE_GVR_ENTC.ordinal())).thenReturn((short) 1111);
    when(rs.getShort(ColumnPosition.STATE_C.ordinal())).thenReturn((short) 1828);
    when(rs.getShort(ColumnPosition.ZIP_SFX_NO.ordinal())).thenReturn((short) 1234);

    when(rs.getString(ColumnPosition.CITY_NM.ordinal())).thenReturn("Campbell");
    when(rs.getString(ColumnPosition.STREET_NM.ordinal())).thenReturn("Via Roma");
    when(rs.getString(ColumnPosition.STREET_NO.ordinal())).thenReturn("2111");

    when(rs.getInt(ColumnPosition.ZIP_NO.ordinal())).thenReturn(95008);
    when(rs.getInt(ColumnPosition.PRM_EXT_NO.ordinal())).thenReturn(1234);

    when(rs.getLong(ColumnPosition.PRM_TEL_NO.ordinal())).thenReturn(4083742790L);

    Date date = new Date();
    when(rs.getTimestamp(ColumnPosition.PH_LST_UPD_TS.ordinal()))
        .thenReturn(new Timestamp(date.getTime()));
  }

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();
    prepResultSetGood(rs);
    target = new PlacementHomeAddress(rs);
  }

  @Test
  public void type() throws Exception {
    assertThat(PlacementHomeAddress.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void toReplicatedClientAddress_A$() throws Exception {
    ReplicatedClientAddress actual = target.toReplicatedClientAddress();
    // ReplicatedClientAddress expected = null;
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getClientId_A$() throws Exception {
    String actual = target.getClientId();
    String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getThirdId_A$() throws Exception {
    String actual = target.getThirdId();
    String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getOtherHomePlacementId_A$() throws Exception {
    String actual = target.getOtherHomePlacementId();
    String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPlacementHomeId_A$() throws Exception {
    String actual = target.getPlacementHomeId();
    String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPlacementEpisodeGovernmentEntityCd_A$() throws Exception {
    Short actual = target.getPlacementEpisodeGovernmentEntityCd();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPlacementHomeGovernmentEntityCd_A$() throws Exception {
    Short actual = target.getPlacementHomeGovernmentEntityCd();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getStreetNumber_A$() throws Exception {
    String actual = target.getStreetNumber();
    String expected = "2111";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getStreetName_A$() throws Exception {
    String actual = target.getStreetName();
    String expected = "Via Roma";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getCity_A$() throws Exception {
    String actual = target.getCity();
    String expected = "Campbell";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getState_A$() throws Exception {
    Short actual = target.getState();
    Short expected = 1828;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getZip_A$() throws Exception {
    Integer actual = target.getZip();
    Integer expected = 95008;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getZip4_A$() throws Exception {
    Short actual = target.getZip4();
    Short expected = 1234;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLastUpdatedTime_A$() throws Exception {
    Date actual = target.getLastUpdatedTime();

    final Calendar cal = Calendar.getInstance();
    cal.add(Calendar.HOUR, -1);
    Date expected = cal.getTime();

    assertThat(actual, is(greaterThanOrEqualTo(expected)));
  }

  @Test
  public void getStart_A$() throws Exception {
    Date actual = target.getStart();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getEnd_A$() throws Exception {
    Date actual = target.getEnd();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPrimaryNumber_A$() throws Exception {
    Long actual = target.getPrimaryNumber();
    Long expected = 4083742790L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPrimaryExtension_A$() throws Exception {
    Integer actual = target.getPrimaryExtension();
    Integer expected = 1234;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getNormalizationClass_A$() throws Exception {
    final Class<ReplicatedClientAddress> actual = target.getNormalizationClass();
    final Class<ReplicatedClientAddress> expected = ReplicatedClientAddress.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getNormalizationGroupKey_A$() throws Exception {
    Serializable actual = target.getNormalizationGroupKey();
    Serializable expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_A$Map() throws Exception {
    Map<Object, ReplicatedClientAddress> arg0 = new HashMap<Object, ReplicatedClientAddress>();
    ReplicatedClientAddress actual = target.normalize(arg0);
    // ReplicatedClientAddress expected = null;
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getPrimaryKey_A$() throws Exception {
    final Serializable actual = target.getPrimaryKey();
    final Serializable expected = new VarargPrimaryKey(DEFAULT_CLIENT_ID, DEFAULT_CLIENT_ID,
        DEFAULT_CLIENT_ID, DEFAULT_CLIENT_ID);
    assertThat(actual, is(equalTo(expected)));
  }

}
