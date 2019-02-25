package gov.ca.cwds.neutron.rocket;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;

@SuppressWarnings({"squid:S1192", "findbugs:HSC_HUGE_SHARED_STRING_CONSTANT"})
public class ClientSQLResource implements ApiMarker {

  private static final long serialVersionUID = 1L;

  // =================================
  // Neutron, the next generation.
  // =================================

  protected static final String[] POLLED_TABLES = {"ADDRS_T", "CASE_T", "CL_ADDRT", "CLIENT_T",
      "CLSCP_ET", "CSECHIST", "O_HM_PLT", "OCL_NM_T", "PLC_EPST", "PLC_HM_T", "SAF_ALRT"};

  public static final String KEY_SOURCE = "FROM GT_ID   gt\n";

  //@formatter:off
  public static final String UPD_TIMESTAMP =
        "UPDATE TX_SCHEMA.ADDRS_T c\n"
      + "SET c.LST_UPD_TS = CURRENT TIMESTAMP\n"
      + "WHERE c.IDENTIFIER IN (\n"
      + "  SELECT x.IDENTIFIER FROM TX_SCHEMA.ADDRS_T x FETCH FIRST 1 ROWS ONLY\n"
      + ")";
  //@formatter:on

  //@formatter:off
  public static final String SEL_TIMESTAMP =
        "SELECT t.LST_UPD_TS AS T_LST_UPD_TS, r.LST_UPD_TS AS R_LST_UPD_TS\n"
      + "FROM (\n"
      + " SELECT x.IDENTIFIER, x.LST_UPD_TS\n"
      + " FROM TX_SCHEMA.ADDRS_T x\n"
      + " FETCH FIRST 1 ROWS ONLY\n"
      + ") t\n"
      + "JOIN ADDRS_T r ON r.IDENTIFIER = t.IDENTIFIER\n"
      + "FOR READ ONLY WITH UR";
  //@formatter:on

  //@formatter:off
  public static final String SEL_OPTIMIZE =
        "OPTIMIZE FOR 1000 ROWS\n"
      + "FOR READ ONLY WITH UR ";
  //@formatter:on

  //@formatter:off
  public static final String INS_CLI_RNG =
        "INSERT INTO GT_ID (IDENTIFIER)\n"
      + "SELECT x.IDENTIFIER\n"
      + "FROM CLIENT_T x\n"
      + "WHERE X.IDENTIFIER BETWEEN ? AND ?\n"
      + "AND x.IBMSNAP_OPERATION IN ('I','U')";
  //@formatter:on

  // =========================================
  // SNAP-915: process changed records once.
  // =========================================

  // Record changed records permanently after successful run.
  //@formatter:off
  public static final String INS_TRACK_CHANGES =
        "INSERT INTO LC_TRK_CHG (CLIENT_ID, OTHER_ID, TBL, REP_TS, REP_OP)\n"
      + "SELECT CLIENT_ID, OTHER_ID, TBL, REP_TS, REP_OP\n"
      + "FROM TMP_LC_TRK_CHG \n"
      + "WHERE RUN_ID = ?";
  //@formatter:on

  //@formatter:off
  public static final String SEL_NEXT_RUN_ID =
      "SELECT SEQ_LC_TRK_RUN.NEXTVAL FROM SYSIBM.SYSDUMMY1 FOR READ ONLY WITH UR";
  //@formatter:on

  //@formatter:off
  public static final String SEL_CLIENTS_BY_RUN_ID =
      "SELECT DISTINCT CLIENT_ID FROM TMP_LC_TRK_CHG WHERE RUN_ID = ?";  
  //@formatter:on

  //@formatter:off
  public static final String CLEANUP_TEMP_CHG =
      "DELETE FROM TMP_LC_TRK_CHG";
   // "TRUNCATE TABLE TMP_LC_TRK_CHG";
  //@formatter:on

  //@formatter:off
  public static final String DEL_OLD_TRACKS =
      "DELETE FROM LC_TRK_CHG x WHERE x.REP_TS < (CURRENT TIMESTAMP - " + 
       (Math.abs(NeutronIntegerDefaults.LOOKBACK_MINUTES.value()) + 3) 
    + " MINUTE)";
  //@formatter:on

  // =========================================
  // SELECT:
  // =========================================

  /**
   * SNAP-754: Launch Command: remove deleted clients from index.
   * 
   * <p>
   * Read deleted clients in order to remove them from the indexes.
   * </p>
   */
  //@formatter:off
  public static final String SEL_CLI =
        "SELECT\n"
      + "  clt.IDENTIFIER        AS CLT_IDENTIFIER,\n"
      + "  clt.LST_UPD_ID        AS CLT_LST_UPD_ID,\n"
      + "  clt.LST_UPD_TS        AS CLT_LST_UPD_TS,\n"
      + "  clt.BIRTH_DT          AS CLT_BIRTH_DT,\n"
      + "  TRIM(clt.CL_INDX_NO)  AS CLT_CL_INDX_NO,\n"
      + "  TRIM(clt.COM_FST_NM)  AS CLT_COM_FST_NM,\n"
      + "  TRIM(clt.COM_LST_NM)  AS CLT_COM_LST_NM,\n"
      + "  TRIM(clt.COM_MID_NM)  AS CLT_COM_MID_NM,\n"
      + "  TRIM(clt.EMAIL_ADDR)  AS CLT_EMAIL_ADDR,\n"
      + "  clt.GENDER_CD         AS CLT_GENDER_CD,\n"
      + "  clt.HISP_UD_CD        AS CLT_HISP_UD_CD,\n"
      + "  clt.HISP_CD           AS CLT_HISP_CD,\n"
      + "  clt.MRTL_STC          AS CLT_MRTL_STC,\n"
      + "  TRIM(clt.NMPRFX_DSC)  AS CLT_NMPRFX_DSC,\n"
      + "  clt.NAME_TPC          AS CLT_NAME_TPC,\n"
      + "  clt.P_ETHNCTYC        AS CLT_P_ETHNCTYC,\n"
      + "  clt.P_LANG_TPC        AS CLT_P_LANG_TPC,\n"
      + "  clt.RLGN_TPC          AS CLT_RLGN_TPC,\n"
      + "  clt.S_LANG_TC         AS CLT_S_LANG_TC,\n"
      + "  clt.SENSTV_IND        AS CLT_SENSTV_IND,\n"
      + "  clt.SS_NO             AS CLT_SS_NO,\n"
      + "  TRIM(clt.SUFX_TLDSC)  AS CLT_SUFX_TLDSC,\n"
      + "  clt.IBMSNAP_LOGMARKER AS CLT_IBMSNAP_LOGMARKER,\n"
      + "  clt.IBMSNAP_OPERATION AS CLT_IBMSNAP_OPERATION,\n"
      + "  clt.ADDED_TS          AS CLT_ADDED_TS\n"
      + KEY_SOURCE
      + "JOIN  CLIENT_T clt ON clt.IDENTIFIER = gt.IDENTIFIER\n"
      + SEL_OPTIMIZE;
  //@formatter:on

  public static final String SEL_CLI_ADDR =
  //@formatter:off
        "SELECT DISTINCT\n"
      + "     gt.IDENTIFIER        AS CLT_IDENTIFIER,\n"
      + "    cla.IDENTIFIER        AS CLA_IDENTIFIER,\n"
      + "    cla.LST_UPD_ID        AS CLA_LST_UPD_ID,\n"
      + "    cla.LST_UPD_TS        AS CLA_LST_UPD_TS,\n"
      + "    cla.ADDR_TPC          AS CLA_ADDR_TPC,\n"
      + "    cla.EFF_END_DT        AS CLA_EFF_END_DT,\n"
      + "    cla.EFF_STRTDT        AS CLA_EFF_STRTDT,\n"
      + "    cla.FKADDRS_T         AS CLA_FKADDRS_T,\n"
      + "    cla.FKCLIENT_T        AS CLA_FKCLIENT_T,\n"
      + "    cla.IBMSNAP_LOGMARKER AS CLA_IBMSNAP_LOGMARKER,\n"
      + "    cla.IBMSNAP_OPERATION AS CLA_IBMSNAP_OPERATION\n"
      + KEY_SOURCE
      + "JOIN CL_ADDRT cla ON  gt.IDENTIFIER = cla.FKCLIENT_T\n"
      + "JOIN ADDRS_T  adr ON cla.FKADDRS_T  = adr.IDENTIFIER\n"
      + "WHERE cla.EFF_END_DT IS NULL\n"
      + "  AND cla.IBMSNAP_OPERATION IN ('I','U')\n"
      + "  AND adr.IBMSNAP_OPERATION IN ('I','U')\n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_ADDR =
      "SELECT DISTINCT\n"
      + "  cla.FKCLIENT_T        AS CLT_IDENTIFIER,\n"
      + "  cla.IDENTIFIER        AS CLA_IDENTIFIER,\n"
      + "  adr.IDENTIFIER        AS ADR_IDENTIFIER,\n"
      + "  adr.LST_UPD_ID        AS ADR_LST_UPD_ID,\n"
      + "  adr.LST_UPD_TS        AS ADR_LST_UPD_TS,\n"
      + "  TRIM(adr.CITY_NM)     AS ADR_CITY_NM,\n"
      + "  adr.EMRG_EXTNO        AS ADR_EMRG_EXTNO,\n"
      + "  adr.EMRG_TELNO        AS ADR_EMRG_TELNO,\n"
      + "  adr.FRG_ADRT_B        AS ADR_FRG_ADRT_B,\n"
      + "  adr.GVR_ENTC          AS ADR_GVR_ENTC,\n"
      + "  adr.MSG_EXT_NO        AS ADR_MSG_EXT_NO,\n"
      + "  adr.MSG_TEL_NO        AS ADR_MSG_TEL_NO,\n"
      + "  adr.PRM_EXT_NO        AS ADR_PRM_EXT_NO,\n"
      + "  adr.PRM_TEL_NO        AS ADR_PRM_TEL_NO,\n"
      + "  adr.STATE_C           AS ADR_STATE_C,\n"
      + "  TRIM(adr.STREET_NM)   AS ADR_STREET_NM,\n"
      + "  TRIM(adr.STREET_NO)   AS ADR_STREET_NO,\n"
      + "  adr.ST_SFX_C          AS ADR_ST_SFX_C,\n"
      + "  adr.UNT_DSGC          AS ADR_UNT_DSGC,\n"
      + "  TRIM(adr.UNIT_NO)     AS ADR_UNIT_NO,\n"
      + "  TRIM(adr.ZIP_NO)      AS ADR_ZIP_NO,\n"
      + "  adr.ZIP_SFX_NO        AS ADR_ZIP_SFX_NO,\n"
      + "  adr.IBMSNAP_LOGMARKER AS ADR_IBMSNAP_LOGMARKER,\n"
      + "  adr.IBMSNAP_OPERATION AS ADR_IBMSNAP_OPERATION,\n"
      + "  adr.ADDED_TS          AS ADR_ADDED_TS\n"
      + KEY_SOURCE
      + "JOIN CL_ADDRT cla ON  gt.IDENTIFIER = cla.FKCLIENT_T\n"
      + "JOIN ADDRS_T  adr ON cla.FKADDRS_T  = adr.IDENTIFIER\n"
      + "WHERE cla.EFF_END_DT IS NULL\n"
      + "  AND cla.IBMSNAP_OPERATION IN ('I','U')\n"
      + "  AND adr.IBMSNAP_OPERATION IN ('I','U')\n"
      + SEL_OPTIMIZE;
  //@formatter:on

  public static final String SEL_CLI_COUNTY =
  //@formatter:off
        "SELECT\n"
      + "    clc.CLIENT_ID         AS CLT_IDENTIFIER,\n"
      + "    clc.GVR_ENTC          AS CLC_GVR_ENTC,\n"
      + "    clc.LST_UPD_TS        AS CLC_LST_UPD_TS,\n"
      + "    clc.LST_UPD_OP        AS CLC_LST_UPD_OP,\n"
      + "    clc.CNTY_RULE         AS CLC_CNTY_RULE\n"
      + KEY_SOURCE
      + "JOIN CLIENT_CNTY clc ON gt.IDENTIFIER = clc.CLIENT_ID\n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_CASE =
        "SELECT\n"
      + "    cas.FKCHLD_CLT        AS CLT_IDENTIFIER,\n"
      + "    cas.IDENTIFIER        AS CAS_IDENTIFIER,\n"
      + "    cas.RSP_AGY_CD        AS CAS_RSP_AGY_CD,\n"
      + "    cas.IBMSNAP_OPERATION AS CAS_IBMSNAP_OPERATION,\n"
      + "    cas.IBMSNAP_LOGMARKER AS CAS_IBMSNAP_LOGMARKER\n"
      + KEY_SOURCE
      + "JOIN CASE_T cas ON cas.FKCHLD_CLT = gt.IDENTIFIER \n"
      + "WHERE cas.END_DT IS NULL\n"
      + "  AND cas.IBMSNAP_OPERATION IN ('I','U')\n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_CSEC =
        "SELECT\n"
      + "    csh.FKCHLD_CLT        AS CLT_IDENTIFIER,\n"
      + "    csh.THIRD_ID          AS CSH_THIRD_ID,\n"
      + "    csh.CSEC_TPC          AS CSH_CSEC_TPC,\n"
      + "    csh.START_DT          AS CSH_START_DT,\n"
      + "    csh.END_DT            AS CSH_END_DT,\n"
      + "    csh.LST_UPD_ID        AS CSH_LST_UPD_ID,\n"
      + "    csh.LST_UPD_TS        AS CSH_LST_UPD_TS,\n"
      + "    csh.IBMSNAP_OPERATION AS CSH_IBMSNAP_OPERATION,\n"
      + "    csh.IBMSNAP_LOGMARKER AS CSH_IBMSNAP_LOGMARKER\n"
      + KEY_SOURCE
      + "JOIN CSECHIST csh ON csh.FKCHLD_CLT = gt.IDENTIFIER \n"
      + "WHERE csh.IBMSNAP_OPERATION IN ('I','U')\n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_ETHNIC =
        "SELECT\n"
      + "     gt.IDENTIFIER        AS CLT_IDENTIFIER,\n"
      + "    eth.IDENTIFIER        AS ETH_IDENTIFIER,\n"
      + "    eth.ETHNCTYC          AS ETHNICITY_CODE,\n"
      + "    eth.IBMSNAP_LOGMARKER AS ETH_IBMSNAP_LOGMARKER,\n"
      + "    eth.IBMSNAP_OPERATION AS ETH_IBMSNAP_OPERATION\n"
      + KEY_SOURCE
      + "JOIN CLSCP_ET eth ON gt.IDENTIFIER = eth.ESTBLSH_ID \n"
      + "WHERE eth.ESTBLSH_CD = 'C'\n"
      + "  AND eth.IBMSNAP_OPERATION IN ('I','U')\n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_AKA =
        "SELECT\n"
      + "     gt.IDENTIFIER        AS CLT_IDENTIFIER,\n"
      + "    onm.THIRD_ID          AS ONM_THIRD_ID,\n"
      + "    onm.FIRST_NM          AS ONM_FIRST_NM,\n"
      + "    onm.LAST_NM           AS ONM_LAST_NM,\n"
      + "    onm.MIDDLE_NM         AS ONM_MIDDLE_NM,\n"
      + "    onm.NMPRFX_DSC        AS ONM_NMPRFX_DSC,\n"
      + "    onm.NAME_TPC          AS ONM_NAME_TPC,\n"
      + "    onm.SUFX_TLDSC        AS ONM_SUFX_TLDSC,\n"
      + "    onm.LST_UPD_ID        AS ONM_LST_UPD_ID,\n"
      + "    onm.LST_UPD_TS        AS ONM_LST_UPD_TS,\n"
      + "    onm.IBMSNAP_OPERATION AS ONM_IBMSNAP_OPERATION,\n"
      + "    onm.IBMSNAP_LOGMARKER AS ONM_IBMSNAP_LOGMARKER\n"
      + KEY_SOURCE
      + "JOIN OCL_NM_T onm ON onm.FKCLIENT_T = gt.IDENTIFIER\n"
      + "WHERE onm.IBMSNAP_OPERATION IN ('I','U')\n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_SAFETY =
        "SELECT\n"
      + "     gt.IDENTIFIER        AS CLT_IDENTIFIER,\n"
      + "    sal.THIRD_ID          AS SAL_THIRD_ID,\n"
      + "    sal.ACTV_RNC          AS SAL_ACTV_RNC,\n"
      + "    sal.ACTV_DT           AS SAL_ACTV_DT,\n"
      + "    sal.ACTV_GEC          AS SAL_ACTV_GEC,\n"
      + "    sal.ACTV_TXT          AS SAL_ACTV_TXT,\n"
      + "    sal.DACT_DT           AS SAL_DACT_DT,\n"
      + "    sal.DACT_GEC          AS SAL_DACT_GEC,\n"
      + "    sal.DACT_TXT          AS SAL_DACT_TXT,\n"
      + "    sal.LST_UPD_ID        AS SAL_LST_UPD_ID,\n"
      + "    sal.LST_UPD_TS        AS SAL_LST_UPD_TS,\n"
      + "    sal.IBMSNAP_LOGMARKER AS SAL_IBMSNAP_LOGMARKER,\n"
      + "    sal.IBMSNAP_OPERATION AS SAL_IBMSNAP_OPERATION\n"
      + KEY_SOURCE
      + "JOIN SAF_ALRT sal ON sal.FKCLIENT_T = gt.IDENTIFIER\n"
      + "WHERE sal.IBMSNAP_OPERATION IN ('I','U')\n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String SEL_PLACE_ADDR =
        "WITH STEP1 AS (\n"
      + "   SELECT\n"
      + "     pe.FKCLIENT_T,\n"
      + "     pe.THIRD_ID,\n"
      + "     ohp.IDENTIFIER    AS OHP_ID,\n"
      + "     ph.IDENTIFIER     AS PH_ID,\n"
      + "     CURRENT TIMESTAMP AS MATERIALIZE_ME,\n"
      + "     DENSE_RANK() OVER (PARTITION BY pe.FKCLIENT_T ORDER BY ohp.START_DT, ohp.END_DT) AS RN\n"
      + KEY_SOURCE
      + "   JOIN PLC_EPST pe  ON gt.IDENTIFIER  = pe.FKCLIENT_T\n"
      + "   JOIN O_HM_PLT ohp ON ohp.FKPLC_EPS0 = pe.THIRD_ID AND ohp.FKPLC_EPST = pe.FKCLIENT_T\n"
      + "   JOIN PLC_HM_T ph  ON ph.IDENTIFIER  = ohp.FKPLC_HM_T\n"
      + "   WHERE DATE('LAST_RUN_END') BETWEEN OHP.START_DT AND NVL(OHP.END_DT, DATE('LAST_RUN_END'))\n"
      + "     AND pe.IBMSNAP_OPERATION  IN ('I','U')\n"
      + "     AND ohp.IBMSNAP_OPERATION IN ('I','U')\n"
      + "     AND ph.IBMSNAP_OPERATION  IN ('I','U')\n"
      + "),\n"
      + "STEP2 AS (\n"
      + "   SELECT DISTINCT s1.FKCLIENT_T, s1.THIRD_ID, s1.OHP_ID, s1.PH_ID\n"
      + "   FROM STEP1 s1\n"
      + "   WHERE s1.rn = 1\n"
      + ")\n"
      + "SELECT\n"
      + "    s2.FKCLIENT_T            AS CLIENT_ID,\n"
      + "    s2.THIRD_ID              AS PE_THIRD_ID,\n"
      + "    s2.OHP_ID,\n"
      + "    s2.PH_ID,\n"
      + "    ohp.START_DT,\n"
      + "    ohp.END_DT,\n"
      + "    NULLIF(pe.GVR_ENTC,   0) AS PE_GVR_ENTC,\n"
      + "    NULLIF(ph.GVR_ENTC,   0) AS PH_GVR_ENTC,\n"
      + "    TRIM(ph.STREET_NO)       AS STREET_NO,\n"
      + "    TRIM(ph.STREET_NM)       AS STREET_NM, \n"
      + "    TRIM(ph.CITY_NM)         AS CITY_NM,\n"
      + "    NULLIF(ph.F_STATE_C,  0) AS STATE_C,\n"
      + "    NULLIF(ph.ZIP_NO,     0) AS ZIP_NO,\n"
      + "    NULLIF(ph.ZIP_SFX_NO, 0) AS ZIP_SFX_NO,\n"
      + "    ph.LST_UPD_TS            AS PH_LST_UPD_TS,\n"
      + "    NULLIF(ph.PRM_TEL_NO, 0) AS PRM_TEL_NO,\n"
      + "    NULLIF(ph.PRM_EXT_NO, 0) AS PRM_EXT_NO\n"
      + "FROM STEP2 s2\n"
      + "JOIN PLC_EPST pe  ON  pe.FKCLIENT_T = s2.FKCLIENT_T AND pe.THIRD_ID = s2.THIRD_ID\n"
      + "JOIN O_HM_PLT ohp ON ohp.IDENTIFIER = s2.OHP_ID\n"
      + "JOIN PLC_HM_T ph  ON  ph.IDENTIFIER = s2.PH_ID\n"
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String INS_CLI_DUMMY =
        "INSERT INTO GT_ID (IDENTIFIER)\n" 
      + "SELECT '1234567abc' FROM SYSIBM.SYSDUMMY1 X WHERE 1=2";
  //@formatter:on

  //@formatter:off
  public static final String INS_PLACE_CLI_FULL =
        "INSERT INTO GT_ID (IDENTIFIER)\n" 
      + "SELECT DISTINCT pe.FKCLIENT_T\n"
      + "FROM PLC_EPST pe\n" 
      + "WHERE pe.FKCLIENT_T BETWEEN ? AND ? AND pe.IBMSNAP_OPERATION IN ('I','U')";
  //@formatter:on

  //@formatter:off
  public static final String INS_LST_CHG_ALL =
        "INSERT INTO TMP_LC_TRK_CHG (CLIENT_ID, OTHER_ID, TBL, REP_TS, REP_OP, RUN_ID)\n"
      + "SELECT DISTINCT s1.CLIENT_ID, s1.OTHER_ID, s1.TBL, s1.IBMSNAP_LOGMARKER, s1.IBMSNAP_OPERATION, CAST(? AS INT) AS RUN_ID\n"
      + "FROM (\n"
      + "   SELECT CLT.IDENTIFIER AS CLIENT_ID, clt.IDENTIFIER AS OTHER_ID, 'CLT' AS TBL, clt.IBMSNAP_LOGMARKER, clt.IBMSNAP_OPERATION\n"
      + "   FROM CLIENT_T clt\n"
      + "   WHERE CLT.IBMSNAP_LOGMARKER BETWEEN ? AND ?\n"
      + " UNION ALL\n"
      + "   SELECT cla.FKCLIENT_T AS CLIENT_ID, cla.IDENTIFIER AS OTHER_ID, 'CLA' AS TBL, cla.IBMSNAP_LOGMARKER, cla.IBMSNAP_OPERATION\n"
      + "   FROM CL_ADDRT cla\n"
      + "   WHERE CLA.IBMSNAP_LOGMARKER BETWEEN ? AND ?\n"
      + " UNION ALL\n"
      + "   SELECT cla.FKCLIENT_T AS CLIENT_ID, adr.IDENTIFIER AS OTHER_ID, 'ADR' AS TBL, adr.IBMSNAP_LOGMARKER, adr.IBMSNAP_OPERATION\n"
      + "   FROM CL_ADDRT cla\n"
      + "   JOIN ADDRS_T  adr ON cla.FKADDRS_T  = adr.IDENTIFIER\n"
      + "   WHERE ADR.IBMSNAP_LOGMARKER BETWEEN ? AND ?\n"
      + " UNION ALL\n"
      + "   SELECT eth.ESTBLSH_ID AS CLIENT_ID, eth.IDENTIFIER AS OTHER_ID, 'ETH' AS TBL, eth.IBMSNAP_LOGMARKER, eth.IBMSNAP_OPERATION\n"
      + "   FROM CLSCP_ET eth\n"
      + "   WHERE ETH.ESTBLSH_CD = 'C'\n"
      + "     AND ETH.IBMSNAP_LOGMARKER BETWEEN ? AND ?\n"
      + " UNION ALL\n"
      + "   SELECT sal.FKCLIENT_T AS CLIENT_ID, sal.THIRD_ID AS OTHER_ID, 'SAL' AS TBL, sal.IBMSNAP_LOGMARKER, sal.IBMSNAP_OPERATION\n"
      + "   FROM SAF_ALRT sal\n"
      + "   WHERE sal.IBMSNAP_LOGMARKER BETWEEN ? AND ?\n"
      + " UNION ALL\n"
      + "   SELECT csh.FKCHLD_CLT AS CLIENT_ID, csh.THIRD_ID AS OTHER_ID, 'CSH' AS TBL, csh.IBMSNAP_LOGMARKER, csh.IBMSNAP_OPERATION\n"
      + "   FROM CSECHIST csh\n"
      + "   WHERE csh.IBMSNAP_LOGMARKER BETWEEN ? AND ?\n"
      + " UNION ALL\n"
      + "   SELECT cas.FKCHLD_CLT AS CLIENT_ID, cas.IDENTIFIER AS OTHER_ID, 'CAS' AS TBL, cas.IBMSNAP_LOGMARKER, cas.IBMSNAP_OPERATION\n"
      + "   FROM CASE_T cas\n"
      + "   WHERE cas.IBMSNAP_LOGMARKER BETWEEN ? AND ?\n"
      + " UNION ALL\n"
      + "   SELECT onm.FKCLIENT_T AS CLIENT_ID, onm.THIRD_ID AS OTHER_ID, 'ONM' AS TBL, onm.IBMSNAP_LOGMARKER, onm.IBMSNAP_OPERATION\n"
      + "   FROM OCL_NM_T onm\n"
      + "   WHERE onm.IBMSNAP_LOGMARKER BETWEEN ? AND ?\n"
      + " UNION ALL\n"
      + "   SELECT cc.CLIENT_ID, cc.CLIENT_ID AS OTHER_ID, 'CC' AS TBL, cc.LST_UPD_TS AS IBMSNAP_LOGMARKER, cc.LST_UPD_OP AS IBMSNAP_OPERATION\n"
      + "   FROM CLIENT_CNTY cc\n"
      + "   WHERE cc.LST_UPD_TS BETWEEN ? AND ?\n"
      + " UNION ALL\n"
      + "   SELECT pe.FKCLIENT_T AS CLIENT_ID, pe.THIRD_ID AS OTHER_ID, 'PE' AS TBL, pe.IBMSNAP_LOGMARKER, pe.IBMSNAP_OPERATION\n"
      + "   FROM PLC_EPST pe\n"
      + "   WHERE pe.IBMSNAP_LOGMARKER BETWEEN ? AND ?\n"
      + " UNION ALL\n"
      + "   SELECT ohp.FKPLC_EPST AS CLIENT_ID, ph.IDENTIFIER AS OTHER_ID, 'PH' AS TBL, ph.IBMSNAP_LOGMARKER, ph.IBMSNAP_OPERATION\n"
      + "   FROM O_HM_PLT ohp\n"
      + "   JOIN PLC_HM_T ph ON ph.IDENTIFIER = ohp.FKPLC_HM_T\n"
      + "   WHERE ph.IBMSNAP_LOGMARKER BETWEEN ? AND ?\n"
//    + "   AND DATE('?') BETWEEN OHP.START_DT AND NVL(OHP.END_DT, DATE('?'))\n"
//    + "   AND DATE(?) BETWEEN OHP.START_DT AND NVL(OHP.END_DT, DATE(?))\n"
//    + "   AND CAST(? AS DATE) BETWEEN OHP.START_DT AND NVL(OHP.END_DT, CAST(? AS DATE))\n"
      + " UNION ALL\n"
      + "   SELECT ohp.FKPLC_EPST AS CLIENT_ID, ohp.IDENTIFIER AS OTHER_ID, 'OHP' AS TBL, ohp.IBMSNAP_LOGMARKER, ohp.IBMSNAP_OPERATION\n"
      + "   FROM O_HM_PLT ohp\n"
      + "   WHERE ohp.IBMSNAP_LOGMARKER BETWEEN ? AND ?\n"
      + ") s1\n"
      + "WHERE (s1.OTHER_ID, s1.TBL, s1.IBMSNAP_LOGMARKER, s1.IBMSNAP_OPERATION) NOT IN (\n"
      + "   SELECT z.OTHER_ID, z.TBL, z.REP_TS, z.REP_OP FROM LC_TRK_CHG z\n"
      + ")\n";
  //@formatter:on

  //@formatter:off
  public static final String SEL_CLI_IDS_LST_CHG =
      SEL_CLIENTS_BY_RUN_ID
      + SEL_OPTIMIZE;
  //@formatter:on

  //@formatter:off
  public static final String INS_CLI_LST_CHG =
        "INSERT INTO GT_ID (IDENTIFIER)\n"
      + SEL_CLIENTS_BY_RUN_ID;
  //@formatter:on

  public static final String INS_LST_CHG_KEY_BUNDLE = "INSERT INTO GT_ID (IDENTIFIER) VALUES (?)";

  //@formatter:off
  public static final String SEL_REPL_TIME_REAL =
        "SELECT x.* FROM (\n"
      + " SELECT 'D' AS TIME_UNIT, '1' AS UNITS_BACK, MAX(ADDED_TS - IBMSNAP_LOGMARKER) AS rep_max\n"
      + " FROM ADDRS_T \n"
      + " WHERE IBMSNAP_LOGMARKER > (CURRENT TIMESTAMP - 24 HOUR)\n"
      + " UNION ALL\n"
      + " SELECT 'H' AS TIME_UNIT, '8' AS UNITS_BACK, MAX(ADDED_TS - IBMSNAP_LOGMARKER) AS rep_max\n"
      + " FROM ADDRS_T \n"
      + " WHERE IBMSNAP_LOGMARKER > (CURRENT TIMESTAMP - 8 HOUR)\n"
      + " UNION ALL\n"
      + " SELECT 'H' AS TIME_UNIT, '4' AS UNITS_BACK, MAX(ADDED_TS - IBMSNAP_LOGMARKER) AS rep_max\n"
      + " FROM ADDRS_T \n"
      + " WHERE IBMSNAP_LOGMARKER > (CURRENT TIMESTAMP - 4 HOUR)\n"
      + " UNION ALL\n"
      + " SELECT 'H' AS TIME_UNIT, '1' AS UNITS_BACK, MAX(ADDED_TS - IBMSNAP_LOGMARKER) AS rep_max\n"
      + " FROM ADDRS_T \n"
      + " WHERE IBMSNAP_LOGMARKER > (CURRENT TIMESTAMP - 1 HOUR)\n"
      + " UNION ALL\n"
      + " SELECT 'M' AS TIME_UNIT, '5' AS UNITS_BACK, MAX(ADDED_TS - IBMSNAP_LOGMARKER) AS rep_max\n"
      + " FROM ADDRS_T \n"
      + " WHERE IBMSNAP_LOGMARKER > (CURRENT TIMESTAMP - 5 MINUTE)\n"
      + " UNION ALL\n"
      + " SELECT 'M' AS TIME_UNIT, '1' AS UNITS_BACK, MAX(ADDED_TS - IBMSNAP_LOGMARKER) AS rep_max\n"
      + " FROM ADDRS_T \n"
      + " WHERE IBMSNAP_LOGMARKER > (CURRENT TIMESTAMP - 1 MINUTE)\n"
      + ") x\n"
      + "WHERE x.REP_MAX IS NOT NULL\n"
      + "ORDER BY TIME_UNIT DESC, UNITS_BACK DESC\n"
      + "FETCH FIRST 1 ROWS ONLY\n"
      + "FOR READ ONLY WITH UR";
  //@formatter:on

  /**
   * Unreliable query results from IBM's replication tracking tables. Keep for records.
   */
  //@formatter:off
  public static final String SEL_REPL_TIME_IBM =
        "WITH STEP1 AS (\n"
      + " SELECT\n"
      + "    m.SOURCE_TABLE,\n"
      + "    t.LASTRUN,\n"
      + "    ((t.endtime - t.lastrun) + (t.source_conn_time - t.synchtime))         AS RUN_LATENCY_SECS\n"
      + "  , DENSE_RANK() OVER(PARTITION BY m.SOURCE_TABLE ORDER BY t.LASTRUN DESC) AS PRIOR_RUN\n"
      + " FROM ASN.IBMSNAP_APPLYTRAIL t\n"
      + " JOIN ASN.IBMSNAP_SUBS_SET   s ON s.SET_NAME = t.SET_NAME\n"
      + " JOIN ASN.IBMSNAP_SUBS_MEMBR m ON m.SET_NAME = s.SET_NAME\n"
      + " WHERE m.SOURCE_OWNER = 'CWSNS1'\n"
      + "   AND t.lastrun > '2018-12-14-04.17.00.000000'\n"
      + "   AND m.SOURCE_TABLE IN ('ADDRS_T', 'CLIENT_T', 'CL_ADDRT')\n"
      + "), LAST_RUN AS (\n"
      + " SELECT s.* FROM STEP1 s WHERE s.prior_run = 1\n"
      + "), PRIOR_RUN AS (\n"
      + " SELECT s.* FROM STEP1 s WHERE s.prior_run = 2\n"
      + ")\n"
      + "SELECT\n"
      + " l.SOURCE_TABLE,\n"
      + " l.LASTRUN               AS LAST_LASTRUN,\n"
      + " r.LASTRUN               AS RIGHT_LASTRUN,\n"
      + " l.RUN_LATENCY_SECS,\n"
      + " TIMESTAMPDIFF(2, CAST((l.LASTRUN - r.LASTRUN) AS CHAR(22))) AS RUN_DELAY_SECS\n"
      + "FROM LAST_RUN  l\n"
      + "LEFT JOIN PRIOR_RUN r ON l.SOURCE_TABLE = r.SOURCE_TABLE\n"
      + "ORDER BY 1, 2 DESC\n"
      + "FETCH FIRST 10 ROWS ONLY\n"
      + "FOR READ ONLY WITH UR";
  //@formatter:on

}
