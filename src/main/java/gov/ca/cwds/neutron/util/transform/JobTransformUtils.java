package gov.ca.cwds.neutron.util.transform;

import org.apache.commons.lang3.StringUtils;

/**
 * Common transformation methods.
 * 
 * @author CWDS API Team
 */
public final class JobTransformUtils {

  private JobTransformUtils() {
    // Default, no-op.
  }

  /**
   * Trim a String to null.
   * 
   * @param value String to trim
   * @return trimmed String or null
   */
  public static final String ifNull(final String value) {
    return StringUtils.trimToNull(value);
  }

}
