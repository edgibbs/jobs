package gov.ca.cwds.neutron.rocket.referral;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;

/**
 * Get key ranges by platform and rocket size.
 * 
 * @author CWDS API Team
 */
public class ReferralJobRanges {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReferralJobRanges.class);

  /**
   * Split tab delim line of key range, beginning and end.
   * 
   * @param line line to parse
   * @return key pair
   */
  private Pair<String, String> splitLine(String line) {
    final String[] pieces = line.split("\t");
    return Pair.of(pieces[0], pieces[1]);
  }

  @SuppressWarnings("rawtypes")
  private List<Pair<String, String>> limitRange(BasePersonRocket rocket,
      final List<Pair<String, String>> allKeyPairs) {
    List<Pair<String, String>> ret = allKeyPairs;
    final FlightPlan flightPlan = rocket.getFlightPlan();

    if (flightPlan.isRangeGiven()) {
      final int start = ((int) flightPlan.getStartBucket()) - 1;
      final int end = ((int) flightPlan.getEndBucket()) - 1;
      LOGGER.warn("KEY RANGES: {} to {}", start + 1, end + 1);
      ret = allKeyPairs.subList(start, end);
    }

    LOGGER.warn("SELECTED RANGE: {}", ret);
    return ret;
  }

  /**
   * Get key ranges by platform and range size.
   * 
   * @param rocket referrals range rocket
   * @return key pairs
   * @throws NeutronCheckedException on parse error
   */
  @SuppressWarnings("rawtypes")
  public List<Pair<String, String>> getPartitionRanges(BasePersonRocket rocket)
      throws NeutronCheckedException {
    List<Pair<String, String>> ret = new ArrayList<>();

    if (rocket.isLargeDataSet()) {
      LOGGER.info("z/OS, LARGE data set, ORDER: a,z,A,Z,0,9");

      try (@SuppressWarnings("unchecked")
      final Stream<String> lines =
          IOUtils.readLines(this.getClass().getResourceAsStream("/referral_ranges.tsv")).stream()) {
        ret = lines.sequential().map(this::splitLine).collect(Collectors.toList());
      } catch (Exception e) {
        throw CheeseRay.checked(LOGGER, e, "FAILED TO LOAD REFERRAL RANGES!");
      }

      ret = limitRange(rocket, ret);
    } else if (rocket.isDB2OnZOS()) {
      LOGGER.info("z/OS, small data set, ORDER: a,z,A,Z,0,9");
      ret.add(Pair.of("aaaaaaaaaa", "9999999999"));
    } else {
      LOGGER.info("Linux, ORDER: 0,9,a,A,z,Z");
      ret.add(Pair.of("0000000000", "ZZZZZZZZZZ"));
    }

    return Collections.unmodifiableList(ret);
  }

}
