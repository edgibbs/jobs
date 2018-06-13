package gov.ca.cwds.neutron.rocket;

import gov.ca.cwds.jobs.ClientPersonIndexerJob;

public class PeopleSummaryLastChangeHandler extends PeopleSummaryThreadHandler {

  private static final long serialVersionUID = 1L;

  public PeopleSummaryLastChangeHandler(ClientPersonIndexerJob rocket) {
    super(rocket);
  }

}
