package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.db2.jcc.DB2Connection;

/**
 * Set DB2 user information on the active connection, including user logon and staff id.
 * 
 * <p>
 * Shockingly, SonarQube complains about vendor-specific JDBC methods, thus the SuppressWarnings
 * annotation.
 * </p>
 * 
 * @author CWDS API Team
 */
public class WorkDB2UserInfo implements Work {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkDB2UserInfo.class);

  private static final String NAME = "Neutron";

  public WorkDB2UserInfo() {
    // Default, no-op
  }

  @SuppressWarnings({"fb-contrib:JVR_JDBC_VENDOR_RELIANCE", "squid:CallToDeprecatedMethod"})
  @Override
  public void execute(Connection con) throws SQLException {
    con.setAutoCommit(false);
    con.setClientInfo("ApplicationName", "CARES Neutron");
    con.setClientInfo("ClientUser", "Neutron");

    if (con instanceof DB2Connection) {
      LOGGER.info("DB2 connection! Set user info ...");
      final String userId = NAME;

      final DB2Connection db2con = (DB2Connection) con;
      db2con.nativeSQL("SET CURRENT DEGREE = 'ANY'");
      db2con.setReadOnly(false);
      db2con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

      db2con.setDB2ClientDebugInfo(userId);
      db2con.setDB2ClientProgramId(NAME);
      db2con.setDB2ClientAccountingInformation(userId);
      db2con.setDB2ClientApplicationInformation(userId);
      db2con.setDB2ClientUser(NAME);

      // ALTERNATIVE: call proc SYSPROC.WLM_SET_CLIENT_INFO.
    }
  }

}
