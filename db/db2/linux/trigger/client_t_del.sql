DROP TRIGGER CWSINT.trg_client_del;

CREATE TRIGGER CWSINT.trg_client_del
AFTER DELETE ON CWSINT.CLIENT_T
REFERENCING OLD AS OROW
FOR EACH ROW MODE DB2SQL
BEGIN ATOMIC
	MERGE INTO CWSRS1.CLIENT_T tgt USING ( 
	SELECT
		orow.IDENTIFIER,
		orow.ADPTN_STCD,
		orow.ALN_REG_NO,
		orow.BIRTH_DT,
		orow.BR_FAC_NM,
		orow.B_STATE_C,
		orow.B_CNTRY_C,
		orow.CHLD_CLT_B,
		orow.COM_FST_NM,
		orow.COM_LST_NM,
		orow.COM_MID_NM,
		orow.CONF_EFIND,
		orow.CONF_ACTDT,
		orow.CREATN_DT,
		orow.DEATH_DT,
		orow.DTH_RN_TXT,
		orow.DRV_LIC_NO,
		orow.D_STATE_C,
		orow.GENDER_CD,
		orow.I_CNTRY_C,
		orow.IMGT_STC,
		orow.INCAPC_CD,
		orow.LITRATE_CD,
		orow.MAR_HIST_B,
		orow.MRTL_STC,
		orow.MILT_STACD,
		orow.NMPRFX_DSC,
		orow.NAME_TPC,
		orow.OUTWRT_IND,
		orow.P_ETHNCTYC,
		orow.P_LANG_TPC,
		orow.RLGN_TPC,
		orow.S_LANG_TC,
		orow.SENSTV_IND,
		orow.SNTV_HLIND,
		orow.SS_NO,
		orow.SSN_CHG_CD,
		orow.SUFX_TLDSC,
		orow.UNEMPLY_CD,
		orow.LST_UPD_ID,
		orow.LST_UPD_TS,
		orow.COMMNT_DSC,
		orow.EST_DOB_CD,
		orow.BP_VER_IND,
		orow.HISP_CD,
		orow.CURRCA_IND,
		orow.CURREG_IND,
		orow.COTH_DESC,
		orow.PREVCA_IND,
		orow.PREREG_IND,
		orow.POTH_DESC,
		orow.HCARE_IND,
		orow.LIMIT_IND,
		orow.BIRTH_CITY,
		orow.HEALTH_TXT,
		orow.MTERM_DT,
		orow.FTERM_DT,
		orow.ZIPPY_IND,
		orow.DEATH_PLC,
		orow.TR_MBVRT_B,
		orow.TRBA_CLT_B,
		orow.SOC158_IND,
		orow.DTH_DT_IND,
		orow.EMAIL_ADDR,
		orow.ADJDEL_IND,
		orow.ETH_UD_CD,
		orow.HISP_UD_CD,
		orow.SOCPLC_CD,
		orow.CL_INDX_NO
		FROM sysibm.sysdummy1
	) X ON (tgt.IDENTIFIER = X.IDENTIFIER)
	WHEN MATCHED THEN UPDATE SET 
		ADPTN_STCD = x.ADPTN_STCD,
		ALN_REG_NO = x.ALN_REG_NO,
		BIRTH_DT = x.BIRTH_DT,
		BR_FAC_NM = x.BR_FAC_NM,
		B_STATE_C = x.B_STATE_C,
		B_CNTRY_C = x.B_CNTRY_C,
		CHLD_CLT_B = x.CHLD_CLT_B,
		COM_FST_NM = x.COM_FST_NM,
		COM_LST_NM = x.COM_LST_NM,
		COM_MID_NM = x.COM_MID_NM,
		CONF_EFIND = x.CONF_EFIND,
		CONF_ACTDT = x.CONF_ACTDT,
		CREATN_DT = x.CREATN_DT,
		DEATH_DT = x.DEATH_DT,
		DTH_RN_TXT = x.DTH_RN_TXT,
		DRV_LIC_NO = x.DRV_LIC_NO,
		D_STATE_C = x.D_STATE_C,
		GENDER_CD = x.GENDER_CD,
		I_CNTRY_C = x.I_CNTRY_C,
		IMGT_STC = x.IMGT_STC,
		INCAPC_CD = x.INCAPC_CD,
		LITRATE_CD = x.LITRATE_CD,
		MAR_HIST_B = x.MAR_HIST_B,
		MRTL_STC = x.MRTL_STC,
		MILT_STACD = x.MILT_STACD,
		NMPRFX_DSC = x.NMPRFX_DSC,
		NAME_TPC = x.NAME_TPC,
		OUTWRT_IND = x.OUTWRT_IND,
		P_ETHNCTYC = x.P_ETHNCTYC,
		P_LANG_TPC = x.P_LANG_TPC,
		RLGN_TPC = x.RLGN_TPC,
		S_LANG_TC = x.S_LANG_TC,
		SENSTV_IND = x.SENSTV_IND,
		SNTV_HLIND = x.SNTV_HLIND,
		SS_NO = x.SS_NO,
		SSN_CHG_CD = x.SSN_CHG_CD,
		SUFX_TLDSC = x.SUFX_TLDSC,
		UNEMPLY_CD = x.UNEMPLY_CD,
		LST_UPD_ID = x.LST_UPD_ID,
		LST_UPD_TS = x.LST_UPD_TS,
		COMMNT_DSC = x.COMMNT_DSC,
		EST_DOB_CD = x.EST_DOB_CD,
		BP_VER_IND = x.BP_VER_IND,
		HISP_CD = x.HISP_CD,
		CURRCA_IND = x.CURRCA_IND,
		CURREG_IND = x.CURREG_IND,
		COTH_DESC = x.COTH_DESC,
		PREVCA_IND = x.PREVCA_IND,
		PREREG_IND = x.PREREG_IND,
		POTH_DESC = x.POTH_DESC,
		HCARE_IND = x.HCARE_IND,
		LIMIT_IND = x.LIMIT_IND,
		BIRTH_CITY = x.BIRTH_CITY,
		HEALTH_TXT = x.HEALTH_TXT,
		MTERM_DT = x.MTERM_DT,
		FTERM_DT = x.FTERM_DT,
		ZIPPY_IND = x.ZIPPY_IND,
		DEATH_PLC = x.DEATH_PLC,
		TR_MBVRT_B = x.TR_MBVRT_B,
		TRBA_CLT_B = x.TRBA_CLT_B,
		SOC158_IND = x.SOC158_IND,
		DTH_DT_IND = x.DTH_DT_IND,
		EMAIL_ADDR = x.EMAIL_ADDR,
		ADJDEL_IND = x.ADJDEL_IND,
		ETH_UD_CD = x.ETH_UD_CD,
		HISP_UD_CD = x.HISP_UD_CD,
		SOCPLC_CD = x.SOCPLC_CD,
		CL_INDX_NO = x.CL_INDX_NO,
		IBMSNAP_OPERATION = 'D',
		IBMSNAP_LOGMARKER = current timestamp
	WHEN NOT MATCHED THEN INSERT (
		IDENTIFIER,
		ADPTN_STCD,
		ALN_REG_NO,
		BIRTH_DT,
		BR_FAC_NM,
		B_STATE_C,
		B_CNTRY_C,
		CHLD_CLT_B,
		COM_FST_NM,
		COM_LST_NM,
		COM_MID_NM,
		CONF_EFIND,
		CONF_ACTDT,
		CREATN_DT,
		DEATH_DT,
		DTH_RN_TXT,
		DRV_LIC_NO,
		D_STATE_C,
		GENDER_CD,
		I_CNTRY_C,
		IMGT_STC,
		INCAPC_CD,
		LITRATE_CD,
		MAR_HIST_B,
		MRTL_STC,
		MILT_STACD,
		NMPRFX_DSC,
		NAME_TPC,
		OUTWRT_IND,
		P_ETHNCTYC,
		P_LANG_TPC,
		RLGN_TPC,
		S_LANG_TC,
		SENSTV_IND,
		SNTV_HLIND,
		SS_NO,
		SSN_CHG_CD,
		SUFX_TLDSC,
		UNEMPLY_CD,
		LST_UPD_ID,
		LST_UPD_TS,
		COMMNT_DSC,
		EST_DOB_CD,
		BP_VER_IND,
		HISP_CD,
		CURRCA_IND,
		CURREG_IND,
		COTH_DESC,
		PREVCA_IND,
		PREREG_IND,
		POTH_DESC,
		HCARE_IND,
		LIMIT_IND,
		BIRTH_CITY,
		HEALTH_TXT,
		MTERM_DT,
		FTERM_DT,
		ZIPPY_IND,
		DEATH_PLC,
		TR_MBVRT_B,
		TRBA_CLT_B,
		SOC158_IND,
		DTH_DT_IND,
		EMAIL_ADDR,
		ADJDEL_IND,
		ETH_UD_CD,
		HISP_UD_CD,
		SOCPLC_CD,
		CL_INDX_NO,
		IBMSNAP_OPERATION,
		IBMSNAP_LOGMARKER
	) VALUES (
		x.IDENTIFIER,
		x.ADPTN_STCD,
		x.ALN_REG_NO,
		x.BIRTH_DT,
		x.BR_FAC_NM,
		x.B_STATE_C,
		x.B_CNTRY_C,
		x.CHLD_CLT_B,
		x.COM_FST_NM,
		x.COM_LST_NM,
		x.COM_MID_NM,
		x.CONF_EFIND,
		x.CONF_ACTDT,
		x.CREATN_DT,
		x.DEATH_DT,
		x.DTH_RN_TXT,
		x.DRV_LIC_NO,
		x.D_STATE_C,
		x.GENDER_CD,
		x.I_CNTRY_C,
		x.IMGT_STC,
		x.INCAPC_CD,
		x.LITRATE_CD,
		x.MAR_HIST_B,
		x.MRTL_STC,
		x.MILT_STACD,
		x.NMPRFX_DSC,
		x.NAME_TPC,
		x.OUTWRT_IND,
		x.P_ETHNCTYC,
		x.P_LANG_TPC,
		x.RLGN_TPC,
		x.S_LANG_TC,
		x.SENSTV_IND,
		x.SNTV_HLIND,
		x.SS_NO,
		x.SSN_CHG_CD,
		x.SUFX_TLDSC,
		x.UNEMPLY_CD,
		x.LST_UPD_ID,
		x.LST_UPD_TS,
		x.COMMNT_DSC,
		x.EST_DOB_CD,
		x.BP_VER_IND,
		x.HISP_CD,
		x.CURRCA_IND,
		x.CURREG_IND,
		x.COTH_DESC,
		x.PREVCA_IND,
		x.PREREG_IND,
		x.POTH_DESC,
		x.HCARE_IND,
		x.LIMIT_IND,
		x.BIRTH_CITY,
		x.HEALTH_TXT,
		x.MTERM_DT,
		x.FTERM_DT,
		x.ZIPPY_IND,
		x.DEATH_PLC,
		x.TR_MBVRT_B,
		x.TRBA_CLT_B,
		x.SOC158_IND,
		x.DTH_DT_IND,
		x.EMAIL_ADDR,
		x.ADJDEL_IND,
		x.ETH_UD_CD,
		x.HISP_UD_CD,
		x.SOCPLC_CD,
		x.CL_INDX_NO,
		'D',
		current timestamp
	);
END
