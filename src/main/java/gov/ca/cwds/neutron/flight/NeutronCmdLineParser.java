package gov.ca.cwds.neutron.flight;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.tuple.Pair;

public class NeutronCmdLineParser {

  /**
   * Define a command line option.
   * 
   * @param shortOpt single letter option name
   * @param longOpt long option name
   * @param description option description
   * @param required true if required
   * @param argc number of arguments to this option
   * @param type arguments Java class
   * @param sep argument separator
   * @return command line option
   */
  public static Option makeOpt(String shortOpt, String longOpt, String description,
      boolean required, int argc, Class<?> type, char sep) {
    return Option.builder(shortOpt).argName(longOpt).required(required).longOpt(longOpt)
        .desc(description).numberOfArgs(argc).type(type).valueSeparator(sep).build();
  }

  /**
   * Define command line options.
   * 
   * @return command line option definitions
   */
  public static Options buildCmdLineOptions() {
    final Options ret = new Options();

    ret.addOption(NeutronCmdLineOption.SIMULATE_LAUNCH.getOpt());
    ret.addOption(NeutronCmdLineOption.LEGACY_PEOPLE_MAPPING.getOpt());
    ret.addOption(NeutronCmdLineOption.ES_CONFIG_PEOPLE.getOpt());
    ret.addOption(NeutronCmdLineOption.ES_CONFIG_PEOPLE_SUMMARY.getOpt());
    ret.addOption(NeutronCmdLineOption.INDEX_NAME.getOpt());
    ret.addOption(NeutronCmdLineOption.THREADS.getOpt());
    ret.addOption(NeutronCmdLineOption.LOAD_SEALED_SENSITIVE.getOpt());

    ret.addOption(NeutronCmdLineOption.LAST_START_TIME.getOpt());
    ret.addOption(NeutronCmdLineOption.LAST_END_TIME.getOpt());

    ret.addOption(NeutronCmdLineOption.FULL_LOAD.getOpt());
    ret.addOption(NeutronCmdLineOption.REFRESH_MQT.getOpt());
    ret.addOption(NeutronCmdLineOption.DROP_INDEX.getOpt());

    ret.addOption(NeutronCmdLineOption.NO_INDEX_PEOPLE.getOpt());
    ret.addOption(NeutronCmdLineOption.EXCLUDE_ROCKETS.getOpt());
    ret.addOption(NeutronCmdLineOption.KEY_BUNDLE_SIZE.getOpt());
    ret.addOption(NeutronCmdLineOption.VALIDATE_INDEXED_DOCS.getOpt());
    ret.addOption(NeutronCmdLineOption.FORCE_PARTITIONS.getOpt());

    ret.addOption(NeutronCmdLineOption.BUCKET_RANGE.getOpt());
    ret.addOption(NeutronCmdLineOption.MIN_ID.getOpt());
    ret.addOption(NeutronCmdLineOption.MAX_ID.getOpt());

    // RUN MODE: mutually exclusive choice.
    // Either provide many last run files in base directory or provide a single last run file.
    final OptionGroup group = new OptionGroup();
    group.setRequired(true);
    group.addOption(NeutronCmdLineOption.LAST_RUN_FILE.getOpt());
    group.addOption(NeutronCmdLineOption.BASE_DIRECTORY.getOpt());
    ret.addOptionGroup(group);

    return ret;
  }

  /**
   * Parse range buckets for Initial Load.
   * 
   * @param vals range in format {@code 1-20}
   * @return range start and end
   */
  public static Pair<Long, Long> parseBuckets(final String[] vals) {
    Long startBucket = Long.MIN_VALUE;
    Long endBucket = startBucket;

    // Appease SonarQube.
    int cntr = 0;
    for (String val : vals) {
      if (cntr++ == 0) {
        startBucket = Long.valueOf(val);
      } else {
        endBucket = Long.valueOf(val);
      }
    }

    return Pair.of(startBucket, endBucket);
  }

}
