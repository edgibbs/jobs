package gov.ca.cwds.data.persistence.cms.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.client.RawClientCounty.ColumnPosition;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class RawClientCountyTest extends Goddard<ReplicatedClient, RawClient> {

  RawClientCounty target;

  public static void prepResultSetGood(ResultSet rs) throws SQLException {
    when(rs.getString(ColumnPosition.CLT_IDENTIFIER.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.CLC_CNTY_RULE.ordinal())).thenReturn("RELATED_CLIENT");
    when(rs.getShort(ColumnPosition.CLC_GVR_ENTC.ordinal())).thenReturn((short) 1068);
  }

  @Override
  public void setup() throws Exception {
    super.setup();

    target = new RawClientCounty();
    target.setClientCounty((short) 1123);
    target.setCltId(DEFAULT_CLIENT_ID);
  }

  @Test
  public void type() throws Exception {
    assertThat(RawClientCounty.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void read_A$ResultSet() throws Exception {
    RawClientCounty actual = target.read(rs);
    // RawClientCounty expected = null;
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
  public void getClientCounty_A$() throws Exception {
    Short actual = target.getClientCounty();
    Short expected = (short) 1123;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClientCounty_A$Short() throws Exception {
    Short clientCounty = null;
    target.setClientCounty(clientCounty);
  }

  @Test
  public void getClientCountyRule_A$() throws Exception {
    String actual = target.getClientCountyRule();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClientCountyRule_A$String() throws Exception {
    String clientCountyRule = null;
    target.setClientCountyRule(clientCountyRule);
  }

}
