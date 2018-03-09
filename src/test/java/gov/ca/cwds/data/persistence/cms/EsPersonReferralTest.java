package gov.ca.cwds.data.persistence.cms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.data.es.ElasticSearchPersonAllegation;
import gov.ca.cwds.data.es.ElasticSearchPersonReferral;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.EmbeddableAccessLimitation;
import gov.ca.cwds.data.persistence.cms.rep.EmbeddableStaffWorker;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;

public class EsPersonReferralTest extends Goddard<ReplicatedPersonReferrals, EsPersonReferral> {

  EsPersonReferral target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    EsPersonReferral.setOpts(flightPlan);
    target = new EsPersonReferral();
    target.setClientId(DEFAULT_CLIENT_ID);
  }

  @Test
  public void type() throws Exception {
    assertThat(EsPersonReferral.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getNormalizationClass_Args__() throws Exception {
    final Class<ReplicatedPersonReferrals> actual = target.getNormalizationClass();
    final Class<ReplicatedPersonReferrals> expected = ReplicatedPersonReferrals.class;
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void normalize_Args__Map() throws Exception {
  //
  // final Map<Object, ReplicatedPersonReferrals> map =
  // new HashMap<Object, ReplicatedPersonReferrals>();
  // final ReplicatedPersonReferrals actual = target.normalize(map);
  // final ReplicatedPersonReferrals expected = new ReplicatedPersonReferrals();
  // assertThat(actual, is(equalTo(expected)));
  // }

  @Test
  public void getNormalizationGroupKey_Args__() throws Exception {
    final Object actual = target.getNormalizationGroupKey();
    final Object expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPrimaryKey_Args__() throws Exception {
    final Serializable actual = target.getPrimaryKey();
    final Serializable expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void equals_Args__Object() throws Exception {
    final Object obj = null;
    final boolean actual = target.equals(obj);
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void toString_Args__() throws Exception {
    final String actual = target.toString();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void normalize_Args__Map() throws Exception {
    target.setClientId("xyz789");
    target.setAllegationId("abc1234");
    target.setReferralId("ddusicnz7");
    final Map<Object, ReplicatedPersonReferrals> map =
        new HashMap<Object, ReplicatedPersonReferrals>();
    final ReplicatedPersonReferrals actual = target.normalize(map);
    final ReplicatedPersonReferrals expected = new ReplicatedPersonReferrals("xyz789");
    final ElasticSearchPersonAllegation allegation = new ElasticSearchPersonAllegation();
    allegation.setId("abc1234");
    allegation.setLegacyId("abc1234");
    allegation.setLegacyDescriptor(gov.ca.cwds.neutron.util.transform.ElasticTransformer
        .createLegacyDescriptor("abc1234", null, LegacyTable.ALLEGATION));
    final ElasticSearchPersonReferral referral = new ElasticSearchPersonReferral();
    referral.setId("ddusicnz7");
    referral.setLegacyId("ddusicnz7");
    referral.setLegacyDescriptor(
        ElasticTransformer.createLegacyDescriptor("ddusicnz7", null, LegacyTable.REFERRAL));
    expected.addReferral(referral, allegation);
    // Value is a literal "null"? Is this right?
    // expected.geReferrals().get(0).getAccessLimitation().setLimitedAccessGovernmentEntityId("null");
    // referral.setCountyId("null");
    // referral.setResponseTimeId("null");
    // allegation.setDispositionId("null");
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getLastChange_Args__() throws Exception {
    final Date expected = new Date();
    target.setLastChange(expected);
    final Date actual = target.getLastChange();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLastChange_Args__Date() throws Exception {
    final Date lastChange = new Date();
    target.setLastChange(lastChange);
  }

  @Test
  public void getClientId_Args__() throws Exception {
    final String actual = target.getClientId();
    final String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClientId_Args__String() throws Exception {
    final String clientId = null;
    target.setClientId(clientId);
  }

  @Test
  public void getReferralId_Args__() throws Exception {
    final String actual = target.getReferralId();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReferralId_Args__String() throws Exception {
    final String referralId = null;
    target.setReferralId(referralId);
  }

  @Test
  public void getStartDate_Args__() throws Exception {
    final Date actual = target.getStartDate();
    final Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setStartDate_Args__Date() throws Exception {
    final Date startDate = new Date();
    target.setStartDate(startDate);
  }

  @Test
  public void getEndDate_Args__() throws Exception {
    final Date actual = target.getEndDate();
    final Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setEndDate_Args__Date() throws Exception {
    final Date endDate = new Date();
    target.setEndDate(endDate);
  }

  @Test
  public void getReferralResponseType_Args__() throws Exception {
    Integer actual = target.getReferralResponseType();
    Integer expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReferralResponseType_Args__Integer() throws Exception {
    Integer referralResponseType = null;
    target.setReferralResponseType(referralResponseType);
  }

  @Test
  public void getCounty_Args__() throws Exception {
    Integer actual = target.getCounty();
    Integer expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCounty_Args__Integer() throws Exception {
    Integer county = null;
    target.setCounty(county);
  }

  @Test
  public void getReporterId_Args__() throws Exception {
    final String actual = target.getReporterId();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReporterId_Args__String() throws Exception {
    String reporterId = null;
    target.setReporterId(reporterId);
  }

  @Test
  public void getReporterFirstName_Args__() throws Exception {
    final String actual = target.getReporterFirstName();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReporterFirstName_Args__String() throws Exception {
    String reporterFirstName = null;
    target.setReporterFirstName(reporterFirstName);
  }

  @Test
  public void getReporterLastName_Args__() throws Exception {
    final String actual = target.getReporterLastName();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReporterLastName_Args__String() throws Exception {
    String reporterLastName = null;
    target.setReporterLastName(reporterLastName);
  }

  @Test
  public void getWorkerId_Args__() throws Exception {
    final String actual = target.getWorkerId();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setWorkerId_Args__String() throws Exception {
    String workerId = null;
    target.setWorkerId(workerId);
  }

  @Test
  public void getWorkerFirstName_Args__() throws Exception {
    final String actual = target.getWorkerFirstName();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setWorkerFirstName_Args__String() throws Exception {
    String workerFirstName = null;
    target.setWorkerFirstName(workerFirstName);
  }

  @Test
  public void getWorkerLastName_Args__() throws Exception {
    final String actual = target.getWorkerLastName();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setWorkerLastName_Args__String() throws Exception {
    String workerLastName = null;
    target.setWorkerLastName(workerLastName);
  }

  @Test
  public void getAllegationId_Args__() throws Exception {
    final String actual = target.getAllegationId();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAllegationId_Args__String() throws Exception {
    String allegationId = null;
    target.setAllegationId(allegationId);
  }

  @Test
  public void getAllegationDisposition_Args__() throws Exception {
    Integer actual = target.getAllegationDisposition();
    Integer expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAllegationDisposition_Args__Integer() throws Exception {
    Integer allegationDisposition = null;
    target.setAllegationDisposition(allegationDisposition);
  }

  @Test
  public void getAllegationType_Args__() throws Exception {
    Integer actual = target.getAllegationType();
    Integer expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAllegationType_Args__Integer() throws Exception {
    Integer allegationType = null;
    target.setAllegationType(allegationType);
  }

  @Test
  public void getVictimId_Args__() throws Exception {
    final String actual = target.getVictimId();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setVictimId_Args__String() throws Exception {
    String victimId = null;
    target.setVictimId(victimId);
  }

  @Test
  public void getVictimFirstName_Args__() throws Exception {
    final String actual = target.getVictimFirstName();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setVictimFirstName_Args__String() throws Exception {
    String victimFirstName = null;
    target.setVictimFirstName(victimFirstName);
  }

  @Test
  public void getVictimLastName_Args__() throws Exception {
    final String actual = target.getVictimLastName();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setVictimLastName_Args__String() throws Exception {
    String victimLastName = null;
    target.setVictimLastName(victimLastName);
  }

  @Test
  public void getPerpetratorId_Args__() throws Exception {
    final String actual = target.getPerpetratorId();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setPerpetratorId_Args__String() throws Exception {
    String perpetratorId = null;
    target.setPerpetratorId(perpetratorId);
  }

  @Test
  public void getPerpetratorFirstName_Args__() throws Exception {
    final String actual = target.getPerpetratorFirstName();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setPerpetratorFirstName_Args__String() throws Exception {
    String perpetratorFirstName = null;
    target.setPerpetratorFirstName(perpetratorFirstName);
  }

  @Test
  public void getPerpetratorLastName_Args__() throws Exception {
    final String actual = target.getPerpetratorLastName();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setPerpetratorLastName_Args__String() throws Exception {
    String perpetratorLastName = null;
    target.setPerpetratorLastName(perpetratorLastName);
  }

  @Test
  public void getLimitedAccessCode_Args__() throws Exception {
    final String actual = target.getLimitedAccessCode();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLimitedAccessCode_Args__String() throws Exception {
    String limitedAccessCode = null;
    target.setLimitedAccessCode(limitedAccessCode);
  }

  @Test
  public void getLimitedAccessDate_Args__() throws Exception {
    Date actual = target.getLimitedAccessDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLimitedAccessDate_Args__Date() throws Exception {
    Date limitedAccessDate = new Date();
    target.setLimitedAccessDate(limitedAccessDate);
  }

  @Test
  public void getLimitedAccessDescription_Args__() throws Exception {
    final String actual = target.getLimitedAccessDescription();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLimitedAccessDescription_Args__String() throws Exception {
    String limitedAccessDescription = null;
    target.setLimitedAccessDescription(limitedAccessDescription);
  }

  @Test
  public void getLimitedAccessGovernmentEntityId_Args__() throws Exception {
    Integer actual = target.getLimitedAccessGovernmentEntityId();
    Integer expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLimitedAccessGovernmentEntityId_Args__Integer() throws Exception {
    Integer limitedAccessGovernmentEntityId = null;
    target.setLimitedAccessGovernmentEntityId(limitedAccessGovernmentEntityId);
  }

  @Test
  public void hashCode_Args__() throws Exception {
    int actual = target.hashCode();
    assertThat(actual, is(not(0)));
  }

  @Test
  public void extractAllegation_Args__ResultSet() throws Exception {
    EsPersonReferral actual = EsPersonReferral.extractAllegation(rs);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void extractAllegation_Args__ResultSet_T__SQLException() throws Exception {
    try {
      doThrow(new SQLException()).when(rs).getString(any());
      EsPersonReferral.extractAllegation(rs);
      fail("Expected exception was not thrown!");
    } catch (SQLException e) {
    }

  }

  @Test
  public void extractReferral_Args__ResultSet() throws Exception {
    EsPersonReferral actual = EsPersonReferral.extractReferral(rs);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void extractReferral_Args__ResultSet_T__SQLException() throws Exception {
    try {
      doThrow(new SQLException()).when(rs).getString(any());
      EsPersonReferral.extractReferral(rs);
      fail("Expected exception was not thrown!");
    } catch (SQLException e) {
    }

  }

  @Test
  public void mergeClientReferralInfo_Args__String__EsPersonReferral() throws Exception {
    String clientId = null;
    EsPersonReferral ref = new EsPersonReferral();
    target.mergeClientReferralInfo(clientId, ref);
  }

  @Test
  public void getReferralLastUpdated_Args__() throws Exception {
    Date actual = target.getReferralLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReferralLastUpdated_Args__Date() throws Exception {
    Date referralLastUpdated = new Date();
    target.setReferralLastUpdated(referralLastUpdated);
  }

  @Test
  public void getReporterLastUpdated_Args__() throws Exception {
    Date actual = target.getReporterLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReporterLastUpdated_Args__Date() throws Exception {
    Date reporterLastUpdated = new Date();
    target.setReporterLastUpdated(reporterLastUpdated);
  }

  @Test
  public void getWorkerLastUpdated_Args__() throws Exception {
    Date actual = target.getWorkerLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setWorkerLastUpdated_Args__Date() throws Exception {
    Date workerLastUpdated = new Date();
    target.setWorkerLastUpdated(workerLastUpdated);
  }

  @Test
  public void getAllegationLastUpdated_Args__() throws Exception {
    Date actual = target.getAllegationLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAllegationLastUpdated_Args__Date() throws Exception {
    Date allegationLastUpdated = new Date();
    target.setAllegationLastUpdated(allegationLastUpdated);
  }

  @Test
  public void getVictimLastUpdated_Args__() throws Exception {
    Date actual = target.getVictimLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setVictimLastUpdated_Args__Date() throws Exception {
    Date victimLastUpdated = new Date();
    target.setVictimLastUpdated(victimLastUpdated);
  }

  @Test
  public void getPerpetratorLastUpdated_Args__() throws Exception {
    Date actual = target.getPerpetratorLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setPerpetratorLastUpdated_Args__Date() throws Exception {
    Date perpetratorLastUpdated = new Date();
    target.setPerpetratorLastUpdated(perpetratorLastUpdated);
  }

  @Test
  public void getVictimSensitivityIndicator_Args__() throws Exception {
    final String actual = target.getVictimSensitivityIndicator();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setVictimSensitivityIndicator_Args__String() throws Exception {
    String victimSensitivityIndicator = null;
    target.setVictimSensitivityIndicator(victimSensitivityIndicator);
  }

  @Test
  public void getPerpetratorSensitivityIndicator_Args__() throws Exception {
    final String actual = target.getPerpetratorSensitivityIndicator();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setPerpetratorSensitivityIndicator_Args__String() throws Exception {
    String perpetratorSensitivityIndicator = null;
    target.setPerpetratorSensitivityIndicator(perpetratorSensitivityIndicator);
  }

  @Test
  public void getClientSensitivity_Args__() throws Exception {
    final String actual = target.getClientSensitivity();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClientSensitivity_Args__String() throws Exception {
    String clientSensitivity = null;
    target.setClientSensitivity(clientSensitivity);
  }

  @Test
  public void setOpts_Args__JobOptions() throws Exception {
    FlightPlan opts = mock(FlightPlan.class);
    EsPersonReferral.setOpts(opts);
  }

  @Test
  public void compare_Args__EsPersonReferral__EsPersonReferral() throws Exception {
    EsPersonReferral o1 = new EsPersonReferral();
    o1.setClientId(DEFAULT_CLIENT_ID);
    EsPersonReferral o2 = new EsPersonReferral();
    o2.setClientId(DEFAULT_CLIENT_ID);
    int actual = target.compare(o1, o2);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void compareTo_Args__EsPersonReferral() throws Exception {
    EsPersonReferral o1 = new EsPersonReferral();
    o1.setClientId(DEFAULT_CLIENT_ID);
    int actual = target.compareTo(o1);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void extractAllegation_A$ResultSet() throws Exception {
    EsPersonReferral actual = EsPersonReferral.extractAllegation(rs);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void extractAllegation_A$ResultSet_T$SQLException() throws Exception {
    try {
      when(rs.getString(any(String.class))).thenThrow(SQLException.class);
      EsPersonReferral.extractAllegation(rs);
      fail("Expected exception was not thrown!");
    } catch (SQLException e) {
    }

  }

  @Test
  public void extractReferral_A$ResultSet() throws Exception {
    EsPersonReferral actual = EsPersonReferral.extractReferral(rs);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void extractReferral_A$ResultSet_T$SQLException() throws Exception {
    try {
      when(rs.getString(any(String.class))).thenThrow(SQLException.class);
      EsPersonReferral.extractReferral(rs);
      fail("Expected exception was not thrown!");
    } catch (SQLException e) {
    }

  }

  @Test
  public void getNormalizationClass_A$() throws Exception {
    final Class<ReplicatedPersonReferrals> actual = target.getNormalizationClass();
    final Class<ReplicatedPersonReferrals> expected = ReplicatedPersonReferrals.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void mergeClientReferralInfo_A$String$EsPersonReferral() throws Exception {
    final String clientId = null;
    final EsPersonReferral ref = mock(EsPersonReferral.class);
    target.mergeClientReferralInfo(clientId, ref);
  }

  @Test
  public void normalizeAllegation_A$ReplicatedPersonReferrals$ElasticSearchPersonReferral()
      throws Exception {
    final ReplicatedPersonReferrals ret = new ReplicatedPersonReferrals();
    ret.setClientId(DEFAULT_CLIENT_ID);
    final ElasticSearchPersonReferral r = new ElasticSearchPersonReferral();
    r.setId(DEFAULT_CLIENT_ID);
    target.setAllegationReplicationOperation(CmsReplicationOperation.U);
    target.normalizeAllegation(ret, r);
  }

  @Test
  public void getNormalizationGroupKey_A$() throws Exception {
    final String actual = target.getNormalizationGroupKey();
    final String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPrimaryKey_A$() throws Exception {
    final Serializable actual = target.getPrimaryKey();
    final Serializable expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getWorkerId_A$() throws Exception {
    final String actual = target.getWorkerId();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setWorkerId_A$String() throws Exception {
    final String workerId = null;
    target.setWorkerId(workerId);
  }

  @Test
  public void getWorkerFirstName_A$() throws Exception {
    final String actual = target.getWorkerFirstName();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setWorkerFirstName_A$String() throws Exception {
    final String workerFirstName = null;
    target.setWorkerFirstName(workerFirstName);
  }

  @Test
  public void getWorkerLastName_A$() throws Exception {
    final String actual = target.getWorkerLastName();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setWorkerLastName_A$String() throws Exception {
    String workerLastName = null;
    target.setWorkerLastName(workerLastName);
  }

  @Test
  public void getWorkerLastUpdated_A$() throws Exception {
    Date actual = target.getWorkerLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setWorkerLastUpdated_A$Date() throws Exception {
    Date workerLastUpdated = new Date();
    target.setWorkerLastUpdated(workerLastUpdated);
  }

  @Test
  public void getClientSensitivity_A$() throws Exception {
    final String actual = target.getClientSensitivity();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClientSensitivity_A$String() throws Exception {
    String clientSensitivity = null;
    target.setClientSensitivity(clientSensitivity);
  }

  @Test
  public void setOpts_A$FlightPlan() throws Exception {
    FlightPlan opts = mock(FlightPlan.class);
    EsPersonReferral.setOpts(opts);
  }

  @Test
  public void toString_A$() throws Exception {
    final String actual = target.toString();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void compare_A$EsPersonReferral$EsPersonReferral() throws Exception {
    EsPersonReferral o1 = new EsPersonReferral();
    o1.setClientId(DEFAULT_CLIENT_ID);
    EsPersonReferral o2 = new EsPersonReferral();
    o2.setClientId(DEFAULT_CLIENT_ID);
    int actual = target.compare(o1, o2);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void compareTo_A$EsPersonReferral() throws Exception {
    EsPersonReferral o = new EsPersonReferral();
    o.setClientId(DEFAULT_CLIENT_ID);
    int actual = target.compareTo(o);
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void hashCode_A$() throws Exception {
    int actual = target.hashCode();
    assertThat(actual, is(not(0)));
  }

  @Test
  public void equals_A$Object() throws Exception {
    final Object obj = null;
    final boolean actual = target.equals(obj);
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLimitedAccessCode_A$() throws Exception {
    final String actual = target.getLimitedAccessCode();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLimitedAccessCode_A$String() throws Exception {
    final String limitedAccessCode = null;
    target.setLimitedAccessCode(limitedAccessCode);
  }

  @Test
  public void getLimitedAccessDate_A$() throws Exception {
    final Date actual = target.getLimitedAccessDate();
    final Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLimitedAccessDate_A$Date() throws Exception {
    final Date limitedAccessDate = new Date();
    target.setLimitedAccessDate(limitedAccessDate);
  }

  @Test
  public void getLimitedAccessDescription_A$() throws Exception {
    final String actual = target.getLimitedAccessDescription();
    final String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLimitedAccessDescription_A$String() throws Exception {
    final String limitedAccessDescription = null;
    target.setLimitedAccessDescription(limitedAccessDescription);
  }

  @Test
  public void getLimitedAccessGovernmentEntityId_A$() throws Exception {
    final Integer actual = target.getLimitedAccessGovernmentEntityId();
    final Integer expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLimitedAccessGovernmentEntityId_A$Integer() throws Exception {
    final Integer limitedAccessGovernmentEntityId = null;
    target.setLimitedAccessGovernmentEntityId(limitedAccessGovernmentEntityId);
  }

  @Test
  public void normalize_A$Map() throws Exception {
    final Map<Object, ReplicatedPersonReferrals> map =
        new HashMap<Object, ReplicatedPersonReferrals>();
    target.setClientId(DEFAULT_CLIENT_ID);
    target.setReferralId(DEFAULT_CLIENT_ID);
    final ReplicatedPersonReferrals actual = target.normalize(map);
    final ReplicatedPersonReferrals expected = new ReplicatedPersonReferrals(DEFAULT_CLIENT_ID);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLastChange_A$() throws Exception {
    Date actual = target.getLastChange();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLastChange_A$Date() throws Exception {
    Date lastChange = mock(Date.class);
    target.setLastChange(lastChange);
  }

  @Test
  public void getClientId_A$() throws Exception {
    String actual = target.getClientId();
    String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClientId_A$String() throws Exception {
    String clientId = null;
    target.setClientId(clientId);
  }

  @Test
  public void getReferralId_A$() throws Exception {
    String actual = target.getReferralId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReferralId_A$String() throws Exception {
    String referralId = null;
    target.setReferralId(referralId);
  }

  @Test
  public void getStartDate_A$() throws Exception {
    Date actual = target.getStartDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setStartDate_A$Date() throws Exception {
    Date startDate = mock(Date.class);
    target.setStartDate(startDate);
  }

  @Test
  public void getEndDate_A$() throws Exception {
    Date actual = target.getEndDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setEndDate_A$Date() throws Exception {
    Date endDate = mock(Date.class);
    target.setEndDate(endDate);
  }

  @Test
  public void getReferralResponseType_A$() throws Exception {
    Integer actual = target.getReferralResponseType();
    Integer expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReferralResponseType_A$Integer() throws Exception {
    Integer referralResponseType = null;
    target.setReferralResponseType(referralResponseType);
  }

  @Test
  public void getCounty_A$() throws Exception {
    Integer actual = target.getCounty();
    Integer expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setCounty_A$Integer() throws Exception {
    Integer county = null;
    target.setCounty(county);
  }

  @Test
  public void getReporterId_A$() throws Exception {
    String actual = target.getReporterId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReporterId_A$String() throws Exception {
    String reporterId = null;
    target.setReporterId(reporterId);
  }

  @Test
  public void getReporterFirstName_A$() throws Exception {
    String actual = target.getReporterFirstName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReporterFirstName_A$String() throws Exception {
    String reporterFirstName = null;
    target.setReporterFirstName(reporterFirstName);
  }

  @Test
  public void getReporterLastName_A$() throws Exception {
    String actual = target.getReporterLastName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReporterLastName_A$String() throws Exception {
    String reporterLastName = null;
    target.setReporterLastName(reporterLastName);
  }

  @Test
  public void getAllegationId_A$() throws Exception {
    String actual = target.getAllegationId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAllegationId_A$String() throws Exception {
    String allegationId = null;
    target.setAllegationId(allegationId);
  }

  @Test
  public void getAllegationDisposition_A$() throws Exception {
    Integer actual = target.getAllegationDisposition();
    Integer expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAllegationDisposition_A$Integer() throws Exception {
    Integer allegationDisposition = null;
    target.setAllegationDisposition(allegationDisposition);
  }

  @Test
  public void getAllegationType_A$() throws Exception {
    Integer actual = target.getAllegationType();
    Integer expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAllegationType_A$Integer() throws Exception {
    Integer allegationType = null;
    target.setAllegationType(allegationType);
  }

  @Test
  public void getVictimId_A$() throws Exception {
    String actual = target.getVictimId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setVictimId_A$String() throws Exception {
    String victimId = null;
    target.setVictimId(victimId);
  }

  @Test
  public void getVictimFirstName_A$() throws Exception {
    String actual = target.getVictimFirstName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setVictimFirstName_A$String() throws Exception {
    String victimFirstName = null;
    target.setVictimFirstName(victimFirstName);
  }

  @Test
  public void getVictimLastName_A$() throws Exception {
    String actual = target.getVictimLastName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setVictimLastName_A$String() throws Exception {
    String victimLastName = null;
    target.setVictimLastName(victimLastName);
  }

  @Test
  public void getPerpetratorId_A$() throws Exception {
    String actual = target.getPerpetratorId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setPerpetratorId_A$String() throws Exception {
    String perpetratorId = null;
    target.setPerpetratorId(perpetratorId);
  }

  @Test
  public void getPerpetratorFirstName_A$() throws Exception {
    String actual = target.getPerpetratorFirstName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setPerpetratorFirstName_A$String() throws Exception {
    String perpetratorFirstName = null;
    target.setPerpetratorFirstName(perpetratorFirstName);
  }

  @Test
  public void getPerpetratorLastName_A$() throws Exception {
    String actual = target.getPerpetratorLastName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setPerpetratorLastName_A$String() throws Exception {
    String perpetratorLastName = null;
    target.setPerpetratorLastName(perpetratorLastName);
  }

  @Test
  public void getReferralLastUpdated_A$() throws Exception {
    Date actual = target.getReferralLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReferralLastUpdated_A$Date() throws Exception {
    Date referralLastUpdated = mock(Date.class);
    target.setReferralLastUpdated(referralLastUpdated);
  }

  @Test
  public void getReporterLastUpdated_A$() throws Exception {
    Date actual = target.getReporterLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReporterLastUpdated_A$Date() throws Exception {
    Date reporterLastUpdated = mock(Date.class);
    target.setReporterLastUpdated(reporterLastUpdated);
  }

  @Test
  public void getAllegationLastUpdated_A$() throws Exception {
    Date actual = target.getAllegationLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAllegationLastUpdated_A$Date() throws Exception {
    Date allegationLastUpdated = mock(Date.class);
    target.setAllegationLastUpdated(allegationLastUpdated);
  }

  @Test
  public void getVictimLastUpdated_A$() throws Exception {
    Date actual = target.getVictimLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setVictimLastUpdated_A$Date() throws Exception {
    Date victimLastUpdated = mock(Date.class);
    target.setVictimLastUpdated(victimLastUpdated);
  }

  @Test
  public void getPerpetratorLastUpdated_A$() throws Exception {
    Date actual = target.getPerpetratorLastUpdated();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setPerpetratorLastUpdated_A$Date() throws Exception {
    Date perpetratorLastUpdated = mock(Date.class);
    target.setPerpetratorLastUpdated(perpetratorLastUpdated);
  }

  @Test
  public void getVictimSensitivityIndicator_A$() throws Exception {
    String actual = target.getVictimSensitivityIndicator();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setVictimSensitivityIndicator_A$String() throws Exception {
    String victimSensitivityIndicator = null;
    target.setVictimSensitivityIndicator(victimSensitivityIndicator);
  }

  @Test
  public void getPerpetratorSensitivityIndicator_A$() throws Exception {
    String actual = target.getPerpetratorSensitivityIndicator();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setPerpetratorSensitivityIndicator_A$String() throws Exception {
    String perpetratorSensitivityIndicator = null;
    target.setPerpetratorSensitivityIndicator(perpetratorSensitivityIndicator);
  }

  @Test
  public void getAccessLimitation_A$() throws Exception {
    EmbeddableAccessLimitation actual = target.getAccessLimitation();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setAccessLimitation_A$EmbeddableAccessLimitation() throws Exception {
    EmbeddableAccessLimitation accessLimitation = mock(EmbeddableAccessLimitation.class);
    target.setAccessLimitation(accessLimitation);
  }

  @Test
  public void getWorker_A$() throws Exception {
    EmbeddableStaffWorker actual = target.getWorker();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setWorker_A$EmbeddableStaffWorker() throws Exception {
    EmbeddableStaffWorker worker = mock(EmbeddableStaffWorker.class);
    target.setWorker(worker);
  }

  @Test
  public void getReferralReplicationOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getReferralReplicationOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReferralReplicationOperation_A$CmsReplicationOperation() throws Exception {
    CmsReplicationOperation referralReplicationOperation = CmsReplicationOperation.U;
    target.setReferralReplicationOperation(referralReplicationOperation);
  }

  @Test
  public void getReferralClientReplicationOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getReferralClientReplicationOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReferralClientReplicationOperation_A$CmsReplicationOperation() throws Exception {
    CmsReplicationOperation referralClientReplicationOperation = CmsReplicationOperation.U;
    target.setReferralClientReplicationOperation(referralClientReplicationOperation);
  }

  @Test
  public void getAllegationReplicationOperation_A$() throws Exception {
    CmsReplicationOperation actual = target.getAllegationReplicationOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setAllegationReplicationOperation_A$CmsReplicationOperation() throws Exception {
    CmsReplicationOperation allegationReplicationOperation = CmsReplicationOperation.U;
    target.setAllegationReplicationOperation(allegationReplicationOperation);
  }

}
