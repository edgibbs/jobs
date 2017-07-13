DROP TRIGGER CWSINT.trg_edprvprt_del;

CREATE TRIGGER CWSINT.trg_edprvprt_del
AFTER DELETE ON CWSINT.EDPRVPRT
REFERENCING OLD AS OROW
FOR EACH ROW MODE DB2SQL
BEGIN ATOMIC
	MERGE INTO CWSRS1.EDPRVPRT tgt USING ( 
		SELECT
		orow.IDENTIFIER,
		orow.EDUPRVCD,
		orow.LST_UPD_ID,
		orow.LST_UPD_TS,
		orow.FKED_PVDRT
		FROM sysibm.sysdummy1
	) X ON (tgt.IDENTIFIER = X.IDENTIFIER)
	WHEN MATCHED THEN UPDATE SET 
		EDUPRVCD = x.EDUPRVCD,
		LST_UPD_ID = x.LST_UPD_ID,
		LST_UPD_TS = x.LST_UPD_TS,
		FKED_PVDRT = x.FKED_PVDRT,
		IBMSNAP_OPERATION = 'D',
		IBMSNAP_LOGMARKER = current timestamp
	WHEN NOT MATCHED THEN INSERT (
		IDENTIFIER,
		EDUPRVCD,
		LST_UPD_ID,
		LST_UPD_TS,
		FKED_PVDRT,
		IBMSNAP_OPERATION,
		IBMSNAP_LOGMARKER
	) VALUES (
		x.IDENTIFIER,
		x.EDUPRVCD,
		x.LST_UPD_ID,
		x.LST_UPD_TS,
		x.FKED_PVDRT,
		'D',
		current timestamp
	);
END
