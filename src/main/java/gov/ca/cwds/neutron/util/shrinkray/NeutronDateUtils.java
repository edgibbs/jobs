package gov.ca.cwds.neutron.util.shrinkray;

import static gov.ca.cwds.neutron.enums.NeutronDateTimeFormat.LEGACY_DATE_FORMAT;
import static gov.ca.cwds.neutron.enums.NeutronDateTimeFormat.LEGACY_TIMESTAMP_FORMAT;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;

/**
 * Date and timestamp utilities, mainly for DB2.
 * 
 * @author CWDS API Team
 */
public class NeutronDateUtils {

  private NeutronDateUtils() {
    // no-op
  }

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
    String trimTimestamp = StringUtils.trim(timestamp);
    if (StringUtils.isNotEmpty(trimTimestamp)) {
      try {
        return new SimpleDateFormat(LEGACY_TIMESTAMP_FORMAT.getFormat()).parse(trimTimestamp);
      } catch (Exception e) {
        throw new NeutronRuntimeException(e);
      }
    }
    return null;
  }

  public static String makeTimestampString(final Date date) {
    final StringBuilder buf = new StringBuilder();
    buf.append("TIMESTAMP('")
        .append(new SimpleDateFormat(LEGACY_TIMESTAMP_FORMAT.getFormat()).format(date))
        .append("')");
    return buf.toString();
  }

  public static String makeSimpleTimestampString(final Date date) {
    return new SimpleDateFormat(LEGACY_TIMESTAMP_FORMAT.getFormat())
        .format(date != null ? date : new Date());
  }

  public static String makeSimpleDateString(final Date date) {
    return new SimpleDateFormat(LEGACY_DATE_FORMAT.getFormat()).format(date);
  }

  public static String makeTimestampStringLookBack(final Date date) {
    String ret;
    final DateFormat fmt = new SimpleDateFormat(LEGACY_TIMESTAMP_FORMAT.getFormat());

    if (date != null) {
      ret = fmt.format(lookBack(date));
    } else {
      final Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MINUTE, 5);
      ret = fmt.format(lookBack(cal.getTime()));
    }

    return ret;
  }

}
