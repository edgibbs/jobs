package gov.ca.cwds.neutron.flight;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.enums.FlightStatus;
import gov.ca.cwds.neutron.launch.StandardFlightSchedule;
import gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils;
import gov.ca.cwds.rest.api.domain.DomainChef;
import gov.ca.cwds.utils.JsonUtils;

/**
 * Summarizes a rocket's flight.
 * 
 * @author CWDS API Team
 */
public class FlightSummary implements ApiMarker {

  private static final long serialVersionUID = 1L;

  /**
   * Runtime rocket name and settings.
   */
  private final StandardFlightSchedule flightSchedule;

  @JsonProperty("first_start")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DomainChef.TIMESTAMP_ISO8601_FORMAT)
  private Date firstStart = new Date();

  @JsonProperty("last_end")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DomainChef.TIMESTAMP_ISO8601_FORMAT)
  private Date lastEnd = new Date();

  @JsonProperty("status_history")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private Map<FlightStatus, Integer> status = new EnumMap<>(FlightStatus.class);

  @JsonProperty("total_runs")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private int totalRuns;

  @JsonProperty("sent_to_index_queue")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private int recsSentToIndexQueue;

  @JsonProperty("sent_to_elasticsearch")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private int recsSentToBulkProcessor;

  @JsonProperty("rows_normalized")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private int rowsNormalized;

  /**
   * Running count of records prepared for bulk indexing.
   */
  @JsonProperty("bulk_prepared")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private int bulkPrepared;

  /**
   * Running count of records prepared for bulk deletion.
   */
  @JsonProperty("bulk_deleted")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private int bulkDeleted;

  /**
   * Running count of records before bulk indexing.
   */
  @JsonProperty("bulk_before")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private int bulkBefore;

  /**
   * Running count of records after bulk indexing.
   */
  @JsonProperty("bulk_after")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private int bulkAfter;

  /**
   * Running count of errors during bulk indexing.
   */
  @JsonProperty("bulk_error")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private int bulkError;

  @JsonProperty("validation_errors")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private boolean validationErrors;

  public FlightSummary(final StandardFlightSchedule flightSchedule) {
    this.flightSchedule = flightSchedule;
  }

  public synchronized void accumulate(final FlightLog flightLog) {
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

  @JsonIgnore
  public Map<FlightStatus, Integer> getStatus() {
    return status;
  }

  public void setStatus(EnumMap<FlightStatus, Integer> status) {
    this.status = status;
  }

  @JsonIgnore
  public int getTotalRuns() {
    return totalRuns;
  }

  public void setTotalRuns(int totalRuns) {
    this.totalRuns = totalRuns;
  }

  @JsonIgnore
  public int getRecsSentToIndexQueue() {
    return recsSentToIndexQueue;
  }

  public void setRecsSentToIndexQueue(int recsSentToIndexQueue) {
    this.recsSentToIndexQueue = recsSentToIndexQueue;
  }

  @JsonIgnore
  public int getRecsSentToBulkProcessor() {
    return recsSentToBulkProcessor;
  }

  public void setRecsSentToBulkProcessor(int recsSentToBulkProcessor) {
    this.recsSentToBulkProcessor = recsSentToBulkProcessor;
  }

  @JsonIgnore
  public int getRowsNormalized() {
    return rowsNormalized;
  }

  public void setRowsNormalized(int rowsNormalized) {
    this.rowsNormalized = rowsNormalized;
  }

  @JsonIgnore
  public int getBulkPrepared() {
    return bulkPrepared;
  }

  public void setBulkPrepared(int recsBulkPrepared) {
    this.bulkPrepared = recsBulkPrepared;
  }

  @JsonIgnore
  public int getBulkDeleted() {
    return bulkDeleted;
  }

  public void setBulkDeleted(int recsBulkDeleted) {
    this.bulkDeleted = recsBulkDeleted;
  }

  @JsonIgnore
  public int getBulkBefore() {
    return bulkBefore;
  }

  public void setBulkBefore(int recsBulkBefore) {
    this.bulkBefore = recsBulkBefore;
  }

  @JsonIgnore
  public int getBulkAfter() {
    return bulkAfter;
  }

  public void setBulkAfter(int recsBulkAfter) {
    this.bulkAfter = recsBulkAfter;
  }

  @JsonIgnore
  public int getBulkError() {
    return bulkError;
  }

  public void setBulkError(int recsBulkError) {
    this.bulkError = recsBulkError;
  }

  @JsonIgnore
  public Date getFirstStart() {
    return NeutronDateUtils.freshDate(firstStart);
  }

  public void setFirstStart(Date firstStart) {
    this.firstStart = NeutronDateUtils.freshDate(firstStart);
  }

  @JsonIgnore
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

  public String toJson() throws JsonProcessingException {
    return JsonUtils.to(this);
  }

  @JsonIgnore
  public boolean isValidationErrors() {
    return validationErrors;
  }

  public void setValidationErrors(boolean validationErrors) {
    this.validationErrors = validationErrors;
  }

}
