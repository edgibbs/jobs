package gov.ca.cwds.jobs.util.transform;

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
   * Trim a String.
   * 
   * @param value String to trim
   * @return trimmed String or null
   */
  public static final String ifNull(String value) {
    return value != null ? value.trim() : null;
  }

}
