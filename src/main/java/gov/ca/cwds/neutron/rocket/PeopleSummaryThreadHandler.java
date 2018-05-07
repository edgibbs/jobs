package gov.ca.cwds.neutron.rocket;

import java.util.HashMap;
import java.util.Map;

import gov.ca.cwds.data.persistence.cms.EsClientPerson;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.jobs.ClientPersonIndexerJob;

public class PeopleSummaryThreadHandler implements ApiMarker {

  private static final long serialVersionUID = 1L;

  private final Map<String, EsClientPerson> denormalized;

  private final Map<String, ReplicatedClient> normalized;

  public PeopleSummaryThreadHandler(ClientPersonIndexerJob rocket) {
    final boolean isLargeLoad = rocket.isLargeLoad();
    this.denormalized = isLargeLoad ? new HashMap<>(40000) : new HashMap<>(10000);
    this.normalized = isLargeLoad ? new HashMap<>(20000) : new HashMap<>(5000);
  }

  public void clear() {
    this.normalized.clear();
    this.denormalized.clear();
  }

}
