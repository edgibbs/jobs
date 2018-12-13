package gov.ca.cwds.neutron.launch;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;

public class NeutronRetryListener implements RetryListener {

  @Override
  public <V> void onRetry(Attempt<V> attempt) {
    // TODO Auto-generated method stub

  }

}
