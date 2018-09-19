package gov.ca.cwds.dao.cms;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import gov.ca.cwds.data.DaoException;
import gov.ca.cwds.data.cms.SystemCodeDao;
import gov.ca.cwds.data.persistence.cms.SystemCode;
import gov.ca.cwds.inject.CmsSessionFactory;

/**
 * Hibernate DAO for DB2 {@link SystemCode}.
 * 
 * @author CWDS API Team
 */
@SuppressWarnings({"squid:S1854"})
public class NeutronSystemCodeDao extends SystemCodeDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronSystemCodeDao.class);

  /**
   * Constructor
   * 
   * @param sessionFactory The session factory
   */
  @Inject
  public NeutronSystemCodeDao(@CmsSessionFactory SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  /**
   * DRS: Don't interfere with transaction management, like XA.
   * 
   * @param foreignKeyMetaTable meta group
   * @return all keys by meta table
   */
  @Override
  @SuppressWarnings("unchecked")
  public SystemCode[] findByForeignKeyMetaTable(String foreignKeyMetaTable) {
    LOGGER.info("NeutronSystemCodeDao.findByForeignKeyMetaTable: meta: {}", foreignKeyMetaTable);
    SystemCode[] ret;

    final String namedQueryName = SystemCode.class.getName() + ".findByForeignKeyMetaTable";
    final Session session = grabSession();
    final Transaction txn = joinTransaction(session);

    try {
      if (!txn.isActive()) {
        txn.begin();
      }
      final Query<SystemCode> query = session.getNamedQuery(namedQueryName)
          .setString("foreignKeyMetaTable", foreignKeyMetaTable).setReadOnly(true)
          .setCacheable(false).setHibernateFlushMode(FlushMode.MANUAL);

      ret = query.list().toArray(new SystemCode[0]);
      LOGGER.info("NeutronSystemCodeDao.findByForeignKeyMetaTable: meta: {}, count: {}",
          foreignKeyMetaTable, ret.length);

      // Shares the thread's connection. Do NOT close!
      // txn.commit();
      // session.close();
    } catch (Exception h) {
      LOGGER.error("NeutronSystemCodeDao.findByForeignKeyMetaTable: ERROR! {}", h.getMessage(), h);
      throw new DaoException(h);
    }

    return ret;
  }

  @Override
  @SuppressWarnings("unchecked")
  public SystemCode findBySystemCodeId(Number systemCodeId) {
    LOGGER.info("NeutronSystemCodeDao.findBySystemCodeId: systemCodeId: {}", systemCodeId);
    final String namedQueryName = SystemCode.class.getName() + ".findBySystemCodeId";
    final Session session = grabSession();
    final Transaction txn = joinTransaction(session);
    SystemCode ret;

    try {
      final Query<SystemCode> query = session.getNamedQuery(namedQueryName)
          .setShort("systemId", systemCodeId.shortValue()).setReadOnly(true).setCacheable(false);
      query.setHibernateFlushMode(FlushMode.MANUAL);

      ret = query.getSingleResult();

      // Shares the thread's connection. Do NOT close!
      // txn.commit();
      // session.close();
    } catch (Exception h) {
      LOGGER.error("NeutronSystemCodeDao.findBySystemCodeId: ERROR! {}", h.getMessage(), h);
      throw new DaoException(h);
    }

    return ret;
  }

}
