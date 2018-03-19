package gov.ca.cwds.neutron.rocket.cases;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.rocket.CaseRocket;

/**
 * SQL statements for the Cases rocket.
 * 
 * <p>
 * Case History only displays cases for <strong>active</strong> relationship types.
 * </p>
 * 
 * <table summary="Relationships">
 * <tr>
 * <th align="justify">Legacy code id</th>
 * <th align="justify">Relationship</th>
 * </tr>
 * <tr>
 * <td align="justify">179</td>
 * <td align="justify">Brother/Brother</td>
 * </tr>
 * <tr>
 * <td>180</td>
 * <td>Brother/Brother (Half)</td>
 * </tr>
 * <tr>
 * <td>181</td>
 * <td>Brother/Brother (Step)</td>
 * </tr>
 * <tr>
 * <td>182</td>
 * <td>Brother/Sister</td>
 * </tr>
 * <tr>
 * <td>183</td>
 * <td>Brother/Sister (Half)</td>
 * </tr>
 * <tr>
 * <td>184</td>
 * <td>Brother/Sister (Step)</td>
 * </tr>
 * <tr>
 * <td>188</td>
 * <td>Daughter/Father (Adoptive)</td>
 * </tr>
 * <tr>
 * <td>189</td>
 * <td>Daughter/Father (Alleged)</td>
 * </tr>
 * <tr>
 * <td>190</td>
 * <td>Daughter/Father (Birth)</td>
 * </tr>
 * <tr>
 * <td>192</td>
 * <td>Daughter/Father (Presumed)</td>
 * </tr>
 * <tr>
 * <td>193</td>
 * <td>Daughter/Father (Step)</td>
 * </tr>
 * <tr>
 * <td>194</td>
 * <td>Daughter/Mother (Adoptive)</td>
 * </tr>
 * <tr>
 * <td>195</td>
 * <td>Daughter/Mother (Alleged)</td>
 * </tr>
 * <tr>
 * <td>196</td>
 * <td>Daughter/Mother (Birth)</td>
 * </tr>
 * <tr>
 * <td>198</td>
 * <td>Daughter/Mother (Presumed)</td>
 * </tr>
 * <tr>
 * <td>199</td>
 * <td>Daughter/Mother (Step)</td>
 * </tr>
 * <tr>
 * <td>203</td>
 * <td>Father/Daughter (Adoptive)</td>
 * </tr>
 * <tr>
 * <td>204</td>
 * <td>Father/Daughter (Alleged)</td>
 * </tr>
 * <tr>
 * <td>205</td>
 * <td>Father/Daughter (Birth)</td>
 * </tr>
 * <tr>
 * <td>207</td>
 * <td>Father/Daughter (Presumed)</td>
 * </tr>
 * <tr>
 * <td>208</td>
 * <td>Father/Daughter (Step)</td>
 * </tr>
 * <tr>
 * <td>209</td>
 * <td>Father/Son (Adoptive)</td>
 * </tr>
 * <tr>
 * <td>210</td>
 * <td>Father/Son (Alleged)</td>
 * </tr>
 * <tr>
 * <td>211</td>
 * <td>Father/Son (Birth)</td>
 * </tr>
 * <tr>
 * <td>213</td>
 * <td>Father/Son (Presumed)</td>
 * </tr>
 * <tr>
 * <td>214</td>
 * <td>Father/Son (Step)</td>
 * </tr>
 * <tr>
 * <td>242</td>
 * <td>Indian Child/Indian Custodian</td>
 * </tr>
 * <tr>
 * <td>243</td>
 * <td>Indian Custodian/Indian Child</td>
 * </tr>
 * <tr>
 * <td>245</td>
 * <td>Mother/Daughter (Adoptive)</td>
 * </tr>
 * <tr>
 * <td>246</td>
 * <td>Mother/Daughter (Alleged)</td>
 * </tr>
 * <tr>
 * <td>247</td>
 * <td>Mother/Daughter (Birth)</td>
 * </tr>
 * <tr>
 * <td>5620</td>
 * <td>Mother/Daughter (Presumed)</td>
 * </tr>
 * <tr>
 * <td>249</td>
 * <td>Mother/Daughter (Step)</td>
 * </tr>
 * <tr>
 * <td>250</td>
 * <td>Mother/Son (Adoptive)</td>
 * </tr>
 * <tr>
 * <td>251</td>
 * <td>Mother/Son (Alleged)</td>
 * </tr>
 * <tr>
 * <td>252</td>
 * <td>Mother/Son (Birth)</td>
 * </tr>
 * <tr>
 * <td>6361</td>
 * <td>Mother/Son (Presumed)</td>
 * </tr>
 * <tr>
 * <td>254</td>
 * <td>Mother/Son (Step)</td>
 * </tr>
 * <tr>
 * <td>276</td>
 * <td>Sister/Brother</td>
 * </tr>
 * <tr>
 * <td>277</td>
 * <td>Sister/Brother (Half)</td>
 * </tr>
 * <tr>
 * <td>278</td>
 * <td>Sister/Brother (Step)</td>
 * </tr>
 * <tr>
 * <td>279</td>
 * <td>Sister/Sister</td>
 * </tr>
 * <tr>
 * <td>280</td>
 * <td>Sister/Sister (Half)</td>
 * </tr>
 * <tr>
 * <td>281</td>
 * <td>Sister/Sister (Step)</td>
 * </tr>
 * <tr>
 * <td>283</td>
 * <td>Son/Father (Adoptive)</td>
 * </tr>
 * <tr>
 * <td>284</td>
 * <td>Son/Father (Alleged)</td>
 * </tr>
 * <tr>
 * <td>285</td>
 * <td>Son/Father (Birth)</td>
 * </tr>
 * <tr>
 * <td>287</td>
 * <td>Son/Father (Presumed)</td>
 * </tr>
 * <tr>
 * <td>288</td>
 * <td>Son/Father (Step)</td>
 * </tr>
 * <tr>
 * <td>289</td>
 * <td>Son/Mother (Adoptive)</td>
 * </tr>
 * <tr>
 * <td>290</td>
 * <td>Son/Mother (Alleged)</td>
 * </tr>
 * <tr>
 * <td>291</td>
 * <td>Son/Mother (Birth)</td>
 * </tr>
 * <tr>
 * <td>6360</td>
 * <td>Son/Mother (Presumed)</td>
 * </tr>
 * <tr>
 * <td>293</td>
 * <td>Son/Mother (Step)</td>
 * </tr>
 * <tr>
 * <td>301</td>
 * <td>Ward/Guardian</td>
 * </tr>
 * 
 * @author CWDS API Team
 * @see CaseRocket
 */
public class CaseSQLResource implements ApiMarker {

  private static final long serialVersionUID = 1L;

  private static final String INCLUDED_RELATIONSHIPS =
      "179,180,181,182,183,184,188,189,190,192,193,194,195,196,198,199,203,204,205,207,208,209,210,211,213,214,242,243,245,246,247,249,250,251,252,254,276,277,278,279,280,281,283,284,285,287,288,289,290,291,293,301,5620,6360,6361";

  //@formatter:off
  public static final String PREP_AFFECTED_CLIENTS_FULL =
       "INSERT INTO GT_ID (IDENTIFIER) \n"
          + "WITH DRIVER AS ( \n"
          + " SELECT c.IDENTIFIER \n"
          + " FROM CLIENT_T C \n"
          + " WHERE c.IDENTIFIER BETWEEN ? AND ? \n"
          + "   AND c.IBMSNAP_OPERATION IN ('I','U') \n"
          + ") \n"
       + "SELECT DISTINCT CAS1.FKCHLD_CLT AS CLIENT_ID \n"
          + "FROM DRIVER d1 \n"
          + "JOIN CASE_T CAS1   ON CAS1.FKCHLD_CLT = d1.IDENTIFIER \n"
       + "UNION SELECT DISTINCT REL2.FKCLIENT_0  AS CLIENT_ID \n"
          + "FROM DRIVER d2 \n"
          + "JOIN CLN_RELT REL2 ON REL2.FKCLIENT_T = d2.IDENTIFIER \n"
          + "JOIN CASE_T   CAS2 ON CAS2.FKCHLD_CLT = REL2.FKCLIENT_0 \n"
          + "WHERE REL2.CLNTRELC IN (" + INCLUDED_RELATIONSHIPS + ") \n"
          + "AND REL2.END_DT IS NULL "
       + "UNION SELECT DISTINCT REL3.FKCLIENT_T  AS CLIENT_ID \n"
          + "FROM DRIVER d3 \n"
          + "JOIN CLN_RELT REL3 ON REL3.FKCLIENT_0 = d3.IDENTIFIER \n"
          + "JOIN CASE_T   CAS3 ON CAS3.FKCHLD_CLT = REL3.FKCLIENT_T  \n"
          + "WHERE REL3.CLNTRELC IN (" + INCLUDED_RELATIONSHIPS + ") \n"
          + "AND REL3.END_DT IS NULL "
       + "UNION SELECT DISTINCT REL4.FKCLIENT_T  AS CLIENT_ID \n"
          + "FROM DRIVER d4 \n"
          + "JOIN CLN_RELT REL4 ON REL4.FKCLIENT_T = d4.IDENTIFIER \n"
          + "JOIN CASE_T   CAS4 ON CAS4.FKCHLD_CLT = REL4.FKCLIENT_0 \n"
          + "WHERE REL4.CLNTRELC IN (" + INCLUDED_RELATIONSHIPS + ") \n"
          + "AND REL4.END_DT IS NULL "
       + "UNION SELECT DISTINCT REL5.FKCLIENT_0  AS CLIENT_ID \n"
          + "FROM DRIVER d5 \n"
          + "JOIN CLN_RELT REL5 ON REL5.FKCLIENT_0 = d5.IDENTIFIER \n"
          + "JOIN CASE_T   CAS5 ON CAS5.FKCHLD_CLT = REL5.FKCLIENT_T \n"
          + "WHERE REL5.CLNTRELC IN (" + INCLUDED_RELATIONSHIPS + ") \n"
          + "AND REL5.END_DT IS NULL ";
  //@formatter:on

  //@formatter:off
  public static final String INSERT_CLIENT_CASE =
        "INSERT INTO GT_REFR_CLT (FKCLIENT_T, FKREFERL_T, SENSTV_IND) \n"
        + "WITH DRIVER AS ( \n"
          + " SELECT CAS1.IDENTIFIER    AS CASE_ID,  \n"
          + "        CAS1.FKCHLD_CLT    AS THIS_CLIENT_ID,  \n"
          + "        CAS1.FKCHLD_CLT    AS FOCUS_CHILD_ID \n"
          + " FROM CASE_T CAS1 \n"
          + " WHERE CAS1.FKCHLD_CLT IN (SELECT gt1.IDENTIFIER FROM GT_ID gt1) \n"
          + " UNION ALL \n"
          + " SELECT CAS2.IDENTIFIER    AS CASE_ID, \n"
          + "        REL2.FKCLIENT_T    AS THIS_CLIENT_ID, \n"
          + "        CAS2.FKCHLD_CLT    AS FOCUS_CHILD_ID \n"
          + " FROM CLN_RELT REL2, CASE_T CAS2 \n"
          + " WHERE CAS2.FKCHLD_CLT = REL2.FKCLIENT_0  \n"
          + "   AND REL2.FKCLIENT_T IN (SELECT gt2.IDENTIFIER FROM GT_ID gt2) \n"
          + "   AND REL2.CLNTRELC IN (" + INCLUDED_RELATIONSHIPS + ") \n"
          + "   AND REL2.END_DT IS NULL \n"
          + " UNION ALL \n"
          + " SELECT CAS3.IDENTIFIER    AS CASE_ID, \n"
          + "        REL3.FKCLIENT_0    AS THIS_CLIENT_ID, \n"
          + "        CAS3.FKCHLD_CLT    AS FOCUS_CHILD_ID \n"
          + " FROM CLN_RELT REL3, CASE_T CAS3 \n"
          + " WHERE CAS3.FKCHLD_CLT = REL3.FKCLIENT_T  \n"
          + "   AND REL3.FKCLIENT_0 IN (SELECT gt3.IDENTIFIER FROM GT_ID gt3) \n"
          + "   AND REL3.CLNTRELC IN (" + INCLUDED_RELATIONSHIPS + ") \n"
          + "   AND REL3.END_DT IS NULL \n"
          + ") \n"
        + "SELECT DISTINCT d1.THIS_CLIENT_ID AS CLIENT_ID, d1.CASE_ID, 'X' AS SENSTV_IND \n"
        + "FROM DRIVER D1 \n"
        + "UNION \n"
        + "SELECT DISTINCT d2.FOCUS_CHILD_ID AS CLIENT_ID, d2.CASE_ID, 'X' AS SENSTV_IND \n"
        + "FROM DRIVER D2 ";
  //@formatter:on

  //@formatter:off
  public static final String SELECT_CLIENT_CASE =
      "SELECT DISTINCT rc.FKCLIENT_T AS CLIENT_ID, rc.FKREFERL_T AS CASE_ID FROM GT_REFR_CLT rc ORDER BY 1,2 WITH UR ";
  //@formatter:on

  //@formatter:off
  public static final String SELECT_CASE =
          "WITH DRIVER AS ( \n"
        + "   SELECT DISTINCT rc.FKREFERL_T AS CASE_ID FROM GT_REFR_CLT rc \n"
        + ") \n"
        + "SELECT DISTINCT    \n"
        +   " CAS.IDENTIFIER          AS CASE_ID, \n"
        +   " CAS.FKCHLD_CLT          AS FOCUS_CHILD_ID, \n"
        +   " TRIM(CAS.CASE_NM)       AS CASE_NAME, \n"
        +   " CAS.START_DT            AS START_DATE, \n"
        +   " CAS.END_DT              AS END_DATE, \n"
        +   " CAS.SRV_CMPC            AS SERVICE_COMP, \n"
        +   " CAS.CLS_RSNC            AS CLOSE_REASON_CODE, \n"
        +   " CAS.FKSTFPERST          AS WORKER_ID, \n"
        +   " CAS.LMT_ACSSCD          AS LIMITED_ACCESS_CODE, \n"
        +   " CAS.LMT_ACS_DT          AS LIMITED_ACCESS_DATE, \n"
        +   " TRIM(CAS.LMT_ACSDSC)    AS LIMITED_ACCESS_DESCRIPTION, \n"
        +   " CAS.L_GVR_ENTC          AS LIMITED_ACCESS_GOVERNMENT_ENT, \n"
        +   " CAS.LST_UPD_TS          AS CASE_LAST_UPDATED, \n"
        +   " CAS.GVR_ENTC            AS COUNTY, \n"
        +   " CAS.APV_STC  \n"
        + "FROM DRIVER d  \n"
        + "JOIN CASE_T CAS ON CAS.IDENTIFIER = d.CASE_ID \n"
        + "WHERE CAS.IBMSNAP_OPERATION IN ('I','U')  \n"
        + "WITH UR ";
  //@formatter:on

  //@formatter:off
  public static final String SELECT_CLIENT = 
      "\nSELECT \n"
          + "    c.IDENTIFIER        AS CLIENT_ID \n"
          + "  , TRIM(c.COM_FST_NM)  AS CLIENT_FIRST_NM \n"
          + "  , TRIM(c.COM_LST_NM)  AS CLIENT_LAST_NM \n"
          + "  , c.SENSTV_IND        AS CLIENT_SENSITIVITY_IND \n"
          + "  , c.LST_UPD_TS        AS CLIENT_LAST_UPDATED \n"
          + "  , c.IBMSNAP_LOGMARKER AS CLIENT_LOGMARKER \n"
          + "  , c.IBMSNAP_OPERATION AS CLIENT_OPERATION \n"
          + " FROM (\n"
          + "   SELECT DISTINCT rc.FKCLIENT_T AS CLIENT_ID FROM GT_REFR_CLT rc \n"
          + ") GT \n"
          + " JOIN CLIENT_T C ON C.IDENTIFIER = GT.CLIENT_ID \n"
      + " FOR READ ONLY WITH UR  ";
  //@formatter:on

  //@formatter:off
  public static final String SELECT_FOCUS_CHILD_PARENTS = 
       "SELECT DISTINCT cas.FKCHLD_CLT AS FOCUS_CHILD_ID,  \n"
          + "ccc.IDENTIFIER AS L_CLIENT_ID, TRIM(ccc.COM_FST_NM) AS L_FIRST, TRIM(ccc.COM_MID_NM) AS L_MIDDLE, TRIM(ccc.COM_LST_NM) AS L_LAST, ccc.BIRTH_DT AS L_BIRTH, ccc.GENDER_CD AS L_GENDER, ccc.SENSTV_IND AS L_SENSTV_IND,\n"
          + "TRIM(sc.SHORT_DSC) AS REL_TYPE, sc.SYS_ID AS REL_CODE, \n"
          + "cc0.IDENTIFIER AS R_CLIENT_ID, TRIM(cc0.COM_FST_NM) AS R_FIRST, TRIM(cc0.COM_MID_NM) AS R_MIDDLE, TRIM(cc0.COM_LST_NM) AS R_LAST, cc0.BIRTH_DT AS R_BIRTH, cc0.GENDER_CD AS R_GENDER, cc0.SENSTV_IND AS R_SENSTV_IND\n"
          + "FROM (SELECT DISTINCT gt.FKREFERL_T AS CASE_ID FROM GT_REFR_CLT gt) x \n"
          + "JOIN CASE_T cas   ON cas.IDENTIFIER  = x.CASE_ID \n"
          + "JOIN CLN_RELT rel ON rel.FKCLIENT_T  = cas.FKCHLD_CLT \n"
          + "JOIN CLIENT_T cc0 ON cc0.identifier  = rel.FKCLIENT_0 \n"
          + "JOIN CLIENT_T ccc ON ccc.identifier  = rel.FKCLIENT_T \n"
          + "JOIN SYS_CD_C SC  ON SC.SYS_ID       = rel.CLNTRELC AND SC.FKS_META_T = 'CLNTRELC'\n"
          + "JOIN SYS_CD_C SC2 ON SC2.SYS_ID      = CAST(SC.LONG_DSC AS SMALLINT) \n"
          + "WHERE SC.SYS_ID  IN (188,189,190,191,192,193,194,195,196,197,198,199,283,284,285,286,287,288,289,290,291,292,293,242,243,301,6360) \n"
     + "UNION \n"
     + "SELECT DISTINCT cas.FKCHLD_CLT AS FOCUS_CHILD_ID,  \n"
          + "cc0.IDENTIFIER AS L_CLIENT_ID, TRIM(cc0.COM_FST_NM) AS L_FIRST, TRIM(cc0.COM_MID_NM) AS L_MIDDLE, TRIM(cc0.COM_LST_NM) AS L_LAST, cc0.BIRTH_DT AS L_BIRTH, cc0.GENDER_CD AS L_GENDER, cc0.SENSTV_IND AS L_SENSTV_IND,\n"
          + "sc2.SHORT_DSC AS REL_TYPE, sc2.SYS_ID AS REL_CODE,  \n"
          + "ccc.IDENTIFIER AS R_CLIENT_ID, TRIM(ccc.COM_FST_NM) AS R_FIRST, TRIM(ccc.COM_MID_NM) AS R_MIDDLE, TRIM(ccc.COM_LST_NM) AS R_LAST, ccc.BIRTH_DT AS R_BIRTH, ccc.GENDER_CD AS R_GENDER, ccc.SENSTV_IND AS R_SENSTV_IND\n"
          + "FROM (SELECT DISTINCT gt.FKREFERL_T AS CASE_ID FROM GT_REFR_CLT gt) x \n"
          + "JOIN CASE_T cas   ON cas.IDENTIFIER  = x.CASE_ID \n"
          + "JOIN CLN_RELT rel ON rel.FKCLIENT_0  = cas.FKCHLD_CLT \n"
          + "JOIN CLIENT_T cc0 ON cc0.identifier  = rel.FKCLIENT_0 \n"
          + "JOIN CLIENT_T ccc ON ccc.identifier  = rel.FKCLIENT_T \n"
          + "JOIN SYS_CD_C SC  ON SC.SYS_ID       = rel.CLNTRELC AND SC.FKS_META_T = 'CLNTRELC'\n"
          + "JOIN SYS_CD_C SC2 ON SC2.SYS_ID      = CAST(SC.LONG_DSC AS SMALLINT) \n"
          + "WHERE SC2.SYS_ID  IN (188,189,190,191,192,193,194,195,196,197,198,199,283,284,285,286,287,288,289,290,291,292,293,242,243,301,6360) \n"
          + "WITH UR";
  //@formatter:on

  /**
   * Filter <strong>deleted</strong> Clients and Cases.
   */
  //@formatter:off
  public static final String PREP_AFFECTED_CLIENTS_LAST_CHG =  
      "INSERT INTO GT_ID (IDENTIFIER) \n"
       + "WITH DRIVER AS ( \n"
               + " SELECT DISTINCT CAS1.FKCHLD_CLT AS IDENTIFIER \n"
            + " FROM  CASE_T CAS1  \n"
            + " WHERE CAS1.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
          + "UNION SELECT DISTINCT CCL2.FKCLIENT_T AS IDENTIFIER  \n"
            + " FROM CASE_T CAS2 \n"
            + " JOIN CHLD_CLT CCL2 ON CCL2.FKCLIENT_T = CAS2.FKCHLD_CLT   \n"
            + " JOIN CLIENT_T CLC2 ON CLC2.IDENTIFIER = CCL2.FKCLIENT_T  \n"
            + " WHERE CCL2.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END'  \n"
          + "UNION SELECT DISTINCT CAS3.FKCHLD_CLT AS IDENTIFIER  \n"
            + " FROM CASE_T CAS3  \n"
            + " JOIN CLIENT_T CLC3 ON CLC3.IDENTIFIER = CAS3.FKCHLD_CLT \n"
            + " WHERE CLC3.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
          + "UNION SELECT DISTINCT CAS4.FKCHLD_CLT AS IDENTIFIER  \n"
            + " FROM CASE_T CAS4  \n"
            + " JOIN CLN_RELT CLR4  ON CLR4.FKCLIENT_T = CAS4.FKCHLD_CLT \n"
            + " WHERE CLR4.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
            + "   AND CLR4.CLNTRELC IN (" + INCLUDED_RELATIONSHIPS + ") \n"
            + "   AND CLR4.END_DT IS NULL \n"
          + "UNION SELECT DISTINCT CLR5.FKCLIENT_0 AS IDENTIFIER  \n"
            + " FROM CASE_T CAS5 \n"
            + " JOIN CLN_RELT CLR5 ON CLR5.FKCLIENT_T = CAS5.FKCHLD_CLT \n"
            + " JOIN CLIENT_T CLP5 ON CLP5.IDENTIFIER = CLR5.FKCLIENT_0  \n"
            + " WHERE CLP5.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
            + "   AND CLR5.CLNTRELC IN (" + INCLUDED_RELATIONSHIPS + ") \n"
            + "   AND CLR5.END_DT IS NULL \n"
          + "UNION SELECT DISTINCT CLR6.FKCLIENT_T AS IDENTIFIER  \n"
            + " FROM CASE_T CAS6 \n"
            + " JOIN CLN_RELT CLR6 ON CLR6.FKCLIENT_T = CAS6.FKCHLD_CLT \n"
            + " JOIN CLIENT_T CLP6 ON CLP6.IDENTIFIER = CLR6.FKCLIENT_0  \n"
            + " WHERE CLP6.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
            + "   AND CLR6.CLNTRELC IN (" + INCLUDED_RELATIONSHIPS + ") \n"
            + "   AND CLR6.END_DT IS NULL \n"
       + ") \n"
       + "SELECT DISTINCT CAS1.FKCHLD_CLT AS CLIENT_ID \n"
          + "FROM DRIVER d1 \n"
          + "JOIN CASE_T CAS1   ON CAS1.FKCHLD_CLT = d1.IDENTIFIER \n"
       + "UNION SELECT DISTINCT REL2.FKCLIENT_0  AS CLIENT_ID \n"
          + "FROM DRIVER d2 \n"
          + "JOIN CLN_RELT REL2 ON REL2.FKCLIENT_T = d2.IDENTIFIER \n"
          + "JOIN CASE_T   CAS2 ON CAS2.FKCHLD_CLT = REL2.FKCLIENT_0 \n"
          + "WHERE REL2.CLNTRELC IN (" + INCLUDED_RELATIONSHIPS + ") \n"
          + "  AND REL2.END_DT IS NULL \n"
       + "UNION SELECT DISTINCT REL3.FKCLIENT_T  AS CLIENT_ID \n"
          + "FROM DRIVER d3 \n"
          + "JOIN CLN_RELT REL3 ON REL3.FKCLIENT_0 = d3.IDENTIFIER \n"
          + "JOIN CASE_T   CAS3 ON CAS3.FKCHLD_CLT = REL3.FKCLIENT_T  \n"
          + "WHERE REL3.CLNTRELC IN (" + INCLUDED_RELATIONSHIPS + ") \n"
          + "  AND REL3.END_DT IS NULL \n"
       + "UNION SELECT DISTINCT REL4.FKCLIENT_T  AS CLIENT_ID \n"
          + "FROM DRIVER d4 \n"
          + "JOIN CLN_RELT REL4 ON REL4.FKCLIENT_T = d4.IDENTIFIER \n"
          + "JOIN CASE_T   CAS4 ON CAS4.FKCHLD_CLT = REL4.FKCLIENT_0 \n"
          + "WHERE REL4.CLNTRELC IN (" + INCLUDED_RELATIONSHIPS + ") \n"
          + "  AND REL4.END_DT IS NULL \n"
       + "UNION SELECT DISTINCT REL5.FKCLIENT_0  AS CLIENT_ID \n"
          + "FROM DRIVER d5 \n"
          + "JOIN CLN_RELT REL5 ON REL5.FKCLIENT_0 = d5.IDENTIFIER \n"
          + "JOIN CASE_T   CAS5 ON CAS5.FKCHLD_CLT = REL5.FKCLIENT_T "
          + "WHERE REL5.CLNTRELC IN (" + INCLUDED_RELATIONSHIPS + ") \n"
          + "  AND REL5.END_DT IS NULL ";
  //@formatter:on

  /**
   * Original, overkill approach. Brings back too much redundant data. Kept here for reference.
   */
  //@formatter:off
  public static final String SELECT_CASES_FULL_EVERYTHING = 
      "\nWITH DRIVER AS (\n"
          + " SELECT     \n"
          + "       c.IDENTIFIER        AS THIS_CLIENT_ID \n"
          + "     , TRIM(c.COM_FST_NM)  AS THIS_CLIENT_FIRST_NM \n"
          + "     , TRIM(c.COM_LST_NM)  AS THIS_CLIENT_LAST_NM \n"
          + "     , c.SENSTV_IND        AS THIS_CLIENT_SENSITIVITY_IND \n"
          + "     , c.LST_UPD_TS        AS THIS_CLIENT_LAST_UPDATED \n"
          + "     , c.IBMSNAP_LOGMARKER AS THIS_CLIENT_LOGMARKER \n"
          + "     , c.IBMSNAP_OPERATION AS THIS_CLIENT_OPERATION \n"
          + " FROM GT_ID GT \n"
          + " JOIN CLIENT_T C ON C.IDENTIFIER = GT.IDENTIFIER \n"
          + ") \n"
     + " SELECT   \n"
          + " CAS1.IDENTIFIER      AS CASE_ID, \n"
          + " CAS1.FKCHLD_CLT      AS FOCUS_CHILD_ID, \n"
          + " DRV1.THIS_CLIENT_ID  AS THIS_CLIENT_ID, \n"
          + " 1                    AS STANZA, \n"
          + " 0                    AS REL_FOCUS_TO_OTHER, \n"
          + " 0                    AS REL_OTHER_TO_FOCUS, \n"
          + " CAS1.CASE_NM         AS CASE_NAME, \n"
          + " CAS1.START_DT        AS START_DATE, \n"
          + " CAS1.END_DT          AS END_DATE, \n"
          + " CAS1.SRV_CMPC        AS SERVICE_COMP, \n"
          + " CAS1.CLS_RSNC        AS CLOSE_REASON_CODE, \n"
          + " CAS1.FKSTFPERST      AS WORKER_ID, \n"
          + " CAS1.LMT_ACSSCD      AS LIMITED_ACCESS_CODE, \n"
          + " CAS1.LMT_ACS_DT      AS LIMITED_ACCESS_DATE, \n"
          + " CAS1.LMT_ACSDSC      AS LIMITED_ACCESS_DESCRIPTION, \n"
          + " CAS1.L_GVR_ENTC      AS LIMITED_ACCESS_GOVERNMENT_ENT, \n"
          + " CAS1.LST_UPD_TS      AS CASE_LAST_UPDATED, \n"
          + " CAS1.GVR_ENTC        AS COUNTY, \n"
          + " CAS1.APV_STC \n"
          + "FROM DRIVER DRV1 \n"
          + "JOIN CASE_T CAS1 ON CAS1.FKCHLD_CLT = DRV1.THIS_CLIENT_ID \n"
          + "WHERE CAS1.IBMSNAP_OPERATION IN ('I','U') \n"
     + "UNION ALL \n"
          + "SELECT     \n"
          + " CAS2.IDENTIFIER      AS CASE_ID, \n"
          + " CAS2.FKCHLD_CLT      AS FOCUS_CHILD_ID, \n"
          + " DRV2.THIS_CLIENT_ID  AS THIS_CLIENT_ID, \n"
          + " 2                    AS STANZA, \n"
          + " REL2.CLNTRELC        AS REL_FOCUS_TO_OTHER, \n"
          + " 0                    AS REL_OTHER_TO_FOCUS, \n"
          + " CAS2.CASE_NM         AS CASE_NAME, \n"
          + " CAS2.START_DT        AS START_DATE, \n"
          + " CAS2.END_DT          AS END_DATE, \n"
          + " CAS2.SRV_CMPC        AS SERVICE_COMP, \n"
          + " CAS2.CLS_RSNC        AS CLOSE_REASON_CODE, \n"
          + " CAS2.FKSTFPERST      AS WORKER_ID, \n"
          + " CAS2.LMT_ACSSCD      AS LIMITED_ACCESS_CODE, \n"
          + " CAS2.LMT_ACS_DT      AS LIMITED_ACCESS_DATE, \n"
          + " CAS2.LMT_ACSDSC      AS LIMITED_ACCESS_DESCRIPTION, \n"
          + " CAS2.L_GVR_ENTC      AS LIMITED_ACCESS_GOVERNMENT_ENT, \n"
          + " CAS2.LST_UPD_TS      AS CASE_LAST_UPDATED, \n"
          + " CAS2.GVR_ENTC        AS COUNTY, \n"
          + " CAS2.APV_STC \n"
          + "FROM DRIVER DRV2 \n"
          + "JOIN CLN_RELT REL2 ON REL2.FKCLIENT_T = DRV2.THIS_CLIENT_ID \n"
          + "JOIN CASE_T   CAS2 ON CAS2.FKCHLD_CLT = REL2.FKCLIENT_0 \n"
          + "WHERE CAS2.IBMSNAP_OPERATION IN ('I','U') \n"
          + "  AND REL2.IBMSNAP_OPERATION IN ('I','U') \n"
    + "UNION ALL \n"
          + "SELECT  \n"
          + " CAS3.IDENTIFIER      AS CASE_ID, \n"
          + " CAS3.FKCHLD_CLT      AS FOCUS_CHILD_ID, \n"
          + " DRV3.THIS_CLIENT_ID  AS THIS_CLIENT_ID, \n"
          + " 3                    AS STANZA, \n"
          + " 0                    AS REL_FOCUS_TO_OTHER, \n"
          + " REL3.CLNTRELC        AS REL_OTHER_TO_FOCUS, \n"
          + " CAS3.CASE_NM         AS CASE_NAME, \n"
          + " CAS3.START_DT        AS START_DATE, \n"
          + " CAS3.END_DT          AS END_DATE, \n"
          + " CAS3.SRV_CMPC        AS SERVICE_COMP, \n"
          + " CAS3.CLS_RSNC        AS CLOSE_REASON_CODE, \n"
          + " CAS3.FKSTFPERST      AS WORKER_ID, \n"
          + " CAS3.LMT_ACSSCD      AS LIMITED_ACCESS_CODE, \n"
          + " CAS3.LMT_ACS_DT      AS LIMITED_ACCESS_DATE, \n"
          + " CAS3.LMT_ACSDSC      AS LIMITED_ACCESS_DESCRIPTION, \n"
          + " CAS3.L_GVR_ENTC      AS LIMITED_ACCESS_GOVERNMENT_ENT, \n"
          + " CAS3.LST_UPD_TS      AS CASE_LAST_UPDATED, \n"
          + " CAS3.GVR_ENTC        AS COUNTY, \n"
          + " CAS3.APV_STC \n"
          + "FROM DRIVER DRV3, CLN_RELT REL3, CASE_T CAS3 \n"
          + "WHERE CAS3.FKCHLD_CLT = REL3.FKCLIENT_T AND REL3.FKCLIENT_0 = DRV3.THIS_CLIENT_ID \n"
          + "  AND CAS3.IBMSNAP_OPERATION IN ('I','U') \n"
          + "  AND REL3.IBMSNAP_OPERATION IN ('I','U') \n"
     + " FOR READ ONLY WITH UR ";
  //@formatter:on

  //@formatter:off
  public static final String SELECT_LAST_RUN_CHILD = 
        "SELECT \n"
      + " CAS.IDENTIFIER AS CASE_ID, \n"
      + " CAS.START_DT   AS START_DATE, \n"
      + " CAS.END_DT     AS END_DATE, \n"
      + " CAS.GVR_ENTC   AS COUNTY, \n"
      + " CAS.SRV_CMPC   AS SERVICE_COMP, \n"
      + " CAS.LMT_ACSSCD AS LIMITED_ACCESS_CODE, \n"
      + " CAS.LMT_ACS_DT AS LIMITED_ACCESS_DATE, \n"
      + " CAS.LMT_ACSDSC AS LIMITED_ACCESS_DESCRIPTION, \n"
      + " CAS.L_GVR_ENTC AS LIMITED_ACCESS_GOVERNMENT_ENT, \n"
      + " CAS.LST_UPD_TS AS CASE_LAST_UPDATED, \n"
      + " CLC.COM_FST_NM AS FOCUS_CHLD_FIRST_NM, \n"
      + " CLC.COM_LST_NM AS FOCUS_CHLD_LAST_NM, \n"
      + " CLC.IDENTIFIER AS FOCUS_CHILD_ID, \n"
      + " CLC.SENSTV_IND AS FOCUS_CHILD_SENSITIVITY_IND, \n"
      + " CLC.LST_UPD_TS AS FOCUS_CHILD_LAST_UPDATED, \n"
      + " CLP.COM_FST_NM AS PARENT_FIRST_NM, \n"
      + " CLP.COM_LST_NM AS PARENT_LAST_NM, \n"
      + " CLR.CLNTRELC   AS PARENT_RELATIONSHIP, \n"
      + " CLP.IDENTIFIER AS PARENT_ID, \n"
      + " CLP.SENSTV_IND AS PARENT_SENSITIVITY_IND, \n"
      + " CLP.LST_UPD_TS AS PARENT_LAST_UPDATED, \n"
      + " 'CLIENT_T'     AS PARENT_SOURCE_TABLE, \n"
      + " STF.FIRST_NM   AS WORKER_FIRST_NM, \n"
      + " STF.LAST_NM    AS WORKER_LAST_NM, \n"
      + " STF.IDENTIFIER AS WORKER_ID, \n"
      + " STF.LST_UPD_TS AS WORKER_LAST_UPDATED, \n"
      + " CAS.IBMSNAP_LOGMARKER AS CAS_IBMSNAP_LOGMARKER, \n"
      + " CAS.IBMSNAP_OPERATION AS CAS_IBMSNAP_OPERATION, \n"
      + " CLC.IBMSNAP_LOGMARKER AS CLC_IBMSNAP_LOGMARKER, \n"
      + " CLC.IBMSNAP_OPERATION AS CLC_IBMSNAP_OPERATION, \n"
      + " CLP.IBMSNAP_LOGMARKER AS CLP_IBMSNAP_LOGMARKER, \n"
      + " CLP.IBMSNAP_OPERATION AS CLP_IBMSNAP_OPERATION, \n"
      + " STF.IBMSNAP_LOGMARKER AS STF_IBMSNAP_LOGMARKER, \n"
      + " STF.IBMSNAP_OPERATION AS STF_IBMSNAP_OPERATION, \n"
      + " MAX( \n"
      + "     NVL(CAS.IBMSNAP_LOGMARKER, TIMESTAMP('2008-09-30 11:54:40')),\n"
      + "     NVL(CLC.IBMSNAP_LOGMARKER, TIMESTAMP('2008-09-30 11:54:40')),\n"
      + "     NVL(CLP.IBMSNAP_LOGMARKER, TIMESTAMP('2008-09-30 11:54:40')) \n"
      + " ) LAST_CHG \n"
      + "FROM CASE_T CAS \n"
      + "JOIN STFPERST STF ON STF.IDENTIFIER = CAS.FKSTFPERST \n"
      + "JOIN CHLD_CLT CCL ON CCL.FKCLIENT_T = CAS.FKCHLD_CLT \n"
      + "JOIN CLIENT_T CLC ON CLC.IDENTIFIER = CCL.FKCLIENT_T \n"
      + "LEFT JOIN CLN_RELT CLR ON CLR.FKCLIENT_0 = CCL.FKCLIENT_T "
      + "LEFT JOIN CLIENT_T CLP ON CLP.IDENTIFIER = CLR.FKCLIENT_T \n"
      + "WHERE CAS.IDENTIFIER IN ( \n"
      + "   SELECT GT.IDENTIFIER FROM GT_ID GT \n"
      + ") \n";
  //@formatter:on

  //@formatter:off
  public static final String SELECT_LAST_RUN_PARENT = 
        "SELECT \n"
      + "  CAS.IDENTIFIER AS CASE_ID, \n"
      + "  CAS.START_DT   AS START_DATE, \n"
      + "  CAS.END_DT     AS END_DATE, \n"
      + "  CAS.GVR_ENTC   AS COUNTY, \n"
      + "  CAS.SRV_CMPC   AS SERVICE_COMP, \n"
      + "  CAS.LMT_ACSSCD AS LIMITED_ACCESS_CODE, \n"
      + "  CAS.LMT_ACS_DT AS LIMITED_ACCESS_DATE, \n"
      + "  CAS.LMT_ACSDSC AS LIMITED_ACCESS_DESCRIPTION, \n"
      + "  CAS.L_GVR_ENTC AS LIMITED_ACCESS_GOVERNMENT_ENT, \n"
      + "  CAS.LST_UPD_TS AS CASE_LAST_UPDATED, \n"
      + "  CLC.COM_FST_NM AS FOCUS_CHLD_FIRST_NM, \n"
      + "  CLC.COM_LST_NM AS FOCUS_CHLD_LAST_NM, \n"
      + "  CLC.IDENTIFIER AS FOCUS_CHILD_ID, \n"
      + "  CLC.SENSTV_IND AS FOCUS_CHILD_SENSITIVITY_IND, \n"
      + "  CLC.LST_UPD_TS AS FOCUS_CHILD_LAST_UPDATED, \n"
      + "  CLP.COM_FST_NM AS PARENT_FIRST_NM, \n"
      + "  CLP.COM_LST_NM AS PARENT_LAST_NM, \n"
      + "  CLR.CLNTRELC   AS PARENT_RELATIONSHIP, \n"
      + "  CLP.IDENTIFIER AS PARENT_ID, \n"
      + "  CLP.SENSTV_IND AS PARENT_SENSITIVITY_IND, \n"
      + "  CLP.LST_UPD_TS AS PARENT_LAST_UPDATED, \n"
      + "  'CLIENT_T'     AS PARENT_SOURCE_TABLE, \n"
      + "  STF.FIRST_NM   AS WORKER_FIRST_NM, \n"
      + "  STF.LAST_NM    AS WORKER_LAST_NM, \n"
      + "  STF.IDENTIFIER AS WORKER_ID, \n"
      + "  STF.LST_UPD_TS AS WORKER_LAST_UPDATED,   \n"
      + "  CAS.IBMSNAP_LOGMARKER AS CAS_IBMSNAP_LOGMARKER, \n"
      + "  CAS.IBMSNAP_OPERATION AS CAS_IBMSNAP_OPERATION, \n"
      + "  CLC.IBMSNAP_LOGMARKER AS CLC_IBMSNAP_LOGMARKER, \n"
      + "  CLC.IBMSNAP_OPERATION AS CLC_IBMSNAP_OPERATION, \n"
      + "  CLP.IBMSNAP_LOGMARKER AS CLP_IBMSNAP_LOGMARKER, \n"
      + "  CLP.IBMSNAP_OPERATION AS CLP_IBMSNAP_OPERATION, \n"
      + "  STF.IBMSNAP_LOGMARKER AS STF_IBMSNAP_LOGMARKER, \n"
      + "  STF.IBMSNAP_OPERATION AS STF_IBMSNAP_OPERATION, \n"
      + "  MAX ( \n"
      + "    NVL(CAS.IBMSNAP_LOGMARKER, TIMESTAMP('2008-09-30 11:54:40')),\n"
      + "    NVL(CLC.IBMSNAP_LOGMARKER, TIMESTAMP('2008-09-30 11:54:40')),\n"
      + "    NVL(CLP.IBMSNAP_LOGMARKER, TIMESTAMP('2008-09-30 11:54:40')) \n"
      + "  ) LAST_CHG \n"
      + "FROM CASE_T CAS \n"
      + "JOIN STFPERST STF ON STF.IDENTIFIER = CAS.FKSTFPERST \n"
      + "JOIN CHLD_CLT CCL ON CCL.FKCLIENT_T = CAS.FKCHLD_CLT \n"
      + "JOIN CLIENT_T CLC ON CLC.IDENTIFIER = CCL.FKCLIENT_T \n"
      + "LEFT JOIN CLN_RELT CLR ON CLR.FKCLIENT_T = CCL.FKCLIENT_T "
      + "LEFT JOIN CLIENT_T CLP ON CLP.IDENTIFIER = CLR.FKCLIENT_0 \n"
      + "WHERE CAS.IDENTIFIER IN ( \n"
      + "   SELECT GT.IDENTIFIER FROM GT_ID GT \n"
      + ") \n";
  //@formatter:on

}
