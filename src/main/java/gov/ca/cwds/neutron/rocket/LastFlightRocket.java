package gov.ca.cwds.neutron.rocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import gov.ca.cwds.neutron.atom.AtomRocketControl;
import gov.ca.cwds.neutron.atom.AtomShared;
import gov.ca.cwds.neutron.component.Rocket;
import gov.ca.cwds.neutron.enums.NeutronDateTimeFormat;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;

/**
 * Abstract base class for all Neutron rockets that rely on a last <strong>successful</strong> run
 * time file.
 * 
 * @author CWDS API Team
 */
public abstract class LastFlightRocket implements Rocket, AtomShared, AtomRocketControl {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(LastFlightRocket.class);

  /**
   * Command line options for this rocket.
   */
  protected FlightPlan flightPlan;

  private String lastRunTimeFilename;

  /**
   * Construct from last successful run date-time.
   * 
   * @param lastGoodRunTimeFilename location of last run time file
   * @param flightPlan rocket options
   */
  public LastFlightRocket(String lastGoodRunTimeFilename, final FlightPlan flightPlan) {
    this.lastRunTimeFilename =
        StringUtils.isBlank(lastGoodRunTimeFilename) ? flightPlan.getLastRunLoc()
            : lastGoodRunTimeFilename;
    this.flightPlan = flightPlan;
  }

  /**
   * HACK for dependency injection issue. (Re-?) initialize the rocket.
   * 
   * @param lastGoodRunTimeFilename last run file location
   * @param flightPlan flight plan
   */
  public void init(String lastGoodRunTimeFilename, final FlightPlan flightPlan) {
    this.lastRunTimeFilename =
        StringUtils.isBlank(lastGoodRunTimeFilename) ? flightPlan.getLastRunLoc()
            : lastGoodRunTimeFilename;
    this.flightPlan = flightPlan;
  }

  @Override
  public final void run() {
    LOGGER.debug("last run time file: {}", lastRunTimeFilename);
    final FlightLog flightLog = getFlightLog();
    flightLog.start();

    try {
      final Date lastRunTime = determineLastSuccessfulRunTime();
      flightLog.setLastChangeSince(lastRunTime);
      writeLastSuccessfulRunTime(launch(lastRunTime));
    } catch (Exception e) {
      fail();
      LOGGER.error("FLIGHT ABORTED!", e);
    }

    try {
      finish(); // Close resources, notify listeners, or even close JVM in standalone mode.
    } catch (NeutronCheckedException e) {
      throw new NeutronRuntimeException("ABORT FLIGHT!", e);
    }

    // Sorry, SonarQube. SLF4J does not yet support conditional invocation.
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(flightLog.toString());
    }
  }

  /**
   * If last run time is provide in options then use it, otherwise use provided
   * lastSuccessfulRunTime.
   * 
   * <p>
   * NEXT: make the look-back period configurable.
   * </p>
   * 
   * @param lastSuccessfulRunTime last successful run
   * @param opts command line rocket options
   * @return appropriate date to detect changes
   */
  protected Date calcLastRunDate(final Date lastSuccessfulRunTime, final FlightPlan opts) {
    final Date lastSuccessfulRunTimeOverride = opts.getOverrideLastRunStartTime();
    final Calendar cal = Calendar.getInstance();
    cal.setTime((lastSuccessfulRunTimeOverride != null) ? lastSuccessfulRunTimeOverride
        : lastSuccessfulRunTime);
    cal.add(Calendar.MINUTE, NeutronIntegerDefaults.LOOKBACK_MINUTES.getValue());
    return cal.getTime();
  }

  /**
   * Calculate last successful run date/time, per
   * {@link LastFlightRocket#calcLastRunDate(Date, FlightPlan)}.
   * 
   * @param lastSuccessfulRunTime last successful run
   * @return appropriate date to detect changes
   */
  public Date calcLastRunDate(final Date lastSuccessfulRunTime) {
    return calcLastRunDate(lastSuccessfulRunTime, getFlightPlan());
  }

  /**
   * Reads the last run file and returns the last run date.
   * 
   * @return last successful run date/time as a Java Date.
   * @throws NeutronCheckedException I/O or parse error
   */
  public Date determineLastSuccessfulRunTime() throws NeutronCheckedException {
    Date ret = getFlightPlan().getOverrideLastRunStartTime();
    if (ret != null) {
      return ret;
    }

    try (BufferedReader br = new BufferedReader(new FileReader(lastRunTimeFilename))) { // NOSONAR
      ret = new SimpleDateFormat(NeutronDateTimeFormat.LAST_RUN_DATE_FORMAT.getFormat())
          .parse(br.readLine().trim()); // NOSONAR
    } catch (IOException | ParseException e) {
      fail();
      throw CheeseRay.checked(LOGGER, e, "ERROR READING LAST RUN TIME: {}", e.getMessage());
    }

    return ret;
  }

  /**
   * Write the last run timestamp <strong>IF</strong> the rocket's flight succeeded.
   * 
   * @param datetime date and time to store
   * @throws NeutronCheckedException I/O or parse error
   */
  public void writeLastSuccessfulRunTime(Date datetime) throws NeutronCheckedException {
    if (!isFailed()) {
      try (BufferedWriter w = new BufferedWriter(new FileWriter(lastRunTimeFilename))) { // NOSONAR
        w.write(NeutronDateTimeFormat.LAST_RUN_DATE_FORMAT.formatter().format(datetime));
      } catch (IOException e) {
        fail();
        throw CheeseRay.checked(LOGGER, e, "ERROR WRITING TIMESTAMP FILE: {}", e.getMessage());
      }
    }
  }

  /**
   * Launch the rocket. Child classes must provide an implementation.
   * 
   * @param lastSuccessfulRunTime The last successful run
   * @return The time of the latest run if successful.
   * @throws NeutronCheckedException if rocket fails
   */
  public abstract Date launch(Date lastSuccessfulRunTime) throws NeutronCheckedException;

  /**
   * Marks the rocket as completed. Close resources, notify listeners, or even close JVM.
   * 
   * @throws NeutronCheckedException rocket landing failure
   */
  protected abstract void finish() throws NeutronCheckedException;

  /**
   * Getter for last run time.
   * 
   * @return last time the rocket ran successfully, in format
   *         {@link NeutronDateTimeFormat#LAST_RUN_DATE_FORMAT}
   */
  public String getLastJobRunTimeFilename() {
    return lastRunTimeFilename;
  }

  /**
   * Getter for this rocket's flight plan.
   * 
   * @return this rocket's flight plan
   */
  @Override
  public FlightPlan getFlightPlan() {
    return flightPlan;
  }

  /**
   * Setter for this rocket's flight plan.
   * 
   * @param flightPlan this rocket's flight plan
   */
  public void setFlightPlan(FlightPlan flightPlan) {
    this.flightPlan = flightPlan;
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void done() {
    this.getFlightLog().done();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void fail() {
    this.getFlightLog().fail();
    this.getFlightLog().done();
    this.doneIndex();
    this.doneTransform();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doneRetrieve() {
    LOGGER.info("\n\t\t *********** RETRIEVAL DONE ***********\n");
    getFlightLog().doneRetrieve();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doneTransform() {
    getFlightLog().doneTransform();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doneIndex() {
    LOGGER.info("\n\t\t *********** INDEXING DONE ***********\n");
    getFlightLog().doneIndex();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRunning() {
    return getFlightLog().isRunning();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isFailed() {
    return getFlightLog().isFailed();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRetrieveDone() {
    return getFlightLog().isRetrieveDone();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isTransformDone() {
    return getFlightLog().isTransformDone();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isIndexDone() {
    return getFlightLog().isIndexDone();
  }

  public String getLastRunTimeFilename() {
    return lastRunTimeFilename;
  }

  public void setLastRunTimeFilename(String lastRunTimeFilename) {
    this.lastRunTimeFilename = lastRunTimeFilename;
  }

}
