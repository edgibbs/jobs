package gov.ca.cwds.neutron.flight;

public class NeutronLongCmdLineName {

  public static final String CMD_LINE_ES_CONFIG_PEOPLE = "config-people";
  public static final String CMD_LINE_ES_CONFIG_PEOPLE_SUMMARY = "config-people-summary";

  public static final String CMD_LINE_INDEX_NAME = "index-name";

  public static final String CMD_LINE_LAST_START_TIME = "last-start-time";
  public static final String CMD_LINE_LAST_END_TIME = "last-end-time";

  public static final String CMD_LINE_LAST_RUN_FILE = "last-run-file";
  public static final String CMD_LINE_LAST_RUN_RANGE = "last-run-range";

  public static final String CMD_LINE_BASE_DIRECTORY = "base-directory";

  public static final String CMD_LINE_THREADS = "thread-num";
  public static final String CMD_LINE_BUCKET_RANGE = "bucket-range";
  public static final String CMD_LINE_LOAD_SEALED_AND_SENSITIVE = "load-sealed-sensitive";
  public static final String CMD_LINE_INITIAL_LOAD = "initial_load";
  public static final String CMD_LINE_REFRESH_MQT = "refresh_mqt";
  public static final String CMD_LINE_DROP_INDEX = "drop_index";

  public static final String CMD_LINE_LEGACY_PEOPLE_MAPPING = "legacy_people_mapping";
  public static final String CMD_LINE_NO_PEOPLE_INDEX = "no_people_index";

  public static final String CMD_LINE_SIMULATE_LAUNCH = "simulate_launch";

  public static final String CMD_LINE_ALT_INPUT_FILE = "alt-input-file";

  /**
   * Obsolete, no longer in use.
   */
  public static final String CMD_LINE_BUCKET_TOTAL = "total-buckets";
  public static final String CMD_LINE_MIN_ID = "min_id";
  public static final String CMD_LINE_MAX_ID = "max_id";

  private NeutronLongCmdLineName() {
    // Because SonarQube says so ...
  }

}
