DROP TRIGGER CWSINT.trg_case_del;

CREATE TRIGGER CWSINT.trg_case_del
AFTER DELETE ON CWSINT.CASE_T
REFERENCING OLD AS OROW
FOR EACH ROW MODE DB2SQL
BEGIN ATOMIC
	MERGE INTO CWSRS1.CASE_T adr USING (
		SELECT 
			orow.IDENTIFIER,
			orow.START_DT,
			orow.SRV_CMPC,
			orow.APRVL_NO,
			orow.APV_STC,
			orow.CLS_RSNC,
			orow.CSPL_DET_B,
			orow.CL_STM_TXT,
			orow.CNTRY_C,
			orow.NOTES_DOC,
			orow.END_DT,
			orow.GVR_ENTC,
			orow.ICPCSTAT_B,
			orow.ICPC_RQT_B,
			orow.LMT_ACSSCD,
			orow.CASE_NM,
			orow.PRJ_END_DT,
			orow.SPRJ_CST_B,
			orow.STATE_C,
			orow.TICKLE_T_B,
			orow.LST_UPD_ID,
			orow.LST_UPD_TS,
			orow.FKCHLD_CLT,
			orow.SRV_CMPDT,
			orow.FKSTFPERST,
			orow.CNTY_SPFCD,
			orow.ALERT_TXT,
			orow.FKREFERL_T,
			orow.RSP_AGY_CD,
			orow.NXT_TILPDT,
			orow.EMANCPN_DT,
			orow.L_GVR_ENTC,
			orow.LMT_ACS_DT,
			orow.LMT_ACSDSC
		FROM sysibm.sysdummy1
	) X ON (adr.IDENTIFIER = X.IDENTIFIER) 
	WHEN MATCHED THEN UPDATE SET 
		START_DT = x.START_DT,
		SRV_CMPC = x.SRV_CMPC,
		APRVL_NO = x.APRVL_NO,
		APV_STC = x.APV_STC,
		CLS_RSNC = x.CLS_RSNC,
		CSPL_DET_B = x.CSPL_DET_B,
		CL_STM_TXT = x.CL_STM_TXT,
		CNTRY_C = x.CNTRY_C,
		NOTES_DOC = x.NOTES_DOC,
		END_DT = x.END_DT,
		GVR_ENTC = x.GVR_ENTC,
		ICPCSTAT_B = x.ICPCSTAT_B,
		ICPC_RQT_B = x.ICPC_RQT_B,
		LMT_ACSSCD = x.LMT_ACSSCD,
		CASE_NM = x.CASE_NM,
		PRJ_END_DT = x.PRJ_END_DT,
		SPRJ_CST_B = x.SPRJ_CST_B,
		STATE_C = x.STATE_C,
		TICKLE_T_B = x.TICKLE_T_B,
		LST_UPD_ID = x.LST_UPD_ID,
		LST_UPD_TS = x.LST_UPD_TS,
		FKCHLD_CLT = x.FKCHLD_CLT,
		SRV_CMPDT = x.SRV_CMPDT,
		FKSTFPERST = x.FKSTFPERST,
		CNTY_SPFCD = x.CNTY_SPFCD,
		ALERT_TXT = x.ALERT_TXT,
		FKREFERL_T = x.FKREFERL_T,
		RSP_AGY_CD = x.RSP_AGY_CD,
		NXT_TILPDT = x.NXT_TILPDT,
		EMANCPN_DT = x.EMANCPN_DT,
		L_GVR_ENTC = x.L_GVR_ENTC,
		LMT_ACS_DT = x.LMT_ACS_DT,
		LMT_ACSDSC = x.LMT_ACSDSC,
		IBMSNAP_OPERATION = 'D',
		IBMSNAP_LOGMARKER = current timestamp
	WHEN NOT MATCHED THEN INSERT (
	    IDENTIFIER,
		START_DT,
		SRV_CMPC,
		APRVL_NO,
		APV_STC,
		CLS_RSNC,
		CSPL_DET_B,
		CL_STM_TXT,
		CNTRY_C,
		NOTES_DOC,
		END_DT,
		GVR_ENTC,
		ICPCSTAT_B,
		ICPC_RQT_B,
		LMT_ACSSCD,
		CASE_NM,
		PRJ_END_DT,
		SPRJ_CST_B,
		STATE_C,
		TICKLE_T_B,
		LST_UPD_ID,
		LST_UPD_TS,
		FKCHLD_CLT,
		SRV_CMPDT,
		FKSTFPERST,
		CNTY_SPFCD,
		ALERT_TXT,
		FKREFERL_T,
		RSP_AGY_CD,
		NXT_TILPDT,
		EMANCPN_DT,
		L_GVR_ENTC,
		LMT_ACS_DT,
		LMT_ACSDSC,
		IBMSNAP_OPERATION,
		IBMSNAP_LOGMARKER
	) VALUES (
		x.IDENTIFIER,
		x.START_DT,
		x.SRV_CMPC,
		x.APRVL_NO,
		x.APV_STC,
		x.CLS_RSNC,
		x.CSPL_DET_B,
		x.CL_STM_TXT,
		x.CNTRY_C,
		x.NOTES_DOC,
		x.END_DT,
		x.GVR_ENTC,
		x.ICPCSTAT_B,
		x.ICPC_RQT_B,
		x.LMT_ACSSCD,
		x.CASE_NM,
		x.PRJ_END_DT,
		x.SPRJ_CST_B,
		x.STATE_C,
		x.TICKLE_T_B,
		x.LST_UPD_ID,
		x.LST_UPD_TS,
		x.FKCHLD_CLT,
		x.SRV_CMPDT,
		x.FKSTFPERST,
		x.CNTY_SPFCD,
		x.ALERT_TXT,
		x.FKREFERL_T,
		x.RSP_AGY_CD,
		x.NXT_TILPDT,
		x.EMANCPN_DT,
		x.L_GVR_ENTC,
		x.LMT_ACS_DT,
		x.LMT_ACSDSC,
		'D',
		current timestamp
	);
END
