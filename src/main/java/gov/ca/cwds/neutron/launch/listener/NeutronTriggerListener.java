package gov.ca.cwds.neutron.launch.listener;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerKey;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import gov.ca.cwds.neutron.enums.NeutronSchedulerConstants;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.launch.LaunchDirector;
import gov.ca.cwds.neutron.launch.NeutronRocket;
import gov.ca.cwds.neutron.util.NeutronThreadUtils;

/**
 * Neutron implementation of Quartz TriggerListener.
 * 
 * @author CWDS API Team
 */
public class NeutronTriggerListener implements TriggerListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronTriggerListener.class);

  private static final String THREAD_NAME = "neutron_trigger_listener";

  private final LaunchDirector launchDirector;

  @Inject
  public NeutronTriggerListener(final LaunchDirector director) {
    this.launchDirector = director;
    NeutronThreadUtils.nameThread(THREAD_NAME, this);
  }

  @Override
  public String getName() {
    return THREAD_NAME;
  }

  @Override
  public void triggerFired(Trigger trigger, JobExecutionContext context) {
    final TriggerKey key = trigger.getKey();
    LOGGER.debug("Trigger fired: key: {}", key);
    launchDirector.getRocketsInFlight().put(key, (NeutronRocket) context.getJobInstance());
  }

  /**
   * Quartz Job instance type is {@link NeutronRocket}.
   */
  @Override
  public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
    final JobDataMap map = context.getJobDetail().getJobDataMap();
    final String className = map.getString(NeutronSchedulerConstants.ROCKET_CLASS);
    boolean answer = true;

    try {
      answer = launchDirector.isLaunchVetoed(className);
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "VETO LAUNCH! rocket: {}", className, e);
    }

    LOGGER.debug("Veto job execution: {}", answer);
    return answer;
  }

  @Override
  public void triggerMisfired(Trigger trigger) {
    final TriggerKey key = trigger.getKey();
    LOGGER.info("TRIGGER MISFIRED! key: {}", key);
    launchDirector.removeExecutingJob(key);
  }

  @Override
  public void triggerComplete(Trigger trigger, JobExecutionContext context,
      CompletedExecutionInstruction triggerInstructionCode) {
    final TriggerKey key = trigger.getKey();
    LOGGER.debug("Trigger complete: key: {}", key);
    launchDirector.removeExecutingJob(key);
  }

}
