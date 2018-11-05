package gov.ca.cwds.jobs.test;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.bulk.BulkProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.data.BaseDaoImpl;
import gov.ca.cwds.data.es.NeutronElasticSearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;

/**
 * Test rocket. Don't light this candle in production because Mach 1 versions tend to, well,
 * explode.
 * 
 * @author CWDS API Team
 * @see Goddard
 */
public class Mach1TestRocket extends BasePersonRocket<TestNormalizedEntity, TestDenormalizedEntity>
    implements ApiGroupNormalizer<TestDenormalizedEntity> {

  private static final long serialVersionUID = 1L;

  private static final String INSERT_CLIENT_LAST_CHG =
      "INSERT INTO GT_ID (IDENTIFIER)\n" + "SELECT CLT.IDENTIFIER \nFROM CLIENT_T clt\n"
          + "WHERE CLT.IBMSNAP_LOGMARKER > ?\nUNION\n" + "SELECT CLT.IDENTIFIER "
          + "FROM CLIENT_T clt\n" + "JOIN CL_ADDRT cla ON clt.IDENTIFIER = cla.FKCLIENT_T \n"
          + "WHERE CLA.IBMSNAP_LOGMARKER > ?\nUNION\n" + "SELECT CLT.IDENTIFIER "
          + "FROM CLIENT_T clt\n" + "JOIN CL_ADDRT cla ON clt.IDENTIFIER = cla.FKCLIENT_T\n"
          + "JOIN ADDRS_T  adr ON cla.FKADDRS_T  = adr.IDENTIFIER\n"
          + "WHERE ADR.IBMSNAP_LOGMARKER > ?";

  private boolean fakeMarkDone;
  private boolean fakeFinish = true;
  private boolean fakeBulkProcessor = true;
  private boolean fakeRanges = false;
  private boolean baseRanges = false;
  private boolean blowUpNameThread = false;
  private boolean shouldDelete = false;

  @Inject
  public Mach1TestRocket(final BaseDaoImpl<TestNormalizedEntity> jobDao,
      @Named("elasticsearch.dao.people") final NeutronElasticSearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper) {
    super(jobDao, esDao, lastRunFile, mapper, null, null);
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

  @Override
  public String getMQTName() {
    return getInitialLoadViewName();
  }

  public String getDriverTableNative() {
    return super.getDriverTable();
  }

  @Override
  public TestNormalizedEntity normalizeSingle(List<TestDenormalizedEntity> recs) {
    return new TestNormalizedEntity((String) recs.get(0).getPrimaryKey());
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
  public String getPrepLastChangeSQL() {
    return INSERT_CLIENT_LAST_CHG;
  }

  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    final StringBuilder buf = new StringBuilder();

    buf.append("SELECT x.* FROM ").append(dbSchemaName).append('.').append(getInitialLoadViewName())
        .append(" x WHERE x.clt_identifier BETWEEN ':fromId' AND ':toId' ");

    if (!getFlightPlan().isLoadSealedAndSensitive()) {
      buf.append(" AND x.CLT_SENSTV_IND = 'N' ");
    }

    buf.append(getJdbcOrderBy()).append(" FOR READ ONLY WITH UR ");
    return buf.toString();
  }

  @Override
  public TestDenormalizedEntity normalize(Map<Object, TestDenormalizedEntity> map) {
    return null;
  }

  @Override
  public void done() {
    if (isFakeMarkDone()) {
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

}
