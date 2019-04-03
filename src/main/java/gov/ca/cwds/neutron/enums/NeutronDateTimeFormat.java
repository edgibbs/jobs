package gov.ca.cwds.neutron.enums;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Common formats for date and timestamp.
 * 
 * @author CWDS API Team
 */
public enum NeutronDateTimeFormat {

  /**
   * Date time format for last run date file.
   */
  FMT_LAST_RUN_DATE("yyyy-MM-dd HH:mm:ss"),

  /**
   * Date format for legacy DB2 on z/OS (mainframe).
   */
  FMT_LEGACY_DATE("yyyy-MM-dd"),

  /**
   * Timestamp format for legacy DB2 on z/OS (mainframe).
   */
  FMT_LEGACY_TIMESTAMP("yyyy-MM-dd HH:mm:ss.SSS");

  private final String format;

  private NeutronDateTimeFormat(String format) {
    this.format = format;
  }

  public String getFormat() {
    return format;
  }

  public DateFormat formatter() {
    return new SimpleDateFormat(format);
  }

}
