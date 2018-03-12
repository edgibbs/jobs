package gov.ca.cwds.neutron.enums;

public enum NeutronIntegerDefaults {

  WAIT_BULK_PROCESSOR(25),

  SLEEP_MILLIS(1500),

  /**
   * Default wait time when polling thread queues. Mostly used in initial load.
   */
  POLL_MILLIS(1000),

  DEFAULT_BUCKETS(1),

  /**
   * To avoid missing changed records, look N minutes before the last successful run timestamp.
   * NOTE: make configurable.
   */
  LOOKBACK_MINUTES(-13),

  /**
   * Default fetch size for Hibernate and JDBC. Pull records in bulk in order to minimize network
   * calls.
   */
  FETCH_SIZE(5000)

  ;

  private final int value;

  private NeutronIntegerDefaults(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

}
