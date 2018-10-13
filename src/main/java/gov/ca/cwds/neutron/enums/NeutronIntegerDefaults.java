package gov.ca.cwds.neutron.enums;

import gov.ca.cwds.neutron.jetpack.JobLogs;

/**
 * Depressingly common Neutron settings.
 * 
 * @author CWDS API Team
 */
public enum NeutronIntegerDefaults {

  /**
   * Give Elasticsearch bulk indexing a chance to catch its breath.
   */
  WAIT_BULK_PROCESSOR(25),

  /**
   * Sadly necessary sometimes. Default thread sleep time, usually when waiting for work without a
   * lock condition or pollable queue.
   */
  SLEEP_MILLIS(1500),

  /**
   * Default wait time when polling thread queues. Mostly used in initial load.
   */
  POLL_MILLIS(2000),

  DEFAULT_BUCKETS(1),

  /**
   * To avoid missing changed records, look N minutes before the last successful run timestamp.
   * NOTE: make configurable.
   */
  LOOKBACK_MINUTES(-11),

  /**
   * Default fetch size for Hibernate and JDBC. Pull records in bulk to minimize network
   * round-trips.
   */
  FETCH_SIZE(5000),

  /**
   * Let queries run -- until it's time to give up. 1 hour.
   */
  QUERY_TIMEOUT_IN_SECONDS(3600),

  /**
   * Log every N records.
   * 
   * @see JobLogs
   */
  LOG_EVERY(5000);

  private final int value;

  private NeutronIntegerDefaults(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

}
