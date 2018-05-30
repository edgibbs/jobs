package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.update.UpdateRequest;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.query.NativeQuery;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.BatchBucket;
import gov.ca.cwds.dao.cms.ReplicatedAkaDao;
import gov.ca.cwds.dao.cms.ReplicatedOtherClientNameDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPersonAka;
import gov.ca.cwds.data.persistence.cms.ReplicatedAkas;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherClientName;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.utils.JsonUtils;

/**
 * 
 * @author CWDS API Team
 */
public class OtherClientNameIndexerJobTest
    extends Goddard<ReplicatedAkas, ReplicatedOtherClientName> {

  ReplicatedAkaDao normDao;
  ReplicatedOtherClientNameDao denormDao;
  OtherClientNameIndexerJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    normDao = new ReplicatedAkaDao(sessionFactory);
    denormDao = new ReplicatedOtherClientNameDao(sessionFactory);

    final javax.persistence.Query q = mock(javax.persistence.Query.class);
    when(em.createNativeQuery(any(String.class), any(Class.class))).thenReturn(q);
    final List<BatchBucket> buckets = new ArrayList<>();
    final BatchBucket b = new BatchBucket();
    b.setBucket(1);
    b.setBucketCount(2);
    b.setMaxId("1");
    b.setMaxId("2");
    buckets.add(b);
    when(q.getResultList()).thenReturn(buckets);

    final NativeQuery<ReplicatedOtherClientName> qn = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any(String.class))).thenReturn(qn);
    when(qn.setString(any(String.class), any(String.class))).thenReturn(qn);
    when(qn.setHibernateFlushMode(any(FlushMode.class))).thenReturn(qn);
    when(qn.setReadOnly(any(Boolean.class))).thenReturn(qn);
    when(qn.setCacheMode(any(CacheMode.class))).thenReturn(qn);
    when(qn.setFetchSize(any(Integer.class))).thenReturn(qn);
    when(qn.setCacheable(any(Boolean.class))).thenReturn(qn);

    final ScrollableResults results = mock(ScrollableResults.class);
    when(qn.scroll(any(ScrollMode.class))).thenReturn(results);

    final List<ReplicatedOtherClientName> denorms = new ArrayList<>();
    final ReplicatedOtherClientName m = new ReplicatedOtherClientName();
    denorms.add(m);
    when(qn.list()).thenReturn(denorms);

    target = new OtherClientNameIndexerJob(normDao, denormDao, esDao, MAPPER, flightPlan, launchDirector);
  }

  private ReplicatedOtherClientName makeReplicatedBean() throws IOException {
    return (ReplicatedOtherClientName) JsonUtils.from(
        "{\"clientId\":\"abc123456789\",\"clientIndexNumber\":null,\"clientSensitivityIndicator\":null,"
            + "\"firstName\":\"abc123456789\",\"id\":\"abc123456789\",\"lastName\":\"abc123456789\","
            + "\"lastUpdatedId\":\"abc123456789\","
            // + "\"lastUpdatedTime\":\"2018-02-15\","
            + "\"legacyDescriptor\":{\"legacy_id\":\"abc123456789\",\"legacy_ui_id\":\"abc123456789\","
            // + "\"legacy_last_updated\":\"2018-02-15T16:42:25.726-0800\","
            + "\"legacy_table_name\":\"OCL_NM_T\","
            + "\"legacy_table_description\":\"Alias or other client name\"},\"legacyId\":\"abc123456789\","
            + "\"middleName\":\"abc123456789\",\"namePrefixDescription\":\"abc123456789\",\"nameType\":0,"
            + "\"normalizationClass\":\"gov.ca.cwds.data.persistence.cms.ReplicatedAkas\","
            + "\"normalizationGroupKey\":\"abc123456789\",\"primaryKey\":\"abc123456789\","
            + "\"replicatedEntity\":{\"replicationOperation\":null,\"replicationDate\":null},\"replicationDate\":null,"
            + "\"replicationOperation\":null,\"sensitivityIndicator\":null,\"soc158SealedClientIndicator\":null,"
            + "\"suffixTitleDescription\":\"abc123456789\",\"thirdId\":\"abc123456789\"}",
        ReplicatedOtherClientName.class);
  }

  @Test
  public void testType() throws Exception {
    assertThat(OtherClientNameIndexerJob.class, notNullValue());
  }

  @Test
  public void testInstantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void type() throws Exception {
    assertThat(OtherClientNameIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getPartitionRanges_Args__() throws Exception {
    final List<?> actual = target.getPartitionRanges();
    assertThat(actual, notNullValue());
  }

  @Test
  public void extract_Args__ResultSet() throws Exception {
    final ReplicatedOtherClientName actual = target.extract(rs);
    assertThat(actual, notNullValue());
  }

  @Test
  public void getDenormalizedClass_Args__() throws Exception {
    final Object actual = target.getDenormalizedClass();
    final Object expected = ReplicatedOtherClientName.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalizeSingle_Args__List() throws Exception {
    final List<ReplicatedOtherClientName> recs = new ArrayList<ReplicatedOtherClientName>();
    final ReplicatedOtherClientName m = new ReplicatedOtherClientName();
    m.setClientId(DEFAULT_CLIENT_ID);
    recs.add(m);
    final ReplicatedAkas actual = target.normalizeSingle(recs);
    assertThat(actual, notNullValue());
  }

  @Test
  public void normalize_Args__List() throws Exception {
    final List<ReplicatedOtherClientName> recs = new ArrayList<ReplicatedOtherClientName>();
    final List<ReplicatedAkas> actual = target.normalize(recs);
    assertThat(actual, notNullValue());
  }

  @Test
  public void prepareUpsertRequest_Args__ElasticSearchPerson__ReplicatedAkas() throws Exception {
    final ElasticSearchPerson esp = new ElasticSearchPerson();
    final ReplicatedAkas p = new ReplicatedAkas(DEFAULT_CLIENT_ID);
    final ElasticSearchPersonAka aka = new ElasticSearchPersonAka();
    aka.setFirstName("Albert");
    aka.setLastName("Einstein");
    p.addAka(aka);
    final UpdateRequest actual = target.prepareUpsertRequest(esp, p);
    assertThat(actual, notNullValue());
  }

  @Test
  public void getInitialLoadViewName_Args__() throws Exception {
    final String actual = target.getInitialLoadViewName();
    assertThat(actual, notNullValue());
  }

  @Test
  public void getJdbcOrderBy_Args__() throws Exception {
    final String actual = target.getJdbcOrderBy().trim();
    final String expected = "ORDER BY x.FKCLIENT_T";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadQuery_Args__String() throws Exception {
    final String dbSchemaName = "CWSRS1";
    final String actual = target.getInitialLoadQuery(dbSchemaName);
    assertThat(actual, notNullValue());
  }

  @Test
  public void main_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/dsmith/client_indexer_time.txt", "-S"};
    OtherClientNameIndexerJob.main(args);
  }

  @Test
  public void getPrepLastChangeSQL_A$() throws Exception {
    final String actual = target.getPrepLastChangeSQL();
    final String expected =
        "INSERT INTO GT_ID (IDENTIFIER)\nSELECT CLT.IDENTIFIER AS CLIENT_ID\nFROM OCL_NM_T ONM\nJOIN CLIENT_T CLT ON CLT.IDENTIFIER = ONM.FKCLIENT_T\nWHERE ONM.IBMSNAP_LOGMARKER > '2018-12-31 03:21:12.000' \nUNION ALL\nSELECT CLT.IDENTIFIER\nFROM CLIENT_T CLT \nWHERE CLT.IBMSNAP_LOGMARKER > '2018-12-31 03:21:12.000' ";
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void extract_A$ResultSet() throws Exception {
  // final ReplicatedOtherClientName actual = target.extract(rs);
  // final ReplicatedOtherClientName expected = makeReplicatedBean();
  // assertThat(actual, is(equalTo(expected)));
  // }

  @Test(expected = SQLException.class)
  public void extract_A$ResultSet_T$SQLException() throws Exception {
    when(rs.getString(any(String.class))).thenThrow(SQLException.class);
    target.extract(rs);
  }

  @Test
  public void getDenormalizedClass_A$() throws Exception {
    final Object actual = target.getDenormalizedClass();
    final Object expected = ReplicatedOtherClientName.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalizeSingle_A$List() throws Exception {
    final List<ReplicatedOtherClientName> recs = new ArrayList<ReplicatedOtherClientName>();
    final ReplicatedAkas actual = target.normalizeSingle(recs);
    final ReplicatedAkas expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_A$List() throws Exception {
    final List<ReplicatedOtherClientName> recs = new ArrayList<ReplicatedOtherClientName>();
    final List<ReplicatedAkas> actual = target.normalize(recs);
    final List<ReplicatedAkas> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getDriverTable_A$() throws Exception {
    final String actual = target.getDriverTable();
    final String expected = "VW_LST_OTHER_CLIENT_NAME";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getOptionalElementName_A$() throws Exception {
    final String actual = target.getOptionalElementName();
    final String expected = "akas";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void prepareUpsertRequest_A$ElasticSearchPerson$ReplicatedAkas() throws Exception {
    final ElasticSearchPerson esp = new ElasticSearchPerson();
    final ReplicatedAkas p = new ReplicatedAkas();
    final UpdateRequest actual = target.prepareUpsertRequest(esp, p);
    // UpdateRequest expected = null;
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = NeutronCheckedException.class)
  public void prepareUpsertRequest_A$ElasticSearchPerson$ReplicatedAkas_T$NeutronCheckedException()
      throws Exception {
    when(rs.next()).thenThrow(SQLException.class);
    final ElasticSearchPerson esp = null;
    final ReplicatedAkas p = new ReplicatedAkas();
    target.prepareUpsertRequest(esp, p);
  }

  @Test
  public void getInitialLoadViewName_A$() throws Exception {
    final String actual = target.getInitialLoadViewName();
    final String expected = "MQT_OTHER_CLIENT_NAME";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJdbcOrderBy_A$() throws Exception {
    final String actual = target.getJdbcOrderBy();
    final String expected = " ORDER BY x.FKCLIENT_T ";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isInitialLoadJdbc_A$() throws Exception {
    final boolean actual = target.isInitialLoadJdbc();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPartitionRanges_A$() throws Exception {
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    final List<Pair<String, String>> expected = new ArrayList<>();
    expected.add(pair);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadQuery_A$String() throws Exception {
    final String dbSchemaName = null;
    final String actual = target.getInitialLoadQuery(dbSchemaName);
    final String expected =
        "SELECT x.* FROM null.MQT_OTHER_CLIENT_NAME x  WHERE x.CLIENT_SENSITIVITY_IND = 'N'  ORDER BY x.FKCLIENT_T  FOR READ ONLY WITH UR ";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = Exception.class)
  public void main_A$StringArray_T$Exception() throws Exception {
    final String[] args = new String[] {};
    OtherClientNameIndexerJob.main(args);
  }

}
