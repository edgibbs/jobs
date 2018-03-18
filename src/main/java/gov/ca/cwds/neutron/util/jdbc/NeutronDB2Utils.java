package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import com.ibm.db2.jcc.DB2Connection;
import com.ibm.db2.jcc.DB2SystemMonitor;

import gov.ca.cwds.data.BaseDaoImpl;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;

/**
 * Miscellaneous DB2 utilities for Neutron rockets.
 * 
 * @author CWDS API Team
 */
public final class NeutronDB2Utils {

  private static final ConditionalLogger LOGGER = new JetPackLogger(NeutronDB2Utils.class);

  private NeutronDB2Utils() {
    // Default no-op.
  }

  /**
   * Enable DB2 parallelism. Ignored for other databases.
   * 
   * @param con connection
   * @throws SQLException connection error
   */
  public static void enableParallelism(Connection con) throws SQLException {
    String dbProductName = con.getMetaData().getDatabaseProductName();
    if (StringUtils.containsIgnoreCase(dbProductName, "db2")) {
      con.nativeSQL("SET CURRENT DEGREE = 'ANY'");
    }
  }

  /**
   * DB2's optimizer is confused by variable timestamp precision. Timestamps in query plans run fine
   * as a String through hard-parsed SQL but not in prepared statements.
   * 
   * @param sql last change SQL
   * @param lastRunStartDate last run start date
   * @param lastRunEndDate last run end date
   * @return DB2 timestamp string
   */
  public static String prepLastChangeSQL(String sql, Date lastRunStartDate, Date lastRunEndDate) {
    final String strStartDate = NeutronJdbcUtils.makeTimestampStringLookBack(lastRunStartDate);
    return sql.replaceAll("XYZ", strStartDate).replaceAll("LAST_RUN_START", strStartDate)
        .replaceAll("LAST_RUN_END", NeutronJdbcUtils.makeTimestampStringLookBack(lastRunEndDate))
        .replaceAll("'CURRENT TIMESTAMP'", "CURRENT TIMESTAMP");
  }

  /**
   * DB2's ORDER BY clause does <strong>NOT</strong> enforce result set order across platforms!
   * Since character sets differ by operating system, CHAR sort order differs and makes ORDER BY and
   * WHERE clauses problematic.
   * 
   * <p>
   * SELECT statements using range partitions depend on sort order.
   * </p>
   * 
   * <p>
   * Database platforms and versions:
   * </p>
   * 
   * <table summary="">
   * <tr>
   * <th align="justify">Platform</th>
   * <th align="justify">Name</th>
   * <th align="justify">Version</th>
   * <th align="justify">Major</th>
   * <th align="justify">Minor</th>
   * <th align="justify">Catalog</th>
   * </tr>
   * <tr>
   * <td align="justify">z/OS</td>
   * <td align="justify">DB2</td>
   * <td align="justify">DSN11010</td>
   * <td align="justify">11</td>
   * <td align="justify">1</td>
   * <td align="justify">location</td>
   * </tr>
   * <tr>
   * <td align="justify">Linux</td>
   * <td align="justify">DB2/LINUXX8664</td>
   * <td align="justify">SQL10057</td>
   * <td align="justify">10</td>
   * <td align="justify">5</td>
   * <td align="justify">database</td>
   * </tr>
   * </table>
   * 
   * @param dao DAO
   * 
   * @return true if DB2 is running on a mainframe
   * @throws NeutronCheckedException on general error
   */
  public static boolean isDB2OnZOS(final BaseDaoImpl<?> dao) throws NeutronCheckedException {
    boolean ret = false;

    try (final Connection con = dao.getSessionFactory().getSessionFactoryOptions()
        .getServiceRegistry().getService(ConnectionProvider.class).getConnection()) {

      final DatabaseMetaData meta = con.getMetaData();
      LOGGER.debug("meta: product name: {}, production version: {}, major: {}, minor: {}",
          meta.getDatabaseProductName(), meta.getDatabaseProductVersion(),
          meta.getDatabaseMajorVersion(), meta.getDatabaseMinorVersion());

      ret = meta.getDatabaseProductVersion().startsWith("DSN");
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "UNABLE TO DETERMINE DB2 PLATFORM! {}", e.getMessage());
    }

    return ret;
  }

  /**
   * Get a DB2 monitor and start it for this transaction.
   * 
   * @param con database connection
   * @return DB2 monitor
   */
  public static DB2SystemMonitor monitorStart(final Connection con) {
    DB2SystemMonitor ret = null;
    try {
      final com.ibm.db2.jcc.t4.b nativeCon =
          (com.ibm.db2.jcc.t4.b) ((com.mchange.v2.c3p0.impl.NewProxyConnection) con)
              .unwrap(Class.forName("com.ibm.db2.jcc.t4.b")); // NOSONAR
      final DB2Connection db2Con = nativeCon;
      LOGGER.debug("sendDataAsIs_: {}, enableRowsetSupport_: {}", nativeCon.sendDataAsIs_,
          nativeCon.enableRowsetSupport_);

      ret = db2Con.getDB2SystemMonitor();
      ret.enable(true);
      ret.start(DB2SystemMonitor.RESET_TIMES);
      return ret;
    } catch (Exception e) {
      LOGGER.warn("UNABLE TO GRAB DB2 MONITOR: {}", e.getMessage(), e);
    }

    return ret;
  }

  /**
   * Stop the DB2 monitor and report statistics.
   * 
   * @param monitor current monitor instance
   * @throws SQLException on JDBC error
   */
  @SuppressWarnings("unchecked")
  public static void monitorStopAndReport(final DB2SystemMonitor monitor) throws SQLException {
    if (monitor != null) {
      monitor.stop();
      final StringBuilder buf = new StringBuilder();

      buf.append("Server elapsed time (microseconds)=").append(monitor.getServerTimeMicros())
          .append("Network I/O elapsed time (microseconds)=")
          .append(monitor.getNetworkIOTimeMicros())
          .append("Core driver elapsed time (microseconds)=")
          .append(monitor.getCoreDriverTimeMicros())
          .append("Application elapsed time (milliseconds)=")
          .append(monitor.getApplicationTimeMillis()).append("monitor.moreData: 0: ")
          .append(monitor.moreData(0)).append("monitor.moreData: 1: ").append(monitor.moreData(1))
          .append("monitor.moreData: 2: ").append(monitor.moreData(2));
      LOGGER.debug("DB2 monitor report: {}", buf::toString);
    }
  }

}
