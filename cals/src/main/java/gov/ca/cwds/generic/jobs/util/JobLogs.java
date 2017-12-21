package gov.ca.cwds.generic.jobs.util;

import gov.ca.cwds.generic.jobs.exception.JobsException;
import gov.ca.cwds.generic.jobs.exception.NeutronException;
import java.text.MessageFormat;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logging utilities for Neutron job classes.
 * 
 * @author CWDS API Team
 */
public final class JobLogs {

  /**
   * Standard Logger.
   */
  protected static final Logger LOGGER = LoggerFactory.getLogger(JobLogs.class);

  private static final int DEFAULT_LOG_EVERY = 5000;

  private JobLogs() {
    // Static methods only, no class instantiation. Evil singleton, blah, blah, blah ... I can't
    // hear you!
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
   * Format message and return a runtime {@link JobsException}.
   * 
   * @param log class logger
   * @param e any Throwable
   * @param pattern MessageFormat pattern
   * @param args error message, excluding throwable message
   * @return JobsException runtime exception
   */
  public static JobsException buildException(final Logger log, Throwable e, String pattern,
      Object... args) {
    final boolean hasArgs = args == null || args.length == 0;
    final boolean hasPattern = !StringUtils.isEmpty(pattern);
    final Logger logger = log != null ? log : LOGGER;

    // Build message:
    final Object[] objs = hasArgs ? new Object[0] : args;
    final String pat = hasPattern ? pattern : StringUtils.join(objs, "{}");
    final String msg = hasPattern && hasArgs ? MessageFormat.format(pat, objs) : "";

    logger.error(msg, e);
    return new JobsException(msg, e);
  }

  /**
   * Format message and return a runtime {@link JobsException}.
   * 
   * @param log class logger
   * @param e any Throwable
   * @param pattern MessageFormat pattern
   * @param args error message, excluding throwable message
   * @return JobsException runtime exception
   */
  public static NeutronException buildCheckedException(final Logger log, Throwable e,
      String pattern, Object... args) {
    final boolean hasArgs = args == null || args.length == 0;
    final boolean hasPattern = !StringUtils.isEmpty(pattern);
    final Logger logger = log != null ? log : LOGGER;

    // Build message:
    final Object[] objs = hasArgs ? new Object[0] : args;
    final String pat = hasPattern ? pattern : StringUtils.join(objs, "{}");
    final String msg = hasPattern && hasArgs ? MessageFormat.format(pat, objs) : "";

    logger.error(msg, e);
    return new NeutronException(msg, e);
  }

  /**
   * Format message and throw a runtime {@link JobsException}.
   * 
   * @param log class logger
   * @param e any Throwable
   * @param pattern MessageFormat pattern
   * @param args error message, excluding throwable message
   * @throws JobsException runtime exception
   */
  public static void raiseError(final Logger log, Throwable e, String pattern, Object... args) {
    throw buildException(log, e, pattern, args);
  }

  /**
   * Format message and throw a runtime {@link JobsException}.
   * 
   * @param log class logger
   * @param e any Throwable
   * @param args error message or throwable message
   * @throws JobsException runtime exception
   */
  public static void raiseError(final Logger log, Throwable e, Object... args) {
    raiseError(log, e, null, args);
  }

}
