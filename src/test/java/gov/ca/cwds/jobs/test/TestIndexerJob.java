package gov.ca.cwds.jobs.test;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.hibernate.SessionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.inject.CmsSessionFactory;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.launch.FlightRecorder;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;

public class TestIndexerJob extends BasePersonRocket<TestNormalizedEntity, TestDenormalizedEntity>
    implements ApiGroupNormalizer<TestDenormalizedEntity> {

  private boolean fakeMarkDone;
  private boolean fakeFinish = true;
  private boolean fakeBulkProcessor = true;
  private boolean fakeRanges = false;
  private boolean baseRanges = false;
  private boolean blowUpNameThread = false;
  private boolean shouldDelete = false;

  private boolean blowup = false;

  @Inject
  public TestIndexerJob(final TestNormalizedEntityDao dao, final ElasticsearchDao esDao,
      @LastRunFile final String lastJobRunTimeFilename, final ObjectMapper mapper,
      @CmsSessionFactory SessionFactory sessionFactory, FlightRecorder jobHistory) {
    super(dao, esDao, lastJobRunTimeFilename, mapper, null);
  }

  @Override
  public boolean mustDeleteLimitedAccessRecords() {
    return true;
  }

  @Override
  public TestDenormalizedEntity extract(final ResultSet rs) throws SQLException {
    return new TestDenormalizedEntity("abc1234567", "1", "2", "3");
  }

  @Override
  public String getLegacySourceTable() {
    return "CRAP_T";
  }

  @Override
  public String getInitialLoadViewName() {
    return "VW_NUTTIN";
  }

  public String getDriverTableNative() {
    return super.getDriverTable();
  }

  @Override
  public TestNormalizedEntity normalizeSingle(List<TestDenormalizedEntity> recs) {
    return recs.isEmpty() ? new TestNormalizedEntity(Goddard.DEFAULT_CLIENT_ID)
        : new TestNormalizedEntity((String) recs.get(0).getPrimaryKey());
  }

  @Override
  public Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return TestDenormalizedEntity.class;
  }

  @Override
  public Class<TestDenormalizedEntity> getNormalizationClass() {
    return TestDenormalizedEntity.class;
  }

  @Override
  public Serializable getNormalizationGroupKey() {
    return null;
  }

  @Override
  public TestDenormalizedEntity normalize(Map<Object, TestDenormalizedEntity> map) {
    return null;
  }

  @Override
  public void done() {
    if (isBlowup() || isFakeMarkDone()) {
      throw new NeutronRuntimeException("fake error");
    }

    super.done();
  }

  @Override
  public void awaitBulkProcessorClose(BulkProcessor bp) {
    if (!fakeBulkProcessor) {
      super.awaitBulkProcessorClose(bp);
    }
  }

  @Override
  public void nameThread(String title) {
    if (blowUpNameThread) {
      throw new NeutronRuntimeException("test bombing");
    }

    super.nameThread(title);
  }

  @Override
  public synchronized void finish() throws NeutronCheckedException {
    if (!fakeFinish) {
      super.finish();
    }
  }

  @Override
  public List<Pair<String, String>> getPartitionRanges() throws NeutronCheckedException {
    if (baseRanges) {
      return super.getPartitionRanges();
    }

    final List<Pair<String, String>> ret = new ArrayList<>();

    if (fakeRanges) {
      ret.add(Pair.of("aaaaaaaaaa", "999999999"));
    }

    return ret;
  }

  public boolean isFakeFinish() {
    return fakeFinish;
  }

  public void setFakeFinish(boolean fakeFinish) {
    this.fakeFinish = fakeFinish;
  }

  public boolean isFakeMarkDone() {
    return fakeMarkDone;
  }

  public void setFakeMarkDone(boolean fakeMarkDone) {
    this.fakeMarkDone = fakeMarkDone;
  }

  public boolean isFakeBulkProcessor() {
    return fakeBulkProcessor;
  }

  public void setFakeBulkProcessor(boolean fakeBulkProcessor) {
    this.fakeBulkProcessor = fakeBulkProcessor;
  }

  public boolean isFakeRanges() {
    return fakeRanges;
  }

  public void setFakeRanges(boolean fakeRanges) {
    this.fakeRanges = fakeRanges;
  }

  public boolean isBlowUpNameThread() {
    return blowUpNameThread;
  }

  public void setBlowUpNameThread(boolean blowUpThreadIndex) {
    this.blowUpNameThread = blowUpThreadIndex;
  }

  public boolean isBaseRanges() {
    return baseRanges;
  }

  public void setBaseRanges(boolean baseRanges) {
    this.baseRanges = baseRanges;
  }

  @Override
  public boolean isDelete(TestNormalizedEntity t) {
    return shouldDelete;
  }

  public boolean isShouldDelete() {
    return shouldDelete;
  }

  public void setShouldDelete(boolean shouldDelete) {
    this.shouldDelete = shouldDelete;
  }

  public boolean isBlowup() {
    return blowup;
  }

  public void plantBomb() {
    this.blowup = true;
  }

}
