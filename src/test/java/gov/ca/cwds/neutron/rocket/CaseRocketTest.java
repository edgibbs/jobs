package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.update.UpdateRequest;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.dao.cms.ReplicatedPersonCasesDao;
import gov.ca.cwds.dao.cms.StaffPersonDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.persistence.cms.EsCaseRelatedPerson;
import gov.ca.cwds.data.persistence.cms.EsPersonCase;
import gov.ca.cwds.data.persistence.cms.ReplicatedPersonCases;
import gov.ca.cwds.data.persistence.cms.StaffPerson;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.rocket.cases.FocusChildParent;

public class CaseRocketTest extends Goddard<ReplicatedPersonCases, EsCaseRelatedPerson> {

  CaseRocket target;
  ReplicatedPersonCasesDao dao;
  ReplicatedClientDao clientDao;
  StaffPersonDao staffPersonDao;

  public PreparedStatement prepStmt;

  @Override
  public void setup() throws Exception {
    super.setup();
    dao = new ReplicatedPersonCasesDao(sessionFactory);
    clientDao = new ReplicatedClientDao(sessionFactory);
    staffPersonDao = mock(StaffPersonDao.class);

    final List<StaffPerson> staffPersons = new ArrayList<>();
    final StaffPerson staffPerson = new StaffPerson();
    staffPersons.add(staffPerson);
    when(staffPersonDao.findAll()).thenReturn(staffPersons);

    prepStmt = mock(PreparedStatement.class);
    when(prepStmt.executeQuery()).thenReturn(rs);

    final Timestamp ts = new Timestamp(new Date().getTime());
    when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(rs.getString("CLT_ADPTN_STCD")).thenReturn("Y");

    when(rs.getString("CLIENT_ID")).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString("CLIENT_FIRST_NM")).thenReturn("Donald");
    when(rs.getString("CLIENT_LAST_NM")).thenReturn("Trump");
    when(rs.getString("CLIENT_SENSITIVITY_IND")).thenReturn("R");
    when(rs.getTimestamp("CLIENT_LAST_UPDATED")).thenReturn(ts);
    when(rs.getString("CLIENT_OPERATION")).thenReturn("U");
    when(rs.getTimestamp("CLIENT_LOGMARKER")).thenReturn(ts);

    target = new CaseRocket(dao, esDao, clientDao, staffPersonDao, lastRunFile, mapper, flightPlan);
  }

  @Test
  public void type() throws Exception {
    assertThat(CaseRocket.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void useTransformThread_Args__() throws Exception {
    final boolean actual = target.useTransformThread();
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPrepLastChangeSQL_Args__() throws Exception {
    final String actual = target.getPrepLastChangeSQL();
    final String expected =
        "INSERT INTO GT_ID (IDENTIFIER) \nWITH DRIVER AS ( \n SELECT DISTINCT CAS1.FKCHLD_CLT AS IDENTIFIER \n FROM  CASE_T CAS1  \n WHERE CAS1.IBMSNAP_LOGMARKER > '2018-12-31 03:21:12.000' \nUNION SELECT DISTINCT CCL2.FKCLIENT_T AS IDENTIFIER  \n FROM CASE_T CAS2 \n JOIN CHLD_CLT CCL2 ON CCL2.FKCLIENT_T = CAS2.FKCHLD_CLT   \n JOIN CLIENT_T CLC2 ON CLC2.IDENTIFIER = CCL2.FKCLIENT_T  \n WHERE CCL2.IBMSNAP_LOGMARKER > '2018-12-31 03:21:12.000'  \nUNION SELECT DISTINCT CAS3.FKCHLD_CLT AS IDENTIFIER  \n FROM CASE_T CAS3  \n JOIN CLIENT_T CLC3 ON CLC3.IDENTIFIER = CAS3.FKCHLD_CLT \n WHERE CLC3.IBMSNAP_LOGMARKER > '2018-12-31 03:21:12.000' \nUNION SELECT DISTINCT CAS4.FKCHLD_CLT AS IDENTIFIER  \n FROM CASE_T CAS4  \n JOIN CLN_RELT CLR4  ON CLR4.FKCLIENT_T = CAS4.FKCHLD_CLT \n WHERE CLR4.IBMSNAP_LOGMARKER > '2018-12-31 03:21:12.000' \nUNION SELECT DISTINCT CLR5.FKCLIENT_0 AS IDENTIFIER  \n FROM CASE_T CAS5 \n JOIN CLN_RELT CLR5 ON CLR5.FKCLIENT_T = CAS5.FKCHLD_CLT \n JOIN CLIENT_T CLP5 ON CLP5.IDENTIFIER = CLR5.FKCLIENT_0  \n WHERE CLP5.IBMSNAP_LOGMARKER > '2018-12-31 03:21:12.000' \nUNION SELECT DISTINCT CLR6.FKCLIENT_T AS IDENTIFIER  \n FROM CASE_T CAS6 \n JOIN CLN_RELT CLR6 ON CLR6.FKCLIENT_T = CAS6.FKCHLD_CLT \n JOIN CLIENT_T CLP6 ON CLP6.IDENTIFIER = CLR6.FKCLIENT_0  \n WHERE CLP6.IBMSNAP_LOGMARKER > '2018-12-31 03:21:12.000' \n) \nSELECT DISTINCT CAS1.FKCHLD_CLT AS CLIENT_ID \nFROM DRIVER d1 \nJOIN CASE_T CAS1   ON CAS1.FKCHLD_CLT = d1.IDENTIFIER \nUNION SELECT DISTINCT REL2.FKCLIENT_0  AS CLIENT_ID \nFROM DRIVER d2 \nJOIN CLN_RELT REL2 ON REL2.FKCLIENT_T = d2.IDENTIFIER \nJOIN CASE_T   CAS2 ON CAS2.FKCHLD_CLT = REL2.FKCLIENT_0 \nUNION SELECT DISTINCT REL3.FKCLIENT_T  AS CLIENT_ID \nFROM DRIVER d3 \nJOIN CLN_RELT REL3 ON REL3.FKCLIENT_0 = d3.IDENTIFIER \nJOIN CASE_T   CAS3 ON CAS3.FKCHLD_CLT = REL3.FKCLIENT_T  \nUNION SELECT DISTINCT REL4.FKCLIENT_T  AS CLIENT_ID \nFROM DRIVER d4 \nJOIN CLN_RELT REL4 ON REL4.FKCLIENT_T = d4.IDENTIFIER \nJOIN CASE_T   CAS4 ON CAS4.FKCHLD_CLT = REL4.FKCLIENT_0 \nUNION SELECT DISTINCT REL5.FKCLIENT_0  AS CLIENT_ID \nFROM DRIVER d5 \nJOIN CLN_RELT REL5 ON REL5.FKCLIENT_0 = d5.IDENTIFIER \nJOIN CASE_T   CAS5 ON CAS5.FKCHLD_CLT = REL5.FKCLIENT_T ";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadViewName_Args__() throws Exception {
    final String actual = target.getInitialLoadViewName();
    final String expected = "VW_MQT_REFRL_ONLY";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isInitialLoadJdbc_Args__() throws Exception {
    final boolean actual = target.isInitialLoadJdbc();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPartitionRanges_Args__() throws Exception {
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    final List<Pair<String, String>> expected = new ArrayList<>();
    expected.add(pair);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getOptionalElementName_Args__() throws Exception {
    final String actual = target.getOptionalElementName();
    final String expected = "cases";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void mustDeleteLimitedAccessRecords_Args__() throws Exception {
    final boolean actual = target.mustDeleteLimitedAccessRecords();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJdbcOrderBy_Args__() throws Exception {
    final String actual = target.getJdbcOrderBy();
    final String expected = "";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadQuery_Args__String() throws Exception {
    final String dbSchemaName = "CWSRS1";
    final String actual = target.getInitialLoadQuery(dbSchemaName);
    final String expected =
        "WITH DRIVER AS (\n SELECT     \n       c.IDENTIFIER        AS THIS_CLIENT_ID \n     , TRIM(c.COM_FST_NM)  AS THIS_CLIENT_FIRST_NM \n     , TRIM(c.COM_LST_NM)  AS THIS_CLIENT_LAST_NM \n     , c.SENSTV_IND        AS THIS_CLIENT_SENSITIVITY_IND \n     , c.LST_UPD_TS        AS THIS_CLIENT_LAST_UPDATED \n     , c.IBMSNAP_LOGMARKER AS THIS_CLIENT_LOGMARKER \n     , c.IBMSNAP_OPERATION AS THIS_CLIENT_OPERATION \n FROM GT_ID GT \n JOIN CLIENT_T C ON C.IDENTIFIER = GT.IDENTIFIER \n) \n SELECT   \n CAS1.IDENTIFIER      AS CASE_ID, \n CAS1.FKCHLD_CLT      AS FOCUS_CHILD_ID, \n DRV1.THIS_CLIENT_ID  AS THIS_CLIENT_ID, \n 1                    AS STANZA, \n 0                    AS REL_FOCUS_TO_OTHER, \n 0                    AS REL_OTHER_TO_FOCUS, \n CAS1.CASE_NM         AS CASE_NAME, \n CAS1.START_DT        AS START_DATE, \n CAS1.END_DT          AS END_DATE, \n CAS1.SRV_CMPC        AS SERVICE_COMP, \n CAS1.CLS_RSNC        AS CLOSE_REASON_CODE, \n CAS1.FKSTFPERST      AS WORKER_ID, \n CAS1.LMT_ACSSCD      AS LIMITED_ACCESS_CODE, \n CAS1.LMT_ACS_DT      AS LIMITED_ACCESS_DATE, \n CAS1.LMT_ACSDSC      AS LIMITED_ACCESS_DESCRIPTION, \n CAS1.L_GVR_ENTC      AS LIMITED_ACCESS_GOVERNMENT_ENT, \n CAS1.LST_UPD_TS      AS CASE_LAST_UPDATED, \n CAS1.GVR_ENTC        AS COUNTY, \n CAS1.APV_STC \nFROM DRIVER DRV1 \nJOIN CASE_T CAS1 ON CAS1.FKCHLD_CLT = DRV1.THIS_CLIENT_ID \nWHERE CAS1.IBMSNAP_OPERATION IN ('I','U') \nUNION ALL \nSELECT     \n CAS2.IDENTIFIER      AS CASE_ID, \n CAS2.FKCHLD_CLT      AS FOCUS_CHILD_ID, \n DRV2.THIS_CLIENT_ID  AS THIS_CLIENT_ID, \n 2                    AS STANZA, \n REL2.CLNTRELC        AS REL_FOCUS_TO_OTHER, \n 0                    AS REL_OTHER_TO_FOCUS, \n CAS2.CASE_NM         AS CASE_NAME, \n CAS2.START_DT        AS START_DATE, \n CAS2.END_DT          AS END_DATE, \n CAS2.SRV_CMPC        AS SERVICE_COMP, \n CAS2.CLS_RSNC        AS CLOSE_REASON_CODE, \n CAS2.FKSTFPERST      AS WORKER_ID, \n CAS2.LMT_ACSSCD      AS LIMITED_ACCESS_CODE, \n CAS2.LMT_ACS_DT      AS LIMITED_ACCESS_DATE, \n CAS2.LMT_ACSDSC      AS LIMITED_ACCESS_DESCRIPTION, \n CAS2.L_GVR_ENTC      AS LIMITED_ACCESS_GOVERNMENT_ENT, \n CAS2.LST_UPD_TS      AS CASE_LAST_UPDATED, \n CAS2.GVR_ENTC        AS COUNTY, \n CAS2.APV_STC \nFROM DRIVER DRV2 \nJOIN CLN_RELT REL2 ON REL2.FKCLIENT_T = DRV2.THIS_CLIENT_ID \nJOIN CASE_T   CAS2 ON CAS2.FKCHLD_CLT = REL2.FKCLIENT_0 \nWHERE CAS2.IBMSNAP_OPERATION IN ('I','U') \n  AND REL2.IBMSNAP_OPERATION IN ('I','U') \nUNION ALL \nSELECT  \n CAS3.IDENTIFIER      AS CASE_ID, \n CAS3.FKCHLD_CLT      AS FOCUS_CHILD_ID, \n DRV3.THIS_CLIENT_ID  AS THIS_CLIENT_ID, \n 3                    AS STANZA, \n 0                    AS REL_FOCUS_TO_OTHER, \n REL3.CLNTRELC        AS REL_OTHER_TO_FOCUS, \n CAS3.CASE_NM         AS CASE_NAME, \n CAS3.START_DT        AS START_DATE, \n CAS3.END_DT          AS END_DATE, \n CAS3.SRV_CMPC        AS SERVICE_COMP, \n CAS3.CLS_RSNC        AS CLOSE_REASON_CODE, \n CAS3.FKSTFPERST      AS WORKER_ID, \n CAS3.LMT_ACSSCD      AS LIMITED_ACCESS_CODE, \n CAS3.LMT_ACS_DT      AS LIMITED_ACCESS_DATE, \n CAS3.LMT_ACSDSC      AS LIMITED_ACCESS_DESCRIPTION, \n CAS3.L_GVR_ENTC      AS LIMITED_ACCESS_GOVERNMENT_ENT, \n CAS3.LST_UPD_TS      AS CASE_LAST_UPDATED, \n CAS3.GVR_ENTC        AS COUNTY, \n CAS3.APV_STC \nFROM DRIVER DRV3, CLN_RELT REL3, CASE_T CAS3 \nWHERE CAS3.FKCHLD_CLT = REL3.FKCLIENT_T AND REL3.FKCLIENT_0 = DRV3.THIS_CLIENT_ID \n  AND CAS3.IBMSNAP_OPERATION IN ('I','U') \n  AND REL3.IBMSNAP_OPERATION IN ('I','U') \n FOR READ ONLY WITH UR  WHERE CAS.LMT_ACSSCD = 'N'";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getDenormalizedClass_Args__() throws Exception {
    final Object actual = target.getDenormalizedClass();
    final Object expected = EsPersonCase.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_Args__List() throws Exception {
    final List<EsCaseRelatedPerson> recs = new ArrayList<EsCaseRelatedPerson>();
    final List<ReplicatedPersonCases> actual = target.normalize(recs);
    final List<ReplicatedPersonCases> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void readStaffWorkers_Args__() throws Exception {
  // Map<String, StaffPerson> actual = target.readStaffWorkers();
  // Map<String, StaffPerson> expected = null;
  // assertThat(actual, is(equalTo(expected)));
  // }

  @Test(expected = NeutronCheckedException.class)
  public void readStaffWorkers_Args___T__NeutronException() throws Exception {
    target = new CaseRocket(dao, esDao, clientDao, null, lastRunFile, mapper, flightPlan);
    target.readStaffWorkers();
  }

  @Test
  public void extract_Args__ResultSet() throws Exception {
    final EsCaseRelatedPerson actual = target.extract(rs);
    final EsCaseRelatedPerson expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void pullNextRange_Args__Pair() throws Exception {
  // final PreparedStatement stmtInsClient = mock(PreparedStatement.class);
  // final PreparedStatement stmtSelClient = mock(PreparedStatement.class);
  // final PreparedStatement stmtSelReferral = mock(PreparedStatement.class);
  // final PreparedStatement stmtSelAllegation = mock(PreparedStatement.class);
  // final ResultSet rsInsClient = mock(ResultSet.class);
  // final ResultSet rsSelClient = mock(ResultSet.class);
  // final ResultSet rsSelReferral = mock(ResultSet.class);
  // final ResultSet rsSelAllegation = mock(ResultSet.class);
  //
  // final String sqlAffectedClients = CaseSQLResource.PREP_AFFECTED_CLIENTS_FULL;
  // final String sqlClient = CaseSQLResource.SELECT_CLIENT_FULL;
  // final String sqlCase = CaseSQLResource.SELECT_CASE_DETAIL;
  // final String sqlRelation = CaseSQLResource.SELECT_CLIENT_CASE_RELATIONSHIP;
  //
  // when(con.prepareStatement(sqlAffectedClients)).thenReturn(stmtInsClient);
  // when(con.prepareStatement(sqlClient)).thenReturn(stmtSelClient);
  // when(con.prepareStatement(sqlCase)).thenReturn(stmtSelReferral);
  // when(con.prepareStatement(sqlRelation)).thenReturn(stmtSelAllegation);
  //
  // when(stmtInsClient.executeQuery()).thenReturn(rsInsClient);
  // when(stmtSelClient.executeQuery()).thenReturn(rsSelClient);
  // when(stmtSelReferral.executeQuery()).thenReturn(rsSelReferral);
  // when(stmtSelAllegation.executeQuery()).thenReturn(rsSelAllegation);
  // when(rsInsClient.next()).thenReturn(true).thenReturn(false);
  // when(rsSelClient.next()).thenReturn(true).thenReturn(false);
  // when(rsSelReferral.next()).thenReturn(false);
  // when(rsSelAllegation.next()).thenReturn(false);
  //
  // when(rsSelClient.getString("FKCLIENT_T")).thenReturn(DEFAULT_CLIENT_ID);
  // when(rsSelClient.getString("SENSTV_IND")).thenReturn("N");
  //
  // int actual = target.pullNextRange(pair);
  // int expected = 0;
  // assertThat(actual, is(equalTo(expected)));
  // }

  @Test(expected = NeutronCheckedException.class)
  public void pullNextRange_Args__Pair_T__NeutronException() throws Exception {
    final Pair<String, String> p = pair;
    target.pullNextRange(p);
  }

  // @Test
  // public void threadRetrieveByJdbc_Args__() throws Exception {
  // target.threadRetrieveByJdbc();
  // }

  @Test
  public void getClientDao_Args__() throws Exception {
    final ReplicatedClientDao actual = target.getClientDao();
    final ReplicatedClientDao expected = this.clientDao;
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void fetchLastRunResults_Args__Date__Set() throws Exception {
  // Date lastRunDt = mock(Date.class);
  // Set<String> deletionResults = mock(Set.class);
  // List<ReplicatedPersonCases> actual = target.fetchLastRunResults(lastRunDt, deletionResults);
  // List<ReplicatedPersonCases> expected = new ArrayList<>();
  // assertThat(actual, is(equalTo(expected)));
  // }

  // @Test
  // public void main_Args__StringArray() throws Exception {
  // String[] args = new String[] {};
  // CaseRocket.main(args);
  // }

  @Test
  public void test_useTransformThread_A$() throws Exception {
    final boolean actual = target.useTransformThread();
    final boolean expected = false;
    assertEquals(expected, actual);
  }

  @Test
  public void test_getPrepLastChangeSQL_A$() throws Exception {
    final String actual = target.getPrepLastChangeSQL();
    final boolean sizeGood = actual.length() > 200;
    assertEquals(sizeGood, true);
  }

  @Test
  public void test_getInitialLoadViewName_A$() throws Exception {
    final String actual = target.getInitialLoadViewName();
    final String expected = "VW_MQT_REFRL_ONLY";
    assertEquals(expected, actual);
  }

  @Test
  public void test_isInitialLoadJdbc_A$() throws Exception {
    final boolean actual = target.isInitialLoadJdbc();
    final boolean expected = true;
    assertEquals(expected, actual);
  }

  @Test
  public void test_getPartitionRanges_A$() throws Exception {
    final List<Pair<String, String>> actual = target.getPartitionRanges();
    final List<Pair<String, String>> expected = new ArrayList<>();
    expected.add(pair);
    assertEquals(expected, actual);
  }

  @Test
  public void test_getOptionalElementName_A$() throws Exception {
    final String actual = target.getOptionalElementName();
    String expected = "cases";
    assertEquals(expected, actual);
  }

  @Test
  public void test_mustDeleteLimitedAccessRecords_A$() throws Exception {
    boolean actual = target.mustDeleteLimitedAccessRecords();
    boolean expected = true;
    assertEquals(expected, actual);
  }

  @Test
  public void test_getJdbcOrderBy_A$() throws Exception {
    final String actual = target.getJdbcOrderBy();
    final String expected = "";
    assertEquals(expected, actual);
  }

  @Test
  public void test_getInitialLoadQuery_A$String() throws Exception {
    final String dbSchemaName = "CWSRS1";
    final String actual = target.getInitialLoadQuery(dbSchemaName);
    final boolean sizeGood = actual.length() > 200;
    assertEquals(sizeGood, true);
  }

  @Test
  public void test_prepareUpsertRequest_A$ElasticSearchPerson$ReplicatedPersonCases()
      throws Exception {
    final ElasticSearchPerson esp = new ElasticSearchPerson();
    final ReplicatedPersonCases p = new ReplicatedPersonCases(DEFAULT_CLIENT_ID);
    final UpdateRequest actual = target.prepareUpsertRequest(esp, p);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void test_prepareUpsertRequest_A$ElasticSearchPerson$ReplicatedPersonCases_T$NeutronException()
      throws Exception {
    final ElasticSearchPerson esp = null;
    final ReplicatedPersonCases p = new ReplicatedPersonCases(DEFAULT_CLIENT_ID);
    try {
      target.prepareUpsertRequest(esp, p);
      fail("Expected exception was not thrown!");
    } catch (NeutronCheckedException e) {
    }
  }

  @Test
  public void test_getDenormalizedClass_A$() throws Exception {
    final Object actual = target.getDenormalizedClass();
    final Object expected = EsPersonCase.class;
    assertEquals(expected, actual);
  }

  @Test
  public void test_normalize_A$List() throws Exception {
    final List<EsCaseRelatedPerson> recs = new ArrayList<EsCaseRelatedPerson>();
    final List<ReplicatedPersonCases> actual = target.normalize(recs);
    final List<ReplicatedPersonCases> expected = new ArrayList<>();
    assertEquals(expected, actual);
  }

  @Test
  public void test_prepAffectedClients_A$PreparedStatement$PreparedStatement$Pair()
      throws Exception {
    final PreparedStatement stmtInsClient = prepStmt;
    final PreparedStatement stmtInsClientCase = prepStmt;
    target.prepAffectedClients(stmtInsClient, stmtInsClientCase, pair);
  }

  @Test
  public void test_prepAffectedClients_A$PreparedStatement$PreparedStatement$Pair_T$SQLException()
      throws Exception {
    final PreparedStatement stmtInsClient = prepStmt;
    final PreparedStatement stmtInsClientCase = prepStmt;

    doThrow(new SQLException("uh oh")).when(stmtInsClient).setString(any(Integer.class),
        any(String.class));
    try {
      target.prepAffectedClients(stmtInsClient, stmtInsClientCase, pair);
      fail("Expected exception was not thrown!");
    } catch (SQLException e) {
    }
  }

  @Test
  public void test_readCaseClients_A$PreparedStatement$List() throws Exception {
    final List list = new ArrayList();
    target.readCaseClients(prepStmt, list);
  }

  @Test
  public void test_readCaseClients_A$PreparedStatement$List_T$SQLException() throws Exception {
    final List list = new ArrayList();
    try {
      when(rs.getString(any(String.class))).thenThrow(SQLException.class);
      target.readCaseClients(prepStmt, list);
      fail("Expected exception was not thrown!");
    } catch (SQLException e) {
    }
  }

  @Test
  public void test_readFocusChildParents_A$PreparedStatement$List() throws Exception {
    final List<FocusChildParent> list = new ArrayList<FocusChildParent>();
    target.readFocusChildParents(prepStmt, list);
  }

  @Test
  public void test_readFocusChildParents_A$PreparedStatement$List_T$SQLException()
      throws Exception {
    final List<FocusChildParent> list = new ArrayList<FocusChildParent>();
    try {
      doThrow(new SQLException("uh oh")).when(rs).getString(any(String.class));
      target.readFocusChildParents(prepStmt, list);
      fail("Expected exception was not thrown!");
    } catch (SQLException e) {
    }
  }

  @Test
  public void test_readCases_A$PreparedStatement$Map() throws Exception {
    final Map<String, EsCaseRelatedPerson> mapCases = new HashMap<String, EsCaseRelatedPerson>();
    target.readCases(prepStmt, mapCases);
  }

  @Test
  public void test_readCases_A$PreparedStatement$Map_T$SQLException() throws Exception {
    final Map<String, EsCaseRelatedPerson> mapCases = new HashMap<String, EsCaseRelatedPerson>();
    try {
      doThrow(new SQLException("uh oh")).when(rs).getString(any(String.class));
      target.readCases(prepStmt, mapCases);
      fail("Expected exception was not thrown!");
    } catch (SQLException e) {
    }
  }

  @Test
  public void test_readStaffWorkers_A$() throws Exception {
    final Map<String, StaffPerson> actual = target.readStaffWorkers();
    final Map<String, StaffPerson> expected = new HashMap<>();
    expected.put(null, new StaffPerson());
    assertEquals(expected, actual);
  }

  @Test
  public void test_readStaffWorkers_A$_T$NeutronException() throws Exception {
    try {
      target = new CaseRocket(dao, esDao, clientDao, null, lastRunFile, mapper, flightPlan);
      target.readStaffWorkers();
      fail("Expected exception was not thrown!");
    } catch (NeutronCheckedException e) {
    }
  }

  @Test
  public void test_extract_A$ResultSet() throws Exception {
    final EsCaseRelatedPerson actual = target.extract(rs);
    final EsCaseRelatedPerson expected = null;
    assertEquals(expected, actual);
  }

  @Test
  public void test_extractClient_A$ResultSet() throws Exception {
    final ReplicatedClient actual = target.extractClient(rs);
    final ReplicatedClient expected = null;
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void test_extractClient_A$ResultSet_T$SQLException() throws Exception {
    when(rs.getString(any(String.class))).thenThrow(SQLException.class);
    try {
      target.extractClient(rs);
      fail("Expected exception was not thrown!");
    } catch (SQLException | IllegalArgumentException e) {
    }
  }

  @Test
  public void test_extractCase_A$ResultSet() throws Exception {
    final EsCaseRelatedPerson actual = target.extractCase(rs);

    // EsCaseRelatedPerson expected = new EsCaseRelatedPerson();
    // expected.setCaseId(DEFAULT_CLIENT_ID);
    // expected.setFocusChildId(DEFAULT_CLIENT_ID);
    // expected.setStartDate(null);
    // expected.setEndDate(null);
    // expected.setAccessLimitation(new EmbeddableAccessLimitation());
    // assertEquals(expected, actual);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void test_extractCase_A$ResultSet_T$SQLException() throws Exception {
    try {
      when(rs.getString(any(String.class))).thenThrow(SQLException.class);
      target.extractCase(rs);
      fail("Expected exception was not thrown!");
    } catch (SQLException e) {
    }
  }

  @Test
  public void test_readClients_A$PreparedStatement$Map() throws Exception {
    final PreparedStatement stmtSelClient = prepStmt;
    final Map<String, ReplicatedClient> mapClients = new HashMap<String, ReplicatedClient>();
    final Map<String, ReplicatedClient> actual = target.readClients(stmtSelClient, mapClients);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void test_readClients_A$PreparedStatement$Map_T$NeutronException() throws Exception {
    final PreparedStatement stmtSelClient = prepStmt;
    final Map<String, ReplicatedClient> mapClients = new HashMap<String, ReplicatedClient>();
    try {
      doThrow(new SQLException("uh oh")).when(rs).getString(any(String.class));
      target.readClients(stmtSelClient, mapClients);
      fail("Expected exception was not thrown!");
    } catch (NeutronCheckedException e) {
    }
  }

  @Test
  public void test_collectCaseClients_A$Map$Pair() throws Exception {
    final Map mapCaseClients = new HashMap();
    target.collectCaseClients(mapCaseClients, pair);
  }

  @Test
  public void test_collectThisClientCase_A$Map$String$String() throws Exception {
    final Map mapClientCases = new HashMap();
    String caseId = null;
    String clientId = null;
    target.collectThisClientCase(mapClientCases, caseId, clientId);
  }

  @Test
  public void test_collectClientCases_A$Map$Pair() throws Exception {
    final Map mapClientCases = new HashMap();
    target.collectClientCases(mapClientCases, pair);
  }

  @Test
  public void test_collectFocusChildParents_A$Map$FocusChildParent() throws Exception {
    final Map mapFocusChildParents = new HashMap();
    FocusChildParent rel = new FocusChildParent(1, DEFAULT_CLIENT_ID, "987654321abc", (short) 247,
        "Fred", "Meyer", "N");
    target.collectFocusChildParents(mapFocusChildParents, rel);
  }

  @Test
  public void test_addFocusChildren_A$Map$Map() throws Exception {
    final Map<String, EsCaseRelatedPerson> mapCases = new HashMap<String, EsCaseRelatedPerson>();
    final Map<String, ReplicatedClient> mapClients = new HashMap<String, ReplicatedClient>();
    target.addFocusChildren(mapCases, mapClients);
  }

  @Test
  public void test_reduceCase_A$ReplicatedPersonCases$EsCaseRelatedPerson$Map$Map()
      throws Exception {
    ReplicatedPersonCases cases = null;
    EsCaseRelatedPerson rawCase = null;
    final Map<String, ReplicatedClient> mapClients = new HashMap<String, ReplicatedClient>();
    Map mapFocusChildParents = new HashMap();
    target.reduceCase(cases, rawCase, mapClients, mapFocusChildParents);
  }

  @Test
  public void test_reduceClientCases_A$String$Map$Map$Map$Map() throws Exception {
    final String clientId = DEFAULT_CLIENT_ID;
    final Map<String, ReplicatedClient> mapClients = new HashMap<String, ReplicatedClient>();
    final ReplicatedClient repClient = new ReplicatedClient();
    repClient.setId(clientId);
    mapClients.put(clientId, repClient);

    final Map<String, EsCaseRelatedPerson> mapCases = new HashMap<String, EsCaseRelatedPerson>();
    final Map<String, Set<String>> mapClientCases = new HashMap<>();
    final Set<String> set = new HashSet<>();
    set.add("987654321xyz");
    mapClientCases.put(DEFAULT_CLIENT_ID, set);

    final Map mapFocusChildParents = new HashMap();
    final ReplicatedPersonCases actual = target.reduceClientCases(clientId, mapClients, mapCases,
        mapClientCases, mapFocusChildParents);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void test_assemblePieces_A$List$List$Map$Map$Map() throws Exception {
    final List<FocusChildParent> listFocusChildParents = new ArrayList<FocusChildParent>();
    final List listCaseClients = new ArrayList();
    final Map<String, EsCaseRelatedPerson> mapCases = new HashMap<String, EsCaseRelatedPerson>();
    final Map<String, ReplicatedClient> mapClients = new HashMap<String, ReplicatedClient>();
    final Map mapClientCases = new HashMap();
    final int actual = target.assemblePieces(listFocusChildParents, listCaseClients, mapCases,
        mapClients, mapClientCases);
    final int expected = 0;
    assertEquals(expected, actual);
  }

  @Test
  public void test_verify_A$Map() throws Exception {
    Map<String, ReplicatedPersonCases> mapReadyClientCases =
        new HashMap<String, ReplicatedPersonCases>();
    mapReadyClientCases.put(DEFAULT_CLIENT_ID, new ReplicatedPersonCases(DEFAULT_CLIENT_ID));
    boolean actual = target.verify(mapReadyClientCases);
    boolean expected = true;
    assertEquals(expected, actual);
  }

  @Test
  public void test_verify_A$Map_T$NeutronException() throws Exception {
    final Map<String, ReplicatedPersonCases> mapReadyClientCases = mock(Map.class);
    when(mapReadyClientCases.get(any(String.class))).thenThrow(IOException.class);
    try {
      target.verify(mapReadyClientCases);
      fail("Expected exception was not thrown!");
    } catch (NeutronCheckedException e) {
    }
  }

  // @Test
  // public void test_pullNextRange_A$Pair() throws Exception {
  // final int actual = target.pullNextRange(pair);
  // final int expected = 1;
  // assertEquals(expected, actual);
  // }

  @Test
  public void test_pullNextRange_A$Pair_T$NeutronException() throws Exception {
    try {
      target.pullNextRange(pair);
      fail("Expected exception was not thrown!");
    } catch (NeutronCheckedException e) {
    }
  }

  // @Test
  // public void test_runMultiThreadIndexing_A$() throws Exception {
  // target.runMultiThreadIndexing();
  // }

  // @Test
  // public void test_fetchLastRunResults_A$Date$Set() throws Exception {
  // Date lastRunDate = new Date();
  // Set<String> deletionResults = new HashSet<>();
  // List<ReplicatedPersonCases> actual = target.fetchLastRunResults(lastRunDate, deletionResults);
  // List<ReplicatedPersonCases> expected = null;
  // assertEquals(expected, actual);
  // }

  // @Test
  // public void test_threadRetrieveByJdbc_A$() throws Exception {
  // target.threadRetrieveByJdbc();
  // }

  @Test
  public void test_getClientDao_A$() throws Exception {
    final ReplicatedClientDao actual = target.getClientDao();
    assertThat(actual, is(notNullValue()));
  }

}
