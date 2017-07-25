package gov.ca.cwds.dao.cms;

import org.hibernate.SessionFactory;

import com.google.inject.Inject;

import gov.ca.cwds.data.persistence.cms.rep.ReplicatedReporterR1;
import gov.ca.cwds.data.std.BatchBucketDao;
import gov.ca.cwds.inject.CmsSessionFactory;

/**
 * Hibernate DAO for DB2 {@link ReplicatedReporterR1}.
 * 
 * @author CWDS API Team
 * @see CmsSessionFactory
 * @see SessionFactory
 */
public class ReplicatedReporterR1Dao extends BatchDaoImpl<ReplicatedReporterR1>
    implements BatchBucketDao<ReplicatedReporterR1> {

  /**
   * Constructor
   * 
   * @param sessionFactory The sessionFactory
   */
  @Inject
  public ReplicatedReporterR1Dao(@CmsSessionFactory SessionFactory sessionFactory) {
    super(sessionFactory);
  }

}
