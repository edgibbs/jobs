package gov.ca.cwds.neutron.util.shrinkray;

import static gov.ca.cwds.neutron.enums.NeutronDateTimeFormat.FMT_LEGACY_DATE;
import static gov.ca.cwds.neutron.enums.NeutronDateTimeFormat.FMT_LEGACY_TIMESTAMP;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;

/**
 * Date and timestamp utilities, mainly for DB2 and last change mode.
 * 
 * @author CWDS API Team
 */
public class NeutronDateUtils {

  private NeutronDateUtils() {
    // no-op
  }

  /**
   * Appease SonarQube's concern about "OMG! This gives away your implementation!"
   * 
   * @param incoming date to clone
   * @return fresh new Date -- to shut SonarQube up
   */
  public static Date freshDate(Date incoming) {
    return incoming != null ? new Date(incoming.getTime()) : null;
  }

  public static Date lookBack(final Date lastRunTime) {
    final Calendar cal = Calendar.getInstance();
    cal.setTime(lastRunTime);
    cal.add(Calendar.MINUTE, NeutronIntegerDefaults.LOOKBACK_MINUTES.getValue());
    return cal.getTime();
  }

  public static Date uncookTimestampString(String timestamp) {
    final String trimTimestamp = StringUtils.trim(timestamp);
    if (StringUtils.isNotEmpty(trimTimestamp)) {
      try {
        return new SimpleDateFormat(FMT_LEGACY_TIMESTAMP.getFormat()).parse(trimTimestamp);
      } catch (Exception e) {
        throw new NeutronRuntimeException(e);
      }
    }
    return null;
  }

  public static String makeTimestampString(final Date date) {
    final StringBuilder buf = new StringBuilder();
    buf.append("TIMESTAMP('")
        .append(new SimpleDateFormat(FMT_LEGACY_TIMESTAMP.getFormat()).format(date)).append("')");
    return buf.toString();
  }

  public static String makeSimpleTimestampString(final Date date) {
    Date useThisDate = date;

    if (date == null) {
      final Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MINUTE, 2); // in case server time doesn't match database
      useThisDate = cal.getTime();
    }

    return new SimpleDateFormat(FMT_LEGACY_TIMESTAMP.getFormat()).format(useThisDate);
  }

  public static String makeBasicTimestampString(final Date date) {
    return new SimpleDateFormat(FMT_LEGACY_TIMESTAMP.getFormat()).format(date);
  }

  public static String makeSimpleDateString(final Date date) {
    return new SimpleDateFormat(FMT_LEGACY_DATE.getFormat()).format(date);
  }

  public static String makeTimestampStringLookBack(final Date date) {
    String ret;
    final DateFormat fmt = new SimpleDateFormat(FMT_LEGACY_TIMESTAMP.getFormat());

    if (date != null) {
      ret = fmt.format(lookBack(date));
    } else {
      final Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MINUTE, NeutronIntegerDefaults.LOOKBACK_MINUTES.getValue());
      ret = fmt.format(lookBack(cal.getTime()));
    }

    return ret;
  }

}
