package gov.ca.cwds.data.persistence.cms.rep;

import gov.ca.cwds.data.es.ElasticSearchSystemCode;
import gov.ca.cwds.data.es.SimpleElasticSearchSystemCode;

/**
 * Substitute {@link SimpleElasticSearchSystemCode} for {@link ElasticSearchSystemCode} to overcome
 * People mapping change to addresses.type.
 * 
 * @author CWDS API Team
 */
public class SimpleReplicatedClient extends ReplicatedClient {

  private static final long serialVersionUID = 1L;

  @Override
  protected ElasticSearchSystemCode makeJsonAddressType() {
    return new SimpleElasticSearchSystemCode();
  }

}
