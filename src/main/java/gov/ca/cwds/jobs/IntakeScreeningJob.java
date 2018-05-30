package gov.ca.cwds.jobs;

import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import java.util.List;

import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.ns.EsIntakeScreeningDao;
import gov.ca.cwds.dao.ns.IntakeParticipantDao;
import gov.ca.cwds.data.ApiTypedIdentifier;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPerson.ESOptionalCollection;
import gov.ca.cwds.data.es.ElasticSearchPersonScreening;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.ns.EsIntakeScreening;
import gov.ca.cwds.data.persistence.ns.IntakeParticipant;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomRowMapper;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.neutron.util.transform.EntityNormalizer;

/**
 * Rocket loads Intake Screenings from PostgreSQL into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class IntakeScreeningJob extends BasePersonRocket<IntakeParticipant, EsIntakeScreening>
    implements AtomRowMapper<EsIntakeScreening> {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(IntakeScreeningJob.class);

  private static final ESOptionalCollection[] KEEP_COLLECTIONS =
      new ESOptionalCollection[] {ESOptionalCollection.SCREENING};

  private transient EsIntakeScreeningDao viewDao;

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao Intake Screening DAO
   * @param viewDao view Dao
   * @param esDao ElasticSearch DAO
   * @param lastRunFile take a guess
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   * @param launchDirector launch director
   */
  @Inject
  public IntakeScreeningJob(final IntakeParticipantDao dao, final EsIntakeScreeningDao viewDao,
      @Named("elasticsearch.dao.people") final ElasticsearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
    this.viewDao = viewDao;
  }

  @Override
  public String getInitialLoadViewName() {
    return getDenormalizedClass().getDeclaredAnnotation(Table.class).name();
  }

  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    final StringBuilder buf = new StringBuilder();
    buf.append("SELECT x.* FROM ").append(dbSchemaName).append('.').append(getInitialLoadViewName())
        .append(" x FOR READ ONLY WITH UR ");
    return buf.toString();
  }

  @Override
  public boolean isInitialLoadJdbc() {
    return true;
  }

  @Override
  protected void threadRetrieveByJdbc() {
    nameThread("retrieval");
    LOGGER.info("BEGIN: retrieval: NS View Reader");

    try {
      final List<EsIntakeScreening> results = this.viewDao.findAll();
      for (EsIntakeScreening es : results) {
        queueNormalize.putLast(es);
      }

    } catch (Exception e) {
      fail();
      throw new NeutronRuntimeException("ERROR READING PG VIEW", e);
    } finally {
      doneRetrieve();
    }

    LOGGER.info("DONE: retrieval: NS View Reader");
  }

  @Override
  public Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return EsIntakeScreening.class;
  }

  /**
   * Which optional ES collections to retain for insert JSON. Child classes that populate optional
   * collections should override this method.
   * 
   * @return array of optional collections to keep in insert JSON
   */
  @Override
  public ESOptionalCollection[] keepCollections() {
    return KEEP_COLLECTIONS.clone();
  }

  @Override
  public String getOptionalElementName() {
    return "screenings";
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setInsertCollections(ElasticSearchPerson esp, IntakeParticipant t,
      List<? extends ApiTypedIdentifier<String>> list) {
    esp.setScreenings((List<ElasticSearchPersonScreening>) list);
  }

  /**
   * Return the optional collection used to build the update JSON, if any. Child classes that
   * populate optional collections should override this method.
   * 
   * @param esp ES person document object
   * @param t normalized type
   * @return List of ES person elements
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<ApiTypedIdentifier<String>> getOptionalCollection(ElasticSearchPerson esp,
      IntakeParticipant t) {
    return (List<ApiTypedIdentifier<String>>) (List<? extends ApiTypedIdentifier<String>>) esp
        .getScreenings();
  }

  @Override
  public List<IntakeParticipant> normalize(List<EsIntakeScreening> recs) {
    return EntityNormalizer.<IntakeParticipant, EsIntakeScreening>normalizeList(recs);
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(IntakeScreeningJob.class, args);
  }

}
