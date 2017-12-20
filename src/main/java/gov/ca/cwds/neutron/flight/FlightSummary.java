package gov.ca.cwds.neutron.flight;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.enums.FlightStatus;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;
import gov.ca.cwds.neutron.util.NeutronDateUtils;

public class FlightSummary implements ApiMarker {

  private static final long serialVersionUID = 1L;

  /**
   * Runtime rocket name. Distinguish this rocket's threads from other running threads.
   */
  private final StandardFlightSchedule flightSchedule;

  private Date firstStart = new Date();

  private Date lastEnd = new Date();

  private Map<FlightStatus, Integer> status = new EnumMap<>(FlightStatus.class);

  private int totalRuns;

  private int recsSentToIndexQueue;

  private int recsSentToBulkProcessor;

  private int rowsNormalized;

  /**
   * Running count of records prepared for bulk indexing.
   */
  private int bulkPrepared;

  /**
   * Running count of records prepared for bulk deletion.
   */
  private int bulkDeleted;

  /**
   * Running count of records before bulk indexing.
   */
  private int bulkBefore;

  /**
   * Running count of records after bulk indexing.
   */
  private int bulkAfter;

  /**
   * Running count of errors during bulk indexing.
   */
  private int bulkError;

  public FlightSummary(final StandardFlightSchedule flightSchedule) {
    this.flightSchedule = flightSchedule;
  }

  public void accumulate(final FlightLog flightLog) {
    totalRuns++;
    this.bulkDeleted += flightLog.getCurrentBulkDeleted();
    this.bulkPrepared += flightLog.getCurrentBulkPrepared();
    this.bulkError += flightLog.getCurrentBulkError();
    this.bulkAfter += flightLog.getCurrentBulkAfter();

    this.rowsNormalized += flightLog.getCurrentNormalized();

    final Date startTime = new Date(flightLog.getStartTime());
    if (firstStart.before(startTime)) {
      firstStart = startTime;
    }

    final Date endTime = new Date(flightLog.getEndTime());
    if (lastEnd.before(endTime)) {
      lastEnd = endTime;
    }

    if (status.containsKey(flightLog.getStatus())) {
      status.put(flightLog.getStatus(), Integer.valueOf(status.get(flightLog.getStatus()) + 1));
    } else {
      status.put(flightLog.getStatus(), 1);
    }
  }

  public Map<FlightStatus, Integer> getStatus() {
    return status;
  }

  public void setStatus(EnumMap<FlightStatus, Integer> status) {
    this.status = status;
  }

  public int getTotalRuns() {
    return totalRuns;
  }

  public void setTotalRuns(int totalRuns) {
    this.totalRuns = totalRuns;
  }

  public int getRecsSentToIndexQueue() {
    return recsSentToIndexQueue;
  }

  public void setRecsSentToIndexQueue(int recsSentToIndexQueue) {
    this.recsSentToIndexQueue = recsSentToIndexQueue;
  }

  public int getRecsSentToBulkProcessor() {
    return recsSentToBulkProcessor;
  }

  public void setRecsSentToBulkProcessor(int recsSentToBulkProcessor) {
    this.recsSentToBulkProcessor = recsSentToBulkProcessor;
  }

  public int getRowsNormalized() {
    return rowsNormalized;
  }

  public void setRowsNormalized(int rowsNormalized) {
    this.rowsNormalized = rowsNormalized;
  }

  public int getBulkPrepared() {
    return bulkPrepared;
  }

  public void setBulkPrepared(int recsBulkPrepared) {
    this.bulkPrepared = recsBulkPrepared;
  }

  public int getBulkDeleted() {
    return bulkDeleted;
  }

  public void setBulkDeleted(int recsBulkDeleted) {
    this.bulkDeleted = recsBulkDeleted;
  }

  public int getBulkBefore() {
    return bulkBefore;
  }

  public void setBulkBefore(int recsBulkBefore) {
    this.bulkBefore = recsBulkBefore;
  }

  public int getBulkAfter() {
    return bulkAfter;
  }

  public void setBulkAfter(int recsBulkAfter) {
    this.bulkAfter = recsBulkAfter;
  }

  public int getBulkError() {
    return bulkError;
  }

  public void setBulkError(int recsBulkError) {
    this.bulkError = recsBulkError;
  }

  public Date getFirstStart() {
    return NeutronDateUtils.freshDate(firstStart);
  }

  public void setFirstStart(Date firstStart) {
    this.firstStart = NeutronDateUtils.freshDate(firstStart);
  }

  public Date getLastEnd() {
    return NeutronDateUtils.freshDate(lastEnd);
  }

  public void setLastEnd(Date lastEnd) {
    this.lastEnd = NeutronDateUtils.freshDate(lastEnd);
  }

  @Override
  public String toString() {
    return "FlightSummary [\n\trocketName=" + flightSchedule.getRocketName() + "\n\tfirstStart="
        + firstStart + "\n\tlastEnd=" + lastEnd + "\n\tstatus=" + status + "\n\ttotalRuns="
        + totalRuns + "\n\trecsSentToIndexQueue=" + recsSentToIndexQueue
        + "\n\trecsSentToBulkProcessor=" + recsSentToBulkProcessor + "\n\trowsNormalized="
        + rowsNormalized + "\n\trecsBulkPrepared=" + bulkPrepared + "\n\trecsBulkDeleted="
        + bulkDeleted + "\n\trecsBulkBefore=" + bulkBefore + "\n\trecsBulkAfter=" + bulkAfter
        + "\n\trecsBulkError=" + bulkError + "\n]";
  }

}
