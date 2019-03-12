package gov.ca.cwds.neutron.rocket;

import java.util.Queue;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;

public class NeutronManagedBlocker<T> implements ManagedBlocker {

  private final int maxQueueSizeBeforeBlocking;
  private Queue<T> queue;

  public NeutronManagedBlocker(Queue<T> queue, int maxSizeBeforeBlocking) {
    this.queue = queue;
    this.maxQueueSizeBeforeBlocking = maxSizeBeforeBlocking;
  }

  @Override
  public boolean block() throws InterruptedException {
    return isReleasable();
  }

  @Override
  public boolean isReleasable() {
    return queue.size() < maxQueueSizeBeforeBlocking;
  }

  public int getMaxSizeBeforeBlocking() {
    return maxQueueSizeBeforeBlocking;
  }

}
