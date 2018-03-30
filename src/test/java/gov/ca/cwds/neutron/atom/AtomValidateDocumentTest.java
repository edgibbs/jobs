package gov.ca.cwds.neutron.atom;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.test.Mach1TestRocket;
import gov.ca.cwds.jobs.test.TestDenormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntityDao;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightLog;

public class AtomValidateDocumentTest
    extends Goddard<TestNormalizedEntity, TestDenormalizedEntity> {

  TestNormalizedEntityDao dao;
  Mach1TestRocket target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    dao = new TestNormalizedEntityDao(sessionFactory);
    target = new Mach1TestRocket(dao, esDao, lastRunFile, MAPPER);
  }

  @Test
  public void type() throws Exception {
    assertThat(AtomValidateDocument.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void readPerson_Args__String() throws Exception {
    final String json =
        IOUtils.toString(getClass().getResourceAsStream("/fixtures/es_person.json"));
    ElasticSearchPerson actual = target.readPerson(json);
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = NeutronCheckedException.class)
  public void readPerson_Args__String_T__NeutronException() throws Exception {
    final String json = null;
    target.readPerson(json);
  }

  @Test
  public void processDocumentHits_Args__SearchHits() throws Exception {
    target.processDocumentHits(hits);
  }

  // @Test(expected = NeutronCheckedException.class)
  // public void processDocumentHits_Args__SearchHits_T__NeutronException() throws Exception {
  // when(hits.getHits()).thenThrow(NeutronCheckedException.class);
  // target.processDocumentHits(hits);
  // }

  @Test
  public void validateDocument_Args__ElasticSearchPerson() throws Exception {
    final ElasticSearchPerson person = new ElasticSearchPerson();
    boolean actual = target.validateDocument(person);
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void validateDocuments_Args__() throws Exception {
    target.validateDocuments();
  }

  @Test
  public void validateDocuments_Args___T__NeutronException() throws Exception {
    final FlightLog flightLog = mock(FlightLog.class);
    try {
      target.setFlightLog(flightLog);
      when(flightLog.getAffectedDocumentIds()).thenThrow(NeutronCheckedException.class);
      target.validateDocuments();
      fail("Expected exception was not thrown!");
    } catch (NeutronCheckedException e) {
    }
  }

}
