package gov.ca.cwds.neutron.rocket;

import java.util.Queue;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;

import gov.ca.cwds.neutron.atom.AtomInitialLoad;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;

/**
 * Implementation of ForkJoinPool.ManagedBlocker. Blocks reader threads, until Elasticsearch
 * indexing queue size drops below the max threshold.
 * 
 * @author CWDS API Team
 * @see AtomInitialLoad#pullMultiThreadJdbc()
 */
public class NeutronManagedBlocker<T> implements ManagedBlocker {

  private static final ConditionalLogger LOGGER = new JetPackLogger(NeutronManagedBlocker.class);

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
    boolean ret = true;
    final String threadName = Thread.currentThread().getName();

    if (threadName.startsWith("extract_")) {
      final int size = queue.size();
      ret = size < maxQueueSizeBeforeBlocking;
      LOGGER.debug("isReleasable: threadName: {}, size: {}, block: {}", threadName, size, ret);
    }

    return ret;
  }

  public int getMaxSizeBeforeBlocking() {
    return maxQueueSizeBeforeBlocking;
  }

}
