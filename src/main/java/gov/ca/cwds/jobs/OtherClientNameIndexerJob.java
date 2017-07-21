package gov.ca.cwds.jobs;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.ReplicatedAkaDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.ReplicatedAkas;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherClientName;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.inject.CmsSessionFactory;
import gov.ca.cwds.jobs.exception.JobsException;
import gov.ca.cwds.jobs.inject.LastRunFile;
import gov.ca.cwds.jobs.util.jdbc.JobResultSetAware;
import gov.ca.cwds.jobs.util.transform.EntityNormalizer;

/**
 * Job to load Other Client Name from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class OtherClientNameIndexerJob
    extends BasePersonIndexerJob<ReplicatedAkas, ReplicatedOtherClientName>
    implements JobResultSetAware<ReplicatedOtherClientName> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OtherClientNameIndexerJob.class);

  /**
   * Construct batch job instance with all required dependencies.
   * 
   * @param dao Relationship View DAO
   * @param esDao ElasticSearch DAO
   * @param lastJobRunTimeFilename last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param sessionFactory Hibernate session factory
   */
  @Inject
  public OtherClientNameIndexerJob(final ReplicatedAkaDao dao, final ElasticsearchDao esDao,
      @LastRunFile final String lastJobRunTimeFilename, final ObjectMapper mapper,
      @CmsSessionFactory SessionFactory sessionFactory) {
    super(dao, esDao, lastJobRunTimeFilename, mapper, sessionFactory);
  }

  @Override
  public ReplicatedOtherClientName extract(ResultSet rs) throws SQLException {
    return ReplicatedOtherClientName.mapRowToBean(rs);
  }

  @Override
  protected Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return ReplicatedOtherClientName.class;
  }

  @Override
  protected ReplicatedAkas normalizeSingle(List<ReplicatedOtherClientName> recs) {
    return normalize(recs).get(0);
  }

  @Override
  protected List<ReplicatedAkas> normalize(List<ReplicatedOtherClientName> recs) {
    return EntityNormalizer.<ReplicatedAkas, ReplicatedOtherClientName>normalizeList(recs);
  }

  @Override
  protected UpdateRequest prepareUpsertRequest(ElasticSearchPerson esp, ReplicatedAkas p)
      throws IOException {

    // If at first you don't succeed, cheat. :-)
    StringBuilder buf = new StringBuilder();
    buf.append("{\"akas\":[");

    if (!p.getAkas().isEmpty()) {
      try {
        buf.append(p.getAkas().stream().map(this::jsonify).sorted(String::compareTo)
            .collect(Collectors.joining(",")));
      } catch (Exception e) {
        LOGGER.error("ERROR SERIALIZING OTHER CLIENT NAMES", e);
        throw new JobsException(e);
      }
    }

    buf.append("]}");

    final String insertJson = mapper.writeValueAsString(esp);
    final String updateJson = buf.toString();

    final String alias = esDao.getConfig().getElasticsearchAlias();
    final String docType = esDao.getConfig().getElasticsearchDocType();

    return new UpdateRequest(alias, docType, esp.getId()).doc(updateJson)
        .upsert(new IndexRequest(alias, docType, esp.getId()).source(insertJson));
  }

  @Override
  public String getInitialLoadViewName() {
    return "MQT_OTHER_CLIENT_NAME";
  }

  @Override
  @Deprecated
  protected String getLegacySourceTable() {
    return "OCL_NM_T";
  }

  /**
   * Optional method to customize JDBC ORDER BY clause on initial load.
   * 
   * @return custom ORDER BY clause for JDBC query
   */
  @Override
  public String getJdbcOrderBy() {
    return " ORDER BY x.FKCLIENT_T ";
  }

  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    StringBuilder buf = new StringBuilder();
    buf.append("SELECT x.* FROM ");
    buf.append(dbSchemaName);
    buf.append(".");
    buf.append(getInitialLoadViewName());
    buf.append(" x ");

    if (!getOpts().isLoadSealedAndSensitive()) {
      buf.append(" WHERE x.CLIENT_SENSITIVITY_IND = 'N' ");
    }

    buf.append(getJdbcOrderBy()).append(" FOR READ ONLY");
    return buf.toString();
  }

  /**
   * Batch job entry point.
   * 
   * @param args command line arguments
   */
  public static void main(String... args) {
    runMain(OtherClientNameIndexerJob.class, args);
  }

}
