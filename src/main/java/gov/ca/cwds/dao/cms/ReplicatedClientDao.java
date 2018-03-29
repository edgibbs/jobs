package gov.ca.cwds.dao.cms;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Inject;

import gov.ca.cwds.data.BaseDaoImpl;
import gov.ca.cwds.data.DaoException;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.BatchBucketDao;
import gov.ca.cwds.inject.CmsSessionFactory;

/**
 * Hibernate DAO for DB2 {@link ReplicatedClient}.
 * 
 * @author CWDS API Team
 * @see CmsSessionFactory
 * @see SessionFactory
 */
public class ReplicatedClientDao extends BaseDaoImpl<ReplicatedClient>
    implements BatchBucketDao<ReplicatedClient> {

  /**
   * Constructor
   * 
   * @param sessionFactory The sessionFactory
   */
  @Inject
  public ReplicatedClientDao(@CmsSessionFactory SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  @SuppressWarnings("unchecked")
  public List<ReplicatedClient> findByTemp() {
    final String namedQueryName = this.makeNamedQueryName("findByTemp");
    final Session session = this.getSessionFactory().getCurrentSession();
    Transaction txn = session.getTransaction();
    txn = txn != null ? txn : session.beginTransaction();
    if (TransactionStatus.NOT_ACTIVE == txn.getStatus() || !txn.isActive()) {
      txn.begin();
    }

    try {
      final NativeQuery<ReplicatedClient> h = session.getNamedNativeQuery(namedQueryName);
      final Builder<ReplicatedClient> immutable = new Builder<>();
      immutable.addAll(h.list());
      txn.commit();
      return immutable.build();
    } catch (HibernateException h) {
      txn.rollback();
      String message = h.getMessage() + ". Transaction Status: " + txn.getStatus();
      throw new DaoException(message, h);
    }
  }

  protected String makeNamedQueryName(String suffix) {
    return this.getEntityClass().getName() + "." + suffix;
  }

}
