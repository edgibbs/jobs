package gov.ca.cwds.neutron.component;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;

import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.launch.listener.NeutronBulkProcessorListener;

/**
 * Builds and executes an Elasticsearch bulk processor for bulk loading.
 * 
 * <p>
 * <a href="http://jimmyneutron.wikia.com/wiki/Hover_Car">Jimmy's Hover Car.</a>
 * </p>
 * 
 * @author CWDS API Team
 */
public class HoverCar implements ApiMarker, NeutronBulkProcessorBuilder {

  private static final long serialVersionUID = 1L;

  private static final int ES_BULK_SIZE = 1000;

  private static final int ES_BYTES_MB = 5;

  /**
   * Track rocket progress.
   */
  protected final FlightLog flightLog;

  /**
   * Elasticsearch client DAO.
   */
  protected transient ElasticsearchDao esDao;

  /**
   * Constructor.
   * 
   * @param esDao ES DAO
   * @param flightLog progress tracker
   */
  public HoverCar(final ElasticsearchDao esDao, final FlightLog flightLog) {
    this.esDao = esDao;
    this.flightLog = flightLog;
  }

  /**
   * Run multiple threads for bulk indexing. Send a max of either {@link #ES_BYTES_MB} megabytes or
   * {@link #ES_BULK_SIZE} records, whichever comes first.
   * 
   * @return Elasticsearch BulkProcessor
   */
  @Override
  public BulkProcessor buildBulkProcessor() {
    return BulkProcessor
        .builder(esDao.getClient(), new NeutronBulkProcessorListener(this.flightLog))
        .setBulkActions(ES_BULK_SIZE).setBulkSize(new ByteSizeValue(ES_BYTES_MB, ByteSizeUnit.MB))
        .setConcurrentRequests(1).build();
  }

  public FlightLog getFlightLog() {
    return flightLog;
  }

}
