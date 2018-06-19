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
public class WorkSetDB2UserInfo implements Work {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkSetDB2UserInfo.class);

  private static final String NAME = "Neutron";

  public WorkSetDB2UserInfo() {
    // Default, no-op
  }

  @SuppressWarnings({"fb-contrib:JVR_JDBC_VENDOR_RELIANCE", "squid:CallToDeprecatedMethod"})
  @Override
  public void execute(Connection connection) throws SQLException {
    if (connection instanceof DB2Connection) {
      LOGGER.info("DB2 connection! Set user info ...");
      final String program = NAME;
      final String userId = NAME;

      final DB2Connection db2con = (DB2Connection) connection;
      db2con.nativeSQL("SET CURRENT DEGREE = 'ANY'");
      db2con.setAutoCommit(false);
      db2con.setReadOnly(true);
      db2con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

      db2con.setDB2ClientDebugInfo(userId);
      db2con.setDB2ClientProgramId(program);
      db2con.setDB2ClientAccountingInformation(userId);
      db2con.setDB2ClientApplicationInformation(userId);
      db2con.setDB2ClientProgramId("CARES Neutron");
      db2con.setClientInfo("ApplicationName", "CARES Neutron");
      db2con.setClientInfo("ClientUser", program);
      db2con.setDB2ClientUser(program);

      // ALTERNATIVE: call proc SYSPROC.WLM_SET_CLIENT_INFO.
    }
  }

}
