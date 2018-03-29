package gov.ca.cwds.data.es;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = SimpleSystemCodeSerializer.class)
public class SimpleElasticSearchSystemCode extends ElasticSearchSystemCode {

  private static final long serialVersionUID = 1L;

  public SimpleElasticSearchSystemCode() {
    super();
  }

  public SimpleElasticSearchSystemCode(String id, String description) {
    super(id, description);
  }

  public SimpleElasticSearchSystemCode(ElasticSearchSystemCode old) {
    super(old);
  }

}
