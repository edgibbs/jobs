package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.dao.ns.EsIntakeScreeningDao;
import gov.ca.cwds.dao.ns.IntakeParticipantDao;
import gov.ca.cwds.data.ApiTypedIdentifier;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPerson.ESOptionalCollection;
import gov.ca.cwds.data.es.ElasticSearchPersonScreening;
import gov.ca.cwds.data.persistence.ns.EsIntakeScreening;
import gov.ca.cwds.data.persistence.ns.IntakeParticipant;

public class IntakeScreeningJobTest extends Goddard<IntakeParticipant, EsIntakeScreening> {

  IntakeParticipantDao normalizedDao;
  EsIntakeScreeningDao viewDao;

  IntakeScreeningJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    normalizedDao = new IntakeParticipantDao(sessionFactory);
    viewDao = new EsIntakeScreeningDao(sessionFactory);
    target = new IntakeScreeningJob(normalizedDao, viewDao, esDao, null, MAPPER, flightPlan, launchDirector);
  }

  @Test
  public void type() throws Exception {
    assertThat(IntakeScreeningJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getDenormalizedClass_Args__() throws Exception {
    final Object actual = target.getDenormalizedClass();
    final Object expected = EsIntakeScreening.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getViewName_Args__() throws Exception {
    final String actual = target.getInitialLoadViewName();
    final String expected = "VW_SCREENING_HISTORY";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void keepCollections_Args__() throws Exception {
    final ESOptionalCollection[] actual = target.keepCollections();
    final ESOptionalCollection[] expected =
        new ESOptionalCollection[] {ESOptionalCollection.SCREENING};
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getOptionalElementName_Args__() throws Exception {
    final String actual = target.getOptionalElementName();
    final String expected = "screenings";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setInsertCollections_Args__ElasticSearchPerson__IntakeParticipant__List()
      throws Exception {
    ElasticSearchPerson esp = mock(ElasticSearchPerson.class);
    IntakeParticipant t = mock(IntakeParticipant.class);
    List list = new ArrayList();
    target.setInsertCollections(esp, t, list);
  }

  @Test
  public void getOptionalCollection_Args__ElasticSearchPerson__IntakeParticipant()
      throws Exception {
    final ElasticSearchPerson esp = mock(ElasticSearchPerson.class);
    final IntakeParticipant t = mock(IntakeParticipant.class);
    final List<? extends ApiTypedIdentifier<String>> actual = target.getOptionalCollection(esp, t);
    final Object expected = new ArrayList<ElasticSearchPersonScreening>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_Args__List() throws Exception {
    final List<EsIntakeScreening> recs = new ArrayList<EsIntakeScreening>();
    final List<IntakeParticipant> actual = target.normalize(recs);
    final List<IntakeParticipant> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void threadExtractJdbc_Args__() throws Exception {
    EsIntakeScreening es = new EsIntakeScreening();
    es.setScreeningId(DEFAULT_CLIENT_ID);
    List<EsIntakeScreening> results = new ArrayList<>();
    results.add(es);

    viewDao = mock(EsIntakeScreeningDao.class);
    when(viewDao.findAll()).thenReturn(results);

    target = new IntakeScreeningJob(normalizedDao, viewDao, esDao, null, MAPPER, flightPlan, launchDirector);

    target.threadRetrieveByJdbc();
  }

  @Test
  public void main_Args__StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/CWS-NS3/client_indexer_time.txt", "-S"};
    IntakeScreeningJob.main(args);
  }

  @Test
  public void getInitialLoadViewName_Args__() throws Exception {
    String actual = target.getInitialLoadViewName();
    String expected = "VW_SCREENING_HISTORY";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadQuery_Args__String() throws Exception {
    String dbSchemaName = null;
    String actual = target.getInitialLoadQuery(dbSchemaName);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void isInitialLoadJdbc_Args__() throws Exception {
    boolean actual = target.isInitialLoadJdbc();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

}
