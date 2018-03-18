package gov.ca.cwds.neutron.flight;

import org.apache.commons.cli.Option;

/**
 * Neutron command line options.
 * 
 * @author CWDS API Team
 */
public enum NeutronCmdLineOption {

  /**
   * ElasticSearch configuration file for the gigantic "people" index.
   * 
   * <p>
   * Originally this was the only index, hence the option "-c" for "config."
   * </p>
   */
  ES_CONFIG_PEOPLE(FlightPlan.makeOpt("c", NeutronLongCmdLineName.CMD_LINE_ES_CONFIG_PEOPLE,
      "ElasticSearch configuration file [index: people]", false, 1, String.class, ',')),

  /**
   * ElasticSearch configuration file for the "people-summary" index.
   * 
   * <p>
   * Jira INT-1073. In the future, the Neutron architecture should handle an arbitrary number of
   * indexes and name them with Guice annotations.
   * </p>
   */
  ES_CONFIG_PEOPLE_SUMMARY(
      FlightPlan.makeOpt("p", NeutronLongCmdLineName.CMD_LINE_ES_CONFIG_PEOPLE_SUMMARY,
          "ElasticSearch configuration file [index: people-summary]", false, 1, String.class, ',')),

  /**
   * ElasticSearch index name to create or use. If not provided then ES Config alias is used.
   */
  INDEX_NAME(FlightPlan.makeOpt("i", NeutronLongCmdLineName.CMD_LINE_INDEX_NAME,
      "ElasticSearch index name", false, 1, String.class, ',')),

  /**
   * Last run start time in format 'yyyy-MM-dd HH:mm:ss'
   */
  LAST_START_TIME(FlightPlan.makeOpt("a", NeutronLongCmdLineName.CMD_LINE_LAST_START_TIME,
      "last run start time (yyyy-MM-dd HH:mm:ss)", false, 1, String.class, ',')),

  /**
   * Last run end time in format 'yyyy-MM-dd HH:mm:ss'
   */
  LAST_END_TIME(FlightPlan.makeOpt("E", NeutronLongCmdLineName.CMD_LINE_LAST_END_TIME,
      "last run end time (yyyy-MM-dd HH:mm:ss)", false, 1, String.class, ',')),

  /**
   * Last run date file (yyyy-MM-dd HH:mm:ss)
   */
  LAST_RUN_FILE(FlightPlan.makeOpt("l", NeutronLongCmdLineName.CMD_LINE_LAST_RUN_FILE,
      "last run date file (yyyy-MM-dd HH:mm:ss)", false, 1, String.class, ',')),

  /**
   * Alternate input file
   */
  BASE_DIRECTORY(FlightPlan.makeOpt("b", NeutronLongCmdLineName.CMD_LINE_BASE_DIRECTORY,
      "base directory", false, 1, String.class, ',')),

  /**
   * Bucket range (-r 20-24).
   */
  BUCKET_RANGE(FlightPlan.makeOpt("r", NeutronLongCmdLineName.CMD_LINE_BUCKET_RANGE,
      "bucket range (-r 20-24)", false, 2, Integer.class, '-')),

  /**
   * Number of threads (optional).
   */
  THREADS(FlightPlan.makeOpt("t", NeutronLongCmdLineName.CMD_LINE_THREADS, "# of threads", false, 1,
      Integer.class, ',')),

  /**
   * Minimum key, inclusive.
   */
  MIN_ID(FlightPlan.makeOpt("m", NeutronLongCmdLineName.CMD_LINE_MIN_ID,
      "minimum identifier, inclusive", false, 1, String.class, ',')),

  /**
   * Maximum key, inclusive.
   */
  MAX_ID(FlightPlan.makeOpt("x", NeutronLongCmdLineName.CMD_LINE_MAX_ID,
      "maximum identifier, exclusive", false, 1, String.class, ',')),

  /**
   * Indicate if sealed and sensitive data should be loaded
   */
  LOAD_SEALED_SENSITIVE(
      FlightPlan.makeOpt("s", NeutronLongCmdLineName.CMD_LINE_LOAD_SEALED_AND_SENSITIVE,
          "true or false - load sealed and sensitive data, default is false", false, 1,
          Boolean.class, ',')),

  /**
   * Run full (initial) load.
   */
  FULL_LOAD(FlightPlan.makeOpt("F", NeutronLongCmdLineName.CMD_LINE_INITIAL_LOAD,
      "Run full (initial) load", false, 0, Boolean.class, ',')),

  /**
   * Refresh materialized query tables for full (initial) load.
   */
  REFRESH_MQT(FlightPlan.makeOpt("M", NeutronLongCmdLineName.CMD_LINE_REFRESH_MQT,
      "Refresh MQT for initial load", false, 0, Boolean.class, ',')),

  /**
   * Drop index before running full (initial) load.
   */
  DROP_INDEX(FlightPlan.makeOpt("D", NeutronLongCmdLineName.CMD_LINE_DROP_INDEX,
      "Drop index for full (initial) load", false, 0, Boolean.class, ',')),

  /**
   * Test mode!
   */
  SIMULATE_LAUNCH(FlightPlan.makeOpt("S", NeutronLongCmdLineName.CMD_LINE_SIMULATE_LAUNCH,
      "Simulate launch (test mode)", false, 0, Boolean.class, ','))

  ;

  private final Option opt;

  NeutronCmdLineOption(Option opt) {
    this.opt = opt;
  }

  /**
   * Getter for the type's command line option definition.
   * 
   * @return command line option definition
   */
  public final Option getOpt() {
    return opt;
  }

}
