DROP TRIGGER CWSINT.trg_oth_kidt_ins;

CREATE TRIGGER CWSINT.trg_oth_kidt_ins
AFTER INSERT ON CWSINT.OTH_KIDT
REFERENCING NEW AS NROW
FOR EACH ROW MODE DB2SQL
BEGIN ATOMIC
	MERGE INTO CWSRS1.OTH_KIDT tgt USING ( 
	SELECT
		nrow.IDENTIFIER,
		nrow.BIRTH_DT,
		nrow.GENDER_CD,
		nrow.OTHCHLD_NM,
		nrow.LST_UPD_ID,
		nrow.LST_UPD_TS,
		nrow.FKPLC_HM_T,
		nrow.YR_INC_AMT
		FROM sysibm.sysdummy1
	) X ON (tgt.IDENTIFIER = X.IDENTIFIER)
	WHEN MATCHED THEN UPDATE SET 
		IDENTIFIER = x.IDENTIFIER,
		BIRTH_DT = x.BIRTH_DT,
		GENDER_CD = x.GENDER_CD,
		OTHCHLD_NM = x.OTHCHLD_NM,
		LST_UPD_ID = x.LST_UPD_ID,
		LST_UPD_TS = x.LST_UPD_TS,
		FKPLC_HM_T = x.FKPLC_HM_T,
		YR_INC_AMT = x.YR_INC_AMT,
		IBMSNAP_OPERATION = 'I',
		IBMSNAP_LOGMARKER = current timestamp
	WHEN NOT MATCHED THEN INSERT (
		IDENTIFIER,
		BIRTH_DT,
		GENDER_CD,
		OTHCHLD_NM,
		LST_UPD_ID,
		LST_UPD_TS,
		FKPLC_HM_T,
		YR_INC_AMT,
		IBMSNAP_OPERATION,
		IBMSNAP_LOGMARKER
	) VALUES (
		x.IDENTIFIER,
		x.BIRTH_DT,
		x.GENDER_CD,
		x.OTHCHLD_NM,
		x.LST_UPD_ID,
		x.LST_UPD_TS,
		x.FKPLC_HM_T,
		x.YR_INC_AMT,
		'I',
		current timestamp
	);
END