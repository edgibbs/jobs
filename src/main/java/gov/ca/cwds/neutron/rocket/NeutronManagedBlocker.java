package gov.ca.cwds.neutron.rocket;

import java.util.Queue;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;

import gov.ca.cwds.neutron.atom.AtomInitialLoad;

/**
 * Implementation of ForkJoinPool.ManagedBlocker. Blocks reader threads, until Elasticsearch
 * indexing queue size drops below the max threshold.
 * 
 * @author CWDS API Team
 * @see AtomInitialLoad#pullMultiThreadJdbc()
 */
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
    final boolean ours = Thread.currentThread().getName().startsWith("extract_");
    return !ours || (ours && queue.size() < maxQueueSizeBeforeBlocking);
  }

  public int getMaxSizeBeforeBlocking() {
    return maxQueueSizeBeforeBlocking;
  }

}
