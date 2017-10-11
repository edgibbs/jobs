package gov.ca.cwds.jobs.inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weakref.jmx.Managed;

import com.google.inject.tools.jmx.Manager;

import gov.ca.cwds.jobs.BasePersonIndexerJob;
import gov.ca.cwds.jobs.EducationProviderContactIndexerJob;
import gov.ca.cwds.jobs.config.JobOptions;
import gov.ca.cwds.jobs.exception.NeutronException;
import gov.ca.cwds.jobs.util.JobLogs;

/**
 * Run standalone jobs or serve up jobs in "continuous" mode.
 * 
 * @author CWDS API Team
 */
public class JobRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);

  /**
   * For unit tests where resources either may not close properly or where expensive resources
   * should be mocked.
   */
  private static boolean testMode = false;

  private static boolean continuousMode = false;

  private static JobOptions startingOpts;

  /**
   * Job options by job type.
   */
  private static final Map<Class<?>, JobOptions> jobOptions = new ConcurrentHashMap<>();

  private JobRunner() {
    // Default, no-op
  }

  /**
   * Entry point for standalone batch jobs, typically for initial load.
   * 
   * <p>
   * This method automatically closes the Hibernate session factory and ElasticSearch DAO and EXITs
   * the JVM.
   * </p>
   * 
   * @param klass batch job class
   * @param args command line arguments
   * @param <T> Person persistence type
   */
  public static <T extends BasePersonIndexerJob<?, ?>> void runStandalone(final Class<T> klass,
      String... args) {
    int exitCode = 0;
    JobRunner.continuousMode = false;

    try (final T job = JobsGuiceInjector.newJob(klass, args)) {
      job.run();
    } catch (Throwable e) { // NOSONAR
      // Intentionally catch a Throwable, not an Exception.
      // Close orphaned resources forcibly, if necessary, by system exit.
      exitCode = 1;
      throw JobLogs.buildException(LOGGER, e, "STANDALONE JOB FAILED!: {}", e.getMessage());
    } finally {
      // WARNING: kills the JVM in testing but may be needed to shutdown resources.
      if (!isTestMode() && !isContinuousMode()) {
        // Shutdown all remaining resources, even those not attached to this job.
        Runtime.getRuntime().exit(exitCode); // NOSONAR
      }
    }
  }

  /**
   * Register a continuously running job.
   * 
   * @param klass batch job class
   * @param args command line arguments
   * @param <T> Person persistence type
   * @throws NeutronException unexpected runtime error
   */
  public static <T extends BasePersonIndexerJob<?, ?>> void registerContinuousJob(
      final Class<T> klass, String... args) throws NeutronException {
    LOGGER.info("Register job: {}", klass.getName());
    try (final T job = JobsGuiceInjector.newJob(klass, args)) {
      jobOptions.put(klass, job.getOpts());
    } catch (Throwable e) { // NOSONAR
      // Intentionally catch a Throwable, not an Exception, for ClassNotFound or the like.
      throw JobLogs.buildCheckedException(LOGGER, e, "JOB REGISTRATION FAILED!: {}",
          e.getMessage());
    }
  }

  /**
   * Run a registered job.
   * 
   * @param klass batch job class
   * @param args command line arguments
   * @throws NeutronException unexpected runtime error
   */
  @Managed
  public static void runRegisteredJob(final Class<?> klass, String... args)
      throws NeutronException {
    try {
      LOGGER.info("Run registered job: {}", klass.getName());
      final JobOptions opts = args != null && args.length > 1 ? JobOptions.parseCommandLine(args)
          : jobOptions.get(klass);
      final BasePersonIndexerJob<?, ?> job =
          (BasePersonIndexerJob<?, ?>) JobsGuiceInjector.getInjector().getInstance(klass);
      job.setOpts(opts);
      job.run();
    } catch (Exception e) {
      throw JobLogs.buildCheckedException(LOGGER, e, "REGISTERED JOB RUN FAILED!: {}",
          e.getMessage());
    }
  }

  /**
   * Load all job definitions and continue running after a job completes.
   * 
   * @return true if running in continuous mode
   */
  public static boolean isContinuousMode() {
    return continuousMode;
  }

  /**
   * For unit tests where resources either may not close properly or where expensive resources
   * should be mocked.
   * 
   * @return whether in test mode
   */
  public static boolean isTestMode() {
    return testMode;
  }

  /**
   * For unit tests where resources either may not close properly or where expensive resources
   * should be mocked.
   * 
   * @param mode whether in test mode
   */
  public static void setTestMode(boolean mode) {
    testMode = mode;
  }

  protected static JobOptions getStartingOpts() {
    return startingOpts;
  }

  public static void main(String[] args) {
    LOGGER.info("START ON DEMAND JOBS");
    try {
      // OPTION: configure individual jobs, just like Rundeck.
      // Best to load a configuration file.
      JobRunner.startingOpts =
          args != null && args.length > 1 ? JobOptions.parseCommandLine(args) : getStartingOpts();
      JobRunner.continuousMode = true;
      JobRunner.registerContinuousJob(EducationProviderContactIndexerJob.class, args);
      Manager.manage("Neutron", JobsGuiceInjector.getInjector());
    } catch (Exception e) {
      LOGGER.error("FATAL ERROR! {}", e.getMessage(), e);
    }
  }

}
