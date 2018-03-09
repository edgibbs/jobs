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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.annotations.Type;

import gov.ca.cwds.data.es.ElasticSearchPersonRelationship;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils;
import gov.ca.cwds.neutron.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;

/**
 * Entity bean for bi-directional relationships.
 * 
 * <p>
 * By request of the Intake team, it only reads relationships from CLN_RELT, for now.
 * </p>
 * 
 * <p>
 * Implements {@link ApiGroupNormalizer} and converts to {@link ReplicatedRelationships}.
 * </p>
 * 
 * @author CWDS API Team
 */
@Entity
@Table(name = "VW_LST_BI_DIR_RELATION")
//@formatter:off
@NamedNativeQuery(name = "gov.ca.cwds.data.persistence.cms.EsRelationship.findAllUpdatedAfter",
    query = 
         "WITH DRIVER as ( \n"
        + " SELECT DISTINCT \n"
        + "     'CLN_RELT'             AS THIS_LEGACY_TABLE, \n"
        + "     'CLIENT_T'             AS RELATED_LEGACY_TABLE, \n"
        + "     CLNS.IDENTIFIER        AS THIS_LEGACY_ID, \n"
        + "     CLNS.SENSTV_IND        AS THIS_SENSITIVITY_IND, \n"
        + "     CLNS.LST_UPD_ID        AS THIS_LEGACY_LAST_UPDATED_ID, \n"
        + "     CLNS.LST_UPD_TS        AS THIS_LEGACY_LAST_UPDATED, \n"
        + "     CLNS.COM_FST_NM        AS THIS_FIRST_NAME, \n"
        + "     CLNS.COM_LST_NM        AS THIS_LAST_NAME, \n"
        + "     CLNR.CLNTRELC          AS REL_CODE, \n"
        + "     CLNP.IDENTIFIER        AS RELATED_LEGACY_ID, \n"
        + "     CLNP.SENSTV_IND        AS RELATED_SENSITIVITY_IND, \n"
        + "     CLNP.LST_UPD_ID        AS RELATED_LEGACY_LAST_UPDATED_ID, \n"
        + "     CLNP.LST_UPD_TS        AS RELATED_LEGACY_LAST_UPDATED, \n"
        + "     CLNP.COM_FST_NM        AS RELATED_FIRST_NAME, \n"
        + "     CLNP.COM_LST_NM        AS RELATED_LAST_NAME, \n"
        + "     CLNR.IBMSNAP_LOGMARKER AS REL_IBMSNAP_LOGMARKER, \n"
        + "     CLNR.IBMSNAP_OPERATION AS REL_IBMSNAP_OPERATION, \n"
        + "     CLNS.IBMSNAP_LOGMARKER AS THIS_IBMSNAP_LOGMARKER, \n"
        + "     CLNS.IBMSNAP_OPERATION AS THIS_IBMSNAP_OPERATION, \n"
        + "     CLNP.IBMSNAP_LOGMARKER AS RELATED_IBMSNAP_LOGMARKER, \n"
        + "     CLNP.IBMSNAP_OPERATION AS RELATED_IBMSNAP_OPERATION, \n"
        + "     MAX (CLNR.IBMSNAP_LOGMARKER, CLNP.IBMSNAP_LOGMARKER, CLNS.IBMSNAP_LOGMARKER) AS LAST_CHG \n"
        + " FROM {h-schema}GT_ID GT \n"
        + " JOIN {h-schema}CLN_RELT CLNR ON GT.IDENTIFIER   = CLNR.FKCLIENT_T \n"
        + " JOIN {h-schema}CLIENT_T CLNS ON CLNR.FKCLIENT_T = CLNS.IDENTIFIER \n"
        + " JOIN {h-schema}CLIENT_T CLNP ON CLNR.FKCLIENT_0 = CLNP.IDENTIFIER \n"
      + " UNION \n"
        + " SELECT DISTINCT \n"
        + "     'CLN_RELT'             AS THIS_LEGACY_TABLE, \n"
        + "     'CLIENT_T'             AS RELATED_LEGACY_TABLE, \n"
        + "     CLNS.IDENTIFIER        AS THIS_LEGACY_ID, \n"
        + "     CLNS.SENSTV_IND        AS THIS_SENSITIVITY_IND, \n"
        + "     CLNS.LST_UPD_ID        AS THIS_LEGACY_LAST_UPDATED_ID, \n"
        + "     CLNS.LST_UPD_TS        AS THIS_LEGACY_LAST_UPDATED, \n"
        + "     CLNS.COM_FST_NM        AS THIS_FIRST_NAME, \n"
        + "     CLNS.COM_LST_NM        AS THIS_LAST_NAME, \n"
        + "     CLNR.CLNTRELC          AS REL_CODE, \n"
        + "     CLNP.IDENTIFIER        AS RELATED_LEGACY_ID, \n"
        + "     CLNP.SENSTV_IND        AS RELATED_SENSITIVITY_IND, \n"
        + "     CLNP.LST_UPD_ID        AS RELATED_LEGACY_LAST_UPDATED_ID, \n"
        + "     CLNP.LST_UPD_TS        AS RELATED_LEGACY_LAST_UPDATED, \n"
        + "     CLNP.COM_FST_NM        AS RELATED_FIRST_NAME, \n"
        + "     CLNP.COM_LST_NM        AS RELATED_LAST_NAME, \n"
        + "     CLNR.IBMSNAP_LOGMARKER AS REL_IBMSNAP_LOGMARKER, \n"
        + "     CLNR.IBMSNAP_OPERATION AS REL_IBMSNAP_OPERATION, \n"
        + "     CLNS.IBMSNAP_LOGMARKER AS THIS_IBMSNAP_LOGMARKER, \n"
        + "     CLNS.IBMSNAP_OPERATION AS THIS_IBMSNAP_OPERATION, \n"
        + "     CLNP.IBMSNAP_LOGMARKER AS RELATED_IBMSNAP_LOGMARKER, \n"
        + "     CLNP.IBMSNAP_OPERATION AS RELATED_IBMSNAP_OPERATION, \n"
        + "     MAX (CLNR.IBMSNAP_LOGMARKER, CLNP.IBMSNAP_LOGMARKER, CLNS.IBMSNAP_LOGMARKER) AS LAST_CHG \n"
        + " FROM {h-schema}GT_ID GT \n"
        + " JOIN {h-schema}CLN_RELT CLNR ON GT.IDENTIFIER   = CLNR.FKCLIENT_0 \n"
        + " JOIN {h-schema}CLIENT_T CLNS ON CLNR.FKCLIENT_T = CLNS.IDENTIFIER \n"
        + " JOIN {h-schema}CLIENT_T CLNP ON CLNR.FKCLIENT_0 = CLNP.IDENTIFIER \n"
        + ") \n"
      + "SELECT \n"
        + " 0 AS REVERSE_RELATIONSHIP, \n"
        + " d1.THIS_LEGACY_ID, \n"
        + " d1.THIS_SENSITIVITY_IND, \n"
        + " d1.THIS_LEGACY_LAST_UPDATED, \n"
        + " d1.THIS_LEGACY_LAST_UPDATED_ID, \n"
        + " d1.THIS_FIRST_NAME, \n"
        + " d1.THIS_LAST_NAME, \n"
        + " d1.REL_CODE, \n"
        + " d1.RELATED_LEGACY_ID, \n"
        + " d1.RELATED_SENSITIVITY_IND, \n"
        + " d1.RELATED_LEGACY_LAST_UPDATED, \n"
        + " d1.RELATED_LEGACY_LAST_UPDATED_ID, \n"
        + " d1.RELATED_FIRST_NAME, \n"
        + " d1.RELATED_LAST_NAME, \n"
        + " d1.REL_IBMSNAP_LOGMARKER, \n"
        + " d1.REL_IBMSNAP_OPERATION, \n"
        + " d1.THIS_IBMSNAP_LOGMARKER, \n"
        + " d1.THIS_IBMSNAP_OPERATION, \n"
        + " d1.RELATED_IBMSNAP_LOGMARKER, \n"
        + " d1.RELATED_IBMSNAP_OPERATION, \n"
        + " d1.LAST_CHG \n"
        + "FROM DRIVER d1 \n"
        + "WHERE 1=1 OR d1.REL_IBMSNAP_LOGMARKER > :after\n"
    + "UNION ALL \n"
      + "SELECT \n"
      + " 1                                 AS REVERSE_RELATIONSHIP, \n"
      + " d2.RELATED_LEGACY_ID              AS THIS_LEGACY_ID, \n"
      + " d2.RELATED_SENSITIVITY_IND        AS THIS_SENSITIVITY_IND, \n"
      + " d2.RELATED_LEGACY_LAST_UPDATED    AS THIS_LEGACY_LAST_UPDATED, \n"
      + " d2.RELATED_LEGACY_LAST_UPDATED_ID AS THIS_LEGACY_LAST_UPDATED_ID, \n"
      + " d2.RELATED_FIRST_NAME             AS THIS_FIRST_NAME, \n"
      + " d2.RELATED_LAST_NAME              AS THIS_LAST_NAME, \n"
      + " d2.REL_CODE, \n"
      + " d2.THIS_LEGACY_ID                 AS RELATED_LEGACY_ID, \n"
      + " d2.THIS_SENSITIVITY_IND           AS RELATED_SENSITIVITY_IND, \n"
      + " d2.THIS_LEGACY_LAST_UPDATED       AS RELATED_LEGACY_LAST_UPDATED, \n"
      + " d2.THIS_LEGACY_LAST_UPDATED_ID    AS RELATED_LEGACY_LAST_UPDATED_ID, \n"
      + " d2.THIS_FIRST_NAME                AS RELATED_FIRST_NAME, \n"
      + " d2.THIS_LAST_NAME                 AS RELATED_LAST_NAME, \n"
      + " d2.REL_IBMSNAP_LOGMARKER, \n"
      + " d2.REL_IBMSNAP_OPERATION, \n"
      + " d2.THIS_IBMSNAP_LOGMARKER, \n"
      + " d2.THIS_IBMSNAP_OPERATION, \n"
      + " d2.RELATED_IBMSNAP_LOGMARKER, \n"
      + " d2.RELATED_IBMSNAP_OPERATION, \n"
      + " d2.LAST_CHG \n"
      + "FROM DRIVER d2 \n"
      + "WHERE d2.THIS_SENSITIVITY_IND = 'N' AND d2.RELATED_SENSITIVITY_IND = 'N' \n"
     + "ORDER BY 2, 9 \n"
     + "FOR READ ONLY WITH UR ",
    resultClass = EsRelationship.class, readOnly = true)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.EsRelationship.findAllUpdatedAfterWithUnlimitedAccess",
        query = 
        "WITH DRIVER as ( \n"
       + " SELECT DISTINCT \n"
       + "     'CLN_RELT'             AS THIS_LEGACY_TABLE, \n"
       + "     'CLIENT_T'             AS RELATED_LEGACY_TABLE, \n"
       + "     CLNS.IDENTIFIER        AS THIS_LEGACY_ID, \n"
       + "     CLNS.SENSTV_IND        AS THIS_SENSITIVITY_IND, \n"
       + "     CLNS.LST_UPD_ID        AS THIS_LEGACY_LAST_UPDATED_ID, \n"
       + "     CLNS.LST_UPD_TS        AS THIS_LEGACY_LAST_UPDATED, \n"
       + "     CLNS.COM_FST_NM        AS THIS_FIRST_NAME, \n"
       + "     CLNS.COM_LST_NM        AS THIS_LAST_NAME, \n"
       + "     CLNR.CLNTRELC          AS REL_CODE, \n"
       + "     CLNP.IDENTIFIER        AS RELATED_LEGACY_ID, \n"
       + "     CLNP.SENSTV_IND        AS RELATED_SENSITIVITY_IND, \n"
       + "     CLNP.LST_UPD_ID        AS RELATED_LEGACY_LAST_UPDATED_ID, \n"
       + "     CLNP.LST_UPD_TS        AS RELATED_LEGACY_LAST_UPDATED, \n"
       + "     CLNP.COM_FST_NM        AS RELATED_FIRST_NAME, \n"
       + "     CLNP.COM_LST_NM        AS RELATED_LAST_NAME, \n"
       + "     CLNR.IBMSNAP_LOGMARKER AS REL_IBMSNAP_LOGMARKER, \n"
       + "     CLNR.IBMSNAP_OPERATION AS REL_IBMSNAP_OPERATION, \n"
       + "     CLNS.IBMSNAP_LOGMARKER AS THIS_IBMSNAP_LOGMARKER, \n"
       + "     CLNS.IBMSNAP_OPERATION AS THIS_IBMSNAP_OPERATION, \n"
       + "     CLNP.IBMSNAP_LOGMARKER AS RELATED_IBMSNAP_LOGMARKER, \n"
       + "     CLNP.IBMSNAP_OPERATION AS RELATED_IBMSNAP_OPERATION, \n"
       + "     MAX ( \n"
       + "         CLNR.IBMSNAP_LOGMARKER, \n"
       + "         CLNP.IBMSNAP_LOGMARKER, \n"
       + "         CLNS.IBMSNAP_LOGMARKER \n"
       + "     ) AS LAST_CHG \n"
       + " FROM {h-schema}GT_ID GT \n"
       + " JOIN {h-schema}CLN_RELT CLNR ON GT.IDENTIFIER   = CLNR.FKCLIENT_T \n"
       + " JOIN {h-schema}CLIENT_T CLNS ON CLNR.FKCLIENT_T = CLNS.IDENTIFIER \n"
       + " JOIN {h-schema}CLIENT_T CLNP ON CLNR.FKCLIENT_0 = CLNP.IDENTIFIER \n"
     + " UNION \n"
       + " SELECT DISTINCT \n"
       + "     'CLN_RELT'             AS THIS_LEGACY_TABLE, \n"
       + "     'CLIENT_T'             AS RELATED_LEGACY_TABLE, \n"
       + "     CLNS.IDENTIFIER        AS THIS_LEGACY_ID, \n"
       + "     CLNS.SENSTV_IND        AS THIS_SENSITIVITY_IND, \n"
       + "     CLNS.LST_UPD_ID        AS THIS_LEGACY_LAST_UPDATED_ID, \n"
       + "     CLNS.LST_UPD_TS        AS THIS_LEGACY_LAST_UPDATED, \n"
       + "     CLNS.COM_FST_NM        AS THIS_FIRST_NAME, \n"
       + "     CLNS.COM_LST_NM        AS THIS_LAST_NAME, \n"
       + "     CLNR.CLNTRELC          AS REL_CODE, \n"
       + "     CLNP.IDENTIFIER        AS RELATED_LEGACY_ID, \n"
       + "     CLNP.SENSTV_IND        AS RELATED_SENSITIVITY_IND, \n"
       + "     CLNP.LST_UPD_ID        AS RELATED_LEGACY_LAST_UPDATED_ID, \n"
       + "     CLNP.LST_UPD_TS        AS RELATED_LEGACY_LAST_UPDATED, \n"
       + "     CLNP.COM_FST_NM        AS RELATED_FIRST_NAME, \n"
       + "     CLNP.COM_LST_NM        AS RELATED_LAST_NAME, \n"
       + "     CLNR.IBMSNAP_LOGMARKER AS REL_IBMSNAP_LOGMARKER, \n"
       + "     CLNR.IBMSNAP_OPERATION AS REL_IBMSNAP_OPERATION, \n"
       + "     CLNS.IBMSNAP_LOGMARKER AS THIS_IBMSNAP_LOGMARKER, \n"
       + "     CLNS.IBMSNAP_OPERATION AS THIS_IBMSNAP_OPERATION, \n"
       + "     CLNP.IBMSNAP_LOGMARKER AS RELATED_IBMSNAP_LOGMARKER, \n"
       + "     CLNP.IBMSNAP_OPERATION AS RELATED_IBMSNAP_OPERATION, \n"
       + "     MAX ( CLNR.IBMSNAP_LOGMARKER, \n"
       + "         CLNP.IBMSNAP_LOGMARKER, \n"
       + "         CLNS.IBMSNAP_LOGMARKER \n"
       + "     ) AS LAST_CHG \n"
       + " FROM {h-schema}GT_ID GT \n"
       + " JOIN {h-schema}CLN_RELT CLNR ON GT.IDENTIFIER   = CLNR.FKCLIENT_0 \n"
       + " JOIN {h-schema}CLIENT_T CLNS ON CLNR.FKCLIENT_T = CLNS.IDENTIFIER \n"
       + " JOIN {h-schema}CLIENT_T CLNP ON CLNR.FKCLIENT_0 = CLNP.IDENTIFIER \n"
       + ") \n"
     + "SELECT \n"
       + " 0 AS REVERSE_RELATIONSHIP, \n"
       + " d1.THIS_LEGACY_ID, \n"
       + " d1.THIS_SENSITIVITY_IND, \n"
       + " d1.THIS_LEGACY_LAST_UPDATED, \n"
       + " d1.THIS_LEGACY_LAST_UPDATED_ID, \n"
       + " d1.THIS_FIRST_NAME, \n"
       + " d1.THIS_LAST_NAME, \n"
       + " d1.REL_CODE, \n"
       + " d1.RELATED_LEGACY_ID, \n"
       + " d1.RELATED_SENSITIVITY_IND, \n"
       + " d1.RELATED_LEGACY_LAST_UPDATED, \n"
       + " d1.RELATED_LEGACY_LAST_UPDATED_ID, \n"
       + " d1.RELATED_FIRST_NAME, \n"
       + " d1.RELATED_LAST_NAME, \n"
       + " d1.REL_IBMSNAP_LOGMARKER, \n"
       + " d1.REL_IBMSNAP_OPERATION, \n"
       + " d1.THIS_IBMSNAP_LOGMARKER, \n"
       + " d1.THIS_IBMSNAP_OPERATION, \n"
       + " d1.RELATED_IBMSNAP_LOGMARKER, \n"
       + " d1.RELATED_IBMSNAP_OPERATION, \n"
       + " d1.LAST_CHG \n"
       + "FROM DRIVER d1 \n"
       + "WHERE d1.THIS_SENSITIVITY_IND = 'N' AND d1.RELATED_SENSITIVITY_IND = 'N' \n"
       + "  AND (1=1 OR d1.REL_IBMSNAP_LOGMARKER > :after)\n"
   + "UNION ALL\n"
     + "SELECT \n"
       + " 1                                 AS REVERSE_RELATIONSHIP, \n"
       + " d2.RELATED_LEGACY_ID              AS THIS_LEGACY_ID, \n"
       + " d2.RELATED_SENSITIVITY_IND        AS THIS_SENSITIVITY_IND, \n"
       + " d2.RELATED_LEGACY_LAST_UPDATED    AS THIS_LEGACY_LAST_UPDATED, \n"
       + " d2.RELATED_LEGACY_LAST_UPDATED_ID AS THIS_LEGACY_LAST_UPDATED_ID, \n"
       + " d2.RELATED_FIRST_NAME             AS THIS_FIRST_NAME, \n"
       + " d2.RELATED_LAST_NAME              AS THIS_LAST_NAME, \n"
       + " d2.REL_CODE, \n"
       + " d2.THIS_LEGACY_ID                 AS RELATED_LEGACY_ID, \n"
       + " d2.THIS_SENSITIVITY_IND           AS RELATED_SENSITIVITY_IND, \n"
       + " d2.THIS_LEGACY_LAST_UPDATED       AS RELATED_LEGACY_LAST_UPDATED, \n"
       + " d2.THIS_LEGACY_LAST_UPDATED_ID    AS RELATED_LEGACY_LAST_UPDATED_ID, \n"
       + " d2.THIS_FIRST_NAME                AS RELATED_FIRST_NAME, \n"
       + " d2.THIS_LAST_NAME                 AS RELATED_LAST_NAME, \n"
       + " d2.REL_IBMSNAP_LOGMARKER, \n"
       + " d2.REL_IBMSNAP_OPERATION, \n"
       + " d2.THIS_IBMSNAP_LOGMARKER, \n"
       + " d2.THIS_IBMSNAP_OPERATION, \n"
       + " d2.RELATED_IBMSNAP_LOGMARKER, \n"
       + " d2.RELATED_IBMSNAP_OPERATION, \n"
       + " d2.LAST_CHG \n"
       + "FROM DRIVER d2 \n"
       + "WHERE d2.THIS_SENSITIVITY_IND = 'N' AND d2.RELATED_SENSITIVITY_IND = 'N' \n"
       + "  AND (1=1 OR d2.REL_IBMSNAP_LOGMARKER > :after)\n"
      + "ORDER BY 2, 9 \n"
      + "FOR READ ONLY WITH UR ",
    resultClass = EsRelationship.class, readOnly = true)
//@formatter:on
public class EsRelationship
    implements PersistentObject, ApiGroupNormalizer<ReplicatedRelationships>,
    Comparable<EsRelationship>, Comparator<EsRelationship> {

  private static final long serialVersionUID = 1L;

  @Id
  @Type(type = "boolean")
  @Column(name = "REVERSE_RELATIONSHIP")
  private Boolean reverseRelationship;

  @Id
  @Column(name = "THIS_LEGACY_ID")
  private String thisLegacyId;

  @Column(name = "THIS_FIRST_NAME")
  private String thisFirstName;

  @Column(name = "THIS_LAST_NAME")
  private String thisLastName;

  @Id
  @Type(type = "short")
  @Column(name = "REL_CODE")
  private Short relCode;

  @Id
  @Column(name = "RELATED_LEGACY_ID")
  private String relatedLegacyId;

  @Column(name = "RELATED_FIRST_NAME")
  private String relatedFirstName;

  @Column(name = "RELATED_LAST_NAME")
  private String relatedLastName;

  @Column(name = "THIS_LEGACY_LAST_UPDATED")
  @Type(type = "date")
  private Date thisLegacyLastUpdated;

  @Column(name = "RELATED_LEGACY_LAST_UPDATED")
  @Type(type = "date")
  private Date relatedLegacyLastUpdated;

  // =====================
  // REPLICATION:
  // =====================

  @Enumerated(EnumType.STRING)
  @Column(name = "REL_IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation relationshipReplicationOperation;

  @Enumerated(EnumType.STRING)
  @Column(name = "THIS_IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation thisClientReplicationOperation;

  @Enumerated(EnumType.STRING)
  @Column(name = "RELATED_IBMSNAP_OPERATION", updatable = false)
  private CmsReplicationOperation relatedClientReplicationOperation;

  @Type(type = "timestamp")
  @Column(name = "REL_IBMSNAP_LOGMARKER", updatable = false)
  private Date relationshipReplicationDate;

  @Type(type = "timestamp")
  @Column(name = "THIS_IBMSNAP_LOGMARKER", updatable = false)
  private Date thisClientReplicationDate;

  @Type(type = "timestamp")
  @Column(name = "RELATED_IBMSNAP_LOGMARKER", updatable = false)
  private Date relatedClientReplicationDate;

  // =====================
  // ROW MAPPING:
  // =====================

  /**
   * Build an EsRelationship from an incoming ResultSet.
   * 
   * @param rs incoming tuple
   * @return a populated EsRelationship
   * @throws SQLException if unable to convert types or stream breaks, etc.
   */
  public static EsRelationship mapRow(ResultSet rs) throws SQLException {
    final EsRelationship ret = new EsRelationship();

    ret.setReverseRelationship(rs.getBoolean("REVERSE_RELATIONSHIP"));
    ret.setThisLegacyId(ifNull(rs.getString("THIS_LEGACY_ID")));
    ret.setThisFirstName(ifNull(rs.getString("THIS_FIRST_NAME")));
    ret.setThisLastName(ifNull(rs.getString("THIS_LAST_NAME")));
    ret.setRelCode(rs.getShort("REL_CODE"));
    ret.setRelatedLegacyId(ifNull(rs.getString("RELATED_LEGACY_ID")));
    ret.setRelatedFirstName(ifNull(rs.getString("RELATED_FIRST_NAME")));
    ret.setRelatedLastName(ifNull(rs.getString("RELATED_LAST_NAME")));
    ret.setThisLegacyLastUpdated(rs.getDate("THIS_LEGACY_LAST_UPDATED"));
    ret.setRelatedLegacyLastUpdated(rs.getDate("RELATED_LEGACY_LAST_UPDATED"));

    ret.relationshipReplicationDate = rs.getTimestamp("REL_IBMSNAP_LOGMARKER");
    ret.thisClientReplicationDate = rs.getTimestamp("THIS_IBMSNAP_LOGMARKER");
    ret.relatedClientReplicationDate = rs.getTimestamp("RELATED_IBMSNAP_LOGMARKER");

    ret.relationshipReplicationOperation =
        CmsReplicationOperation.strToRepOp(rs.getString("REL_IBMSNAP_OPERATION"));
    ret.thisClientReplicationOperation =
        CmsReplicationOperation.strToRepOp(rs.getString("THIS_IBMSNAP_OPERATION"));
    ret.relatedClientReplicationOperation =
        CmsReplicationOperation.strToRepOp(rs.getString("RELATED_IBMSNAP_OPERATION"));

    return ret;
  }

  @Override
  public Class<ReplicatedRelationships> getNormalizationClass() {
    return ReplicatedRelationships.class;
  }

  /**
   * Parse bi-directional relationships and add to appropriate side.
   * 
   * @param rel relationship to modify
   */
  protected void parseBiDirectionalRelationship(final ElasticSearchPersonRelationship rel) {
    if (this.relCode != null && this.relCode.intValue() != 0) {
      CmsRelationship relationship;
      final Map<Short, CmsRelationship> mapRelationCodes =
          RelationshipTypeCache.getInstance().getMapRelationCodes();
      if (mapRelationCodes.containsKey(relCode)) {
        relationship = mapRelationCodes.get(relCode);
      } else {
        relationship = new CmsRelationship(relCode);
        mapRelationCodes.put(relCode, relationship);
      }

      // Reverse relationship direction.
      if (getReverseRelationship() == null || getReverseRelationship().booleanValue()) {
        rel.setIndexedPersonRelationship(relationship.secondaryRel);
        rel.setRelatedPersonRelationship(relationship.primaryRel);
      } else {
        rel.setIndexedPersonRelationship(relationship.primaryRel);
        rel.setRelatedPersonRelationship(relationship.secondaryRel);
      }

      // Context remains the same.
      rel.setRelationshipContext(relationship.relContext);
    }
  }

  /**
   * <strong>Implementation notes:</strong> Only reading from CLN_RELT, for the moment. Intake sets
   * field "related_person_id" <strong>from PostgreSQL, NOT from DB2</strong>.
   */
  @Override
  public ReplicatedRelationships normalize(Map<Object, ReplicatedRelationships> map) {
    final boolean isClientAdded = map.containsKey(this.thisLegacyId);
    final ReplicatedRelationships ret =
        isClientAdded ? map.get(this.thisLegacyId) : new ReplicatedRelationships(this.thisLegacyId);

    // INT-1037: Omit deleted relationships and clients.
    if (CmsReplicationOperation.isActiveRecord(this.thisClientReplicationOperation)
        && CmsReplicationOperation.isActiveRecord(this.relatedClientReplicationOperation)
        && CmsReplicationOperation.isActiveRecord(this.relationshipReplicationOperation)) {
      final ElasticSearchPersonRelationship rel = new ElasticSearchPersonRelationship();
      ret.addRelation(rel);

      if (StringUtils.isNotBlank(this.relatedFirstName)) {
        rel.setRelatedPersonFirstName(this.relatedFirstName.trim());
      }

      if (StringUtils.isNotBlank(this.relatedLastName)) {
        rel.setRelatedPersonLastName(this.relatedLastName.trim());
      }

      if (StringUtils.isNotBlank(this.relatedLegacyId)) {
        rel.setRelatedPersonLegacyId(this.relatedLegacyId.trim());
      }

      rel.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.relatedLegacyId,
          this.relatedLegacyLastUpdated, LegacyTable.CLIENT));

      parseBiDirectionalRelationship(rel);
      map.put(ret.getId(), ret);
    }

    return ret;
  }

  @Override
  public String getNormalizationGroupKey() {
    return this.thisLegacyId;
  }

  public boolean isActive() {
    return thisClientReplicationOperation != CmsReplicationOperation.D
        && relatedClientReplicationOperation != CmsReplicationOperation.D
        && relationshipReplicationOperation != CmsReplicationOperation.D;
  }

  public String getThisLegacyId() {
    return thisLegacyId;
  }

  public void setThisLegacyId(String thisLegacyId) {
    this.thisLegacyId = thisLegacyId;
  }

  public String getThisFirstName() {
    return thisFirstName;
  }

  public void setThisFirstName(String thisFirstName) {
    this.thisFirstName = thisFirstName;
  }

  public String getThisLastName() {
    return thisLastName;
  }

  public void setThisLastName(String thisLastName) {
    this.thisLastName = thisLastName;
  }

  public Short getRelCode() {
    return relCode;
  }

  public void setRelCode(Short relCode) {
    this.relCode = relCode;
  }

  public String getRelatedLegacyId() {
    return relatedLegacyId;
  }

  public void setRelatedLegacyId(String relatedLegacyId) {
    this.relatedLegacyId = relatedLegacyId;
  }

  public String getRelatedFirstName() {
    return relatedFirstName;
  }

  public void setRelatedFirstName(String relatedFirstName) {
    this.relatedFirstName = relatedFirstName;
  }

  public String getRelatedLastName() {
    return relatedLastName;
  }

  public void setRelatedLastName(String relatedLastName) {
    this.relatedLastName = relatedLastName;
  }

  public Boolean getReverseRelationship() {
    return reverseRelationship;
  }

  public void setReverseRelationship(Boolean reverseRelationship) {
    this.reverseRelationship = reverseRelationship;
  }

  public Date getThisLegacyLastUpdated() {
    return NeutronDateUtils.freshDate(thisLegacyLastUpdated);
  }

  public void setThisLegacyLastUpdated(Date thisLegacyLastUpdated) {
    this.thisLegacyLastUpdated = NeutronDateUtils.freshDate(thisLegacyLastUpdated);
  }

  public Date getRelatedLegacyLastUpdated() {
    return NeutronDateUtils.freshDate(relatedLegacyLastUpdated);
  }

  public void setRelatedLegacyLastUpdated(Date relatedLegacyLastUpdated) {
    this.relatedLegacyLastUpdated = NeutronDateUtils.freshDate(relatedLegacyLastUpdated);
  }

  public CmsReplicationOperation getThisReplicationOperation() {
    return thisClientReplicationOperation;
  }

  public void setThisReplicationOperation(CmsReplicationOperation thisReplicationOperation) {
    this.thisClientReplicationOperation = thisReplicationOperation;
  }

  public Date getThisReplicationDate() {
    return NeutronDateUtils.freshDate(thisClientReplicationDate);
  }

  public void setThisReplicationDate(Date thisReplicationDate) {
    this.thisClientReplicationDate = NeutronDateUtils.freshDate(thisReplicationDate);
  }

  public CmsReplicationOperation getRelatedReplicationOperation() {
    return relatedClientReplicationOperation;
  }

  public void setRelatedReplicationOperation(CmsReplicationOperation relatedReplicationOperation) {
    this.relatedClientReplicationOperation = relatedReplicationOperation;
  }

  public Date getRelatedReplicationDate() {
    return NeutronDateUtils.freshDate(relatedClientReplicationDate);
  }

  public void setRelatedReplicationDate(Date relatedReplicationDate) {
    this.relatedClientReplicationDate = NeutronDateUtils.freshDate(relatedReplicationDate);
  }

  public CmsReplicationOperation getRelationshipReplicationOperation() {
    return relationshipReplicationOperation;
  }

  public void setRelationshipReplicationOperation(
      CmsReplicationOperation relationshipReplicationOperation) {
    this.relationshipReplicationOperation = relationshipReplicationOperation;
  }

  public CmsReplicationOperation getThisClientReplicationOperation() {
    return thisClientReplicationOperation;
  }

  public void setThisClientReplicationOperation(
      CmsReplicationOperation thisClientReplicationOperation) {
    this.thisClientReplicationOperation = thisClientReplicationOperation;
  }

  public CmsReplicationOperation getRelatedClientReplicationOperation() {
    return relatedClientReplicationOperation;
  }

  public void setRelatedClientReplicationOperation(
      CmsReplicationOperation relatedClientReplicationOperation) {
    this.relatedClientReplicationOperation = relatedClientReplicationOperation;
  }

  public Date getRelationshipReplicationDate() {
    return NeutronDateUtils.freshDate(relationshipReplicationDate);
  }

  public void setRelationshipReplicationDate(Date relationshipReplicationDate) {
    this.relationshipReplicationDate = NeutronDateUtils.freshDate(relationshipReplicationDate);
  }

  public Date getThisClientReplicationDate() {
    return NeutronDateUtils.freshDate(thisClientReplicationDate);
  }

  public void setThisClientReplicationDate(Date thisClientReplicationDate) {
    this.thisClientReplicationDate = NeutronDateUtils.freshDate(thisClientReplicationDate);
  }

  public Date getRelatedClientReplicationDate() {
    return NeutronDateUtils.freshDate(relatedClientReplicationDate);
  }

  public void setRelatedClientReplicationDate(Date relatedClientReplicationDate) {
    this.relatedClientReplicationDate = NeutronDateUtils.freshDate(relatedClientReplicationDate);
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  /**
   * This view (i.e., materialized query table) doesn't have a proper unique key, but a combination
   * of several fields might come close.
   */
  @Override
  public Serializable getPrimaryKey() {
    return null;
  }

  // =====================
  // IDENTITY:
  // =====================

  @Override
  public int compare(EsRelationship o1, EsRelationship o2) {
    return o1.getThisLegacyId().compareTo(o2.getThisLegacyId());
  }

  @Override
  public int compareTo(EsRelationship o) {
    return compare(this, o);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public final int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public final boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE, true);
  }

}
