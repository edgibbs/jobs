package gov.ca.cwds.dao.cms;

import java.util.Collection;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.type.StringType;

import com.google.inject.Inject;

import gov.ca.cwds.data.BaseDaoImpl;
import gov.ca.cwds.data.persistence.cms.DatabaseResetEntry;
import gov.ca.cwds.inject.CmsSessionFactory;

/**
 * Hibernate DAO for DB2 {@link DatabaseResetEntry}.
 * 
 * @author CWDS API Team
 * @see CmsSessionFactory
 * @see SessionFactory
 */
public class DbResetStatusDao extends BaseDaoImpl<DatabaseResetEntry>
 {

  /**
   * Constructor
   * 
   * @param sessionFactory The sessionFactory
   */
  @Inject
  public DbResetStatusDao(@CmsSessionFactory SessionFactory sessionFactory) {
    super(sessionFactory);
  }
  
  /**
   * Find by Schema Start Time Stamp
   * 
   * @param refreshStatus - the status of the refresh job
   * @return max start time stamp
   */
  @SuppressWarnings("unchecked")
  public DatabaseResetEntry findBySchemaStartTime(String refreshStatus) {
	    final Query<DatabaseResetEntry> query = this.getSessionFactory().getCurrentSession()
	        .getNamedQuery("gov.ca.cwds.data.persistence.cms.DatabaseResetEntry.findLastRun");
	    query.setParameter("refreshStatus", refreshStatus, StringType.INSTANCE);
	    return query.getSingleResult();
	  }

}
