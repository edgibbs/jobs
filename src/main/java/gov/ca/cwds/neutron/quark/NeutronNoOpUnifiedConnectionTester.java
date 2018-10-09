package gov.ca.cwds.neutron.quark;

import java.sql.Connection;

import com.mchange.v2.c3p0.UnifiedConnectionTester;

public class NeutronNoOpUnifiedConnectionTester implements UnifiedConnectionTester {

  private static final long serialVersionUID = 1L;

  @Override
  public int activeCheckConnection(Connection c) {
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int activeCheckConnection(Connection c, Throwable[] rootCauseOutParamHolder) {
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int activeCheckConnection(Connection c, String preferredTestQuery) {
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int activeCheckConnection(Connection c, String preferredTestQuery,
      Throwable[] rootCauseOutParamHolder) {
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int statusOnException(Connection c, Throwable t) {
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int statusOnException(Connection c, Throwable t, Throwable[] rootCauseOutParamHolder) {
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int statusOnException(Connection c, Throwable t, String preferredTestQuery) {
    return CONNECTION_IS_OKAY;
  }

  @Override
  public int statusOnException(Connection c, Throwable t, String preferredTestQuery,
      Throwable[] rootCauseOutParamHolder) {
    return CONNECTION_IS_OKAY;
  }

}
