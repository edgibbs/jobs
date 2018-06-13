package gov.ca.cwds.neutron.util.jdbc;

import org.hibernate.jdbc.Work;

public interface NeutronWorkTotal extends Work {

  int getTotalProcessed();

}
