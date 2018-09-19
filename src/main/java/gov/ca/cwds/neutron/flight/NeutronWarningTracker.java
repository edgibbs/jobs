package gov.ca.cwds.neutron.flight;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import gov.ca.cwds.data.std.ApiObjectIdentity;
import gov.ca.cwds.rest.api.Response;

/**
 * Track flight warnings.
 * 
 * @author CWDS API Team
 */
@SuppressWarnings({"squid:S2160"})
public class NeutronWarningTracker extends ApiObjectIdentity implements Response {

  private static final long serialVersionUID = 1L;

  private static final AtomicInteger sequence = new AtomicInteger(0);

  private final int id = sequence.incrementAndGet(); // unique id
  private final StackTraceElement[] stack = getStackTrace();
  private final long startTime = System.currentTimeMillis();
  private final long threadId = Thread.currentThread().getId();

  /**
   * Default constructor.
   */
  public NeutronWarningTracker() {
    // Dear SonarQube, whineth not.
  }

  public static StackTraceElement[] getStackTrace() {
    try {
      final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
      return Arrays.stream(stack, 0, stack.length - 1)
          .filter(e -> e.getClassName().startsWith("gov.ca.cwds")
              && !e.getClassName().startsWith("gov.ca.cwds.rest.filters")
              && !e.getClassName().contains("$$"))
          .collect(Collectors.toList()).toArray(new StackTraceElement[0]);
    } catch (Exception e) {
      throw e;
    }
  }

  public long getThreadId() {
    return threadId;
  }

  public long getStartTime() {
    return startTime;
  }

  public StackTraceElement[] getStack() {
    return stack.clone(); // Appease SonarQube
  }

  public int getId() {
    return id;
  }

}
