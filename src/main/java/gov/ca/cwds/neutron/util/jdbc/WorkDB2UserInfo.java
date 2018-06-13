package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
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

      LOGGER.info("\n\n\nclient info properties\n\n\n");
      try (final ResultSet rs = db2con.getMetaData().getClientInfoProperties()) {
        while (rs.next()) {
          // NAME String=> The name of the client info property
          // MAX_LEN int=> The maximum length of the value for the property
          // DEFAULT_VALUE String=> The default value of the property
          // DESCRIPTION String=> A description of the property. This will typically contain
          // information as to where this property is stored in the database.
          LOGGER.info("client info prop: name: {}, max len: {}, default: {}, description: {}",
              rs.getString(1), rs.getInt(2), rs.getString(3), rs.getString(4));
        }
      } catch (Exception e) {
        LOGGER.warn("TROUBLE READING CLIENT INFO PROPERTIES!", e);
      }

      // ALTERNATIVE: call proc SYSPROC.WLM_SET_CLIENT_INFO.
    }
  }

}
