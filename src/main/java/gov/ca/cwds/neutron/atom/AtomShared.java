package gov.ca.cwds.neutron.atom;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.jobs.util.jdbc.NeutronThreadUtils;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.launch.RocketSpecification;

/**
 * Shared features of all Elasticsearch indexing rockets.
 * 
 * @author CWDS API Team
 */
public interface AtomShared extends ApiMarker {

  /**
   * Make logger available to interfaces.
   * 
   * @return SLF4J logger
   */
  Logger getLogger();

  /**
   * Track the rocket's flight progress.
   * 
   * @return the rocket's progress tracker
   */
  FlightLog getFlightLog();

  /**
   * Elasticsearch operations.
   * 
   * @return Elasticsearch DAO
   */
  ElasticsearchDao getEsDao();

  /**
   * Jackson ObjectMapper suitable for Elasticsearch document operations.
   * 
   * @return Jackson ObjectMapper
   */
  ObjectMapper getMapper();

  /**
   * Getter for the rocket's flight plan.
   * 
   * @return this rocket's flight plan
   */
  FlightPlan getFlightPlan();

  /**
   * Common method sets the thread's name.
   * 
   * @param title title of thread
   */
  default void nameThread(final String title) {
    NeutronThreadUtils.nameThread(title, this);
  }

  default RocketSpecification rocketSpecs() {
    return new RocketSpecification();
  }

  default void catchYourBreath() {
    NeutronThreadUtils.catchYourBreath();
  }

}
