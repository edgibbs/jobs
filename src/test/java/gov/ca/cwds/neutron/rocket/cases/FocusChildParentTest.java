package gov.ca.cwds.neutron.rocket.cases;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.rest.api.domain.cms.SystemCode;

public class FocusChildParentTest extends Goddard<ReplicatedClient, RawClient> {

  String focusClientId = DEFAULT_CLIENT_ID;
  String parentClientId = DEFAULT_CLIENT_ID;
  short relationCode = 0;
  String parentFirstName = "Joseph";
  String parentLastName = "Jones";
  String parentSensitivity = "N";

  FocusChildParent target;

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();
    target = new FocusChildParent(focusClientId, parentClientId, relationCode, parentFirstName,
        parentLastName, parentSensitivity);
  }

  @Test
  public void type() throws Exception {
    assertThat(FocusChildParent.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void extract_A$ResultSet() throws Exception {
    FocusChildParent actual = FocusChildParent.extract(rs);
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = SQLException.class)
  public void extract_A$ResultSet_T$SQLException() throws Exception {
    when(rs.getString(any(String.class))).thenThrow(SQLException.class);
    FocusChildParent.extract(rs);
  }

  @Test
  public void getFocusClientId_A$() throws Exception {
    String actual = target.getFocusClientId();
    String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setFocusClientId_A$String() throws Exception {
    String focusClientId_ = null;
    target.setFocusClientId(focusClientId_);
  }

  @Test
  public void getRelationCode_A$() throws Exception {
    short actual = target.getRelationCode();
    short expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelationCode_A$short() throws Exception {
    short relationCode_ = 0;
    target.setRelationCode(relationCode_);
  }

  @Test
  public void translateRelationship_A$() throws Exception {
    SystemCode actual = target.translateRelationship();
    SystemCode expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void translateRelationshipToString_A$() throws Exception {
    String actual = target.translateRelationshipToString();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getParentClientId_A$() throws Exception {
    String actual = target.getParentClientId();
    String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setParentClientId_A$String() throws Exception {
    String parentClientId_ = null;
    target.setParentClientId(parentClientId_);
  }

  @Test
  public void getParentFirstName_A$() throws Exception {
    String actual = target.getParentFirstName();
    String expected = "Joseph";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setParentFirstName_A$String() throws Exception {
    String parentFirstName_ = null;
    target.setParentFirstName(parentFirstName_);
  }

  @Test
  public void getParentLastName_A$() throws Exception {
    String actual = target.getParentLastName();
    String expected = "Jones";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setParentLastName_A$String() throws Exception {
    String parentLastName_ = null;
    target.setParentLastName(parentLastName_);
  }

  @Test
  public void toString_A$() throws Exception {
    String actual = target.toString();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void hashCode_A$() throws Exception {
    int actual = target.hashCode();
    int expected = 0;
    assertThat(actual, is(not(expected)));
  }

  @Test
  public void equals_A$Object() throws Exception {
    Object obj = null;
    boolean actual = target.equals(obj);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getParentSensitivity_A$() throws Exception {
    String actual = target.getParentSensitivity();
    String expected = "N";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setParentSensitivity_A$String() throws Exception {
    String parentSensitivity_ = null;
    target.setParentSensitivity(parentSensitivity_);
  }

  @Test
  public void getStanza_A$() throws Exception {
    int actual = target.getStanza();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setStanza_A$int() throws Exception {
    int stanza = 0;
    target.setStanza(stanza);
  }

}
