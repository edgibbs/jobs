package gov.ca.cwds.neutron.util.shrinkray;

import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public final class NeutronStringUtils {

  private NeutronStringUtils() {
    // static methods only
  }

  public static Optional<String> filePath(String path) {
    return StringUtils.isNotEmpty(path)
        ? Optional.<String>of(Paths.get(path).getParent().toString())
        : Optional.<String>empty();
  }

}
