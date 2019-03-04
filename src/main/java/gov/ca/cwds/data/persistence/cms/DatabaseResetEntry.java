package gov.ca.cwds.data.persistence.cms;

import static gov.ca.cwds.neutron.util.shrinkray.NeutronDateUtils.freshDate;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.annotations.Type;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.rest.api.domain.DomainChef;

/**
 * Entity bean for table CWSTMP.DBREFSTA.
 * 
 * <p>
 * Status column:
 * </p>
 * 
 * <table summary="Run Status">
 * <tr>
 * <th align="justify">Code</th>
 * <th align="justify">Meaning</th>
 * </tr>
 * <tr>
 * <td align="justify">R</td>
 * <td align="justify">running</td>
 * </tr>
 * <tr>
 * <td>F</td>
 * <td>failed</td>
 * </tr>
 * <tr>
 * <td>S</td>
 * <td>succeeded</td>
 * </tr>
 * </table>
 *
 * @author CWDS API Team
 */
@Entity
@Table(schema = "CWSTMP", name = "DBREFSTA")
//@formatter:off
@NamedNativeQuery(name = "gov.ca.cwds.data.persistence.cms.DatabaseResetEntry.findLastRun",
    query = "SELECT r.SCHEMA_NM, r.START_TS, r.END_TS, r.REF_STATUS \n"
        + " FROM ( \n"
        + "     SELECT r1.SCHEMA_NM, MAX(r1.START_TS) AS LAST_START \n"
        + "     FROM CWSTMP.DBREFSTA r1 \n"
        + "     WHERE r1.SCHEMA_NM = :schema_name \n"
        + "     GROUP BY SCHEMA_NM \n"
        + " ) d \n"
        + " JOIN CWSTMP.DBREFSTA r ON d.SCHEMA_NM = r.SCHEMA_NM AND d.LAST_START = r.START_TS \n"
        + " ORDER BY SCHEMA_NM, START_TS \n"
        + " WITH UR",
    resultClass = DatabaseResetEntry.class, readOnly = true)
//@formatter:on
public class DatabaseResetEntry
    implements PersistentObject, ApiGroupNormalizer<DatabaseResetEntry> {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "SCHEMA_NM")
  protected String schemaName;

  @Id
  @Type(type = "timestamp")
  @Column(name = "START_TS")
  protected Date startTime;

  @Type(type = "timestamp")
  @Column(name = "END_TS")
  protected Date endTime;

  /**
   * <table summary="Run Status">
   * <tr>
   * <th align="justify">Code</th>
   * <th align="justify">Meaning</th>
   * </tr>
   * <tr>
   * <td align="justify">R</td>
   * <td align="justify">running</td>
   * </tr>
   * <tr>
   * <td>F</td>
   * <td>failed</td>
   * </tr>
   * <tr>
   * <td>S</td>
   * <td>succeeded</td>
   * </tr>
   * </table>
   */
  @Column(name = "REF_STATUS")
  protected String refreshStatus;

  // =====================
  // ACCESSORS:
  // =====================

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public Date getStartTime() {
    return freshDate(startTime);
  }

  public void setStartTime(Date startTime) {
    this.startTime = freshDate(startTime);
  }

  public Date getEndTime() {
    return freshDate(endTime);
  }

  public void setEndTime(Date endTime) {
    this.endTime = freshDate(endTime);
  }

  public String getRefreshStatus() {
    return refreshStatus;
  }

  public void setRefreshStatus(String refreshStatus) {
    this.refreshStatus = refreshStatus;
  }

  // =====================
  // IDENTITY:
  // =====================

  @Override
  public Serializable getPrimaryKey() {
    return new VarargPrimaryKey(schemaName, DomainChef.cookTimestamp(startTime));
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE, true);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false);
  }

  @Override
  public Class<DatabaseResetEntry> getNormalizationClass() {
    return null;
  }

  @Override
  public Serializable getNormalizationGroupKey() {
    return null;
  }

  @Override
  public DatabaseResetEntry normalize(Map<Object, DatabaseResetEntry> arg0) {
    return null;
  }

}
