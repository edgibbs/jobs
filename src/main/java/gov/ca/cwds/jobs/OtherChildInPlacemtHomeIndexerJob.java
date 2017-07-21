package gov.ca.cwds.jobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.ReplicatedOtherChildInPlacemtHomeR1Dao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherChildInPlacemtHomeR1;
import gov.ca.cwds.inject.CmsSessionFactory;
import gov.ca.cwds.jobs.inject.LastRunFile;

/**
 * Job to load Other Child In Placement Home from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class OtherChildInPlacemtHomeIndexerJob
    extends BasePersonIndexerJob<ReplicatedOtherChildInPlacemtHomeR1> {

  private static final Logger LOGGER =
      LogManager.getLogger(OtherChildInPlacemtHomeIndexerJob.class);

  /**
   * Construct batch job instance with all required dependencies.
   * 
   * @param mainDao OtherChildInPlacemtHomeDao DAO
   * @param elasticsearchDao ElasticSearch DAO
   * @param lastJobRunTimeFilename last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param sessionFactory Hibernate session factory
   */
  @Inject
  public OtherChildInPlacemtHomeIndexerJob(final ReplicatedOtherChildInPlacemtHomeR1Dao mainDao,
      final ElasticsearchDao elasticsearchDao, @LastRunFile final String lastJobRunTimeFilename,
      final ObjectMapper mapper, @CmsSessionFactory SessionFactory sessionFactory) {
    super(mainDao, elasticsearchDao, lastJobRunTimeFilename, mapper, sessionFactory);
  }

  @Override
  protected boolean isDelete(ReplicatedOtherChildInPlacemtHomeR1 t) {
    return t.getReplicationOperation() == CmsReplicationOperation.D;
  }

  /**
   * Batch job entry point.
   * 
   * @param args command line arguments
   */
  public static void main(String... args) {
    LOGGER.info("Run Other Child In Placement Home indexer job");
    try {
      runJob(OtherChildInPlacemtHomeIndexerJob.class, args);
    } catch (JobsException e) {
      LOGGER.error("STOPPING BATCH: " + e.getMessage(), e);
      throw e;
    }
  }

}
