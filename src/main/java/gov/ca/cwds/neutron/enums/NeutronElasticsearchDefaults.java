package gov.ca.cwds.neutron.enums;

/**
 * Common settings and mapping files for Elasticsearch indexes.
 * 
 * @author CWDS API Team
 */
public enum NeutronElasticsearchDefaults {

  /**
   * People index settings.
   */
  SETTINGS_PEOPLE("/neutron/elasticsearch/setting/people-index-settings.json"),

  /**
   * People Summary index settings.
   */
  SETTINGS_PEOPLE_SUMMARY("/neutron/elasticsearch/setting/people-summary-index-settings.json"),

  /**
   * Legacy People index mapping, Snapshot 0.9.
   */
  MAPPING_PEOPLE_SNAPSHOT_1_0("/neutron/elasticsearch/mapping/map_people_snapshot_0.9.json"),

  /**
   * People index, Person document mapping.
   */
  MAPPING_PEOPLE_SNAPSHOT_1_1("/neutron/elasticsearch/mapping/map_people_snapshot_1.1.json"),

  /**
   * People Summary index, person-summary document mapping.
   */
  MAPPING_PEOPLE_SUMMARY("/neutron/elasticsearch/mapping/map_person_summary.json")

  ;

  private final String value;

  private NeutronElasticsearchDefaults(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
