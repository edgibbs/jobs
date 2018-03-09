package gov.ca.cwds.neutron.inject;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.function.Function;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.binder.AnnotatedBindingBuilder;

import gov.ca.cwds.data.CmsSystemCodeSerializer;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.test.Mach1TestRocket;
import gov.ca.cwds.jobs.test.TestDenormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntityDao;
import gov.ca.cwds.neutron.atom.AtomFlightPlanManager;
import gov.ca.cwds.neutron.atom.AtomFlightRecorder;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.atom.AtomRocketFactory;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.launch.LaunchCommandSettings;
import gov.ca.cwds.neutron.launch.RocketFactory;
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
  HyperCube target;

  public HyperCube makeOurOwnCube(FlightPlan plan) {
    return target;
  }

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    flightPlan = new FlightPlan();
    flightPlan.setEsConfigLoc("config"+File.separator+"local.yaml");
    target = new TestHyperCube(flightPlan, new File(flightPlan.getEsConfigLoc()), lastRunFile);
    target.setHibernateConfigCms("test-h2-cms.xml");
    target.setHibernateConfigNs("test-h2-ns.xml");
    HyperCube.setCubeMaker(opts -> this.makeOurOwnCube(opts));
    lastTester = this;

    final Injector injector = mock(Injector.class);
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
    Client actual = target.elasticsearchClientPeople();
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

  // @Test
  // public void buildInjectorFunctional_Args__FlightPlan() throws Exception {
  // Injector actual = HyperCube.buildInjectorFunctional(flightPlan);
  // assertThat(actual, is(notNullValue()));
  // }

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

  // @Test
  // public void configure_Args__() throws Exception {
  // final Binder binder = mock(Binder.class);
  // target.setTestBinder(binder);
  // target.configure();
  // }

  @Test
  public void additionalDaos_Args__Configuration() throws Exception {
    Configuration config = mock(Configuration.class);
    Configuration actual = target.additionalDaos(config);
    Configuration expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void makeCmsSessionFactory_Args__() throws Exception {
  // SessionFactory actual = target.makeCmsSessionFactory();
  // assertThat(actual, is(notNullValue()));
  // }

  // @Test
  // public void makeNsSessionFactory_Args__() throws Exception {
  // SessionFactory actual = target.makeNsSessionFactory();
  // assertThat(actual, is(notNullValue()));
  // }

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
  public void configureQuartz_Args__Injector__AtomFlightRecorder__AtomRocketFactory__AtomFlightPlanManager()
      throws Exception {
    Injector injector = mock(Injector.class);
    AtomFlightRecorder flightRecorder = mock(AtomFlightRecorder.class);
    AtomRocketFactory rocketFactory = mock(AtomRocketFactory.class);
    AtomFlightPlanManager flightPlanMgr = mock(AtomFlightPlanManager.class);
    AtomLaunchDirector actual =
        target.configureQuartz(injector, flightRecorder, rocketFactory, flightPlanMgr);
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
    HyperCube actual = HyperCube.getInstance();
    HyperCube expected = null;
    assertThat(actual, is(equalTo(expected)));
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
    Injector injector = mock(Injector.class);
    HyperCube.setInjector(injector);
  }

}
