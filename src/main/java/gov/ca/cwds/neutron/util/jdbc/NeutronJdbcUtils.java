package gov.ca.cwds.neutron.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.jdbc.Work;
import org.hibernate.query.Query;

import gov.ca.cwds.neutron.atom.AtomInitialLoad;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
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

  private static final String[] BASE_PARTITIONS = {"AeRTLuZ6WW", "A6y48OH3Ut", "BzaK4AmDfS",
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

  private static final String[] EXTENDED_PARTITIONS = {"AeRTLuZ6WW", "Ahg03Ap3Ez", "AkEfSN73RZ",
      "An1tg0144S", "AroiT1Z7OF", "Av2FphfBw8", "AzyEzYCFYI", "AC5wEdz9YH", "AG82rVX2KA",
      "AK6Qtf82LK", "AOv5WvzM5K", "ARUrOuS8Xz", "AVkHVOY99d", "AYNERiMCOB", "A24xxaN6bb",
      "A6zehUX30A", "A94Mn377OF", "BdzzZCiCFA", "Bg2L0ID36B", "Bkuv1Zh37S", "Bo6SRjV8fe",
      "BswDyXx2JO", "BvUTq041aS", "BzmM1zmDSq", "BDW936LBob", "BHoTLgPIOS", "BKROH44C6v",
      "BOjtO56EfU", "BRJfLaP6qq", "BWk3nfi9Jd", "BZMQ61bBdr", "B3hMQAuGrR", "B6MVtbh5Cn",
      "CafWrQn2JO", "CdL5Eva5DT", "CioqP495Df", "ClMLKLe5DQ", "CpagvOT42A", "CsyRiMr41S",
      "CvTwjeg2gm", "Cz5VkfFCPl", "CDze065BG3", "CG0WpVJ54S", "CKuVKfV7VE", "COv4vCuKQP",
      "CSzmGUM9A4", "CV482PyLVK", "CZAjOTrFpP", "C20XzVp36B", "C7Bq2HQJBs", "DccPcJU1F4",
      "DfEpVdR8mB", "Di4Wj6v41S", "DmsU72V0m9", "DpSvTfe1nH", "DtGXapjC1F", "DxR7cUj36B",
      "DBhwMgj72f", "DEJCr5REAJ", "DJfp0pv6d7", "DMSytwW7CO", "DQpEM5X4Zj", "DTV1rL3MFQ",
      "DXsbuc7MTJ", "D1jOxQ6C1F", "D4Jmet20kC", "D8bzVLe3oq", "EbCXMcz30A", "Ee2K01a5ir",
      "Eis8zLt01S", "ElTdWZm5Ch", "EqwFjZB4rh", "EtZ88RwLk6", "ExriE8qBp1", "EAPX2yC90Z",
      "EFwjdjIG0n", "EI1bZVr9YH", "EMyBeEv7G8", "EP2SJxy7LI", "ETxLKXh41S", "EYcWpzqALF",
      "E1D2lZUKjf", "E45a8bR07S", "E8vol4yGUs", "FbWzVe2BU5", "Ffl3Ee210S", "FjRX1VV0C5",
      "FnuKakr9yH", "FqWbcyrHab", "Fupkg5IC8p", "FxSPHNy9FG", "FBlYImw199", "FEJJ7FF5DQ",
      "FH891Mp2qR", "FLxK7PC6v3", "FOVtfv26eS", "FTAhSZaDNc", "FW1IQyo10S", "F0r3JH17ec",
      "F3TSSVV36B", "F8xpLY72P4", "GbXGxVAC96", "GgFqHXG2PY", "Gj9rGByC1a", "GnDIgJJ0o5",
      "Gq8SBwF9yb", "GuGkjXZ1re", "GzcYQDi7r0", "GCBnnfQJHy", "GFVwuoh3ZB", "GJKw6HR37S",
      "GNWhBII199", "GRnBMBB2Un", "GUSgRgYDNc", "GYi3Ty46bL", "G2UJCPcA3u", "G6gwZ7J37S",
      "G9Da4v2EaC", "Hc4PiBF37S", "HgzoEWi1VX", "Hj6hU175zM", "HnEj3qh3sQ", "Hshpyn15ES",
      "HvHLl0T34A", "Hy8GXXoNoW", "HCwsDpc6Nh", "HG7LWiwDSq", "HKwWVph01S", "HNYoxZTDZt",
      "HRpQKaP5b3", "HUMyZmv3iB", "HZseJCo2hA", "H2R80SxIwz", "H6dQOes9gO", "H9AO1II5VY",
      "Ic5y38w5Zq", "IgDjU9bKqL", "IlkKuJF8SW", "IoMD70SDeC", "IsdcyA6199", "IvDuMZX3Gq",
      "Iy3ojnl30A", "ICtOcLC9yg", "IFQ6Mw1Muo", "IJetDsq6uL", "IMBsSwc199", "IPZQ52I199",
      "IUEEph6DoH", "IX9CaLi10S", "I1FrP5oB3b", "I44ydQc7GE", "I9zhYOu63f", "Jc7Njrc0YK",
      "JhH1lcA10S", "Jk8qHY38Vu", "JozZM7Z7Dm", "Jr2j4pZ4tP", "JvsQf2H194", "Jz3iKk7DsV",
      "JDvU0yTA6L", "JGYOECY2Tb", "JKsQdOc10S", "JO3uUBJ9Bz", "JSrwfX37Jo", "JVRTkXGHxM",
      "JZgNCGN8kK", "J3szsFY7LI", "J7iM7p2C7A", "KaMbR9N5Dm", "KedCGjk8kG", "KhDHAM177R",
      "Kk5IEbp40S", "KowuLpR0RM", "Ks9EWVFBjJ", "KwC03SWGQn", "Kz7wjqPAS2", "KDAubZJ01S",
      "KIbXWiuCKo", "KLAUDL92PY", "KOZKF2Q70m", "KSlLUc0A5E", "KVIN1SF5oX", "K0jXVPF30A",
      "K3NOnLu2LL", "K7hKfREGv0", "LaMEWPj9d2", "LedJTG6BoD", "LhAbxbh2N3", "LkXpLGs199",
      "LpEXzFc6WR", "Ltay4RJ2OJ", "LwEA1SJBjD", "LAhWmnRB42", "LEiVigz37S", "LHGaNGe5oq",
      "LK4lnHAN0l", "LOqu9y11Lh", "LRPbBzd37S", "LWmHNQLOjd", "LZLTq1RIig", "L3hluEJDVd",
      "L6LTPTF34A", "MaLbLKKCtl", "Md9qe57Dxv", "MiHXxYyDWe", "Ml5HzT80AR", "MpywdRj1LU",
      "MsZclWoIE6", "MwpdDzsCtD", "MAY7jEl76g", "MEo5iX15om", "MHPZs2u2PY", "MLgkNSj41S",
      "MPPk3zK7NP", "MTiwQc7Jhr", "MWIyC27AVU", "MZ90FA93ZB", "M3VKCyv30A", "M8ksKIFBJI",
      "NcrwiDOH6i", "NfRmjem4jJ", "Njf7zjS2LR", "NmEgkUf1tm", "Np28oE8N2e", "NuEbE721zg",
      "Nx43Up741S", "NBuMMef37S", "NESo28v44S", "NJuJLr2EnF", "NMXz0kRCOp", "NQpxIQ7MHw",
      "NTOYvKL30A", "NXfkaGtCOl", "N1aXnNzFsf", "N4E0Drv7zE", "N77xAW37zn", "ObvTEsL2KA",
      "OeUe1BYJp6", "OihoNh70ZX", "OlFb9cZ2KR", "OqgXITDJnJ", "OtIcspo7l7", "Ow64Lno9j6",
      "OAy7tTw2UL", "OFgu8Y538A", "OIKpUxu6Fj", "OMf0nOD94v", "OPJeT0f6qq", "OTddn4E5Ch",
      "OXOvSp48iJ", "O1cQu0T7CO", "O4GUERPFqp", "O74Z6XMBXe", "PbvqRA3CKT", "PeSwVBH996",
      "Pjs6vRsMs2", "PmOFBGG2qR", "Pqa1LbV30A", "PtxgHxdKHD", "PwTHnQ937S", "PAI41Uc4zO",
      "PEdcSsT5Le", "PHJB7gg9ip", "PLddEt9DwH", "PPJDU0130A", "PTlSijVAGY", "PWMBedICfj",
      "PZ9Yt91BG3", "P3x7nHkCfI", "P8eQOXJ42A", "QcKo4vN9KP", "QglZ9HC5Zq", "QjPzeLf5Je",
      "Qnf4tBICKo", "QqGqEFvCzG", "QvkhJPY0RP", "QyJpek54Al", "QCcRqvy10S", "QFHKYK37PC",
      "QKmU1gY6fx", "QNOcKD2CL6", "QRblfib0C5", "QUBsrmv5Df", "QX3chxQCXj", "Q2gAOlVKJM",
      "Q5LgO0X36B", "Q9f3grSLHs", "RcH2Jjq6ob", "RgbILK22My", "RjFumkvH0A", "Rm8ThUS2Cp",
      "RrKNSgcDqZ", "Ru6hBIH37S", "Ryp1h9a1tm", "RBL6vOo2OJ", "RGpjvCK3Xv", "RJQzDbkBm3",
      "RNeyThDAcg", "RQFOMlAJVg", "RT6nuVG4tA", "RYKglwf36B", "R2bbweX07S", "R5xLuI9AXU",
      "R8VQhByIpJ", "ScoUlkMAcg", "SfUXMLz65e", "Skw47N0Czc", "SnW8wvK4ZD", "Srnoxhn0kC",
      "SuLEsJF43S", "SyaEAWj9Bm", "SBAb7TpG4f", "SEY7Bru5db", "SIqLGmyOQ6", "SLRE76t9mp",
      "SPLgTTJEh4", "ST0qAd743S", "SXvvb4j36B", "S0ZEAQm2UB", "S4nLAor43S", "S8VMx0vAcR",
      "TcQreQF2gm", "TgZ9mBM1II", "TkqqidZ5xC", "TnRXMwxCVp", "TrhWtkRCpS", "TuGXkED30A",
      "TzhNmKp4iv", "TCIY0uEB3q", "TF7AeQ4H1k", "TJI4aOg5ae", "TObNVGm201", "TRHwldN3dA",
      "TVeRtMlBp1", "TYJhKTr1Io", "T3jtsu130A", "T6FOqaw8Ml", "T94X97zA7N", "UDu93HkH6i",
      "U2TA06j36B", "U6jNZKd16W", "U9KNDLo3aN", "0erb44n43S", "0hWKaaP30A", "0ltW8BA3F7",
      "0o0xoZu6b3", "0sxEksQ9Vh", "0xaAkD6NVz", "0Azfjcn37S", "0D16JzUHyw", "0HTaqGo6Si",
      "0L4QB1j9Eo", "0Pw8OS9IE6", "0SYbE6z40S", "0Wsdhlk96D", "0ZTxUx85p7", "03kKRbY9LH",
      "06LEeVh8ZL", "1ahDZz730A", "1dPDHAmC4Q", "1hj2Ssz30A", "1kQbeioKie", "1ps9GwT01S",
      "1sVJSbd42E", "1wletOL9xL", "1zKVBUI4iV", "1EqIh2JFce", "1HSHaQj30A", "1LkceAG8kF",
      "1OK3tXV30A", "1SfYt1QNVz", "1WUtOZV01S", "10rfRNc83S", "13TQ8xWDpj", "17lAm2c199",
      "2aM4OW35Cm", "2ekKJ9d37S", "2irDJsf0Z6", "2mviyeYGqA", "2pWlePSA3u", "2tolFFx37S",
      "2wQbijJ40S", "2Agq40QDjK", "2DHCpfY8kK", "2G9KlHYC96", "2Kyu0tN1O7", "2NYMk2c4tA",
      "2SCqRmr37S", "2WcmFr3IGi", "2ZIkpBZ5Dm", "229d8Vt6eS", "27t5F9VCOQ", "3befB3Z01S",
      "3fRL431J5y", "3jiVDgeCrd", "3mK2pZi199", "3qcYbQ39mp", "3tFCGyoDoH", "3yoMonB36B",
      "3BPbtNh01S", "3Fd0k9o3Yq", "3IAovSf0jp", "3NgIhmtG9P", "3QLUaSf4nd", "3UjBBus76g",
      "3XOeJ9QCGB", "32wPvg11nH", "35YWcMr54S", "39nMdaMDNc", "4cNOzlT6TV", "4gkX3JhCjN",
      "4jNgRlN9JN", "4nfRSkr30A", "4rU7heABha", "4voFEdl38A", "4yTLvzF15A", "4CnCB074jJ",
      "4GY0qh736B", "4KoqEHPD7r", "4NNBQyzAR1", "4Rc3bKW7OF", "4UB5AUo5Dk", "4Zft6Bg3dA",
      "42HEDisLZ4", "457XKwz37S", "49A5eMX9x3", "5c1k9k57PC", "5gw1EdgDNc", "5lbH58xAzZ",
      "5oHtU2N2gd", "5r9LsbB4Ca", "5vGl7FCJEQ", "5zdwpQK0Yt", "5CCJu2mJfK", "5F1vtexBZ8",
      "5JpCL1f6eV", "5MNrmI4A6L", "5QB2C2L30A", "5UOsxk136B", "5YhMy26JPT", "51KQJMn33A",
      "55gLAVa8Kw", "59W9mBV25z", "6d0fftaAYx", "6h6EF0cGAN", "6lFPK9Y5Df", "6pdsylsCpt",
      "6sHtppe5DQ", "6w4wL1c7iy", "6AQ41Ez046", "6Ehi2ss195", "6HFuHdlFns", "6MilbeY5Df",
      "6PHRhLR6VR", "6S5VcZ4Fut", "6Wt0zIX43S", "6ZT5JQ55qq", "64zwCDD3Ev", "67XXLMH37S",
      "7bqFSs288W", "7eRroS930A", "7iibCQI2Ns", "7lHq4bG76n", "7paz06w7GH", "7tNQjgJ1Iu",
      "7xe11dJ5DO", "7AKuVOWEgm", "7EdPqf15Ch", "7IVoJHN059", "7Ml346549S", "7PNMP6s3Qu",
      "7TkfkOqDVO", "7WOEK5E8IJ", "71s3Coo3sQ", "74Wivds7vm", "78qgTwo10S", "8bQ3f3WBG3",
      "8fhEMf8H3N", "8iLsuYb07S", "8nmB0Bo6Cv", "8qOYzAT0V1", "8ugKRjI6VR", "8xJiwTFBng",
      "8BxrhPAAlQ", "8E1KN5dDmM", "8IxJMLi7HX", "8LYYtD26AD", "8PtZVB334A", "8T7vFGz30A",
      "8XwwqrSLFV", "80WpSRINaW", "84paKIjBVn", "87SrzXt36B", "9b9Xul5CSc", "9gGdrr0KCR",
      "9kaoR7L3Ro", "9nxSqm36WY", "9qZueu101S", "9upLmOFCOk", "9yrL47w5bY", "9CwWORh4Ez",
      "9F3Mjaa9us", "9Jzi20936B", "9Oe3WLlBng", "9RDHqEFFTz", "9U4SXT86gX", "9YvgJqz49S",
      "91W4KCkALI", "96C1s5R30A", "997Da6t5dE", Z_OS_END};

  private NeutronJdbcUtils() {
    // Static utility class.
  }

  /**
   * @return default CMS schema name
   */
  public static String getDBSchemaName() {
    return System.getProperty("DB_CMS_SCHEMA");
  }

  /**
   * Steal a connection from a Hibernate SessionFactory.
   * 
   * @param sessionFactory Hibernate SessionFactory
   * @return database Connection
   * @throws SQLException on database error
   * @deprecated prefer method {@link #prepConnection(Session)}
   * @see #prepConnection(Session)
   */
  @Deprecated
  public static Connection prepConnection(final SessionFactory sessionFactory) throws SQLException {
    final Connection con = sessionFactory.getSessionFactoryOptions().getServiceRegistry()
        .getService(ConnectionProvider.class).getConnection();
    NeutronDB2Utils.enableBatchSettings(con);
    return con;
  }

  /**
   * Steal a connection from an active Hibernate session.
   * 
   * @param session active Hibernate session
   * @return database Connection
   * @throws SQLException on database error
   */
  public static Connection prepConnection(final Session session) throws SQLException {
    final NeutronWorkConnectionStealer work = new NeutronWorkConnectionStealer();
    session.doWork(work);
    final Connection con = work.getConnection();
    NeutronDB2Utils.enableBatchSettings(con);
    return con;
  }

  public static int runStatementInsertLastChangeKeys(final Session session, final Date lastRunTime,
      final String sql, final Function<Connection, PreparedStatement> func) {
    final NeutronWorkTotalImpl work = new WorkPrepareLastChange(lastRunTime, sql, func);
    doWork(session, work);
    return work.getTotalProcessed();
  }

  public static int runStatementInsertRownumBundle(final Session session, final String sql,
      int start, int end, final Function<Connection, PreparedStatement> func) {
    final NeutronWorkTotalImpl work = new WorkPrepareRownumBundle(start, end, func);
    doWork(session, work);
    return work.getTotalProcessed();
  }

  /**
   * Prepare a statement through a Java Function to avoid vulnerability to SQL injection. Thanks a
   * lot, SonarQube. Boo! Hiss!
   * 
   * @param sql SQL statement to prepare
   * @return Java Function to execute the prepared statement
   */
  public static Function<Connection, PreparedStatement> getPreparedStatementMaker(String sql) {
    return c -> {
      try {
        LOGGER.info("PREPARE LAST CHANGE SQL:\n\n{}\n", sql);
        return c.prepareStatement(sql);
      } catch (SQLException e) {
        throw CheeseRay.runtime(LOGGER, e, "FAILED TO PREPARE STATEMENT! SQL: {}", sql);
      }
    };
  }

  /**
   * Clear a Hibernate session and trap transaction errors.
   * 
   * @param session active Hibernate session
   */
  public static void clearSession(final Session session) {
    try {
      // Hibernate session clear may fail without a transaction.
      session.clear(); // Hibernate "duplicate object" bug
    } catch (Exception e) {
      LOGGER.warn("'clear' without transaction", e);
    }
  }

  /**
   * Generic method to execute a Hibernate Work implementation (arbitrary JDBC through Hibernate).
   * 
   * @param session active Hibernate session
   * @param work Hibernate Work instance
   */
  public static void doWork(final Session session, Work work) {
    clearSession(session);
    session.doWork(work);
    clearSession(session);
  }

  /**
   * Make a Hibernate query read-only.
   * 
   * @param q query to make read-only
   * @see #optimizeQuery(Query)
   */
  public static void readOnlyQuery(Query<?> q) {
    optimizeQuery(q);
    q.setReadOnly(true);
  }

  /**
   * Optimize a Hibernate query for batch performance. Disable Hibernate caching, set flush mode to
   * manual, and set fetch size to {@link NeutronIntegerDefaults.FETCH_SIZE}.
   * 
   * @param q query to optimize
   */
  public static void optimizeQuery(Query<?> q) {
    q.setCacheable(false);
    q.setCacheMode(CacheMode.IGNORE);
    q.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());
    q.setFlushMode(FlushMode.MANUAL);
    q.setHibernateFlushMode(FlushMode.MANUAL);
  }

  private static List<Pair<String, String>> buildPartitionsRanges(int partitionCount,
      String[] partitions) {
    final int len = partitions.length;
    final int skip = len / partitionCount;
    LOGGER.info("len: {}, skip: {}", len, skip);

    final Integer[] positions =
        IntStream.rangeClosed(0, len - 1).boxed().flatMap(NeutronStreamUtils.everyNth(skip))
            .sorted().sequential().collect(Collectors.toList()).toArray(new Integer[0]);

    if (LOGGER.isInfoEnabled()) {
      // Print it. Show off to your friends.
      LOGGER.info(ToStringBuilder.reflectionToString(positions, ToStringStyle.MULTI_LINE_STYLE));
    }

    final List<Pair<String, String>> ret = new ArrayList<>();
    for (int i = 0; i < positions.length; i++) {
      ret.add(Pair.of(i > 0 ? partitions[positions[i - 1]] : Z_OS_START,
          i == positions.length - 1 ? Z_OS_END : partitions[positions[i]]));
    }

    return ret;
  }

  @SuppressWarnings("unchecked")
  private static List<Pair<String, String>> getCommonPartitionRanges(
      @SuppressWarnings("rawtypes") AtomInitialLoad initialLoad, int numPartitions,
      String[] partitions) throws NeutronCheckedException {
    List<Pair<String, String>> ret = new ArrayList<>(numPartitions);
    if (initialLoad.isLargeDataSet()) { // Large data sets are only on z/OS.
      // ----------------------------
      // z/OS, large data set:
      // ORDER: a,z,A,Z,0,9
      // ----------------------------
      ret = initialLoad.limitRange(buildPartitionsRanges(numPartitions, partitions));
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

      // Yep, you read that right: by default Linux sorts in ASCII order, not EBCIDIC.
      ret.add(Pair.of("0000000000", "ZZZZZZZZZZ"));
    }

    return ret;
  }

  /**
   * Partition strategy with 4 partitions.
   * 
   * @return partition range pairs
   */
  public static List<Pair<String, String>> getPartitionRanges4() {
    return buildPartitionsRanges(4, BASE_PARTITIONS);
  }

  /**
   * Partition strategy with 16 partitions.
   * 
   * @return partition range pairs
   */
  public static List<Pair<String, String>> getPartitionRanges16() {
    return buildPartitionsRanges(16, BASE_PARTITIONS);
  }

  /**
   * Partition strategy with 64 partitions.
   * 
   * @return partition range pairs
   */
  public static List<Pair<String, String>> getPartitionRanges64() {
    return buildPartitionsRanges(64, BASE_PARTITIONS);
  }

  /**
   * Partition strategy with 512 (ish) partitions.
   * 
   * @return partition range pairs
   */
  public static List<Pair<String, String>> getPartitionRanges512() {
    return buildPartitionsRanges(512, EXTENDED_PARTITIONS);
  }

  public static List<Pair<String, String>> getCommonPartitionRanges4(
      @SuppressWarnings("rawtypes") AtomInitialLoad initialLoad) throws NeutronCheckedException {
    return getCommonPartitionRanges(initialLoad, 4, BASE_PARTITIONS);
  }

  public static List<Pair<String, String>> getCommonPartitionRanges16(
      @SuppressWarnings("rawtypes") AtomInitialLoad initialLoad) throws NeutronCheckedException {
    return getCommonPartitionRanges(initialLoad, 16, BASE_PARTITIONS);
  }

  public static List<Pair<String, String>> getCommonPartitionRanges64(
      @SuppressWarnings("rawtypes") AtomInitialLoad initialLoad) throws NeutronCheckedException {
    return getCommonPartitionRanges(initialLoad, 64, BASE_PARTITIONS);
  }

  public static List<Pair<String, String>> getCommonPartitionRanges512(
      @SuppressWarnings("rawtypes") AtomInitialLoad initialLoad) throws NeutronCheckedException {
    // Off by one. Close enough
    // However, "close" only counts in horseshoes (outdoor game), hand grenades, and hydrogen bombs.
    return getCommonPartitionRanges(initialLoad, 511, EXTENDED_PARTITIONS);
  }

}
