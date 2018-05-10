package gov.ca.cwds.neutron.launch.listener;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeutronJobListener implements JobListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronJobListener.class);

  @Override
  public String getName() {
    return "neutron_job_listener";
  }

  @Override
  public void jobToBeExecuted(JobExecutionContext context) {
    LOGGER.debug("Quartz job to be executed");
  }

  @Override
  public void jobExecutionVetoed(JobExecutionContext context) {
    LOGGER.debug("Quartz job execution vetoed");
  }

  @Override
  public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
    LOGGER.debug("Quartz job was executed");
  }

}
