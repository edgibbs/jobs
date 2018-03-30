package gov.ca.cwds.data.es;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * JSON serializer streams an {@link ElasticSearchSystemCode} as a simple String (description field)
 * instead of a JSON object with fields "type" and "description."
 * 
 * @author CWDS API Team
 */
public class SimpleSystemCodeSerializer extends JsonSerializer<ElasticSearchSystemCode> {

  @Override
  public void serialize(ElasticSearchSystemCode sysCode, JsonGenerator generator,
      SerializerProvider serializers) throws IOException {
    generator.writeString(sysCode.getDescription());
  }

}
