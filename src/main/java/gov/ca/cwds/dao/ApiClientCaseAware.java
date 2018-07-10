package gov.ca.cwds.dao;

/**
 * @author CWDS API Team
 */
public interface ApiClientCaseAware {

  /**
   * Get client's open case id
   * 
   * @return The open case id
   */
  String getOpenCaseId();

  /**
   * Get responsible agency code for open case.
   * 
   * @return Responsible agency code for open case.
   */
  String getOpenCaseResponsibleAgencyCode();

}
