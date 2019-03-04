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
   * Default bucket size for Initial Load clients using 1024 buckets.
   */
  FULL_DENORMALIZED_SIZE(16001),

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
  LOOKBACK_MINUTES(-4),

  /**
   * Default fetch size for Hibernate and JDBC. Fetch records in bulk to minimize network
   * round-trips.
   */
  FETCH_SIZE(5000),

  /**
   * Let queries run -- until it's time to give up. Default to 3 minutes.
   */
  QUERY_TIMEOUT_IN_SECONDS(180),

  /**
   * Log every N records.
   * 
   * @see JobLogs
   */
  LOG_EVERY(1000);

  private final int value;

  private NeutronIntegerDefaults(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public int value() {
    return value;
  }

}
