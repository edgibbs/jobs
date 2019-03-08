package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import javax.persistence.Query;

import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StringType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.dao.cms.BatchBucket;
import gov.ca.cwds.data.ApiTypedIdentifier;
import gov.ca.cwds.data.BaseDaoImpl;
import gov.ca.cwds.data.DaoException;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPerson.ESOptionalCollection;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.jobs.test.TestDenormalizedEntity;
import gov.ca.cwds.jobs.test.TestIndexerJob;
import gov.ca.cwds.jobs.test.TestNormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntityDao;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.launch.LaunchCommandSettings;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

public class BasePersonRocketTest extends Goddard<TestNormalizedEntity, TestDenormalizedEntity> {

  TestNormalizedEntityDao dao;
  TestIndexerJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    dao = new TestNormalizedEntityDao(sessionFactory);
    target = new TestIndexerJob(dao, esDao, lastRunFile, MAPPER, sessionFactory, flightRecorder);
    target.setFlightPlan(flightPlan);
    target.setFlightLog(flightRecord);
  }

  @Test
  public void type() throws Exception {
    assertThat(BasePersonRocket.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getViewName_Args__() throws Exception {
    final String actual = target.getInitialLoadViewName();
    final String expected = "VW_NUTTIN";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void extract_Args__ResultSet() throws Exception {
    final Object actual = target.extract(rs);
  }

  @Test
  public void buildBulkProcessor_Args__() throws Exception {
    final BulkProcessor actual = target.buildBulkProcessor();
    assertThat(actual, notNullValue());
  }

  @Test
  public void getIdColumn_Args__() throws Exception {
    final String actual = target.getIdColumn();
    final String expected = "IDENTIFIER";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getDenormalizedClass_Args__() throws Exception {
    final Object actual = target.getDenormalizedClass();
    assertThat(actual, notNullValue());
  }

  @Test
  public void normalize_Args__List() throws Exception {
    final List<TestDenormalizedEntity> recs = new ArrayList<>();
    recs.add(new TestDenormalizedEntity(DEFAULT_CLIENT_ID, "one", "two", "three", "four"));

    final List<TestNormalizedEntity> actual = target.normalize(recs);
    final List<TestNormalizedEntity> expected = new ArrayList<>();
    final TestNormalizedEntity expect = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    expected.add(expect);

    assertThat(actual, notNullValue());
  }

  @Test
  public void normalizeSingle_Args__List() throws Exception {
    final List<TestDenormalizedEntity> recs = new ArrayList<>();
    recs.add(new TestDenormalizedEntity(DEFAULT_CLIENT_ID));

    final TestNormalizedEntity actual = target.normalizeSingle(recs);
    final TestNormalizedEntity expected = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isViewNormalizer_Args__() throws Exception {
    final boolean actual = target.isViewNormalizer();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void prepareDocument_Args__BulkProcessor__Object() throws Exception {
    final BulkProcessor bp = mock(BulkProcessor.class);
    final TestNormalizedEntity t = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    target.prepareDocument(bp, t);
  }

  @Test
  public void setInsertCollections_Args__ElasticSearchPerson__Object__List() throws Exception {
    final TestNormalizedEntity t = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    final List list = new ArrayList();
    target.setInsertCollections(esp, t, list);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void prepareUpsertRequestNoChecked_Args__ElasticSearchPerson__Object__bomb()
      throws Exception {
    when(target.getFlightPlan().getIndexName()).thenThrow(IllegalStateException.class);
    final TestNormalizedEntity t = new TestNormalizedEntity(null);
    final DocWriteRequest actual = target.prepareUpsertRequestNoChecked(esp, t);
    assertThat(actual, notNullValue());
  }

  @Test
  public void prepareUpsertRequestNoChecked_Args__ElasticSearchPerson__Object() throws Exception {
    final TestNormalizedEntity t = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    final DocWriteRequest actual = target.prepareUpsertRequestNoChecked(esp, t);
    assertThat(actual, notNullValue());
  }

  @Test
  public void prepareUpsertRequestNoChecked_Args__ElasticSearchPerson__2() throws Exception {
    final TestNormalizedEntity t = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    target.setShouldDelete(true);

    final DocWriteRequest actual = target.prepareUpsertRequestNoChecked(esp, t);
    assertThat(actual, notNullValue());
  }

  @Test
  public void prepareUpsertRequest_Args__ElasticSearchPerson__Object() throws Exception {
    final TestNormalizedEntity t = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    final UpdateRequest actual = target.prepareUpsertRequest(esp, t);
    assertThat(actual, notNullValue());
  }

  @Test
  public void keepCollections_Args__() throws Exception {
    final ESOptionalCollection[] actual = target.keepCollections();
    assertThat(actual, notNullValue());
  }

  @Test
  public void getOptionalElementName_Args__() throws Exception {
    final String actual = target.getOptionalElementName();
  }

  @Test
  public void getOptionalCollection_Args__ElasticSearchPerson__Object() throws Exception {
    final TestNormalizedEntity t = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    final List<? extends ApiTypedIdentifier<String>> actual = target.getOptionalCollection(esp, t);
    final List<? extends ApiTypedIdentifier<String>> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void waitOnQueue() throws Exception {
    try {
      target.waitOnQueue();
    } catch (Exception e) {
      if (e instanceof InterruptedException) {
        // ignore
      } else {
        throw e;
      }
    }
  }

  @Test
  public void threadNormalize_Args__() throws Exception {
    try {
      for (int i = 0; i < 100; i++) {
        target.queueNormalize.putLast(new TestDenormalizedEntity(DEFAULT_CLIENT_ID,
            String.valueOf(i), String.valueOf(i + 3), String.valueOf(i + 7)));
        target.queueNormalize.putLast(new TestDenormalizedEntity(DEFAULT_CLIENT_ID, "4", "5", "6"));
        target.queueNormalize.putLast(new TestDenormalizedEntity(DEFAULT_CLIENT_ID, "7", "8", "9"));
        target.queueNormalize.putLast(new TestDenormalizedEntity("xyz1234567", "1", "2", "3"));
        target.queueNormalize.putLast(new TestDenormalizedEntity("xyz1234567", "4", "5", "6"));
      }

      runKillThread(target, NeutronIntegerDefaults.POLL_MILLIS.getValue() + 3500L);
      target.getFlightLog().start();
      target.getFlightLog().doneRetrieve();
      target.threadNormalize(); // method to test
      target.catchYourBreath();
    } finally {
      markTestDone();
    }
  }

  @Test(expected = NeutronRuntimeException.class)
  public void threadNormalize__bomb() throws Exception {
    try {
      final FlightLog fl = mock(FlightLog.class);
      target.setFlightLog(fl);
      when(fl.isRunning()).thenThrow(IllegalStateException.class);
      threadNormalize_Args__();
    } finally {
      markTestDone();
    }
  }

  @Test
  public void extractLastRunRecsFromTable_Args__Date() throws Exception {
    final NativeQuery<TestDenormalizedEntity> q = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any())).thenReturn(q);
    final List<TestDenormalizedEntity> list = new ArrayList<>();
    final TestDenormalizedEntity t = new TestDenormalizedEntity(DEFAULT_CLIENT_ID, "1", "2", "3");
    list.add(t);
    when(q.list()).thenReturn(list);
    final List<TestNormalizedEntity> actual = target.extractLastRunRecsFromTable(lastRunTime);
    assertThat(actual, notNullValue());
  }

  @Test(expected = DaoException.class)
  public void extractLastRunRecsFromTable_Args__Date__error() throws Exception {
    final NativeQuery<TestDenormalizedEntity> q = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any())).thenThrow(HibernateException.class);
    final List<TestNormalizedEntity> actual = target.extractLastRunRecsFromTable(lastRunTime);
    assertThat(actual, notNullValue());
  }

  @Test
  public void extractLastRunRecsFromView_Args__Date() throws Exception {
    final NativeQuery<TestDenormalizedEntity> qn = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any())).thenReturn(qn);
    final List<TestDenormalizedEntity> recs = new ArrayList<>();
    recs.add(new TestDenormalizedEntity(DEFAULT_CLIENT_ID, "one", "two"));
    recs.add(new TestDenormalizedEntity(DEFAULT_CLIENT_ID, "2", "3"));
    recs.add(new TestDenormalizedEntity("xyz1234567", "1", "2"));
    recs.add(new TestDenormalizedEntity("xyz1234567", "3", "4"));
    recs.add(new TestDenormalizedEntity("abc1234567", "1", "2"));
    recs.add(new TestDenormalizedEntity(null, "1", "2"));
    when(qn.list()).thenReturn(recs);
    final List<TestNormalizedEntity> actual =
        target.extractLastRunRecsFromView(lastRunTime, new HashSet<String>());
    assertThat(actual, notNullValue());
  }

  @Test(expected = NeutronRuntimeException.class)
  public void extractLastRunRecsFromView_Args__Date__SQLException() throws Exception {
    final NativeQuery<TestDenormalizedEntity> qn = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any())).thenReturn(qn);
    when(session.beginTransaction()).thenThrow(SQLException.class);
    when(session.getTransaction()).thenThrow(SQLException.class);
    final List<TestNormalizedEntity> actual =
        target.extractLastRunRecsFromView(lastRunTime, new HashSet<String>());
  }

  @Test(expected = NeutronRuntimeException.class)
  public void extractLastRunRecsFromView_Args__Date__HibernateException() throws Exception {
    final NativeQuery<TestDenormalizedEntity> qn = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any())).thenReturn(qn);
    when(session.beginTransaction()).thenThrow(HibernateException.class);
    when(session.getTransaction()).thenThrow(HibernateException.class);
    final List<TestNormalizedEntity> actual =
        target.extractLastRunRecsFromView(lastRunTime, new HashSet<String>());
  }

  @Test
  public void getPartitionRanges_Args__() throws Exception {
    final javax.persistence.Query q = mock(javax.persistence.Query.class);
    when(em.createNativeQuery(any(String.class), any(Class.class))).thenReturn(q);
    when(q.setParameter(any(String.class), any(String.class))).thenReturn(q);
    final List<?> actual = target.getPartitionRanges();
    assertThat(actual, notNullValue());
  }

  @Test
  public void getPartitionRanges_Args__base() throws Exception {
    target.setBaseRanges(true);
    final List<?> actual = target.getPartitionRanges();
    assertThat(actual, notNullValue());
  }

  @Test
  public void close_Args__() throws Exception {
    target.close();
  }

  @Test
  public void close__interrupted() throws Exception {
    target.fail();
    target.close();
  }

  @Test(expected = IOException.class)
  public void close_Args___T__IOException() throws Exception {
    doThrow(new IOException()).when(esDao).close();
    target.close();
  }

  @Test
  public void finish_Args__() throws Exception {
    target.setFakeFinish(false);
    target.finish();
  }

  @Test(expected = NeutronCheckedException.class)
  public void finish_Args__error() throws Exception {
    target.setFakeMarkDone(true);
    target.setFakeFinish(false);
    doThrow(new NeutronRuntimeException("whatever")).when(esDao).close();
    target.finish();
  }

  @Test
  public void extractHibernate_Args__() throws Exception {
    final Query q = mock(Query.class);
    when(em.createNativeQuery(any(String.class), any(Class.class))).thenReturn(q);
    when(q.getResultList()).thenReturn(new ArrayList<TestDenormalizedEntity>());
    when(q.setParameter(any(String.class), any(String.class))).thenReturn(q);

    final List<BatchBucket> buckets = new ArrayList<>();
    final BatchBucket b = new BatchBucket();
    b.setBucket(1);
    b.setBucketCount(1);
    b.setMinId("1");
    b.setMaxId("2");
    buckets.add(b);
    when(q.getResultList()).thenReturn(buckets);

    final NativeQuery<TestDenormalizedEntity> nq = mock(NativeQuery.class);
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

    final ScrollableResults results = mock(ScrollableResults.class);
    when(nq.scroll(any(ScrollMode.class))).thenReturn(results);
    when(results.next()).thenReturn(true).thenReturn(false);

    final TestNormalizedEntity[] entities = new TestNormalizedEntity[1];
    final TestNormalizedEntity entity = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    entity.setFirstName("Fred");
    entity.setLastName("Meyer");
    entities[0] = entity;
    when(results.get()).thenReturn(entities);

    target.setFakeRanges(true);
    final int actual = target.extractHibernate();
    final int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getOpts_Args__() throws Exception {
    final FlightPlan actual = target.getFlightPlan();
    assertThat(actual, notNullValue());
  }

  @Test
  public void setOpts_Args__JobOptions() throws Exception {
    target.setFlightPlan(flightPlan);
  }

  @Test
  public void isTestMode_Args__() throws Exception {
    final boolean actual = LaunchCommand.isTestMode();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setTestMode_Args__boolean() throws Exception {
    final boolean testMode = false;
    LaunchCommand.setTestMode(testMode);
  }

  @Test
  public void doLastRun_Args__Date() throws Exception {
    final NativeQuery<TestDenormalizedEntity> qn = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any())).thenReturn(qn);

    final NativeQuery<TestDenormalizedEntity> q = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any())).thenReturn(q);

    final List<TestDenormalizedEntity> recs = new ArrayList<>();
    final TestDenormalizedEntity rec = new TestDenormalizedEntity(DEFAULT_CLIENT_ID, "one", "two");
    recs.add(rec);

    final List<TestDenormalizedEntity> list = new ArrayList<>();
    final TestDenormalizedEntity t = new TestDenormalizedEntity(DEFAULT_CLIENT_ID, "1", "2", "3");
    list.add(t);

    when(q.list()).thenReturn(list);
    final Set<String> deletionSet = new HashSet<>();
    deletionSet.add("xyz1234567");

    final Date actual = target.doLastRun(lastRunTime);
    assertThat(actual, notNullValue());
    markTestDone();
  }

  @Test(expected = NeutronCheckedException.class)
  public void doLastRun_Args__Date__error() throws Exception {
    final NativeQuery<TestDenormalizedEntity> qn = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any(String.class))).thenThrow(NeutronRuntimeException.class);

    final Date actual = target.doLastRun(lastRunTime);
    markTestDone();
  }

  @Test
  public void _run_Args__Date() throws Exception {
    final NativeQuery<TestDenormalizedEntity> qn = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any(String.class))).thenReturn(qn);

    final javax.persistence.Query q = mock(javax.persistence.Query.class);
    when(em.createNativeQuery(any(String.class), any(Class.class))).thenReturn(q);
    when(flightPlan.isLastRunMode()).thenReturn(true);

    final Date actual = target.launch(lastRunTime);
    assertThat(actual, notNullValue());
    markTestDone();
  }

  @Test
  public void _run_Args__Date__auto() throws Exception {
    final javax.persistence.Query q = mock(javax.persistence.Query.class);
    when(em.createNativeQuery(any(String.class), any(Class.class))).thenReturn(q);
    when(q.getResultList()).thenReturn(new ArrayList<TestDenormalizedEntity>());
    when(q.setParameter(any(String.class), any(String.class))).thenReturn(q);
    when(flightPlan.isLastRunMode()).thenReturn(true);

    final Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -50);
    lastRunTime = cal.getTime();

    final Date actual = target.launch(lastRunTime);
    assertThat(actual, notNullValue());
    markTestDone();
  }

  @Test(expected = NeutronCheckedException.class)
  public void _run_Args__Date__error() throws Exception {
    final javax.persistence.Query q = mock(javax.persistence.Query.class);
    when(em.createNativeQuery(any(String.class), any(Class.class))).thenReturn(q);
    when(esDao.getConfig()).thenThrow(NeutronRuntimeException.class);

    final Date actual = target.launch(lastRunTime);
    markTestDone();
    assertThat(actual, notNullValue());
  }

  @Test
  @Ignore
  public void threadRetrieveByJdbc_Args() throws Exception {
    when(rs.next()).thenReturn(true, true, false);
    target.getFlightLog().start();
    runKillThread(target);
    target.threadRetrieveByJdbc();
    markTestDone();
  }

  @Test(expected = NeutronRuntimeException.class)
  public void threadRetrieveByJdbc_Args__bomb() throws Exception {
    when(rs.next()).thenReturn(true, false);
    when(con.createStatement()).thenThrow(SQLException.class);
    runKillThread(target);
    target.threadRetrieveByJdbc();
    markTestDone();
  }

  @Test
  public void getInitialLoadViewName_Args__() throws Exception {
    final String actual = target.getInitialLoadViewName();
    assertThat(actual, notNullValue());
  }

  @Test
  public void getInitialLoadQuery_Args__String() throws Exception {
    final String dbSchemaName = "CWSRS1";
    final String actual = target.getInitialLoadQuery(dbSchemaName);
    // assertThat(actual, notNullValue());
  }

  @Test
  public void getJdbcOrderBy_Args__() throws Exception {
    final String actual = target.getJdbcOrderBy();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void mustDeleteLimitedAccessRecords_Args__() throws Exception {
    final boolean actual = target.mustDeleteLimitedAccessRecords();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isDelete_Args__Object() throws Exception {
    final TestNormalizedEntity t = null;
    final boolean actual = target.isDelete(t);
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void bulkDelete_Args__String() throws Exception {
    final String id = DEFAULT_CLIENT_ID;
    final DeleteRequest actual = target.bulkDelete(id);
    assertThat(actual, notNullValue());
  }

  @Test
  public void addToIndexQueue_Args__Object() throws Exception {
    final TestNormalizedEntity norm = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    target.addToIndexQueue(norm);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void addToIndexQueue_Args__interrupt() throws Exception {
    final TestNormalizedEntity norm = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    final ConcurrentLinkedDeque deque = mock(ConcurrentLinkedDeque.class);
    when(deque.add(any(TestNormalizedEntity.class))).thenThrow(InterruptedException.class);
    doThrow(new IllegalStateException()).when(deque).add(any(TestNormalizedEntity.class));

    target.setQueueIndex(deque);
    target.addToIndexQueue(norm);
  }

  @Test
  public void useTransformThread_Args__() throws Exception {
    final boolean actual = target.useTransformThread();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  @Ignore
  public void doInitialLoadJdbc_Args__() throws Exception {
    runKillThread(target, 5500L);
    target.doInitialLoadJdbc();
    markTestDone();
  }

  @Test(expected = NeutronCheckedException.class)
  public void doInitialLoadJdbc_Args__error() throws Exception {
    when(rs.next()).thenReturn(true, false);
    target.setBlowUpNameThread(true);
    runKillThread(target);
    target.doInitialLoadJdbc();
    markTestDone();
  }

  @Test
  public void bulkPrepare_Args__BulkProcessor__int() throws Exception {
    final LaunchCommandSettings settings = new LaunchCommandSettings();
    settings.setInitialMode(true);
    settings.setTestMode(true);
    LaunchCommand.setSettings(settings);
    final TestNormalizedEntity entity = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    for (int i = 0; i < 1000; i++) {
      target.queueIndex.add(entity);
    }

    target.catchYourBreath();
    final BulkProcessor bp = mock(BulkProcessor.class);
    int cntr = 0;
    try {
      runKillThread(target, 3500L);
      final int actual = target.bulkPrepare(bp, cntr);
      target.catchYourBreath();
      assertThat(actual, is(not(0)));
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      markTestDone();
    }
  }

  @Test
  public void threadIndex_Args() throws Exception {
    runKillThread(target, 30000L); // long running test

    try {
      for (int i = 0; i < 1000; i++) {
        target.addToIndexQueue(new TestNormalizedEntity(DEFAULT_CLIENT_ID));
      }

      target.doneRetrieve();
      target.doneTransform();
      target.threadIndex(); // method to test
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      target.done();
      markTestDone();
    }
  }

  @Test(expected = NeutronRuntimeException.class)
  public void threadIndex_Args__error() throws Exception {
    final FlightLog track = mock(FlightLog.class);
    when(track.isRunning()).thenThrow(IllegalStateException.class);
    target.setFlightLog(track);
    runKillThread(target);
    target.threadIndex();
    markTestDone();
  }

  @Test
  public void prepLastRunDoc_Args__BulkProcessor__Object() throws Exception {
    final BulkProcessor bp = mock(BulkProcessor.class);
    final TestNormalizedEntity p = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    target.prepareDocumentTrapException(bp, p);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void prepareDocumentTrapIO__error() throws Exception {
    final BulkProcessor bp = mock(BulkProcessor.class);
    final TestNormalizedEntity p = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    final FlightLog track = mock(FlightLog.class);
    when(track.incrementBulkPrepared()).thenThrow(IOException.class);
    when(track.markQueuedToIndex()).thenThrow(IOException.class);
    target.setFlightLog(track);
    target.prepareDocumentTrapException(bp, p);
  }

  @Test
  public void calcLastRunDate_Args__Date() throws Exception {
    final Date actual = target.calcLastRunDate(lastRunTime);
    assertThat(actual, notNullValue());
  }

  @Test
  public void isRangeSelfManaging_Args__() throws Exception {
    final boolean actual = target.isInitialLoadJdbc();
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void extractLastRunRecsFromView_Args__Date__Set() throws Exception {
    final NativeQuery<TestDenormalizedEntity> qn = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any())).thenReturn(qn);

    final List<TestDenormalizedEntity> denorms = new ArrayList<>();
    final TestDenormalizedEntity m = new TestDenormalizedEntity(DEFAULT_CLIENT_ID);
    denorms.add(m);
    when(qn.list()).thenReturn(denorms);

    final Set<String> deletionResults = mock(Set.class);
    final List<TestNormalizedEntity> actual =
        target.extractLastRunRecsFromView(lastRunTime, deletionResults);
    assertThat(actual, notNullValue());
  }

  @Test(expected = NeutronRuntimeException.class)
  public void extractLastRunRecsFromView_Args__sql_error() throws Exception {
    final NativeQuery<TestDenormalizedEntity> qn = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any())).thenThrow(SQLException.class);

    final List<TestDenormalizedEntity> denorms = new ArrayList<>();
    final TestDenormalizedEntity m = new TestDenormalizedEntity(DEFAULT_CLIENT_ID);
    denorms.add(m);
    when(qn.list()).thenReturn(denorms);

    final Set<String> deletionResults = mock(Set.class);
    final List<TestNormalizedEntity> actual =
        target.extractLastRunRecsFromView(lastRunTime, deletionResults);
    assertThat(actual, notNullValue());
  }

  @Test(expected = NeutronRuntimeException.class)
  public void extractLastRunRecsFromView_Args__sql_error_again() throws Exception {
    final NativeQuery<TestDenormalizedEntity> qn = mock(NativeQuery.class);
    doThrow(SQLException.class).when(session).clear();
    doThrow(SQLException.class).when(session).flush();
    doThrow(SQLException.class).when(transaction).rollback();

    final List<TestDenormalizedEntity> denorms = new ArrayList<>();
    final TestDenormalizedEntity m = new TestDenormalizedEntity(DEFAULT_CLIENT_ID);
    denorms.add(m);
    when(qn.list()).thenReturn(denorms);

    final Set<String> deletionResults = mock(Set.class);
    final List<TestNormalizedEntity> actual =
        target.extractLastRunRecsFromView(lastRunTime, deletionResults);
    assertThat(actual, notNullValue());
  }

  @Test(expected = NeutronRuntimeException.class)
  public void extractLastRunRecsFromView_Args__sql_error_yet_again() throws Exception {
    final NativeQuery<TestDenormalizedEntity> qn = mock(NativeQuery.class);
    doThrow(SQLException.class).when(session).clear();
    doThrow(SQLException.class).when(session).flush();

    final List<TestDenormalizedEntity> denorms = new ArrayList<>();
    final TestDenormalizedEntity m = new TestDenormalizedEntity(DEFAULT_CLIENT_ID);
    denorms.add(m);
    when(qn.list()).thenReturn(denorms);

    final Set<String> deletionResults = mock(Set.class);
    final List<TestNormalizedEntity> actual =
        target.extractLastRunRecsFromView(lastRunTime, deletionResults);
    assertThat(actual, notNullValue());
  }

  @Test
  public void prepHibernatePull_Args__Session__Transaction__Date() throws Exception {
    target.runInsertAllLastChangeKeys(session, lastRunTime, ClientSQLResource.INS_CLI_LST_CHG);
  }

  @Test
  public void getLegacySourceTable_Args__() throws Exception {
    final String actual = target.getLegacySourceTable();
    final String expected = "CRAP_T";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getDBSchemaName_Args__() throws Exception {
    final String actual = target.getDBSchemaName();
    final String expected = "CWSRS1";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void isDB2OnZOS_Args__() throws Exception {
    when(con.getMetaData()).thenThrow(NeutronRuntimeException.class);
    final boolean actual = target.isDB2OnZOS();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isDB2OnZOS_Args__error() throws Exception {
    final boolean actual = target.isDB2OnZOS();
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void enableParallelism_Args__Connection() throws Exception {
    NeutronJdbcUtils.enableBatchSettings(con);
  }

  @Test
  public void testGetEsDao() {
    assertThat(target.getEsDao(), notNullValue());
  }

  @Test
  public void testLoadRecsForDeletion() {
    final List<TestNormalizedEntity> deletionRecs = new ArrayList<>();
    final TestNormalizedEntity entity = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    deletionRecs.add(entity);

    final NativeQuery<TestNormalizedEntity> nq = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any())).thenReturn(nq);
    when(nq.list()).thenReturn(deletionRecs);

    final Set<String> deletionSet = new HashSet<>();
    deletionSet.add(DEFAULT_CLIENT_ID);
    target.loadRecsForDeletion(TestNormalizedEntity.class, session, lastRunTime, deletionSet);
  }

  @Test
  public void pullBucketRange_Args__String__String() throws Exception {
    LaunchCommand.setTestMode(true);

    final NativeQuery<TestDenormalizedEntity> q = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any(String.class))).thenReturn(q);
    when(q.setString(any(String.class), any(String.class))).thenReturn(q);
    when(q.setParameter(any(String.class), any(String.class), any(StringType.class))).thenReturn(q);
    when(q.setFlushMode(any(FlushMode.class))).thenReturn(q);
    when(q.setHibernateFlushMode(any(FlushMode.class))).thenReturn(q);
    when(q.setReadOnly(any(Boolean.class))).thenReturn(q);
    when(q.setCacheMode(any(CacheMode.class))).thenReturn(q);
    when(q.setFetchSize(any(Integer.class))).thenReturn(q);
    when(q.setCacheable(any(Boolean.class))).thenReturn(q);

    final ScrollableResults results = mock(ScrollableResults.class);
    when(q.scroll(any(ScrollMode.class))).thenReturn(results);

    final Boolean[] rsNext = new Boolean[10000];
    Arrays.fill(rsNext, 0, rsNext.length, true);
    when(results.next()).thenReturn(true, rsNext).thenReturn(false);

    final TestNormalizedEntity[] entities = new TestNormalizedEntity[10];
    final TestNormalizedEntity entity = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    entity.setFirstName("Fred");
    entity.setLastName("Meyer");
    Arrays.fill(entities, 0, entities.length, entity);
    when(results.get()).thenReturn(entities);

    final String minId = "1";
    final String maxId = "2";
    final List<TestNormalizedEntity> actual = target.pullBucketRange(minId, maxId);

    assertThat(actual, notNullValue());
  }

  @Test(expected = DaoException.class)
  public void pullBucketRange_Args__error() throws Exception {
    LaunchCommand.setTestMode(true);

    final NativeQuery<TestDenormalizedEntity> q = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any(String.class))).thenThrow(HibernateException.class);
    when(q.setString(any(String.class), any(String.class))).thenReturn(q);
    when(q.setParameter(any(String.class), any(String.class), any(StringType.class))).thenReturn(q);
    when(q.setFlushMode(any(FlushMode.class))).thenReturn(q);
    when(q.setHibernateFlushMode(any(FlushMode.class))).thenReturn(q);
    when(q.setReadOnly(any(Boolean.class))).thenReturn(q);
    when(q.setCacheMode(any(CacheMode.class))).thenReturn(q);
    when(q.setFetchSize(any(Integer.class))).thenReturn(q);
    when(q.setCacheable(any(Boolean.class))).thenReturn(q);

    final ScrollableResults results = mock(ScrollableResults.class);
    when(q.scroll(any(ScrollMode.class))).thenReturn(results);
    when(results.next()).thenReturn(true).thenReturn(false);

    final TestNormalizedEntity[] entities = new TestNormalizedEntity[1];
    final TestNormalizedEntity entity = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    entity.setFirstName("Fred");
    entity.setLastName("Meyer");
    entities[0] = entity;
    when(results.get()).thenReturn(entities);

    final String minId = "1";
    final String maxId = "2";
    final List<TestNormalizedEntity> actual = target.pullBucketRange(minId, maxId);

    assertThat(actual, notNullValue());
  }

  @Ignore
  @Test(expected = InterruptedException.class)
  public void testNormalizeLoop() throws Exception {
    final List<TestDenormalizedEntity> grpRecs = new ArrayList<>();
    final int cntr = 0;
    final Object lastId = new Object();
    final TestDenormalizedEntity x = new TestDenormalizedEntity("xyz9876543");

    grpRecs.add(x);
    final TestDenormalizedEntity entity = new TestDenormalizedEntity(DEFAULT_CLIENT_ID);
    target.queueNormalize.add(entity);
    target.normalizeLoop(grpRecs, lastId, cntr);
  }

  @Test
  public void refreshMQT() throws Exception {
    final NativeQuery<TestDenormalizedEntity> q = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any(String.class))).thenReturn(q);
    when(q.setString(any(String.class), any(String.class))).thenReturn(q);
    when(q.setParameter(any(String.class), any(String.class), any(StringType.class))).thenReturn(q);
    when(nq.setFlushMode(any(FlushMode.class))).thenReturn(nq);
    when(q.setHibernateFlushMode(any(FlushMode.class))).thenReturn(q);
    when(q.setReadOnly(any(Boolean.class))).thenReturn(q);
    when(q.setCacheMode(any(CacheMode.class))).thenReturn(q);
    when(q.setFetchSize(any(Integer.class))).thenReturn(q);
    when(q.setCacheable(any(Boolean.class))).thenReturn(q);

    final FlightPlan opts = new FlightPlan();
    opts.setRefreshMqt(true);
    opts.setEsConfigLoc("config/local.yaml");
    target.setFlightPlan(opts);
    target.refreshMQT();
  }

  @Test
  public void getQueueIndex() throws Exception {
    final Queue<TestNormalizedEntity> actual = target.getQueueIndex();
    assertThat(actual, notNullValue());
  }

  @Test
  public void awaitBulkProcessorClose() throws Exception {
    final BulkProcessor bp = mock(BulkProcessor.class);
    target.setFakeBulkProcessor(false);
    target.awaitBulkProcessorClose(bp);
  }

  @Test(expected = NeutronRuntimeException.class)
  public void awaitBulkProcessorClose_error() throws Exception {
    final BulkProcessor bp = mock(BulkProcessor.class);
    when(bp.awaitClose(any(Integer.class), any(TimeUnit.class)));
    target.setFakeBulkProcessor(false);
    target.awaitBulkProcessorClose(bp);
  }

  @Test
  public void handleDeletes_Args__Set__BulkProcessor() throws Exception {
    final Set<String> deletionResults = new HashSet<>();
    deletionResults.add(DEFAULT_CLIENT_ID);

    final BulkProcessor bp = mock(BulkProcessor.class);
    target.deleteRestricted(deletionResults, bp);
  }

  @Test
  public void sizeQueues_A$Date_initial_load() throws Exception {
    when(flightPlan.determineInitialLoad(any(Date.class))).thenReturn(true);
    final Date lastRun = new Date();
    target.sizeQueues(lastRun);
  }

  @Test
  public void sizeQueues_A$Date_last_run() throws Exception {
    when(flightPlan.determineInitialLoad(any(Date.class))).thenReturn(false);
    final Date lastRun = new Date();
    target.sizeQueues(lastRun);
  }

  @Test
  public void bulkDelete_A$String() throws Exception {
    final String id = DEFAULT_CLIENT_ID;
    final DeleteRequest actual = target.bulkDelete(id);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void addToIndexQueue_A$Object() throws Exception {
    final TestNormalizedEntity norm = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    target.addToIndexQueue(norm);
  }

  @Test
  public void buildBulkProcessor_A$() throws Exception {
    BulkProcessor actual = target.buildBulkProcessor();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void prepareDocument_A$BulkProcessor$Object() throws Exception {
    BulkProcessor bp = mock(BulkProcessor.class);
    final TestNormalizedEntity t = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    target.prepareDocument(bp, t);
  }

  @Test
  public void prepareDocument_A$BulkProcessor$Object_T$IOException() throws Exception {
    final BulkProcessor bp = mock(BulkProcessor.class);
    final TestNormalizedEntity t = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    try {
      target.plantBomb();
      target.prepareDocument(bp, t);
      fail("Expected exception was not thrown!");
    } catch (IOException e) {
    }
  }

  @Test
  public void prepareUpsertRequestNoChecked_A$ElasticSearchPerson$Object() throws Exception {
    final ElasticSearchPerson esp = new ElasticSearchPerson();
    esp.setId(DEFAULT_CLIENT_ID);

    final TestNormalizedEntity t = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    final DocWriteRequest request = target.prepareUpsertRequestNoChecked(esp, t);

    final String actual =
        ((UpdateRequest) request).upsertRequest().toString().replaceAll("\\s+", "");
    final String expected =
        "index{[null][person][abc1234567],source[{\"first_name\":null,\"middle_name\":null,\"last_name\":null,\"name_suffix\":null,\"date_of_birth\":null,\"ssn\":null,\"sensitivity_indicator\":\"N\",\"source\":\"\",\"legacy_descriptor\":{},\"legacy_source_table\":\"CRAP_T\",\"legacy_id\":\"abc1234567\",\"addresses\":[],\"phone_numbers\":[],\"languages\":[],\"csec\":[],\"id\":\"abc1234567\"}]}";

    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void prepareUpsertRequest_A$ElasticSearchPerson$Object() throws Exception {
    final ElasticSearchPerson esp = new ElasticSearchPerson();
    esp.setId(DEFAULT_CLIENT_ID);

    final TestNormalizedEntity t = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    final UpdateRequest request = target.prepareUpsertRequest(esp, t);

    final String actual = request.upsertRequest().toString().replaceAll("\\s+", "");
    final String expected =
        "index{[null][person][abc1234567],source[{\"first_name\":null,\"middle_name\":null,\"last_name\":null,\"name_suffix\":null,\"date_of_birth\":null,\"ssn\":null,\"sensitivity_indicator\":\"N\",\"source\":\"\",\"legacy_descriptor\":{},\"legacy_source_table\":\"CRAP_T\",\"legacy_id\":\"abc1234567\",\"addresses\":[],\"phone_numbers\":[],\"languages\":[],\"csec\":[],\"id\":\"abc1234567\"}]}";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void prepareUpsertRequest_A$ElasticSearchPerson$Object_T$NeutronCheckedException()
      throws Exception {
    final ElasticSearchPerson esp = mock(ElasticSearchPerson.class);
    final TestNormalizedEntity t = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    try {
      target.prepareUpsertRequest(esp, t);
      fail("Expected exception was not thrown!");
    } catch (NeutronCheckedException e) {
    }
  }

  @Test
  public void addThread_A$Runnable$List() throws Exception {
    final Runnable target_ = null;
    final List<Thread> threads = new ArrayList<Thread>();
    target.addThread(target_, threads);
  }

  @Test
  public void addThread_A$boolean$Runnable$List() throws Exception {
    final boolean make = false;
    final Runnable target_ = null;
    final List<Thread> threads = new ArrayList<Thread>();
    target.addThread(make, target_, threads);
  }

  @Test
  public void normalizeLoop_A$List$Object$int() throws Exception {
    runKillThread(target, NeutronIntegerDefaults.POLL_MILLIS.getValue() + 3500L);

    try {
      final List<TestDenormalizedEntity> grpRecs = new ArrayList<>();
      final TestDenormalizedEntity theLastId = new TestDenormalizedEntity(DEFAULT_CLIENT_ID);
      grpRecs.add(theLastId);

      target.queueNormalize.putLast(theLastId);
      final int inCntr = 0;
      final int actual = target.normalizeLoop(grpRecs, theLastId, inCntr);
      final int expected = 2;
      assertThat(actual, is(equalTo(expected)));
    } catch (Exception e) {
    }
  }

  @Ignore
  @Test
  public void bulkPrepare_A$BulkProcessor$int() throws Exception {
    final BulkProcessor bp = mock(BulkProcessor.class);
    final int cntr = 0;
    final int actual = target.bulkPrepare(bp, cntr);
    final int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void prepareDocumentTrapException_A$BulkProcessor$Object() throws Exception {
    final BulkProcessor bp = mock(BulkProcessor.class);
    final TestNormalizedEntity p = new TestNormalizedEntity(DEFAULT_CLIENT_ID);
    target.prepareDocumentTrapException(bp, p);
  }

  @Test
  public void deleteRestricted_A$Set$BulkProcessor() throws Exception {
    final Set<String> deletionResults = new HashSet<>();
    deletionResults.add(DEFAULT_CLIENT_ID);

    final BulkProcessor bp = mock(BulkProcessor.class);
    target.deleteRestricted(deletionResults, bp);
  }

  @Test
  public void fetchLastRunResults_A$Date$Set() throws Exception {
    final Date lastRunDate = new Date();
    final Set<String> deletionResults = mock(Set.class);
    final List<TestNormalizedEntity> actual =
        target.fetchLastRunResults(lastRunDate, deletionResults);
    final List<TestNormalizedEntity> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void doLastRun_A$Date() throws Exception {
    final Date lastRunDt = new Date();
    final Date actual = target.doLastRun(lastRunDt);
    final Date expected = new Date(target.getFlightLog().getStartTime());
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void determineInitialLoad_A$Date() throws Exception {
    final Date lastRun = new Date();
    final boolean actual = target.determineInitialLoad(lastRun);
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void launch_A$Date() throws Exception {
    final Date lastSuccessfulRunTime = new Date();
    final Date actual = target.launch(lastSuccessfulRunTime);
    assertThat(actual, is(notNullValue()));
  }

  @Test()
  public void launch_A$Date__close_error() throws Exception {
    target.setBlowupOnClose(true);
    final Date lastSuccessfulRunTime = new Date();
    final Date actual = target.launch(lastSuccessfulRunTime);
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = Exception.class)
  public void launch_A$Date_T$NeutronCheckedException() throws Exception {
    final Date lastSuccessfulRunTime = new Date();
    target.plantBomb();
    target.launch(lastSuccessfulRunTime);
  }

  @Test
  public void extractLastRunRecsFromTable_A$Date() throws Exception {
    final Date lastRunTime = new Date();
    final List<TestNormalizedEntity> actual = target.extractLastRunRecsFromTable(lastRunTime);
    final List<TestNormalizedEntity> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void loadRecsForDeletion_A$Class$Session$Date$Set() throws Exception {
    when(session.getNamedNativeQuery(any(String.class))).thenReturn(nq);
    final Class<?> entityClass = target.getDenormalizedClass();
    final Date lastRunTime = new Date();
    final Set<String> deletionResults = mock(Set.class);
    target.loadRecsForDeletion(entityClass, session, lastRunTime, deletionResults);
  }

  @Test
  public void extractLastRunRecsFromView_A$Date$Set() throws Exception {
    final Date lastRunTime = new Date();
    final Set<String> deletionResults = mock(Set.class);
    final List<TestNormalizedEntity> actual =
        target.extractLastRunRecsFromView(lastRunTime, deletionResults);
    final List<TestNormalizedEntity> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void close_A$() throws Exception {
    target.close();
  }

  @Test(expected = IOException.class)
  public void close_A$_T$IOException() throws Exception {
    target.plantBomb();
    doThrow(IOException.class).when(sessionFactory).close();
    target.close();
  }

  @Test
  public void finish_A$() throws Exception {
    target.setFakeFinish(false);
    target.finish();
  }

  @Test(expected = NeutronCheckedException.class)
  public void finish_A$_T$NeutronCheckedException() throws Exception {
    target.plantBomb();
    target.setFakeFinish(false);
    target.finish();
  }

  @Test
  public void pullBucketRange_A$String$String() throws Exception {
    final String minId = "a";
    final String maxId = "z";
    final List<TestNormalizedEntity> actual = target.pullBucketRange(minId, maxId);
    final List<TestNormalizedEntity> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void awaitBulkProcessorClose_A$BulkProcessor() throws Exception {
    final BulkProcessor bp = mock(BulkProcessor.class);
    target.awaitBulkProcessorClose(bp);
  }

  @Test
  public void extractHibernate_A$() throws Exception {
    final int actual = target.extractHibernate();
    final int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getJobDao_A$() throws Exception {
    final BaseDaoImpl<TestNormalizedEntity> actual = target.getJobDao();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getFlightLog_A$() throws Exception {
    final FlightLog actual = target.getFlightLog();
    assertThat(actual, is(notNullValue())); // other tests for content
  }

  @Test
  public void getEsDao_A$() throws Exception {
    final ElasticsearchDao actual = target.getEsDao();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getQueueIndex_A$() throws Exception {
    final Queue<TestNormalizedEntity> actual = target.getQueueIndex();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setQueueIndex_A$LinkedBlockingDeque() throws Exception {
    final ConcurrentLinkedDeque<TestNormalizedEntity> queueIndex =
        mock(ConcurrentLinkedDeque.class);
    target.setQueueIndex(queueIndex);
  }

  @Test
  public void setFlightLog_A$FlightLog() throws Exception {
    final FlightLog track = mock(FlightLog.class);
    target.setFlightLog(track);
  }

  @Test
  public void getMapper_A$() throws Exception {
    final ObjectMapper actual = target.getMapper();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setMapper_A$ObjectMapper() throws Exception {
    final ObjectMapper mapper_ = mock(ObjectMapper.class);
    target.setMapper(mapper_);
  }

}
