package gov.ca.cwds.neutron.enums;

public enum NeutronColumn {

  /**
   * Common "after" column in "last change" queries.
   */
  SQL_COLUMN_AFTER("after");

  private final String value;

  private NeutronColumn(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
