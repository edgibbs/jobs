package gov.ca.cwds.jobs.config;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.EntityType;

import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.Metamodel;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.StatelessSessionBuilder;
import org.hibernate.TypeHelper;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;

public class NeutronStaticSessionFactory {

  public static class NeutronSharedSessionFactory implements SessionFactory {

    private static final int DEFAULT_DELAY = 3500;

    private SessionFactory sf;
    private final Lock lock;
    private final Condition condition;
    private volatile boolean held = true;

    public NeutronSharedSessionFactory(SessionFactory sf) {
      this.sf = sf;
      lock = new ReentrantLock();
      condition = lock.newCondition();
      runCloseThread();
    }

    @Override
    public SessionFactoryImplementor getSessionFactory() {
      return sf.getSessionFactory();
    }

    @Override
    public Reference getReference() throws NamingException {
      return sf.getReference();
    }

    @Override
    public EntityManager createEntityManager() {
      return sf.createEntityManager();
    }

    @Override
    public <T> List<EntityGraph<? super T>> findEntityGraphsByType(Class<T> entityClass) {
      return sf.findEntityGraphsByType(entityClass);
    }

    @Override
    public SessionFactoryOptions getSessionFactoryOptions() {
      return sf.getSessionFactoryOptions();
    }

    @Override
    public EntityManager createEntityManager(Map map) {
      return sf.createEntityManager(map);
    }

    @Override
    public SessionBuilder withOptions() {
      return sf.withOptions();
    }

    @Override
    public Session openSession() throws HibernateException {
      return sf.openSession();
    }

    @Override
    public EntityType getEntityTypeByName(String entityName) {
      return sf.getEntityTypeByName(entityName);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
      return sf.createEntityManager(synchronizationType);
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
      return sf.getCurrentSession();
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
      return sf.createEntityManager(synchronizationType, map);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public StatelessSessionBuilder withStatelessOptions() {
      return sf.withStatelessOptions();
    }

    @Override
    public StatelessSession openStatelessSession() {
      return sf.openStatelessSession();
    }

    @Override
    public StatelessSession openStatelessSession(Connection connection) {
      return sf.openStatelessSession(connection);
    }

    @Override
    public Statistics getStatistics() {
      return sf.getStatistics();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
      return sf.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
      return sf.getMetamodel();
    }

    @Override
    public boolean isClosed() {
      return sf.isClosed();
    }

    @Override
    public boolean isOpen() {
      return sf.isOpen();
    }

    @Override
    public Cache getCache() {
      return sf.getCache();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Set getDefinedFilterNames() {
      return sf.getDefinedFilterNames();
    }

    @Override
    public void close() {
      // Close thread calls close().
      held = false;
      condition.signalAll();
      lock.unlock();
    }

    @Override
    public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
      return sf.getFilterDefinition(filterName);
    }

    @Override
    public boolean containsFetchProfileDefinition(String name) {
      return sf.containsFetchProfileDefinition(name);
    }

    @Override
    public Map<String, Object> getProperties() {
      return sf.getProperties();
    }

    @Override
    public TypeHelper getTypeHelper() {
      return sf.getTypeHelper();
    }

    @SuppressWarnings({"deprecation", "rawtypes"})
    @Override
    public ClassMetadata getClassMetadata(Class entityClass) {
      return sf.getClassMetadata(entityClass);
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
      return sf.getPersistenceUnitUtil();
    }

    @Override
    public ClassMetadata getClassMetadata(String entityName) {
      return sf.getClassMetadata(entityName);
    }

    @Override
    public void addNamedQuery(String name, Query query) {
      sf.addNamedQuery(name, query);
    }

    @Override
    public CollectionMetadata getCollectionMetadata(String roleName) {
      return sf.getCollectionMetadata(roleName);
    }

    @Override
    public Map<String, ClassMetadata> getAllClassMetadata() {
      return sf.getAllClassMetadata();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
      return sf.unwrap(cls);
    }

    @Override
    public Map getAllCollectionMetadata() {
      return sf.getAllCollectionMetadata();
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
      sf.addNamedEntityGraph(graphName, entityGraph);
    }

    protected void runCloseThread() {
      new Thread(() -> {
        try {
          Thread.sleep(DEFAULT_DELAY); // NOSONAR
          while (true) {
            System.out.println("condition.await()");
            condition.await(); // Possible spurious wake-up. Must still evaluation the situation.
            Thread.sleep(DEFAULT_DELAY); // NOSONAR

            if (!held) {
              try {
                lock.lock();
                held = false;
                condition.signalAll();
                System.out.println("CLOSE DOWN SESSION FACTORY!");
              } finally {
                lock.unlock(); // Always unlock, no matter what!
              }
              break;
            }
          }

        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }

      }).start();
    }

    protected boolean isHeld() {
      return held;
    }

    protected void setHeld(boolean held) {
      this.held = held;
    }

    protected Lock getLock() {
      return lock;
    }

    protected Condition getCondition() {
      return condition;
    }

  }

  private static NeutronSharedSessionFactory sessionFactory;

  static {
    sessionFactory = new NeutronSharedSessionFactory(
        new Configuration().configure("test-cms-hibernate.cfg.xml").buildSessionFactory());
  }

  public static SessionFactory getSessionFactory() {
    sessionFactory.getLock().lock();
    sessionFactory.held = true;
    return sessionFactory;
  }

}
