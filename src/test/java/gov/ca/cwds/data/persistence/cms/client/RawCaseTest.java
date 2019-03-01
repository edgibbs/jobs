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

import gov.ca.cwds.data.persistence.cms.client.RawCase.ColumnPosition;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class RawCaseTest extends Goddard<ReplicatedClient, RawClient> {

  RawCase target;

  public static void prepResultSetGood(ResultSet rs) throws SQLException {
    when(rs.getString(ColumnPosition.CLT_IDENTIFIER.ordinal())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(ColumnPosition.CAS_IDENTIFIER.ordinal())).thenReturn("1234567xyz");
    when(rs.getString(ColumnPosition.CAS_RSP_AGY_CD.ordinal())).thenReturn("C");
  }

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new RawCase();
  }

  @Test
  public void type() throws Exception {
    assertThat(RawCase.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void read_A$ResultSet() throws Exception {
    RawCaseTest.prepResultSetGood(rs);
    RawCase actual = target.read(rs);

    RawCase expected = new RawCase();
    expected.setOpenCaseId("1234567xyz");
    expected.setOpenCaseResponsibleAgencyCode("C");
    expected.setCltId(DEFAULT_CLIENT_ID);

    assertThat(actual, is(equalTo(expected)));
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
  public void getOpenCaseId_A$() throws Exception {
    String actual = target.getOpenCaseId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setOpenCaseId_A$String() throws Exception {
    String openCaseId = null;
    target.setOpenCaseId(openCaseId);
  }

  @Test
  public void getOpenCaseResponsibleAgencyCode_A$() throws Exception {
    String actual = target.getOpenCaseResponsibleAgencyCode();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setOpenCaseResponsibleAgencyCode_A$String() throws Exception {
    String openCaseResponsibleAgencyCode = null;
    target.setOpenCaseResponsibleAgencyCode(openCaseResponsibleAgencyCode);
  }

}
