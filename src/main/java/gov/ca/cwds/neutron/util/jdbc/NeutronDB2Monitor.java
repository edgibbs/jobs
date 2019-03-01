package gov.ca.cwds.neutron.util.jdbc;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.db2.jcc.DB2SystemMonitor;

/**
 * Auto-closeable delegate wrapper for IBM's DB2SystemMonitor.
 * 
 * @author CWDS API Team
 * @see NeutronDB2Utils
 */
public class NeutronDB2Monitor implements Closeable, DB2SystemMonitor {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronDB2Monitor.class);

  private DB2SystemMonitor monitor;

  public NeutronDB2Monitor(final Connection con, boolean monitorMe) {
    init(con, monitorMe);
  }

  public NeutronDB2Monitor(final Connection con) {
    init(con, true);
  }

  private final void init(final Connection con, boolean monitorMe) {
    if (monitorMe) {
      LOGGER.debug("MONITOR DB2 CONNECTIONS");
      monitor = NeutronDB2Utils.monitorStart(con);
    }
  }

  @Override
  public void close() throws IOException {
    try {
      if (monitor != null) {
        NeutronDB2Utils.monitorStopAndReport(monitor);
      }
    } catch (Exception e) {
      LOGGER.warn("ERROR STOPPING DB2 MONITOR!", e);
    }
  }

  @Override
  public void enable(boolean arg0) throws SQLException {
    monitor.enable(arg0);
  }

  @Override
  public long getApplicationTimeMillis() throws SQLException {
    return monitor.getApplicationTimeMillis();
  }

  @Override
  public long getCoreDriverTimeMicros() throws SQLException {
    return monitor.getCoreDriverTimeMicros();
  }

  @Override
  public long getNetworkIOTimeMicros() throws SQLException {
    return monitor.getNetworkIOTimeMicros();
  }

  @Override
  public long getServerTimeMicros() throws SQLException {
    return monitor.getServerTimeMicros();
  }

  @Override
  public Object moreData(int arg0) throws SQLException {
    return monitor.moreData(arg0);
  }

  @Override
  public void start(int arg0) throws SQLException {
    monitor.start(arg0);
  }

  @Override
  public void stop() throws SQLException {
    monitor.stop();
  }

}
