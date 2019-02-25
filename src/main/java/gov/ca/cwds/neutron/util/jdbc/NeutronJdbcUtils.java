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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
import org.hibernate.query.Query;

import gov.ca.cwds.data.BaseDaoImpl;
import gov.ca.cwds.neutron.atom.AtomHibernate;
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
 * <p>
 * DB2 sorts results differently by platform due to native character sets. That is, {@code ORDER BY}
 * does <strong>NOT</strong> order results the same way in Linux and z/OS! You've been warned.
 * </p>
 * 
 * @author CWDS API Team
 */
public final class NeutronJdbcUtils {

  private static final ConditionalLogger LOGGER = new JetPackLogger(NeutronJdbcUtils.class);

  private static final String Z_OS_START = "aaaaaaaaaa";
  private static final String Z_OS_END = "9999999999";

  /**
   * Basic client identifier partitioning for z/OS.
   */
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

  /**
   * Extended client identifier partitioning for z/OS. Use for large jobs.
   */
  private static final String[] PARTITIONS_512 = {"AeRTLuZ6WW", "Ahg03Ap3Ez", "AkEfSN73RZ",
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

  private static final String[] PARTITIONS_1024 =
      {"Aca36tZGdm", "AdRRWID5Jb", "Afyx2TR9Zp", "Ahfy9MT8SW", "AiV4DMD4zP", "AkCPFYvCf2",
          "Amh3DXRB3p", "AnZYLRE9Zp", "ApIrZ1C2AW", "ArmA7yzEaC", "AufJuGu63O", "AvZYQIT30A",
          "AxLrTc0H2T", "Azwa1Kf5xB", "ABf0yJoA3u", "AC2uRhi5ET", "AEJ2uhx1M7", "AG4dgoL3tZ",
          "AJkS3g930A", "AK2lXsX36B", "AMKfaL57SU", "AOqBA3W9xb", "AP7GhID6eP", "ARPjYdq2kl",
          "ATxUhPFDSs", "AVetix42Np", "AWYFM3m7NP", "AYFU6w7Acg", "A1dPodZ63f", "A2W7D52Ny6",
          "A4HUbVs9oD", "A6rEyzJ5Dg", "A8cCSh75Q0", "A9VHUHKJVC", "BbErdtR07S", "BdpB8RILLm",
          "Be7vxGvBuJ", "BgSiCUYGLZ", "BiAIuseG9P", "BkjDmsi5Aj", "Bl1OMG19ip", "BoUGYSsAS2",
          "BqCi4eF3Qu", "Bsj8vObAJH", "Bt12tBC99d", "BvIIXFB30A", "BxqA7KW0Py", "By84prY6U9",
          "BBsYoOb2Ld", "BDJyaUhDeC", "BFqMsblGF7", "BG9ezMMH4Y", "BISpDph43S", "BKA1hzV5Dj",
          "BMjMcFA0mF", "BN22e3O5xB", "BPIYlKW046", "BRrSDYrKRD", "BS9KdyMEzB", "BV3aT4EERK",
          "BXKiD2o1ze", "BZuNxgX43S", "B1eAt7NMYX", "B2XqfG1Bha", "B4IkQ8fCOq", "B6sOQt749S",
          "B8db0pCCOl", "B9Wze4X6eV", "CbEDajO9TC", "CdpPzGJJ8m", "Ce9Fgm76x4", "ChsbTfwMWM",
          "CjIhjlZ43S", "Clp2id5ALH", "Cm61PiF5DQ", "CoNiR935VY", "CqswmZdCqG", "CsaBNab30A",
          "CtP5wrS5DU", "CvufA4pC1F", "CxaGiM2BNQ", "CyRL0KFCKo", "CBopndC717", "CC8g0BNE6M",
          "CESIsfxGzG", "CGBal4i5Dk", "CIi06v03Yq", "CJ3cH2CGrR", "CLLXcwL36B", "CNLWEv0Bt6",
          "CQlJPKOH6i", "CR6dl82Ht6", "CTQF0WU5om", "CVBE97pC5E", "CXlwQ8901S", "CY4yLrN43S",
          "C0NoW4A5db", "C2wwmKx2TQ", "C4bCMVi0Z6", "C66aKEp4rh", "C8MFytt01S", "DaQlB9Y5TH",
          "Ddp8ooj49S", "De7hdUrC96", "DgPzwSO6Au", "DixltN8GF7", "DkcZBlK6ob", "DlUiUPGCOB",
          "DnCN6fz6V1", "Dpjalfd34A", "DqZ0JLS922", "DsIdBWPF4m", "DvzRBPL5Dg", "DxhJydA0P3",
          "DyXF1XF8kK", "DAF4ULNHc7", "DCpkvjT5Jb", "DD7t2L0FRj", "DFQf2Av6eP", "DHCGI792Jf",
          "DKtMRZDA15", "DMdAnGj7PC", "DNZMyyEA16", "DPKPoCc6AD", "DRvdvYS6UO", "DTgMABF0AH",
          "DU1tn7PBjD", "DWMwR0e4F3", "DYyfnPN34A", "D0h2Yvn03d", "D2k7uaeCmn", "D32Kg5f9yb",
          "D5IZjs4H9Q", "D7tmQdz6L5", "D9btFIH04F", "EaSxRwJ6L0", "EcBLM1MJkK", "EeiLtWl6vU",
          "EfZSzNj78v", "EhJHSwkEfU", "EjpQxkUBYW", "Ek7bqZOBm2", "EmO3HDMCHt", "EpIcBkG6V9",
          "ErrKRMO2Un", "Es9AzIQAs8", "EuSWxPtH6i", "EwCxwQMJ5y", "EykSJGx9Bz", "Ez1WazS15l",
          "EBKcYe6EaC", "EEDD0c544S", "EGqaiTN2LR", "EH9JbEI3b4", "EJTXhWI2SP", "ELFkShl7OF",
          "ENpprtH0AH", "EO9vReAAVR", "EQTHV5t99d", "ESDAcYD14N", "EUmNguZ26h", "EXjmXHS5TH",
          "EY0VSdw5DQ", "E0JthFS6oG", "E2qrq326yv", "E38UnrN54S", "E5N1mxH5Ov", "E7x5WmX8qt",
          "E9g4TXaMh6", "FaYdGnR43S", "FcFBHdJ30A", "FenCE7743S", "Ff5Zfav6uI", "FhRESxLIUP",
          "FkKL0qo196", "FmtsqCf30A", "FobxNbU5Dg", "FpTVWiC75R", "FrDlSxt5xu", "FtmD5G23UZ",
          "Fu55aan30A", "FwOYWrR36B", "Fyy3M4tEuS", "FAhteUCAR1", "FBXlWOP34A", "FDGqqL66cS",
          "FFlJ1PiaaK", "FG1DYUj33A", "FIKGRBM9Eo", "FKrDDvzBVn", "FL8WzaaDJB", "FNNIsJx5AH",
          "FQj8xd1C2O", "FSqWUMfOy2", "FT8AZV8Bha", "FVRmspd3sQ", "FXy97bv4t0", "FZhXge149S",
          "F0ZlREW2Ke", "F2H5vTF41S", "F4qrV0E2xc", "F59swEaGj1", "F82VSOv30A", "GaJ6iLaCco",
          "Gde483I72f", "GfpQGjF2AW", "Gg9cnZAAzW", "GiTvKAT40S", "GkCr2bOF9L", "GmjUMbt9ge",
          "Gn5QUVP6UO", "GpPLu4663f", "GryWOOL4tA", "GtjshV8JFj", "Gu7wm4A192", "GxV8JeW21w",
          "GzCGm49197", "GBisMcp7g2", "GCYEgpr4uJ", "GEFt5O507S", "GGh73G49j5", "GHYE5a76eP",
          "GKifKwD36B", "GMAMamFGLw", "GOhxriD43S", "GPZmtfE3aj", "GRJdJ6B6DS", "GTstt6C4tA",
          "GVcTRtF7l7", "GWU6yii6cu", "GYBW5ej24F", "G0CfuwDBsj", "G3b0n6v2ej", "G4SOFmPA3u",
          "G6yy6wT30A", "G8dYubl9Eo", "G9RVsb26VO", "HbARTu0GzA", "HdjRMVy6cS", "He44vPT9a5",
          "HgOL2Ib3Rp", "HizWIdO4si", "Hkj7Qn50LU", "Hl5ISaQ5vN", "HnTjHEyFjg", "Hqo54ht2hL",
          "Hsu7tqlFsf", "HucGDL1Bha", "HvUjMMZ2Ke", "HxBPSbp3PK", "HzjbTNRAzi", "HA0fV8z36B",
          "HCHzTCj1Gz", "HFzJdbz6aI", "HHg2uqz8Gq", "HIZi8Us4Zx", "HKFIIX49Zp", "HMouhec10S",
          "HN6e3Fqz33", "HPMPpj26SD", "HRv98k0F7i", "HTeMPIx5ij", "HUTRMZP5Dm", "HW8SgSM9hu",
          "HZw5k9hAmr", "H1e4XDt37S", "H2V5ze55VY", "H4BIsVg8Ow", "H6iJV10199", "H7XKExRBV3",
          "H9EKYUr2bX", "IbobEDh34A", "Ic8MEhyGzs", "IeUEOP337S", "IgF8wEg5Oc", "Iiqgslt54S",
          "Ilmdyvz3sC", "Im4bFlqDyB", "IoMTFZ5Cws", "IqvNU0L2N2", "Iscj9Ak1Yc", "ItSeE0LDN8",
          "IvAQFaz30A", "IxkdB22CQa", "Iy1fh3p54S", "IALJka19j5", "ICqqZHP1Kz", "ID6XBjN30A",
          "IFNoq8Y4qv", "IHtdvnI5Ds", "IJaesEDCfU", "IKPFBo137S", "IMvdTmx3hy", "IObRgBP6eV",
          "IPSJJuGHJG", "ISMoyqRN09", "IUwrp7vI5J", "IWgTBGoA6L", "IX1BAXj0RM", "IZN8R6o043",
          "I1vbDINBm3", "I3dT8yH5Dj", "I4Uc5ZZ0NY", "I6CakUNCfj", "I9obxLOLUU", "Jbdw3dZ43S",
          "JcVkpXaFX0", "JfNkJ21BjD", "Jhu2a49197", "JjcM7AB4Zy", "JkVM3beDDC", "JmCPSTyAcg",
          "JolbtcFBm5", "Jp3W0STOy2", "JrLSqKpEaC", "JtuQZwx2Ld", "JvcbeOT6ob", "JxsUYGf3Lv",
          "JzLcfMk2hL", "JBtFYMv4oz", "JDdKPnq2vI", "JEVZ7h55wz", "JGEJPGWBm2", "JInW2Bd6cS",
          "JJ7Q9YAAbA", "JMQtXOc5Cx", "JOIA2Jw2qt", "JQoBxLW9of", "JR50RsuCrw", "JTOccDxDDF",
          "JVuDdWULIQ", "JXdDuanCCS", "JYR8QCsNqb", "J0BxFVI6d0", "J24uLCm6cu", "J49MXmn5Fe",
          "J6TeSE55Ch", "J8BrxVh07S", "KakSZ1cDNc", "Kb3QAEfJ1J", "KdLP8F8DNc", "KfsvR6HA2T",
          "KhanfxeDsF", "KiSlQHPB0R", "KkBUCkU8eE", "KmjX56XKbI", "Kn2XSbJKE8", "KpKVm2z37S",
          "KsCErrd37S", "KumpyjlAli", "Kv5tssDDzS", "KxPogPF5Fe", "KzyDZoJMPq", "KBhRRMh4nn",
          "KC2oN484Zy", "KEK3Ke06f4", "KHC39iH0V1", "KJlcKBCAUi", "KK0OxmXEZD", "KMG2ppP9mp",
          "KOofsqT5ES", "KP4Lk0z2qR", "KRLfNl6Bp1", "KTqyqig10S", "KU77oLE8AK", "KWPc1rl2Rw",
          "KZwBfvBA16", "K1pXOCL5Fe", "K28T2Gp7g2", "K4Szr19M9L", "K6Bjpfc6bb", "K8lpaMb36B",
          "K96Wd6bOyk", "LbO6fG18SL", "Ldv2fBvBKy", "Lfc6ngmATF", "LgTFJaE6gX", "LizVjs75D9",
          "LkgEuOML8Q", "LnauVof30A", "LoUFhx337S", "LqDmaS07l7", "LspvMsD01S", "Lt9ACu63Lw",
          "LvThTDm10S", "LxDX6ht37S", "LzoOxAHChq", "LBNqTkb3ax", "LDvIk0SMXK", "LFdF2Sy5Fe",
          "LGUaZO6192", "LIzez7n5Cx", "LKfBYnA0KD", "LLWoPzo5VY", "LNBJaQP5Df", "LPiai3t37S",
          "LQZ5F8tC3m", "LTq3dnX2k5", "LVw2ztm9j6", "LXd5EBfAwr", "LYU7U156DS", "L0Cu0sF5Dk",
          "L2nCNHt30A", "L37zxL394v", "L5QUk9k4nn", "L7BVsMV6uL", "L9LEYzS3c0", "MbwOfw2AnH",
          "Mdeami9Eh4", "MeVaftd8xK", "MhNfitN43S", "MjtigWOFty", "MlaiYcoDNc", "MmRZzHt37S",
          "Moz81yBC4X", "MqjXhfIMRY", "Mr2Gdsn2LK", "MtJIiUuGzs", "MvqTHxR30A", "Mw7uL4i4zY",
          "MzN5FX042E", "MBFqGqT9A2", "MDnLZVPCnO", "ME6NCJl37S", "MGN9zeT7CO", "MIvH8450TZ",
          "MKdg0x4C7Y", "MLU0DtcDjK", "MOMk53c5Do", "MQvSkicMP0", "MScAYJo5xu", "MTVFg1sMiP",
          "MVDwun46oG", "MXlm8xi0kC", "MY4DmVF6qq", "M0NNF8FMxR", "M2znnTLBrf", "M5sYUux3hi",
          "M7drznvMQk", "M8TOV9d30A", "NbkdCIE4tA", "Nc0Hfsr3M6", "NeJe5mFBp6", "Ngp86K90Ut",
          "Nh42QYj43S", "NjOiUa2Kye", "NlwHVOc9ck", "NncP1bq6nO", "NoRD3R56lq", "NqyWnVR37S",
          "Ns0PH7p01S", "NvadBIu5X0", "NwSDYfL4kv", "NyywnIM6qG", "NAgHboS9jA", "NBZvmzaG43",
          "NDE7JxEFfE", "NFlVyu930A", "NIeTUBk2ha", "NJYNdnmFNu", "NLGURJsBaA", "NNqIPcz8aR",
          "NO77ain07S", "NQNHIRe1mN", "NSwZNob7Jo", "NUegMG28mw", "NVWdAZdC1a", "NXEGfRs4F3",
          "NZMIkle7XZ", "N1zkHwL3F8", "N3jMnsL2LR", "N419H8b30A", "N6L8phZ5Ch", "N8tHrSgAQf",
          "OaaEf1s0kC", "ObRqiHoC8S", "Odw0l3l2My", "OfeRfkS5DT", "OgVtDgK5q5", "OiA0qpO73p",
          "OkhqS6uGPM", "OlYdRfD6gX", "OoTylzt2EC", "Oqy7vYw5dM", "OsfSTrqCMi", "OtZdVzJBn5",
          "OvGx5PzAAz", "OxogZ9t34A", "Oy6idx75q5", "OAN9fvRPTl", "ODLPHPi1M7", "OFw1GA305E",
          "OHfyFpy10S", "OIZbWwD01S", "OKJMU7x3hi", "OMug8O15Cx", "OOchICzAOM", "OPVt29O9A4",
          "OREr2R0Bdr", "OTCh9V0C1a", "OWg3m8oCd6", "OXYg6hl30A", "OZEVZggBn5", "O1m49V39cz",
          "O26XiyH2xh", "O4QRltQCnH", "O6w1h7zMtC", "O8b1MmfB3r", "O9VDSMu3Ek", "PbCVLhy199",
          "PdjA2Ia4D9", "PeYUv4gJfK", "PgS74aXD8q", "Pjy3KaL2vI", "Pldz8Hy26h", "PmTqrTQ0RM",
          "PoAeapZA17", "PqfIOsk3qY", "PrUTsC937S", "PtBAzmO7NP", "Pvf2KMV6V1", "PwWLp9WLAb",
          "PyB8ld75Yk", "PALyGqjO0s", "PCvSX62DTW", "PEe0Xir30A", "PFZbp570Yt", "PHKFxlvDxO",
          "PJrXpZ0GZY", "PLcHRAK4F3", "PMWjW7R7l7", "PPIkabM6a7", "PRBwmOaAwU", "PTjs5u7D55",
          "PU1fepf8iJ", "PWJ08KCMR8", "PYqqF6QEXp", "PZ56RKT01S", "P1MBSSI7B5", "P3ua4juBGv",
          "P5CNhGj36B", "P79H63p21w", "P9Tvy5J197", "QcEoeurHj9", "QexTz0kAmr", "QgfJSFO5yt",
          "QhZVoHe5Du", "QjIrZ6E5Fx", "Qlo3v7lH7J", "Qm60W4QFUU", "QoNKdFRDEx", "QqwZyjKKiq",
          "QsgLeCpAzW", "Qvak9wD4kN", "QwO2d0gFNu", "QyxI6sL5Le", "QAe5fD5CVF", "QB0o4eTCfj",
          "QDK7pqO9cm", "QFtQayD5Cw", "QHdXvqT37S", "QJYyWAN1mN", "QLPSLET5Cx", "QNyOdLTEA7",
          "QPgrDSPE0v", "QQWn2F926z", "QSC6gHJBre", "QUkEQS68p7", "QV3nDSf2kl", "QXKRryz197",
          "QZsvKky7bU", "Q1XShwQ4tA", "Q3H86M501S", "Q5ssLxxDSt", "Q7bYsmUMlM", "Q8WeDas2r5",
          "RaEVOrKJp6", "Rcm0jeGNy0", "Rd5tbDl36B", "RfOO5au1X9", "RhyAtqlKYJ", "RjiM6PVOph",
          "Rk0V29f5UG", "RmJZ1Af8ZL", "RppDJSm2OJ", "RrkX2kz01S", "Rs0tiAD7D3", "RuH8NhD16S",
          "RwmiSzF8IJ", "Rx0eV7cO78", "RzGsZMM1Ij", "RBmgIjf0BN", "REf1Ehu3eH", "RFX9nzx5UB",
          "RHFvJtC5qH", "RJncs5RD90", "RK4E1t6Ans", "RMLTlQ5Opb", "ROt6ZONBXe", "RQbHTVm2Mq",
          "RRS0bRlFx7", "RTAvrlp15A", "RVH1bf642E", "RYdyuA95Ds", "RZWlRbW5DT", "R1DrjG536B",
          "R3jxDWZH4Y", "R40PbkwEO2", "R6IeWjW4k5", "R8n2grlDxO", "R94gLQn36B", "SbOEW6GNQw",
          "SdyKDKS6L5", "Sfjt5UO3Em", "Sg3zMX87l9", "SjrJsMjDB0", "SlCVquf046", "SnlowoiDha",
          "So2SazC5Cp", "SqKlHqz37S", "SsprRD730A", "St6NX4lGsk", "SvOocKX38A", "SxvO2SgDdh",
          "SzdT45w1B5", "SAUr3pI6AD", "SCCk7AA2dT", "SEkq73H4wm", "SF1uf1F07S", "SHIxQWNC42",
          "SJrd52YCVp", "SK89oBt43S", "SMSb12KFns", "SOBeK6k5Du", "SRtYylV5q5", "STeEuGs5Du",
          "SU0d0I35xB", "SWJhGcgMF8", "SYtkXk5G0z", "S0c6qEEKFQ", "S1T4Lxu77R", "S3zSDyW2U6",
          "S5goo51GzG", "S68sAHRA17", "S9OzxKiBDj", "Tbzs6pm3oq", "Ter3W9z2Nl", "Tf9wEcS6i8",
          "ThQZcQWH0A", "TjxK4Zd43S", "TlhfMso3Xv", "TmZ097jDNc", "ToGKYV0BKw", "Tqn2HUXAzW",
          "Tr5Rj6a6xC", "TtMEghFC4r", "Tv89Puf0kW", "TykXeoD30A", "Tz3tthD8fE", "TBL1vDn6p7",
          "TDs3wN00MM", "TFbsELWGUs", "TGScLLxMF4", "TIAle2mI6B", "TLqo6Wv01S", "TM99n7a4hE",
          "TOWJuin44S", "TQFYRju2vI", "TSqC1ysz19", "TUazrBB43S", "TVVAoAk199", "TXETopd30A",
          "TZpdRPPC42", "T07bqvx36B", "T3WVEwy45J", "T5DTVl9IVF", "T7jigj343S", "T8ZpfSW64q",
          "UAF9Wvn8Jb", "UCpEeKf36B", "UD7hqhl37S", "U1M7MhUBm2", "U3v8Spe6bm", "U5dpT9NJmZ",
          "U6UOomQ10S", "U8B2j6bAnE", "0a01y4H6d2", "0dgu6UGC42", "0eZfEPp07S", "0gKvSNvFJA",
          "0it3D9h8qa", "0kfx71G5Du", "0l2bfRZ1mC", "0nMMf0j36B", "0pxIDxZ2ex", "0ripWcoBQB",
          "0s2OjjkAcg", "0vVKrpJGmy", "0xDSnbP5dU", "0zj3O3l37S", "0A22gSeCpS", "0CLeIMt4nn",
          "0EsGRij30A", "0GalXPSPRS", "0ItoNE42U6", "0KLrRSKEzB", "0Mvexes4wm", "0OfD6bl0KD",
          "0PWIKBiA6L", "0RE9aZV4gP", "0TmsOM05EE", "0U6A4bC4nd", "0WOwzFcFLP", "0YyiVz26Au",
          "00fsqa82SP", "01XIFBdGdQ", "03GVwip5RC", "05nnMXZ41S", "065wTg64zP", "08Qvoql6sl",
          "1az04JtJx3", "1clSLQr4D9", "1d7c4iJ5q5", "1fRlQmB06Q", "1hCVijpFyl", "1jltVjd30A",
          "1k5qj6LI4N", "1mNUevu9A4", "1pHDVI32Ld", "1rqOv1l9mp", "1s9lI6TJPT", "1uQZbrsC42",
          "1wxpC4X37S", "1yfLqOY6v3", "1zXKtUp5om", "1BQJL8s10S", "1EBkDxZ37S", "1GjDHQmBm4",
          "1H2rh4c5DT", "1JJdFgf92i", "1Ls0ILNDq8", "1NbIVaL4rN", "1OUfc8M3dA", "1QEXG9V9dr",
          "1Sm6Qgz41S", "1T5NayQBDI", "1W0utrl30A", "1YKyrPV37S", "10vTcn315A", "12fnSC96ob",
          "13YhNgn43S", "15F3NRLFfE", "17o5qkN5gH", "187fB7ZDdh", "2aPkkAgCxs", "2cBMZk6C1a",
          "2el8UJX4nn", "2f7X0lu5gS", "2ispzylKRt", "2kK2jNy1DY", "2muRNis10S", "2oby2yk2SP",
          "2pTJdxh5Du", "2rCujv4BLr", "2tl6UN45pf", "2u3WD6MBn5", "2wMZuOoDjS", "2yvs6YqC5M",
          "2AcWKMnD7r", "2BTeM1YBzd", "2DCJr6K91i", "2FlazGg9us", "2G3zJ5P4wm", "2IKp9xIChq",
          "2KrVNe05X8", "2L94yXi65j", "2NRndfPB3q", "2QH2MeS42H", "2StdWyl7D7", "2Uejk3U2vI",
          "2V2YAAT33A", "2XMS3IY6cS", "2ZwYD6L4p3", "21eV25z2O2", "22WQ3G536B", "24E0CE0Jx4",
          "27g14L726z", "29hDU5B36B", "3aZKrBeMKG", "3dTGR3Q3Xv", "3fC1NNU3Ju", "3hlG6txHt7",
          "3i39JjFC8p", "3kMUkLqHwr", "3mvF4EXAPX", "3ocVGTr5nH", "3pVHYbcG0k", "3rD9vef59R",
          "3toQDvm6mi", "3wlSCJ96bJ", "3x5LP039mp", "3zOykrR2KA", "3BwaMsL43S", "3DcNgbCGi9",
          "3ETlHv3AQf", "3Gyz7Vp1LU", "3Ie1RGiBm3", "3KXBGbp197", "3MTufNbBS0", "3OFjSFK2Un",
          "3QpG8blB3b", "3SbBF7y5Ds", "3TVwkd1CR3", "3VELCwWMPo", "3Xp1YH8Awr", "3ZcXMp9DNc",
          "31WllAH33A", "33PLZ6ZFhE", "35yNgyNHcP", "37d6fF60P3", "38VIICW0Nn", "4aBjbnk9Z4",
          "4cjtFrBLlN", "4d3dshNCnH", "4fPuDIl5yN", "4hyYjxW9gv", "4jieyRB2Np", "4kZWcgp63f",
          "4mKo17C06Q", "4oAH2ph4Zl", "4rl9G8r8kF", "4s7bLIf36B", "4uObuXtACq", "4wAQufD7PC",
          "4yjCbK6199", "4z3uKDR3Or", "4BNDZ961Wb", "4D9g5JJ36B", "4GojxJh2OJ", "4H4uCec6zK",
          "4JNiYD336B", "4Ls7mXmJ1k", "4M7yomh5Cn", "4ORKNWwAWR", "4QzTsOH36B", "4SgV6kEEfU",
          "4TXFxIf6jR", "4VFlyOD2qt", "4YzIfuyDcX", "40gPITy5Oi", "41Y5LYAIu0", "43H7KyI2vu",
          "45rWUDaKlc", "47aUn11N6u", "48SPzZ12S9", "5aCjdJW9NQ", "5ckbwLv4mv", "5d2nM0XJEF",
          "5fMtGE53tM", "5huVRpp3SL", "5j3kdUjHcP", "5l9AOh96VR", "5nTP3Y1GTf", "5pEgrbuKWV",
          "5rmhu0v34A", "5s6cmzsAEx", "5uQMrYX43S", "5wCNBg22QH", "5ynvg77BG3", "5z8cbWaB42",
          "5BL8zIqMtC", "5Duxkcy94v", "5Fa2C6z7PC", "5GStuHR43S", "5IzjQm26mW", "5KeEanoCpS",
          "5LVHhB65xC", "5NBY4Ly5rb", "5PijBmZCbt", "5Scmbwn6gX", "5TU6oLp37S", "5VEtOhF40S",
          "5XmMZIOAzf", "5Y6kUb83UM", "50OrE0H44S", "52yUD1V30A", "54i65GdC96", "553Tz2F9so",
          "58qyZTl34A", "6aHacr4199", "6cqmaUR21w", "6fkUZy6EDF", "6g7iedJ2k5", "6iSV0uC7U4",
          "6kEtF9WM5K", "6mpa27Q4m3", "6ocazrT5Df", "6pV2CoTDOk", "6rDCiYd4nn", "6toUmJt36B",
          "6u9RQv7Lch", "6xU72yTD3U", "6zNbiV85Ps", "6Bu16yh2uR", "6DdaWRe2rU", "6EUjDb80ZX",
          "6Gzva1C191", "6IgPttc3ht", "6JZmKkcBgW", "6MTirUdIfU", "6OzVdEG9dT", "6Qgl5SkCPl",
          "6RWoz3Z54S", "6TDuXC556F", "6Vj0cci0Ut", "6W3noIRChq", "6YKbYQxAzW", "60rc7mA9q0",
          "63j742lKyJ", "6442jZ45mG", "66MXlu56cS", "68tIqHJ01S", "7acBOuh5Ds", "7bUzCwJ2JJ",
          "7dB8DWU3gC", "7fkgnUp7ir", "7g2GPrRP9Q", "7iKlphp887", "7kr8khv1zr", "7l94uPg5vN",
          "7nTD0M52en", "7pBfh87FKI", "7suDZuj36B", "7uedii99Zp", "7vWWLlH99d", "7xFpfnrGLk",
          "7zpQ73VG6l", "7BaezXWDga", "7CStRVvH0A", "7EKOQIl1DY", "7HzVpoLJKg", "7Jje7Dl37S",
          "7K1GsJh6cS", "7MLnTH730A", "7OtsTlRH30", "7QbyJPSALH", "7RWUwGi3oq", "7THAiBxGrw",
          "7VqfNHYAF8", "7XaBecN1aS", "7Z4rEsbCZO", "71NwKr75DL", "73weGcxH3U", "75fQGkW5D8",
          "76Zq2zK9Vh", "78IaWsKC42", "8arBloT9Uu", "8cae78L30A", "8dRacCt8jr", "8fyYq8H36B",
          "8hhXi5r06Q", "8i3ylvO5DT", "8k58YZTAF8", "8nDsH8n2NJ", "8pm9PqD30A", "8q5qdyD2bO",
          "8sNnfpz4kN", "8uxjs5B15A", "8wgYtDG5DB", "8xZObFU0kC", "8zJIKrz5Df", "8BNotGrEfU",
          "8DxJV1o2qT", "8Fg4sGQE1P", "8G2k9N372f", "8ILVpdQGgG", "8KuVX7d64h", "8MdXWPqCrw",
          "8NXQAWfAzZ", "8PGi0KQM39", "8Roo39B2Nd", "8UiTs29COp", "8V0fDICNxt", "8XItjhE2LR",
          "8ZoRvJ8CVp", "8071lPp3fm", "82Qc5k1A16", "84x1G4MEfU", "86hEr0D30A", "871qsOmAon",
          "9aAtMtF30A", "9ciePCX1Gz", "9dZdAC7Bre", "9gPsXKtAu7", "9iAciQr30A", "9khRhxL0RH",
          "9lZaYQaB7S", "9nG6C9J37S", "9ppKzOj36B", "9q7zsHr4p3", "9sPiX9IKFN", "9uvY5u06wK",
          "9weYuKv36B", "9yxHKHLDeC", "9ARtjub07S", "9CDjJrX5ae", "9EnOhaj30A", "9F9op9738A",
          "9HTZMDI2cc", "9JEbS9t5Do", "9Lo4MUg8Op", "9OiueMl26h", "9PZjuNK9oD", "9RGzOXT78h",
          "9TpjASA4ez", "9U7EikE2en", "9WQzzdJ0Ur", "9YxdT1t37S", "90frs1t0Ut", "91YYq1jBFV",
          "94klpEG2cD", "96Egptr6Nh", "98oXLe75Us", "998wqSDI6s", Z_OS_END};

  private NeutronJdbcUtils() {
    // Static utility class.
  }

  /**
   * @return default CMS schema name
   */
  public static String getDBSchemaName() {
    return System.getProperty(AtomHibernate.CURRENT_SCHEMA);
  }

  /**
   * Steal a connection from an active Hibernate session.
   * 
   * @param session active Hibernate session
   * @return database Connection
   */
  public static Connection prepConnection(final Session session) {
    final NeutronWorkConnectionStealer work = new NeutronWorkConnectionStealer();
    doWork(session, work);
    return work.getConnection();
  }

  public static int runStatementInsertLastChangeKeys(final Session session, final Date lastRunTime,
      final String sql, final Function<Connection, PreparedStatement> func) {
    final NeutronWorkTotalImpl work = new WorkPrepareLastChange(lastRunTime, sql, func);
    doWork(session, work);
    return work.getTotalProcessed();
  }

  public static int runPreparedStatementInsertLastChangeKeys(final Session session, Date start,
      Date end, int runId, final String sql, final Function<Connection, PreparedStatement> func) {
    final NeutronWorkTotalImpl work = new WorkPrepareWhatChanged(start, end, runId, sql, func);
    doWork(session, work);
    return work.getTotalProcessed();
  }

  public static int runStatementInsertRownumBundle(final Session session, int start, int end,
      final Function<Connection, PreparedStatement> func) {
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
        LOGGER.trace("PREPARE LAST CHANGE SQL:\n\n{}\n", sql);
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
      // Hibernate Session.clear() may fail without a transaction.
      grabTransaction(session);
      session.clear(); // Hibernate "duplicate object" bug
    } catch (Exception e) {
      LOGGER.trace("'clear' without transaction", e);
    }
  }

  /**
   * "Work-around" (gentle euphemism for <strong>HACK</strong>) for annoying condition where a
   * transaction should have started but did not.
   * 
   * <p>
   * Get the current transaction from the current session or start a new transaction.
   * </p>
   * 
   * @param dao DAO
   * @return current, active transaction
   */
  public static Transaction grabTransaction(final BaseDaoImpl<?> dao) {
    return grabTransaction(dao.grabSession());
  }

  /**
   * @param session active Hibernation session
   * @return current, active transaction
   * @see #grabTransaction(BaseDaoImpl)
   */
  public static Transaction grabTransaction(final Session session) {
    Transaction txn = null;
    try {
      txn = session.beginTransaction();
    } catch (Exception e) { // NOSONAR
      txn = session.getTransaction();
    }
    return txn;
  }

  /**
   * Generic method to execute a Hibernate Work implementation (arbitrary JDBC through Hibernate).
   * 
   * @param session active Hibernate session
   * @param work Hibernate Work instance
   */
  public static void doWork(final Session session, Work work) {
    clearSession(session);
    grabTransaction(session);
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
   * manual, and set fetch size to {@code NeutronIntegerDefaults.FETCH_SIZE}.
   * 
   * @param q query to optimize
   */
  public static void optimizeQuery(Query<?> q) {
    q.setCacheable(false);
    q.setCacheMode(CacheMode.IGNORE);
    q.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());
    q.setHibernateFlushMode(FlushMode.MANUAL);
    q.setTimeout(120); // 2 minutes tops
  }

  private static List<Pair<String, String>> buildPartitionsRanges(int partitionCount,
      String[] partitions) {
    final int len = partitions.length;
    final int skip = len / partitionCount;
    LOGGER.debug("len: {}, skip: {}", len, skip);

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
    return buildPartitionsRanges(512, PARTITIONS_512);
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
    return getCommonPartitionRanges(initialLoad, 511, PARTITIONS_512);
  }

  public static List<Pair<String, String>> getCommonPartitionRanges1024(
      @SuppressWarnings("rawtypes") AtomInitialLoad initialLoad) throws NeutronCheckedException {
    return getCommonPartitionRanges(initialLoad, 1024, PARTITIONS_1024);
  }

  public static void enableBatchSettings(final Session session) {
    session.setCacheMode(CacheMode.IGNORE);
    session.setDefaultReadOnly(true);
    session.setHibernateFlushMode(FlushMode.MANUAL);
  }

  /**
   * Enable DB2 parallelism. Ignored for other databases.
   * 
   * @param con connection
   * @throws SQLException connection error
   */
  public static void enableBatchSettings(Connection con) throws SQLException {
    final String dbProductName = con.getMetaData().getDatabaseProductName();
    con.setSchema(getDBSchemaName());
    con.setAutoCommit(false);

    if (StringUtils.containsIgnoreCase(dbProductName, "db2")) {
      new WorkDB2UserInfo().execute(con);
    }
  }

}
