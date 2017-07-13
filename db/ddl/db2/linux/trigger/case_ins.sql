DROP TRIGGER CWSINT.trg_case_ins;

CREATE TRIGGER CWSINT.trg_case_ins
AFTER INSERT ON CWSINT.CASE_T
REFERENCING NEW AS NROW
FOR EACH ROW MODE DB2SQL
BEGIN ATOMIC
INSERT INTO CWSRS1.CASE_T (
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
	nrow.IDENTIFIER,
	nrow.START_DT,
	nrow.SRV_CMPC,
	nrow.APRVL_NO,
	nrow.APV_STC,
	nrow.CLS_RSNC,
	nrow.CSPL_DET_B,
	nrow.CL_STM_TXT,
	nrow.CNTRY_C,
	nrow.NOTES_DOC,
	nrow.END_DT,
	nrow.GVR_ENTC,
	nrow.ICPCSTAT_B,
	nrow.ICPC_RQT_B,
	nrow.LMT_ACSSCD,
	nrow.CASE_NM,
	nrow.PRJ_END_DT,
	nrow.SPRJ_CST_B,
	nrow.STATE_C,
	nrow.TICKLE_T_B,
	nrow.LST_UPD_ID,
	nrow.LST_UPD_TS,
	nrow.FKCHLD_CLT,
	nrow.SRV_CMPDT,
	nrow.FKSTFPERST,
	nrow.CNTY_SPFCD,
	nrow.ALERT_TXT,
	nrow.FKREFERL_T,
	nrow.RSP_AGY_CD,
	nrow.NXT_TILPDT,
	nrow.EMANCPN_DT,
	nrow.L_GVR_ENTC,
	nrow.LMT_ACS_DT,
	nrow.LMT_ACSDSC,
	'I',
	current timestamp
);
END
