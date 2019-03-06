package gov.ca.cwds.neutron.flight;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newrelic.api.agent.NewRelic;

import gov.ca.cwds.neutron.atom.AtomMonitorNotifier;

public class NeutronNewRelicNotifier implements AtomMonitorNotifier {

  protected static final Logger LOGGER = LoggerFactory.getLogger(NeutronNewRelicNotifier.class);

  /**
   * Send flight metrics to New Relic (or other monitoring system).
   * 
   * <p>
   * <strong>Replication metrics (AR-325):</strong>
   * </p>
   * <ul>
   * <li>blue line: DB2 replication</li>
   * <li>green line: job processing time + ES refresh interval + delay between job runs.</li>
   * </ul>
   * 
   * Summary: blue line is replication, green line is everything else.
   * 
   * @param eventType registered New Relic event
   */
  @Override
  public void notifyMonitor(final FlightLog fl, final String eventType) {
    LOGGER.debug("Prepare to notify New Relic");
    final Map<String, Object> attribs = new LinkedHashMap<>();

    if (!fl.isInitialLoad()) {
      if (!fl.getTimings().isEmpty()) {
        // Convert long timestamp to UNIX timestamp (aka "seconds since epoch").
        fl.getTimings().entrySet().stream().forEach(e -> attribs.put(e.getKey(),
            Instant.ofEpochMilli(new Date(e.getValue()).getTime()).getEpochSecond()));
      }

      // SNAP-796: replication metrics.
      if (!fl.getOtherMetrics().isEmpty()) {
        fl.getOtherMetrics().entrySet().stream()
            .forEach(e -> attribs.put(e.getKey(), e.getValue()));
      }

      final Date lastChgSince = fl.getLastChangeSince();
      if (lastChgSince != null) {
        attribs.putIfAbsent("changed_since",
            Instant.ofEpochMilli(lastChgSince.getTime()).getEpochSecond());
      }

      attribs.putIfAbsent("warnings", fl.getWarnings().size());
      attribs.putIfAbsent("run_failed", fl.isFatalError());
      attribs.putIfAbsent("recs_pulled", fl.getRecsSentToIndexQueue().get());

      attribs.putIfAbsent("es_deleted", fl.getRecsBulkDeleted().get());
      attribs.putIfAbsent("es_indexed", fl.getRecsBulkBefore().get());
      attribs.putIfAbsent("es_errors", fl.getRecsBulkError().get());

      // NEXT: read index settings live
      final int refreshInterval =
          fl.getRecsSentToIndexQueue().get() > 0 ? FlightLog.getEsrefreshintervalsecs() : 0;
      attribs.putIfAbsent("es_refresh_interval", refreshInterval);

      attribs.putIfAbsent("run_start_time",
          Instant.ofEpochMilli(fl.getStartTime()).getEpochSecond());
      attribs.putIfAbsent("run_end_time", Instant.ofEpochMilli(fl.getEndTime()).getEpochSecond());

      final float runSeconds = (fl.getEndTime() - fl.getStartTime()) / 1000F;
      LOGGER.debug("Neutron: this run seconds: {}", runSeconds);
      attribs.putIfAbsent("run_seconds", runSeconds);

      final float runMillis = fl.getEndTime() - fl.getStartTime();
      attribs.putIfAbsent("run_millis", runMillis);

      if (fl.getLastEndTime() != 0) {
        attribs.putIfAbsent("last_run_end_time",
            Instant.ofEpochMilli(fl.getLastEndTime()).getEpochSecond());
        final float runTotalMillis = fl.getLastEndTime() - fl.getStartTime();
        final float runTotalSeconds = runTotalMillis / 1000F;

        LOGGER.debug("since last run: millis: {}, seconds: {}", runTotalMillis, runTotalSeconds);
        attribs.putIfAbsent("run_since_last_run_secs", runTotalSeconds);
        attribs.putIfAbsent("run_since_last_run_millis", runTotalMillis);

        final float totalGreenLineSecs = runTotalSeconds + refreshInterval;
        attribs.putIfAbsent("green_line_secs", totalGreenLineSecs);

        final float totalGreenLineMillis = runTotalMillis + (refreshInterval * 1000F);
        attribs.putIfAbsent("green_line_millis", totalGreenLineMillis);
      }

      if (!attribs.isEmpty()) {
        try {
          final StringBuilder buf = new StringBuilder();
          attribs.entrySet().stream().filter(e -> Objects.nonNull(e.getKey()))
              .sorted(Comparator.comparing(Map.Entry<String, Object>::getKey))
              .forEach(e -> this.append(buf, e));
          LOGGER.info("****** Notify New Relic ****** event: {}, attribs: {}\n{}\n", eventType,
              attribs.size(), buf.toString());

          // NEXT: inject this dependency.
          // Base FlightLog should NOT be tied to New Relic.
          NewRelic.getAgent().getInsights().recordCustomEvent(eventType, attribs);
        } catch (Exception e) {
          // Don't re-throw. Don't fail the run, because you can't send metrics to NR.
          final String msg = "FAILED TO SEND TO NEW RELIC!";
          LOGGER.error(msg, e);
          fl.addWarning(msg);
        }
      }
    }
  }

  protected void append(StringBuilder buf, Map.Entry<String, Object> e) {
    buf.append('\n').append('\t').append(StringUtils.rightPad(e.getKey(), 27)).append(": ")
        .append(e.getValue());
  }

}
