package gov.ca.cwds.neutron.flight;

import static gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils.freshDate;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;

/**
 * Represents batch rocket options from the command line.
 * 
 * @author CWDS API Team
 */
public class FlightPlan implements ApiMarker {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(FlightPlan.class);

  /**
   * Location of Elasticsearch configuration file for people index.
   */
  String esConfigPeopleLoc;

  /**
   * Location of Elasticsearch configuration file for people summary index.
   */
  String esConfigPeopleSummaryLoc;

  /**
   * Name of index to create or use. If this is not provided then alias is used from ES Config file.
   */
  private String indexName;

  private boolean simulateLaunch;

  private boolean legacyPeopleMapping;

  private boolean loadPeopleIndex;

  /**
   * Last time rocket was executed in format 'yyyy-MM-dd HH.mm.ss' If this is provided then time
   * stamp given in last run time file is ignored.
   */
  private Date overrideLastStartTime;

  /**
   * Optional end date for standalone last change runs. Pseudo code:
   * {@code BETWEEN :overrideLastRunTime AND overrideLastEndTime}.
   * 
   * <p>
   * If not provided, defaults to current timestamp.
   * </p>
   */
  private Date overrideLastEndTime;

  /**
   * Location of last run file.
   */
  private String lastRunLoc;

  /**
   * Whether to run in periodic "last run" mode or "initial" mode.
   */
  boolean lastRunMode = true;

  /**
   * Debug mode. Monitor database connections.
   */
  private boolean debug = false;

  /**
   * When running in "initial load" mode, specifies the starting bucket of records to be processed
   * by this rocket.
   * <p>
   * Required for "initial load" mode.
   * </p>
   */
  private long startBucket = 1L;

  /**
   * When running in "initial load" mode, specifies the ending bucket of records to be processed by
   * this rocket.
   * <p>
   * Required for "initial load" mode.
   * </p>
   */
  private long endBucket = 1L;

  /**
   * Total threads to allocate to this batch run. Defaults to all available cores.
   */
  private long threadCount;

  /**
   * If true then load sealed and sensitive data.
   */
  private boolean loadSealedAndSensitive;

  private boolean rangeGiven;

  private String baseDirectory;

  private boolean refreshMqt;

  private boolean dropIndex;

  private boolean validateAfterIndexing = false;

  private boolean lastChangeDynamicSql = false;

  private Set<StandardFlightSchedule> excludedRockets = new HashSet<>();

  private Deque<String> dequeRerunIds = new ConcurrentLinkedDeque<>();

  /**
   * Default constructor.
   */
  public FlightPlan() {
    // Default constructor
  }

  /**
   * Construct from all settings.
   * 
   * @param esConfigPeopleLoc location of Elasticsearch configuration file for people index
   * @param esConfigPeopleSummaryLoc location of Elasticsearch configuration file for people summary
   *        index
   * @param indexName Name of index to use. If not provided, then use alias from ES config.
   * @param lastStartTime override last start time\
   * @param lastEndTime override Last end time
   * @param lastRunLoc location of last run file
   * @param lastRunMode is last run mode or not
   * @param startBucket starting bucket number
   * @param endBucket ending bucket number
   * @param threadCount number of simultaneous threads
   * @param loadSealedAndSensitive If true then load sealed and sensitive data
   * @param rangeGiven initial load -- provided range (full load only)
   * @param baseDirectory base folder for rocket execution (full load only)
   * @param refreshMqt refresh materialized query tables (full load only)
   * @param dropIndex drop the index before start (full load only)
   * @param simulateLaunch simulate launch (test mode!)
   * @param legacyPeopleMapping use Snapshot 0.9 mapping for People index
   * @param loadPeopleIndex launch rockets for People index
   * @param debug debug mode
   * @param lastChangeStaticSql use static SQL for Last Change mode "what changed" query
   * @param excludedRockets optionally turn off rockets
   */
  public FlightPlan(String esConfigPeopleLoc, String esConfigPeopleSummaryLoc, String indexName,
      Date lastStartTime, Date lastEndTime, String lastRunLoc, boolean lastRunMode,
      long startBucket, long endBucket, long threadCount, boolean loadSealedAndSensitive,
      boolean rangeGiven, String baseDirectory, boolean refreshMqt, boolean dropIndex,
      boolean simulateLaunch, boolean legacyPeopleMapping, boolean loadPeopleIndex, boolean debug,
      boolean lastChangeStaticSql, Set<StandardFlightSchedule> excludedRockets) {
    this.esConfigPeopleLoc = esConfigPeopleLoc;
    this.esConfigPeopleSummaryLoc = esConfigPeopleSummaryLoc;
    this.indexName = StringUtils.trimToNull(indexName);
    this.overrideLastStartTime = freshDate(lastStartTime);
    this.overrideLastEndTime = freshDate(lastEndTime);
    this.lastRunLoc = lastRunLoc;
    this.lastRunMode = lastRunMode;
    this.startBucket = startBucket;
    this.endBucket = endBucket;
    this.threadCount = threadCount;
    this.loadSealedAndSensitive = loadSealedAndSensitive;
    this.rangeGiven = rangeGiven;
    this.baseDirectory = baseDirectory;
    this.refreshMqt = refreshMqt;
    this.dropIndex = dropIndex;
    this.simulateLaunch = simulateLaunch;
    this.legacyPeopleMapping = legacyPeopleMapping;
    this.loadPeopleIndex = loadPeopleIndex;
    this.debug = debug;
    this.excludedRockets = excludedRockets;
  }

  /**
   * Copy constructor.
   * 
   * @param fp other rocket options
   */
  public FlightPlan(final FlightPlan fp) {
    this.esConfigPeopleLoc = fp.esConfigPeopleLoc;
    this.esConfigPeopleSummaryLoc = fp.esConfigPeopleSummaryLoc;
    this.indexName = StringUtils.trimToNull(fp.indexName);
    this.overrideLastStartTime = fp.overrideLastStartTime;
    this.overrideLastEndTime = fp.overrideLastEndTime;
    this.lastRunLoc = fp.lastRunLoc;
    this.lastRunMode = fp.lastRunMode;
    this.startBucket = fp.startBucket;
    this.endBucket = fp.endBucket;
    this.threadCount = fp.threadCount;
    this.loadSealedAndSensitive = fp.loadSealedAndSensitive;
    this.rangeGiven = fp.rangeGiven;
    this.baseDirectory = fp.baseDirectory;
    this.refreshMqt = fp.refreshMqt;
    this.dropIndex = fp.dropIndex;
    this.simulateLaunch = fp.simulateLaunch;
    this.legacyPeopleMapping = fp.legacyPeopleMapping;
    this.loadPeopleIndex = fp.loadPeopleIndex;
    this.excludedRockets = fp.excludedRockets;
    this.debug = fp.debug;
    this.lastChangeDynamicSql = fp.lastChangeDynamicSql;
  }

  /**
   * Smart/auto mode. If last run date is older than 25 years, assume initial load. Written when
   * DevOps started using Rundeck and was unable to pass parameters to jobs.
   * 
   * <p>
   * HACK: This approach was concocted because Rundeck jobs were not configured to accept
   * parameters.
   * </p>
   * 
   * @param lastRun last successful run date
   * @return true if running initial load
   */
  public boolean determineInitialLoad(final Date lastRun) {
    LOGGER.debug("Last successsful run time: {}", lastRun); // NOSONAR

    final Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -25);
    return !isLastRunMode() || lastRun.before(cal.getTime());
  }

  /**
   * Getter for location of Elasticsearch configuration file.
   * 
   * @return location of Elasticsearch configuration file
   */
  public String getEsConfigLoc() {
    return esConfigPeopleLoc;
  }

  /**
   * Get name of the index to create or use.
   * 
   * @return Name of the index to use.
   */
  public String getIndexName() {
    return indexName;
  }

  /**
   * Get last run time override in format 'yyyy-MM-dd HH.mm.ss'. If this is non-null then time
   * provided in last run time file is ignored.
   * 
   * @return Last run time
   */
  public Date getOverrideLastRunStartTime() {
    return overrideLastStartTime != null ? new Date(overrideLastStartTime.getTime()) : null;
  }

  /**
   * Getter for location of last run date/time file.
   * 
   * @return location of last run file
   */
  public String getLastRunLoc() {
    return lastRunLoc;
  }

  public void setLastRunLoc(String lastRunLoc) {
    this.lastRunLoc = lastRunLoc;
  }

  /**
   * Getter for last run mode.
   * 
   * @return last run mode
   */
  public boolean isLastRunMode() {
    return lastRunMode;
  }

  /**
   * Getter for starting bucket.
   * 
   * @return starting bucket
   */
  public long getStartBucket() {
    return startBucket;
  }

  /**
   * Getter for last bucket.
   * 
   * @return last bucket
   */
  public long getEndBucket() {
    return endBucket;
  }

  /**
   * Getter for thread count.
   * 
   * @return thread count
   */
  public long getThreadCount() {
    return threadCount;
  }

  /**
   * Get if sealed and sensitive data should be loaded.
   * 
   * @return true if sealed and sensitive data should be loaded, false otherwise.
   */
  public boolean isLoadSealedAndSensitive() {
    return loadSealedAndSensitive;
  }

  /**
   * Pretty print usage.
   * 
   * @throws NeutronCheckedException on IO exception
   */
  protected static void printUsage() throws NeutronCheckedException {
    try (final StringWriter sw = new StringWriter()) {
      final String pad = StringUtils.leftPad("", 90, '=');
      new HelpFormatter().printHelp(new PrintWriter(sw), 100, "Batch loader",
          pad + "\nUSAGE: java <rocket class> ...\n" + pad,
          NeutronCmdLineParser.buildCmdLineOptions(), 4, 8, pad, true);
      LOGGER.error(sw.toString()); // NOSONAR
    } catch (IOException e) {
      throw CheeseRay.checked(LOGGER, e, "INCORRECT USAGE! {}", e.getMessage());
    }
  }

  /**
   * Parse the command line return the rocket settings.
   * 
   * @param args command line to parse
   * @return JobOptions defining this rocket
   * @throws NeutronCheckedException if unable to parse command line
   */
  public static FlightPlan parseCommandLine(final String[] args) throws NeutronCheckedException {
    String esConfigPeopleLoc = null;
    String esConfigPeopleSummaryLoc = null;
    String indexName = null;
    String lastRunLoc = null;
    String baseDirectory = null;

    Date lastStartTime = null;
    Date lastEndTime = null;
    long threadCount = 0L;

    boolean lastRunMode = true;
    boolean loadSealedAndSensitive = false;
    boolean rangeGiven = false;
    boolean refreshMqt = false;
    boolean dropIndex = false;
    boolean simulateLaunch = false;
    boolean legacyPeopleMapping = false;
    boolean loadPeopleIndex = true;
    boolean debug = false;

    // CHECKSTYLE:OFF
    Pair<Long, Long> bucketRange = Pair.of(-1L, 0L);
    // CHECKSTYLE:ON

    Set<StandardFlightSchedule> excludedRockets = new HashSet<>();

    try {
      final Options options = NeutronCmdLineParser.buildCmdLineOptions();
      final CommandLineParser parser = new DefaultParser();
      final CommandLine cmd = parser.parse(options, args);

      // Java clincher: case statements only take constants. Even compile-time constants, like
      // enum members (evaluated at compile time), are not considered "constants."
      for (final Option opt : cmd.getOptions()) {
        switch (opt.getArgName()) {
          case NeutronLongCmdLineName.CMD_LINE_ES_CONFIG_PEOPLE:
            esConfigPeopleLoc = opt.getValue().trim();
            break;

          case NeutronLongCmdLineName.CMD_LINE_ES_CONFIG_PEOPLE_SUMMARY:
            esConfigPeopleSummaryLoc = opt.getValue().trim();
            break;

          case NeutronLongCmdLineName.CMD_LINE_INDEX_NAME:
            indexName = opt.getValue().trim();
            break;

          case NeutronLongCmdLineName.CMD_LINE_LAST_START_TIME:
            lastRunMode = true;
            lastStartTime = createDate(opt.getValue().trim());
            break;

          case NeutronLongCmdLineName.CMD_LINE_LAST_END_TIME:
            lastRunMode = true;
            lastEndTime = createDate(opt.getValue().trim());
            break;

          case NeutronLongCmdLineName.CMD_LINE_LAST_RUN_FILE:
            lastRunLoc = opt.getValue().trim();
            break;

          case NeutronLongCmdLineName.CMD_LINE_BASE_DIRECTORY:
            baseDirectory = opt.getValue().trim();
            break;

          case NeutronLongCmdLineName.CMD_LINE_INITIAL_LOAD:
            lastRunMode = false;
            break;

          case NeutronLongCmdLineName.CMD_LINE_BUCKET_RANGE:
            lastRunMode = false;
            rangeGiven = true;
            bucketRange = NeutronCmdLineParser.parseBuckets(opt.getValues());
            break;

          case NeutronLongCmdLineName.CMD_LINE_THREADS:
            threadCount = Long.parseLong(opt.getValue());
            break;

          case NeutronLongCmdLineName.CMD_LINE_LOAD_SEALED_AND_SENSITIVE:
            loadSealedAndSensitive = Boolean.parseBoolean(opt.getValue().trim());
            break;

          case NeutronLongCmdLineName.CMD_LINE_REFRESH_MQT:
            lastRunMode = false;
            refreshMqt = true;
            break;

          case NeutronLongCmdLineName.CMD_LINE_DROP_INDEX:
            lastRunMode = false;
            dropIndex = true;
            break;

          case NeutronLongCmdLineName.CMD_LINE_SIMULATE_LAUNCH:
            simulateLaunch = true;
            break;

          case NeutronLongCmdLineName.CMD_LINE_DEBUG:
            debug = true;
            break;

          case NeutronLongCmdLineName.CMD_LINE_LEGACY_PEOPLE_MAPPING:
            legacyPeopleMapping = true;
            break;

          case NeutronLongCmdLineName.CMD_LINE_NO_PEOPLE_INDEX:
            loadPeopleIndex = false;
            break;

          case NeutronLongCmdLineName.CMD_LINE_EXCLUDE_ROCKETS:
            excludedRockets = Arrays.asList(opt.getValue().trim().split(",")).stream()
                .map(StandardFlightSchedule::lookupByRocketName).collect(Collectors.toSet());
            break;

          default:
            throw new IllegalArgumentException(opt.getArgName());
        }
      }
    } catch (IllegalArgumentException | java.text.ParseException | ParseException e) {
      printUsage();
      throw CheeseRay.checked(LOGGER, e, "INVALID ARGS", e.getMessage(), e);
    }

    return new FlightPlan(esConfigPeopleLoc, esConfigPeopleSummaryLoc, indexName, lastStartTime,
        lastEndTime, lastRunLoc, lastRunMode, bucketRange.getLeft(), bucketRange.getRight(),
        threadCount, loadSealedAndSensitive, rangeGiven, baseDirectory, refreshMqt, dropIndex,
        simulateLaunch, legacyPeopleMapping, loadPeopleIndex, debug, true, excludedRockets);
  }

  public void setStartBucket(long startBucket) {
    this.startBucket = startBucket;
  }

  public void setEndBucket(long endBucket) {
    this.endBucket = endBucket;
  }

  public void setThreadCount(long threadCount) {
    this.threadCount = threadCount;
  }

  public void setIndexName(String indexName) {
    this.indexName = indexName;
  }

  public static Date createDate(String timestamp) throws java.text.ParseException {
    Date ret = null;
    final String trimTimestamp = StringUtils.trim(timestamp);
    if (StringUtils.isNotEmpty(trimTimestamp)) {
      ret = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(trimTimestamp);
    }
    return ret;
  }

  /**
   * Did the caller request to run ranges? If so, then don't create a new index or swap index
   * aliases.
   * 
   * @return true if {@code -r} was passed
   */
  public boolean isRangeGiven() {
    return rangeGiven;
  }

  public void setLoadSealedAndSensitive(boolean loadSealedAndSensitive) {
    this.loadSealedAndSensitive = loadSealedAndSensitive;
  }

  public void setRangeGiven(boolean rangeGiven) {
    this.rangeGiven = rangeGiven;
  }

  public void setLastRunMode(boolean flag) {
    this.lastRunMode = flag;
  }

  public String getBaseDirectory() {
    return baseDirectory;
  }

  public void setBaseDirectory(String baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  public void setOverrideLastRunTime(Date lastRunTime) {
    this.overrideLastStartTime = freshDate(lastRunTime);
  }

  public boolean isRefreshMqt() {
    return refreshMqt;
  }

  public void setRefreshMqt(boolean refreshMqt) {
    this.refreshMqt = refreshMqt;
  }

  public boolean isDropIndex() {
    return dropIndex;
  }

  public void setDropIndex(boolean dropIndex) {
    this.dropIndex = dropIndex;
  }

  public void setEsConfigLoc(String esConfigLoc) {
    this.esConfigPeopleLoc = esConfigLoc;
  }

  public boolean isSimulateLaunch() {
    return simulateLaunch;
  }

  public void setSimulateLaunch(boolean testMode) {
    this.simulateLaunch = testMode;
  }

  public String getEsConfigPeopleLoc() {
    return esConfigPeopleLoc;
  }

  public String getEsConfigPeopleSummaryLoc() {
    return esConfigPeopleSummaryLoc;
  }

  public Date getOverrideLastEndTime() {
    return freshDate(overrideLastEndTime);
  }

  public void setOverrideLastEndTime(Date overrideLastEndTime) {
    this.overrideLastEndTime = freshDate(overrideLastEndTime);
  }

  public boolean isLegacyPeopleMapping() {
    return legacyPeopleMapping;
  }

  public void setLegacyPeopleMapping(boolean legacyPeopleMapping) {
    this.legacyPeopleMapping = legacyPeopleMapping;
  }

  public boolean isLoadPeopleIndex() {
    return loadPeopleIndex;
  }

  public void setLoadPeopleIndex(boolean loadPeopleIndex) {
    this.loadPeopleIndex = loadPeopleIndex;
  }

  public Set<StandardFlightSchedule> getExcludedRockets() {
    return excludedRockets;
  }

  public void setOptPeopleRockets(Set<StandardFlightSchedule> setExcludedRockets) {
    this.excludedRockets = setExcludedRockets;
  }

  public boolean isValidateAfterIndexing() {
    return validateAfterIndexing;
  }

  public void setValidateAfterIndexing(boolean validateAfterIndexing) {
    this.validateAfterIndexing = validateAfterIndexing;
  }

  public Deque<String> getDequeRerunIds() {
    return dequeRerunIds;
  }

  public void setDequeRerunIds(Deque<String> dequeRerunIds) {
    this.dequeRerunIds = dequeRerunIds;
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public boolean isLastChangeStaticSql() {
    return lastChangeDynamicSql;
  }

  public void setLastChangeStaticSql(boolean lastChangeStaticSql) {
    this.lastChangeDynamicSql = lastChangeStaticSql;
  }

  @Override
  public String toString() {
    return "FlightPlan [esConfigPeopleLoc=" + esConfigPeopleLoc + ", esConfigPeopleSummaryLoc="
        + esConfigPeopleSummaryLoc + ", indexName=" + indexName + ", simulateLaunch="
        + simulateLaunch + ", legacyPeopleMapping=" + legacyPeopleMapping + ", loadPeopleIndex="
        + loadPeopleIndex + ", overrideLastStartTime=" + overrideLastStartTime
        + ", overrideLastEndTime=" + overrideLastEndTime + ", lastRunLoc=" + lastRunLoc
        + ", lastRunMode=" + lastRunMode + ", debug=" + debug + ", startBucket=" + startBucket
        + ", endBucket=" + endBucket + ", threadCount=" + threadCount + ", loadSealedAndSensitive="
        + loadSealedAndSensitive + ", rangeGiven=" + rangeGiven + ", baseDirectory=" + baseDirectory
        + ", refreshMqt=" + refreshMqt + ", dropIndex=" + dropIndex + ", validateAfterIndexing="
        + validateAfterIndexing + ", lastChangeStaticSql=" + lastChangeDynamicSql
        + ", excludedRockets=" + excludedRockets + ", dequeRerunIds=" + dequeRerunIds + "]";
  }

}
