package gov.ca.cwds.jobs;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.jdbc.Work;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.query.NativeQuery;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.type.StringType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.db2.jcc.am.DatabaseMetaData;

import gov.ca.cwds.ObjectMapperUtils;
import gov.ca.cwds.data.cms.SystemCodeDao;
import gov.ca.cwds.data.cms.SystemMetaDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.jobs.test.Mach1TestRocket;
import gov.ca.cwds.jobs.test.SimpleTestSystemCodeCache;
import gov.ca.cwds.jobs.test.TestNormalizedEntityDao;
import gov.ca.cwds.neutron.atom.AtomFlightPlanManager;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.launch.FlightPlanRegistry;
import gov.ca.cwds.neutron.launch.FlightRecorder;
import gov.ca.cwds.neutron.launch.LaunchDirector;
import gov.ca.cwds.neutron.launch.RocketFactory;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.neutron.vox.jmx.VoxLaunchPadMBean;
import gov.ca.cwds.rest.ElasticsearchConfiguration;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;

/**
 * <a href="http://jimmyneutron.wikia.com/wiki/Goddard">Goddard</a> is Jimmy's mechanical dog. He is
 * loyal, intelligent, resourceful, playful, friendly, and has a seemingly unlimited supply of
 * gadgets, whatever you need, whenever you need.
 * 
 * @param <T> normalized type
 * @param <M> de-normalized type
 * 
 * @author CWDS API Team
 */
public abstract class Goddard<T extends PersistentObject, M extends ApiGroupNormalizer<?>> {

  protected static final ObjectMapper MAPPER = ObjectMapperUtils.createObjectMapper();

  public static final String DEFAULT_CLIENT_ID = "abc1234567";

  public static final AtomicBoolean isRunwayClear = new AtomicBoolean(false);

  public static final Lock lock = new ReentrantLock();

  @BeforeClass
  public static void setupClass() {
    LaunchCommand.setTestMode(true);
    SimpleTestSystemCodeCache.init();
    SystemCodeCache.global().getAllSystemCodes();
    ElasticTransformer.setMapper(MAPPER);
  }

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  public ElasticsearchConfiguration esConfig;
  public ElasticsearchDao esDao;
  public Client client;
  public ElasticSearchPerson esp;

  public File tempFile;
  public File jobConfigFile;
  public File esConfileFile;
  public String lastRunFile;
  public java.util.Date lastRunTime = new java.util.Date();

  public SessionFactory sessionFactory;
  public Session session;
  public EntityManager em;
  public SessionFactoryOptions sfo;
  public Transaction transaction;
  public StandardServiceRegistry reg;
  public ConnectionProvider cp;
  public Connection con;
  public Statement stmt;
  public PreparedStatement preparedStatement;
  public ResultSet rs;
  public DatabaseMetaData meta;
  public NativeQuery<M> nq;
  public ProcedureCall proc;

  public SystemCodeDao systemCodeDao;
  public SystemMetaDao systemMetaDao;

  public Configuration hibernationConfiguration;
  public TestNormalizedEntityDao testNormalizedEntityDao;

  public FlightPlan flightPlan;
  public FlightLog flightRecord;
  public FlightRecorder flightRecorder;
  public FlightPlanRegistry flightPlanRegistry;
  public StandardFlightSchedule flightSchedule;

  public Scheduler scheduler;
  public LaunchDirector launchDirector;
  public ListenerManager listenerManager;

  public RocketFactory rocketFactory;
  public Mach1TestRocket mach1Rocket;
  public AtomFlightPlanManager flightPlanManager;

  public ObjectMapper mapper;
  public Pair<String, String> pair;

  public SearchHits hits;
  public SearchHit hit;
  public SearchHit[] hitArray;
  public SearchResponse sr;

  public VoxLaunchPadMBean mbean;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    System.setProperty("DB_CMS_SCHEMA", "CWSRS1");

    LaunchCommand.getSettings().setInitialMode(true);
    LaunchCommand.getSettings().setTestMode(true);

    // Last run time:
    tempFile = tempFolder.newFile("tempFile.txt");
    jobConfigFile = tempFolder.newFile("jobConfigFile.yml");
    lastRunFile = tempFile.getAbsolutePath();

    FileUtils.writeStringToFile(tempFile, "2018-12-31 03:34:12");

    mapper = MAPPER;
    pair = Pair.of("aaaaaaaaaa", "9999999999");

    // JDBC:
    sessionFactory = mock(SessionFactory.class);
    session = mock(Session.class);
    transaction = mock(Transaction.class);
    sfo = mock(SessionFactoryOptions.class);
    reg = mock(StandardServiceRegistry.class);
    cp = mock(ConnectionProvider.class);
    con = mock(Connection.class);
    rs = mock(ResultSet.class);
    meta = mock(DatabaseMetaData.class);
    stmt = mock(Statement.class);
    em = mock(EntityManager.class);
    hibernationConfiguration = mock(Configuration.class);
    rocketFactory = mock(RocketFactory.class);
    scheduler = mock(Scheduler.class);
    listenerManager = mock(ListenerManager.class);
    flightPlanManager = mock(AtomFlightPlanManager.class);
    proc = mock(ProcedureCall.class);
    client = mock(Client.class);

    final TestNormalizedEntityDao testNormalizedEntityDao =
        new TestNormalizedEntityDao(sessionFactory);
    mach1Rocket = new Mach1TestRocket(testNormalizedEntityDao, esDao, lastRunFile, MAPPER);
    flightPlanRegistry = new FlightPlanRegistry(flightPlan);
    flightSchedule = StandardFlightSchedule.CLIENT;

    final Map<String, Object> sessionProperties = new HashMap<>();
    sessionProperties.put("hibernate.default_schema", "CWSRS1");

    when(sessionFactory.getCurrentSession()).thenReturn(session);
    when(sessionFactory.openSession()).thenReturn(session);
    when(sessionFactory.createEntityManager()).thenReturn(em);
    when(sessionFactory.getSessionFactoryOptions()).thenReturn(sfo);
    when(sessionFactory.isOpen()).thenReturn(true);
    when(sessionFactory.getProperties()).thenReturn(sessionProperties);

    when(session.getSessionFactory()).thenReturn(sessionFactory);
    when(session.getProperties()).thenReturn(sessionProperties);
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.getTransaction()).thenReturn(transaction);
    when(session.createStoredProcedureCall(any(String.class))).thenReturn(proc);

    when(transaction.getStatus()).thenReturn(TransactionStatus.MARKED_ROLLBACK);

    when(sfo.getServiceRegistry()).thenReturn(reg);
    when(reg.getService(ConnectionProvider.class)).thenReturn(cp);
    when(cp.getConnection()).thenReturn(con);

    when(con.getMetaData()).thenReturn(meta);
    when(con.createStatement()).thenReturn(stmt);

    when(meta.getDatabaseProductName()).thenReturn("DB2");
    when(stmt.executeQuery(any())).thenReturn(rs);
    when(stmt.executeUpdate(any(String.class))).thenReturn(1);

    preparedStatement = mock(PreparedStatement.class);
    when(con.prepareStatement(any(String.class))).thenReturn(preparedStatement);
    when(con.prepareStatement(any())).thenReturn(preparedStatement);
    when(con.prepareStatement(any(String.class), any(int[].class))).thenReturn(preparedStatement);

    when(preparedStatement.executeQuery()).thenReturn(rs);
    when(preparedStatement.executeQuery(any())).thenReturn(rs);
    when(preparedStatement.executeUpdate()).thenReturn(1);

    // Result set:
    when(rs.next()).thenReturn(true).thenReturn(false);
    when(rs.getString(any())).thenReturn(DEFAULT_CLIENT_ID);
    when(rs.getString(contains("IBMSNAP_OPERATION"))).thenReturn("I");
    when(rs.getString("LIMITED_ACCESS_CODE")).thenReturn("N");
    when(rs.getInt(any())).thenReturn(0);

    final java.util.Date date = new java.util.Date();
    final Timestamp ts = new Timestamp(date.getTime());
    when(rs.getDate(any())).thenReturn(new Date(date.getTime()));
    when(rs.getTimestamp("LIMITED_ACCESS_CODE")).thenReturn(ts);
    when(rs.getTimestamp(any())).thenReturn(ts);

    // DB2 platform and version:
    when(meta.getDatabaseMajorVersion()).thenReturn(11);
    when(meta.getDatabaseMinorVersion()).thenReturn(2);
    when(meta.getDatabaseProductName()).thenReturn("DB2");
    when(meta.getDatabaseProductVersion()).thenReturn("DSN11010");

    // Elasticsearch basics:
    esp = new ElasticSearchPerson();
    esDao = mock(ElasticsearchDao.class);
    esConfig = mock(ElasticsearchConfiguration.class);

    when(esDao.getConfig()).thenReturn(esConfig);
    when(esDao.getClient()).thenReturn(client);

    final Settings settings = Settings.builder().put(new HashMap<String, String>()).build();
    when(client.settings()).thenReturn(settings);

    when(esConfig.getElasticsearchAlias()).thenReturn("people");
    when(esConfig.getElasticsearchDocType()).thenReturn("person");

    // Flight options:
    esConfileFile = tempFolder.newFile("es.yml");
    flightPlan = mock(FlightPlan.class);

    when(flightPlan.isLoadSealedAndSensitive()).thenReturn(false);
    when(flightPlan.getEsConfigLoc()).thenReturn(esConfileFile.getAbsolutePath());
    when(flightPlan.getThreadCount()).thenReturn(1L);
    when(flightPlan.getLastRunLoc()).thenReturn(this.lastRunFile);

    // Queries.
    nq = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any(String.class))).thenReturn(nq);
    when(nq.setString(any(String.class), any(String.class))).thenReturn(nq);
    when(nq.setParameter(any(String.class), any(String.class), any(StringType.class)))
        .thenReturn(nq);
    when(nq.setFlushMode(any(FlushMode.class))).thenReturn(nq);
    when(nq.setHibernateFlushMode(any(FlushMode.class))).thenReturn(nq);
    when(nq.setReadOnly(any(Boolean.class))).thenReturn(nq);
    when(nq.setCacheMode(any(CacheMode.class))).thenReturn(nq);
    when(nq.setFetchSize(any(Integer.class))).thenReturn(nq);
    when(nq.setCacheable(any(Boolean.class))).thenReturn(nq);

    final ScrollableResults scrollableResults = mock(ScrollableResults.class);
    when(nq.scroll(any(ScrollMode.class))).thenReturn(scrollableResults);

    // Flight track:
    flightRecord = new FlightLog();
    flightRecorder = new FlightRecorder();
    launchDirector = mock(LaunchDirector.class);

    // Elasticsearch _msearch.
    final MultiSearchRequestBuilder mBuilder = mock(MultiSearchRequestBuilder.class);
    final MultiSearchResponse multiResponse = mock(MultiSearchResponse.class);
    final SearchRequestBuilder sBuilder = mock(SearchRequestBuilder.class);
    final MultiSearchResponse.Item item = mock(MultiSearchResponse.Item.class);
    final MultiSearchResponse.Item[] items = new MultiSearchResponse.Item[1];
    items[0] = item;

    hits = mock(SearchHits.class);
    hit = mock(SearchHit.class);
    hitArray = new SearchHit[1];
    hitArray[0] = hit;
    sr = mock(SearchResponse.class);

    when(client.prepareMultiSearch()).thenReturn(mBuilder);
    when(client.prepareSearch()).thenReturn(sBuilder);

    when(mBuilder.add(any(SearchRequestBuilder.class))).thenReturn(mBuilder);
    when(mBuilder.get()).thenReturn(multiResponse);
    when(sBuilder.setQuery(any())).thenReturn(sBuilder);
    when(multiResponse.getResponses()).thenReturn(items);
    when(item.getResponse()).thenReturn(sr);
    when(sr.getHits()).thenReturn(hits);
    when(hits.getHits()).thenReturn(hitArray);

    when(hit.docId()).thenReturn(12345);
    when(hit.getSourceAsString()).thenReturn(IOUtils.toString(
        getClass().getResourceAsStream("/fixtures/es_person.json"), Charset.forName("UTF-8")));

    systemCodeDao = mock(SystemCodeDao.class);
    systemMetaDao = mock(SystemMetaDao.class);

    // Command and control.
    when(launchDirector.fuelRocket(any(String.class), any(FlightPlan.class)))
        .thenReturn(mach1Rocket);
    when(launchDirector.fuelRocket(any(Class.class), any(FlightPlan.class)))
        .thenReturn(mach1Rocket);

    when(launchDirector.getScheduler()).thenReturn(scheduler);
    when(launchDirector.getFlightPlanManger()).thenReturn(flightPlanManager);
    when(launchDirector.getFlightRecorder()).thenReturn(flightRecorder);

    when(launchDirector.launch(any(Class.class), any(FlightPlan.class))).thenReturn(flightRecord);
    when(launchDirector.launch(any(String.class), any(FlightPlan.class))).thenReturn(flightRecord);

    when(rocketFactory.fuelRocket(any(Class.class), any(FlightPlan.class))).thenReturn(mach1Rocket);
    when(rocketFactory.fuelRocket(any(String.class), any(FlightPlan.class)))
        .thenReturn(mach1Rocket);

    when(scheduler.getListenerManager()).thenReturn(listenerManager);
    mbean = mock(VoxLaunchPadMBean.class);

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) {
        final Work w = (Work) invocation.getArguments()[0];
        try {
          w.execute(con);
        } catch (SQLException e) {
          // eat it
        }
        return null;
      }
    }).when(session).doWork(any(Work.class));


    markTestDone(); // reset
  }

  public Thread runKillThread(final BasePersonRocket<T, M> target, long sleepMillis) {
    final Thread t = new Thread(() -> {
      try {
        lock.lockInterruptibly();
        await("kill thread").atMost(sleepMillis, TimeUnit.MILLISECONDS).untilTrue(isRunwayClear);
        target.doneRetrieve();
        target.doneTransform();
        target.doneIndex();
        target.done();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        lock.unlock();
      }
    });

    t.start();
    return t;
  }

  public Thread runKillThread(final BasePersonRocket<T, M> target) {
    return runKillThread(target, 1400L);
  }

  public void markTestDone() {
    isRunwayClear.set(true);
  }

}
