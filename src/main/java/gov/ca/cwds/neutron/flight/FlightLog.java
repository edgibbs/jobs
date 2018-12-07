package gov.ca.cwds.neutron.flight;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.newrelic.api.agent.NewRelic;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.atom.AtomRocketControl;
import gov.ca.cwds.neutron.enums.FlightStatus;
import gov.ca.cwds.neutron.enums.NeutronDateTimeFormat;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils;
import gov.ca.cwds.rest.api.domain.DomainChef;
import gov.ca.cwds.utils.JsonUtils;
import io.dropwizard.jackson.JsonSnakeCase;

/**
 * Track rocket flight progress and record counts.
 * 
 * <p>
 * Class instances represent an individual rocket flight and are not intended for reuse. Hence, some
 * member variables are {@code final} or effectively non-modifiable.
 * </p>
 * 
 * @author CWDS API Team
 */
@JsonSnakeCase
public class FlightLog implements ApiMarker, AtomRocketControl {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(FlightLog.class);

  private static volatile boolean globalErrorFlag = false;

  /**
   * Runtime rocket name. Distinguish this rocket's threads from other running threads.
   */
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private String rocketName;

  /**
   * Completion flag for fatal errors.
   * <p>
   * Volatile guarantees that changes to this flag are immediately made visible to other threads. In
   * other words, threads don't cache a copy of this variable in their local memory for performance.
   * </p>
   */
  private volatile boolean fatalError = false;

  /**
   * Completion flag for data retrieval.
   */
  private volatile boolean doneRetrieve = false;

  /**
   * Completion flag for normalization/transformation.
   */
  private volatile boolean doneTransform = false;

  /**
   * Completion flag for document indexing.
   */
  private volatile boolean doneIndex = false;

  /**
   * Completion flag for whole rocket.
   */
  private volatile boolean doneFlight = false;

  /**
   * Flag any/all validation errors.
   */
  private volatile boolean validationErrors = false;

  /**
   * Official start time.
   */
  @JsonIgnore
  private long startTime = System.currentTimeMillis();

  /**
   * Official end time.
   */
  @JsonIgnore
  private long endTime;

  @JsonIgnore
  private long timeStartPoll;

  @JsonIgnore
  private long timeStartPull;

  @JsonIgnore
  private long timeEndPull;

  private final Map<String, Long> timings = new LinkedHashMap<>(31);

  private boolean initialLoad;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DomainChef.DATE_FORMAT)
  private Date lastChangeSince; // last change only

  private FlightStatus status = FlightStatus.NOT_STARTED;

  @JsonIgnore
  private final AtomicInteger recsSentToIndexQueue = new AtomicInteger(0);

  @JsonIgnore
  private final AtomicInteger recsSentToBulkProcessor = new AtomicInteger(0);

  @JsonIgnore
  private final AtomicInteger rowsNormalized = new AtomicInteger(0);

  @JsonIgnore
  private final AtomicInteger rowsDenormalized = new AtomicInteger(0);

  /**
   * Running count of records prepared for bulk indexing.
   */
  @JsonIgnore
  private final AtomicInteger recsBulkPrepared = new AtomicInteger(0);

  /**
   * Running count of records prepared for bulk deletion.
   */
  @JsonIgnore
  private final AtomicInteger recsBulkDeleted = new AtomicInteger(0);

  /**
   * Running count of records before bulk indexing.
   */
  @JsonIgnore
  private final AtomicInteger recsBulkBefore = new AtomicInteger(0);

  /**
   * Running count of records after bulk indexing.
   */
  @JsonIgnore
  private final AtomicInteger recsBulkAfter = new AtomicInteger(0);

  /**
   * Running count of errors during bulk indexing.
   */
  @JsonIgnore
  private final AtomicInteger recsBulkError = new AtomicInteger(0);

  @JsonIgnore
  private final List<String> warnings = Collections.synchronizedList(new ArrayList<>(512));

  /**
   * Initial load only.
   */
  private final Map<Pair<String, String>, FlightStatus> initialLoadRangeStatus =
      new ConcurrentHashMap<>();

  /**
   * Last change only. Log Elasticsearch documents created or modified by this rocket.
   */
  private final Queue<String> affectedDocumentIds = new CircularFifoQueue<>();

  private String failureCause;

  // =======================
  // CONSTRUCTORS:
  // =======================

  public FlightLog() {
    // default ctor
  }

  public FlightLog(String jobName) {
    this.rocketName = jobName;
  }

  // =======================
  // STATUS:
  // =======================

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRunning() {
    return !this.doneFlight && !this.fatalError;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isFailed() {
    return this.fatalError;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRetrieveDone() {
    return this.doneRetrieve;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isTransformDone() {
    return this.doneTransform;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isIndexDone() {
    return this.doneIndex;
  }

  // =======================
  // ACTIONS:
  // =======================

  public void start() {
    if (this.status == FlightStatus.NOT_STARTED) {
      this.status = FlightStatus.RUNNING;
      startTime = System.currentTimeMillis();
    }
  }

  public void fail(String reason) {
    if (StringUtils.isBlank(failureCause) && StringUtils.isNotBlank(reason)) {
      setFailureCause(reason);
    }
    fail();
  }

  @Override
  public void fail() {
    this.status = FlightStatus.FAILED;
    this.fatalError = true;

    if (initialLoad) {
      globalErrorFlag = true; // Don't swap index aliases!
    }

    done();
  }

  @Override
  public void done() {
    // Once failed, it cannot be rescinded.
    // NEXT: no longer true! You can run failed ranges a second time!
    if (this.status != FlightStatus.FAILED) {
      this.status = FlightStatus.SUCCEEDED;
    }

    // Done with ALL steps.
    this.endTime = System.currentTimeMillis();
    this.doneRetrieve = true;
    this.doneIndex = true;
    this.doneTransform = true;
    this.doneFlight = true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doneIndex() {
    this.doneIndex = true;
  }

  @Override
  public void doneRetrieve() {
    this.doneRetrieve = true;
  }

  @Override
  public void doneTransform() {
    this.doneTransform = true;
  }

  // =======================
  // PRETTY PRINT:
  // =======================

  private String pad(Integer padme) {
    return StringUtils.leftPad(new DecimalFormat("###,###,###").format(padme.intValue()), 8, ' ');
  }

  public String toJson() {
    try {
      return JsonUtils.to(this);
    } catch (JsonProcessingException e) {
      throw CheeseRay.runtime(LOGGER, e, "FAILED SERIALIZE TO JSON! rocket: {}", rocketName);
    }
  }

  // =======================
  // IDENTITY:
  // =======================

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false);
  }

  // =======================
  // ADDERS:
  // =======================

  public int addToQueuedToIndex(int addMe) {
    return this.recsSentToIndexQueue.getAndAdd(addMe);
  }

  public int addToNormalized(int addMe) {
    return this.rowsNormalized.getAndAdd(addMe);
  }

  public int addToDenormalized(int addMe) {
    return this.rowsDenormalized.getAndAdd(addMe);
  }

  public int addToBulkDeleted(int addMe) {
    return this.recsBulkDeleted.getAndAdd(addMe);
  }

  public int addToBulkPrepared(int addMe) {
    return this.recsBulkPrepared.getAndAdd(addMe);
  }

  public int addToBulkError(int addMe) {
    return this.recsBulkError.getAndAdd(addMe);
  }

  public int addToBulkAfter(int addMe) {
    return this.recsBulkAfter.getAndAdd(addMe);
  }

  public int addToBulkBefore(int addMe) {
    return this.recsBulkBefore.getAndAdd(addMe);
  }

  // =======================
  // LAST CHANGE TIMING:
  // =======================

  public void markStartChangePoll() {
    this.timeStartPoll = new Date().getTime();
  }

  public void markStartDataPoll() {
    this.timeStartPull = new Date().getTime();
  }

  public void markEndDataPoll() {
    this.timeEndPull = new Date().getTime();
  }

  // =======================
  // INCREMENT:
  // =======================

  public int markQueuedToIndex() {
    return this.recsSentToIndexQueue.incrementAndGet();
  }

  public int incrementNormalized() {
    return this.rowsNormalized.incrementAndGet();
  }

  public int incrementDenormalized() {
    return this.rowsDenormalized.incrementAndGet();
  }

  public int incrementBulkDeleted() {
    return this.recsBulkDeleted.incrementAndGet();
  }

  public int incrementBulkPrepared() {
    return this.recsBulkPrepared.incrementAndGet();
  }

  public int trackBulkError() {
    return this.recsBulkError.incrementAndGet();
  }

  // =======================
  // INITIAL LOAD RANGES:
  // =======================

  protected void setRangeStatus(final Pair<String, String> pair, final FlightStatus flightStatus) {
    initialLoadRangeStatus.put(pair, flightStatus);
  }

  public void markRangeStart(final Pair<String, String> pair) {
    setRangeStatus(pair, FlightStatus.RUNNING);
  }

  public void markRangeComplete(final Pair<String, String> pair) {
    setRangeStatus(pair, FlightStatus.SUCCEEDED);
  }

  public void markRangeSuccess(final Pair<String, String> pair) {
    setRangeStatus(pair, FlightStatus.SUCCEEDED);
  }

  public void markRangeError(final Pair<String, String> pair) {
    LOGGER.error("FAIL RANGE! {}", pair);
    setRangeStatus(pair, FlightStatus.FAILED);
  }

  // Java doesn't offer an IN operator like SQL.
  protected boolean filterStatus(FlightStatus actual, FlightStatus... scanFor) {
    boolean ret = false;

    for (FlightStatus state : scanFor) {
      if (actual == state) {
        ret = true;
        break;
      }
    }

    return ret;
  }

  public List<Pair<String, String>> filterRanges(FlightStatus... statuses) {
    return initialLoadRangeStatus.entrySet().stream()
        .filter(x -> filterStatus(x.getValue(), statuses)).map(x -> x.getKey())
        .collect(Collectors.toList());
  }

  public List<Pair<String, String>> getFailedRanges() {
    return filterRanges(FlightStatus.FAILED);
  }

  // =======================
  // ACCESSORS:
  // =======================

  public void addAffectedDocumentId(String docId) {
    affectedDocumentIds.add(docId);
  }

  protected List<Pair<String, String>> buildImmutableList(FlightStatus... statuses) {
    final TreeSet<Pair<String, String>> unique = new TreeSet<>();
    for (FlightStatus state : statuses) {
      unique.addAll(filterRanges(state));
    }

    final ImmutableList.Builder<Pair<String, String>> results = new ImmutableList.Builder<>();
    results.addAll(unique);
    return results.build();
  }

  public List<Pair<String, String>> getInitialLoadRangesStarted() {
    return buildImmutableList(FlightStatus.RUNNING);
  }

  public List<Pair<String, String>> getInitialLoadRangesCompleted() {
    return buildImmutableList(FlightStatus.RUNNING, FlightStatus.FAILED);
  }

  @JsonProperty("to_index_queue")
  public int getCurrentQueuedToIndex() {
    return this.recsSentToIndexQueue.get();
  }

  @JsonProperty("normalized")
  public int getCurrentNormalized() {
    return this.rowsNormalized.get();
  }

  @JsonProperty("denormalized")
  public int getCurrentDenormalized() {
    return this.rowsDenormalized.get();
  }

  @JsonProperty("bulk_deleted")
  public int getCurrentBulkDeleted() {
    return this.recsBulkDeleted.get();
  }

  @JsonProperty("bulk_prepared")
  public int getCurrentBulkPrepared() {
    return this.recsBulkPrepared.get();
  }

  @JsonProperty("bulk_error")
  public int getCurrentBulkError() {
    return this.recsBulkError.get();
  }

  @JsonProperty("bulk_after")
  public int getCurrentBulkAfter() {
    return this.recsBulkAfter.get();
  }

  @JsonProperty("warnings")
  public List<String> getWarnings() {
    return warnings;
  }

  public boolean isInitialLoad() {
    return initialLoad;
  }

  public void setInitialLoad(boolean initialLoad) {
    this.initialLoad = initialLoad;
  }

  public Date getLastChangeSince() {
    return NeutronDateUtils.freshDate(lastChangeSince);
  }

  public void setLastChangeSince(Date lastChangeSince) {
    this.lastChangeSince = NeutronDateUtils.freshDate(lastChangeSince);
  }

  public String getRocketName() {
    return rocketName;
  }

  public void setRocketName(String jobName) {
    this.rocketName = jobName;
  }

  @JsonIgnore
  public long getStartTime() {
    return startTime;
  }

  @JsonIgnore
  public long getEndTime() {
    return endTime;
  }

  @JsonProperty("start_time")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DomainChef.TIMESTAMP_ISO8601_FORMAT)
  public Date getStartTimeAsDate() {
    return startTime != 0 ? new Date(startTime) : null;
  }

  @JsonProperty("end_time")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DomainChef.TIMESTAMP_ISO8601_FORMAT)
  public Date getEndTimeAsDate() {
    return endTime != 0 ? new Date(endTime) : null;
  }

  public FlightStatus getStatus() {
    return status;
  }

  public String[] getAffectedDocumentIds() {
    return affectedDocumentIds.toArray(new String[0]);
  }

  public boolean isValidationErrors() {
    return validationErrors;
  }

  public void failValidation() {
    this.validationErrors = true;
  }

  public static boolean isGlobalError() {
    return globalErrorFlag;
  }

  /**
   * Format for JMX console and logs.
   */
  //@formatter:off
  @Override
  public String toString() {
    final DateFormat fmt = new SimpleDateFormat(NeutronDateTimeFormat.FMT_LEGACY_TIMESTAMP.getFormat());
    final StringBuilder buf = new StringBuilder();
    buf.append("\n[\n    FLIGHT STATUS: ").append(status).append(":\t").append(rocketName);

    if (initialLoad) {
      buf.append("\n\n    INITIAL LOAD:\n\tranges started:  ")
       // .append(pad(filterRanges(FlightStatus.SUCCEEDED,FlightStatus.FAILED,FlightStatus.RUNNING).size()))
          .append("\n\tranges completed:")
          .append(pad(filterRanges(FlightStatus.SUCCEEDED).size()))
       // .append("\n\tranges failed:")
       // .append(pad(filterRanges(FlightStatus.FAILED).size()))
          ;
    } else {
      buf.append("\n\n    LAST CHANGE:\n\tchanged since:          ").append(this.lastChangeSince)
      // .append("\n\tstart change polling:   ").append(new Date(timeStartPoll))
      // .append("\n\tstart pulling data:     ").append(new Date(timeStartPull))
      // .append("\n\tdone  pulling data:     ").append(new Date(timeEndPull))
         ;
    }

    if (!warnings.isEmpty()) {
      buf.append("\n\n  >>>>> WARNINGS:\n\t:          ").append(this.warnings.size());
    }

    if (!isInitialLoad() && !timings.isEmpty()) {
      buf.append("\n\n    STEPS:");
      timings.entrySet().stream().forEach(e -> 
        buf.append("\n\t")
           .append(StringUtils.rightPad(e.getKey() + ":", 24))
           .append(fmt.format(new Date(e.getValue()))));
    }

    buf.append("\n\n    RUN TIME:\n\tstart:                  ").append(fmt.format(new Date(startTime)));
    if (endTime > 0L) {
      buf.append("\n\tend:                    ").append(fmt.format(new Date(endTime)))
          .append("\n\ttotal seconds:          ").append((endTime - startTime) / 1000);
    }

    buf.append("\n\n    RECORDS RETRIEVED:").append("\n\tdenormalized:    ")
        .append(pad(recsSentToIndexQueue.get()))
        .append("\n\tnormalized:      ").append(pad(rowsNormalized.get()))
        .append("\n\tde-normalized:   ").append(pad(rowsDenormalized.get()))
        .append("\n\n    ELASTICSEARCH:")
        .append("\n\tto bulk:         ").append(pad(recsSentToBulkProcessor.get()))
        .append("\n\tbulk prepared:   ").append(pad(recsBulkPrepared.get()))
        .append("\n\tbulk deleted:    ").append(pad(recsBulkDeleted.get()))
        .append("\n\tbulk before:     ").append(pad(recsBulkBefore.get()))
        .append("\n\tbulk after:      ").append(pad(recsBulkAfter.get()))
        .append("\n\tbulk errors:     ").append(pad(recsBulkError.get()));

    if (!initialLoad && !affectedDocumentIds.isEmpty()) {
      buf.append("\n\n    SAMPLE DOCUMENTS:").append("\n\tdocument id's:    ")
          .append(StringUtils.joinWith(",", (Object[]) getAffectedDocumentIds()));
    }

    buf.append("\n]");
    return buf.toString();
  }
  //@formatter:on

  public static boolean isGlobalErrorFlag() {
    return globalErrorFlag;
  }

  public boolean isFatalError() {
    return fatalError;
  }

  public boolean isDoneRetrieve() {
    return doneRetrieve;
  }

  public boolean isDoneTransform() {
    return doneTransform;
  }

  public boolean isDoneIndex() {
    return doneIndex;
  }

  public boolean isDoneFlight() {
    return doneFlight;
  }

  public AtomicInteger getRecsSentToIndexQueue() {
    return recsSentToIndexQueue;
  }

  public AtomicInteger getRecsSentToBulkProcessor() {
    return recsSentToBulkProcessor;
  }

  public AtomicInteger getRowsNormalized() {
    return rowsNormalized;
  }

  public AtomicInteger getRowsDenormalized() {
    return rowsDenormalized;
  }

  public AtomicInteger getRecsBulkPrepared() {
    return recsBulkPrepared;
  }

  public AtomicInteger getRecsBulkDeleted() {
    return recsBulkDeleted;
  }

  public AtomicInteger getRecsBulkBefore() {
    return recsBulkBefore;
  }

  public AtomicInteger getRecsBulkAfter() {
    return recsBulkAfter;
  }

  public AtomicInteger getRecsBulkError() {
    return recsBulkError;
  }

  public Map<Pair<String, String>, FlightStatus> getInitialLoadRangeStatus() {
    return initialLoadRangeStatus;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public String getFailureCause() {
    return failureCause;
  }

  public void setFailureCause(String failureCause) {
    this.failureCause = failureCause;
  }

  public long getTimeStartPoll() {
    return timeStartPoll;
  }

  public void setTimeStartPoll(long timeStartPoll) {
    this.timeStartPoll = timeStartPoll;
  }

  public long getTimeStartPull() {
    return timeStartPull;
  }

  public void setTimeStartPull(long timeStartPull) {
    this.timeStartPull = timeStartPull;
  }

  public long getTimeEndPull() {
    return timeEndPull;
  }

  public void setTimeEndPull(long timeEndPull) {
    this.timeEndPull = timeEndPull;
  }

  public void addWarning(String warning) {
    warnings.add(warning);
  }

  public void addTimingEvent(String event) {
    timings.putIfAbsent(event, System.currentTimeMillis());
  }

  public Map<String, Long> getTimings() {
    return timings;
  }

  public void notifyMonitor(String eventType) {
    LOGGER.info("Notify New Relic");
    final Map<String, Object> eventAttributes = new LinkedHashMap<>();

    if (!isInitialLoad()) {
      if (!timings.isEmpty()) {
        timings.entrySet().stream().forEach(e -> eventAttributes.put(e.getKey(),
            Instant.ofEpochMilli(new Date(e.getValue()).getTime()).getEpochSecond()));
      }
    }

    // "changed_since": 1544143034,
    // "warnings": 0,
    // "errors": 0,
    // "recs_pulled": 24,
    // "es_deleted": 0,
    // "es_before": 24,
    // "es_after": 24,
    // "es_errors": 0


    NewRelic.getAgent().getInsights().recordCustomEvent(eventType, eventAttributes);
  }

}
