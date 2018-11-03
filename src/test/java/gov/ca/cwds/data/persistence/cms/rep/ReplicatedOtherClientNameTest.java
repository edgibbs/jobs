package gov.ca.cwds.data.persistence.cms.rep;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;
import gov.ca.cwds.data.es.ElasticSearchPersonAka;
import gov.ca.cwds.data.persistence.cms.ReplicatedAkas;
import gov.ca.cwds.jobs.test.SimpleTestSystemCodeCache;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;

public class ReplicatedOtherClientNameTest {

  private static ReplicatedOtherClientName emptyTarget;

  @Mock
  private ResultSet rs;

  @BeforeClass
  public static void setupClass() throws Exception {
    SimpleTestSystemCodeCache.init();
    emptyTarget = ReplicatedOtherClientName.mapRowToBean(Mockito.mock(ResultSet.class));
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(rs.first()).thenReturn(true);

    final Short shortZero = Short.valueOf((short) 0);

    when(rs.getString("FKCLIENT_T")).thenReturn("abc12340x3");
    when(rs.getString("THIRD_ID")).thenReturn("def56780x3");
    when(rs.getString("LST_UPD_ID")).thenReturn("0x5");
    when(rs.getString("FIRST_NM")).thenReturn("Maynard");
    when(rs.getString("MIDDLE_NM")).thenReturn("James");
    when(rs.getString("LAST_NM")).thenReturn("Keynamn");
    when(rs.getString("NMPRFX_DSC")).thenReturn("Lord");
    when(rs.getString("SUFX_TLDSC")).thenReturn("IV");

    when(rs.getShort("NAME_TPC")).thenReturn(shortZero);

    when(rs.getDate("LST_UPD_TS")).thenReturn(java.sql.Date.valueOf("2017-10-31"));
  }

  @Test
  public void testReplicationOperation() throws Exception {
    ReplicatedOtherClientName target = new ReplicatedOtherClientName();
    target.setReplicationOperation(CmsReplicationOperation.I);
    CmsReplicationOperation actual = target.getReplicationOperation();
    CmsReplicationOperation expected = CmsReplicationOperation.I;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void testReplicationDate() throws Exception {
    ReplicatedOtherClientName target = new ReplicatedOtherClientName();
    DateFormat fmt = new SimpleDateFormat("yyyy-mm-dd");
    Date date = fmt.parse("2012-10-31");
    target.setReplicationDate(date);
    Date actual = target.getReplicationDate();
    Date expected = fmt.parse("2012-10-31");
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void type() throws Exception {
    assertThat(ReplicatedOtherClientName.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    ReplicatedOtherClientName target = new ReplicatedOtherClientName();
    assertThat(target, notNullValue());
  }

  @Test
  public void getNormalizationClass_Args__() throws Exception {
    ReplicatedOtherClientName target = new ReplicatedOtherClientName();
    Class<ReplicatedAkas> actual = target.getNormalizationClass();
    Class<ReplicatedAkas> expected = ReplicatedAkas.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_Args__Map() throws Exception {
    String key = "asd12340x5";
    String thirdId = "ABC12340x5";
    ReplicatedOtherClientName target = new ReplicatedOtherClientName();
    target.setClientId(key);
    target.setThirdId("thirdId");
    target.setFirstName("fred");
    target.setMiddleName("jason");
    target.setLastName("meyer");
    target.setNameType(new Short((short) 1311));
    target.setSuffixTitleDescription("junior");

    Map<Object, ReplicatedAkas> map = new HashMap<Object, ReplicatedAkas>();
    ReplicatedAkas akas = new ReplicatedAkas();
    akas.setId(key);

    ElasticSearchPersonAka aka = new ElasticSearchPersonAka();
    aka.setFirstName("fred");
    aka.setMiddleName("jason");
    aka.setLastName("meyer");
    aka.setLegacyDescriptor(new ElasticSearchLegacyDescriptor(key, thirdId, "2017-12-31",
        LegacyTable.ADDRESS.getName(), "whatever"));
    akas.addAka(aka);
    map.put(key, akas);

    ReplicatedAkas actual = target.normalize(map);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void extract_Args__ResultSet() throws Exception {
    ReplicatedOtherClientName target = new ReplicatedOtherClientName();
    final ReplicatedOtherClientName actual = target.read(rs);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getNormalizationGroupKey_Args__() throws Exception {
    ReplicatedOtherClientName target = new ReplicatedOtherClientName();
    Object actual = target.getNormalizationGroupKey();
    Object expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLegacyId_Args__() throws Exception {
    ReplicatedOtherClientName target = new ReplicatedOtherClientName();
    String actual = target.getLegacyId();
    String expected = "";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getId_Args__() throws Exception {
    ReplicatedOtherClientName target = new ReplicatedOtherClientName();
    String actual = target.getId();
    String expected = "";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLegacyDescriptor_Args__() throws Exception {
    ReplicatedOtherClientName target = new ReplicatedOtherClientName();
    Date lastUpdatedTime = new Date();
    target.setReplicationOperation(CmsReplicationOperation.U);
    target.setLastUpdatedId("0x5");
    target.setLastUpdatedTime(lastUpdatedTime);
    target.setReplicationDate(lastUpdatedTime);
    ElasticSearchLegacyDescriptor actual = target.getLegacyDescriptor();
    assertThat(actual, is(notNullValue()));
  }

}
