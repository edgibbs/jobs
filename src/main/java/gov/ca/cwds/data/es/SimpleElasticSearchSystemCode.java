package gov.ca.cwds.data.es;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = SimpleSystemCodeSerializer.class)
public class SimpleElasticSearchSystemCode extends ElasticSearchSystemCode {

  private static final long serialVersionUID = 1L;

  public SimpleElasticSearchSystemCode() {
    super();
  }

  @JsonCreator
  public SimpleElasticSearchSystemCode(String description) {
    super();
    setDescription(description);
  }

  public SimpleElasticSearchSystemCode(String id, String description) {
    super();
    setId(id);
    setDescription(description);
  }

  /**
   * Copy constructor.
   * 
   * @param old object to copy from
   */
  public SimpleElasticSearchSystemCode(ElasticSearchSystemCode old) {
    super();
    setId(old.getId());
    setDescription(old.getDescription());
  }

}
