package gov.ca.cwds.neutron.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.flight.FlightPlan;

/**
 * Methods to manage threads and memory.
 * 
 * @author CWDS API Team
 */
public class NeutronThreadUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronThreadUtils.class);

  private NeutronThreadUtils() {
    // static methods only
  }

  /**
   * Calculate the number of reader threads to run from incoming rocket options and available
   * processors.
   * 
   * @param flightPlan flight options
   * @return number of reader threads to run
   */
  public static int calcReaderThreads(final FlightPlan flightPlan) {
    final int ret = flightPlan.getThreadCount() != 0L ? (int) flightPlan.getThreadCount()
        : Math.max(Runtime.getRuntime().availableProcessors() - 4, 4);
    LOGGER.info(">>>>>>>> # OF READER THREADS: {} <<<<<<<<", ret);
    return ret;
  }

  /**
   * Set the name of the current thread. Simplifies logging when a rocket runs multiple threads or
   * when multiple rockets are in flight.
   * 
   * @param title title of thread
   * @param obj calling object
   */
  public static void nameThread(final String title, final Object obj) {
    Thread.currentThread().setName(obj.getClass().getSimpleName() + "_" + title);
  }

  /**
   * Super lame but sometimes effective approach to thread management, especially when
   * thread/connection pools warm up or other resources initialize.
   * 
   * <p>
   * Better to use {@link CyclicBarrier}, {@link CountDownLatch}, {@link Phaser}, or even a raw
   * Condition.
   * </p>
   */
  public static void catchYourBreath() {
    try {
      Thread.sleep(NeutronIntegerDefaults.SLEEP_MILLIS.getValue()); // NOSONAR
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.trace("Interrupted", e); // appease SonarQube
    }
  }

  /**
   * Calculate memory usage for batch processing.
   * 
   * @return free memory in MB
   */
  public static long calcMemory() {
    final Runtime runtime = Runtime.getRuntime();
    return (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / 1024L;
  }

  /**
   * Log available memory, request garbage collection, then log memory again.
   */
  public static void freeMemory() {
    LOGGER.info("Free memory, before gc: {} MB", calcMemory());
    System.gc();
    LOGGER.info("Free memory, after  gc: {} MB", calcMemory());
  }

}
