DROP TRIGGER CWSINT.trg_coltrl_t_del;

CREATE TRIGGER CWSINT.trg_coltrl_t_del
AFTER DELETE ON CWSINT.COLTRL_T
REFERENCING OLD AS OROW
FOR EACH ROW MODE DB2SQL
BEGIN ATOMIC
	MERGE INTO CWSRS1.COLTRL_T tgt USING ( 
	SELECT
		orow.IDENTIFIER,
		orow.BADGE_NO,
		orow.CITY_NM,
		orow.EMPLYR_NM,
		orow.FAX_NO,
		orow.FIRST_NM,
		orow.FRG_ADRT_B,
		orow.LAST_NM,
		orow.MID_INI_NM,
		orow.NMPRFX_DSC,
		orow.PRM_TEL_NO,
		orow.PRM_EXT_NO,
		orow.STATE_C,
		orow.STREET_NM,
		orow.STREET_NO,
		orow.SUFX_TLDSC,
		orow.ZIP_NO,
		orow.LST_UPD_ID,
		orow.LST_UPD_TS,
		orow.ZIP_SFX_NO,
		orow.COMNT_DSC,
		orow.GENDER_CD,
		orow.BIRTH_DT,
		orow.MRTL_STC,
		orow.EMAIL_ADDR,
		orow.ESTBLSH_CD,
		orow.ESTBLSH_ID,
		orow.RESOST_IND
		FROM sysibm.sysdummy1
	) X ON (tgt.IDENTIFIER = X.IDENTIFIER)
	WHEN MATCHED THEN UPDATE SET 
		BADGE_NO = x.BADGE_NO,
		CITY_NM = x.CITY_NM,
		EMPLYR_NM = x.EMPLYR_NM,
		FAX_NO = x.FAX_NO,
		FIRST_NM = x.FIRST_NM,
		FRG_ADRT_B = x.FRG_ADRT_B,
		LAST_NM = x.LAST_NM,
		MID_INI_NM = x.MID_INI_NM,
		NMPRFX_DSC = x.NMPRFX_DSC,
		PRM_TEL_NO = x.PRM_TEL_NO,
		PRM_EXT_NO = x.PRM_EXT_NO,
		STATE_C = x.STATE_C,
		STREET_NM = x.STREET_NM,
		STREET_NO = x.STREET_NO,
		SUFX_TLDSC = x.SUFX_TLDSC,
		ZIP_NO = x.ZIP_NO,
		LST_UPD_ID = x.LST_UPD_ID,
		LST_UPD_TS = x.LST_UPD_TS,
		ZIP_SFX_NO = x.ZIP_SFX_NO,
		COMNT_DSC = x.COMNT_DSC,
		GENDER_CD = x.GENDER_CD,
		BIRTH_DT = x.BIRTH_DT,
		MRTL_STC = x.MRTL_STC,
		EMAIL_ADDR = x.EMAIL_ADDR,
		ESTBLSH_CD = x.ESTBLSH_CD,
		ESTBLSH_ID = x.ESTBLSH_ID,
		RESOST_IND = x.RESOST_IND,
		IBMSNAP_OPERATION = 'D',
		IBMSNAP_LOGMARKER = current timestamp
	WHEN NOT MATCHED THEN INSERT (
		IDENTIFIER,
		BADGE_NO,
		CITY_NM,
		EMPLYR_NM,
		FAX_NO,
		FIRST_NM,
		FRG_ADRT_B,
		LAST_NM,
		MID_INI_NM,
		NMPRFX_DSC,
		PRM_TEL_NO,
		PRM_EXT_NO,
		STATE_C,
		STREET_NM,
		STREET_NO,
		SUFX_TLDSC,
		ZIP_NO,
		LST_UPD_ID,
		LST_UPD_TS,
		ZIP_SFX_NO,
		COMNT_DSC,
		GENDER_CD,
		BIRTH_DT,
		MRTL_STC,
		EMAIL_ADDR,
		ESTBLSH_CD,
		ESTBLSH_ID,
		RESOST_IND,
		IBMSNAP_OPERATION,
		IBMSNAP_LOGMARKER
	) VALUES (
		x.IDENTIFIER,
		x.BADGE_NO,
		x.CITY_NM,
		x.EMPLYR_NM,
		x.FAX_NO,
		x.FIRST_NM,
		x.FRG_ADRT_B,
		x.LAST_NM,
		x.MID_INI_NM,
		x.NMPRFX_DSC,
		x.PRM_TEL_NO,
		x.PRM_EXT_NO,
		x.STATE_C,
		x.STREET_NM,
		x.STREET_NO,
		x.SUFX_TLDSC,
		x.ZIP_NO,
		x.LST_UPD_ID,
		x.LST_UPD_TS,
		x.ZIP_SFX_NO,
		x.COMNT_DSC,
		x.GENDER_CD,
		x.BIRTH_DT,
		x.MRTL_STC,
		x.EMAIL_ADDR,
		x.ESTBLSH_CD,
		x.ESTBLSH_ID,
		x.RESOST_IND,
		'D',
		current timestamp
	);
END
