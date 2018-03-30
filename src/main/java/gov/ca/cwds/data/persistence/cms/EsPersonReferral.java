package gov.ca.cwds.data.persistence.cms;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.annotations.Type;

import gov.ca.cwds.data.es.ElasticSearchAccessLimitation;
import gov.ca.cwds.data.es.ElasticSearchPerson.ElasticSearchPersonSocialWorker;
import gov.ca.cwds.data.es.ElasticSearchPersonAllegation;
import gov.ca.cwds.data.es.ElasticSearchPersonNestedPerson;
import gov.ca.cwds.data.es.ElasticSearchPersonReferral;
import gov.ca.cwds.data.es.ElasticSearchPersonReporter;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.EmbeddableAccessLimitation;
import gov.ca.cwds.data.persistence.cms.rep.EmbeddableStaffWorker;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.atom.AtomDocumentSecurity;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.DomainChef;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;

/**
 * Entity bean for view VW_LST_REFERRAL_HIST.
 * 
 * <p>
 * Implements {@link ApiGroupNormalizer} and converts to {@link ReplicatedPersonReferrals}.
 * </p>
 * 
 * @author CWDS API Team
 */
@Entity
@Table(name = "VW_LST_REFERRAL_HIST")
//@formatter:off
@NamedNativeQuery(name = "gov.ca.cwds.data.persistence.cms.EsPersonReferral.findAllUpdatedAfter",
    query = "SELECT DISTINCT " 
        + EsPersonReferral.COLUMNS
        + " FROM {h-schema}VW_LST_REFERRAL_HIST r \n"
        + "WHERE (1=1 OR CURRENT TIMESTAMP < :after) \n"
        + "ORDER BY CLIENT_ID, REFERRAL_ID, ALLEGATION_ID, VICTIM_ID \n"
        + "WITH UR ",
    resultClass = EsPersonReferral.class, readOnly = true)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.EsPersonReferral.findAllUpdatedAfterWithUnlimitedAccess",
    query = "SELECT DISTINCT " 
        + EsPersonReferral.COLUMNS
        + " FROM {h-schema}VW_LST_REFERRAL_HIST r \n" 
        + "WHERE (1=1 OR CURRENT TIMESTAMP < :after) \n"
        + "AND r.LIMITED_ACCESS_CODE = 'N' \n"
        + "ORDER BY CLIENT_ID, REFERRAL_ID, ALLEGATION_ID, VICTIM_ID \n"
        + "WITH UR ",
    resultClass = EsPersonReferral.class, readOnly = true)
//@formatter:on
public class EsPersonReferral
    implements PersistentObject, ApiGroupNormalizer<ReplicatedPersonReferrals>,
    Comparable<EsPersonReferral>, Comparator<EsPersonReferral> {

  //@formatter:off
  protected static final String COLUMNS =
        "r.CLIENT_ID,r.CLIENT_SENSITIVITY_IND,r.REFERRAL_ID,r.START_DATE,r.END_DATE,r.REFERRAL_RESPONSE_TYPE,"
      + "r.LIMITED_ACCESS_CODE,r.LIMITED_ACCESS_DATE,r.LIMITED_ACCESS_DESCRIPTION,r.LIMITED_ACCESS_GOVERNMENT_ENT,"
      + "r.REFERRAL_LAST_UPDATED,r.REFERRAL_COUNTY,"
      + "r.REPORTER_ID,r.REPORTER_FIRST_NM,r.REPORTER_LAST_NM,r.REPORTER_LAST_UPDATED,"
      + "r.WORKER_ID,r.WORKER_FIRST_NM,r.WORKER_LAST_NM,r.WORKER_LAST_UPDATED,"
      + "r.PERPETRATOR_ID,r.PERPETRATOR_SENSITIVITY_IND,r.PERPETRATOR_FIRST_NM,r.PERPETRATOR_LAST_NM,r.PERPETRATOR_LAST_UPDATED,"
      + "r.VICTIM_ID,r.VICTIM_SENSITIVITY_IND,r.VICTIM_FIRST_NM,r.VICTIM_LAST_NM,r.VICTIM_LAST_UPDATED,"
      + "r.ALLEGATION_ID,r.ALLEGATION_DISPOSITION,r.ALLEGATION_TYPE,r.ALLEGATION_LAST_UPDATED,"
      + "r.RCT_IBMSNAP_LOGMARKER,r.RCT_IBMSNAP_OPERATION,r.RFL_IBMSNAP_LOGMARKER,r.RFL_IBMSNAP_OPERATION,"
      + "r.STP_IBMSNAP_LOGMARKER,r.STP_IBMSNAP_OPERATION,r.RPT_IBMSNAP_LOGMARKER,r.RPT_IBMSNAP_OPERATION,"
      + "r.ALG_IBMSNAP_LOGMARKER,r.ALG_IBMSNAP_OPERATION,r.CLP_IBMSNAP_LOGMARKER,r.CLP_IBMSNAP_OPERATION,"
      + "r.CLV_IBMSNAP_LOGMARKER,r.CLV_IBMSNAP_OPERATION,r.LAST_CHG";
  //@formatter:on

  private static final long serialVersionUID = -2265057057202257108L;

  private static final String COLUMN_REFERRAL_ID = "REFERRAL_ID";

  private static FlightPlan opts; // WARNING: not a good idea. Try another way.

  @Type(type = "timestamp")
  @Column(name = "LAST_CHG", updatable = false)
  private Date lastChange;

  // ===================
  // KEYS:
  // ===================

  @Id
  @Column(name = "CLIENT_ID")
  private String clientId;

  @Transient
  private transient String clientSensitivity;

  @Id
  @Column(name = COLUMN_REFERRAL_ID)
  private String referralId;

  // ===================
  // REFERRAL:
  // ===================

  @Column(name = "START_DATE")
  @Type(type = "date")
  private Date startDate;

  @Column(name = "END_DATE")
  @Type(type = "date")
  private Date endDate;

  @Column(name = "REFERRAL_RESPONSE_TYPE")
  @Type(type = "integer")
  private Integer referralResponseType;

  @Column(name = "REFERRAL_COUNTY")
  @Type(type = "integer")
  private Integer county;

  @Column(name = "REFERRAL_LAST_UPDATED")
  @Type(type = "timestamp")
  private Date referralLastUpdated;

  // =================
  // REPORTER:
  // =================

  @Column(name = "REPORTER_ID")
  private String reporterId;

  @Column(name = "REPORTER_FIRST_NM")
  @ColumnTransformer(read = "trim(REPORTER_FIRST_NM)")
  private String reporterFirstName;

  @Column(name = "REPORTER_LAST_NM")
  @ColumnTransformer(read = "trim(REPORTER_LAST_NM)")
  private String reporterLastName;

  @Column(name = "REPORTER_LAST_UPDATED")
  @Type(type = "timestamp")
  private Date reporterLastUpdated;

  // =====================
  // SOCIAL WORKER:
  // =====================

  private EmbeddableStaffWorker worker = new EmbeddableStaffWorker();

  // =====================
  // ALLEGATION:
  // =====================

  @Id
  @Column(name = "ALLEGATION_ID")
  private String allegationId;

  @Column(name = "ALLEGATION_DISPOSITION")
  @Type(type = "integer")
  private Integer allegationDisposition;

  @Column(name = "ALLEGATION_TYPE")
  @Type(type = "integer")
  private Integer allegationType;

  @Column(name = "ALLEGATION_LAST_UPDATED")
  @Type(type = "timestamp")
  private Date allegationLastUpdated;

  // =====================
  // VICTIM:
  // =====================

  @Id
  @Column(name = "VICTIM_ID")
  private String victimId;

  @Column(name = "VICTIM_FIRST_NM")
  @ColumnTransformer(read = "trim(VICTIM_FIRST_NM)")
  private String victimFirstName;

  @Column(name = "VICTIM_LAST_NM")
  @ColumnTransformer(read = "trim(VICTIM_LAST_NM)")
  private String victimLastName;

  @Column(name = "VICTIM_LAST_UPDATED")
  @Type(type = "timestamp")
  private Date victimLastUpdated;

  @Column(name = "VICTIM_SENSITIVITY_IND")
  private String victimSensitivityIndicator;

  // =====================
  // ACCESS LIMITATION:
  // =====================

  private EmbeddableAccessLimitation accessLimitation = new EmbeddableAccessLimitation();

  // =====================
  // PERPETRATOR:
  // =====================

  @Column(name = "PERPETRATOR_ID")
  private String perpetratorId;

  @Column(name = "PERPETRATOR_FIRST_NM")
  @ColumnTransformer(read = "trim(PERPETRATOR_FIRST_NM)")
  private String perpetratorFirstName;

  @Column(name = "PERPETRATOR_LAST_NM")
  @ColumnTransformer(read = "trim(PERPETRATOR_LAST_NM)")
  private String perpetratorLastName;

  @Column(name = "PERPETRATOR_LAST_UPDATED")
  @Type(type = "timestamp")
  private Date perpetratorLastUpdated;

  @Column(name = "PERPETRATOR_SENSITIVITY_IND")
  private String perpetratorSensitivityIndicator;

  // =====================
  // REPLICATION:
  // =====================

  @Enumerated(EnumType.STRING)
  @Column(name = "RFL_IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation referralReplicationOperation;

  @Enumerated(EnumType.STRING)
  @Column(name = "RCT_IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation referralClientReplicationOperation;

  @Enumerated(EnumType.STRING)
  @Column(name = "ALG_IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation allegationReplicationOperation;

  // =====================
  // CONSTRUCTOR:
  // =====================

  public EsPersonReferral() {
    // Default no-op.
  }

  /**
   * Construct from incoming result set.
   * 
   * @param rs result set
   * @throws SQLException on database read error
   */
  public EsPersonReferral(final ResultSet rs) throws SQLException {
    this.referralId = ifNull(rs.getString(COLUMN_REFERRAL_ID));
    this.startDate = rs.getDate("START_DATE");
    this.endDate = rs.getDate("END_DATE");
    this.referralResponseType = rs.getInt("REFERRAL_RESPONSE_TYPE");

    this.accessLimitation.accept(rs);
    this.referralLastUpdated = rs.getTimestamp("REFERRAL_LAST_UPDATED");

    this.allegationId = ifNull(rs.getString("ALLEGATION_ID"));
    this.allegationType = rs.getInt("ALLEGATION_TYPE");
    this.allegationDisposition = rs.getInt("ALLEGATION_DISPOSITION");
    this.allegationLastUpdated = rs.getTimestamp("ALLEGATION_LAST_UPDATED");

    this.perpetratorId = ifNull(rs.getString("PERPETRATOR_ID"));
    this.perpetratorFirstName = ifNull(rs.getString("PERPETRATOR_FIRST_NM"));
    this.perpetratorLastName = ifNull(rs.getString("PERPETRATOR_LAST_NM"));
    this.perpetratorLastUpdated = rs.getTimestamp("PERPETRATOR_LAST_UPDATED");
    this.perpetratorSensitivityIndicator = rs.getString("PERPETRATOR_SENSITIVITY_IND");

    this.reporterId = ifNull(rs.getString("REPORTER_ID"));
    this.reporterFirstName = ifNull(rs.getString("REPORTER_FIRST_NM"));
    this.reporterLastName = ifNull(rs.getString("REPORTER_LAST_NM"));
    this.reporterLastUpdated = rs.getTimestamp("REPORTER_LAST_UPDATED");

    this.victimId = ifNull(rs.getString("VICTIM_ID"));
    this.victimFirstName = ifNull(rs.getString("VICTIM_FIRST_NM"));
    this.victimLastName = ifNull(rs.getString("VICTIM_LAST_NM"));
    this.victimLastUpdated = rs.getTimestamp("VICTIM_LAST_UPDATED");
    this.victimSensitivityIndicator = rs.getString("VICTIM_SENSITIVITY_IND");

    this.worker.setWorkerId(ifNull(rs.getString("WORKER_ID")));
    this.worker.setWorkerFirstName(ifNull(rs.getString("WORKER_FIRST_NM")));
    this.worker.setWorkerLastName(ifNull(rs.getString("WORKER_LAST_NM")));
    this.worker.setWorkerLastUpdated(rs.getTimestamp("WORKER_LAST_UPDATED"));

    this.county = rs.getInt("REFERRAL_COUNTY");
    this.lastChange = rs.getDate("LAST_CHG");

    this.referralReplicationOperation =
        CmsReplicationOperation.strToRepOp(rs.getString("RFL_IBMSNAP_OPERATION"));
    this.referralClientReplicationOperation =
        CmsReplicationOperation.strToRepOp(rs.getString("RCT_IBMSNAP_OPERATION"));
    this.allegationReplicationOperation =
        CmsReplicationOperation.strToRepOp(rs.getString("ALG_IBMSNAP_OPERATION"));
  }

  /**
   * Extract allegation. Reuses this class to extract only those fields required for Allegations.
   * 
   * @param rs allegation result set
   * @return allegation side of referral
   * @throws SQLException database error
   */
  public static EsPersonReferral extractAllegation(final ResultSet rs) throws SQLException {
    final EsPersonReferral ret = new EsPersonReferral();

    ret.referralId = ifNull(rs.getString(COLUMN_REFERRAL_ID));
    ret.allegationId = ifNull(rs.getString("ALLEGATION_ID"));
    ret.allegationType = rs.getInt("ALLEGATION_TYPE");
    ret.allegationDisposition = rs.getInt("ALLEGATION_DISPOSITION");
    ret.allegationLastUpdated = rs.getTimestamp("ALLEGATION_LAST_UPDATED");

    ret.perpetratorId = ifNull(rs.getString("PERPETRATOR_ID"));
    ret.perpetratorFirstName = ifNull(rs.getString("PERPETRATOR_FIRST_NM"));
    ret.perpetratorLastName = ifNull(rs.getString("PERPETRATOR_LAST_NM"));
    ret.perpetratorLastUpdated = rs.getTimestamp("PERPETRATOR_LAST_UPDATED");
    ret.perpetratorSensitivityIndicator = rs.getString("PERPETRATOR_SENSITIVITY_IND");

    ret.victimId = ifNull(rs.getString("VICTIM_ID"));
    ret.victimFirstName = ifNull(rs.getString("VICTIM_FIRST_NM"));
    ret.victimLastName = ifNull(rs.getString("VICTIM_LAST_NM"));
    ret.victimLastUpdated = rs.getTimestamp("VICTIM_LAST_UPDATED");
    ret.victimSensitivityIndicator = rs.getString("VICTIM_SENSITIVITY_IND");

    ret.setAllegationReplicationOperation(
        CmsReplicationOperation.strToRepOp(rs.getString("ALG_IBMSNAP_OPERATION")));

    return ret;
  }

  /**
   * Extract referral. Reuses this class to extract only those fields required for Allegations.
   * 
   * <p>
   * IBM strongly recommends retrieving column results by position and not by column name.
   * </p>
   * 
   * @param rs referral result set
   * @return parent referral element
   * @throws SQLException on DB error
   */
  public static EsPersonReferral extractReferral(final ResultSet rs) throws SQLException {
    final EsPersonReferral ret = new EsPersonReferral();

    ret.referralId = ifNull(rs.getString(COLUMN_REFERRAL_ID));
    ret.startDate = rs.getDate("START_DATE");
    ret.endDate = rs.getDate("END_DATE");
    ret.referralResponseType = rs.getInt("REFERRAL_RESPONSE_TYPE");

    ret.accessLimitation.accept(rs);
    ret.referralLastUpdated = rs.getTimestamp("REFERRAL_LAST_UPDATED");

    ret.reporterId = ifNull(rs.getString("REPORTER_ID"));
    ret.reporterFirstName = ifNull(rs.getString("REPORTER_FIRST_NM"));
    ret.reporterLastName = ifNull(rs.getString("REPORTER_LAST_NM"));
    ret.reporterLastUpdated = rs.getTimestamp("REPORTER_LAST_UPDATED");

    ret.worker.setWorkerId(ifNull(rs.getString("WORKER_ID")));
    ret.worker.setWorkerFirstName(ifNull(rs.getString("WORKER_FIRST_NM")));
    ret.worker.setWorkerLastName(ifNull(rs.getString("WORKER_LAST_NM")));
    ret.worker.setWorkerLastUpdated(rs.getTimestamp("WORKER_LAST_UPDATED"));

    ret.county = rs.getInt("REFERRAL_COUNTY");
    ret.lastChange = rs.getDate("LAST_CHG");

    ret.setReferralReplicationOperation(
        CmsReplicationOperation.strToRepOp(rs.getString("RFL_IBMSNAP_OPERATION")));

    return ret;
  }

  @Override
  public Class<ReplicatedPersonReferrals> getNormalizationClass() {
    return ReplicatedPersonReferrals.class;
  }

  /**
   * Merge common Referral fields into this Allegation before normalizing. Very silly, yes, but it
   * works with existing code without refactoring.
   * 
   * @param clientId target client id
   * @param ref parent referral to merge
   */
  public void mergeClientReferralInfo(String clientId, EsPersonReferral ref) {
    this.clientId = clientId;
    this.referralId = ref.referralId;
    this.county = ref.county;
    this.startDate = ref.startDate;
    this.endDate = ref.endDate;
    this.referralResponseType = ref.referralResponseType;
    this.referralLastUpdated = ref.referralLastUpdated;
    this.accessLimitation = ref.accessLimitation;
    this.reporterFirstName = ifNull(ref.reporterFirstName);
    this.reporterId = ifNull(ref.reporterId);
    this.reporterLastName = ifNull(ref.reporterLastName);
    this.reporterLastUpdated = ref.reporterLastUpdated;

    this.worker.setWorkerFirstName(ifNull(ref.getWorkerFirstName()));
    this.worker.setWorkerId(ref.getWorkerId());
    this.worker.setWorkerLastName(ifNull(ref.getWorkerLastName()));
    this.worker.setWorkerLastUpdated(ref.getWorkerLastUpdated());

    if (ref.referralClientReplicationOperation != null) {
      this.referralReplicationOperation = ref.referralClientReplicationOperation;
    }
  }

  private ElasticSearchPersonReporter makeReporter() {
    final ElasticSearchPersonReporter ret = new ElasticSearchPersonReporter();
    ret.setId(this.reporterId);
    ret.setLegacyClientId(this.reporterId);
    ret.setFirstName(ifNull(this.reporterFirstName));
    ret.setLastName(ifNull(this.reporterLastName));
    ret.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.reporterId,
        this.reporterLastUpdated, LegacyTable.REPORTER));
    return ret;
  }

  private ElasticSearchPersonSocialWorker makeAssignedWorker() {
    final ElasticSearchPersonSocialWorker ret = new ElasticSearchPersonSocialWorker();

    final String id = this.getWorkerId();
    ret.setId(id);
    ret.setLegacyClientId(id);
    ret.setFirstName(ifNull(this.getWorkerFirstName()));
    ret.setLastName(ifNull(this.getWorkerLastName()));
    ret.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.getWorkerId(),
        this.getWorkerLastUpdated(), LegacyTable.STAFF_PERSON));
    return ret;
  }

  private ElasticSearchAccessLimitation makeAccessLimitation() {
    final ElasticSearchAccessLimitation ret = new ElasticSearchAccessLimitation();
    ret.setLimitedAccessCode(this.getLimitedAccessCode());
    ret.setLimitedAccessDate(DomainChef.cookDate(this.getLimitedAccessDate()));
    ret.setLimitedAccessDescription(ifNull(this.getLimitedAccessDescription()));
    ret.setLimitedAccessGovernmentEntityId(this.getLimitedAccessGovernmentEntityId() == null ? null
        : this.getLimitedAccessGovernmentEntityId().toString());
    ret.setLimitedAccessGovernmentEntityName(SystemCodeCache.global()
        .getSystemCodeShortDescription(this.getLimitedAccessGovernmentEntityId()));
    return ret;
  }

  private ElasticSearchPersonAllegation makeAllegation() {
    final ElasticSearchPersonAllegation ret = new ElasticSearchPersonAllegation();
    ret.setId(this.allegationId);
    ret.setLegacyId(this.allegationId);
    ret.setAllegationDescription(
        SystemCodeCache.global().getSystemCodeShortDescription(this.allegationType));
    ret.setDispositionId(
        this.allegationDisposition == null ? null : this.allegationDisposition.toString());
    ret.setDispositionDescription(
        SystemCodeCache.global().getSystemCodeShortDescription(this.allegationDisposition));
    ret.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.allegationId,
        this.allegationLastUpdated, LegacyTable.ALLEGATION));
    return ret;
  }

  private ElasticSearchPersonNestedPerson makePerpetrator() {
    final ElasticSearchPersonNestedPerson perpetrator = new ElasticSearchPersonNestedPerson();
    perpetrator.setId(this.perpetratorId);
    perpetrator.setFirstName(ifNull(this.perpetratorFirstName));
    perpetrator.setLastName(ifNull(this.perpetratorLastName));
    perpetrator.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.perpetratorId,
        this.perpetratorLastUpdated, LegacyTable.CLIENT));
    perpetrator.setSensitivityIndicator(perpetratorSensitivityIndicator);
    return perpetrator;
  }

  private ElasticSearchPersonNestedPerson makeVictim() {
    final ElasticSearchPersonNestedPerson victim = new ElasticSearchPersonNestedPerson();
    victim.setId(this.victimId);
    victim.setFirstName(ifNull(this.victimFirstName));
    victim.setLastName(ifNull(this.victimLastName));
    victim.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.victimId,
        this.victimLastUpdated, LegacyTable.CLIENT));
    victim.setSensitivityIndicator(victimSensitivityIndicator);
    return victim;
  }

  protected void normalizeAllegation(ReplicatedPersonReferrals ret, ElasticSearchPersonReferral r) {
    // A referral may have multiple allegations.
    if (this.allegationReplicationOperation != null
        && this.allegationReplicationOperation != CmsReplicationOperation.D) {
      final ElasticSearchPersonAllegation allegation = makeAllegation();

      if (AtomDocumentSecurity.isNotSealedSensitive(opts, perpetratorSensitivityIndicator)) {
        allegation.setPerpetrator(makePerpetrator());

        // NOTE: #148091785: deprecated person fields.
        allegation.setPerpetratorId(this.perpetratorId);
        allegation.setPerpetratorLegacyClientId(this.perpetratorId);
        allegation.setPerpetratorFirstName(ifNull(this.perpetratorFirstName));
        allegation.setPerpetratorLastName(ifNull(this.perpetratorLastName));
      }

      if (AtomDocumentSecurity.isNotSealedSensitive(opts, victimSensitivityIndicator)) {
        allegation.setVictim(makeVictim());

        // NOTE: #148091785: deprecated person fields.
        allegation.setVictimId(this.victimId);
        allegation.setVictimLegacyClientId(this.victimId);
        allegation.setVictimFirstName(ifNull(this.victimFirstName));
        allegation.setVictimLastName(ifNull(this.victimLastName));
      }

      ret.addReferral(r, allegation);
    }
  }

  @Override
  public ReplicatedPersonReferrals normalize(Map<Object, ReplicatedPersonReferrals> map) {
    ReplicatedPersonReferrals ret = map.get(this.clientId);
    if (ret == null) {
      ret = new ReplicatedPersonReferrals(this.clientId);
      map.put(this.clientId, ret);
    }

    // Pivotal #152932457: **Snapshot** person ES has HOI but no referrals in legacy.
    // Remove deleted referrals elements by overwriting them.
    if (this.referralReplicationOperation != CmsReplicationOperation.D) {
      final boolean isNewReferral = !ret.hasReferral(this.referralId);
      final ElasticSearchPersonReferral r =
          !isNewReferral ? ret.getReferral(referralId) : new ElasticSearchPersonReferral();

      if (isNewReferral) {
        r.setId(this.referralId);
        r.setLegacyId(this.referralId);
        r.setLegacyLastUpdated(DomainChef.cookStrictTimestamp(this.referralLastUpdated));
        r.setStartDate(DomainChef.cookDate(this.startDate));
        r.setEndDate(DomainChef.cookDate(this.endDate));
        r.setCountyId(this.county == null ? null : this.county.toString());
        r.setCountyName(SystemCodeCache.global().getSystemCodeShortDescription(this.county));
        r.setResponseTimeId(
            this.referralResponseType == null ? null : this.referralResponseType.toString());
        r.setResponseTime(
            SystemCodeCache.global().getSystemCodeShortDescription(this.referralResponseType));
        r.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.referralId,
            this.referralLastUpdated, LegacyTable.REFERRAL));

        r.setReporter(makeReporter());
        r.setAssignedSocialWorker(makeAssignedWorker());
        r.setAccessLimitation(makeAccessLimitation());
      }

      // A referral may have multiple allegations.
      normalizeAllegation(ret, r);
    }

    return ret;
  }

  @Override
  public String getNormalizationGroupKey() {
    return this.clientId;
  }

  @Override
  public Serializable getPrimaryKey() {
    return null;
  }

  /**
   * Getter for composite "last change", the latest time that any associated record was created or
   * updated.
   * 
   * @return last change date
   */
  public Date getLastChange() {
    return NeutronDateUtils.freshDate(lastChange);
  }

  /**
   * Setter for composite "last change", the latest time that any associated record was created or
   * updated.
   * 
   * @param lastChange last change date
   */
  public void setLastChange(Date lastChange) {
    this.lastChange = NeutronDateUtils.freshDate(lastChange);
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getReferralId() {
    return referralId;
  }

  public void setReferralId(String referralId) {
    this.referralId = referralId;
  }

  public Date getStartDate() {
    return NeutronDateUtils.freshDate(startDate);
  }

  public void setStartDate(Date startDate) {
    this.startDate = NeutronDateUtils.freshDate(startDate);
  }

  public Date getEndDate() {
    return NeutronDateUtils.freshDate(endDate);
  }

  public void setEndDate(Date endDate) {
    this.endDate = NeutronDateUtils.freshDate(endDate);
  }

  public Integer getReferralResponseType() {
    return referralResponseType;
  }

  public void setReferralResponseType(Integer referralResponseType) {
    this.referralResponseType = referralResponseType;
  }

  public Integer getCounty() {
    return county;
  }

  public void setCounty(Integer county) {
    this.county = county;
  }

  public String getReporterId() {
    return reporterId;
  }

  public void setReporterId(String reporterId) {
    this.reporterId = reporterId;
  }

  public String getReporterFirstName() {
    return reporterFirstName;
  }

  public void setReporterFirstName(String reporterFirstName) {
    this.reporterFirstName = reporterFirstName;
  }

  public String getReporterLastName() {
    return reporterLastName;
  }

  public void setReporterLastName(String reporterLastName) {
    this.reporterLastName = reporterLastName;
  }

  public String getWorkerId() {
    return this.worker != null ? this.worker.getWorkerId() : null;
  }

  public void setWorkerId(String workerId) {
    this.worker.setWorkerId(workerId);
  }

  public String getWorkerFirstName() {
    return this.worker != null ? this.worker.getWorkerFirstName() : null;
  }

  public void setWorkerFirstName(String workerFirstName) {
    this.worker.setWorkerFirstName(workerFirstName);
  }

  public String getWorkerLastName() {
    return this.worker != null ? this.worker.getWorkerLastName() : null;
  }

  public void setWorkerLastName(String workerLastName) {
    this.worker.setWorkerLastName(workerLastName);
  }

  public String getAllegationId() {
    return allegationId;
  }

  public void setAllegationId(String allegationId) {
    this.allegationId = allegationId;
  }

  public Integer getAllegationDisposition() {
    return allegationDisposition;
  }

  public void setAllegationDisposition(Integer allegationDisposition) {
    this.allegationDisposition = allegationDisposition;
  }

  public Integer getAllegationType() {
    return allegationType;
  }

  public void setAllegationType(Integer allegationType) {
    this.allegationType = allegationType;
  }

  public String getVictimId() {
    return victimId;
  }

  public void setVictimId(String victimId) {
    this.victimId = victimId;
  }

  public String getVictimFirstName() {
    return victimFirstName;
  }

  public void setVictimFirstName(String victimFirstName) {
    this.victimFirstName = victimFirstName;
  }

  public String getVictimLastName() {
    return victimLastName;
  }

  public void setVictimLastName(String victimLastName) {
    this.victimLastName = victimLastName;
  }

  public String getPerpetratorId() {
    return perpetratorId;
  }

  public void setPerpetratorId(String perpetratorId) {
    this.perpetratorId = perpetratorId;
  }

  public String getPerpetratorFirstName() {
    return perpetratorFirstName;
  }

  public void setPerpetratorFirstName(String perpetratorFirstName) {
    this.perpetratorFirstName = perpetratorFirstName;
  }

  public String getPerpetratorLastName() {
    return perpetratorLastName;
  }

  public void setPerpetratorLastName(String perpetratorLastName) {
    this.perpetratorLastName = perpetratorLastName;
  }

  public Date getReferralLastUpdated() {
    return NeutronDateUtils.freshDate(referralLastUpdated);
  }

  public void setReferralLastUpdated(Date referralLastUpdated) {
    this.referralLastUpdated = NeutronDateUtils.freshDate(referralLastUpdated);
  }

  public Date getReporterLastUpdated() {
    return NeutronDateUtils.freshDate(reporterLastUpdated);
  }

  public void setReporterLastUpdated(Date reporterLastUpdated) {
    this.reporterLastUpdated = NeutronDateUtils.freshDate(reporterLastUpdated);
  }

  public Date getWorkerLastUpdated() {
    return this.worker != null ? NeutronDateUtils.freshDate(this.worker.getWorkerLastUpdated())
        : null;
  }

  public void setWorkerLastUpdated(Date workerLastUpdated) {
    this.worker.setWorkerLastUpdated(NeutronDateUtils.freshDate(workerLastUpdated));
  }

  public Date getAllegationLastUpdated() {
    return NeutronDateUtils.freshDate(allegationLastUpdated);
  }

  public void setAllegationLastUpdated(Date allegationLastUpdated) {
    this.allegationLastUpdated = NeutronDateUtils.freshDate(allegationLastUpdated);
  }

  public Date getVictimLastUpdated() {
    return NeutronDateUtils.freshDate(victimLastUpdated);
  }

  public void setVictimLastUpdated(Date victimLastUpdated) {
    this.victimLastUpdated = NeutronDateUtils.freshDate(victimLastUpdated);
  }

  public Date getPerpetratorLastUpdated() {
    return NeutronDateUtils.freshDate(perpetratorLastUpdated);
  }

  public void setPerpetratorLastUpdated(Date perpetratorLastUpdated) {
    this.perpetratorLastUpdated = NeutronDateUtils.freshDate(perpetratorLastUpdated);
  }

  public String getVictimSensitivityIndicator() {
    return victimSensitivityIndicator;
  }

  public void setVictimSensitivityIndicator(String victimSensitivityIndicator) {
    this.victimSensitivityIndicator = victimSensitivityIndicator;
  }

  public String getPerpetratorSensitivityIndicator() {
    return perpetratorSensitivityIndicator;
  }

  public void setPerpetratorSensitivityIndicator(String perpetratorSensitivityIndicator) {
    this.perpetratorSensitivityIndicator = perpetratorSensitivityIndicator;
  }

  public String getClientSensitivity() {
    return clientSensitivity;
  }

  public void setClientSensitivity(String clientSensitivity) {
    this.clientSensitivity = clientSensitivity;
  }

  /**
   * WARNING: Bad idea. Inject or access by other means.
   * 
   * @param opts rocket options
   */
  public static void setOpts(FlightPlan opts) {
    EsPersonReferral.opts = opts;
  }

  public EmbeddableAccessLimitation getAccessLimitation() {
    return accessLimitation;
  }

  public void setAccessLimitation(EmbeddableAccessLimitation accessLimitation) {
    this.accessLimitation = accessLimitation;
  }

  public String getLimitedAccessCode() {
    return accessLimitation.getLimitedAccessCode();
  }

  public void setLimitedAccessCode(String limitedAccessCode) {
    accessLimitation.setLimitedAccessCode(limitedAccessCode);
  }

  public Date getLimitedAccessDate() {
    return accessLimitation.getLimitedAccessDate();
  }

  public void setLimitedAccessDate(Date limitedAccessDate) {
    accessLimitation.setLimitedAccessDate(limitedAccessDate);
  }

  public String getLimitedAccessDescription() {
    return accessLimitation.getLimitedAccessDescription();
  }

  public void setLimitedAccessDescription(String limitedAccessDescription) {
    accessLimitation.setLimitedAccessDescription(limitedAccessDescription);
  }

  public Integer getLimitedAccessGovernmentEntityId() {
    return accessLimitation.getLimitedAccessGovernmentEntityId();
  }

  public void setLimitedAccessGovernmentEntityId(Integer limitedAccessGovernmentEntityId) {
    accessLimitation.setLimitedAccessGovernmentEntityId(limitedAccessGovernmentEntityId);
  }

  public EmbeddableStaffWorker getWorker() {
    return worker;
  }

  public void setWorker(EmbeddableStaffWorker worker) {
    this.worker = worker;
  }

  public CmsReplicationOperation getReferralReplicationOperation() {
    return referralReplicationOperation;
  }

  public void setReferralReplicationOperation(
      CmsReplicationOperation referralReplicationOperation) {
    this.referralReplicationOperation = referralReplicationOperation;
  }

  public CmsReplicationOperation getReferralClientReplicationOperation() {
    return referralClientReplicationOperation;
  }

  public void setReferralClientReplicationOperation(
      CmsReplicationOperation referralClientReplicationOperation) {
    this.referralClientReplicationOperation = referralClientReplicationOperation;
  }

  public CmsReplicationOperation getAllegationReplicationOperation() {
    return allegationReplicationOperation;
  }

  public void setAllegationReplicationOperation(
      CmsReplicationOperation allegationReplicationOperation) {
    this.allegationReplicationOperation = allegationReplicationOperation;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE, false);
  }

  @Override
  public int compare(EsPersonReferral o1, EsPersonReferral o2) {
    return o1.getClientId().compareTo(o2.getClientId());
  }

  @Override
  public int compareTo(EsPersonReferral o) {
    return compare(this, o);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false);
  }

}
