package gov.ca.cwds.neutron.jetpack;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Adds conditional logging to SLF4J. Supplied arguments are not invoked unless the log level
 * threshold is met. Reduces object churn by means of optional logging.
 * 
 * <p>
 * The name refers to Jimmy Neutron's iconic
 * <a href="http://jimmyneutron.wikia.com/wiki/Jet_Pack">jet pack</a>.
 * </p>
 * 
 * @author CWDS API Team
 * @see Logger
 */
public class JetPackLogger implements ConditionalLogger {

  /**
   * Delegate SLF4J Logger.
   */
  private Logger logger;

  public JetPackLogger(Class<?> klass) {
    this.logger = LoggerFactory.getLogger(klass);
  }

  /**
   * Convert lambda Supplier arguments to Object array for the SLF4J method signatures.
   * 
   * @param args
   * @return
   */
  @SuppressWarnings("unchecked")
  private Object[] collect(Supplier<Object>... args) {
    return Arrays.stream(args).map(Supplier::get).collect(Collectors.toList())
        .toArray(new Object[0]);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void trace(String format, Supplier<Object>... args) {
    if (isTraceEnabled()) {
      logger.trace(format, collect(args));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void debug(String format, Supplier<Object>... args) {
    if (isDebugEnabled()) {
      logger.debug(format, collect(args));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void info(String format, Supplier<Object>... args) {
    if (isInfoEnabled()) {
      logger.info(format, collect(args));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void warn(String format, Supplier<Object>... args) {
    if (isWarnEnabled()) {
      logger.warn(format, collect(args));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void error(String format, Supplier<Object>... args) {
    if (isErrorEnabled()) {
      logger.error(format, collect(args));
    }
  }

  @Override
  public String getName() {
    return logger.getName();
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  @Override
  public void trace(String msg) {
    logger.trace(msg);
  }

  @Override
  public void trace(String format, Object arg) {
    logger.trace(format, arg);
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    logger.trace(format, arg1, arg2);
  }

  @Override
  public void trace(String format, Object... arguments) {
    logger.trace(format, arguments);
  }

  @Override
  public void trace(String msg, Throwable t) {
    logger.trace(msg, t);
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return logger.isTraceEnabled(marker);
  }

  @Override
  public void trace(Marker marker, String msg) {
    logger.trace(marker, msg);
  }

  @Override
  public void trace(Marker marker, String format, Object arg) {
    logger.trace(marker, format, arg);
  }

  @Override
  public void trace(Marker marker, String format, Object arg1, Object arg2) {
    logger.trace(marker, format, arg1, arg2);
  }

  @Override
  public void trace(Marker marker, String format, Object... argArray) {
    logger.trace(marker, format, argArray);
  }

  @Override
  public void trace(Marker marker, String msg, Throwable t) {
    logger.trace(marker, msg, t);
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public void debug(String msg) {
    logger.debug(msg);
  }

  @Override
  public void debug(String format, Object arg) {
    logger.debug(format, arg);
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    logger.debug(format, arg1, arg2);
  }

  @Override
  public void debug(String format, Object... arguments) {
    logger.debug(format, arguments);
  }

  @Override
  public void debug(String msg, Throwable t) {
    logger.debug(msg, t);
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return logger.isDebugEnabled(marker);
  }

  @Override
  public void debug(Marker marker, String msg) {
    logger.debug(marker, msg);
  }

  @Override
  public void debug(Marker marker, String format, Object arg) {
    logger.debug(marker, format, arg);
  }

  @Override
  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    logger.debug(marker, format, arg1, arg2);
  }

  @Override
  public void debug(Marker marker, String format, Object... arguments) {
    logger.debug(marker, format, arguments);
  }

  @Override
  public void debug(Marker marker, String msg, Throwable t) {
    logger.debug(marker, msg, t);
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public void info(String msg) {
    logger.info(msg);
  }

  @Override
  public void info(String format, Object arg) {
    logger.info(format, arg);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    logger.info(format, arg1, arg2);
  }

  @Override
  public void info(String format, Object... arguments) {
    logger.info(format, arguments);
  }

  @Override
  public void info(String msg, Throwable t) {
    logger.info(msg, t);
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return logger.isInfoEnabled(marker);
  }

  @Override
  public void info(Marker marker, String msg) {
    logger.info(marker, msg);
  }

  @Override
  public void info(Marker marker, String format, Object arg) {
    logger.info(marker, format, arg);
  }

  @Override
  public void info(Marker marker, String format, Object arg1, Object arg2) {
    logger.info(marker, format, arg1, arg2);
  }

  @Override
  public void info(Marker marker, String format, Object... arguments) {
    logger.info(marker, format, arguments);
  }

  @Override
  public void info(Marker marker, String msg, Throwable t) {
    logger.info(marker, msg, t);
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  @Override
  public void warn(String msg) {
    logger.warn(msg);
  }

  @Override
  public void warn(String format, Object arg) {
    logger.warn(format, arg);
  }

  @Override
  public void warn(String format, Object... arguments) {
    logger.warn(format, arguments);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    logger.warn(format, arg1, arg2);
  }

  @Override
  public void warn(String msg, Throwable t) {
    logger.warn(msg, t);
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return logger.isWarnEnabled(marker);
  }

  @Override
  public void warn(Marker marker, String msg) {
    logger.warn(marker, msg);
  }

  @Override
  public void warn(Marker marker, String format, Object arg) {
    logger.warn(marker, format, arg);
  }

  @Override
  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    logger.warn(marker, format, arg1, arg2);
  }

  @Override
  public void warn(Marker marker, String format, Object... arguments) {
    logger.warn(marker, format, arguments);
  }

  @Override
  public void warn(Marker marker, String msg, Throwable t) {
    logger.warn(marker, msg, t);
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  @Override
  public void error(String msg) {
    logger.error(msg);
  }

  @Override
  public void error(String format, Object arg) {
    logger.error(format, arg);
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    logger.error(format, arg1, arg2);
  }

  @Override
  public void error(String format, Object... arguments) {
    logger.error(format, arguments);
  }

  @Override
  public void error(String msg, Throwable t) {
    logger.error(msg, t);
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return logger.isErrorEnabled(marker);
  }

  @Override
  public void error(Marker marker, String msg) {
    logger.error(marker, msg);
  }

  @Override
  public void error(Marker marker, String format, Object arg) {
    logger.error(marker, format, arg);
  }

  @Override
  public void error(Marker marker, String format, Object arg1, Object arg2) {
    logger.error(marker, format, arg1, arg2);
  }

  @Override
  public void error(Marker marker, String format, Object... arguments) {
    logger.error(marker, format, arguments);
  }

  @Override
  public void error(Marker marker, String msg, Throwable t) {
    logger.error(marker, msg, t);
  }

  public Logger getLogger() {
    return logger;
  }

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

}
