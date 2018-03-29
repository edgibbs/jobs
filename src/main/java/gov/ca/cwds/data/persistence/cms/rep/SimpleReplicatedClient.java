package gov.ca.cwds.data.persistence.cms.rep;

import gov.ca.cwds.data.es.ElasticSearchSystemCode;
import gov.ca.cwds.data.es.SimpleElasticSearchSystemCode;

public class SimpleReplicatedClient extends ReplicatedClient {

  private static final long serialVersionUID = 1L;

  @Override
  protected ElasticSearchSystemCode makeJsonAddress() {
    return new SimpleElasticSearchSystemCode();
  };

}
