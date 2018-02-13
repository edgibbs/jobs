package gov.ca.cwds.neutron.rocket;

import gov.ca.cwds.data.std.ApiMarker;

public class ClientSQLResource implements ApiMarker {

  private static final long serialVersionUID = 1L;

  public static final String LAST_CHG_COLUMNS =
      "x.CLT_IDENTIFIER,x.CLT_LST_UPD_ID,x.CLT_LST_UPD_TS,x.CLT_ADJDEL_IND,x.CLT_ADPTN_STCD,x.CLT_ALN_REG_NO,x.CLT_BIRTH_CITY,x.CLT_B_CNTRY_C,x.CLT_BIRTH_DT,x.CLT_BR_FAC_NM,x.CLT_B_STATE_C,x.CLT_BP_VER_IND,x.CLT_CHLD_CLT_B,x.CLT_CL_INDX_NO,x.CLT_COMMNT_DSC,x.CLT_COM_FST_NM,x.CLT_COM_LST_NM,x.CLT_COM_MID_NM,x.CLT_CONF_ACTDT,x.CLT_CONF_EFIND,x.CLT_CREATN_DT,x.CLT_CURRCA_IND,x.CLT_COTH_DESC,x.CLT_CURREG_IND,x.CLT_DEATH_DT,x.CLT_DTH_DT_IND,x.CLT_DEATH_PLC,x.CLT_DTH_RN_TXT,x.CLT_DRV_LIC_NO,x.CLT_D_STATE_C,x.CLT_EMAIL_ADDR,x.CLT_EST_DOB_CD,x.CLT_ETH_UD_CD,x.CLT_FTERM_DT,x.CLT_GENDER_CD,x.CLT_HEALTH_TXT,x.CLT_HISP_UD_CD,x.CLT_HISP_CD,x.CLT_I_CNTRY_C,x.CLT_IMGT_STC,x.CLT_INCAPC_CD,x.CLT_HCARE_IND,x.CLT_LIMIT_IND,x.CLT_LITRATE_CD,x.CLT_MAR_HIST_B,x.CLT_MRTL_STC,x.CLT_MILT_STACD,x.CLT_MTERM_DT,x.CLT_NMPRFX_DSC,x.CLT_NAME_TPC,x.CLT_OUTWRT_IND,x.CLT_PREVCA_IND,x.CLT_POTH_DESC,x.CLT_PREREG_IND,x.CLT_P_ETHNCTYC,x.CLT_P_LANG_TPC,x.CLT_RLGN_TPC,x.CLT_S_LANG_TC,x.CLT_SNTV_HLIND,x.CLT_SENSTV_IND,x.CLT_SOCPLC_CD,x.CLT_SOC158_IND,x.CLT_SSN_CHG_CD,x.CLT_SS_NO,x.CLT_SUFX_TLDSC,x.CLT_TRBA_CLT_B,x.CLT_TR_MBVRT_B,x.CLT_UNEMPLY_CD,x.CLT_ZIPPY_IND,x.CLT_IBMSNAP_LOGMARKER,x.CLT_IBMSNAP_OPERATION,x.CLA_IDENTIFIER,x.CLA_LST_UPD_ID,x.CLA_LST_UPD_TS,x.CLA_ADDR_TPC,x.CLA_BK_INMT_ID,x.CLA_EFF_END_DT,x.CLA_EFF_STRTDT,x.CLA_FKADDRS_T,x.CLA_FKCLIENT_T,x.CLA_FKREFERL_T,x.CLA_HOMLES_IND,x.CLA_IBMSNAP_LOGMARKER,x.CLA_IBMSNAP_OPERATION,x.ADR_IDENTIFIER,x.ADR_LST_UPD_ID,x.ADR_LST_UPD_TS,x.ADR_ADDR_DSC,x.ADR_CITY_NM,x.ADR_EMRG_EXTNO,x.ADR_EMRG_TELNO,x.ADR_FRG_ADRT_B,x.ADR_GVR_ENTC,x.ADR_HEADER_ADR,x.ADR_MSG_EXT_NO,x.ADR_MSG_TEL_NO,x.ADR_POSTDIR_CD,x.ADR_PREDIR_CD,x.ADR_PRM_EXT_NO,x.ADR_PRM_TEL_NO,x.ADR_STATE_C,x.ADR_STREET_NM,x.ADR_STREET_NO,x.ADR_ST_SFX_C,x.ADR_UNT_DSGC,x.ADR_UNIT_NO,x.ADR_ZIP_NO,x.ADR_ZIP_SFX_NO,x.ADR_IBMSNAP_LOGMARKER,x.ADR_IBMSNAP_OPERATION,x.ETH_IDENTIFIER,x.ETHNICITY_CODE,x.ETH_IBMSNAP_LOGMARKER,x.ETH_IBMSNAP_OPERATION,x.CLC_CLIENT_ID,x.CLC_GVR_ENTC,x.CLC_LST_UPD_TS,x.CLC_LST_UPD_OP,x.CLC_CNTY_RULE,x.LAST_CHG";

  //@formatter:off
  public static final String INSERT_CLIENT_LAST_CHG =
      "INSERT INTO GT_ID (IDENTIFIER)\n" 
          + "SELECT DISTINCT CLT.IDENTIFIER \n"
       + "FROM CLIENT_T clt \n"
       + "WHERE CLT.IBMSNAP_LOGMARKER > 'XYZ' \n"
    + "UNION SELECT DISTINCT cla.FKCLIENT_T AS IDENTIFIER \n"
       + "FROM CL_ADDRT cla \n"
       + "WHERE CLA.IBMSNAP_LOGMARKER > 'XYZ' \n"
    + "UNION SELECT DISTINCT cla.FKCLIENT_T AS IDENTIFIER \n"
       + "FROM CL_ADDRT cla \n"
       + "JOIN ADDRS_T  adr ON cla.FKADDRS_T  = adr.IDENTIFIER \n"
       + "WHERE ADR.IBMSNAP_LOGMARKER > 'XYZ' \n"
    + "UNION SELECT DISTINCT eth.ESTBLSH_ID AS IDENTIFIER \n"
       + "FROM CLSCP_ET eth \n"
       + "WHERE ETH.ESTBLSH_CD = 'C' \n"
       + "AND ETH.IBMSNAP_LOGMARKER > 'XYZ' ";
  //@formatter:on

}
