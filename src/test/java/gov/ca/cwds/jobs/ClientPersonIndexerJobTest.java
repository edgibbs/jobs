package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPerson.ESOptionalCollection;
import gov.ca.cwds.data.persistence.cms.EsClientPerson;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;

public class ClientPersonIndexerJobTest extends Goddard<ReplicatedClient, EsClientPerson> {

  ReplicatedClientDao dao;
  ClientPersonIndexerJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    when(rs.next()).thenReturn(true, true, false);
    dao = new ReplicatedClientDao(sessionFactory);
    target =
        new ClientPersonIndexerJob(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
    target.allocateThreadHandler();
  }

  @Test
  public void type() throws Exception {
    assertThat(ClientPersonIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void useTransformThread_A$() throws Exception {
    final boolean actual = target.useTransformThread();
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPrepLastChangeSQL_A$() throws Exception {
    // final Date date = target.determineLastSuccessfulRunTime();
    // NeutronDateTimeFormat.LAST_RUN_DATE_FORMAT.formatter().format(datetime);
    // target.writeLastSuccessfulRunTime(datetime);
    // target.getFlightPlan().setOverrideLastRunTime(lastRunTime);

    // NOTE: the date is **dynamic** (current date/time).
    final String actual = target.getPrepLastChangeSQL();
    // final String expected =
    // "\"INSERT INTO GT_ID (IDENTIFIER)\\nSELECT DISTINCT CLT.IDENTIFIER \\nFROM CLIENT_T clt
    // \\nWHERE CLT.IBMSNAP_LOGMARKER > '2018-12-31 03:21:12.000' \\nUNION SELECT DISTINCT
    // cla.FKCLIENT_T AS IDENTIFIER \\nFROM CL_ADDRT cla \\nWHERE CLA.IBMSNAP_LOGMARKER >
    // '2018-12-31 03:21:12.000' \\nUNION SELECT DISTINCT cla.FKCLIENT_T AS IDENTIFIER \\nFROM
    // CL_ADDRT cla \\nJOIN ADDRS_T adr ON cla.FKADDRS_T = adr.IDENTIFIER \\nWHERE
    // ADR.IBMSNAP_LOGMARKER > '2018-12-31 03:21:12.000' \\nUNION SELECT DISTINCT eth.ESTBLSH_ID AS
    // IDENTIFIER \\nFROM CLSCP_ET eth \\nWHERE ETH.ESTBLSH_CD = 'C' \\nAND ETH.IBMSNAP_LOGMARKER >
    // '2018-12-31 03:21:12.000' \"";
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void extract_A$ResultSet() throws Exception {
    final EsClientPerson actual = target.extract(rs);
    // EsClientPerson expected = null;
    // assertThat(actual, is(equalTo(expected)));
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = SQLException.class)
  public void extract_A$ResultSet_T$SQLException() throws Exception {
    when(rs.getString(any(String.class))).thenThrow(SQLException.class);
    target.extract(rs);
  }

  @Test
  public void getDenormalizedClass_A$() throws Exception {
    final Object actual = target.getDenormalizedClass();
    final Object expected = EsClientPerson.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadViewName_A$() throws Exception {
    final String actual = target.getInitialLoadViewName();
    final String expected = "MQT_CLIENT_ADDRESS";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getMQTName_A$() throws Exception {
    final String actual = target.getMQTName();
    final String expected = "MQT_CLIENT_ADDRESS";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJdbcOrderBy_A$() throws Exception {
    final String actual = target.getJdbcOrderBy();
    final String expected = " ORDER BY X.CLT_IDENTIFIER ";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadQuery_A$String() throws Exception {
    final String dbSchemaName = "CWSRS4";
    final String actual = target.getInitialLoadQuery(dbSchemaName).replaceAll("  ", " ");
    final String expected =
        "SELECT x.CLT_IDENTIFIER,\n x.CLT_LST_UPD_ID,\n x.CLT_LST_UPD_TS,\n x.CLT_ADJDEL_IND,\n x.CLT_ADPTN_STCD,\n "
            + "x.CLT_ALN_REG_NO,\n x.CLT_BIRTH_CITY,\n x.CLT_B_CNTRY_C,\n x.CLT_BIRTH_DT,\n x.CLT_BR_FAC_NM,\n "
            + "x.CLT_B_STATE_C,\n x.CLT_BP_VER_IND,\n x.CLT_CHLD_CLT_B,\n x.CLT_CL_INDX_NO,\n x.CLT_COMMNT_DSC,\n "
            + "x.CLT_COM_FST_NM,\n x.CLT_COM_LST_NM,\n x.CLT_COM_MID_NM,\n x.CLT_CONF_ACTDT,\n x.CLT_CONF_EFIND,\n "
            + "x.CLT_CREATN_DT,\n x.CLT_CURRCA_IND,\n x.CLT_COTH_DESC,\n x.CLT_CURREG_IND,\n x.CLT_DEATH_DT,\n "
            + "x.CLT_DTH_DT_IND,\n x.CLT_DEATH_PLC,\n x.CLT_DTH_RN_TXT,\n x.CLT_DRV_LIC_NO,\n x.CLT_D_STATE_C,\n "
            + "x.CLT_EMAIL_ADDR,\n x.CLT_EST_DOB_CD,\n x.CLT_ETH_UD_CD,\n x.CLT_FTERM_DT,\n x.CLT_GENDER_CD,\n "
            + "x.CLT_HEALTH_TXT,\n x.CLT_HISP_UD_CD,\n x.CLT_HISP_CD,\n x.CLT_I_CNTRY_C,\n x.CLT_IMGT_STC,\n "
            + "x.CLT_INCAPC_CD,\n x.CLT_HCARE_IND,\n x.CLT_LIMIT_IND,\n x.CLT_LITRATE_CD,\n x.CLT_MAR_HIST_B,\n "
            + "x.CLT_MRTL_STC,\n x.CLT_MILT_STACD,\n x.CLT_MTERM_DT,\n x.CLT_NMPRFX_DSC,\n x.CLT_NAME_TPC,\n "
            + "x.CLT_OUTWRT_IND,\n x.CLT_PREVCA_IND,\n x.CLT_POTH_DESC,\n x.CLT_PREREG_IND,\n x.CLT_P_ETHNCTYC,\n "
            + "x.CLT_P_LANG_TPC,\n x.CLT_RLGN_TPC,\n x.CLT_S_LANG_TC,\n x.CLT_SNTV_HLIND,\n x.CLT_SENSTV_IND,\n "
            + "x.CLT_SOCPLC_CD,\n x.CLT_SOC158_IND,\n x.CLT_SSN_CHG_CD,\n x.CLT_SS_NO,\n x.CLT_SUFX_TLDSC,\n "
            + "x.CLT_TRBA_CLT_B,\n x.CLT_TR_MBVRT_B,\n x.CLT_UNEMPLY_CD,\n x.CLT_ZIPPY_IND,\n "
            + "x.CLT_IBMSNAP_LOGMARKER,\n x.CLT_IBMSNAP_OPERATION,\n x.CLA_IDENTIFIER,\n "
            + "x.CLA_LST_UPD_ID,\n x.CLA_LST_UPD_TS,\n x.CLA_ADDR_TPC,\n x.CLA_BK_INMT_ID,\n "
            + "x.CLA_EFF_END_DT,\n x.CLA_EFF_STRTDT,\n x.CLA_FKADDRS_T,\n x.CLA_FKCLIENT_T,\n "
            + "x.CLA_FKREFERL_T,\n x.CLA_HOMLES_IND,\n x.CLA_IBMSNAP_LOGMARKER,\n x.CLA_IBMSNAP_OPERATION,\n "
            + "x.ADR_IDENTIFIER,\n x.ADR_LST_UPD_ID,\n x.ADR_LST_UPD_TS,\n x.ADR_ADDR_DSC,\n x.ADR_CITY_NM,\n "
            + "x.ADR_EMRG_EXTNO,\n x.ADR_EMRG_TELNO,\n x.ADR_FRG_ADRT_B,\n x.ADR_GVR_ENTC,\n x.ADR_HEADER_ADR,\n "
            + "x.ADR_MSG_EXT_NO,\n x.ADR_MSG_TEL_NO,\n x.ADR_POSTDIR_CD,\n x.ADR_PREDIR_CD,\n "
            + "x.ADR_PRM_EXT_NO,\n x.ADR_PRM_TEL_NO,\n x.ADR_STATE_C,\n x.ADR_STREET_NM,\n x.ADR_STREET_NO,\n "
            + "x.ADR_ST_SFX_C,\n x.ADR_UNT_DSGC,\n x.ADR_UNIT_NO,\n x.ADR_ZIP_NO,\n x.ADR_ZIP_SFX_NO,\n "
            + "x.ADR_IBMSNAP_LOGMARKER,\n x.ADR_IBMSNAP_OPERATION,\n x.ETH_IDENTIFIER,\n x.ETHNICITY_CODE,\n "
            + "x.ETH_IBMSNAP_LOGMARKER,\n x.ETH_IBMSNAP_OPERATION,\n x.CLC_CLIENT_ID,\n x.CLC_GVR_ENTC,\n "
            + "x.CLC_LST_UPD_TS,\n x.CLC_LST_UPD_OP,\n x.CLC_CNTY_RULE,\n x.SAL_THIRD_ID,\n x.SAL_ACTV_RNC,\n "
            + "x.SAL_ACTV_DT,\n x.SAL_ACTV_GEC,\n x.SAL_ACTV_TXT,\n x.SAL_DACT_DT,\n x.SAL_DACT_GEC,\n "
            + "x.SAL_DACT_TXT,\n x.SAL_LST_UPD_ID,\n x.SAL_LST_UPD_TS,\n x.SAL_IBMSNAP_LOGMARKER,\n "
            + "x.SAL_IBMSNAP_OPERATION,\n x.ONM_THIRD_ID,\n x.ONM_FIRST_NM,\n x.ONM_LAST_NM,\n x.ONM_MIDDLE_NM,\n "
            + "x.ONM_NMPRFX_DSC,\n x.ONM_NAME_TPC,\n x.ONM_SUFX_TLDSC,\n x.ONM_LST_UPD_ID,\n x.ONM_LST_UPD_TS,\n "
            + "x.ONM_IBMSNAP_OPERATION,\n x.ONM_IBMSNAP_LOGMARKER,\n x.CAS_IDENTIFIER,\n x.CAS_RSP_AGY_CD,\n "
            + "x.CAS_IBMSNAP_OPERATION,\n x.CAS_IBMSNAP_LOGMARKER,\n x.CSH_THIRD_ID,\n x.CSH_CSEC_TPC,\n "
            + "x.CSH_START_DT,\n x.CSH_END_DT,\n x.CSH_LST_UPD_ID,\n x.CSH_LST_UPD_TS,\n x.CSH_IBMSNAP_OPERATION,\n "
            + "x.CSH_IBMSNAP_LOGMARKER,\n x.LAST_CHG " + "FROM CWSRS4.MQT_CLIENT_ADDRESS x "
            + "WHERE X.CLT_IDENTIFIER BETWEEN ':fromId' AND ':toId' AND x.CLT_SENSTV_IND = 'N' "
            + "ORDER BY X.CLT_IDENTIFIER FOR READ ONLY WITH UR ";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void initialLoadProcessRangeResults_A$ResultSet() throws Exception {
    target.handleMainResults(rs, con);
  }

  @Test(expected = SQLException.class)
  public void initialLoadProcessRangeResults_A$ResultSet_T$SQLException() throws Exception {
    when(rs.next()).thenThrow(SQLException.class);
    target.handleMainResults(rs, null);
  }

  @Test
  public void validateAddresses_A$ReplicatedClient$ElasticSearchPerson() throws Exception {
    final ElasticSearchPerson person = new ElasticSearchPerson();
    person.setLastName("Young");
    person.setFirstName("Angus");
    person.setMiddleName("McKinnon");
    person.setId(DEFAULT_CLIENT_ID);

    final ReplicatedClient rep = new ReplicatedClient();
    rep.setCommonLastName("Young");
    rep.setCommonFirstName("Angus");
    rep.setCommonMiddleName("McKinnon");
    rep.setBirthCity("Glasgow");

    dao = mock(ReplicatedClientDao.class);
    when(dao.find(any())).thenReturn(rep);

    final TestClientPersonIndexerJob target = new TestClientPersonIndexerJob(dao, esDao,
        lastRunFile, mapper, sessionFactory, null, flightPlan);
    target.setTxn(transaction);
    final boolean actual = target.validateAddresses(rep, person);
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void validateDocument_A$ElasticSearchPerson() throws Exception {
    final ElasticSearchPerson person = new ElasticSearchPerson();
    person.setId(DEFAULT_CLIENT_ID);
    person.setLastName("Young");
    person.setFirstName("Angus");
    person.setMiddleName("McKinnon");

    final ReplicatedClient rep = new ReplicatedClient();
    rep.setId(DEFAULT_CLIENT_ID);
    rep.setCommonLastName("Young");
    rep.setCommonFirstName("Angus");
    rep.setCommonMiddleName("McKinnon");
    rep.setBirthCity("Glasgow");

    dao = mock(ReplicatedClientDao.class);
    when(dao.find(any())).thenReturn(rep);

    final TestClientPersonIndexerJob target = new TestClientPersonIndexerJob(dao, esDao,
        lastRunFile, mapper, sessionFactory, null, flightPlan);
    target.setTxn(transaction);
    final boolean actual = target.validateDocument(person);
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void validateDocument_A$ElasticSearchPerson__explode() throws Exception {
    final ElasticSearchPerson person = new ElasticSearchPerson();
    person.setId(DEFAULT_CLIENT_ID);
    person.setLastName("Young");
    person.setFirstName("Angus");
    person.setMiddleName("McKinnon");

    final ReplicatedClient rep = new ReplicatedClient();
    rep.setCommonLastName("Young");
    rep.setCommonFirstName("Angus");
    rep.setCommonMiddleName("McKinnon");
    rep.setBirthCity("Glasgow");

    dao = mock(ReplicatedClientDao.class);
    when(dao.find(any())).thenThrow(SQLException.class);

    final TestClientPersonIndexerJob target = new TestClientPersonIndexerJob(dao, esDao,
        lastRunFile, mapper, sessionFactory, null, flightPlan);
    target.setTxn(transaction);

    final boolean actual = target.validateDocument(person);
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  @Ignore
  public void threadRetrieveByJdbc_A$() throws Exception {
    target.threadRetrieveByJdbc();
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
  public void mustDeleteLimitedAccessRecords_A$() throws Exception {
    final boolean actual = target.mustDeleteLimitedAccessRecords();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_A$List() throws Exception {
    final List<EsClientPerson> recs = new ArrayList<EsClientPerson>();
    final List<ReplicatedClient> actual = target.normalize(recs);
    final List<ReplicatedClient> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void nextThreadNumber_A$() throws Exception {
    final int actual = target.nextThreadNumber();
    final int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void keepCollections_A$() throws Exception {
    final ESOptionalCollection[] actual = target.keepCollections();
    final ESOptionalCollection[] expected =
        {ESOptionalCollection.AKA, ESOptionalCollection.SAFETY_ALERT};
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void main_A$StringArray_T$Exception() throws Exception {
    final String[] args = new String[] {};
    ClientPersonIndexerJob.main(args);
  }

}
