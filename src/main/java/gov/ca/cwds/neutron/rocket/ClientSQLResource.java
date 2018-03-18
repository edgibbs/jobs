package gov.ca.cwds.neutron.rocket;

import gov.ca.cwds.data.std.ApiMarker;

public class ClientSQLResource implements ApiMarker {

  private static final long serialVersionUID = 1L;

  //@formatter:off
  public static final String LAST_CHG_COLUMNS =
      "x.*";
  //@formatter:on

  //@formatter:off
  public static final String INSERT_CLIENT_LAST_CHG =
      "INSERT INTO GT_ID (IDENTIFIER)\n" 
          + "SELECT DISTINCT CLT.IDENTIFIER \n"
       + "FROM CLIENT_T clt \n"
       + "WHERE CLT.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
    + "UNION SELECT DISTINCT cla.FKCLIENT_T AS IDENTIFIER \n"
       + "FROM CL_ADDRT cla \n"
       + "WHERE CLA.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
    + "UNION SELECT DISTINCT cla.FKCLIENT_T AS IDENTIFIER \n"
       + "FROM CL_ADDRT cla \n"
       + "JOIN ADDRS_T  adr ON cla.FKADDRS_T  = adr.IDENTIFIER \n"
       + "WHERE ADR.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' \n"
    + "UNION SELECT DISTINCT eth.ESTBLSH_ID AS IDENTIFIER \n"
       + "FROM CLSCP_ET eth \n"
       + "WHERE ETH.ESTBLSH_CD = 'C' \n"
       + "AND ETH.IBMSNAP_LOGMARKER BETWEEN 'LAST_RUN_START' AND 'LAST_RUN_END' ";
  //@formatter:on

}
