package gov.ca.cwds.neutron.inject;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.function.Function;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestHighLevelClient;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.binder.AnnotatedBindingBuilder;

import gov.ca.cwds.data.CmsSystemCodeSerializer;
import gov.ca.cwds.data.cms.SystemCodeDao;
import gov.ca.cwds.data.cms.SystemMetaDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.es.NeutronElasticSearchDao;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.jobs.test.Mach1TestRocket;
import gov.ca.cwds.jobs.test.TestDenormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntityDao;
import gov.ca.cwds.neutron.atom.AtomFlightPlanManager;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.launch.LaunchCommandSettings;
import gov.ca.cwds.neutron.launch.RocketFactory;
import gov.ca.cwds.neutron.launch.ZombieKillerTimerTask;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.rest.ElasticsearchConfiguration;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;

public class HyperCubeTest extends Goddard<TestNormalizedEntity, TestDenormalizedEntity> {

  public static class TestHyperCube extends HyperCube {

    Goddard lastTest;
    Configuration configuration;

    public TestHyperCube(final FlightPlan opts, final File esConfigFile,
        String lastJobRunTimeFilename) {
      super(opts, esConfigFile, lastJobRunTimeFilename);
      configuration = mock(Configuration.class);
    }

    @Override
    public void init() {
      this.lastTest = HyperCubeTest.lastTester;
    }

    @Override
    protected SessionFactory makeCmsSessionFactory() {
      return new Configuration().configure("test-h2-cms.xml").buildSessionFactory();
      // return lastTest.sessionFactory;
    }

    @Override
    protected SessionFactory makeNsSessionFactory() {
      return new Configuration().configure("test-h2-ns.xml").buildSessionFactory();
      // return lastTest.sessionFactory;
    }

    @Override
    protected boolean isScaffoldSystemCodeCache() {
      return true;
    }

    @Override
    protected SystemCodeCache scaffoldSystemCodeCache() {
      return mock(SystemCodeCache.class);
    }

    @Override
    public Configuration makeHibernateConfiguration() {
      return configuration;
    }

    @Override
    protected Configuration additionalDaos(Configuration config) {
      return config.addAnnotatedClass(TestNormalizedEntityDao.class);
    }

    @Override
    protected <T> AnnotatedBindingBuilder<T> bind(Class<T> clazz) {
      final AnnotatedBindingBuilder<T> builder = mock(AnnotatedBindingBuilder.class);
      when(builder.annotatedWith(any(Annotation.class))).thenReturn(builder);
      return builder;
    }
  }

  public static Goddard<TestNormalizedEntity, TestDenormalizedEntity> lastTester;

  AtomFlightPlanManager flightPlanMgr;
  Injector injector;
  Binder testBinder;
  HyperCube target;

  public HyperCube makeOurOwnCube(FlightPlan plan) {
    return target;
  }

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    LaunchCommand.setStandardFlightPlan(null);
    flightPlan = new FlightPlan();
    flightPlan.setEsConfigLoc("config" + File.separator + "local.yaml");
    flightPlanMgr = mock(AtomFlightPlanManager.class);

    target = new TestHyperCube(flightPlan, new File(flightPlan.getEsConfigLoc()), lastRunFile);
    target.setHibernateConfigCms("test-h2-cms.xml");
    target.setHibernateConfigNs("test-h2-ns.xml");
    // target.setEsConfigPeople(esConfig); // takes a file, not a config

    testBinder = mock(Binder.class);
    target.setTestBinder(testBinder);

    HyperCube.setCubeMaker(opts -> this.makeOurOwnCube(opts));
    lastTester = this;

    injector = mock(Injector.class);
    when(injector.getInstance(RocketFactory.class)).thenReturn(rocketFactory);
    when(injector.getInstance(Mach1TestRocket.class)).thenReturn(mach1Rocket);
    HyperCube.setInjector(injector);
  }

  @Test
  public void type() throws Exception {
    assertNotNull(HyperCube.class);
  }

  @Test
  public void instantiation() throws Exception {
    assertNotNull(target);
  }

  @Test
  public void elasticSearchConfig_Args__() throws Exception {
    ElasticsearchConfiguration actual = target.elasticSearchConfigPeople();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void provideSystemCodeCache_Args__SystemCodeDao__SystemMetaDao() throws Exception {
    SystemCodeCache actual = target.provideSystemCodeCache(systemCodeDao, systemMetaDao);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void provideCmsSystemCodeSerializer_Args__SystemCodeCache() throws Exception {
    final SystemCodeCache systemCodeCache = mock(SystemCodeCache.class);
    CmsSystemCodeSerializer actual = target.provideCmsSystemCodeSerializer(systemCodeCache);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getOpts_Args__() throws Exception {
    final FlightPlan actual = target.getFlightPlan();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setOpts_Args__JobOptions() throws Exception {
    target.setFlightPlan(flightPlan);
  }

  @Test
  public void elasticsearchClient_Args__() throws Exception {
    RestHighLevelClient actual = target.elasticsearchClientPeople();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setOpts_Args__FlightPlan() throws Exception {
    target.setFlightPlan(flightPlan);
  }

  @Test
  public void getInjector_Args__() throws Exception {
    Injector actual = HyperCube.getInjector();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getHibernateConfigCms_Args__() throws Exception {
    String actual = target.getHibernateConfigCms();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setHibernateConfigCms_Args__String() throws Exception {
    String hibernateConfigCms = null;
    target.setHibernateConfigCms(hibernateConfigCms);
  }

  @Test
  public void getHibernateConfigNs_Args__() throws Exception {
    String actual = target.getHibernateConfigNs();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setHibernateConfigNs_Args__String() throws Exception {
    String hibernateConfigNs = null;
    target.setHibernateConfigNs(hibernateConfigNs);
  }

  // Actually a live test, though it could connect to H2 and a mock Elasticsearch.
  @Test
  public void buildInjector_Args__FlightPlan() throws Exception {
    final Injector actual = HyperCube.buildInjector(flightPlan);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void newJob_Args__Class__FlightPlan() throws Exception {
    final Class klass = Mach1TestRocket.class;
    Object actual = HyperCube.newRocket(klass, flightPlan);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void newJob_Args__Class__StringArray() throws Exception {
    final Class klass = Mach1TestRocket.class;
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/CWS-NS3/client_indexer_time.txt", "-t", "4", "-S"};
    final Object actual = HyperCube.newRocket(klass, args);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void bindDaos_Args__() throws Exception {
    final Binder binder = mock(Binder.class);
    target.setTestBinder(binder);
    target.bindDaos(); // can only call from module
  }

  @Test
  public void makeHibernateConfiguration_Args__() throws Exception {
    Configuration actual = target.makeHibernateConfiguration();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void init_Args__() throws Exception {
    target.init();
  }

  @Test
  public void buildCube_Args__FlightPlan() throws Exception {
    HyperCube actual = HyperCube.buildCube(flightPlan);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void newRocket_Args__Class__FlightPlan() throws Exception {
    Object actual = HyperCube.newRocket(Mach1TestRocket.class, flightPlan);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void newRocket_Args__Class__FlightPlan_T__NeutronException() throws Exception {
    HyperCube.newRocket(Mach1TestRocket.class, flightPlan);
  }

  @Test
  public void newRocket_Args__Class__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/CWS-NS3/client_indexer_time.txt", "-t", "4", "-S"};
    Object actual = HyperCube.newRocket(Mach1TestRocket.class, args);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void additionalDaos_Args__Configuration() throws Exception {
    Configuration config = mock(Configuration.class);
    Configuration actual = target.additionalDaos(config);
    Configuration expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isScaffoldSystemCodeCache_Args__() throws Exception {
    boolean actual = target.isScaffoldSystemCodeCache();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void scaffoldSystemCodeCache_Args__() throws Exception {
    SystemCodeCache actual = target.scaffoldSystemCodeCache();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void commandCenterSettings_Args__() throws Exception {
    LaunchCommandSettings actual = target.commandCenterSettings();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getFlightPlan_Args__() throws Exception {
    FlightPlan actual = target.getFlightPlan();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setFlightPlan_Args__FlightPlan() throws Exception {
    FlightPlan opts = mock(FlightPlan.class);
    target.setFlightPlan(opts);
  }

  @Test
  public void getInstance_Args__() throws Exception {
    final HyperCube actual = HyperCube.getInstance();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setInstance_Args__HyperCube() throws Exception {
    HyperCube instance = mock(HyperCube.class);
    HyperCube.setInstance(instance);
  }

  @Test
  public void getCubeMaker_Args__() throws Exception {
    Function<FlightPlan, HyperCube> actual = HyperCube.getCubeMaker();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setCubeMaker_Args__Function() throws Exception {
    Function<FlightPlan, HyperCube> cubeMaker = mock(Function.class);
    HyperCube.setCubeMaker(cubeMaker);
  }

  @Test
  public void setInjector_Args__Injector() throws Exception {
    HyperCube.setInjector(injector);
  }

  @Test
  public void makeHibernateConfiguration_A$() throws Exception {
    final Configuration actual = target.makeHibernateConfiguration();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void init_A$() throws Exception {
    target.init();
  }

  @Test(expected = NullPointerException.class)
  public void bindSystemProperties_A$() throws Exception {
    target.bindSystemProperties();
  }

  @Test
  public void buildCube_A$FlightPlan() throws Exception {
    final FlightPlan opts = mock(FlightPlan.class);
    final HyperCube actual = HyperCube.buildCube(opts);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void buildInjectorFunctional_A$FlightPlan() throws Exception {
    final Injector actual = HyperCube.buildInjectorFunctional(flightPlan);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void buildInjector_A$FlightPlan() throws Exception {
    final Injector actual = HyperCube.buildInjector(flightPlan);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void buildInjector_A$FlightPlan_T$NeutronCheckedException() throws Exception {
    try {
      HyperCube.setInjector(null);
      HyperCube.buildInjector(flightPlan);
      fail("Expected exception was not thrown!");
    } catch (NeutronCheckedException e) {
    }
  }

  @Test
  public void newRocket_A$Class$FlightPlan() throws Exception {
    final Class<Mach1TestRocket> klass = Mach1TestRocket.class;
    final Mach1TestRocket actual = HyperCube.newRocket(klass, flightPlan);
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = NeutronCheckedException.class)
  public void newRocket_A$Class$FlightPlan_T$NeutronCheckedException() throws Exception {
    final Class<BasePersonRocket> klass = BasePersonRocket.class;
    HyperCube.newRocket(klass, flightPlan);
  }

  @Test(expected = NeutronCheckedException.class)
  public void newRocket_A$Class$StringArray() throws Exception {
    final Class<Mach1TestRocket> klass = Mach1TestRocket.class;
    final String[] args = new String[] {};
    final Object actual = HyperCube.newRocket(klass, args);
    final Object expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NullPointerException.class)
  public void configure_A$() throws Exception {
    final Binder binder = mock(Binder.class);
    target.setTestBinder(binder);
    target.configure();
  }

  @Test
  public void bindDaos_A$() throws Exception {
    target.bindDaos();
  }

  @Test
  public void additionalDaos_A$Configuration() throws Exception {
    final Configuration config = mock(Configuration.class);
    final Configuration actual = target.additionalDaos(config);
    final Configuration expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void commandCenterSettings_A$() throws Exception {
    final LaunchCommandSettings actual = target.commandCenterSettings();
    assertThat(actual, is(notNullValue()));
  }

  /**
   * Works great in Eclipse but fails in Gradle command line. Go figure.
   * 
   * @throws Exception test
   */
  @Test
  @Ignore
  public void makeCmsSessionFactory_A$() throws Exception {
    final SessionFactory actual = target.makeCmsSessionFactory();
    assertThat(actual, is(notNullValue()));
  }

  /**
   * Works great in Eclipse but fails in Gradle command line. Go figure.
   * 
   * @throws Exception test
   */
  @Test
  @Ignore
  public void makeNsSessionFactory_A$() throws Exception {
    final SessionFactory actual = target.makeNsSessionFactory();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void provideSystemCodeCache_A$SystemCodeDao$SystemMetaDao() throws Exception {
    final SystemCodeDao systemCodeDao = mock(SystemCodeDao.class);
    final SystemMetaDao systemMetaDao = mock(SystemMetaDao.class);
    final SystemCodeCache actual = target.provideSystemCodeCache(systemCodeDao, systemMetaDao);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void provideCmsSystemCodeSerializer_A$SystemCodeCache() throws Exception {
    final SystemCodeCache systemCodeCache = mock(SystemCodeCache.class);
    final CmsSystemCodeSerializer actual = target.provideCmsSystemCodeSerializer(systemCodeCache);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void elasticsearchClientPeople_A$() throws Exception {
    final RestHighLevelClient actual = target.elasticsearchClientPeople();
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = NeutronCheckedException.class)
  public void elasticsearchClientPeople_A$_T$NeutronCheckedException() throws Exception {
    target.setEsConfigPeople(esConfileFile);
    target.elasticsearchClientPeople();
  }

  @Test(expected = NeutronCheckedException.class)
  public void elasticsearchClientPeopleSummary_A$() throws Exception {
    final RestHighLevelClient actual = target.elasticsearchClientPeopleSummary();
    final RestHighLevelClient expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void elasticsearchClientPeopleSummary_A$_T$NeutronCheckedException() throws Exception {
    target.elasticsearchClientPeopleSummary();
  }

  @Test
  public void makeElasticsearchDaoPeople_A$() throws Exception {
    final NeutronElasticSearchDao actual = target.makeElasticsearchDaoPeople();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void makeElasticsearchDaoPeopleSummary_A$Client$ElasticsearchConfiguration()
      throws Exception {
    final Client client = mock(Client.class);
    final ElasticsearchConfiguration config = mock(ElasticsearchConfiguration.class);
    final ElasticsearchDao actual = target.makeElasticsearchDaoPeopleSummary(client, config);
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = NeutronCheckedException.class)
  public void loadElasticSearchConfig_A$File() throws Exception {
    final ElasticsearchConfiguration actual = target.loadElasticSearchConfig(esConfileFile);
    final ElasticsearchConfiguration expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void elasticSearchConfigPeople_A$() throws Exception {
    final ElasticsearchConfiguration actual = target.elasticSearchConfigPeople();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void elasticSearchConfigPeopleSummary_A$() throws Exception {
    final ElasticsearchConfiguration actual = target.elasticSearchConfigPeopleSummary();
    final ElasticsearchConfiguration expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void makeScheduler_A$Injector$AtomRocketFactory() throws Exception {
    final Scheduler actual = target.makeScheduler(injector, rocketFactory);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void configureQuartz_A$Injector$AtomFlightRecorder$AtomRocketFactory$AtomFlightPlanManager$Scheduler$ZombieKillerTimerTask$String()
      throws Exception {
    LaunchCommand.setStandardFlightPlan(flightPlan);
    final ZombieKillerTimerTask zombieKillerTimerTask = mock(ZombieKillerTimerTask.class);
    final String strTimeToAbort = "120000";
    final AtomLaunchDirector actual = target.configureQuartz(injector, flightRecorder,
        rocketFactory, flightPlanMgr, scheduler, zombieKillerTimerTask, strTimeToAbort);
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = SchedulerException.class)
  public void configureQuartz_A$Injector$AtomFlightRecorder$AtomRocketFactory$AtomFlightPlanManager$Scheduler$ZombieKillerTimerTask$String_T$SchedulerException()
      throws Exception {
    final ZombieKillerTimerTask zombieKillerTimerTask = mock(ZombieKillerTimerTask.class);
    final String strTimeToAbort = "120000";
    when(scheduler.getListenerManager()).thenThrow(SchedulerException.class);
    target.configureQuartz(injector, flightRecorder, rocketFactory, flightPlanMgr, scheduler,
        zombieKillerTimerTask, strTimeToAbort);
  }

  @Test
  public void getFlightPlan_A$() throws Exception {
    final FlightPlan actual = target.getFlightPlan();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setFlightPlan_A$FlightPlan() throws Exception {
    final FlightPlan opts = mock(FlightPlan.class);
    target.setFlightPlan(opts);
  }

  @Test
  public void getInjector_A$() throws Exception {
    final Injector actual = HyperCube.getInjector();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getHibernateConfigCms_A$() throws Exception {
    final String actual = target.getHibernateConfigCms();
    final String expected = "test-h2-cms.xml";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setHibernateConfigCms_A$String() throws Exception {
    final String hibernateConfigCms = null;
    target.setHibernateConfigCms(hibernateConfigCms);
  }

  @Test
  public void getHibernateConfigNs_A$() throws Exception {
    final String actual = target.getHibernateConfigNs();
    final String expected = "test-h2-ns.xml";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setHibernateConfigNs_A$String() throws Exception {
    final String hibernateConfigNs = null;
    target.setHibernateConfigNs(hibernateConfigNs);
  }

  @Test
  public void getInstance_A$() throws Exception {
    final HyperCube actual = HyperCube.getInstance();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setInstance_A$HyperCube() throws Exception {
    final HyperCube instance = mock(HyperCube.class);
    HyperCube.setInstance(instance);
  }

  @Test
  public void getCubeMaker_A$() throws Exception {
    final Function<FlightPlan, HyperCube> actual = HyperCube.getCubeMaker();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setCubeMaker_A$Function() throws Exception {
    final Function<FlightPlan, HyperCube> cubeMaker = mock(Function.class);
    HyperCube.setCubeMaker(cubeMaker);
  }

  @Test
  public void setInjector_A$Injector() throws Exception {
    HyperCube.setInjector(injector);
  }

  @Test
  public void getEsConfigPeopleSummary_A$() throws Exception {
    final File actual = target.getEsConfigPeopleSummary();
    final File expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getEsConfigPeople_A$() throws Exception {
    final File actual = target.getEsConfigPeople();
    final File expected = new File("config/local.yaml");
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isScaffoldSystemCodeCache_A$() throws Exception {
    final boolean actual = target.isScaffoldSystemCodeCache();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void scaffoldSystemCodeCache_A$() throws Exception {
    final SystemCodeCache actual = target.scaffoldSystemCodeCache();
    assertThat(actual, is(notNullValue()));
  }

  // @Test
  // public void configure_Args__() throws Exception {
  // final Binder binder = mock(Binder.class);
  // target.setTestBinder(binder);
  // target.configure();
  // }

  // @Test
  // @Ignore
  // public void configure_Args__() throws Exception {
  // target.configure(); // can only call from module
  // }

  // @Test(expected = NeutronException.class)
  // @Ignore
  // public void newJob_Args__Class__FlightPlan_T__NeutronException() throws Exception {
  // final Class klass = TestIndexerJob.class;
  //
  // flightPlan = new FlightPlan();
  // flightPlan.setEsConfigLoc("config/local.yaml");
  // flightPlan.setSimulateLaunch(true);
  // target = new TestHyperCube(flightPlan, new File(flightPlan.getEsConfigLoc()),
  // lastJobRunTimeFilename);
  //
  // target.setHibernateConfigCms("/test-h2-cms.xml");
  // target.setHibernateConfigNs("/test-h2-ns.xml");
  // target.setTestBinder(mock(Binder.class));
  // HyperCube.setInstance(target);
  //
  // HyperCube.newRocket(klass, flightPlan);
  // }

  // =================================
  // ERROR:
  // H2 credentials on Jenkins.
  // =================================
  // @Test
  // @Ignore
  // public void makeCmsSessionFactory_Args__() throws Exception {
  // final SessionFactory actual = target.makeCmsSessionFactory();
  // assertThat(actual, is(notNullValue()));
  // }

  // @Test
  // @Ignore
  // public void makeNsSessionFactory_Args__() throws Exception {
  // final SessionFactory actual = target.makeNsSessionFactory();
  // assertThat(actual, is(notNullValue()));
  // }

  // @Test
  // public void
  // configureQuartz_Args__Injector__AtomFlightRecorder__AtomRocketFactory__AtomFlightPlanManager()
  // throws Exception {
  // final AtomFlightRecorder flightRecorder = mock(AtomFlightRecorder.class);
  // final AtomRocketFactory rocketFactory = mock(AtomRocketFactory.class);
  // final AtomFlightPlanManager flightPlanMgr = mock(AtomFlightPlanManager.class);
  // final AbortFlightTimerTask timerTask = new AbortFlightTimerTask(scheduler, 90000);
  //
  // final HyperCube cube = HyperCube.buildCube(flightPlan);
  // final Injector injector = HyperCube.getInjector();
  //
  // (injector.getInstance(AbortFlightTimerTask.class)).thenReturn(timerTask);
  // // HyperCube.setInjector(injector);
  //
  // final AtomLaunchDirector actual =
  // target.configureQuartz(injector, flightRecorder, rocketFactory, flightPlanMgr);
  // assertThat(actual, is(notNullValue()));
  // }

}
