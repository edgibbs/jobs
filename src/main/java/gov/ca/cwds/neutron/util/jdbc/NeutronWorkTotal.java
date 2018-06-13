package gov.ca.cwds.neutron.util.jdbc;

import org.hibernate.jdbc.Work;

/**
 * Expose the total number of records processed to Hibernate's Work interface.
 * 
 * @author CWDS API Team
 */
public interface NeutronWorkTotal extends Work {

  int getTotalProcessed();

}
