package gov.ca.cwds.neutron.util.transform;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;

/**
 * Legacy descriptor extension adds {@code legacy_ui_id_flat} to facilitate match on 19 digit client
 * id by removing hyphens per SNAP-954.
 * 
 * @author CWDS API Team
 */
public class NeutronElasticSearchLegacyDescriptor extends ElasticSearchLegacyDescriptor {

  private static final long serialVersionUID = 1L;

  /**
   * Default, no-arg constructor.
   */
  public NeutronElasticSearchLegacyDescriptor() {
    super();
  }

  /**
   * Create from all fields.
   * 
   * @param legacyId Legacy ID
   * @param legacyUiId Legacy UI ID
   * @param legacyLastUpdated Legacy last updated time stamp
   * @param legacyTableName Legacy table name
   * @param legacyTableDescription Legacy table description
   */
  public NeutronElasticSearchLegacyDescriptor(String legacyId, String legacyUiId,
      String legacyLastUpdated, String legacyTableName, String legacyTableDescription) {
    super(legacyId, legacyUiId, legacyLastUpdated, legacyTableName, legacyTableDescription);
  }

  @JsonProperty("legacy_ui_id_flat")
  public String getLegacyUiIdFlat() {
    return StringUtils.isNotBlank(getLegacyUiId()) ? getLegacyUiId().trim().replaceAll("[^0-9]", "")
        : null;
  }

}
