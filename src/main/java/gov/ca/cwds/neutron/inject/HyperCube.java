package gov.ca.cwds.neutron.inject;

import java.io.File;
import java.util.Properties;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.NeutronGuiceModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import gov.ca.cwds.ObjectMapperUtils;
import gov.ca.cwds.common.ApiFileAssistant;
import gov.ca.cwds.dao.cms.BatchBucket;
import gov.ca.cwds.dao.cms.DbResetStatusDao;
import gov.ca.cwds.dao.cms.ReplicatedAttorneyDao;
import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.dao.cms.ReplicatedCollateralIndividualDao;
import gov.ca.cwds.dao.cms.ReplicatedEducationProviderContactDao;
import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.dao.cms.ReplicatedOtherChildInPlacemtHomeDao;
import gov.ca.cwds.dao.cms.ReplicatedOtherClientNameDao;
import gov.ca.cwds.dao.cms.ReplicatedPersonCasesDao;
import gov.ca.cwds.dao.cms.ReplicatedPersonReferralsDao;
import gov.ca.cwds.dao.cms.ReplicatedRelationshipsDao;
import gov.ca.cwds.dao.cms.ReplicatedReporterDao;
import gov.ca.cwds.dao.cms.ReplicatedSafetyAlertsDao;
import gov.ca.cwds.dao.cms.ReplicatedServiceProviderDao;
import gov.ca.cwds.dao.cms.ReplicatedSubstituteCareProviderDao;
import gov.ca.cwds.dao.cms.StaffPersonDao;
import gov.ca.cwds.dao.ns.EsIntakeScreeningDao;
import gov.ca.cwds.data.CmsSystemCodeSerializer;
import gov.ca.cwds.data.cms.SystemCodeDao;
import gov.ca.cwds.data.cms.SystemMetaDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.cms.DatabaseResetEntry;
import gov.ca.cwds.data.persistence.cms.EsChildPersonCase;
import gov.ca.cwds.data.persistence.cms.EsClientAddress;
import gov.ca.cwds.data.persistence.cms.EsClientPerson;
import gov.ca.cwds.data.persistence.cms.EsParentPersonCase;
import gov.ca.cwds.data.persistence.cms.EsPersonReferral;
import gov.ca.cwds.data.persistence.cms.EsRelationship;
import gov.ca.cwds.data.persistence.cms.EsSafetyAlert;
import gov.ca.cwds.data.persistence.cms.StaffPerson;
import gov.ca.cwds.data.persistence.cms.SystemCode;
import gov.ca.cwds.data.persistence.cms.SystemMeta;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedAttorney;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClientAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedCollateralIndividual;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedEducationProviderContact;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherChildInPlacemtHome;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherClientName;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedReporter;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedServiceProvider;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedSubstituteCareProvider;
import gov.ca.cwds.data.persistence.cms.rep.SimpleReplicatedClient;
import gov.ca.cwds.data.persistence.ns.EsIntakeScreening;
import gov.ca.cwds.data.persistence.ns.IntakeScreening;
import gov.ca.cwds.inject.CmsSessionFactory;
import gov.ca.cwds.inject.NsSessionFactory;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomCommandCenterConsole;
import gov.ca.cwds.neutron.atom.AtomFlightPlanManager;
import gov.ca.cwds.neutron.atom.AtomFlightRecorder;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.atom.AtomRocketFactory;
import gov.ca.cwds.neutron.enums.NeutronSchedulerConstants;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.launch.AbortFlightTimerTask;
import gov.ca.cwds.neutron.launch.FlightPlanRegistry;
import gov.ca.cwds.neutron.launch.FlightRecorder;
import gov.ca.cwds.neutron.launch.LaunchCommandSettings;
import gov.ca.cwds.neutron.launch.LaunchDirector;
import gov.ca.cwds.neutron.launch.RocketFactory;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;
import gov.ca.cwds.neutron.launch.listener.NeutronJobListener;
import gov.ca.cwds.neutron.launch.listener.NeutronSchedulerListener;
import gov.ca.cwds.neutron.launch.listener.NeutronTriggerListener;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.neutron.vox.XRaySpex;
import gov.ca.cwds.rest.ElasticUtils;
import gov.ca.cwds.rest.ElasticsearchConfiguration;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;
import gov.ca.cwds.rest.services.cms.CachingSystemCodeService;

/**
 * Guice dependency injection (DI), module which constructs and manages common class instances for
 * batch jobs.
 * 
 * <p>
 * Also known as, <a href="http://jimmyneutron.wikia.com/wiki/Hyper_Corn">Hyper Cube</a>, Jimmy's
 * invention to store an infinite number of items in a small place.
 * </p>
 * 
 * @author CWDS API Team
 */
public class HyperCube extends NeutronGuiceModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(HyperCube.class);

  private static final String HIBERNATE_CONFIG_CMS = "jobs-cms-hibernate.cfg.xml";

  private static final String HIBERNATE_CONFIG_NS = "jobs-ns-hibernate.cfg.xml";

  /**
   * The <strong>singleton</strong> Guice injector used for all rocket instances during the life of
   * this JVM.
   */
  private static Injector injector;

  private static HyperCube instance;

  private static Function<FlightPlan, HyperCube> cubeMaker = HyperCube::buildCube;

  private File esConfigPeople;

  private File esConfigPeopleSummary;

  private String lastJobRunTimeFilename;

  private FlightPlan flightPlan;

  private String hibernateConfigCms = HIBERNATE_CONFIG_CMS;

  private String hibernateConfigNs = HIBERNATE_CONFIG_NS;

  /**
   * Default constructor.
   */
  public HyperCube() {
    this.flightPlan = null;
  }

  /**
   * Preferred constructor. Construct from command line options and required arguments.
   * 
   * @param flightPlan command line options
   * @param esConfigFilePeople location of Elasticsearch configuration file for the people file
   * @param lastJobRunTimeFilename location of last run file
   */
  public HyperCube(final FlightPlan flightPlan, final File esConfigFilePeople,
      String lastJobRunTimeFilename) {
    LOGGER.debug("HyperCube.ctor");
    this.esConfigPeople = esConfigFilePeople;
    this.lastJobRunTimeFilename =
        !StringUtils.isBlank(lastJobRunTimeFilename) ? lastJobRunTimeFilename : "";
    this.flightPlan = flightPlan;

    if (StringUtils.isNotBlank(flightPlan.getEsConfigPeopleSummaryLoc())) {
      this.esConfigPeopleSummary = new File(flightPlan.getEsConfigPeopleSummaryLoc());
    }
  }

  public Configuration makeHibernateConfiguration() {
    return new Configuration();
  }

  protected void init() {
    // Optional initialization, mostly for testing.
  }

  public static synchronized HyperCube buildCube(final FlightPlan opts) {
    HyperCube ret;

    LOGGER.debug("HyperCube.buildCube");
    if (instance != null) {
      ret = instance;
    } else {
      final boolean usePeopleIndex = StringUtils.isNotBlank(opts.getEsConfigLoc());
      if (usePeopleIndex) {
        new ApiFileAssistant().validateFileLocation(opts.getEsConfigLoc());
      }
      ret = new HyperCube(opts, usePeopleIndex ? new File(opts.getEsConfigLoc()) : null,
          opts.getLastRunLoc());
    }

    return ret;
  }

  public static synchronized Injector buildInjectorFunctional(final FlightPlan flightPlan) {
    try {
      return buildInjector(flightPlan);
    } catch (NeutronCheckedException e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED TO BUILD INJECTOR! {}", e.getMessage());
    }
  }

  /**
   * Build the Guice Injector once and use it for all Rocket instances.
   * 
   * @param flightPlan command line options
   * @return Guice Injector
   * @throws NeutronCheckedException if unable to construct dependencies
   */
  public static synchronized Injector buildInjector(final FlightPlan flightPlan)
      throws NeutronCheckedException {
    if (injector == null) {
      try {
        LOGGER.debug("HyperCube.buildInjector");
        injector = Guice.createInjector(cubeMaker.apply(flightPlan));

        // Initialize system code cache.
        injector.getInstance(gov.ca.cwds.rest.api.domain.cms.SystemCodeCache.class);

        // Static injection.
        ElasticTransformer.setMapper(injector.getInstance(ObjectMapper.class));
        ElasticSearchPerson.getSystemCodes();
      } catch (Exception e) {
        throw CheeseRay.checked(LOGGER, e, "FAILED TO BUILD INJECTOR! {}", e.getMessage());
      }
    }

    return injector;
  }

  /**
   * Prepare a rocket with all required dependencies.
   * 
   * @param klass batch rocket class
   * @param flightPlan command line options
   * @return batch rocket, ready to run
   * @param <T> Person persistence type
   * @throws NeutronCheckedException checked exception
   */
  public static <T extends BasePersonRocket<?, ?>> T newRocket(final Class<T> klass,
      final FlightPlan flightPlan) throws NeutronCheckedException {
    try {
      LOGGER.debug("HyperCube.newRocket");
      final T ret = buildInjector(flightPlan).getInstance(klass);
      ret.setFlightPlan(flightPlan);
      return ret;
    } catch (CreationException e) {
      throw CheeseRay.checked(LOGGER, e, "FAILED TO BUILD ROCKET!: {}", e.getMessage());
    }
  }

  /**
   * Prepare a batch rocket with all required dependencies.
   * 
   * @param klass batch rocket class
   * @param args command line arguments
   * @return batch rocket, ready to run
   * @param <T> Person persistence type
   * @throws NeutronCheckedException checked exception
   */
  public static <T extends BasePersonRocket<?, ?>> T newRocket(final Class<T> klass, String... args)
      throws NeutronCheckedException {
    return newRocket(klass, FlightPlan.parseCommandLine(args));
  }

  /**
   * Register DB2 and PostgreSQL replication entity classes with Hibernate.
   * 
   * <p>
   * Parent class:
   * </p>
   * {@inheritDoc}
   * 
   * @see com.google.inject.AbstractModule#configure()
   */
  @Override
  protected void configure() {
    LOGGER.debug("HyperCube.configure");
    bind(FlightPlan.class).toInstance(this.flightPlan);

    // DB2 session factory:
    bind(SessionFactory.class).annotatedWith(CmsSessionFactory.class)
        .toInstance(makeCmsSessionFactory());

    // PostgreSQL session factory:
    bind(SessionFactory.class).annotatedWith(NsSessionFactory.class)
        .toInstance(makeNsSessionFactory());

    bindDaos();

    // Inject annotations.
    bindConstant().annotatedWith(LastRunFile.class).to(this.lastJobRunTimeFilename);

    // Singleton:
    final ObjectMapper om = ObjectMapperUtils.createObjectMapper();
    om.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    om.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    om.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    bind(ObjectMapper.class).toInstance(om);

    // Command Center:
    bind(AtomFlightRecorder.class).to(FlightRecorder.class).asEagerSingleton();
    bind(AtomFlightPlanManager.class).to(FlightPlanRegistry.class).asEagerSingleton();
    bind(AtomRocketFactory.class).to(RocketFactory.class).asEagerSingleton();
    bind(AtomCommandCenterConsole.class).to(XRaySpex.class);
  }

  /**
   * Initialize all Data Access Objects (DAO).
   */
  protected void bindDaos() {
    LOGGER.debug("make DAOs");

    // DB2 replicated tables:
    bind(ReplicatedRelationshipsDao.class);
    bind(ReplicatedClientDao.class);
    bind(ReplicatedReporterDao.class);
    bind(ReplicatedAttorneyDao.class);
    bind(ReplicatedCollateralIndividualDao.class);
    bind(ReplicatedOtherAdultInPlacemtHomeDao.class);
    bind(ReplicatedOtherChildInPlacemtHomeDao.class);
    bind(ReplicatedOtherClientNameDao.class);
    bind(ReplicatedServiceProviderDao.class);
    bind(ReplicatedSubstituteCareProviderDao.class);
    bind(ReplicatedEducationProviderContactDao.class);
    bind(ReplicatedPersonReferralsDao.class);
    bind(ReplicatedPersonCasesDao.class);
    bind(ReplicatedSafetyAlertsDao.class);
    bind(StaffPersonDao.class);
    bind(DbResetStatusDao.class);

    // PostgreSQL:
    // OPTION: only connect to Postgres if needed.
    bind(EsIntakeScreeningDao.class);

    // CMS system codes.
    bind(SystemCodeDao.class);
    bind(SystemMetaDao.class);
  }

  protected Configuration additionalDaos(Configuration config) {
    return config;
  }

  @Provides
  @Singleton
  public LaunchCommandSettings commandCenterSettings() {
    LOGGER.debug("HyperCube.commandCenterSettings");
    return LaunchCommand.getSettings();
  }

  // =========================
  // DB2:
  // =========================

  protected SessionFactory makeCmsSessionFactory() {
    LOGGER.debug("HyperCube.makeCmsSessionFactory");
    final Configuration config = makeHibernateConfiguration().configure(getHibernateConfigCms())
        .addAnnotatedClass(BatchBucket.class).addAnnotatedClass(EsClientAddress.class)
        .addAnnotatedClass(EsClientPerson.class).addAnnotatedClass(EsRelationship.class)
        .addAnnotatedClass(EsPersonReferral.class).addAnnotatedClass(EsChildPersonCase.class)
        .addAnnotatedClass(EsParentPersonCase.class).addAnnotatedClass(ReplicatedAttorney.class)
        .addAnnotatedClass(ReplicatedCollateralIndividual.class)
        .addAnnotatedClass(ReplicatedEducationProviderContact.class)
        .addAnnotatedClass(ReplicatedOtherAdultInPlacemtHome.class)
        .addAnnotatedClass(ReplicatedOtherChildInPlacemtHome.class)
        .addAnnotatedClass(ReplicatedOtherClientName.class)
        .addAnnotatedClass(ReplicatedReporter.class)
        .addAnnotatedClass(ReplicatedServiceProvider.class)
        .addAnnotatedClass(ReplicatedSubstituteCareProvider.class)
        .addAnnotatedClass(SimpleReplicatedClient.class).addAnnotatedClass(ReplicatedClient.class)
        .addAnnotatedClass(ReplicatedClientAddress.class).addAnnotatedClass(ReplicatedAddress.class)
        .addAnnotatedClass(SystemCode.class).addAnnotatedClass(EsSafetyAlert.class)
        .addAnnotatedClass(SystemMeta.class).addAnnotatedClass(StaffPerson.class)
        .addAnnotatedClass(DatabaseResetEntry.class);

    LOGGER.debug("HyperCube.makeCmsSessionFactory: connect");
    return additionalDaos(config).buildSessionFactory();
  }

  // =========================
  // POSTGRESQL:
  // =========================

  protected SessionFactory makeNsSessionFactory() {
    LOGGER.debug("HyperCube.makeNsSessionFactory");
    return makeHibernateConfiguration().configure(getHibernateConfigNs())
        .addAnnotatedClass(EsIntakeScreening.class).addAnnotatedClass(IntakeScreening.class)
        .buildSessionFactory();
  }

  // =========================
  // CMS SYSTEM CODE CACHE:
  // =========================

  @Provides
  @Singleton
  public SystemCodeCache provideSystemCodeCache(SystemCodeDao systemCodeDao,
      SystemMetaDao systemMetaDao) {
    LOGGER.debug("HyperCube.provideSystemCodeCache");
    if (isScaffoldSystemCodeCache()) {
      return scaffoldSystemCodeCache();
    } else {
      final long secondsToRefreshCache = 15 * 24 * 60 * (long) 60; // 15 days
      final SystemCodeCache ret =
          new CachingSystemCodeService(systemCodeDao, systemMetaDao, secondsToRefreshCache, false);
      ret.register();
      return ret;
    }
  }

  protected boolean isScaffoldSystemCodeCache() {
    return false;
  }

  protected SystemCodeCache scaffoldSystemCodeCache() {
    return null;
  }

  @Provides
  public AbortFlightTimerTask provideAbortFlightTimerTask(Scheduler scheduler) {
    return new AbortFlightTimerTask(scheduler, 90000); // NEXT: soft-code
  }

  @Provides
  @Singleton
  public CmsSystemCodeSerializer provideCmsSystemCodeSerializer(SystemCodeCache systemCodeCache) {
    LOGGER.debug("HyperCube.provideCmsSystemCodeSerializer");
    return new CmsSystemCodeSerializer(systemCodeCache);
  }

  // =========================
  // ELASTICSEARCH:
  // =========================

  protected TransportClient buildElasticsearchClient(final ElasticsearchConfiguration config)
      throws NeutronCheckedException {
    TransportClient client = null;
    LOGGER.debug("HyperCube.buildElasticsearchClient");
    try {
      client = ElasticUtils.buildElasticsearchClient(config);
      return client;
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e,
          "ERROR initializing Elasticsearch client for people index: {}", e.getMessage(), e);
    }
  }

  /**
   * Elasticsearch 5.x. Instantiate the singleton ElasticSearch client on demand. Initializes X-Pack
   * security.
   * 
   * @return initialized singleton ElasticSearch client, people index
   * @throws NeutronCheckedException on ES connection error
   */
  @Provides
  @Singleton
  @Named("elasticsearch.client.people")
  public Client elasticsearchClientPeople() throws NeutronCheckedException {
    LOGGER.debug("HyperCube.elasticsearchClientPeople");
    TransportClient client = null;
    if (esConfigPeople != null) {
      client = buildElasticsearchClient(elasticSearchConfigPeople());
    }
    return client;
  }

  /**
   * Instantiate the singleton Elasticsearch 5.x client on demand and initialize X-Pack security.
   * 
   * @return initialized singleton ElasticSearch client, people summary index
   * @throws NeutronCheckedException on ES connection error
   */
  @Provides
  @Singleton
  @Named("elasticsearch.client.people-summary")
  public Client elasticsearchClientPeopleSummary() throws NeutronCheckedException {
    LOGGER.debug("HyperCube.elasticsearchClientPeopleSummary");
    return buildElasticsearchClient(elasticSearchConfigPeopleSummary());
  }

  @Provides
  @Singleton
  @Named("elasticsearch.dao.people")
  public ElasticsearchDao makeElasticsearchDaoPeople() throws NeutronCheckedException {
    LOGGER.debug("HyperCube.makeElasticsearchDaoPeople");
    return new ElasticsearchDao(elasticsearchClientPeople(), elasticSearchConfigPeople());
  }

  @Provides
  @Singleton
  @Named("elasticsearch.dao.people-summary")
  public ElasticsearchDao makeElasticsearchDaoPeopleSummary(
      @Named("elasticsearch.client.people-summary") Client client,
      @Named("elasticsearch.config.people-summary") ElasticsearchConfiguration config) {
    LOGGER.debug("HyperCube.makeElasticsearchDaoPeopleSummary");
    return new ElasticsearchDao(client, config);
  }

  protected ElasticsearchConfiguration loadElasticSearchConfig(File esConfig)
      throws NeutronCheckedException {
    try {
      LOGGER.debug("HyperCube.loadElasticSearchConfig");
      return new ObjectMapper(new YAMLFactory()).readValue(esConfig,
          ElasticsearchConfiguration.class);
    } catch (Exception e) {
      throw CheeseRay.checked(LOGGER, e, "ERROR READING ES CONFIG! {}", e.getMessage(), e);
    }
  }

  /**
   * Read Elasticsearch configuration for the People index.
   * 
   * @return ES configuration for the People index
   * @throws NeutronCheckedException on error
   */
  @Provides
  @Named("elasticsearch.config.people")
  public ElasticsearchConfiguration elasticSearchConfigPeople() throws NeutronCheckedException {
    LOGGER.debug("HyperCube.elasticSearchConfigPeople");
    ElasticsearchConfiguration ret = null;
    if (esConfigPeople != null) {
      LOGGER.debug("Create NEW ES configuration: people");
      ret = loadElasticSearchConfig(this.esConfigPeople);
    }
    return ret;
  }

  /**
   * Read Elasticsearch configuration for the People Summary index.
   * 
   * @return ES configuration for the People Summary index
   * @throws NeutronCheckedException on error
   */
  @Provides
  @Named("elasticsearch.config.people-summary")
  public ElasticsearchConfiguration elasticSearchConfigPeopleSummary()
      throws NeutronCheckedException {
    LOGGER.debug("HyperCube.elasticSearchConfigPeopleSummary");
    ElasticsearchConfiguration ret = null;
    if (esConfigPeopleSummary != null) {
      LOGGER.debug("Create NEW ES configuration: people summary");
      ret = loadElasticSearchConfig(this.esConfigPeopleSummary);
    }
    return ret;
  }

  // =========================
  // QUARTZ SCHEDULER:
  // =========================

  @Provides
  @Singleton
  protected Scheduler makeScheduler(final Injector injector, AtomRocketFactory rocketFactory)
      throws SchedulerException {
    final boolean initialMode = LaunchCommand.isInitialMode();
    final Properties p = new Properties();
    p.put("org.quartz.scheduler.instanceName", NeutronSchedulerConstants.SCHEDULER_INSTANCE_NAME);

    // NEXT: make configurable.
    p.put("org.quartz.threadPool.threadCount",
        initialMode ? "1" : NeutronSchedulerConstants.SCHEDULER_THREAD_COUNT);
    final StdSchedulerFactory factory = new StdSchedulerFactory(p);
    final Scheduler scheduler = factory.getScheduler();

    // NEXT: inject scheduler and rocket factory.
    scheduler.setJobFactory(rocketFactory);

    // Quartz scheduler listeners.
    return scheduler;
  }

  /**
   * Configure Quartz scheduling. Quartz operates as a singleton.
   * 
   * @param injector Guice dependency injection
   * @param flightRecorder flight recorder
   * @param rocketFactory rocket factory
   * @param flightPlanMgr flight plan manager
   * @return configured launch scheduler
   * @throws SchedulerException if unable to configure Quartz
   */
  @Provides
  @Singleton
  protected AtomLaunchDirector configureQuartz(final Injector injector,
      final AtomFlightRecorder flightRecorder, final AtomRocketFactory rocketFactory,
      final AtomFlightPlanManager flightPlanMgr, Scheduler scheduler,
      AbortFlightTimerTask abortFlightTimerTask) throws SchedulerException {
    LOGGER.debug("HyperCube.configureQuartz");
    final boolean initialMode = LaunchCommand.isInitialMode();
    final LaunchDirector ret =
        new LaunchDirector(flightRecorder, rocketFactory, flightPlanMgr, abortFlightTimerTask);

    ret.setScheduler(scheduler);
    final FlightPlan commonFlightPlan = LaunchCommand.getStandardFlightPlan();
    final ListenerManager mgr = ret.getScheduler().getListenerManager();
    mgr.addSchedulerListener(new NeutronSchedulerListener());
    mgr.addTriggerListener(new NeutronTriggerListener(ret));
    mgr.addJobListener(initialMode
        ? StandardFlightSchedule.buildInitialLoadJobChainListener(
            commonFlightPlan.isLoadPeopleIndex(), commonFlightPlan.getExcludedRockets())
        : new NeutronJobListener());
    return ret;
  }

  // =========================
  // ACCESSORS:
  // =========================

  public FlightPlan getFlightPlan() {
    return flightPlan;
  }

  public void setFlightPlan(FlightPlan opts) {
    this.flightPlan = opts;
  }

  public static Injector getInjector() {
    return injector;
  }

  public String getHibernateConfigCms() {
    return hibernateConfigCms;
  }

  public void setHibernateConfigCms(String hibernateConfigCms) {
    this.hibernateConfigCms = hibernateConfigCms;
  }

  public String getHibernateConfigNs() {
    return hibernateConfigNs;
  }

  public void setHibernateConfigNs(String hibernateConfigNs) {
    this.hibernateConfigNs = hibernateConfigNs;
  }

  public static HyperCube getInstance() {
    return instance;
  }

  public static void setInstance(HyperCube instance) {
    HyperCube.instance = instance;
  }

  public static Function<FlightPlan, HyperCube> getCubeMaker() {
    return cubeMaker;
  }

  public static void setCubeMaker(Function<FlightPlan, HyperCube> cubeMaker) {
    HyperCube.cubeMaker = cubeMaker;
  }

  public static void setInjector(Injector injector) {
    HyperCube.injector = injector;
  }

  public File getEsConfigPeopleSummary() {
    return esConfigPeopleSummary;
  }

  public File getEsConfigPeople() {
    return esConfigPeople;
  }

}
