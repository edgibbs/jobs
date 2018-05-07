package gov.ca.cwds.data.persistence.cms;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Type;

import gov.ca.cwds.data.std.ApiMarker;

public class PlacementHomeAddress implements ApiMarker {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "CLIENT_ID")
  protected String clientId; // PLC_EPST.FKCLIENT_T

  @Id
  @Column(name = "THIRD_ID")
  protected String thirdId; // PLC_EPST.THIRD_ID

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
  protected String adrStreetNumber;

  @Column(name = "STREET_NM")
  @ColumnTransformer(read = "TRIM(STREET_NM)")
  protected String adrStreetName;

  @Column(name = "CITY_NM")
  protected String adrCity;

  @Type(type = "short")
  @Column(name = "STATE_C")
  protected Short adrState;

  @Column(name = "ZIP_NO")
  protected String adrZip;

  @Type(type = "short")
  @Column(name = "ZIP_SFX_NO")
  protected Short adrZip4;

  @Column(name = "LST_UPD_TS")
  protected Date lastUpdatedTime;

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getThirdId() {
    return thirdId;
  }

  public void setThirdId(String thirdId) {
    this.thirdId = thirdId;
  }

  public String getOtherHomePlacementId() {
    return otherHomePlacementId;
  }

  public void setOtherHomePlacementId(String otherHomePlacementId) {
    this.otherHomePlacementId = otherHomePlacementId;
  }

  public String getPlacementHomeId() {
    return placementHomeId;
  }

  public void setPlacementHomeId(String placementHomeId) {
    this.placementHomeId = placementHomeId;
  }

  public Short getPlacementEpisodeGovernmentEntityCd() {
    return placementEpisodeGovernmentEntityCd;
  }

  public void setPlacementEpisodeGovernmentEntityCd(Short placementEpisodeGovernmentEntityCd) {
    this.placementEpisodeGovernmentEntityCd = placementEpisodeGovernmentEntityCd;
  }

  public Short getPlacementHomeGovernmentEntityCd() {
    return placementHomeGovernmentEntityCd;
  }

  public void setPlacementHomeGovernmentEntityCd(Short placementHomeGovernmentEntityCd) {
    this.placementHomeGovernmentEntityCd = placementHomeGovernmentEntityCd;
  }

  public String getAdrStreetNumber() {
    return adrStreetNumber;
  }

  public void setAdrStreetNumber(String adrStreetNumber) {
    this.adrStreetNumber = adrStreetNumber;
  }

  public String getAdrStreetName() {
    return adrStreetName;
  }

  public void setAdrStreetName(String adrStreetName) {
    this.adrStreetName = adrStreetName;
  }

  public String getAdrCity() {
    return adrCity;
  }

  public void setAdrCity(String adrCity) {
    this.adrCity = adrCity;
  }

  public Short getAdrState() {
    return adrState;
  }

  public void setAdrState(Short adrState) {
    this.adrState = adrState;
  }

  public String getAdrZip() {
    return adrZip;
  }

  public void setAdrZip(String adrZip) {
    this.adrZip = adrZip;
  }

  public Short getAdrZip4() {
    return adrZip4;
  }

  public void setAdrZip4(Short adrZip4) {
    this.adrZip4 = adrZip4;
  }

  public Date getLastUpdatedTime() {
    return lastUpdatedTime;
  }

  public void setLastUpdatedTime(Date lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

}
