package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.update.UpdateRequest;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedRelationshipsDao;
import gov.ca.cwds.data.es.ElasticSearchPersonRelationship;
import gov.ca.cwds.data.persistence.cms.EsRelationship;
import gov.ca.cwds.data.persistence.cms.ReplicatedRelationships;
import gov.ca.cwds.neutron.enums.NeutronDateTimeFormat;

public class RelationshipIndexerJobTest extends Goddard<ReplicatedRelationships, EsRelationship> {

  ReplicatedRelationshipsDao dao;
  RelationshipIndexerJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    dao = new ReplicatedRelationshipsDao(sessionFactory);
    target = new RelationshipIndexerJob(dao, esDao, lastRunFile, MAPPER, flightPlan);
    target.writeLastSuccessfulRunTime(
        new SimpleDateFormat(NeutronDateTimeFormat.LAST_RUN_DATE_FORMAT.getFormat())
            .parse("2018-01-22 10:53:20"));
  }

  @Test
  public void type() throws Exception {
    assertThat(RelationshipIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void threadExtractJdbc_Args__() throws Exception {
    target.threadRetrieveByJdbc();
  }

  @Test
  public void extract_Args__ResultSet() throws Exception {
    when(rs.next()).thenReturn(true).thenReturn(false);
    EsRelationship actual = target.extract(rs);
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = SQLException.class)
  public void extract_Args__ResultSet_T__SQLException() throws Exception {
    when(rs.next()).thenThrow(SQLException.class);
    when(rs.getString(any(String.class))).thenThrow(SQLException.class);
    target.extract(rs);
  }

  @Test
  public void getDenormalizedClass_Args__() throws Exception {
    Object actual = target.getDenormalizedClass();
    Object expected = EsRelationship.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getViewName_Args__() throws Exception {
    String actual = target.getInitialLoadViewName();
    String expected = "VW_MQT_BI_DIR_RELATION";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJdbcOrderBy_Args__() throws Exception {
    String actual = target.getJdbcOrderBy();
    String expected = " ORDER BY THIS_LEGACY_ID, RELATED_LEGACY_ID ";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void prepareUpsertRequest_Args__ElasticSearchPerson__ReplicatedRelationships()
      throws Exception {
    ReplicatedRelationships p = new ReplicatedRelationships(DEFAULT_CLIENT_ID);
    ElasticSearchPersonRelationship rel = new ElasticSearchPersonRelationship();
    rel.setIndexedPersonRelationship("Uncle");
    rel.setRelatedPersonFirstName("Fred");
    rel.setRelatedPersonLastName("Meyer");
    rel.setRelatedPersonLegacyId("xyz1234567");
    rel.setRelatedPersonId("xyz1234567");
    p.addRelation(rel);

    UpdateRequest actual = target.prepareUpsertRequest(esp, p);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void pullRange_Args__Pair() throws Exception {
    final Pair<String, String> p = Pair.of("aaaaaaaaaa", "9999999999");
    target.pullRange(p, null);
  }

  @Test
  public void getPartitionRanges_Args() throws Exception {
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    final List<Pair<String, String>> expected = new ArrayList<>();
    expected.add(Pair.of("aaaaaaaaaa", "9999999999"));
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPartitionRanges_RSQ() throws Exception {
    System.setProperty("DB_CMS_SCHEMA", "CWSRSQ");
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    assertThat(actual.size(), is(equalTo(64)));
  }

  @Test
  public void getPrepLastChangeSQL() throws Exception {
    assertThat(target.getPrepLastChangeSQL(), is(equalTo(
        "INSERT INTO GT_ID (IDENTIFIER)\nWITH LAST_CHG AS (\n SELECT DISTINCT CLNR.IDENTIFIER AS REL_ID\n FROM CLN_RELT CLNR \n WHERE CLNR.IBMSNAP_LOGMARKER > '2018-01-22 10:40:20.000' \n UNION \n SELECT DISTINCT CLNR.IDENTIFIER AS REL_ID \n FROM CLN_RELT CLNR\n JOIN CLIENT_T CLNS ON CLNR.FKCLIENT_T = CLNS.IDENTIFIER\n WHERE CLNS.IBMSNAP_LOGMARKER > '2018-01-22 10:40:20.000' \n UNION \n SELECT DISTINCT CLNR.IDENTIFIER AS REL_ID\n FROM CLN_RELT CLNR\n JOIN CLIENT_T CLNP ON CLNR.FKCLIENT_0 = CLNP.IDENTIFIER\n WHERE CLNP.IBMSNAP_LOGMARKER > '2018-01-22 10:40:20.000' \n),\nCHG_CLIENTS AS (\n SELECT DISTINCT CLNP.IDENTIFIER AS CLIENT_ID\n FROM LAST_CHG LC\n JOIN CLN_RELT CLNR ON CLNR.IDENTIFIER = LC.REL_ID\n JOIN CLIENT_T CLNP ON CLNR.FKCLIENT_0 = CLNP.IDENTIFIER\n UNION\n SELECT DISTINCT CLNS.IDENTIFIER AS CLIENT_ID\n FROM LAST_CHG LC\n JOIN CLN_RELT CLNR ON CLNR.IDENTIFIER = LC.REL_ID\n JOIN CLIENT_T CLNS ON CLNR.FKCLIENT_T = CLNS.IDENTIFIER\n)\nSELECT chg.CLIENT_ID FROM CHG_CLIENTS chg\n")));
  }

  @Test
  public void normalizeAndQueueIndex() throws Exception {
    List<EsRelationship> grpRecs = new ArrayList<EsRelationship>();
    EsRelationship rel = new EsRelationship();
    rel.setThisLegacyId(DEFAULT_CLIENT_ID);
    rel.setRelatedLegacyId("xyz1234567");
    grpRecs.add(rel);
    target.normalizeAndQueueIndex(grpRecs);
  }

  @Test
  public void normalizeSingle_Args__List() throws Exception {
    List<EsRelationship> recs = new ArrayList<EsRelationship>();
    EsRelationship rel = new EsRelationship();
    rel.setRelatedLegacyId(DEFAULT_CLIENT_ID);
    recs.add(rel);
    ReplicatedRelationships actual = target.normalizeSingle(recs);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void normalize_Args__List() throws Exception {
    List<EsRelationship> recs = new ArrayList<EsRelationship>();
    List<ReplicatedRelationships> actual = target.normalize(recs);
    List<ReplicatedRelationships> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPrepLastChangeSQL_Args__() throws Exception {
    String actual = target.getPrepLastChangeSQL();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getInitialLoadViewName_Args__() throws Exception {
    String actual = target.getInitialLoadViewName();
    String expected = "VW_MQT_BI_DIR_RELATION";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadQuery_Args__String() throws Exception {
    String dbSchemaName = null;
    String actual = target.getInitialLoadQuery(dbSchemaName);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void normalizeAndQueueIndex_Args__List() throws Exception {
    List<EsRelationship> grpRecs = new ArrayList<EsRelationship>();
    target.normalizeAndQueueIndex(grpRecs);
  }

  @Test
  public void threadRetrieveByJdbc_Args__() throws Exception {
    target.threadRetrieveByJdbc();
  }

  @Test
  public void useTransformThread_Args__() throws Exception {
    boolean actual = target.useTransformThread();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isInitialLoadJdbc_Args__() throws Exception {
    boolean actual = target.isInitialLoadJdbc();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPartitionRanges_Args__() throws Exception {
    List<Pair<String, String>> actual = target.getPartitionRanges();
    assertThat(actual.size(), is(equalTo(1)));
  }

  @Test
  public void getOptionalElementName_Args__() throws Exception {
    String actual = target.getOptionalElementName();
    String expected = "relationships";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void main_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/CWS-NS3/client_indexer_time.txt", "-S"};
    RelationshipIndexerJob.main(args);
  }

}
