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

import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;

public class RawSafetyAlertTest extends Goddard<ReplicatedClient, RawClient> {

  RawSafetyAlert target;

  @Override
  public void setup() throws Exception {
    super.setup();
    target = new RawSafetyAlert();
  }

  @Test
  public void type() throws Exception {
    assertThat(RawSafetyAlert.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void read_A$ResultSet() throws Exception {
    RawSafetyAlert actual = target.read(rs);
    // RawSafetyAlert expected = null;
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
  public void getSafetyAlertId_A$() throws Exception {
    String actual = target.getSafetyAlertId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertId_A$String() throws Exception {
    String safetyAlertId = null;
    target.setSafetyAlertId(safetyAlertId);
  }

  @Test
  public void getSafetyAlertActivationReasonCode_A$() throws Exception {
    Short actual = target.getSafetyAlertActivationReasonCode();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertActivationReasonCode_A$Short() throws Exception {
    Short safetyAlertActivationReasonCode = null;
    target.setSafetyAlertActivationReasonCode(safetyAlertActivationReasonCode);
  }

  @Test
  public void getSafetyAlertActivationDate_A$() throws Exception {
    Date actual = target.getSafetyAlertActivationDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertActivationDate_A$Date() throws Exception {
    Date safetyAlertActivationDate = mock(Date.class);
    target.setSafetyAlertActivationDate(safetyAlertActivationDate);
  }

  @Test
  public void getSafetyAlertActivationCountyCode_A$() throws Exception {
    Short actual = target.getSafetyAlertActivationCountyCode();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertActivationCountyCode_A$Short() throws Exception {
    Short safetyAlertActivationCountyCode = null;
    target.setSafetyAlertActivationCountyCode(safetyAlertActivationCountyCode);
  }

  @Test
  public void getSafetyAlertActivationExplanation_A$() throws Exception {
    String actual = target.getSafetyAlertActivationExplanation();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertActivationExplanation_A$String() throws Exception {
    String safetyAlertActivationExplanation = null;
    target.setSafetyAlertActivationExplanation(safetyAlertActivationExplanation);
  }

  @Test
  public void getSafetyAlertDeactivationDate_A$() throws Exception {
    Date actual = target.getSafetyAlertDeactivationDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertDeactivationDate_A$Date() throws Exception {
    Date safetyAlertDeactivationDate = mock(Date.class);
    target.setSafetyAlertDeactivationDate(safetyAlertDeactivationDate);
  }

  @Test
  public void getSafetyAlertDeactivationCountyCode_A$() throws Exception {
    Short actual = target.getSafetyAlertDeactivationCountyCode();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertDeactivationCountyCode_A$Short() throws Exception {
    Short safetyAlertDeactivationCountyCode = null;
    target.setSafetyAlertDeactivationCountyCode(safetyAlertDeactivationCountyCode);
  }

  @Test
  public void getSafetyAlertDeactivationExplanation_A$() throws Exception {
    String actual = target.getSafetyAlertDeactivationExplanation();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertDeactivationExplanation_A$String() throws Exception {
    String safetyAlertDeactivationExplanation = null;
    target.setSafetyAlertDeactivationExplanation(safetyAlertDeactivationExplanation);
  }

  @Test
  public void getSafetyAlertLastUpdatedId_A$() throws Exception {
    String actual = target.getSafetyAlertLastUpdatedId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertLastUpdatedId_A$String() throws Exception {
    String safetyAlertLastUpdatedId = null;
    target.setSafetyAlertLastUpdatedId(safetyAlertLastUpdatedId);
  }

  @Test
  public void getSafetyAlertLastUpdatedTimestamp_A$() throws Exception {
    Date actual = target.getSafetyAlertLastUpdatedTimestamp();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertLastUpdatedTimestamp_A$Date() throws Exception {
    Date safetyAlertLastUpdatedTimestamp = mock(Date.class);
    target.setSafetyAlertLastUpdatedTimestamp(safetyAlertLastUpdatedTimestamp);
  }

  @Test
  public void getSafetyAlertLastUpdatedOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getSafetyAlertLastUpdatedOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertLastUpdatedOperation_A$CmsReplicationOperation() throws Exception {
    CmsReplicationOperation safetyAlertLastUpdatedOperation = CmsReplicationOperation.U;
    target.setSafetyAlertLastUpdatedOperation(safetyAlertLastUpdatedOperation);
  }

  @Test
  public void getSafetyAlertReplicationTimestamp_A$() throws Exception {
    Date actual = target.getSafetyAlertReplicationTimestamp();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSafetyAlertReplicationTimestamp_A$Date() throws Exception {
    Date safetyAlertReplicationTimestamp = mock(Date.class);
    target.setSafetyAlertReplicationTimestamp(safetyAlertReplicationTimestamp);
  }

}
