package gov.ca.cwds.data.persistence.cms;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;

/**
 * Parse legacy relationship strings in the format of "left/right (context)", such as
 * "Mother/Daughter (birth)".
 * 
 * @author CWDS API Team
 */
public final class CmsRelationship implements ApiMarker {

  private static final long serialVersionUID = 1L;

  private static final Pattern RGX_RELATIONSHIP = Pattern
      .compile("^\\s*([A-Za-z0-9 _-]+)[/]?([A-Za-z0-9 _-]+)?\\s*(\\([A-Za-z0-9 _-]+\\))?\\s*$"); // NOSONAR

  short sysCodeId;
  String primaryRel = "";
  String secondaryRel = "";
  String relContext = "";

  /**
   * Construct a relationship parser from a system code id.
   * 
   * @param relCode legacy system code id
   */
  public CmsRelationship(final Short relCode) {
    sysCodeId = relCode.shortValue();
    final gov.ca.cwds.rest.api.domain.cms.SystemCode code =
        SystemCodeCache.global().getSystemCode(relCode);
    final String wholeRel = ifNull(code.getShortDescription());

    final Matcher m = RGX_RELATIONSHIP.matcher(wholeRel);
    if (m.matches()) {
      for (int i = 0; i <= m.groupCount(); i++) {
        final String s = m.group(i);
        switch (i) {
          case 1:
            primaryRel = s.trim();
            break;

          case 2:
            secondaryRel = s.trim();
            break;

          case 3:
            relContext =
                StringUtils.isNotBlank(s) ? s.replaceAll("\\(", "").replaceAll("\\)", "").trim()
                    : "";
            break;

          default:
            break;
        }
      }
    }
  }

  public short getSysCodeId() {
    return sysCodeId;
  }

  public void setSysCodeId(short sysCodeId) {
    this.sysCodeId = sysCodeId;
  }

  public String getPrimaryRel() {
    return primaryRel;
  }

  public void setPrimaryRel(String primaryRel) {
    this.primaryRel = primaryRel;
  }

  public String getSecondaryRel() {
    return secondaryRel;
  }

  public void setSecondaryRel(String secondaryRel) {
    this.secondaryRel = secondaryRel;
  }

  public String getRelContext() {
    return relContext;
  }

  public void setRelContext(String relContext) {
    this.relContext = relContext;
  }

  @Override
  public String toString() {
    return "CmsRelationship [sysCodeId=" + sysCodeId + ", primaryRel=" + primaryRel
        + ", secondaryRel=" + secondaryRel + ", relContext=" + relContext + "]";
  }

}
