package gov.ca.cwds.data.persistence.cms.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.sql.SQLException;

import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.EsClientPerson;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class RawCaseTest extends Goddard<ReplicatedClient, EsClientPerson> {
  RawCase target;

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
    RawCase actual = target.read(rs);
    // RawCase expected = null;
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
