package gov.ca.cwds.neutron.flight;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.HashSet;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.neutron.exception.NeutronCheckedException;

public class FlightPlanTest {

  FlightPlan target;

  @Before
  public void setup() throws Exception {
    target = makeGeneric();
  }

  public static final FlightPlan makeGeneric() {
    return new FlightPlan("config/local.yaml", null, null, null, null, null, false, 1, 5, 1, true,
        false, null, false, false, false, false, true, false, true, new HashSet<>());
  }

  @Test
  public void type() throws Exception {
    assertThat(FlightPlan.class, notNullValue());
  }

  @Test
  public void getEsConfigLoc_Args__() throws Exception {
    String actual = target.getEsConfigLoc();
    String expected = "config/local.yaml";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLastRunLoc_Args__() throws Exception {
    String actual = target.getLastRunLoc();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLastRunTime_Args__() throws Exception {
    Date actual = target.getOverrideLastRunStartTime();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getIndexName_Args__() throws Exception {
    String actual = target.getIndexName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getStartBucket_Args__() throws Exception {
    long actual = target.getStartBucket();
    long expected = 1L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getEndBucket_Args__() throws Exception {
    long actual = target.getEndBucket();
    long expected = 5L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getThreadCount_Args__() throws Exception {
    long actual = target.getThreadCount();
    long expected = 1L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void printUsage_Args__() throws Exception {
    FlightPlan.printUsage();
  }

  @Test(expected = NeutronCheckedException.class)
  public void parseCommandLine_Args__T__no_args() throws Exception {
    String[] args = new String[] {"--invalid"};
    FlightPlan actual = FlightPlan.parseCommandLine(args);
  }

  @Test
  public void parseCommandLine_Args__1() throws Exception {
    String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/dsmith/client_indexer_time.txt", "-t", "4", "--index-name", "cases"};
    FlightPlan.parseCommandLine(args);
  }

  @Test
  public void parseCommandLine_Args__delete_index() throws Exception {
    String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/dsmith/client_indexer_time.txt", "-t", "1", "-F", "-D", "-M"};
    FlightPlan.parseCommandLine(args);
  }

  @Test
  public void parseCommandLine_Args__2() throws Exception {
    String[] args = new String[] {"-c", "config/local.yaml", "-b", "3", "-r", "20-24", "-t", "4",
        "-a", "2010-01-01 00:00:00", "-i", "my-index"};
    FlightPlan.parseCommandLine(args);
  }

  @Test(expected = NeutronCheckedException.class)
  public void parseCommandLine_Args__3() throws Exception {
    String[] args = new String[] {"-c", "config/local.yaml", "-b", "3", "-m", "4", "-r", "20-24",
        "-t", "4", "-x", "99", "-a", "2010-01-01 00:00:gg", "-i", "my-index"};
    FlightPlan.parseCommandLine(args);
  }

  @Test(expected = NeutronCheckedException.class)
  public void parseCommandLine_Args__4() throws Exception {
    String[] args = new String[] {"-c", "config/local.yaml", "-b", "g", "-m", "4", "-r", "20-24",
        "-t", "4", "-x", "99", "-a", "2010-01-01 00:00:gg", "-i", "my-index"};
    FlightPlan.parseCommandLine(args);
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void setStartBucket_Args__long() throws Exception {
    long startBucket_ = 0L;
    target.setStartBucket(startBucket_);
  }

  @Test
  public void setEndBucket_Args__long() throws Exception {
    long endBucket_ = 0L;
    target.setEndBucket(endBucket_);
  }

  @Test
  public void setThreadCount_Args__long() throws Exception {
    long threadCount_ = 0L;
    target.setThreadCount(threadCount_);
  }

  @Test
  public void getOverrideLastRunTime_Args__() throws Exception {
    Date actual = target.getOverrideLastRunStartTime();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLastRunLoc_Args__String() throws Exception {
    String lastRunLoc = null;
    target.setLastRunLoc(lastRunLoc);
  }

  @Test
  public void isLastRunMode_Args__() throws Exception {
    boolean actual = target.isLastRunMode();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isLoadSealedAndSensitive_Args__() throws Exception {
    boolean actual = target.isLoadSealedAndSensitive();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void makeOpt_Args__String__String__String__boolean__int__Class__char() throws Exception {
    String shortOpt = "p";
    String longOpt = "pollywog";
    String description = null;
    boolean required = false;
    int argc = 0;
    Class<?> type = Integer.class;
    char sep = ' ';
    Option actual =
        NeutronCmdLineParser.makeOpt(shortOpt, longOpt, description, required, argc, type, sep);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void buildCmdLineOptions_Args__() throws Exception {
    Options actual = NeutronCmdLineParser.buildCmdLineOptions();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void parseCommandLine_Args__StringArray() throws Exception {
    String[] args = new String[] {"-c", "config/local.yaml", "-b", "3", "-r", "20-24", "-t", "4",
        "-a", "2010-01-01 00:00:00", "-i", "my-index"};
    FlightPlan actual = FlightPlan.parseCommandLine(args);
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = NeutronCheckedException.class)
  public void parseCommandLine_Args__StringArray_T__NeutronException() throws Exception {
    String[] args = new String[] {};
    FlightPlan.parseCommandLine(args);
  }

  @Test
  public void setIndexName_Args__String() throws Exception {
    String indexName = null;
    target.setIndexName(indexName);
  }

  @Test
  public void isRangeGiven_Args__() throws Exception {
    boolean actual = target.isRangeGiven();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLoadSealedAndSensitive_Args__boolean() throws Exception {
    boolean loadSealedAndSensitive = false;
    target.setLoadSealedAndSensitive(loadSealedAndSensitive);
  }

  @Test
  public void setRangeGiven_Args__boolean() throws Exception {
    boolean rangeGiven = false;
    target.setRangeGiven(rangeGiven);
  }

  @Test
  public void setLastRunMode_Args__boolean() throws Exception {
    boolean flag = false;
    target.setLastRunMode(flag);
  }

  @Test
  public void getBaseDirectory_Args__() throws Exception {
    String actual = target.getBaseDirectory();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setBaseDirectory_Args__String() throws Exception {
    String baseDirectory = null;
    target.setBaseDirectory(baseDirectory);
  }

  @Test
  public void setOverrideLastRunTime_Args__Date() throws Exception {
    Date lastRunTime = mock(Date.class);
    target.setOverrideLastRunTime(lastRunTime);
  }

  @Test
  public void isRefreshMqt_Args__() throws Exception {
    boolean actual = target.isRefreshMqt();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRefreshMqt_Args__boolean() throws Exception {
    boolean refreshMqt = false;
    target.setRefreshMqt(refreshMqt);
  }

  @Test
  public void isDropIndex_Args__() throws Exception {
    boolean actual = target.isDropIndex();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setDropIndex_Args__boolean() throws Exception {
    boolean dropIndex = false;
    target.setDropIndex(dropIndex);
  }

  @Test
  public void setEsConfigLoc_Args__String() throws Exception {
    String esConfigLoc = null;
    target.setEsConfigLoc(esConfigLoc);
  }

  @Test
  public void isSimulateLaunch_Args__() throws Exception {
    boolean actual = target.isSimulateLaunch();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setSimulateLaunch_Args__boolean() throws Exception {
    boolean testMode = false;
    target.setSimulateLaunch(testMode);
  }

  @Test
  public void determineInitialLoad_A$Date() throws Exception {
    FlightPlan target = new FlightPlan();
    Date lastRun = mock(Date.class);
    boolean actual = target.determineInitialLoad(lastRun);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getEsConfigLoc_A$() throws Exception {
    FlightPlan target = new FlightPlan();
    String actual = target.getEsConfigLoc();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isLastRunMode_A$() throws Exception {
    FlightPlan target = new FlightPlan();
    boolean actual = target.isLastRunMode();
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void makeOpt_A$String$String$String$boolean$int$Class$char() throws Exception {
    Option actual =
        NeutronCmdLineParser.makeOpt("c", NeutronLongCmdLineName.CMD_LINE_ES_CONFIG_PEOPLE,
            "ElasticSearch configuration file [index: people]", false, 1, String.class, ',');
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void buildCmdLineOptions_A$() throws Exception {
    Options actual = NeutronCmdLineParser.buildCmdLineOptions();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void printUsage_A$() throws Exception {
    FlightPlan.printUsage();
  }

  @Test
  public void parseCommandLine_A$StringArray() throws Exception {
    final String[] args = new String[] {"-c", "config/local.yaml", "-l",
        "/Users/dsmith/client_indexer_time.txt", "-t", "4", "--index-name", "cases"};
    FlightPlan actual = FlightPlan.parseCommandLine(args);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void parseCommandLine_A$StringArray_T$NeutronCheckedException() throws Exception {
    String[] args = new String[] {};
    try {
      FlightPlan.parseCommandLine(args);
      fail("Expected exception was not thrown!");
    } catch (NeutronCheckedException e) {
    }
  }

  @Test
  public void setLastRunMode_A$boolean() throws Exception {
    FlightPlan target = new FlightPlan();
    boolean flag = false;
    target.setLastRunMode(flag);
  }

  @Test
  public void setEsConfigLoc_A$String() throws Exception {
    FlightPlan target = new FlightPlan();
    String esConfigLoc = null;
    target.setEsConfigLoc(esConfigLoc);
  }

  @Test
  public void getEsConfigPeopleLoc_A$() throws Exception {
    FlightPlan target = new FlightPlan();
    String actual = target.getEsConfigPeopleLoc();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getEsConfigPeopleSummaryLoc_A$() throws Exception {
    FlightPlan target = new FlightPlan();
    String actual = target.getEsConfigPeopleSummaryLoc();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getOverrideLastEndTime_A$() throws Exception {
    Date actual = target.getOverrideLastEndTime();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setOverrideLastRunTime_A$Date() throws Exception {
    final Date lastRunTime = new Date();
    target.setOverrideLastRunTime(lastRunTime);
  }

}
