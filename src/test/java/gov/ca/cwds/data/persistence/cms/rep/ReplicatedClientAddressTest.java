package gov.ca.cwds.data.persistence.cms.rep;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;
import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.rest.api.domain.DomainChef;

public class ReplicatedClientAddressTest extends Goddard<ReplicatedClient, RawClient> {

  ReplicatedClientAddress target;

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();
    target = new ReplicatedClientAddress();
  }

  @Test
  public void type() throws Exception {
    assertThat(ReplicatedClientAddress.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getAddresses_Args__() throws Exception {
    Set<ReplicatedAddress> actual = target.getAddresses();
    Set<ReplicatedAddress> expected = new HashSet<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAddresses_Args__Set() throws Exception {
    Set<ReplicatedAddress> addresses = mock(Set.class);
    target.setAddresses(addresses);
  }

  @Test
  public void setAddresses_Args__Set__null() throws Exception {
    Set<ReplicatedAddress> addresses = null;
    target.setAddresses(addresses);
  }

  @Test
  public void addAddress_Args__ReplicatedAddress() throws Exception {
    ReplicatedAddress address = mock(ReplicatedAddress.class);
    target.addAddress(address);
  }

  @Test
  public void addAddress_Args__ReplicatedAddress__null() throws Exception {
    ReplicatedAddress address = null;
    target.addAddress(address);
  }

  @Test
  public void getAddresses_A$() throws Exception {
    Set<ReplicatedAddress> actual = target.getAddresses();
    Set<ReplicatedAddress> expected = new HashSet<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAddresses_A$Set() throws Exception {
    Set<ReplicatedAddress> addresses = mock(Set.class);
    target.setAddresses(addresses);
  }

  @Test
  public void addAddress_A$ReplicatedAddress() throws Exception {
    ReplicatedAddress address = mock(ReplicatedAddress.class);
    target.addAddress(address);
  }

  @Test
  public void isActive_A$() throws Exception {
    boolean actual = target.isActive();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isActive__end_dated() throws Exception {
    target.setEffEndDt(new Date());
    boolean actual = target.isActive();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isResidence_A$() throws Exception {
    boolean actual = target.isResidence();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isBusiness_A$() throws Exception {
    boolean actual = target.isBusiness();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLegacyId_A$() throws Exception {
    String actual = target.getLegacyId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLegacyDescriptor_A$() throws Exception {
    ElasticSearchLegacyDescriptor actual = target.getLegacyDescriptor();
    ElasticSearchLegacyDescriptor expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getReplicatedEntity_A$() throws Exception {
    final Date date = DomainChef.uncookDateString("2018-05-21");
    target.setReplicationDate(date);
    target.setReplicationOperation(CmsReplicationOperation.I);

    final EmbeddableCmsReplicatedEntity actual = target.getReplicatedEntity();
    final EmbeddableCmsReplicatedEntity expected = new EmbeddableCmsReplicatedEntity();
    expected.setReplicationDate(date);
    expected.setReplicationOperation(CmsReplicationOperation.I);

    assertThat(actual, is(equalTo(expected)));
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

}
