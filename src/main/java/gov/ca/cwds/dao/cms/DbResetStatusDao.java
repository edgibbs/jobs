package gov.ca.cwds.dao.cms;

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
public class DbResetStatusDao extends BaseDaoImpl<DatabaseResetEntry> {

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
   * @param schemaName - target schema
   * @return max start time stamp
   */
  @SuppressWarnings("unchecked")
  public DatabaseResetEntry findBySchemaStartTime(String schemaName) {
    final Query<DatabaseResetEntry> query = this.getSessionFactory().getCurrentSession()
        .getNamedQuery("gov.ca.cwds.data.persistence.cms.DatabaseResetEntry.findLastRun");
    query.setParameter("schema_name", schemaName, StringType.INSTANCE);
    return query.getSingleResult();
  }

}
