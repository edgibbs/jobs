package gov.ca.cwds.neutron.launch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;

public class NeutronRetryListener implements RetryListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronRetryListener.class);

  @Override
  public <V> void onRetry(Attempt<V> attempt) {
    LOGGER.info("attempt");
  }

}
