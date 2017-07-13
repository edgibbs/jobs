--==========================
-- TRIGGER TESTS:
--==========================

--==========================
-- ADDRS_T:
--==========================

----------------------------
-- EXISTS: DELETE:
----------------------------

BEGIN ATOMIC

	DECLARE v_tgt_id  char(10) ;
	DECLARE v_op      char(1);
	DECLARE v_chg     char(20);

	SET v_tgt_id = '0p2D1jW06s';
	SET v_chg = (SELECT r.CITY_NM FROM CWSINT.ADDRS_T r WHERE r.IDENTIFIER = v_tgt_id);
	
	IF (v_chg IS NULL) THEN
		SIGNAL SQLSTATE '70002'
		SET MESSAGE_TEXT = 'Failed: verify existing';
	END IF;

	DELETE FROM CWSINT.ADDRS_T r WHERE r.IDENTIFIER = v_tgt_id ;
	SET (v_op, v_chg) = (SELECT r.IBMSNAP_OPERATION, r.CITY_NM FROM CWSRS1.ADDRS_T r WHERE r.IDENTIFIER = v_tgt_id);
	
	IF (v_op != 'D') THEN
		SIGNAL SQLSTATE '70002'
		SET MESSAGE_TEXT = 'Failed: delete existing';
	END IF;

END;

rollback;


----------------------------
-- EXISTS: UPDATE:
----------------------------

BEGIN ATOMIC

	DECLARE v_tgt_id  char(10);
	DECLARE v_op      char(1);
	DECLARE v_chg     char(20);

	SET v_tgt_id = '0p2D1jW06s';
	
	UPDATE CWSINT.ADDRS_T r
	SET 
	    r.CITY_NM      = 'Weed',
	    r.ZIP_NO       = '96094',
	    r.LST_UPD_ID   = '0x5',
	    r.LST_UPD_TS   = current timestamp
	WHERE r.IDENTIFIER = v_tgt_id ;

	SET (v_op, v_chg) = (SELECT r.IBMSNAP_OPERATION, r.CITY_NM FROM CWSRS1.ADDRS_T r WHERE r.IDENTIFIER = v_tgt_id);
	
	IF (v_op != 'U' OR v_chg != 'Weed') THEN
		SIGNAL SQLSTATE '70002'
		SET MESSAGE_TEXT = 'Failed: update existing';
	END IF;

END;

rollback;


----------------------------
-- EXISTS: UPDATE, DELETE:
----------------------------

BEGIN ATOMIC

	DECLARE v_tgt_id char(10);
	DECLARE v_op     char(1);
	DECLARE v_chg    char(20);

	SET v_tgt_id = '0p2D1jW06s';
	
	UPDATE CWSINT.ADDRS_T r
	SET 
	    r.CITY_NM      = 'Weed',
	    r.ZIP_NO       = '96094',
	    r.LST_UPD_ID   = '0x5',
	    r.LST_UPD_TS   = current timestamp
	WHERE r.IDENTIFIER = v_tgt_id ;

	DELETE FROM CWSINT.ADDRS_T r WHERE r.IDENTIFIER = v_tgt_id ;

	SET (v_op, v_chg) = (SELECT r.IBMSNAP_OPERATION, r.CITY_NM FROM CWSRS1.ADDRS_T r WHERE r.IDENTIFIER = v_tgt_id);
	
	IF (v_op != 'D' OR v_chg != 'Weed') THEN
		SIGNAL SQLSTATE '70002'
		SET MESSAGE_TEXT = 'Failed: update/delete existing';
	END IF;

END;

rollback;


----------------------------
-- NEW: INSERT, UPDATE:
----------------------------

BEGIN ATOMIC

	DECLARE v_tgt_id  char(10);
	DECLARE v_src_id  char(10);
	DECLARE v_op      char(1);
	DECLARE v_chg     char(20);

	SET v_src_id = '0p2D1jW06s';
	SET v_tgt_id = '9p291jX96s';
	
	INSERT INTO CWSINT.ADDRS_T (IDENTIFIER, CITY_NM, EMRG_TELNO, EMRG_EXTNO, FRG_ADRT_B, GVR_ENTC, MSG_TEL_NO, MSG_EXT_NO, HEADER_ADR, PRM_TEL_NO, PRM_EXT_NO, STATE_C, STREET_NM, STREET_NO, ZIP_NO, LST_UPD_ID, LST_UPD_TS, ADDR_DSC, ZIP_SFX_NO, POSTDIR_CD, PREDIR_CD, ST_SFX_C, UNT_DSGC, UNIT_NO)
	SELECT 
	    v_tgt_id as IDENTIFIER,
		CITY_NM,
		EMRG_TELNO,
		EMRG_EXTNO,
		FRG_ADRT_B,
		GVR_ENTC,
		MSG_TEL_NO,
		MSG_EXT_NO,
		HEADER_ADR,
		PRM_TEL_NO,
		PRM_EXT_NO,
		STATE_C,
		STREET_NM,
		STREET_NO,
		ZIP_NO,
		LST_UPD_ID,
		LST_UPD_TS,
		ADDR_DSC,
		ZIP_SFX_NO,
		POSTDIR_CD,
		PREDIR_CD,
		ST_SFX_C,
		UNT_DSGC,
		UNIT_NO
	FROM CWSINT.ADDRS_T x
	WHERE x.IDENTIFIER = v_src_id ;
	
	UPDATE CWSINT.ADDRS_T r
	SET 
	    r.CITY_NM      = 'Weed',
	    r.ZIP_NO       = '96094',
	    r.LST_UPD_ID   = '0x5',
	    r.LST_UPD_TS   = current timestamp
	WHERE r.IDENTIFIER = v_tgt_id ;

	SET (v_op, v_chg) = (SELECT r.IBMSNAP_OPERATION, r.CITY_NM FROM CWSRS1.ADDRS_T r WHERE r.IDENTIFIER = v_tgt_id);
	
	IF (v_op != 'U' OR v_chg != 'Weed') THEN
		SIGNAL SQLSTATE '70002'
		SET MESSAGE_TEXT = 'Failed: insert new';
	END IF;

END;

rollback;




----------------------------
-- NEW: INSERT, UPDATE:
----------------------------

BEGIN ATOMIC

	DECLARE v_tgt_id  char(10);
	DECLARE v_src_id  char(10);
	DECLARE v_op      char(1);
	DECLARE v_chg     char(20);

	SET v_src_id = '0p2D1jW06s';
	SET v_tgt_id = '9p291jX96s';
	
	INSERT INTO CWSINT.ADDRS_T (IDENTIFIER, CITY_NM, EMRG_TELNO, EMRG_EXTNO, FRG_ADRT_B, GVR_ENTC, MSG_TEL_NO, MSG_EXT_NO, HEADER_ADR, PRM_TEL_NO, PRM_EXT_NO, STATE_C, STREET_NM, STREET_NO, ZIP_NO, LST_UPD_ID, LST_UPD_TS, ADDR_DSC, ZIP_SFX_NO, POSTDIR_CD, PREDIR_CD, ST_SFX_C, UNT_DSGC, UNIT_NO)
	SELECT 
	    v_tgt_id as IDENTIFIER,
		CITY_NM,
		EMRG_TELNO,
		EMRG_EXTNO,
		FRG_ADRT_B,
		GVR_ENTC,
		MSG_TEL_NO,
		MSG_EXT_NO,
		HEADER_ADR,
		PRM_TEL_NO,
		PRM_EXT_NO,
		STATE_C,
		STREET_NM,
		STREET_NO,
		ZIP_NO,
		LST_UPD_ID,
		LST_UPD_TS,
		ADDR_DSC,
		ZIP_SFX_NO,
		POSTDIR_CD,
		PREDIR_CD,
		ST_SFX_C,
		UNT_DSGC,
		UNIT_NO
	FROM CWSINT.ADDRS_T x
	WHERE x.IDENTIFIER = v_src_id ;
	
	SET (v_op, v_chg) = (SELECT r.IBMSNAP_OPERATION, r.CITY_NM FROM CWSRS1.ADDRS_T r WHERE r.IDENTIFIER = v_tgt_id);
	
	IF (v_op != 'I') THEN
		SIGNAL SQLSTATE '70002'
		SET MESSAGE_TEXT = 'Failed: insert new';
	END IF;

END;

rollback;











