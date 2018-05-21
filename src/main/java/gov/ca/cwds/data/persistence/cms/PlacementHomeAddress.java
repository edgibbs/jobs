package gov.ca.cwds.data.persistence.cms;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Id;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Type;

import gov.ca.cwds.data.legacy.cms.entity.converter.ZipCodeConverter;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClientAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedPlacementHomeAddress;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.data.std.ApiObjectIdentity;

/**
 * Represents a placement home as a recent address per, HOT-1885 and rule R-02294, Client Abstract
 * Most Recent Address.
 * 
 * <p>
 * Normalizes to {@link ReplicatedClientAddress}.
 * </p>
 * 
 * @author CWDS API Team
 */
public class PlacementHomeAddress extends ApiObjectIdentity
    implements PersistentObject, ApiGroupNormalizer<ReplicatedClientAddress> {

  private static final long serialVersionUID = 1L;

  // TOD0: Code cache is unhappy with this unknown value.
  // No code loader interface method to add custom, non-CMS codes.
  // public static final short ADDRESS_TYPE_PLACEMENT_HOME = 10001;
  public static final short ADDRESS_TYPE_PLACEMENT_HOME = 32; // call it a residence

  @Id
  @Column(name = "CLIENT_ID")
  protected String clientId; // PLC_EPST.FKCLIENT_T, PLC_EPST composite key

  @Id
  @Column(name = "PE_THIRD_ID")
  protected String peThirdId; // PLC_EPST.THIRD_ID, PLC_EPST composite key

  @Id
  @Column(name = "OHP_ID")
  protected String otherHomePlacementId; // O_HM_PLT.IDENTIFIER

  @Id
  @Column(name = "PH_ID")
  protected String placementHomeId; // PLC_HM_T.IDENTIFIER

  @Type(type = "short")
  @Column(name = "PE_GVR_ENTC")
  protected Short placementEpisodeGovernmentEntityCd;

  @Type(type = "short")
  @Column(name = "PH_GVR_ENTC")
  protected Short placementHomeGovernmentEntityCd;

  @Column(name = "STREET_NO")
  @ColumnTransformer(read = "TRIM(STREET_NO)")
  protected String streetNumber;

  @Column(name = "STREET_NM")
  @ColumnTransformer(read = "TRIM(STREET_NM)")
  protected String streetName;

  @Column(name = "CITY_NM")
  @ColumnTransformer(read = "TRIM(CITY_NM)")
  protected String city;

  @Type(type = "short")
  @Column(name = "STATE_C")
  protected Short state;

  @Column(name = "ZIP_NO")
  protected Integer zip;

  @Type(type = "short")
  @Column(name = "ZIP_SFX_NO")
  protected Short zip4;

  @Column(name = "START_DT")
  protected Date start;

  @Column(name = "END_DT")
  protected Date end;

  @Column(name = "LST_UPD_TS")
  protected Date lastUpdatedTime;

  @Column(name = "PRM_TEL_NO", nullable = false)
  protected Long primaryNumber;

  @Type(type = "integer")
  @Column(name = "PRM_EXT_NO", nullable = false)
  protected Integer primaryExtension;

  public PlacementHomeAddress(ResultSet rs) throws SQLException {
    this.clientId = ifNull(rs.getString("CLIENT_ID"));
    this.peThirdId = ifNull(rs.getString("PE_THIRD_ID"));
    this.otherHomePlacementId = ifNull(rs.getString("PH_ID"));
    this.placementHomeId = ifNull(rs.getString("OHP_ID"));

    this.placementHomeGovernmentEntityCd = rs.getShort("PH_GVR_ENTC");
    this.placementEpisodeGovernmentEntityCd = rs.getShort("PE_GVR_ENTC");

    this.city = ifNull(rs.getString("CITY_NM"));
    this.state = rs.getShort("STATE_C");
    this.streetName = ifNull(rs.getString("STREET_NM"));
    this.streetNumber = ifNull(rs.getString("STREET_NO"));
    this.zip = rs.getInt("ZIP_NO");
    this.zip4 = rs.getShort("ZIP_SFX_NO");

    this.primaryNumber = rs.getLong("PRM_TEL_NO");
    this.primaryExtension = rs.getInt("PRM_EXT_NO");

    this.lastUpdatedTime = rs.getTimestamp("PH_LST_UPD_TS");
  }

  /**
   * Convert to {@link ReplicatedClientAddress}.
   * 
   * @return an equivalent ReplicatedClientAddress
   */
  public ReplicatedClientAddress toReplicatedClientAddress() {
    final ReplicatedClientAddress ret = new ReplicatedClientAddress();

    ret.setAddressType(ADDRESS_TYPE_PLACEMENT_HOME);
    ret.setEffEndDt(end);
    ret.setEffStartDt(start);
    ret.setId(placementHomeId);
    ret.setLastUpdatedTime(lastUpdatedTime);
    ret.setFkClient(clientId);

    final ReplicatedPlacementHomeAddress addr = new ReplicatedPlacementHomeAddress();
    ret.addAddress(addr);

    addr.setCity(city);
    addr.setGovernmentEntityCd(placementEpisodeGovernmentEntityCd);
    addr.setState(state);
    addr.setId(placementHomeId);
    addr.setLastUpdatedTime(lastUpdatedTime);
    addr.setStreetNumber(streetNumber);
    addr.setStreetName(streetName);
    addr.getLegacyDescriptor();

    addr.setPrimaryNumber(primaryNumber);
    addr.setPrimaryExtension(primaryExtension);

    if (zip != null) {
      addr.setZip(new ZipCodeConverter().convertToEntityAttribute(zip));
      addr.setZip4(zip4);
    }

    return ret;
  }

  public String getClientId() {
    return clientId;
  }

  public String getThirdId() {
    return peThirdId;
  }

  public String getOtherHomePlacementId() {
    return otherHomePlacementId;
  }

  public String getPlacementHomeId() {
    return placementHomeId;
  }

  public Short getPlacementEpisodeGovernmentEntityCd() {
    return placementEpisodeGovernmentEntityCd;
  }

  public Short getPlacementHomeGovernmentEntityCd() {
    return placementHomeGovernmentEntityCd;
  }

  public String getStreetNumber() {
    return streetNumber;
  }

  public String getStreetName() {
    return streetName;
  }

  public String getCity() {
    return city;
  }

  public Short getState() {
    return state;
  }

  public Integer getZip() {
    return zip;
  }

  public Short getZip4() {
    return zip4;
  }

  public Date getLastUpdatedTime() {
    return lastUpdatedTime;
  }

  public Date getStart() {
    return start;
  }

  public Date getEnd() {
    return end;
  }

  public Long getPrimaryNumber() {
    return primaryNumber;
  }

  public Integer getPrimaryExtension() {
    return primaryExtension;
  }

  @Override
  public Class<ReplicatedClientAddress> getNormalizationClass() {
    return ReplicatedClientAddress.class;
  }

  @Override
  public Serializable getNormalizationGroupKey() {
    return this.clientId;
  }

  @Override
  public ReplicatedClientAddress normalize(Map<Object, ReplicatedClientAddress> arg0) {
    return toReplicatedClientAddress();
  }

  @Override
  public Serializable getPrimaryKey() {
    return new VarargPrimaryKey(clientId, peThirdId, otherHomePlacementId, placementHomeId);
  }

}
