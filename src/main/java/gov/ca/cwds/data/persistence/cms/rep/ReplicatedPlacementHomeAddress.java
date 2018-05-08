package gov.ca.cwds.data.persistence.cms.rep;

import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;

public class ReplicatedPlacementHomeAddress extends ReplicatedAddress {

  private static final long serialVersionUID = 1L;

  @Override
  public ElasticSearchLegacyDescriptor getLegacyDescriptor() {
    return ElasticTransformer.createLegacyDescriptor(getId(), getLastUpdatedTime(),
        LegacyTable.PLACEMENT_HOME);
  }

}
