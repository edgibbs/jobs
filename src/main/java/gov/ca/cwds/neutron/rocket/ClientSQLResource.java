package gov.ca.cwds.neutron.rocket;

import gov.ca.cwds.data.std.ApiMarker;

@SuppressWarnings("squid:S1192")
public class ClientSQLResource implements ApiMarker {

  private static final long serialVersionUID = 1L;

  //@formatter:off
  public static final String LAST_CHG_COLUMNS =
        " x.CLT_IDENTIFIER,\n"
      + " x.CLT_LST_UPD_ID,\n"
      + " x.CLT_LST_UPD_TS,\n"
      + " x.CLT_ADJDEL_IND,\n"
      + " x.CLT_ADPTN_STCD,\n"
      + " x.CLT_ALN_REG_NO,\n"
      + " x.CLT_BIRTH_CITY,\n"
      + " x.CLT_B_CNTRY_C,\n"
      + " x.CLT_BIRTH_DT,\n"
      + " x.CLT_BR_FAC_NM,\n"
      + " x.CLT_B_STATE_C,\n"
      + " x.CLT_BP_VER_IND,\n"
      + " x.CLT_CHLD_CLT_B,\n"
      + " x.CLT_CL_INDX_NO,\n"
      + " x.CLT_COMMNT_DSC,\n"
      + " x.CLT_COM_FST_NM,\n"
      + " x.CLT_COM_LST_NM,\n"
      + " x.CLT_COM_MID_NM,\n"
      + " x.CLT_CONF_ACTDT,\n"
      + " x.CLT_CONF_EFIND,\n"
      + " x.CLT_CREATN_DT,\n"
      + " x.CLT_CURRCA_IND,\n"
      + " x.CLT_COTH_DESC,\n"
      + " x.CLT_CURREG_IND,\n"
      + " x.CLT_DEATH_DT,\n"
      + " x.CLT_DTH_DT_IND,\n"
      + " x.CLT_DEATH_PLC,\n"
      + " x.CLT_DTH_RN_TXT,\n"
      + " x.CLT_DRV_LIC_NO,\n"
      + " x.CLT_D_STATE_C,\n"
      + " x.CLT_EMAIL_ADDR,\n"
      + " x.CLT_EST_DOB_CD,\n"
      + " x.CLT_ETH_UD_CD,\n"
      + " x.CLT_FTERM_DT,\n"
      + " x.CLT_GENDER_CD,\n"
      + " x.CLT_HEALTH_TXT,\n"
      + " x.CLT_HISP_UD_CD,\n"
      + " x.CLT_HISP_CD,\n"
      + " x.CLT_I_CNTRY_C,\n"
      + " x.CLT_IMGT_STC,\n"
      + " x.CLT_INCAPC_CD,\n"
      + " x.CLT_HCARE_IND,\n"
      + " x.CLT_LIMIT_IND,\n"
      + " x.CLT_LITRATE_CD,\n"
      + " x.CLT_MAR_HIST_B,\n"
      + " x.CLT_MRTL_STC,\n"
      + " x.CLT_MILT_STACD,\n"
      + " x.CLT_MTERM_DT,\n"
      + " x.CLT_NMPRFX_DSC,\n"
      + " x.CLT_NAME_TPC,\n"
      + " x.CLT_OUTWRT_IND,\n"
      + " x.CLT_PREVCA_IND,\n"
      + " x.CLT_POTH_DESC,\n"
      + " x.CLT_PREREG_IND,\n"
      + " x.CLT_P_ETHNCTYC,\n"
      + " x.CLT_P_LANG_TPC,\n"
      + " x.CLT_RLGN_TPC,\n"
      + " x.CLT_S_LANG_TC,\n"
      + " x.CLT_SNTV_HLIND,\n"
      + " x.CLT_SENSTV_IND,\n"
      + " x.CLT_SOCPLC_CD,\n"
      + " x.CLT_SOC158_IND,\n"
      + " x.CLT_SSN_CHG_CD,\n"
      + " x.CLT_SS_NO,\n"
      + " x.CLT_SUFX_TLDSC,\n"
      + " x.CLT_TRBA_CLT_B,\n"
      + " x.CLT_TR_MBVRT_B,\n"
      + " x.CLT_UNEMPLY_CD,\n"
      + " x.CLT_ZIPPY_IND,\n"
      + " x.CLT_IBMSNAP_LOGMARKER,\n"
      + " x.CLT_IBMSNAP_OPERATION,\n"
      + " x.CLA_IDENTIFIER,\n"
      + " x.CLA_LST_UPD_ID,\n"
      + " x.CLA_LST_UPD_TS,\n"
      + " x.CLA_ADDR_TPC,\n"
      + " x.CLA_BK_INMT_ID,\n"
      + " x.CLA_EFF_END_DT,\n"
      + " x.CLA_EFF_STRTDT,\n"
      + " x.CLA_FKADDRS_T,\n"
      + " x.CLA_FKCLIENT_T,\n"
      + " x.CLA_FKREFERL_T,\n"
      + " x.CLA_HOMLES_IND,\n"
      + " x.CLA_IBMSNAP_LOGMARKER,\n"
      + " x.CLA_IBMSNAP_OPERATION,\n"
      + " x.ADR_IDENTIFIER,\n"
      + " x.ADR_LST_UPD_ID,\n"
      + " x.ADR_LST_UPD_TS,\n"
      + " x.ADR_ADDR_DSC,\n"
      + " x.ADR_CITY_NM,\n"
      + " x.ADR_EMRG_EXTNO,\n"
      + " x.ADR_EMRG_TELNO,\n"
      + " x.ADR_FRG_ADRT_B,\n"
      + " x.ADR_GVR_ENTC,\n"
      + " x.ADR_HEADER_ADR,\n"
      + " x.ADR_MSG_EXT_NO,\n"
      + " x.ADR_MSG_TEL_NO,\n"
      + " x.ADR_POSTDIR_CD,\n"
      + " x.ADR_PREDIR_CD,\n"
      + " x.ADR_PRM_EXT_NO,\n"
      + " x.ADR_PRM_TEL_NO,\n"
      + " x.ADR_STATE_C,\n"
      + " x.ADR_STREET_NM,\n"
      + " x.ADR_STREET_NO,\n"
      + " x.ADR_ST_SFX_C,\n"
      + " x.ADR_UNT_DSGC,\n"
      + " x.ADR_UNIT_NO,\n"
      + " x.ADR_ZIP_NO,\n"
      + " x.ADR_ZIP_SFX_NO,\n"
      + " x.ADR_IBMSNAP_LOGMARKER,\n"
      + " x.ADR_IBMSNAP_OPERATION,\n"
      + " x.ETH_IDENTIFIER,\n"
      + " x.ETHNICITY_CODE,\n"
      + " x.ETH_IBMSNAP_LOGMARKER,\n"
      + " x.ETH_IBMSNAP_OPERATION,\n"
      + " x.CLC_CLIENT_ID,\n"
      + " x.CLC_GVR_ENTC,\n"
      + " x.CLC_LST_UPD_TS,\n"
      + " x.CLC_LST_UPD_OP,\n"
      + " x.CLC_CNTY_RULE,\n"
      + " x.SAL_THIRD_ID,\n"
      + " x.SAL_ACTV_RNC,\n"
      + " x.SAL_ACTV_DT,\n"
      + " x.SAL_ACTV_GEC,\n"
      + " x.SAL_ACTV_TXT,\n"
      + " x.SAL_DACT_DT,\n"
      + " x.SAL_DACT_GEC,\n"
      + " x.SAL_DACT_TXT,\n"
      + " x.SAL_LST_UPD_ID,\n"
      + " x.SAL_LST_UPD_TS,\n"
      + " x.SAL_IBMSNAP_LOGMARKER,\n"
      + " x.SAL_IBMSNAP_OPERATION,\n"
      + " x.ONM_THIRD_ID,\n"
      + " x.ONM_FIRST_NM,\n"
      + " x.ONM_LAST_NM,\n"
      + " x.ONM_MIDDLE_NM,\n"
      + " x.ONM_NMPRFX_DSC,\n"
      + " x.ONM_NAME_TPC,\n"
      + " x.ONM_SUFX_TLDSC,\n"
      + " x.ONM_LST_UPD_ID,\n"
      + " x.ONM_LST_UPD_TS,\n"
      + " x.ONM_IBMSNAP_OPERATION,\n"
      + " x.ONM_IBMSNAP_LOGMARKER,\n"
      + " x.CAS_IDENTIFIER,\n"
      + " x.CAS_IBMSNAP_OPERATION,\n"
      + " x.CAS_IBMSNAP_LOGMARKER,\n"
      + " x.LAST_CHG ";  
  //@formatter:on

  //@formatter:off
  public static final String SELECT_PLACEMENT_ADDRESS =
        "SELECT \n"
      +   " x.FKCLIENT_T CLIENT_ID, x.THIRD_ID PE_THIRD_ID, x.PE_GVR_ENTC, \n"
      +   " x.OHP_ID, x.START_DT, x.END_DT, \n"
      +   " x.PH_ID, x.PH_GVR_ENTC, x.STREET_NO, x.STREET_NM, \n"
      +   " x.CITY_NM, x.STATE_C, x.ZIP_NO, x.ZIP_SFX_NO, x.PH_LST_UPD_TS, \n"
      +   " x.PRM_TEL_NO, x.PRM_EXT_NO \n"
      + "FROM ( \n"
      + " SELECT \n"
      + "     PE.FKCLIENT_T, PE.THIRD_ID, PE.GVR_ENTC PE_GVR_ENTC \n"
      + "   , OHP.IDENTIFIER OHP_ID, ohp.START_DT, ohp.END_DT \n"
      + "   , PH.IDENTIFIER PH_ID, PH.GVR_ENTC PH_GVR_ENTC \n"
      + "   , TRIM(PH.STREET_NO) STREET_NO, TRIM(PH.STREET_NM) STREET_NM, TRIM(PH.CITY_NM) CITY_NM \n"
      + "   , PH.F_STATE_C STATE_C, PH.ZIP_NO, PH.ZIP_SFX_NO, ph.LST_UPD_TS PH_LST_UPD_TS \n"
      + "   , PH.PRM_TEL_NO, PH.PRM_EXT_NO \n"
      + "   , DENSE_RANK() OVER (PARTITION BY PE.FKCLIENT_T ORDER BY OHP.START_DT, OHP.END_DT) RN \n"
      + " FROM GT_ID GT \n"
      + " JOIN PLC_EPST PE  ON GT.IDENTIFIER  = PE.FKCLIENT_T \n"
      + " JOIN O_HM_PLT OHP ON OHP.FKPLC_EPS0 = PE.THIRD_ID AND OHP.FKPLC_EPST = PE.FKCLIENT_T \n"
      + " JOIN PLC_HM_T PH  ON PH.IDENTIFIER  = OHP.FKPLC_HM_T \n"
      + " WHERE CURRENT DATE BETWEEN OHP.START_DT AND NVL(OHP.END_DT, CURRENT DATE) \n"
      + "   AND PE.IBMSNAP_OPERATION  IN ('I','U') \n"
      + "   AND OHP.IBMSNAP_OPERATION IN ('I','U') \n"
      + "   AND PH.IBMSNAP_OPERATION  IN ('I','U') \n"
      + " ORDER BY FKCLIENT_T, START_DT \n"
      + ") X \n"
      + "WHERE X.RN = 1 \n"
      + "ORDER BY CLIENT_ID, START_DT \n"
      + "WITH UR";  
  //@formatter:on

  //@formatter:off
  public static final String INSERT_CLIENT_FULL =
      "INSERT INTO GT_ID (IDENTIFIER) \n" 
    + "SELECT '1234567abc' FROM SYSIBM.SYSDUMMY1 X WHERE 1=2 AND '0' BETWEEN ? AND ?";
  //@formatter:on

  //@formatter:off
  public static final String INSERT_PLACEMENT_HOME_CLIENT_FULL =
      "INSERT INTO GT_ID (IDENTIFIER) \n" 
    + "SELECT DISTINCT pe.FKCLIENT_T \n"
    + "FROM PLC_EPST pe \n" 
    + "WHERE pe.FKCLIENT_T BETWEEN ? AND ? AND pe.IBMSNAP_OPERATION IN ('I','U')";
  //@formatter:on

  //@formatter:off
  public static final String INSERT_CLIENT_LAST_CHG =
     "INSERT INTO GT_ID (IDENTIFIER) \n"
          + "SELECT DISTINCT CLT.IDENTIFIER \n"
          + "FROM CLIENT_T clt \n"
          + "WHERE CLT.LST_UPD_TS BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
    + "UNION SELECT DISTINCT cla.FKCLIENT_T AS IDENTIFIER \n"
          + "FROM CL_ADDRT cla \n"
          + "WHERE CLA.LST_UPD_TS BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
    + "UNION SELECT DISTINCT cla.FKCLIENT_T AS IDENTIFIER \n"
          + "FROM CL_ADDRT cla \n"
          + "JOIN ADDRS_T  adr ON cla.FKADDRS_T  = adr.IDENTIFIER \n"
          + "WHERE ADR.LST_UPD_TS BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
    + "UNION SELECT DISTINCT eth.ESTBLSH_ID AS IDENTIFIER \n"
          + "FROM CLSCP_ET eth \n"
          + "WHERE ETH.ESTBLSH_CD = 'C' \n"
          + "AND ETH.LST_UPD_TS BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END'";
  //@formatter:on

  //@formatter:off
  public static final String INSERT_PLACEMENT_HOME_CLIENT_LAST_CHG =
     "INSERT INTO GT_ID (IDENTIFIER) \n"
          + "SELECT DISTINCT pe.FKCLIENT_T \n"
          + "FROM PLC_EPST pe \n"
          + "WHERE pe.LST_UPD_TS BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
    + "UNION SELECT DISTINCT ohp.FKPLC_EPST \n"
          + "FROM O_HM_PLT ohp \n"
          + "JOIN PLC_HM_T ph  ON ph.IDENTIFIER  = ohp.FKPLC_HM_T \n"
          + "WHERE ph.LST_UPD_TS BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
    + "UNION SELECT DISTINCT ohp.FKPLC_EPST \n"
          + "FROM O_HM_PLT ohp  \n"
          + "WHERE ohp.LST_UPD_TS BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END'";
  //@formatter:on

}
