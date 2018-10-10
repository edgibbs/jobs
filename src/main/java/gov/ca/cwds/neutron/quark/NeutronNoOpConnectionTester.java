package gov.ca.cwds.neutron.quark;

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.UnifiedConnectionTester;

public class NeutronNoOpConnectionTester implements UnifiedConnectionTester {

  private static final long serialVersionUID = 1L;

  protected static final Logger LOGGER = LoggerFactory.getLogger(NeutronNoOpConnectionTester.class);

  @Override
  public int activeCheckConnection(Connection c) {
    LOGGER.info("NeutronNoOpConnectionTester.activeCheckConnection");
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int activeCheckConnection(Connection c, Throwable[] rootCauseOutParamHolder) {
    LOGGER.info("NeutronNoOpConnectionTester.activeCheckConnection(Connection,Throwable[])");
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int activeCheckConnection(Connection c, String preferredTestQuery) {
    LOGGER.info(
        "NeutronNoOpConnectionTester.activeCheckConnection(Connection,String): preferredTestQuery: {}",
        preferredTestQuery);
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int activeCheckConnection(Connection c, String preferredTestQuery,
      Throwable[] rootCauseOutParamHolder) {
    LOGGER.info(
        "NeutronNoOpConnectionTester.activeCheckConnection(Connection,String,Throwable[]): preferredTestQuery: {}",
        preferredTestQuery);
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int statusOnException(Connection c, Throwable t) {
    LOGGER.info("NeutronNoOpConnectionTester.statusOnException(Connection,Throwable): {}",
        t.getMessage());
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int statusOnException(Connection c, Throwable t, Throwable[] rootCauseOutParamHolder) {
    LOGGER.info(
        "NeutronNoOpConnectionTester.statusOnException(Connection,Throwable,Throwable[]): {}",
        t.getMessage());
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int statusOnException(Connection c, Throwable t, String preferredTestQuery) {
    LOGGER.info(
        "NeutronNoOpConnectionTester.statusOnException(Connection,Throwable,String): preferredTestQuery: {}",
        preferredTestQuery);
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int statusOnException(Connection c, Throwable t, String preferredTestQuery,
      Throwable[] rootCauseOutParamHolder) {
    LOGGER.info(
        "NeutronNoOpConnectionTester.statusOnException(Connection,Throwable,String,Throwable[]): preferredTestQuery: {}",
        preferredTestQuery);
    return CONNECTION_IS_OKAY;
  }

}
