package gov.ca.cwds.neutron.inject;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClientRelationship;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.rest.api.domain.cms.SystemCode;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeDescriptor;
import gov.ca.cwds.rest.api.domain.cms.SystemMeta;

public class NeutronSystemCodeCacheTest
    extends Goddard<ReplicatedClientRelationship, ReplicatedClientRelationship> {

  NeutronSystemCodeCache target;
  SystemCodeCache sourceCache;

  private static final Short DEFAULT_SYSCODE_KEY = new Short((short) 3199);

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    sourceCache = mock(SystemCodeCache.class);
    target = new NeutronSystemCodeCache(sourceCache);
  }

  @Test
  public void type() throws Exception {
    assertThat(NeutronSystemCodeCache.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getAllSystemMetas_A$() throws Exception {
    final Set<SystemMeta> actual = target.getAllSystemMetas();
    final Set<SystemMeta> expected = new HashSet<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAllSystemCodes_A$() throws Exception {
    final Set<SystemCode> actual = target.getAllSystemCodes();
    final Set<SystemCode> expected = new HashSet<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getSystemCode_A$Number() throws Exception {
    final Number key = DEFAULT_SYSCODE_KEY;
    final SystemCode actual = target.getSystemCode(key);
    final SystemCode expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getSystemCodeShortDescription_A$Number() throws Exception {
    final Number key = DEFAULT_SYSCODE_KEY;
    final String actual = target.getSystemCodeShortDescription(key);
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getSystemCodeDescriptor_A$Number() throws Exception {
    final Number key = DEFAULT_SYSCODE_KEY;
    final SystemCodeDescriptor actual = target.getSystemCodeDescriptor(key);
    final SystemCodeDescriptor expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getSystemCodesForMeta_A$String() throws Exception {
    final String key = "LANG_TPC";
    final Set<SystemCode> actual = target.getSystemCodesForMeta(key);
    final Set<SystemCode> expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getSystemCodeId_A$String$String() throws Exception {
    final String shortDescription = null;
    final String meta = null;
    final Short actual = target.getSystemCodeId(shortDescription, meta);
    final Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void verifyActiveSystemCodeIdForMeta_A$Number$String$boolean() throws Exception {
    final Number systemCodeId = DEFAULT_SYSCODE_KEY;
    final String metaId = null;
    final boolean checkCategoryIdValueIsZero = false;
    final boolean actual =
        target.verifyActiveSystemCodeIdForMeta(systemCodeId, metaId, checkCategoryIdValueIsZero);
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void verifyActiveLogicalIdForMeta_A$String$String() throws Exception {
    final String logicalId = null;
    final String metaId = null;
    final boolean actual = target.verifyActiveLogicalIdForMeta(logicalId, metaId);
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void verifyActiveSystemCodeDescriptionForMeta_A$String$String() throws Exception {
    final String shortDesc = null;
    final String metaId = null;
    final boolean actual = target.verifyActiveSystemCodeDescriptionForMeta(shortDesc, metaId);
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

}
