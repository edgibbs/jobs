package gov.ca.cwds.neutron.rocket;

import gov.ca.cwds.data.std.ApiMarker;

@SuppressWarnings({"squid:S1192", "findbugs:HSC_HUGE_SHARED_STRING_CONSTANT"})
public class ClientSQLResource implements ApiMarker {

  private static final long serialVersionUID = 1L;

  // =================================
  // Neutron, the next generation.
  // =================================

  public static final String KEY_SOURCE = "FROM GT_ID   gt \n";

  //@formatter:off
  public static final String SEL_OPTIMIZE =
        "OPTIMIZE FOR 1000 ROWS \n"
      + "FOR READ ONLY WITH UR ";
  //@formatter:on

  //@formatter:off
  public static final String INS_CLI_RNG =
        "INSERT INTO GT_ID (IDENTIFIER) \n"
      + "SELECT x.IDENTIFIER \n"
      + "FROM CLIENT_T x \n"
      + "WHERE X.IDENTIFIER BETWEEN ? AND ? \n"
      + "AND x.IBMSNAP_OPERATION IN ('I','U')";
  //@formatter:on

  /**
   * SNAP-754: Launch Command: remove deleted clients from index.
   * 
   * <p>
   * Read deleted clients in order to remove them from the indexes.
   * </p>
   */
  //@formatter:off
  public static final String SEL_CLI =
         "SELECT \n"
      + "    clt.IDENTIFIER        AS CLT_IDENTIFIER, \n"
      + "    clt.LST_UPD_ID        AS CLT_LST_UPD_ID, \n"
      + "    clt.LST_UPD_TS        AS CLT_LST_UPD_TS, \n"
      + "    clt.ADJDEL_IND        AS CLT_ADJDEL_IND, \n"
      + "    clt.ADPTN_STCD        AS CLT_ADPTN_STCD, \n"
      + "    clt.ALN_REG_NO        AS CLT_ALN_REG_NO, \n"
      + "    clt.BIRTH_CITY        AS CLT_BIRTH_CITY, \n"
      + "    clt.B_CNTRY_C         AS CLT_B_CNTRY_C, \n"
      + "    clt.BIRTH_DT          AS CLT_BIRTH_DT, \n"
      + "    clt.BR_FAC_NM         AS CLT_BR_FAC_NM, \n"
      + "    clt.B_STATE_C         AS CLT_B_STATE_C, \n"
      + "    clt.BP_VER_IND        AS CLT_BP_VER_IND, \n"
      + "    clt.CHLD_CLT_B        AS CLT_CHLD_CLT_B, \n"
      + "    clt.CL_INDX_NO        AS CLT_CL_INDX_NO, \n"
      + "    clt.COMMNT_DSC        AS CLT_COMMNT_DSC, \n"
      + "    clt.COM_FST_NM        AS CLT_COM_FST_NM, \n"
      + "    clt.COM_LST_NM        AS CLT_COM_LST_NM, \n"
      + "    clt.COM_MID_NM        AS CLT_COM_MID_NM, \n"
      + "    clt.CONF_ACTDT        AS CLT_CONF_ACTDT, \n"
      + "    clt.CONF_EFIND        AS CLT_CONF_EFIND, \n"
      + "    clt.CREATN_DT         AS CLT_CREATN_DT, \n"
      + "    clt.CURRCA_IND        AS CLT_CURRCA_IND, \n"
      + "    clt.COTH_DESC         AS CLT_COTH_DESC, \n"
      + "    clt.CURREG_IND        AS CLT_CURREG_IND, \n"
      + "    clt.DEATH_DT          AS CLT_DEATH_DT, \n"
      + "    clt.DTH_DT_IND        AS CLT_DTH_DT_IND, \n"
      + "    clt.DEATH_PLC         AS CLT_DEATH_PLC, \n"
      + "    clt.DTH_RN_TXT        AS CLT_DTH_RN_TXT, \n"
      + "    clt.DRV_LIC_NO        AS CLT_DRV_LIC_NO, \n"
      + "    clt.D_STATE_C         AS CLT_D_STATE_C, \n"
      + "    clt.EMAIL_ADDR        AS CLT_EMAIL_ADDR, \n"
      + "    clt.EST_DOB_CD        AS CLT_EST_DOB_CD, \n"
      + "    clt.ETH_UD_CD         AS CLT_ETH_UD_CD, \n"
      + "    clt.FTERM_DT          AS CLT_FTERM_DT, \n"
      + "    clt.GENDER_CD         AS CLT_GENDER_CD, \n"
      + "    clt.HEALTH_TXT        AS CLT_HEALTH_TXT, \n"
      + "    clt.HISP_UD_CD        AS CLT_HISP_UD_CD, \n"
      + "    clt.HISP_CD           AS CLT_HISP_CD, \n"
      + "    clt.I_CNTRY_C         AS CLT_I_CNTRY_C, \n"
      + "    clt.IMGT_STC          AS CLT_IMGT_STC, \n"
      + "    clt.INCAPC_CD         AS CLT_INCAPC_CD, \n"
      + "    clt.HCARE_IND         AS CLT_HCARE_IND, \n"
      + "    clt.LIMIT_IND         AS CLT_LIMIT_IND, \n"
      + "    clt.LITRATE_CD        AS CLT_LITRATE_CD, \n"
      + "    clt.MAR_HIST_B        AS CLT_MAR_HIST_B, \n"
      + "    clt.MRTL_STC          AS CLT_MRTL_STC, \n"
      + "    clt.MILT_STACD        AS CLT_MILT_STACD, \n"
      + "    clt.MTERM_DT          AS CLT_MTERM_DT, \n"
      + "    clt.NMPRFX_DSC        AS CLT_NMPRFX_DSC, \n"
      + "    clt.NAME_TPC          AS CLT_NAME_TPC, \n"
      + "    clt.OUTWRT_IND        AS CLT_OUTWRT_IND, \n"
      + "    clt.PREVCA_IND        AS CLT_PREVCA_IND, \n"
      + "    clt.POTH_DESC         AS CLT_POTH_DESC, \n"
      + "    clt.PREREG_IND        AS CLT_PREREG_IND, \n"
      + "    clt.P_ETHNCTYC        AS CLT_P_ETHNCTYC, \n"
      + "    clt.P_LANG_TPC        AS CLT_P_LANG_TPC, \n"
      + "    clt.RLGN_TPC          AS CLT_RLGN_TPC, \n"
      + "    clt.S_LANG_TC         AS CLT_S_LANG_TC, \n"
      + "    clt.SNTV_HLIND        AS CLT_SNTV_HLIND, \n"
      + "    clt.SENSTV_IND        AS CLT_SENSTV_IND, \n"
      + "    clt.SOCPLC_CD         AS CLT_SOCPLC_CD, \n"
      + "    clt.SOC158_IND        AS CLT_SOC158_IND, \n"
      + "    clt.SSN_CHG_CD        AS CLT_SSN_CHG_CD, \n"
      + "    clt.SS_NO             AS CLT_SS_NO, \n"
      + "    clt.SUFX_TLDSC        AS CLT_SUFX_TLDSC, \n"
      + "    clt.TRBA_CLT_B        AS CLT_TRBA_CLT_B, \n"
      + "    clt.TR_MBVRT_B        AS CLT_TR_MBVRT_B, \n"
      + "    clt.UNEMPLY_CD        AS CLT_UNEMPLY_CD, \n"
      + "    clt.ZIPPY_IND         AS CLT_ZIPPY_IND, \n"
      + "    clt.IBMSNAP_LOGMARKER AS CLT_IBMSNAP_LOGMARKER, \n"
      + "    clt.IBMSNAP_OPERATION AS CLT_IBMSNAP_OPERATION \n"
      + KEY_SOURCE
      + "JOIN  CLIENT_T  clt ON clt.IDENTIFIER = gt.IDENTIFIER \n"
      + SEL_OPTIMIZE;
  //@formatter:on

  public static final String SEL_CLI_ADDR =
  //@formatter:off
        "SELECT \n"
      + "     gt.IDENTIFIER        AS CLT_IDENTIFIER, \n"
      + "    cla.IDENTIFIER        AS CLA_IDENTIFIER, \n"
      + "    cla.LST_UPD_ID        AS CLA_LST_UPD_ID, \n"
      + "    cla.LST_UPD_TS        AS CLA_LST_UPD_TS, \n"
      + "    cla.ADDR_TPC          AS CLA_ADDR_TPC, \n"
      + "    cla.BK_INMT_ID        AS CLA_BK_INMT_ID, \n"
      + "    cla.EFF_END_DT        AS CLA_EFF_END_DT, \n"
      + "    cla.EFF_STRTDT        AS CLA_EFF_STRTDT, \n"
      + "    cla.FKADDRS_T         AS CLA_FKADDRS_T, \n"
      + "    cla.FKCLIENT_T        AS CLA_FKCLIENT_T, \n"
      + "    cla.FKREFERL_T        AS CLA_FKREFERL_T, \n"
      + "    cla.HOMLES_IND        AS CLA_HOMLES_IND, \n"
      + "    cla.IBMSNAP_LOGMARKER AS CLA_IBMSNAP_LOGMARKER, \n"
      + "    cla.IBMSNAP_OPERATION AS CLA_IBMSNAP_OPERATION \n"
      + KEY_SOURCE
      + "JOIN CL_ADDRT  cla ON  gt.IDENTIFIER = cla.FKCLIENT_T \n"
      + "JOIN ADDRS_T   adr ON cla.FKADDRS_T  = adr.IDENTIFIER \n"
      + "WHERE cla.EFF_END_DT IS NULL \n"
      + "  AND cla.IBMSNAP_OPERATION IN ('I','U') \n"
      + "  AND adr.IBMSNAP_OPERATION IN ('I','U') \n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_ADDR =
        "SELECT \n"
      + "     gt.IDENTIFIER        AS CLT_IDENTIFIER, \n"
      + "    cla.IDENTIFIER        AS CLA_IDENTIFIER, \n"
      + "    adr.IDENTIFIER        AS ADR_IDENTIFIER, \n"
      + "    adr.LST_UPD_ID        AS ADR_LST_UPD_ID, \n"
      + "    adr.LST_UPD_TS        AS ADR_LST_UPD_TS, \n"
      + "    TRIM(adr.ADDR_DSC)    AS ADR_ADDR_DSC, \n"
      + "    TRIM(adr.CITY_NM)     AS ADR_CITY_NM, \n"
      + "    adr.EMRG_EXTNO        AS ADR_EMRG_EXTNO, \n"
      + "    adr.EMRG_TELNO        AS ADR_EMRG_TELNO, \n"
      + "    adr.FRG_ADRT_B        AS ADR_FRG_ADRT_B, \n"
      + "    adr.GVR_ENTC          AS ADR_GVR_ENTC, \n"
      + "    TRIM(adr.HEADER_ADR)  AS ADR_HEADER_ADR, \n"
      + "    adr.MSG_EXT_NO        AS ADR_MSG_EXT_NO, \n"
      + "    adr.MSG_TEL_NO        AS ADR_MSG_TEL_NO, \n"
      + "    TRIM(adr.POSTDIR_CD)  AS ADR_POSTDIR_CD, \n"
      + "    TRIM(adr.PREDIR_CD)   AS ADR_PREDIR_CD, \n"
      + "    adr.PRM_EXT_NO        AS ADR_PRM_EXT_NO, \n"
      + "    adr.PRM_TEL_NO        AS ADR_PRM_TEL_NO, \n"
      + "    adr.STATE_C           AS ADR_STATE_C, \n"
      + "    TRIM(adr.STREET_NM)   AS ADR_STREET_NM, \n"
      + "    TRIM(adr.STREET_NO)   AS ADR_STREET_NO, \n"
      + "    adr.ST_SFX_C          AS ADR_ST_SFX_C, \n"
      + "    adr.UNT_DSGC          AS ADR_UNT_DSGC, \n"
      + "    TRIM(adr.UNIT_NO)     AS ADR_UNIT_NO, \n"
      + "    TRIM(adr.ZIP_NO)      AS ADR_ZIP_NO, \n"
      + "    adr.ZIP_SFX_NO        AS ADR_ZIP_SFX_NO, \n"
      + "    adr.IBMSNAP_LOGMARKER AS ADR_IBMSNAP_LOGMARKER, \n"
      + "    adr.IBMSNAP_OPERATION AS ADR_IBMSNAP_OPERATION \n"
      + KEY_SOURCE
      + "JOIN CL_ADDRT  cla ON  gt.IDENTIFIER = cla.FKCLIENT_T \n"
      + "JOIN ADDRS_T   adr ON cla.FKADDRS_T  = adr.IDENTIFIER \n"
      + "WHERE cla.EFF_END_DT IS NULL \n"
      + "  AND cla.IBMSNAP_OPERATION IN ('I','U') \n"
      + "  AND adr.IBMSNAP_OPERATION IN ('I','U') \n"
      + SEL_OPTIMIZE;
  //@formatter:on

  public static final String SEL_CLI_COUNTY =
  //@formatter:off
        "SELECT \n"
      + "    clc.CLIENT_ID         AS CLT_IDENTIFIER, \n"
      + "    clc.GVR_ENTC          AS CLC_GVR_ENTC, \n"
      + "    clc.LST_UPD_TS        AS CLC_LST_UPD_TS, \n"
      + "    clc.LST_UPD_OP        AS CLC_LST_UPD_OP, \n"
      + "    clc.CNTY_RULE         AS CLC_CNTY_RULE \n"
      + KEY_SOURCE
      + "JOIN CLIENT_CNTY clc ON gt.IDENTIFIER = clc.CLIENT_ID \n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_CASE =
        "SELECT \n"
      + "    cas.FKCHLD_CLT        AS CLT_IDENTIFIER, \n"
      + "    cas.IDENTIFIER        AS CAS_IDENTIFIER, \n"
      + "    cas.RSP_AGY_CD        AS CAS_RSP_AGY_CD, \n"
      + "    cas.IBMSNAP_OPERATION AS CAS_IBMSNAP_OPERATION, \n"
      + "    cas.IBMSNAP_LOGMARKER AS CAS_IBMSNAP_LOGMARKER \n"
      + KEY_SOURCE
      + "JOIN CASE_T cas ON cas.FKCHLD_CLT = gt.IDENTIFIER  \n"
      + "WHERE cas.END_DT IS NULL \n"
      + "  AND cas.IBMSNAP_OPERATION IN ('I','U') \n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_CSEC =
        "SELECT \n"
      + "    csh.FKCHLD_CLT        AS CLT_IDENTIFIER, \n"
      + "    csh.THIRD_ID          AS CSH_THIRD_ID, \n"
      + "    csh.CSEC_TPC          AS CSH_CSEC_TPC, \n"
      + "    csh.START_DT          AS CSH_START_DT, \n"
      + "    csh.END_DT            AS CSH_END_DT, \n"
      + "    csh.LST_UPD_ID        AS CSH_LST_UPD_ID, \n"
      + "    csh.LST_UPD_TS        AS CSH_LST_UPD_TS, \n"
      + "    csh.IBMSNAP_OPERATION AS CSH_IBMSNAP_OPERATION, \n"
      + "    csh.IBMSNAP_LOGMARKER AS CSH_IBMSNAP_LOGMARKER \n"
      + KEY_SOURCE
      + "JOIN CSECHIST csh ON csh.FKCHLD_CLT = gt.IDENTIFIER  \n"
      + "WHERE csh.IBMSNAP_OPERATION IN ('I','U') \n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_ETHNIC =
        "SELECT \n"
      + "     gt.IDENTIFIER        AS CLT_IDENTIFIER, \n"
      + "    eth.IDENTIFIER        AS ETH_IDENTIFIER, \n"
      + "    eth.ETHNCTYC          AS ETHNICITY_CODE, \n"
      + "    eth.IBMSNAP_LOGMARKER AS ETH_IBMSNAP_LOGMARKER, \n"
      + "    eth.IBMSNAP_OPERATION AS ETH_IBMSNAP_OPERATION \n"
      + KEY_SOURCE
      + "JOIN CLSCP_ET eth ON gt.IDENTIFIER = eth.ESTBLSH_ID  \n"
      + "WHERE eth.ESTBLSH_CD = 'C' \n"
      + "  AND eth.IBMSNAP_OPERATION IN ('I','U') \n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_AKA =
        "SELECT \n"
      + "     gt.IDENTIFIER        AS CLT_IDENTIFIER, \n"
      + "    onm.THIRD_ID          AS ONM_THIRD_ID, \n"
      + "    onm.FIRST_NM          AS ONM_FIRST_NM, \n"
      + "    onm.LAST_NM           AS ONM_LAST_NM, \n"
      + "    onm.MIDDLE_NM         AS ONM_MIDDLE_NM, \n"
      + "    onm.NMPRFX_DSC        AS ONM_NMPRFX_DSC, \n"
      + "    onm.NAME_TPC          AS ONM_NAME_TPC, \n"
      + "    onm.SUFX_TLDSC        AS ONM_SUFX_TLDSC, \n"
      + "    onm.LST_UPD_ID        AS ONM_LST_UPD_ID, \n"
      + "    onm.LST_UPD_TS        AS ONM_LST_UPD_TS, \n"
      + "    onm.IBMSNAP_OPERATION AS ONM_IBMSNAP_OPERATION, \n"
      + "    onm.IBMSNAP_LOGMARKER AS ONM_IBMSNAP_LOGMARKER \n"
      + KEY_SOURCE
      + "JOIN OCL_NM_T onm ON onm.FKCLIENT_T = gt.IDENTIFIER \n"
      + "WHERE onm.IBMSNAP_OPERATION IN ('I','U') \n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_SAFETY =
        "SELECT \n"
      + "     gt.IDENTIFIER        AS CLT_IDENTIFIER, \n"
      + "    sal.THIRD_ID          AS SAL_THIRD_ID, \n"
      + "    sal.ACTV_RNC          AS SAL_ACTV_RNC, \n"
      + "    sal.ACTV_DT           AS SAL_ACTV_DT, \n"
      + "    sal.ACTV_GEC          AS SAL_ACTV_GEC, \n"
      + "    sal.ACTV_TXT          AS SAL_ACTV_TXT, \n"
      + "    sal.DACT_DT           AS SAL_DACT_DT, \n"
      + "    sal.DACT_GEC          AS SAL_DACT_GEC, \n"
      + "    sal.DACT_TXT          AS SAL_DACT_TXT, \n"
      + "    sal.LST_UPD_ID        AS SAL_LST_UPD_ID, \n"
      + "    sal.LST_UPD_TS        AS SAL_LST_UPD_TS, \n"
      + "    sal.IBMSNAP_LOGMARKER AS SAL_IBMSNAP_LOGMARKER, \n"
      + "    sal.IBMSNAP_OPERATION AS SAL_IBMSNAP_OPERATION \n"
      + KEY_SOURCE
      + "JOIN SAF_ALRT sal ON sal.FKCLIENT_T = gt.IDENTIFIER \n"
      + "WHERE sal.IBMSNAP_OPERATION IN ('I','U') \n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_PLACE_ADDR =
        "WITH STEP1 AS ( \n"
      + "   SELECT \n"
      + "     pe.FKCLIENT_T, \n"
      + "     pe.THIRD_ID, \n"
      + "     ohp.IDENTIFIER    AS OHP_ID, \n"
      + "     ph.IDENTIFIER     AS PH_ID, \n"
      + "     CURRENT TIMESTAMP AS MATERIALIZE_ME, \n"
      + "     DENSE_RANK() OVER (PARTITION BY pe.FKCLIENT_T ORDER BY ohp.START_DT, ohp.END_DT) AS RN \n"
      + KEY_SOURCE
      + "   JOIN PLC_EPST pe  ON gt.IDENTIFIER  = pe.FKCLIENT_T \n"
      + "   JOIN O_HM_PLT ohp ON ohp.FKPLC_EPS0 = pe.THIRD_ID AND ohp.FKPLC_EPST = pe.FKCLIENT_T \n"
      + "   JOIN PLC_HM_T ph  ON ph.IDENTIFIER  = ohp.FKPLC_HM_T \n"
      + "   WHERE DATE('LAST_RUN_END') BETWEEN OHP.START_DT AND NVL(OHP.END_DT, DATE('LAST_RUN_END')) \n"
      + "     AND pe.IBMSNAP_OPERATION  IN ('I','U') \n"
      + "     AND ohp.IBMSNAP_OPERATION IN ('I','U') \n"
      + "     AND ph.IBMSNAP_OPERATION  IN ('I','U') \n"
      + "), \n"
      + "STEP2 AS ( \n"
      + "   SELECT DISTINCT s1.FKCLIENT_T, s1.THIRD_ID, s1.OHP_ID, s1.PH_ID \n"
      + "   FROM STEP1 s1 \n"
      + "   WHERE s1.rn = 1 \n"
      + ") \n"
      + "SELECT \n"
      + "    s2.FKCLIENT_T            AS CLIENT_ID, \n"
      + "    s2.THIRD_ID              AS PE_THIRD_ID, \n"
      + "    s2.OHP_ID, \n"
      + "    s2.PH_ID, \n"
      + "    ohp.START_DT, \n"
      + "    ohp.END_DT, \n"
      + "    NULLIF(pe.GVR_ENTC,   0) AS PE_GVR_ENTC, \n"
      + "    NULLIF(ph.GVR_ENTC,   0) AS PH_GVR_ENTC, \n"
      + "    TRIM(ph.STREET_NO)       AS STREET_NO, \n"
      + "    TRIM(ph.STREET_NM)       AS STREET_NM,  \n"
      + "    TRIM(ph.CITY_NM)         AS CITY_NM, \n"
      + "    NULLIF(ph.F_STATE_C,  0) AS STATE_C, \n"
      + "    NULLIF(ph.ZIP_NO,     0) AS ZIP_NO, \n"
      + "    NULLIF(ph.ZIP_SFX_NO, 0) AS ZIP_SFX_NO, \n"
      + "    ph.LST_UPD_TS            AS PH_LST_UPD_TS, \n"
      + "    NULLIF(ph.PRM_TEL_NO, 0) AS PRM_TEL_NO, \n"
      + "    NULLIF(ph.PRM_EXT_NO, 0) AS PRM_EXT_NO \n"
      + "FROM STEP2 s2 \n"
      + "JOIN PLC_EPST pe  ON  pe.FKCLIENT_T = s2.FKCLIENT_T AND pe.THIRD_ID = s2.THIRD_ID \n"
      + "JOIN O_HM_PLT ohp ON ohp.IDENTIFIER = s2.OHP_ID \n"
      + "JOIN PLC_HM_T ph  ON  ph.IDENTIFIER = s2.PH_ID \n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String INS_CLI_DUMMY =
        "INSERT INTO GT_ID (IDENTIFIER) \n" 
      + "SELECT '1234567abc' FROM SYSIBM.SYSDUMMY1 X WHERE 1=2";
  //@formatter:on

  //@formatter:off
  public static final String INS_PLACE_CLI_FULL =
        "INSERT INTO GT_ID (IDENTIFIER) \n" 
      + "SELECT DISTINCT pe.FKCLIENT_T \n"
      + "FROM PLC_EPST pe \n" 
      + "WHERE pe.FKCLIENT_T BETWEEN ? AND ? AND pe.IBMSNAP_OPERATION IN ('I','U')";
  //@formatter:on

  //@formatter:off
  public static final String BASE_CLI_IDS_LST_CHG =
        "SELECT DISTINCT x.CLIENT_ID FROM ( \n"
      + " SELECT s1.CLIENT_ID FROM ( \n"
      + "      SELECT CLT.IDENTIFIER AS CLIENT_ID \n"
      + "      FROM CLIENT_T clt \n"
      + "      WHERE CLT.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
      + "  UNION ALL SELECT cla.FKCLIENT_T AS CLIENT_ID \n"
      + "      FROM CL_ADDRT cla \n"
      + "      WHERE CLA.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
      + "  UNION ALL SELECT cla.FKCLIENT_T AS CLIENT_ID \n"
      + "      FROM CL_ADDRT cla \n"
      + "      JOIN ADDRS_T  adr ON cla.FKADDRS_T  = adr.IDENTIFIER \n"
      + "      WHERE ADR.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
      + "  UNION ALL SELECT eth.ESTBLSH_ID AS CLIENT_ID \n"
      + "      FROM CLSCP_ET eth \n"
      + "      WHERE ETH.ESTBLSH_CD = 'C' \n"
      + "      AND ETH.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
      + "  UNION ALL SELECT cc.CLIENT_ID\n"
      + "      FROM CLIENT_CNTY cc\n"
      + "      WHERE cc.LST_UPD_TS BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END'\n"
      + "  UNION ALL SELECT sal.FKCLIENT_T AS CLIENT_ID\n"
      + "      FROM SAF_ALRT sal\n"
      + "      WHERE sal.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END'\n"
      + "  UNION ALL SELECT csh.FKCHLD_CLT AS CLIENT_ID\n"
      + "      FROM CSECHIST csh\n"
      + "      WHERE csh.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END'\n"
      + "  UNION ALL SELECT cas.FKCHLD_CLT AS CLIENT_ID\n"
      + "      FROM CASE_T cas\n"
      + "      WHERE cas.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END'\n"
      + "  UNION ALL SELECT onm.FKCLIENT_T AS CLIENT_ID\n"
      + "      FROM OCL_NM_T onm\n"
      + "      WHERE onm.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END'\n"
      + " ) s1 \n"
      + " UNION ALL \n"
      + " SELECT s2.CLIENT_ID FROM ( \n"
      + "      SELECT pe.FKCLIENT_T AS CLIENT_ID \n"
      + "      FROM PLC_EPST pe \n"
      + "      WHERE pe.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
      + "  UNION ALL SELECT ohp.FKPLC_EPST AS CLIENT_ID \n"
      + "      FROM O_HM_PLT ohp \n"
      + "      JOIN PLC_HM_T ph ON ph.IDENTIFIER = ohp.FKPLC_HM_T \n"
      + "      WHERE ph.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
      + "      AND DATE('LAST_RUN_END') BETWEEN OHP.START_DT AND NVL(OHP.END_DT, DATE('LAST_RUN_END')) \n"
      + "  UNION ALL SELECT ohp.FKPLC_EPST AS CLIENT_ID \n"
      + "      FROM O_HM_PLT ohp \n"
      + "      WHERE ohp.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
      + "  UNION ALL SELECT csh.FKCHLD_CLT AS CLIENT_ID \n"
      + "      FROM CSECHIST csh \n"
      + "      WHERE csh.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
      + " ) s2 \n"
      + ") x \n";
  //@formatter:on

  //@formatter:off
  public static final String SEL_CLI_IDS_LST_CHG =
        BASE_CLI_IDS_LST_CHG
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String INS_CLI_LST_CHG =
        "INSERT INTO GT_ID (IDENTIFIER) \n"
      + BASE_CLI_IDS_LST_CHG;
  //@formatter:on

  public static final String INS_LST_CHG_KEY_BUNDLE = "INSERT INTO GT_ID (IDENTIFIER) VALUES (?)";

}
