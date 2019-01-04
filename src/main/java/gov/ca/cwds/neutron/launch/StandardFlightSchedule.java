package gov.ca.cwds.neutron.launch;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.listeners.JobChainingJobListener;

import gov.ca.cwds.jobs.ClientPersonIndexerJob;
import gov.ca.cwds.jobs.CollateralIndividualIndexerJob;
import gov.ca.cwds.jobs.CollateralIndividualSIndexerJob;
import gov.ca.cwds.jobs.EducationProviderContactIndexerJob;
import gov.ca.cwds.jobs.EducationProviderContactSIndexerJob;
import gov.ca.cwds.jobs.IntakeScreeningJob;
import gov.ca.cwds.jobs.OtherAdultInPlacemtHomeIndexerJob;
import gov.ca.cwds.jobs.OtherAdultInPlacemtHomeSIndexerJob;
import gov.ca.cwds.jobs.OtherChildInPlacemtHomeIndexerJob;
import gov.ca.cwds.jobs.OtherChildInPlacemtHomeSIndexerJob;
import gov.ca.cwds.jobs.ReferralHistoryIndexerJob;
import gov.ca.cwds.jobs.RelationshipIndexerJob;
import gov.ca.cwds.jobs.ReporterIndexerJob;
import gov.ca.cwds.jobs.ReporterSIndexerJob;
import gov.ca.cwds.jobs.ServiceProviderIndexerJob;
import gov.ca.cwds.jobs.ServiceProviderSIndexerJob;
import gov.ca.cwds.jobs.SubstituteCareProviderIndexJob;
import gov.ca.cwds.jobs.SubstituteCareProviderSIndexJob;
import gov.ca.cwds.neutron.enums.NeutronSchedulerConstants;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.rocket.CaseRocket;
import gov.ca.cwds.neutron.rocket.ExitInitialLoadRocket;
import gov.ca.cwds.neutron.rocket.IndexResetPeopleRocket;
import gov.ca.cwds.neutron.rocket.IndexResetPeopleSummaryRocket;
import gov.ca.cwds.neutron.rocket.LightThisCandleRocket;
import gov.ca.cwds.neutron.rocket.ReplicationLagRocket;
import gov.ca.cwds.neutron.rocket.SchemaResetRocket;
import gov.ca.cwds.neutron.rocket.VoxListenerRocket;

/**
 * Standard rocket settings for both modes, Initial Load and Last Change (on-going, continuous).
 * 
 * @author CWDS API Team
 * @see LaunchPad#schedule()
 */
public enum StandardFlightSchedule {

  // ===============================
  // RECREATE INDEXES:
  // ===============================

  /**
   * Dummy rocket. Just starts the Quartz scheduler process.
   */
  LIGHT_THIS_CANDLE(LightThisCandleRocket.class, // rocket class
      "light_this_candle", // rocket name
      1, // initial load order
      0, // start delay seconds. N/A.
      10000, // execute every N seconds. N/A.
      null, // last run priority. N/A.
      false, // run in Last Change mode
      true, // run in Initial Load
      false // People index
  ),

  /**
   * If requested, drop and create Elasticsearch People index.
   */
  RESET_PEOPLE_INDEX(IndexResetPeopleRocket.class, // rocket class
      "reset_people_index", // rocket name
      2, // initial load order
      3, // start delay seconds. N/A.
      10000, // execute every N seconds. N/A.
      null, // last run priority. N/A.
      false, // run in Last Change mode
      true, // run in Initial Load
      true // People index
  ),

  /**
   * If requested, drop and create Elasticsearch People Summary index.
   */
  RESET_PEOPLE_SUMMARY_INDEX(IndexResetPeopleSummaryRocket.class, // rocket class
      "reset_people_summary_index", // rocket name
      4, // initial load order
      200000000, // start delay seconds. N/A.
      10000, // execute every N seconds. N/A.
      null, // last run priority. N/A.
      false, // run in Last Change mode
      true, // run in Initial Load
      false // People index
  ),

  /**
   * If requested, drop and create Elasticsearch People Summary index.
   */
  VOX_ROCKET(VoxListenerRocket.class, // rocket class
      "vox", // rocket name
      5, // initial load order
      20, // start delay seconds. N/A.
      60000, // execute every N seconds. N/A.
      null, // last run priority. N/A.
      true, // run in Last Change mode
      false, // run in Initial Load
      false // People index
  ),

  /**
   * Periodically measure replication lag.
   */
  REPLICATION_TIME(ReplicationLagRocket.class, // rocket class
      "replication_time", // rocket name
      6, // start delay seconds.
      45, // execute every N seconds.
      800, // last run priority.
      null, // N/A
      true, // run in Last Change mode
      false, // run in Initial Load
      false // People index
  ),

  // ===============================
  // PEOPLE SUMMARY INDEX ROCKETS:
  // ===============================

  // private StandardFlightSchedule(Class<?> klazz, String rocketName, int startDelaySeconds,
  // int waitPeriodSeconds, int lastRunPriority, String nestedElement, boolean runLastChange,
  // boolean runInitialLoad, boolean forPeopleIndex) {

  /**
   * People Summary index.
   */
  PEOPLE_SUMMARY(ClientPersonIndexerJob.class, // rocket class
      "people_summary", // rocket name
      12, // start delay in seconds.
      3, // execute every N seconds.
      10000, // last run priority. Highest wins.
      null, // nested element: N/A
      true, // run in Last Change mode
      true, // run in Initial Load
      false // for People Index
  ),

  /**
   * Document root: Reporter.
   */
  REPORTER_S(ReporterSIndexerJob.class, "ps_reporter", 30, 25, 950, null, true, true, false),

  /**
   * Document root: Collateral Individual.
   */
  COLLATERAL_INDIVIDUAL_S(CollateralIndividualSIndexerJob.class, "ps_collateral_individual", 40, 30,
      90, null, true, true, false),

  /**
   * Document root: Service Provider.
   */
  SERVICE_PROVIDER_S(ServiceProviderSIndexerJob.class, "ps_service_provider", 65, 120, 85, null,
      true, true, false),

  /**
   * Document root: Substitute Care Provider.
   */
  SUBSTITUTE_CARE_PROVIDER_S(SubstituteCareProviderSIndexJob.class, "ps_substitute_care_provider",
      30, 25, 80, null, true, true, false),

  /**
   * Document root: Education Provider.
   */
  EDUCATION_PROVIDER_S(EducationProviderContactSIndexerJob.class, "ps_education_provider", 42, 120,
      75, null, true, true, false),

  /**
   * Document root: Other Adult in Home.
   */
  OTHER_ADULT_IN_HOME_S(OtherAdultInPlacemtHomeSIndexerJob.class, "ps_other_adult", 50, 120, 70,
      null, true, true, false),

  /**
   * Document root: Other Child in Home.
   */
  OTHER_CHILD_IN_HOME_S(OtherChildInPlacemtHomeSIndexerJob.class, "ps_other_child", 55, 120, 65,
      null, true, true, false),

  // ===============================
  // PEOPLE INDEX ROCKETS:
  // ===============================

  /**
   * Document root: Reporter.
   */
  REPORTER(ReporterIndexerJob.class, "reporter", 14, 30, 950, null, true, true, true),

  /**
   * Document root: Collateral Individual.
   */
  COLLATERAL_INDIVIDUAL(CollateralIndividualIndexerJob.class, "collateral_individual", 20, 30, 90,
      null, true, true, true),

  /**
   * Document root: Service Provider.
   */
  SERVICE_PROVIDER(ServiceProviderIndexerJob.class, "service_provider", 25, 120, 85, null, true,
      true, true),

  /**
   * Document root: Substitute Care Provider.
   */
  SUBSTITUTE_CARE_PROVIDER(SubstituteCareProviderIndexJob.class, "substitute_care_provider", 30, 25,
      80, null, true, true, true),

  /**
   * Document root: Education Provider.
   */
  EDUCATION_PROVIDER(EducationProviderContactIndexerJob.class, "education_provider", 42, 120, 75,
      null, true, true, true),

  OTHER_ADULT_IN_HOME(OtherAdultInPlacemtHomeIndexerJob.class, "other_adult", 50, 120, 70, null,
      true, true, true),

  OTHER_CHILD_IN_HOME(OtherChildInPlacemtHomeIndexerJob.class, "other_child", 55, 120, 65, null,
      true, true, true),

  //
  // Nested JSON elements, inside a people/person document.
  //

  /**
   * Combines child and parent case.
   */
  CASES(CaseRocket.class, "case", 70, 30, 550, "cases", true, true, true),

  /**
   * Relationships.
   */
  RELATIONSHIP(RelationshipIndexerJob.class, "relationship", 90, 30, 600, "relationships", true,
      true, true),

  /**
   * Referrals.
   */
  REFERRAL(ReferralHistoryIndexerJob.class, "referral", 45, 30, 700, "referrals", true, true, true),

  // ===============================
  // SCREENINGS:
  // ===============================

  /**
   * Screenings in People index, <strong>NOT</strong> the separate Screenings index.
   */
  INTAKE_SCREENING(IntakeScreeningJob.class, "intake_screening", 90, 20, 900, "screenings", true,
      true, true),

  // ===============================
  // DB2 SCHEMA RESET:
  // ===============================

  /**
   * Reset test schema. Automatic prevents reset of production-like schemas.
   */
  RESET_TEST_SCHEMA(SchemaResetRocket.class, "reset_schema", 2000, 2000000, 10000, null, false,
      false, true),

  // ===============================
  // UTILITY:
  // ===============================

  /**
   * Exit the initial load process.
   */
  EXIT_INITIAL_LOAD(ExitInitialLoadRocket.class, "exit_initial_load", 140, 2000000, 10000, null,
      false, true, false),

  ;

  private static final ConditionalLogger LOGGER = new JetPackLogger(StandardFlightSchedule.class);

  private final Class<?> klazz;

  private final boolean runLastChange;

  private final boolean runInitialLoad;

  private final boolean forPeopleIndex;

  private final String rocketName;

  private final int initialLoadOrder = ordinal();

  private final int startDelaySeconds;

  private final int waitPeriodSeconds;

  private final int lastRunPriority;

  private final String nestedElement;

  private static final Map<String, StandardFlightSchedule> mapName;

  private static final Map<Class<?>, StandardFlightSchedule> mapClass;

  static {
    final Map<String, StandardFlightSchedule> xMapName = new HashMap<>(31);
    final Map<Class<?>, StandardFlightSchedule> xMapClass = new HashMap<>(31);

    for (StandardFlightSchedule sched : StandardFlightSchedule.values()) {
      xMapName.put(sched.rocketName, sched);
      xMapClass.put(sched.klazz, sched);
    }

    mapName = Collections.unmodifiableMap(xMapName);
    mapClass = Collections.unmodifiableMap(xMapClass);
  }

  private StandardFlightSchedule(Class<?> klazz, String rocketName, int startDelaySeconds,
      int waitPeriodSeconds, int lastRunPriority, String nestedElement, boolean runLastChange,
      boolean runInitialLoad, boolean forPeopleIndex) {
    this.klazz = klazz;
    this.rocketName = rocketName;
    this.startDelaySeconds = startDelaySeconds;
    this.waitPeriodSeconds = waitPeriodSeconds;
    this.lastRunPriority = lastRunPriority;
    this.nestedElement = nestedElement;
    this.runLastChange = runLastChange;
    this.runInitialLoad = runInitialLoad;
    this.forPeopleIndex = forPeopleIndex;
  }

  /**
   * A JobChainingJobListener executes Quartz jobs in sequence by blocking scheduled triggers.
   * Appropriate for initial load, not last change.
   * 
   * @param loadPeopleIndex launch People index rockets (Snapshot version less than 1.1)
   * @param excludeRockets optionally exclude rockets
   * @return Quartz JobChainingJobListener
   */
  public static JobChainingJobListener buildInitialLoadJobChainListener(boolean loadPeopleIndex,
      Set<StandardFlightSchedule> excludeRockets) {
    final JobChainingJobListener ret =
        new JobChainingJobListener(NeutronSchedulerConstants.GRP_FULL_LOAD);

    final StandardFlightSchedule[] rawArr = getInitialLoadRockets(loadPeopleIndex, excludeRockets)
        .toArray(new StandardFlightSchedule[0]);

    final StandardFlightSchedule[] arr = Arrays.copyOf(rawArr, rawArr.length);
    Arrays.sort(arr, (o1, o2) -> Integer.compare(o1.initialLoadOrder, o2.initialLoadOrder));

    StandardFlightSchedule sched;
    final int len = arr.length;

    for (int i = 0; i < len; i++) {
      sched = arr[i];
      final String first = sched.getRocketName();
      final String second = i != (len - 1) ? arr[i + 1].rocketName : "exit_initial_load";
      LOGGER.info("intial load order: {} => {}", first, second);

      ret.addJobChainLink(new JobKey(first, NeutronSchedulerConstants.GRP_FULL_LOAD),
          new JobKey(second, NeutronSchedulerConstants.GRP_FULL_LOAD));
    }

    return ret;
  }

  /**
   * Gets the default list of rockets for initial load.
   * 
   * @param loadPeopleIndex launch People index rockets (Snapshot version less than 1.1)
   * @param excludeRockets optionally exclude rockets
   * @return rockets for initial load
   */
  public static List<StandardFlightSchedule> getInitialLoadRockets(boolean loadPeopleIndex,
      Set<StandardFlightSchedule> excludeRockets) {
    return Arrays.asList(values()).stream().sequential()
        .sorted(Comparator.comparingInt(StandardFlightSchedule::getInitialLoadOrder))
        .filter(StandardFlightSchedule::isRunInitialLoad)
        .filter(s -> !s.isForPeopleIndex() || (loadPeopleIndex && s.isForPeopleIndex()))
        .filter(s -> !excludeRockets.contains(s)).collect(Collectors.toList());
  }

  /**
   * Builds rocket flight schedule for on-going polling mode (last change).
   * 
   * @param loadPeopleIndex launch People index rockets (Snapshot version less than 1.1)
   * @param excludeRockets optionally exclude rockets
   * @return rockets for last run
   */
  public static List<StandardFlightSchedule> getLastChangeRockets(boolean loadPeopleIndex,
      Set<StandardFlightSchedule> excludeRockets) {
    return Arrays.asList(values()).stream().sequential()
        .filter(StandardFlightSchedule::isRunLastChange)
        .filter(s -> !s.isForPeopleIndex() || (loadPeopleIndex && s.isForPeopleIndex()))
        .filter(s -> !excludeRockets.contains(s)).collect(Collectors.toList());
  }

  public Class<?> getRocketClass() {
    return klazz;
  }

  public String getRocketName() {
    return rocketName;
  }

  public boolean isNewDocument() {
    return StringUtils.isBlank(this.nestedElement);
  }

  public int getStartDelaySeconds() {
    return startDelaySeconds;
  }

  public int getWaitPeriodSeconds() {
    return waitPeriodSeconds;
  }

  public int getLastRunPriority() {
    return lastRunPriority;
  }

  public String getNestedElement() {
    return nestedElement;
  }

  public static StandardFlightSchedule lookupByRocketName(String rocketName) {
    return mapName.get(rocketName.trim());
  }

  public static StandardFlightSchedule lookupByRocketClass(Class<?> klazz) {
    return mapClass.get(klazz);
  }

  public int getInitialLoadOrder() {
    return initialLoadOrder;
  }

  public Class<?> getKlazz() {
    return klazz;
  }

  public boolean isRunLastChange() {
    return runLastChange;
  }

  public boolean isRunInitialLoad() {
    return runInitialLoad;
  }

  public boolean isForPeopleIndex() {
    return forPeopleIndex;
  }

}
