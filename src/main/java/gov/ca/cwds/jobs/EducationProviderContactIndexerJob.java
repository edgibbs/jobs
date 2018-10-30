package gov.ca.cwds.jobs;

import gov.ca.cwds.data.es.NeutronElasticSearchDao;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import gov.ca.cwds.dao.cms.ReplicatedEducationProviderContactDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedEducationProviderContact;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

/**
 * Rocket to load Education Provider Contact from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class EducationProviderContactIndexerJob extends
    BasePersonRocket<ReplicatedEducationProviderContact, ReplicatedEducationProviderContact> {

  private static final long serialVersionUID = 1L;

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao Education Provider DAO
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   * @param launchDirector launch director
   */
  @Inject
  public EducationProviderContactIndexerJob(final ReplicatedEducationProviderContactDao dao,
      @Named("elasticsearch.dao.people") final NeutronElasticSearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
  }

  @Override
  public List<Pair<String, String>> getPartitionRanges() throws NeutronCheckedException {
    return NeutronJdbcUtils.getCommonPartitionRanges4(this);
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(EducationProviderContactIndexerJob.class, args);
  }

}
