package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.update.UpdateRequest;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.db2.jcc.DB2SystemMonitor;

import gov.ca.cwds.dao.cms.ReplicatedPersonReferralsDao;
import gov.ca.cwds.data.es.ElasticSearchPersonAllegation;
import gov.ca.cwds.data.es.ElasticSearchPersonReferral;
import gov.ca.cwds.data.es.NeutronElasticSearchDao;
import gov.ca.cwds.data.persistence.cms.EsPersonReferral;
import gov.ca.cwds.data.persistence.cms.ReplicatedPersonReferrals;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.flight.FlightPlanTest;
import gov.ca.cwds.neutron.rocket.referral.MinClientReferral;
import gov.ca.cwds.neutron.util.NeutronThreadUtils;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;

public class ReferralHistoryIndexerJobTest
    extends Goddard<ReplicatedPersonReferrals, EsPersonReferral> {

  @SuppressWarnings("serial")
  public static class TestReferralHistoryIndexerJob extends ReferralHistoryIndexerJob {

    private boolean fakePull = true;
    private boolean throwOnRanges = false;

    public TestReferralHistoryIndexerJob(ReplicatedPersonReferralsDao clientDao,
        NeutronElasticSearchDao esDao, String lastRunFile, ObjectMapper mapper,
        FlightPlan flightPlan, AtomLaunchDirector launchDirector) {
      super(clientDao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
    }

    public static DB2SystemMonitor monitorStart(final Connection con) {
      return new TestDB2SystemMonitor();
    }

    @Override
    protected int pullNextRange(Pair<String, String> p) throws NeutronCheckedException {
      return fakePull ? 0 : super.pullNextRange(p);
    }

    @Override
    public List<Pair<String, String>> getPartitionRanges() throws NeutronCheckedException {
      if (throwOnRanges) {
        throw new NeutronCheckedException("test");
      }

      return super.getPartitionRanges();
    }

    @Override
    public void cleanUpMemory(List<EsPersonReferral> listAllegations,
        Map<String, EsPersonReferral> mapReferrals, List<MinClientReferral> listClientReferralKeys,
        List<EsPersonReferral> listReadyToNorm) {
      super.cleanUpMemory(listAllegations, mapReferrals, listClientReferralKeys, listReadyToNorm);
    }

    @Override
    public int mapReduce(List<EsPersonReferral> listAllegations,
        Map<String, EsPersonReferral> mapReferrals, List<MinClientReferral> listClientReferralKeys,
        List<EsPersonReferral> listReadyToNorm) {
      return super.mapReduce(listAllegations, mapReferrals, listClientReferralKeys,
          listReadyToNorm);
    }

  }

  public static final String DEFAULT_REFERRAL_ID = "ref1234567";
  public static final String DEFAULT_ALLEGATION_ID = "alg1234567";

  // ====================
  // TEST MEMBERS:
  // ====================

  ReplicatedPersonReferralsDao dao;
  TestReferralHistoryIndexerJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    dao = new ReplicatedPersonReferralsDao(sessionFactory);
    target = new TestReferralHistoryIndexerJob(dao, esDao, lastRunFile, MAPPER,
        FlightPlanTest.makeGeneric(), launchDirector);

    when(rs.next()).thenReturn(true, true, false);
  }

  @Test
  public void type() throws Exception {
    assertThat(ReferralHistoryIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getDenormalizedClass_Args__() throws Exception {
    Object actual = target.getDenormalizedClass();
    Object expected = EsPersonReferral.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getViewName_Args__() throws Exception {
    final String actual = target.getInitialLoadViewName();
    final String expected = "VW_MQT_REFRL_ONLY";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJdbcOrderBy_Args__() throws Exception {
    final String actual = target.getJdbcOrderBy().trim();
    final String expected = "";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalizeSingle_Args__List() throws Exception {
    List<EsPersonReferral> recs = new ArrayList<EsPersonReferral>();

    EsPersonReferral addMe = new EsPersonReferral();
    addMe.setClientId("qz11234567");
    addMe.setReferralId("abc1234567");

    recs.add(addMe);
    ReplicatedPersonReferrals actual = target.normalizeSingle(recs);
    assertThat(actual, notNullValue());
  }

  @Test
  public void normalize_Args__List() throws Exception {
    List<EsPersonReferral> recs = new ArrayList<EsPersonReferral>();
    List<ReplicatedPersonReferrals> actual = target.normalize(recs);
    List<ReplicatedPersonReferrals> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void prepareUpsertRequest_Args__ElasticSearchPerson__ReplicatedPersonReferrals()
      throws Exception {
    final ReplicatedPersonReferrals referrals = new ReplicatedPersonReferrals(DEFAULT_CLIENT_ID);
    final ElasticSearchPersonReferral ref = new ElasticSearchPersonReferral();
    ref.setId(DEFAULT_CLIENT_ID);
    final ElasticSearchPersonAllegation allegation = new ElasticSearchPersonAllegation();
    allegation.setId(DEFAULT_ALLEGATION_ID);
    referrals.addReferral(ref, allegation);
    UpdateRequest actual = target.prepareUpsertRequest(esp, referrals);
    assertThat(actual, notNullValue());
  }

  @Test
  public void prepareUpsertRequest_Args__ElasticSearchPerson__ReplicatedPersonReferrals_T__Exception()
      throws Exception {
    final ReplicatedPersonReferrals referrals = new ReplicatedPersonReferrals(DEFAULT_CLIENT_ID);
    when(esDao.getConfig().getElasticsearchAlias()).thenThrow(new NeutronRuntimeException("test"));
    try {
      target.prepareUpsertRequest(esp, referrals);
      fail("Expected exception was not thrown!");
    } catch (Exception e) {
    }

  }

  @Test(expected = NeutronCheckedException.class)
  public void prepareUpsertRequest_Args__boom() throws Exception {
    final ReplicatedPersonReferrals referrals = new ReplicatedPersonReferrals(DEFAULT_CLIENT_ID);
    final ElasticSearchPersonReferral ref = new ElasticSearchPersonReferral();
    ref.setId(DEFAULT_CLIENT_ID);
    final ElasticSearchPersonAllegation allegation = new ElasticSearchPersonAllegation();
    allegation.setId(DEFAULT_ALLEGATION_ID);
    referrals.addReferral(ref, allegation);
    mapper = mock(ObjectMapper.class);
    when(mapper.writeValueAsString(any(Object.class))).thenThrow(JsonProcessingException.class);
    target.setMapper(mapper);
    target.prepareUpsertRequest(esp, referrals);
  }

  @Test
  public void extract_Args__ResultSet() throws Exception {
    final EsPersonReferral actual = target.extract(rs);
    assertThat(actual, notNullValue());
  }

  @Test(expected = SQLException.class)
  public void extract_Args__ResultSet_T__SQLException() throws Exception {
    doThrow(new SQLException()).when(rs).getString(any());
    target.extract(rs);
  }

  @Test
  public void getInitialLoadViewName_Args__() throws Exception {
    final String actual = target.getInitialLoadViewName();
    assertThat(actual, notNullValue());
  }

  @Test
  public void getInitialLoadQuery_Args__String() throws Exception {
    String dbSchemaName = "CWSRS1";
    final String actual = target.getInitialLoadQuery(dbSchemaName);
    assertThat(actual, notNullValue());
  }

  @Test
  public void getInitialLoadQuery_Args__no_sealed() throws Exception {
    String dbSchemaName = "CWSRS1";
    target.getFlightPlan().setLoadSealedAndSensitive(false);
    final String actual = target.getInitialLoadQuery(dbSchemaName);
    assertThat(actual, notNullValue());
  }

  @Test
  public void testNormalizeQueryResults() throws Exception {
    final Map<String, EsPersonReferral> mapReferrals = new HashMap<>();
    final List<EsPersonReferral> listReadyToNorm = new ArrayList<>();
    final Map<String, List<MinClientReferral>> mapReferralByClient = new HashMap<>();
    final Map<String, List<EsPersonReferral>> mapAllegationByReferral = new HashMap<>();
    List<EsPersonReferral> allegations = new ArrayList<>();
    List<MinClientReferral> minClientReferrals = new ArrayList<>();
    MinClientReferral minClRef =
        new MinClientReferral(DEFAULT_CLIENT_ID, DEFAULT_REFERRAL_ID, "N", null);
    minClientReferrals.add(minClRef);
    mapReferralByClient.put(DEFAULT_CLIENT_ID, minClientReferrals);
    EsPersonReferral ref = new EsPersonReferral();
    ref.setClientId(DEFAULT_CLIENT_ID);
    ref.setReferralId(DEFAULT_REFERRAL_ID);
    ref.setAllegationId("alg1111111");
    ref.setAllegationType(4);
    listReadyToNorm.add(ref);
    allegations.add(ref);
    mapReferrals.put(DEFAULT_REFERRAL_ID, ref);
    ref = new EsPersonReferral();
    ref.setClientId(DEFAULT_CLIENT_ID);
    ref.setReferralId(DEFAULT_REFERRAL_ID);
    ref.setAllegationId("alg2222222");
    ref.setAllegationType(3);
    listReadyToNorm.add(ref);
    allegations.add(ref);
    mapAllegationByReferral.put(ref.getReferralId(), allegations);
    target.normalizeQueryResults(mapReferrals, listReadyToNorm, mapReferralByClient,
        mapAllegationByReferral);
  }

  @Test
  public void testReadClients() throws Exception {
    final PreparedStatement stmtInsClient = mock(PreparedStatement.class);
    final PreparedStatement stmtSelClient = mock(PreparedStatement.class);
    when(stmtSelClient.executeQuery()).thenReturn(rs);
    final List<MinClientReferral> listClientReferralKeys = new ArrayList<>();
    when(rs.getString("FKCLIENT_T")).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString("FKREFERL_T")).thenReturn(DEFAULT_REFERRAL_ID);
    when(rs.getString("SENSTV_IND")).thenReturn("N");
    final Pair<String, String> p = pair;
    target.readClients(stmtInsClient, stmtSelClient, listClientReferralKeys, p);
  }

  @Test
  public void testReadAllegations() throws Exception {
    final PreparedStatement stmtSelAllegation = mock(PreparedStatement.class);
    when(stmtSelAllegation.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(true).thenReturn(false);
    final List<EsPersonReferral> listAllegations = new ArrayList<>();
    target.readAllegations(stmtSelAllegation, listAllegations);
  }

  @Test
  public void testMonitorStopAndReport() throws Exception {
    NeutronDB2Utils.monitorStopAndReport(new TestDB2SystemMonitor());
  }

  @Test
  public void testCalcThreads() throws Exception {
    NeutronThreadUtils.calcReaderThreads(target.getFlightPlan());
  }

  @Test
  public void testReadReferrals() throws Exception {
    final PreparedStatement stmtSelReferral = mock(PreparedStatement.class);
    when(stmtSelReferral.executeQuery()).thenReturn(rs);
    final Map<String, EsPersonReferral> mapReferrals = new HashMap<>();
    target.readReferrals(stmtSelReferral, mapReferrals);
  }

  @Test
  public void pullRange_Args__Pair() throws Exception {
    final String schema = target.getDBSchemaName();
    final PreparedStatement stmtInsClient = mock(PreparedStatement.class);
    final PreparedStatement stmtSelClient = mock(PreparedStatement.class);
    final PreparedStatement stmtSelReferral = mock(PreparedStatement.class);
    final PreparedStatement stmtSelAllegation = mock(PreparedStatement.class);
    final ResultSet rsInsClient = mock(ResultSet.class);
    final ResultSet rsSelClient = mock(ResultSet.class);
    final ResultSet rsSelReferral = mock(ResultSet.class);
    final ResultSet rsSelAllegation = mock(ResultSet.class);

    final String sqlInsClient =
        target.INSERT_CLIENT_FULL.replaceAll("#SCHEMA#", schema).replaceAll("\\s+", " ").trim();
    final String sqlSelClient =
        target.SELECT_CLIENT.replaceAll("#SCHEMA#", schema).replaceAll("\\s+", " ").trim();
    final String sqlSelReferral = target.getInitialLoadQuery(schema).replaceAll("\\s+", " ").trim();
    final String selAllegation =
        target.SELECT_ALLEGATION.replaceAll("#SCHEMA#", schema).replaceAll("\\s+", " ").trim();

    when(con.prepareStatement(sqlInsClient)).thenReturn(stmtInsClient);
    when(con.prepareStatement(sqlSelClient)).thenReturn(stmtSelClient);
    when(con.prepareStatement(sqlSelReferral)).thenReturn(stmtSelReferral);
    when(con.prepareStatement(selAllegation)).thenReturn(stmtSelAllegation);

    when(stmtInsClient.executeQuery()).thenReturn(rsInsClient);
    when(stmtSelClient.executeQuery()).thenReturn(rsSelClient);
    when(stmtSelReferral.executeQuery()).thenReturn(rsSelReferral);
    when(stmtSelAllegation.executeQuery()).thenReturn(rsSelAllegation);
    when(rsInsClient.next()).thenReturn(true).thenReturn(false);
    when(rsSelClient.next()).thenReturn(true).thenReturn(false);
    when(rsSelReferral.next()).thenReturn(false);
    when(rsSelAllegation.next()).thenReturn(false);

    when(rsSelClient.getString("FKCLIENT_T")).thenReturn(DEFAULT_CLIENT_ID);
    when(rsSelClient.getString("FKREFERL_T")).thenReturn(DEFAULT_REFERRAL_ID);
    when(rsSelClient.getString("SENSTV_IND")).thenReturn("N");

    final Pair<String, String> p = pair;
    target.fakePull = false;
    target.pullRange(p, null);
  }

  @Test
  public void pullNextRange_Args__Pair() throws Exception {
    final String schema = target.getDBSchemaName();
    final PreparedStatement stmtInsClient = mock(PreparedStatement.class);
    final PreparedStatement stmtSelClient = mock(PreparedStatement.class);
    final PreparedStatement stmtSelReferral = mock(PreparedStatement.class);
    final PreparedStatement stmtSelAllegation = mock(PreparedStatement.class);
    final ResultSet rsInsClient = mock(ResultSet.class);
    final ResultSet rsSelClient = mock(ResultSet.class);
    final ResultSet rsSelReferral = mock(ResultSet.class);
    final ResultSet rsSelAllegation = mock(ResultSet.class);

    final String sqlInsClient = target.getClientSeedQuery();
    final String sqlSelClient = target.SELECT_CLIENT;
    final String sqlSelReferral = target.getInitialLoadQuery(schema);
    final String selAllegation = target.SELECT_ALLEGATION;

    when(con.prepareStatement(sqlInsClient)).thenReturn(stmtInsClient);
    when(con.prepareStatement(sqlSelClient)).thenReturn(stmtSelClient);
    when(con.prepareStatement(sqlSelReferral)).thenReturn(stmtSelReferral);
    when(con.prepareStatement(selAllegation)).thenReturn(stmtSelAllegation);

    when(stmtInsClient.executeQuery()).thenReturn(rsInsClient);
    when(stmtSelClient.executeQuery()).thenReturn(rsSelClient);
    when(stmtSelReferral.executeQuery()).thenReturn(rsSelReferral);
    when(stmtSelAllegation.executeQuery()).thenReturn(rsSelAllegation);

    when(rsInsClient.next()).thenReturn(true).thenReturn(false);
    when(rsSelClient.next()).thenReturn(true).thenReturn(false);
    when(rsSelReferral.next()).thenReturn(false);
    when(rsSelAllegation.next()).thenReturn(false);

    when(rsSelClient.getString("FKCLIENT_T")).thenReturn(DEFAULT_CLIENT_ID);
    when(rsSelClient.getString("FKREFERL_T")).thenReturn(DEFAULT_REFERRAL_ID);
    when(rsSelClient.getString("SENSTV_IND")).thenReturn("N");

    final Pair<String, String> p = pair;
    target.fakePull = false;
    target.pullNextRange(p);
  }

  @Test(expected = NeutronCheckedException.class)
  public void pullNextRange_Args__Pair__boom() throws Exception {
    final String schema = target.getDBSchemaName();

    final PreparedStatement stmtInsClient = mock(PreparedStatement.class);
    final PreparedStatement stmtSelClient = mock(PreparedStatement.class);
    final PreparedStatement stmtSelReferral = mock(PreparedStatement.class);
    final PreparedStatement stmtSelAllegation = mock(PreparedStatement.class);

    final ResultSet rsInsClient = mock(ResultSet.class);
    final ResultSet rsSelClient = mock(ResultSet.class);
    final ResultSet rsSelReferral = mock(ResultSet.class);
    final ResultSet rsSelAllegation = mock(ResultSet.class);

    final String sqlInsClient =
        target.INSERT_CLIENT_FULL.replaceAll("#SCHEMA#", schema).replaceAll("\\s+", " ").trim();
    final String sqlSelClient =
        target.SELECT_CLIENT.replaceAll("#SCHEMA#", schema).replaceAll("\\s+", " ").trim();
    final String sqlSelReferral = target.getInitialLoadQuery(schema).replaceAll("\\s+", " ").trim();
    final String selAllegation =
        target.SELECT_ALLEGATION.replaceAll("#SCHEMA#", schema).replaceAll("\\s+", " ").trim();

    when(con.prepareStatement(sqlInsClient)).thenReturn(stmtInsClient);
    when(con.prepareStatement(sqlSelClient)).thenReturn(stmtSelClient);
    when(con.prepareStatement(sqlSelReferral)).thenReturn(stmtSelReferral);
    when(con.prepareStatement(selAllegation)).thenReturn(stmtSelAllegation);

    when(stmtInsClient.executeQuery()).thenReturn(rsInsClient);
    when(stmtSelClient.executeQuery()).thenReturn(rsSelClient);
    when(stmtSelReferral.executeQuery()).thenReturn(rsSelReferral);
    when(stmtSelAllegation.executeQuery()).thenReturn(rsSelAllegation);

    when(rsInsClient.next()).thenReturn(true).thenReturn(false);
    when(rsSelClient.next()).thenReturn(true).thenReturn(false);
    when(rsSelReferral.next()).thenReturn(false);
    when(rsSelAllegation.next()).thenReturn(false);

    when(rsSelClient.getString("FKCLIENT_T")).thenReturn(DEFAULT_CLIENT_ID);
    when(rsSelClient.getString("FKREFERL_T")).thenReturn(DEFAULT_REFERRAL_ID);
    when(rsSelClient.getString("SENSTV_IND")).thenReturn("N");

    when(con.prepareStatement(any(String.class))).thenThrow(SQLException.class);

    final Pair<String, String> p = pair;
    target.fakePull = false;
    target.pullNextRange(p);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void pullRange_Args__Pair_throw() throws Exception {
    final String schema = target.getDBSchemaName();
    when(con.prepareStatement(any())).thenThrow(NeutronRuntimeException.class);
    when(con.prepareStatement(any())).thenThrow(NeutronRuntimeException.class);
    doThrow(SQLException.class).when(con).setAutoCommit(false);
    final Pair<String, String> p = pair;
    target.fakePull = false;
    target.pullRange(p, null);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void threadExtractJdbc_Args__() throws Exception {
    target.threadRetrieveByJdbc();
  }

  @Test(expected = NeutronRuntimeException.class)
  public void threadExtractJdbc_Args__throw() throws Exception {
    target.throwOnRanges = true;
    target.threadRetrieveByJdbc();
  }

  @Test
  public void useTransformThread_Args__() throws Exception {
    boolean actual = target.useTransformThread();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPartitionRanges_Args__() throws Exception {
    final List actual = target.getPartitionRanges();
    assertThat(actual, notNullValue());
  }

  @Test
  public void mustDeleteLimitedAccessRecords_Args__() throws Exception {
    boolean actual = target.mustDeleteLimitedAccessRecords();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void extractReferral_Args__ResultSet() throws Exception {
    final EsPersonReferral actual = target.extract(rs);
    assertThat(actual, notNullValue());
  }

  @Test(expected = SQLException.class)
  public void extractReferral_Args__ResultSet_T__SQLException() throws Exception {
    when(rs.next()).thenThrow(new SQLException());
    when(rs.getString(any(String.class))).thenThrow(new SQLException());
    when(rs.getString(any(Integer.class))).thenThrow(new SQLException());
    target.extract(rs);
  }

  @Test
  public void isRangeSelfManaging_Args__() throws Exception {
    final boolean actual = target.isInitialLoadJdbc();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getConnection_Args__() throws Exception {
    final Connection actual = target.getConnection();
    assertThat(actual, notNullValue());
  }

  @Test(expected = SQLException.class)
  public void getConnection_Args___T__SQLException() throws Exception {
    when(dao.grabSession()).thenThrow(SQLException.class);
    target.getConnection();
  }

  @Test
  public void allocateThreadMemory_Args__() throws Exception {
    target.allocateThreadMemory();
  }

  @Test
  public void getPrepLastChangeSQL_Args__() throws Exception {
    final String actual = target.getPrepLastChangeSQL();
    assertThat(actual, notNullValue());
  }

  @Test
  public void getOptionalElementName() throws Exception {
    final String actual = target.getOptionalElementName();
    final String expected = "referrals";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void main_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/var/lib/jenkins/client_indexer_time.txt", "-S"};
    TestReferralHistoryIndexerJob.main(args);
  }

  @Test
  public void readClients_Args__PreparedStatement__PreparedStatement__List__Pair()
      throws Exception {
    PreparedStatement stmtInsClient = mock(PreparedStatement.class);
    PreparedStatement stmtSelClient = mock(PreparedStatement.class);
    List<MinClientReferral> listClientReferralKeys = new ArrayList<MinClientReferral>();
    Pair<String, String> p = pair;
    when(stmtSelClient.executeQuery()).thenReturn(rs);
    target.readClients(stmtInsClient, stmtSelClient, listClientReferralKeys, p);
  }

  @Test
  public void readReferrals_Args__PreparedStatement__Map_T() throws Exception {
    when(rs.getString("FKCLIENT_T")).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString("FKREFERL_T")).thenReturn(DEFAULT_REFERRAL_ID);
    when(rs.getString("SENSTV_IND")).thenReturn("N");
    when(rs.next()).thenReturn(true, false);

    final PreparedStatement stmtSelReferral = mock(PreparedStatement.class);
    when(stmtSelReferral.executeQuery()).thenReturn(rs);

    Map<String, EsPersonReferral> mapReferrals = new HashMap<String, EsPersonReferral>();
    target.readReferrals(stmtSelReferral, mapReferrals);
  }

  @Test
  public void readAllegations_Args__PreparedStatement__List() throws Exception {
    PreparedStatement stmtSelAllegation = mock(PreparedStatement.class);
    when(stmtSelAllegation.executeQuery()).thenReturn(rs);
    List<EsPersonReferral> listAllegations = new ArrayList<EsPersonReferral>();
    target.readAllegations(stmtSelAllegation, listAllegations);
  }

  @Test
  public void normalizeClientReferrals_Args__int__MinClientReferral__String__Map__List__Map()
      throws Exception {
    int cntr = 0;
    MinClientReferral rc1 = mock(MinClientReferral.class);
    String clientId = null;
    Map<String, EsPersonReferral> mapReferrals = new HashMap<String, EsPersonReferral>();
    List<EsPersonReferral> listReadyToNorm = new ArrayList<EsPersonReferral>();
    final Map mapAllegationByReferral = new HashMap<>();
    int actual = target.normalizeClientReferrals(cntr, rc1, clientId, mapReferrals, listReadyToNorm,
        mapAllegationByReferral);
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalizeQueryResults_Args__Map__List__Map__Map() throws Exception {
    Map<String, EsPersonReferral> mapReferrals = new HashMap<String, EsPersonReferral>();
    List<EsPersonReferral> listReadyToNorm = new ArrayList<EsPersonReferral>();
    Map mapReferralByClient = new HashMap();
    Map mapAllegationByReferral = new HashMap();
    int actual = target.normalizeQueryResults(mapReferrals, listReadyToNorm, mapReferralByClient,
        mapAllegationByReferral);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void cleanUpMemory_Args__List__Map__List__List() throws Exception {
    List<EsPersonReferral> listAllegations = new ArrayList<EsPersonReferral>();
    Map<String, EsPersonReferral> mapReferrals = new HashMap<String, EsPersonReferral>();
    List<MinClientReferral> listClientReferralKeys = new ArrayList<MinClientReferral>();
    List<EsPersonReferral> listReadyToNorm = new ArrayList<EsPersonReferral>();
    target.cleanUpMemory(listAllegations, mapReferrals, listClientReferralKeys, listReadyToNorm);
  }

  @Test
  public void mapReduce_Args__List__Map__List__List() throws Exception {
    List<EsPersonReferral> listAllegations = new ArrayList<EsPersonReferral>();
    Map<String, EsPersonReferral> mapReferrals = new HashMap<String, EsPersonReferral>();
    List<MinClientReferral> listClientReferralKeys = new ArrayList<MinClientReferral>();
    List<EsPersonReferral> listReadyToNorm = new ArrayList<EsPersonReferral>();

    EsPersonReferral addMe = new EsPersonReferral();
    addMe.setClientId("qz11234567");
    addMe.setReferralId("abc1234567");
    listReadyToNorm.add(addMe);

    addMe = new EsPersonReferral();
    addMe.setClientId(DEFAULT_CLIENT_ID);
    addMe.setReferralId("abc1234567");
    listReadyToNorm.add(addMe);

    final int actual =
        target.mapReduce(listAllegations, mapReferrals, listClientReferralKeys, listReadyToNorm);
    final int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronRuntimeException.class)
  public void threadRetrieveByJdbc_Args__() throws Exception {
    target.threadRetrieveByJdbc();
  }

  @Test
  public void isInitialLoadJdbc_Args__() throws Exception {
    final boolean actual = target.isInitialLoadJdbc();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getOptionalElementName_Args__() throws Exception {
    final String actual = target.getOptionalElementName();
    final String expected = "referrals";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void buildMonitor_Args__Connection() throws Exception {
    final Connection con = mock(Connection.class);
    final DB2SystemMonitor actual = target.buildMonitor(con);
    final DB2SystemMonitor expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void monitorStopAndReport_Args__DB2SystemMonitor() throws Exception {
    DB2SystemMonitor monitor = mock(DB2SystemMonitor.class);
    target.monitorStopAndReport(monitor);
  }

  @Test
  public void monitorStopAndReport_Args__DB2SystemMonitor_T__SQLException() throws Exception {
    DB2SystemMonitor monitor = mock(DB2SystemMonitor.class);
    doThrow(SQLException.class).when(monitor).stop();
    try {
      target.monitorStopAndReport(monitor);
      fail("Expected exception was not thrown!");
    } catch (SQLException e) {
    }
  }

  @Test
  public void getClientSeedQuery_Args__() throws Exception {
    final String actual = target.getClientSeedQuery();
    final String expected =
        "INSERT INTO GT_REFR_CLT (FKREFERL_T, FKCLIENT_T, SENSTV_IND)\nSELECT rc.FKREFERL_T, rc.FKCLIENT_T, c.SENSTV_IND\nFROM REFR_CLT rc\nJOIN CLIENT_T c on c.IDENTIFIER = rc.FKCLIENT_T\nWHERE rc.FKCLIENT_T BETWEEN ? AND ?\n  AND  c.IBMSNAP_OPERATION IN ('I','U') ";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void releaseLocalMemory_Args__List__Map__List__List() throws Exception {
    final List<EsPersonReferral> listAllegations = new ArrayList<EsPersonReferral>();
    final Map<String, EsPersonReferral> mapReferrals = new HashMap<String, EsPersonReferral>();
    final List<MinClientReferral> listClientReferralKeys = new ArrayList<MinClientReferral>();
    final List<EsPersonReferral> listReadyToNorm = new ArrayList<EsPersonReferral>();
    target.releaseLocalMemory(listAllegations, mapReferrals, listClientReferralKeys,
        listReadyToNorm);
  }

  @Test
  public void isMonitorDb2_Args__() throws Exception {
    final boolean actual = target.isMonitorDb2();
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setMonitorDb2_Args__boolean() throws Exception {
    final boolean monitorDb2 = false;
    target.setMonitorDb2(monitorDb2);
  }

}
