package gov.ca.cwds.neutron.rocket.syscode;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import gov.ca.cwds.data.cms.SystemCodeDao;
import gov.ca.cwds.data.cms.SystemMetaDao;
import gov.ca.cwds.inject.CmsSessionFactory;
import gov.ca.cwds.inject.NsSessionFactory;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;
import gov.ca.cwds.rest.services.cms.CachingSystemCodeService;

/**
 * System codes loader Guice module.
 * 
 * @author CWDS API Team
 */
public class SystemCodesLoaderModule extends AbstractModule {

  private String cmsHibernateConfig = "jobs-cms-hibernate.cfg.xml";

  private String nsHibernateConfig = "jobs-ns-hibernate.cfg.xml";

  /**
   * Default constructor.
   */
  public SystemCodesLoaderModule() {
    // Default constructor
  }

  public SystemCodesLoaderModule(String cmsHibernateConfig, String nsHibernateConfig) {
    this.cmsHibernateConfig = cmsHibernateConfig;
    this.nsHibernateConfig = nsHibernateConfig;
  }

  @Override
  protected void configure() {
    // DB2 session factory:
    bind(SessionFactory.class).annotatedWith(CmsSessionFactory.class)
        .toInstance(new Configuration().configure(cmsHibernateConfig)
            .addAnnotatedClass(gov.ca.cwds.data.persistence.cms.SystemCode.class)
            .addAnnotatedClass(gov.ca.cwds.data.persistence.cms.SystemMeta.class)
            .buildSessionFactory());

    // PostgreSQL session factory:
    bind(SessionFactory.class).annotatedWith(NsSessionFactory.class).toInstance(new Configuration()
        .configure(nsHibernateConfig).addAnnotatedClass(NsSystemCode.class).buildSessionFactory());

    // DB2 tables:
    bind(SystemCodeDao.class);
    bind(SystemMetaDao.class);

    // PostgreSQL:
    bind(NsSystemCodeDao.class);
  }

  @Provides
  public SystemCodeCache provideSystemCodeCache(SystemCodeDao systemCodeDao,
      SystemMetaDao systemMetaDao) {
    final long secondsToRefreshCache = 15 * 24 * 60 * (long) 60; // 15 days
    final SystemCodeCache systemCodeCache =
        new CachingSystemCodeService(systemCodeDao, systemMetaDao, secondsToRefreshCache, true);
    systemCodeCache.register();
    return systemCodeCache;
  }

  public String getCmsHibernateConfig() {
    return cmsHibernateConfig;
  }

  public void setCmsHibernateConfig(String cmsHibernateConfig) {
    this.cmsHibernateConfig = cmsHibernateConfig;
  }

  public String getNsHibernateConfig() {
    return nsHibernateConfig;
  }

  public void setNsHibernateConfig(String nsHibernateConfig) {
    this.nsHibernateConfig = nsHibernateConfig;
  }

}
