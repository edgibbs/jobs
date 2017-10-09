------------------------------------------------------------------------------------
-- TRIGGERS TO CALL SQL PROCEDURE GENCLNCNTY FOR FINDING THE CLIENT COUNTY AND 
-- INSERT/UPDATE CLIENT_CNTY TABLE                             
-- AUTHOR: TPT1 TEAM
------------------------------------------------------------------------------------
-- TRIGGER ON REFERL_T UPDATE
CREATE OR REPLACE TRIGGER CWSRS1.TRIG_UPD_REFR
AFTER UPDATE ON CWSRS1.REFERL_T
REFERENCING OLD AS OROW
	        NEW AS NROW
FOR EACH ROW MODE DB2SQL
BEGIN ATOMIC
	DECLARE OUTPARM CHAR(4) DEFAULT '?';
	CALL CWSRS1.GENCLNCNTY ('U', OROW.IDENTIFIER, 'REFR', OUTPARM);
END 
@ 

-- TRIGGER ON REFERL_T INSERT
CREATE OR REPLACE TRIGGER CWSRS1.TRIG_INS_REFR
AFTER INSERT ON CWSRS1.REFERL_T
REFERENCING NEW AS NROW
FOR EACH ROW MODE DB2SQL
BEGIN ATOMIC
	DECLARE OUTPARM CHAR(4) DEFAULT '?';
	CALL CWSRS1.GENCLNCNTY ('I', NROW.IDENTIFIER, 'REFR', OUTPARM);
END
@ 

-- TRIGGER ON CASE_T UPDATE
CREATE OR REPLACE TRIGGER CWSRS1.TRIG_UPD_CASE
AFTER UPDATE ON CWSRS1.CASE_T
REFERENCING OLD AS OROW
	        NEW AS NROW
FOR EACH ROW MODE DB2SQL
BEGIN ATOMIC
	DECLARE OUTPARM CHAR(4) DEFAULT '?';
	CALL CWSRS1.GENCLNCNTY ('U', OROW.IDENTIFIER, 'CASE', OUTPARM);
END
@ 

-- TRIGGER ON CASE_T INSERT
CREATE OR REPLACE TRIGGER CWSRS1.TRIG_INS_CASE
AFTER INSERT ON CWSRS1.CASE_T
REFERENCING NEW AS NROW
FOR EACH ROW MODE DB2SQL
BEGIN ATOMIC
	DECLARE OUTPARM CHAR(4) DEFAULT '?';
	CALL CWSRS1.GENCLNCNTY ('I', NROW.IDENTIFIER, 'CASE', OUTPARM);
END
@ 

-- TRIGGER ON CLIENT_T INSERT
CREATE OR REPLACE TRIGGER CWSRS1.TRIG_INS_CLIENT
AFTER INSERT ON CWSRS1.CLIENT_T
REFERENCING NEW AS NROW
FOR EACH ROW MODE DB2SQL
BEGIN ATOMIC
	DECLARE OUTPARM CHAR(4) DEFAULT '?';
	CALL CWSRS1.GENCLNCNTY ('I', NROW.IDENTIFIER, 'CLNT', OUTPARM);
END
@ 