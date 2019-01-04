package gov.ca.cwds.neutron.rocket;

import gov.ca.cwds.data.std.ApiMarker;

public final class NeutronCounter implements ApiMarker {

  private static final long serialVersionUID = 1L;

  private int counter = 0;

  public final int increment() {
    ++counter;
    return get();
  }

  public final int get() {
    return counter;
  }

}
