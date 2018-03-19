package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.jdbc.Work;

import gov.ca.cwds.jobs.util.jdbc.WorkPrepareLastChange;
import gov.ca.cwds.neutron.atom.AtomInitialLoad;
import gov.ca.cwds.neutron.enums.NeutronDateTimeFormat;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils;
import gov.ca.cwds.neutron.util.transform.NeutronStreamUtils;

/**
 * JDBC utilities for Neutron rockets.
 * 
 * @author CWDS API Team
 */
public final class NeutronJdbcUtils {

  private static final ConditionalLogger LOGGER = new JetPackLogger(NeutronJdbcUtils.class);

  private static final String Z_OS_START = "aaaaaaaaaa";
  private static final String Z_OS_END = "9999999999";

  private static final String[] BASE_PARTITIONS = {"ACZAVXC2PZ", "A6y48OH3Ut", "BzaK4AmDfS",
      "B26CwOn9qX", "CvIYhuyCJt", "CY8r2uwEBI", "DsNAQ1W4l6", "DWQGOmFAzi", "EpKrO9a9xb",
      "ESH4wdQ02u", "Fmy2cMI0kW", "FN4JbQ0BFV", "GjekFiPEuJ", "GMSl4aH4wm", "HfvKXzA7rJ",
      "HJtGK9J07S", "IbMKNtL5Dk", "IEwHjr9FXO", "I6RlOno3qC", "JBHjcBU74E", "J5p3IOTEaC",
      "Kybpi693es", "K1KqHJ7AUR", "LuIBpq0Ftl", "LXN8vc87GE", "Mq5VGFd5Tk", "MUOYtgq2Mq",
      "NnZWsz1AzZ", "NRQqLqM5xF", "OjJmdJW5Dm", "ONwCecl5DQ", "Pf5jLGj9UX", "PI0rahL9yH",
      "Qd7xBSt3sQ", "QG0okqi0AR", "RaKSN5wCaR", "REwFaZn4iv", "R62gv73C4Z", "SzBubpb5sR",
      "S2pw19Q2Br", "Txj1LpTFgm", "T0eZIQpDjK", "0fXmBiY5Ch", "0JGoWelDYN", "1a6ExS95Ch",
      "1Fbwaaw3b4", "17PbXTH73o", "2AzE4ua8rX", "23rHzlZ30A", "3yq0BFP7Nw", "32qlKK2ICi",
      "4u0U0MECwr", "4YRF9Dd70O", "5rOfwNO3gC", "5T7PS2j37S", "6oipPRSKDX", "6R6kaia0SL",
      "7ki4MYoAzi", "7NYwtxJ7Lu", "8guC2hG4ak", "8JpJrxB37S", "9cRG3VmH6i", "9GwwRzY7D3", Z_OS_END};

  private NeutronJdbcUtils() {
    // Static utility class.
  }

  public static Date uncookTimestampString(String timestamp) {
    String trimTimestamp = StringUtils.trim(timestamp);
    if (StringUtils.isNotEmpty(trimTimestamp)) {
      try {
        return new SimpleDateFormat(NeutronDateTimeFormat.LEGACY_TIMESTAMP_FORMAT.getFormat())
            .parse(trimTimestamp);
      } catch (Exception e) {
        throw new NeutronRuntimeException(e);
      }
    }
    return null;
  }

  public static String makeTimestampString(final Date date) {
    final StringBuilder buf = new StringBuilder();
    buf.append("TIMESTAMP('")
        .append(new SimpleDateFormat(NeutronDateTimeFormat.LEGACY_TIMESTAMP_FORMAT.getFormat())
            .format(date))
        .append("')");
    return buf.toString();
  }

  public static String makeSimpleTimestampString(final Date date) {
    return new SimpleDateFormat(NeutronDateTimeFormat.LEGACY_TIMESTAMP_FORMAT.getFormat())
        .format(date);
  }

  public static String makeTimestampStringLookBack(final Date date) {
    return date != null
        ? new SimpleDateFormat(NeutronDateTimeFormat.LEGACY_TIMESTAMP_FORMAT.getFormat())
            .format(NeutronDateUtils.lookBack(date))
        : "CURRENT TIMESTAMP";
  }

  /**
   * @return default CMS schema name
   */
  public static String getDBSchemaName() {
    return System.getProperty("DB_CMS_SCHEMA");
  }

  public static Connection prepConnection(final SessionFactory sessionFactory) throws SQLException {
    final Connection con = sessionFactory.getSessionFactoryOptions().getServiceRegistry()
        .getService(ConnectionProvider.class).getConnection();
    con.setSchema(getDBSchemaName());
    con.setAutoCommit(false);
    NeutronDB2Utils.enableParallelism(con);
    return con;
  }

  public static void prepHibernateLastChange(final Session session, final Date lastRunTime,
      final String sql, final Function<Connection, PreparedStatement> func) {
    final Work work = new WorkPrepareLastChange(lastRunTime, sql, func);

    try {
      // May fail without a transaction.
      session.clear(); // Fixes Hibernate "duplicate object" bug
    } catch (Exception e) {
      LOGGER.warn("'clear' without transaction", e);
    }

    session.doWork(work);
    session.clear();
  }

  private static List<Pair<String, String>> buildPartitionsRanges(int partitions) {
    final int len = BASE_PARTITIONS.length;
    final int skip = len / partitions;
    LOGGER.info("len: {}, skip: {}", len, skip);

    final Integer[] positions =
        IntStream.rangeClosed(0, len - 1).boxed().flatMap(NeutronStreamUtils.everyNth(skip))
            .sorted().sequential().collect(Collectors.toList()).toArray(new Integer[0]);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(ToStringBuilder.reflectionToString(positions, ToStringStyle.MULTI_LINE_STYLE));
    }

    final List<Pair<String, String>> ret = new ArrayList<>();
    for (int i = 0; i < positions.length; i++) {
      ret.add(Pair.of(i > 0 ? BASE_PARTITIONS[positions[i - 1]] : Z_OS_START,
          i == positions.length - 1 ? Z_OS_END : BASE_PARTITIONS[positions[i]]));
    }

    return ret;
  }

  @SuppressWarnings("unchecked")
  public static List<Pair<String, String>> getCommonPartitionRanges(
      @SuppressWarnings("rawtypes") AtomInitialLoad initialLoad, int numPartitions)
      throws NeutronCheckedException {
    List<Pair<String, String>> ret = new ArrayList<>(numPartitions);
    if (initialLoad.isLargeDataSet()) { // Large data sets are only on z/OS.
      // ----------------------------
      // z/OS, large data set:
      // ORDER: a,z,A,Z,0,9
      // ----------------------------
      ret = initialLoad.limitRange(buildPartitionsRanges(numPartitions));
    } else if (initialLoad.isDB2OnZOS()) {
      // ----------------------------
      // z/OS, small data set:
      // ORDER: a,z,A,Z,0,9
      // ----------------------------
      ret.add(Pair.of(Z_OS_START, Z_OS_END));
    } else {
      // ----------------------------
      // Linux or small data set:
      // ORDER: 0,9,a,A,z,Z
      // ----------------------------
      ret.add(Pair.of("0000000000", "ZZZZZZZZZZ"));
    }

    return ret;
  }

  public static List<Pair<String, String>> getPartitionRanges4() {
    return buildPartitionsRanges(4);
  }

  public static List<Pair<String, String>> getPartitionRanges16() {
    return buildPartitionsRanges(16);
  }

  public static List<Pair<String, String>> getPartitionRanges64() {
    return buildPartitionsRanges(64);
  }

  public static List<Pair<String, String>> getCommonPartitionRanges4(
      @SuppressWarnings("rawtypes") AtomInitialLoad initialLoad) throws NeutronCheckedException {
    return getCommonPartitionRanges(initialLoad, 4);
  }

  public static List<Pair<String, String>> getCommonPartitionRanges16(
      @SuppressWarnings("rawtypes") AtomInitialLoad initialLoad) throws NeutronCheckedException {
    return getCommonPartitionRanges(initialLoad, 16);
  }

  public static List<Pair<String, String>> getCommonPartitionRanges64(
      @SuppressWarnings("rawtypes") AtomInitialLoad initialLoad) throws NeutronCheckedException {
    return getCommonPartitionRanges(initialLoad, 64);
  }

}
