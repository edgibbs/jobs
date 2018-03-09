package gov.ca.cwds.data.persistence.cms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.data.es.ElasticSearchPersonRelationship;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.jobs.Goddard;

public class EsRelationshipTest extends Goddard<ReplicatedRelationships, EsRelationship> {

  EsRelationship target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    target = new EsRelationship();
    target.setThisLegacyId(DEFAULT_CLIENT_ID);
  }

  @Test
  public void type() throws Exception {
    assertThat(EsRelationship.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void mapRow_A$ResultSet() throws Exception {
    final EsRelationship actual = EsRelationship.mapRow(rs);
    // final EsRelationship expected = new EsRelationship();
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = SQLException.class)
  public void mapRow_A$ResultSet_T$SQLException() throws Exception {
    when(rs.getString(any(String.class))).thenThrow(SQLException.class);
    EsRelationship.mapRow(rs);
  }

  @Test
  public void getNormalizationClass_A$() throws Exception {
    final Class<ReplicatedRelationships> actual = target.getNormalizationClass();
    final Class<ReplicatedRelationships> expected = ReplicatedRelationships.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void parseBiDirectionalRelationship_A$ElasticSearchPersonRelationship() throws Exception {
    final ElasticSearchPersonRelationship rel = new ElasticSearchPersonRelationship();
    rel.setRelatedPersonId(DEFAULT_CLIENT_ID);

    target.setRelCode((short) 199);
    target.setReverseRelationship(false);
    target.parseBiDirectionalRelationship(rel);
  }

  @Test
  public void parseBiDirectionalRelationship_reverse_relationship() throws Exception {
    final ElasticSearchPersonRelationship rel = new ElasticSearchPersonRelationship();
    rel.setRelatedPersonId(DEFAULT_CLIENT_ID);

    target.setRelCode((short) 199);
    target.setReverseRelationship(true);
    target.parseBiDirectionalRelationship(rel);
  }

  @Test
  public void normalize_A$Map() throws Exception {
    final Map<Object, ReplicatedRelationships> map = new HashMap<>();
    final ReplicatedRelationships rel = new ReplicatedRelationships();
    rel.setId(DEFAULT_CLIENT_ID);
    map.put(DEFAULT_CLIENT_ID, rel);

    target.setThisClientReplicationOperation(CmsReplicationOperation.U);
    target.setRelatedClientReplicationOperation(CmsReplicationOperation.U);
    target.setRelationshipReplicationOperation(CmsReplicationOperation.U);
    target.setThisFirstName("Boy");
    target.setThisLastName("George");
    target.setRelatedFirstName("Homer");
    target.setRelatedLastName("Simpson");
    target.setRelatedLegacyId("1234567yca");

    final ReplicatedRelationships actual = target.normalize(map);

    final ReplicatedRelationships expected = new ReplicatedRelationships();
    expected.setId(DEFAULT_CLIENT_ID);
    // expected.getReplicatedEntity().setReplicationDate(new Date());
    // expected.getReplicatedEntity().setReplicationOperation(CmsReplicationOperation.I);
    // expected.setReplicationDate(new Date());
    // expected.setReplicationOperation(CmsReplicationOperation.I);
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getNormalizationGroupKey_A$() throws Exception {
    final String actual = target.getNormalizationGroupKey();
    final String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isActive_A$() throws Exception {
    final boolean actual = target.isActive();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getThisReplicationOperation_A$() throws Exception {
    final CmsReplicationOperation actual = target.getThisReplicationOperation();
    final CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setThisReplicationOperation_A$CmsReplicationOperation() throws Exception {
    final CmsReplicationOperation thisReplicationOperation = CmsReplicationOperation.U;
    target.setThisReplicationOperation(thisReplicationOperation);
  }

  @Test
  public void getThisReplicationDate_A$() throws Exception {
    final Date actual = target.getThisReplicationDate();
    final Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setThisReplicationDate_A$Date() throws Exception {
    final Date thisReplicationDate = new Date();
    target.setThisReplicationDate(thisReplicationDate);
  }

  @Test
  public void getRelatedReplicationOperation_A$() throws Exception {
    final CmsReplicationOperation actual = target.getRelatedReplicationOperation();
    final CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelatedReplicationOperation_A$CmsReplicationOperation() throws Exception {
    final CmsReplicationOperation relatedReplicationOperation = CmsReplicationOperation.U;
    target.setRelatedReplicationOperation(relatedReplicationOperation);
  }

  @Test
  public void getRelatedReplicationDate_A$() throws Exception {
    final Date actual = target.getRelatedReplicationDate();
    final Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelatedReplicationDate_A$Date() throws Exception {
    final Date relatedReplicationDate = new Date();
    target.setRelatedReplicationDate(relatedReplicationDate);
  }

  @Test
  public void getSerialversionuid_A$() throws Exception {
    final long actual = EsRelationship.getSerialversionuid();
    final long expected = 1L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPrimaryKey_A$() throws Exception {
    final Serializable actual = target.getPrimaryKey();
    final Serializable expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void compare_A$EsRelationship$EsRelationship() throws Exception {
    final EsRelationship o1 = new EsRelationship();
    o1.setThisLegacyId(DEFAULT_CLIENT_ID);
    final EsRelationship o2 = new EsRelationship();
    o2.setThisLegacyId(DEFAULT_CLIENT_ID);
    final int actual = target.compare(o1, o2);
    final int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void compareTo_A$EsRelationship() throws Exception {
    final EsRelationship o = new EsRelationship();
    o.setThisLegacyId(DEFAULT_CLIENT_ID);
    final int actual = target.compareTo(o);
    final int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void hashCode_A$() throws Exception {
    final int actual = target.hashCode();
    assertThat(actual, is(not(0)));
  }

  @Test
  public void equals_A$Object() throws Exception {
    final Object obj = null;
    final boolean actual = target.equals(obj);
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void toString_A$() throws Exception {
    String actual = target.toString();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getThisLegacyId_A$() throws Exception {
    String actual = target.getThisLegacyId();
    String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setThisLegacyId_A$String() throws Exception {
    String thisLegacyId = null;
    target.setThisLegacyId(thisLegacyId);
  }

  @Test
  public void getThisFirstName_A$() throws Exception {
    String actual = target.getThisFirstName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setThisFirstName_A$String() throws Exception {
    String thisFirstName = null;
    target.setThisFirstName(thisFirstName);
  }

  @Test
  public void getThisLastName_A$() throws Exception {
    String actual = target.getThisLastName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setThisLastName_A$String() throws Exception {
    String thisLastName = null;
    target.setThisLastName(thisLastName);
  }

  @Test
  public void getRelCode_A$() throws Exception {
    Short actual = target.getRelCode();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelCode_A$Short() throws Exception {
    Short relCode = null;
    target.setRelCode(relCode);
  }

  @Test
  public void getRelatedLegacyId_A$() throws Exception {
    String actual = target.getRelatedLegacyId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelatedLegacyId_A$String() throws Exception {
    String relatedLegacyId = null;
    target.setRelatedLegacyId(relatedLegacyId);
  }

  @Test
  public void getRelatedFirstName_A$() throws Exception {
    String actual = target.getRelatedFirstName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelatedFirstName_A$String() throws Exception {
    String relatedFirstName = null;
    target.setRelatedFirstName(relatedFirstName);
  }

  @Test
  public void getRelatedLastName_A$() throws Exception {
    String actual = target.getRelatedLastName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelatedLastName_A$String() throws Exception {
    String relatedLastName = null;
    target.setRelatedLastName(relatedLastName);
  }

  @Test
  public void getReverseRelationship_A$() throws Exception {
    Boolean actual = target.getReverseRelationship();
    Boolean expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReverseRelationship_A$Boolean() throws Exception {
    Boolean reverseRelationship = null;
    target.setReverseRelationship(reverseRelationship);
  }

  @Test
  public void getThisLegacyLastUpdated_A$() throws Exception {
    Date actual = target.getThisLegacyLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setThisLegacyLastUpdated_A$Date() throws Exception {
    Date thisLegacyLastUpdated = new Date();
    target.setThisLegacyLastUpdated(thisLegacyLastUpdated);
  }

  @Test
  public void getRelatedLegacyLastUpdated_A$() throws Exception {
    Date actual = target.getRelatedLegacyLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelatedLegacyLastUpdated_A$Date() throws Exception {
    Date relatedLegacyLastUpdated = new Date();
    target.setRelatedLegacyLastUpdated(relatedLegacyLastUpdated);
  }

  @Test
  public void getRelationshipReplicationOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getRelationshipReplicationOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelationshipReplicationOperation_A$CmsReplicationOperation() throws Exception {
    CmsReplicationOperation relationshipReplicationOperation = CmsReplicationOperation.U;
    target.setRelationshipReplicationOperation(relationshipReplicationOperation);
  }

  @Test
  public void getThisClientReplicationOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getThisClientReplicationOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setThisClientReplicationOperation_A$CmsReplicationOperation() throws Exception {
    CmsReplicationOperation thisClientReplicationOperation = CmsReplicationOperation.U;
    target.setThisClientReplicationOperation(thisClientReplicationOperation);
  }

  @Test
  public void getRelatedClientReplicationOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getRelatedClientReplicationOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelatedClientReplicationOperation_A$CmsReplicationOperation() throws Exception {
    CmsReplicationOperation relatedClientReplicationOperation = CmsReplicationOperation.U;
    target.setRelatedClientReplicationOperation(relatedClientReplicationOperation);
  }

  @Test
  public void getRelationshipReplicationDate_A$() throws Exception {
    Date actual = target.getRelationshipReplicationDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelationshipReplicationDate_A$Date() throws Exception {
    Date relationshipReplicationDate = new Date();
    target.setRelationshipReplicationDate(relationshipReplicationDate);
  }

  @Test
  public void getThisClientReplicationDate_A$() throws Exception {
    Date actual = target.getThisClientReplicationDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setThisClientReplicationDate_A$Date() throws Exception {
    Date thisClientReplicationDate = new Date();
    target.setThisClientReplicationDate(thisClientReplicationDate);
  }

  @Test
  public void getRelatedClientReplicationDate_A$() throws Exception {
    Date actual = target.getRelatedClientReplicationDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelatedClientReplicationDate_A$Date() throws Exception {
    Date relatedClientReplicationDate = new Date();
    target.setRelatedClientReplicationDate(relatedClientReplicationDate);
  }

}
