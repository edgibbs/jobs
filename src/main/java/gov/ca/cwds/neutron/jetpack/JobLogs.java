package gov.ca.cwds.neutron.jetpack;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;

/**
 * Neutron logging utilities.
 * 
 * @author CWDS API Team
 */
public class JobLogs {

  private static final ConditionalLogger LOGGER = new JetPackLogger(JobLogs.class);

  protected static final int DEFAULT_LOG_EVERY = 5000;

  protected JobLogs() {
    // Static methods only; do not instantiate.
    // Evil singleton, blah, blah, blah ... I can't hear you ...
  }

  /**
   * Log every N records.
   * 
   * @param log Logger
   * @param logEvery log every N records
   * @param cntr record count
   * @param action action message (extract, transform, load, etc)
   * @param args variable message arguments
   */
  public static void logEvery(Logger log, int logEvery, int cntr, String action, Object... args) {
    if (cntr > 0 && (cntr % logEvery) == 0) {
      log.info("{} {} {}", action, cntr, args);
    }
  }

  /**
   * Log every {@link #DEFAULT_LOG_EVERY} records.
   * 
   * @param log Logger
   * @param cntr record count
   * @param action action message (extract, transform, load, etc)
   * @param args variable message arguments
   */
  public static void logEvery(Logger log, int cntr, String action, Object... args) {
    logEvery(log, DEFAULT_LOG_EVERY, cntr, action, args);
  }

  /**
   * Log every {@link #DEFAULT_LOG_EVERY} records.
   * 
   * @param cntr record count
   * @param action action message (extract, transform, load, etc)
   * @param args variable message arguments
   */
  public static void logEvery(int cntr, String action, Object... args) {
    logEvery(LOGGER, cntr, action, args);
  }

  /**
   * Format message and return a runtime {@link NeutronRuntimeException}.
   * 
   * @param log class logger
   * @param e any Throwable
   * @param pattern MessageFormat pattern
   * @param args error message, excluding throwable message
   * @return JobsException runtime exception
   */
  public static NeutronRuntimeException buildRuntimeException(final Logger log, Throwable e,
      String pattern, Object... args) {
    NeutronRuntimeException ret;
    final boolean hasArgs = args == null || args.length == 0;
    final boolean hasPattern = !StringUtils.isEmpty(pattern);
    final Logger logger = log != null ? log : LOGGER;

    // Build message:
    final Object[] objs = hasArgs ? new Object[0] : args;
    final String pat = hasPattern ? pattern : StringUtils.join(objs, "{}");
    final String msg = hasPattern && hasArgs ? MessageFormat.format(pat, objs) : "";

    if (e != null) {
      logger.error(msg, e);
      ret = new NeutronRuntimeException(msg, e);
    } else {
      logger.error(msg);
      ret = new NeutronRuntimeException(msg);
    }

    return ret;
  }

  /**
   * Format message and return a runtime {@link NeutronRuntimeException}.
   * 
   * @param log class logger
   * @param e any Throwable
   * @param pattern MessageFormat pattern
   * @param args error message, excluding throwable message
   * @return NeutronException checked exception
   */
  public static NeutronCheckedException buildCheckedException(final Logger log, Throwable e,
      String pattern, Object... args) {
    final boolean hasArgs = args == null || args.length == 0;
    final boolean hasPattern = !StringUtils.isEmpty(pattern);
    final Logger logger = log != null ? log : LOGGER;

    // Build message:
    final Object[] objs = hasArgs ? new Object[0] : args;
    final String pat = hasPattern ? pattern : StringUtils.join(objs, "{}");
    final String msg = hasPattern && hasArgs ? MessageFormat.format(pat, objs) : "";

    logger.error(msg, e);
    return new NeutronCheckedException(msg, e);
  }

  public static NeutronCheckedException checked(final Logger log, Throwable e, String pattern,
      Object... args) {
    return buildCheckedException(log, e, pattern, args);
  }

  public static NeutronCheckedException checked(final Logger log, String pattern, Object... args) {
    return buildCheckedException(log, null, pattern, args);
  }

  public static NeutronRuntimeException runtime(final Logger log, Throwable e, String pattern,
      Object... args) {
    return buildRuntimeException(log, e, pattern, args);
  }

  public static NeutronRuntimeException runtime(final Logger log, String pattern, Object... args) {
    return buildRuntimeException(log, null, pattern, args);
  }

  public static String stackToString(Exception e) {
    return ExceptionUtils.getStackTrace(e);
  }

}
