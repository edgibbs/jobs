package gov.ca.cwds.jobs;

import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedSubstituteCareProviderDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedSubstituteCareProvider;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

/**
 * Rocket to load Substitute Care Providers from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class SubstituteCareProviderIndexJob
    extends BasePersonRocket<ReplicatedSubstituteCareProvider, ReplicatedSubstituteCareProvider> {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SubstituteCareProviderIndexJob.class);

  //@formatter:off
  static final String INSERT_LAST_CHG =
      "INSERT INTO GT_ID (IDENTIFIER)\n" 
       + " SELECT DISTINCT R.IDENTIFIER \n" 
       + " FROM SB_PVDRT R \n"
       + " WHERE R.IBMSNAP_LOGMARKER > 'XYZ'";
  // @formatter:on

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao main DAO
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   * @param launchDirector launch director
   */
  @Inject
  public SubstituteCareProviderIndexJob(final ReplicatedSubstituteCareProviderDao dao,
      @Named("elasticsearch.dao.people") final ElasticsearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
  }

  @Override
  public List<Pair<String, String>> getPartitionRanges() throws NeutronCheckedException {
    return NeutronJdbcUtils.getCommonPartitionRanges16(this);
  }

  @Override
  public String getPrepLastChangeSQL() {
    try {
      return INSERT_LAST_CHG.replaceAll("XYZ",
          NeutronJdbcUtils.makeTimestampStringLookBack(determineLastSuccessfulRunTime()));
    } catch (NeutronCheckedException e) {
      throw CheeseRay.runtime(LOGGER, e, "ERROR BUILDING LAST CHANGE SQL: {}", e.getMessage());
    }
  }

  @Override
  public boolean isViewNormalizer() {
    return true;
  }

  @Override
  public Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return ReplicatedSubstituteCareProvider.class;
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(SubstituteCareProviderIndexJob.class, args);
  }

}
