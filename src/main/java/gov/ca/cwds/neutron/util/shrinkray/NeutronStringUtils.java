package gov.ca.cwds.neutron.util.shrinkray;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public final class NeutronStringUtils {

  private NeutronStringUtils() {
    // static methods only
  }

  public static Optional<String> filePath(String path) {
    Optional<String> ret = Optional.<String>empty();
    if (StringUtils.isNotEmpty(path)) {
      final Path thePath = Paths.get(path);
      final Path parent = thePath.getParent();
      if (parent != null) {
        ret = Optional.<String>of(parent.toString());
      } else {
        ret = Optional.<String>of(thePath.toString());
      }
    }

    return ret;
  }

}
