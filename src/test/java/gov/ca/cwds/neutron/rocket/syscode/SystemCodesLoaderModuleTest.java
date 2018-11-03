package gov.ca.cwds.neutron.rocket.syscode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hibernate.HibernateException;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.binder.AnnotatedBindingBuilder;

import gov.ca.cwds.data.cms.SystemCodeDao;
import gov.ca.cwds.data.cms.SystemMetaDao;
import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;

public class SystemCodesLoaderModuleTest extends Goddard<ReplicatedClient, RawClient> {

  SystemCodesLoaderModule target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    target = new SystemCodesLoaderModule();
  }

  @Test
  public void type() throws Exception {
    assertThat(SystemCodesLoaderModule.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void instantiation_ctor() throws Exception {
    target = new SystemCodesLoaderModule("one.xml", "two.xml");
    assertThat(target, notNullValue());
  }

  @Test
  public void provideSystemCodeCache_Args__SystemCodeDao__SystemMetaDao() throws Exception {
    SystemCodeCache actual = target.provideSystemCodeCache(systemCodeDao, systemMetaDao);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getCmsHibernateConfig_Args__() throws Exception {
    final String actual = target.getCmsHibernateConfig();
    final String expected = "jobs-cms-hibernate.cfg.xml";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCmsHibernateConfig_Args__String() throws Exception {
    String cmsHibernateConfig = null;
    target.setCmsHibernateConfig(cmsHibernateConfig);
  }

  @Test
  public void getNsHibernateConfig_Args__() throws Exception {
    final String actual = target.getNsHibernateConfig();
    final String expected = "jobs-ns-hibernate.cfg.xml";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setNsHibernateConfig_Args__String() throws Exception {
    String nsHibernateConfig = null;
    target.setNsHibernateConfig(nsHibernateConfig);
  }

  @Test(expected = HibernateException.class)
  public void configure_A$() throws Exception {
    final Binder binder = mock(Binder.class);
    final AnnotatedBindingBuilder builder = mock(AnnotatedBindingBuilder.class);
    when(binder.bind(any(Class.class))).thenReturn(builder);
    target.setTestBinder(binder);
    target.configure();
  }

  @Test
  public void provideSystemCodeCache_A$SystemCodeDao$SystemMetaDao() throws Exception {
    SystemCodeDao systemCodeDao = mock(SystemCodeDao.class);
    SystemMetaDao systemMetaDao = mock(SystemMetaDao.class);
    SystemCodeCache actual = target.provideSystemCodeCache(systemCodeDao, systemMetaDao);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getCmsHibernateConfig_A$() throws Exception {
    String actual = target.getCmsHibernateConfig();
    String expected = "jobs-cms-hibernate.cfg.xml";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCmsHibernateConfig_A$String() throws Exception {
    String cmsHibernateConfig = null;
    target.setCmsHibernateConfig(cmsHibernateConfig);
  }

  @Test
  public void getNsHibernateConfig_A$() throws Exception {
    String actual = target.getNsHibernateConfig();
    String expected = "jobs-ns-hibernate.cfg.xml";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setNsHibernateConfig_A$String() throws Exception {
    String nsHibernateConfig = null;
    target.setNsHibernateConfig(nsHibernateConfig);
  }

}
