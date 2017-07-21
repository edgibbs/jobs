package gov.ca.cwds.data.persistence.cms.rep;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.annotations.NamedNativeQueries;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.ca.cwds.data.persistence.cms.BaseReporter;

@NamedNativeQueries({
    @NamedNativeQuery(
        name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedReporterR1.findAllUpdatedAfter",
        query = "SELECT trim(z.RPTR_BDGNO) RPTR_BDGNO, trim(z.RPTR_CTYNM) RPTR_CTYNM, z.COL_RELC, "
            + "z.CMM_MTHC, z.CNFWVR_IND, z.FDBACK_DOC, z.RPTR_EMPNM, z.FEEDBCK_DT, z.FB_RQR_IND, "
            + "z.RPTR_FSTNM, trim(z.RPTR_LSTNM) RPTR_LSTNM, z.MNRPTR_IND, z.MSG_EXT_NO, "
            + "z.MSG_TEL_NO, trim(z.MID_INI_NM) MID_INI_NM, trim(z.NMPRFX_DSC) NMPRFX_DSC, "
            + "z.PRM_TEL_NO, z.PRM_EXT_NO, z.STATE_C, trim(z.RPTR_ST_NM) RPTR_ST_NM, "
            + "trim(z.RPTR_ST_NO) RPTR_ST_NO, trim(z.SUFX_TLDSC) SUFX_TLDSC, z.RPTR_ZIPNO, "
            + "z.LST_UPD_ID, z.LST_UPD_TS, z.FKREFERL_T, z.FKLAW_ENFT, z.ZIP_SFX_NO, z.CNTY_SPFCD, "
            + "z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER FROM {h-schema}REPTR_T z "
            + "WHERE z.IBMSNAP_LOGMARKER >= :after FOR READ ONLY WITH UR ",
        resultClass = ReplicatedReporterR1.class),
    // @NamedNativeQuery(
    // name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedReporterR1.findPartitionedBuckets",
    // query = "select trim(z.RPTR_BDGNO) RPTR_BDGNO, trim(z.RPTR_CTYNM) RPTR_CTYNM,
    // z.COL_RELC, z.CMM_MTHC, z.CNFWVR_IND, z.FDBACK_DOC, z.RPTR_EMPNM, z.FEEDBCK_DT, z.FB_RQR_IND,
    // z.RPTR_FSTNM, trim(z.RPTR_LSTNM) RPTR_LSTNM, z.MNRPTR_IND, z.MSG_EXT_NO, z.MSG_TEL_NO,
    // trim(z.MID_INI_NM) MID_INI_NM, trim(z.NMPRFX_DSC) NMPRFX_DSC, z.PRM_TEL_NO,
    // z.PRM_EXT_NO, z.STATE_C, trim(z.RPTR_ST_NM) RPTR_ST_NM, trim(z.RPTR_ST_NO) RPTR_ST_NO,
    // trim(z.SUFX_TLDSC) SUFX_TLDSC, z.RPTR_ZIPNO, z.LST_UPD_ID, z.LST_UPD_TS, z.FKREFERL_T,
    // z.FKLAW_ENFT, z.ZIP_SFX_NO, z.CNTY_SPFCD , \'U\' IBMSNAP_OPERATION, z.LST_UPD_TS as
    // IBMSNAP_LOGMARKER from ( select mod(y.rn, CAST(:total_buckets AS INTEGER)) + 1 bucket, y.*
    // from ( select row_number() over (order by 1) rn, x.* from {h-schema}REPTR_T x WHERE
    // x.FKREFERL_T >= :min_id and x.FKREFERL_T < :max_id ) y ) z where z.bucket = :bucket_num for
    // read only",
    // resultClass = ReplicatedReporterR1.class)})
    @NamedNativeQuery(
        name = "gov.ca.cwds.data.persistence.cms.rep.ReplicatedReporterR1.findPartitionedBuckets",
        query = "SELECT trim(z.RPTR_BDGNO) RPTR_BDGNO, trim(z.RPTR_CTYNM) RPTR_CTYNM, z.COL_RELC, "
            + "z.CMM_MTHC, z.CNFWVR_IND, z.FDBACK_DOC, z.RPTR_EMPNM, z.FEEDBCK_DT, z.FB_RQR_IND, "
            + "z.RPTR_FSTNM, trim(z.RPTR_LSTNM) RPTR_LSTNM, z.MNRPTR_IND, z.MSG_EXT_NO, "
            + "z.MSG_TEL_NO, trim(z.MID_INI_NM) MID_INI_NM, trim(z.NMPRFX_DSC) NMPRFX_DSC, "
            + "z.PRM_TEL_NO, z.PRM_EXT_NO, z.STATE_C, trim(z.RPTR_ST_NM) RPTR_ST_NM, "
            + "trim(z.RPTR_ST_NO) RPTR_ST_NO, trim(z.SUFX_TLDSC) SUFX_TLDSC, z.RPTR_ZIPNO, "
            + "z.LST_UPD_ID, z.LST_UPD_TS, z.FKREFERL_T, z.FKLAW_ENFT, z.ZIP_SFX_NO, z.CNTY_SPFCD, "
            + "z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER FROM {h-schema}REPTR_T z "
            + "WHERE z.FKREFERL_T >= :min_id AND z.FKREFERL_T < :max_id "
            + "AND (1=1 OR 57 = :bucket_num OR 92 = :total_buckets) FOR READ ONLY WITH UR",
        resultClass = ReplicatedReporterR1.class)})
@Entity
@Table(name = "REPTR_T")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicatedReporterR1 extends BaseReporter implements CmsReplicatedEntity {

  private static final long serialVersionUID = 1L;

  @Enumerated(EnumType.STRING)
  @Column(name = "IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation replicationOperation;

  @Type(type = "timestamp")
  @Column(name = "IBMSNAP_LOGMARKER", updatable = false)
  private Date replicationDate;

  @Override
  public CmsReplicationOperation getReplicationOperation() {
    return this.replicationOperation;
  }

  @Override
  public void setReplicationOperation(CmsReplicationOperation replicationOperation) {
    this.replicationOperation = replicationOperation;
  }

  @Override
  public Date getReplicationDate() {
    return this.replicationDate;
  }

  @Override
  public void setReplicationDate(Date replicationDate) {
    this.replicationDate = replicationDate;
  }
}
