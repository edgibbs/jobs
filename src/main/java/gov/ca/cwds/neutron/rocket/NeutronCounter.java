package gov.ca.cwds.neutron.rocket;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.jetpack.CheeseRay;

/**
 * Java 8 lambda requires that referenced variables be "effectively final", including integer
 * counters for {@link CheeseRay#logEvery(org.slf4j.Logger, int, int, String, Object...)}, for which
 * this class is a facade.
 * 
 * @author CWDS API Team
 */
public final class NeutronCounter implements ApiMarker {

  private static final long serialVersionUID = 1L;

  private int counter = 0;

  public final int incrementAndGet() {
    ++counter;
    return get();
  }

  public final int get() {
    return counter;
  }

}
