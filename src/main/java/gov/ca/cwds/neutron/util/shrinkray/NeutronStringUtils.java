package gov.ca.cwds.neutron.util.shrinkray;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.ObjectMapperUtils;
import gov.ca.cwds.neutron.jetpack.CheeseRay;

/**
 * Dumping ground for common String utility methods. Can be safely moved to api-core.
 * 
 * @author CWDS API Team
 */
public class NeutronStringUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronStringUtils.class);

  public static final ObjectMapper MAPPER = ObjectMapperUtils.createObjectMapper();

  private NeutronStringUtils() {
    // static methods only
  }

  /**
   * Convert JSON string to Map.
   * 
   * @param json JSON string to convert
   * @return Map of String keys and Object values
   */
  public static Map<String, Object> jsonToMap(String json) {
    Map<String, Object> ret = null;
    try {
      ret = MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      CheeseRay.runtime(LOGGER, e, "ERROR STREAMING JSON TO MAP! {}: json: {}", e.getMessage(),
          json);
    }

    return ret;
  }

  public static Optional<String> filePath(String path) {
    Optional<String> ret = Optional.<String>empty();
    if (StringUtils.isNotEmpty(path)) {
      final Path thePath = Paths.get(path);
      final Path parent = thePath.getParent();
      ret = Optional.<String>of(parent != null ? parent.toString() : thePath.toString());
    }

    return ret;
  }

}
