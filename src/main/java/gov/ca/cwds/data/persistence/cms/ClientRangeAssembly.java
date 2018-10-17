package gov.ca.cwds.data.persistence.cms;

import java.util.List;

import gov.ca.cwds.neutron.rocket.ClientSQLResource;

/**
 * 
 * @author CWDS API Team
 * @see ClientSQLResource
 */
public interface ClientRangeAssembly {

  EsClientPerson matchOrMake(int clientId);

  List<EsClientPerson> read();

}
