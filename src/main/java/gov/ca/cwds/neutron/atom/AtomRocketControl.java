package gov.ca.cwds.neutron.atom;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.flight.FlightLog;

/**
 * Rocket control interface. Mark major steps completed (retrieve, transform, index) or fail the
 * entire flight.
 * 
 * @author CWDS API Team
 * @see FlightLog
 */
public interface AtomRocketControl extends ApiMarker {

  /**
   * Is the rocket still flying?
   * 
   * @return true if rocket has not completed
   */
  boolean isRunning();

  /**
   * Did the rocket fail?
   * 
   * @return true if Job has failed
   */
  boolean isFailed();

  /**
   * Has Elasticsearch indexing step finished?
   * 
   * @return true if indexing has completed
   */
  boolean isIndexDone();

  /**
   * Has the normalization/transformation step finished?
   * 
   * @return true if normalization has completed
   */
  boolean isTransformDone();

  /**
   * Has the retrieval step finished?
   * 
   * @return true if retrieval has completed
   */
  boolean isRetrieveDone();

  /**
   * Mark the rocket as failed and stop the rocket.
   * 
   * <p>
   * Worker threads should stop themselves.
   * </p>
   */
  void fail();

  /**
   * Mark Elasticsearch indexing complete.
   */
  void doneIndex();

  /**
   * Mark the rocket done. Working threads should stop themselves.
   */
  void done();

  /**
   * Mark the retrieval step done.
   */
  void doneRetrieve();

  /**
   * Mark the normalize/transform step done.
   */
  void doneTransform();

}
