package gov.ca.cwds.neutron.rocket.cases;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.Test;

import gov.ca.cwds.jobs.Goddard;

public class MinClientCaseTest extends Goddard {

  final String clientId = DEFAULT_CLIENT_ID;
  final String caseId = "1234567abc";
  MinClientCase target;

  @Override
  public void setup() throws Exception {
    super.setup();

    target = new MinClientCase(clientId, caseId);
  }

  @Test
  public void type() throws Exception {
    assertThat(MinClientCase.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void extract_A$ResultSet() throws Exception {
    final MinClientCase actual = MinClientCase.extract(rs);
    final MinClientCase expected = new MinClientCase(DEFAULT_CLIENT_ID, DEFAULT_CLIENT_ID);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = SQLException.class)
  public void extract_A$ResultSet_T$SQLException() throws Exception {
    when(rs.getString(any(String.class))).thenThrow(SQLException.class);
    MinClientCase.extract(rs);
  }

  @Test
  public void getClientId_A$() throws Exception {
    String actual = target.getClientId();
    String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClientId_A$String() throws Exception {
    String clientId_ = DEFAULT_CLIENT_ID;
    target.setClientId(clientId_);
  }

  @Test
  public void getCaseId_A$() throws Exception {
    String actual = target.getCaseId();
    String expected = "1234567abc";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCaseId_A$String() throws Exception {
    String referralId = null;
    target.setCaseId(referralId);
  }

}
