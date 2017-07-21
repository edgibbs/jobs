package gov.ca.cwds.jobs.util;

import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.ca.cwds.jobs.exception.JobsException;

/**
 * Logging utilities for Neutron job classes.
 * 
 * @author CWDS API Team
 */
public final class JobLogUtils {

  private static final Logger LOGGER = LogManager.getLogger(JobLogUtils.class);

  private static final int DEFAULT_LOG_EVERY = 5000;

  private JobLogUtils() {
    // No class instantiation.
  }

  /**
   * Log every {@link #DEFAULT_LOG_EVERY} records.
   * 
   * @param log Logger
   * @param cntr record count
   * @param action action message (extract, transform, load, etc)
   * @param args variable message arguments
   */
  public static void logEvery(Logger log, int cntr, String action, String... args) {
    if (cntr > 0 && (cntr % DEFAULT_LOG_EVERY) == 0) {
      log.info("{} {} {}", action, cntr, args);
    }
  }

  /**
   * Log every {@link #DEFAULT_LOG_EVERY} records.
   * 
   * @param cntr record count
   * @param action action message (extract, transform, load, etc)
   * @param args variable message arguments
   */
  public static void logEvery(int cntr, String action, String... args) {
    logEvery(LOGGER, cntr, action, args);
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
  public static void throwFatalError(final Logger log, Throwable e, String pattern,
      Object... args) {
    final String msg = MessageFormat.format(pattern, args);
    log.fatal(msg, e);
    throw new JobsException(msg, e);
  }

  /**
   * Format message and throw a runtime {@link JobsException}.
   * 
   * @param log class logger
   * @param e any Throwable
   * @param message error message, excluding throwable message
   * @throws JobsException runtime exception
   */
  public static void throwFatalError(final Logger log, Throwable e, String message) {
    final String msg = MessageFormat.format("ERROR: {}: MSG: {}", message, e.getMessage()); // NOSONAR
    log.fatal(msg, e);
    throw new JobsException(msg, e);
  }

}
