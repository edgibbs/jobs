package gov.ca.cwds.neutron.flight;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.atom.AtomRocketControl;
import gov.ca.cwds.neutron.enums.FlightStatus;
import gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils;

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
public class FlightLog implements ApiMarker, AtomRocketControl {

  private static final long serialVersionUID = 1L;

  /**
   * Runtime rocket name. Distinguish this rocket's threads from other running threads.
   */
  private String rocketName;

  /**
   * Completion flag for fatal errors.
   * <p>
   * Volatile guarantees that changes to this flag become visible other threads immediately. In
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
  private volatile boolean doneJob = false;

  /**
   * Flag any/all validation errors.
   */
  private volatile boolean validationErrors = false;

  /**
   * Official start time.
   */
  private long startTime = System.currentTimeMillis();

  /**
   * Official end time.
   */
  private long endTime;

  private boolean initialLoad;

  private Date lastChangeSince; // last change only

  private FlightStatus status = FlightStatus.NOT_STARTED;

  private final AtomicInteger recsSentToIndexQueue = new AtomicInteger(0);

  private final AtomicInteger recsSentToBulkProcessor = new AtomicInteger(0);

  private final AtomicInteger rowsNormalized = new AtomicInteger(0);

  /**
   * Running count of records prepared for bulk indexing.
   */
  private final AtomicInteger recsBulkPrepared = new AtomicInteger(0);

  /**
   * Running count of records prepared for bulk deletion.
   */
  private final AtomicInteger recsBulkDeleted = new AtomicInteger(0);

  /**
   * Running count of records before bulk indexing.
   */
  private final AtomicInteger recsBulkBefore = new AtomicInteger(0);

  /**
   * Running count of records after bulk indexing.
   */
  private final AtomicInteger recsBulkAfter = new AtomicInteger(0);

  /**
   * Running count of errors during bulk indexing.
   */
  private final AtomicInteger recsBulkError = new AtomicInteger(0);

  /**
   * Initial load only.
   */
  private final List<Pair<String, String>> initialLoadRangesStarted = new ArrayList<>();

  /**
   * Initial load only.
   */
  private final List<Pair<String, String>> initialLoadRangesCompleted = new ArrayList<>();

  /**
   * Last change only. Log ES documents created or modified by this rocket.
   */
  private final Queue<String> affectedDocumentIds = new CircularFifoQueue<>();

  public FlightLog() {
    // default ctor
  }

  public FlightLog(String jobName) {
    this.rocketName = jobName;
  }

  // =======================
  // AtomJobControl:
  // =======================

  public void start() {
    if (this.status == FlightStatus.NOT_STARTED) {
      this.status = FlightStatus.RUNNING;
      startTime = System.currentTimeMillis();
    }
  }

  @Override
  public void fail() {
    if (this.status != FlightStatus.FAILED) {
      this.status = FlightStatus.FAILED;
      this.endTime = System.currentTimeMillis();

      this.fatalError = true;
      this.doneJob = true;
    }
  }

  @Override
  public void done() {
    if (this.status != FlightStatus.FAILED) {
      this.status = FlightStatus.SUCCEEDED;
    }

    this.endTime = System.currentTimeMillis();
    this.doneRetrieve = true;
    this.doneIndex = true;
    this.doneTransform = true;
    this.doneJob = true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRunning() {
    return !this.doneJob && !this.fatalError;
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
  // PRINT:
  // =======================

  private String pad(Integer padme) {
    return StringUtils.leftPad(new DecimalFormat("###,###,###").format(padme.intValue()), 8, ' ');
  }

  /**
   * Format for JMX console.
   */
  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder();
    buf.append("\n[\n    JOB STATUS: ").append(status).append(":\t").append(rocketName);

    if (initialLoad) {
      buf.append("\n\n    INITIAL LOAD:\n\tranges started:  ")
          .append(pad(initialLoadRangesStarted.size())).append("\n\tranges completed:")
          .append(pad(initialLoadRangesCompleted.size()));
    } else {
      buf.append("\n\n    LAST CHANGE:\n\tchanged since:          ").append(this.lastChangeSince);
    }

    buf.append("\n\n    RUN TIME:\n\tstart:                  ").append(new Date(startTime));
    if (endTime > 0L) {
      buf.append("\n\tend:                    ").append(new Date(endTime))
          .append("\n\ttotal seconds:          ").append((endTime - startTime) / 1000);
    }

    buf.append("\n\n    RECORDS RETRIEVED:").append("\n\tdenormalized:    ")
        .append(pad(recsSentToIndexQueue.get())).append("\n\tnormalized:      ")
        .append(pad(rowsNormalized.get())).append("\n\n    ELASTICSEARCH:")
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
  // ACCESSORS:
  // =======================

  public List<Pair<String, String>> getInitialLoadRangesStarted() {
    final ImmutableList.Builder<Pair<String, String>> results = new ImmutableList.Builder<>();
    results.addAll(initialLoadRangesStarted);
    return results.build();
  }

  public List<Pair<String, String>> getInitialLoadRangesCompleted() {
    final ImmutableList.Builder<Pair<String, String>> results = new ImmutableList.Builder<>();
    results.addAll(initialLoadRangesCompleted);
    return results.build();
  }

  public int getCurrentQueuedToIndex() {
    return this.recsSentToIndexQueue.get();
  }

  public int getCurrentNormalized() {
    return this.rowsNormalized.get();
  }

  public int getCurrentBulkDeleted() {
    return this.recsBulkDeleted.get();
  }

  public int getCurrentBulkPrepared() {
    return this.recsBulkPrepared.get();
  }

  public int getCurrentBulkError() {
    return this.recsBulkError.get();
  }

  public int getCurrentBulkAfter() {
    return this.recsBulkAfter.get();
  }

  public int addToQueuedToIndex(int addMe) {
    return this.recsSentToIndexQueue.getAndAdd(addMe);
  }

  public int addToNormalized(int addMe) {
    return this.rowsNormalized.getAndAdd(addMe);
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

  public int markQueuedToIndex() {
    return this.recsSentToIndexQueue.incrementAndGet();
  }

  public int incrementNormalized() {
    return this.rowsNormalized.incrementAndGet();
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

  public void markRangeStart(final Pair<String, String> pair) {
    initialLoadRangesStarted.add(pair);
  }

  public void markRangeComplete(final Pair<String, String> pair) {
    initialLoadRangesCompleted.add(pair);
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

  public void addAffectedDocumentId(String docId) {
    affectedDocumentIds.add(docId);
  }

  public String getRocketName() {
    return rocketName;
  }

  public void setRocketName(String jobName) {
    this.rocketName = jobName;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
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

}
