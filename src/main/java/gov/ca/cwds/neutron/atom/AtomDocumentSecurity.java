package gov.ca.cwds.neutron.atom;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.neutron.flight.FlightPlan;

/**
 * Common security features for Elasticsearch indexing jobs, especially legacy CMS.
 * 
 * @author CWDS API Team
 */
public interface AtomDocumentSecurity extends ApiMarker {

  /**
   * Determine if limited access records must be deleted from ES.
   * 
   * @return True if limited access records must be deleted from ES, false otherwise.
   */
  default boolean mustDeleteLimitedAccessRecords() {
    return false;
  }

  /**
   * Determine whether to index a sealed or sensitive record.
   * 
   * @param opts options for current rocket
   * @param indicator CMS client sensitivity or case/referral limited access indicator
   * @return true if Rocket is not loading sealed/sensitive OR record is not restricted
   */
  static boolean isNotSealedSensitive(final FlightPlan opts, final String indicator) {
    return opts.isLoadSealedAndSensitive()
        || (indicator == null || "N".equalsIgnoreCase(indicator));
  }

}
