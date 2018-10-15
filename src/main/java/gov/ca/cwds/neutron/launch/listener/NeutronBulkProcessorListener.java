package gov.ca.cwds.neutron.launch.listener;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;

import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;

/**
 * Neutron implementation of Elasticsearch BulkProcessor.Listener.
 * 
 * @author CWDS API Team
 */
public class NeutronBulkProcessorListener implements BulkProcessor.Listener {

  private static final ConditionalLogger LOGGER =
      new JetPackLogger(NeutronBulkProcessorListener.class);

  /**
   * Track rocket's flight progress.
   */
  protected final FlightLog flightLog;

  /**
   * Construct for a single rocket and bulk processor.
   * 
   * @param flightLog the rocket's flight log
   */
  public NeutronBulkProcessorListener(FlightLog flightLog) {
    this.flightLog = flightLog;
  }

  @Override
  public void beforeBulk(long executionId, BulkRequest request) {
    final int numActions = request.numberOfActions();
    flightLog.addToBulkBefore(numActions);
    LOGGER.debug("Ready to execute bulk of {} actions", numActions);
  }

  @Override
  public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
    final int numActions = request.numberOfActions();

    if (response.hasFailures()) {
      // NEXT: use CindyBulkResponse for per record error details instead of toString().
      final String failure = response.buildFailureMessage();
      LOGGER.error("\n\t\t >>>>>> BULK FAILURES! status: {}, errors: {}\n", response.status(),
          failure);
      flightLog.trackBulkError();
    }

    flightLog.addToBulkAfter(numActions);
    LOGGER.info("Executed bulk of {} actions", numActions);
  }

  @Override
  public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
    flightLog.trackBulkError();
    LOGGER.error("ERROR EXECUTING BULK", failure);
  }

}
