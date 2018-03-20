-- DB2 View
-- ORDER BY clause is only valid on mainframe, remove it if running on other hosts.

-- SET CURRENT SCHEMA = 'CWSRS1';

DROP TABLE MQT_CLIENT_ADDRESS;

CREATE TABLE MQT_CLIENT_ADDRESS AS (
	SELECT 
	clt.IDENTIFIER AS CLT_IDENTIFIER,
	clt.LST_UPD_ID AS CLT_LST_UPD_ID,
	clt.LST_UPD_TS AS CLT_LST_UPD_TS,
	clt.ADJDEL_IND AS CLT_ADJDEL_IND,
	clt.ADPTN_STCD AS CLT_ADPTN_STCD,
	clt.ALN_REG_NO AS CLT_ALN_REG_NO,
	clt.BIRTH_CITY AS CLT_BIRTH_CITY,
	clt.B_CNTRY_C  AS CLT_B_CNTRY_C, 
	clt.BIRTH_DT   AS CLT_BIRTH_DT,  
	clt.BR_FAC_NM  AS CLT_BR_FAC_NM, 
	clt.B_STATE_C  AS CLT_B_STATE_C, 
	clt.BP_VER_IND AS CLT_BP_VER_IND,
	clt.CHLD_CLT_B AS CLT_CHLD_CLT_B,
	clt.CL_INDX_NO AS CLT_CL_INDX_NO,
	clt.COMMNT_DSC AS CLT_COMMNT_DSC,
	clt.COM_FST_NM AS CLT_COM_FST_NM,
	clt.COM_LST_NM AS CLT_COM_LST_NM,
	clt.COM_MID_NM AS CLT_COM_MID_NM,
	clt.CONF_ACTDT AS CLT_CONF_ACTDT,
	clt.CONF_EFIND AS CLT_CONF_EFIND,
	clt.CREATN_DT  AS CLT_CREATN_DT, 
	clt.CURRCA_IND AS CLT_CURRCA_IND,
	clt.COTH_DESC  AS CLT_COTH_DESC, 
	clt.CURREG_IND AS CLT_CURREG_IND,
	clt.DEATH_DT   AS CLT_DEATH_DT,  
	clt.DTH_DT_IND AS CLT_DTH_DT_IND,
	clt.DEATH_PLC  AS CLT_DEATH_PLC, 
	clt.DTH_RN_TXT AS CLT_DTH_RN_TXT,
	clt.DRV_LIC_NO AS CLT_DRV_LIC_NO,
	clt.D_STATE_C  AS CLT_D_STATE_C, 
	clt.EMAIL_ADDR AS CLT_EMAIL_ADDR,
	clt.EST_DOB_CD AS CLT_EST_DOB_CD,
	clt.ETH_UD_CD  AS CLT_ETH_UD_CD, 
	clt.FTERM_DT   AS CLT_FTERM_DT,  
	clt.GENDER_CD  AS CLT_GENDER_CD, 
	clt.HEALTH_TXT AS CLT_HEALTH_TXT,
	clt.HISP_UD_CD AS CLT_HISP_UD_CD,
	clt.HISP_CD    AS CLT_HISP_CD,   
	clt.I_CNTRY_C  AS CLT_I_CNTRY_C, 
	clt.IMGT_STC   AS CLT_IMGT_STC,  
	clt.INCAPC_CD  AS CLT_INCAPC_CD, 
	clt.HCARE_IND  AS CLT_HCARE_IND, 
	clt.LIMIT_IND  AS CLT_LIMIT_IND, 
	clt.LITRATE_CD AS CLT_LITRATE_CD,
	clt.MAR_HIST_B AS CLT_MAR_HIST_B,
	clt.MRTL_STC   AS CLT_MRTL_STC,  
	clt.MILT_STACD AS CLT_MILT_STACD,
	clt.MTERM_DT   AS CLT_MTERM_DT,  
	clt.NMPRFX_DSC AS CLT_NMPRFX_DSC,
	clt.NAME_TPC   AS CLT_NAME_TPC,  
	clt.OUTWRT_IND AS CLT_OUTWRT_IND,
	clt.PREVCA_IND AS CLT_PREVCA_IND,
	clt.POTH_DESC  AS CLT_POTH_DESC, 
	clt.PREREG_IND AS CLT_PREREG_IND,
	clt.P_ETHNCTYC AS CLT_P_ETHNCTYC,
	clt.P_LANG_TPC AS CLT_P_LANG_TPC,
	clt.RLGN_TPC   AS CLT_RLGN_TPC,  
	clt.S_LANG_TC  AS CLT_S_LANG_TC, 
	clt.SNTV_HLIND AS CLT_SNTV_HLIND,
	clt.SENSTV_IND AS CLT_SENSTV_IND,
	clt.SOCPLC_CD  AS CLT_SOCPLC_CD, 
	clt.SOC158_IND AS CLT_SOC158_IND,
	clt.SSN_CHG_CD AS CLT_SSN_CHG_CD,
	clt.SS_NO      AS CLT_SS_NO,    
	clt.SUFX_TLDSC AS CLT_SUFX_TLDSC,
	clt.TRBA_CLT_B AS CLT_TRBA_CLT_B,
	clt.TR_MBVRT_B AS CLT_TR_MBVRT_B,
	clt.UNEMPLY_CD AS CLT_UNEMPLY_CD,
	clt.ZIPPY_IND  AS CLT_ZIPPY_IND, 
	clt.IBMSNAP_LOGMARKER AS CLT_IBMSNAP_LOGMARKER,
	clt.IBMSNAP_OPERATION AS CLT_IBMSNAP_OPERATION,
	cla.IDENTIFIER AS CLA_IDENTIFIER,
	cla.LST_UPD_ID AS CLA_LST_UPD_ID,
	cla.LST_UPD_TS AS CLA_LST_UPD_TS,
	cla.ADDR_TPC   AS CLA_ADDR_TPC,  
	cla.BK_INMT_ID AS CLA_BK_INMT_ID,
	cla.EFF_END_DT AS CLA_EFF_END_DT,
	cla.EFF_STRTDT AS CLA_EFF_STRTDT,
	cla.FKADDRS_T  AS CLA_FKADDRS_T, 
	cla.FKCLIENT_T AS CLA_FKCLIENT_T,
	cla.FKREFERL_T AS CLA_FKREFERL_T,
	cla.HOMLES_IND AS CLA_HOMLES_IND,
	cla.IBMSNAP_LOGMARKER AS CLA_IBMSNAP_LOGMARKER,
	cla.IBMSNAP_OPERATION AS CLA_IBMSNAP_OPERATION,
	adr.IDENTIFIER        AS ADR_IDENTIFIER,
	adr.LST_UPD_ID        AS ADR_LST_UPD_ID,
	adr.LST_UPD_TS        AS ADR_LST_UPD_TS,
	TRIM(adr.ADDR_DSC)    AS ADR_ADDR_DSC,
	TRIM(adr.CITY_NM)     AS ADR_CITY_NM,
	adr.EMRG_EXTNO        AS ADR_EMRG_EXTNO,
	adr.EMRG_TELNO        AS ADR_EMRG_TELNO,
	adr.FRG_ADRT_B        AS ADR_FRG_ADRT_B,
	adr.GVR_ENTC          AS ADR_GVR_ENTC,  
	TRIM(adr.HEADER_ADR)  AS ADR_HEADER_ADR,
	adr.MSG_EXT_NO        AS ADR_MSG_EXT_NO,
	adr.MSG_TEL_NO        AS ADR_MSG_TEL_NO,
	TRIM(adr.POSTDIR_CD)  AS ADR_POSTDIR_CD,
	TRIM(adr.PREDIR_CD)   AS ADR_PREDIR_CD,
	adr.PRM_EXT_NO        AS ADR_PRM_EXT_NO,
	adr.PRM_TEL_NO        AS ADR_PRM_TEL_NO,
	adr.STATE_C           AS ADR_STATE_C,   
	TRIM(adr.STREET_NM)   AS ADR_STREET_NM,
	TRIM(adr.STREET_NO)   AS ADR_STREET_NO,
	adr.ST_SFX_C          AS ADR_ST_SFX_C,
	adr.UNT_DSGC          AS ADR_UNT_DSGC,
	TRIM(adr.UNIT_NO)     AS ADR_UNIT_NO,
	TRIM(adr.ZIP_NO)      AS ADR_ZIP_NO,
	adr.ZIP_SFX_NO        AS ADR_ZIP_SFX_NO,
	adr.IBMSNAP_LOGMARKER AS ADR_IBMSNAP_LOGMARKER,
	adr.IBMSNAP_OPERATION AS ADR_IBMSNAP_OPERATION,
	eth.IDENTIFIER        AS ETH_IDENTIFIER,
	eth.ETHNCTYC          AS ETHNICITY_CODE,
	eth.IBMSNAP_LOGMARKER AS ETH_IBMSNAP_LOGMARKER,
	eth.IBMSNAP_OPERATION AS ETH_IBMSNAP_OPERATION,
	clc.CLIENT_ID         AS CLC_CLIENT_ID,
	clc.GVR_ENTC          AS CLC_GVR_ENTC,
	clc.LST_UPD_TS        AS LST_UPD_TS,
	clc.LST_UPD_OP        AS CLC_LST_UPD_OP,
	clc.CNTY_RULE 		  AS CLC_CNTY_RULE
	MAX(  clt.IBMSNAP_LOGMARKER, 
	  NVL(cla.IBMSNAP_LOGMARKER, TIMESTAMP('2008-09-30 11:54:40')),
	  NVL(adr.IBMSNAP_LOGMARKER, TIMESTAMP('2008-09-30 11:54:40')),
	  NVL(eth.IBMSNAP_LOGMARKER, TIMESTAMP('2008-09-30 11:54:40')),
	  NVL(clc.LST_UPD_TS,        TIMESTAMP('2008-09-30 11:54:40'))
	) LAST_CHG
	FROM      CWSRS1.CLIENT_T    clt
	LEFT JOIN CWSRS1.CL_ADDRT    cla ON clt.IDENTIFIER = cla.FKCLIENT_T 
	LEFT JOIN CWSRS1.ADDRS_T     adr ON cla.FKADDRS_T  = adr.IDENTIFIER
	LEFT JOIN CWSRS1.CLSCP_ET    eth ON clt.IDENTIFIER = eth.ESTBLSH_ID AND eth.ESTBLSH_CD = 'C'
	LEFT JOIN CWSRS1.CLIENT_CNTY clc ON clt.IDENTIFIER = clc.CLIENT_ID
	ORDER BY clt_IDENTIFIER    -- MAINFRAME ONLY
)
DATA INITIALLY DEFERRED
REFRESH DEFERRED
DISABLE QUERY OPTIMIZATION;

COMMIT;

-- Execute following commands on linux hosts
-- SET INTEGRITY FOR MQT_CLIENT_ADDRESS MATERIALIZED QUERY IMMEDIATE UNCHECKED;

REFRESH TABLE MQT_CLIENT_ADDRESS;

COMMIT;


-- INDEX:

CREATE INDEX CWSRSQ.MQTCLPP ON CWSRSQ.MQT_CLIENT_ADDRESS (CLT_IDENTIFIER ASC, CLT_SENSTV_IND ASC) 
USING STOGROUP SMSSG000 
PRIQTY -1 SECQTY -1 
FREEPAGE 0 PCTFREE 5 
GBPCACHE CHANGED 
CLUSTER 
COMPRESS NO 
BUFFERPOOL BP8 
CLOSE NO 
COPY NO 
DEFER NO 
DEFINE YES; 

COMMIT;


  