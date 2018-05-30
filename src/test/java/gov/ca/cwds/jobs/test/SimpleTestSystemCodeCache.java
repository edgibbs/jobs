package gov.ca.cwds.jobs.test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import gov.ca.cwds.data.persistence.cms.SystemCodeDaoFileImpl;
import gov.ca.cwds.rest.api.domain.cms.SystemCode;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeDescriptor;
import gov.ca.cwds.rest.api.domain.cms.SystemMeta;

@SuppressWarnings("serial")
public class SimpleTestSystemCodeCache implements SystemCodeCache {

  private static SimpleTestSystemCodeCache instance;

  private final Map<Short, SystemCode> mapSysCodes;

  public static synchronized void init() {
    if (instance == null) {
      instance = new SimpleTestSystemCodeCache();
    }
  }

  public SimpleTestSystemCodeCache() {
    mapSysCodes = new SystemCodeDaoFileImpl().getAllSystemCodes().stream()
        .map(c -> new SystemCode((short) c.getSysId(), // system_id
            StringUtils.isNotBlank(c.getCategoryId()) ? Short.parseShort(c.getCategoryId()) : null, // category_id
            c.getInactvInd(), // inactive_indicator
            c.getOtherCd(), // other_cd
            c.getShortDsc(), // short_description
            c.getLgcId(), // logical_id
            "third_id", // third_id
            c.getFksMetaT(), // foreign_key_meta_table
            c.getLongDsc() // long_description
        )).collect(Collectors.toMap(SystemCode::getSystemId, e -> e));
    register();
  }

  @Override
  public Set<SystemCode> getAllSystemCodes() {
    Set<SystemCode> systemCodes = new HashSet<>();
    // Add META_A_ active codes
    for (int i = 1; i < 4; i++) {
      systemCodes.add(new SystemCode(new Integer(i).shortValue(), null, "N", null,
          ("DESCRIPTION_A" + i), null, null, "META_A", null));
    }

    // Add META_A_ inactive codes
    for (int i = 4; i < 7; i++) {
      systemCodes.add(new SystemCode(new Integer(i).shortValue(), null, "Y", null,
          ("DESCRIPTION_A" + i), null, null, "META_A", null));
    }

    // Add META_B_ active codes
    for (int i = 7; i < 10; i++) {
      systemCodes.add(new SystemCode(new Integer(i).shortValue(), null, "N", null,
          ("DESCRIPTION_B" + i), null, null, "META_B", null));
    }

    return systemCodes;
  }

  @Override
  public Set<SystemMeta> getAllSystemMetas() {
    final Set<SystemMeta> systemMetas = new HashSet<>();
    systemMetas.add(new SystemMeta("META_A", "META_A", "META_A_DESC"));
    systemMetas.add(new SystemMeta("META_B", "META_B", "META_B_DESC"));
    systemMetas.add(new SystemMeta("META_C", "META_C", "META_C_DESC"));
    return systemMetas;
  }

  @Override
  public SystemCode getSystemCode(Number id) {
    return id != null && id.shortValue() != 0 ? this.mapSysCodes.get(id.shortValue()) : null;
  }

  @Override
  public String getSystemCodeShortDescription(Number arg0) {
    return null;
  }

  @Override
  public Set<SystemCode> getSystemCodesForMeta(String arg0) {
    return null;
  }

  @Override
  public boolean verifyActiveSystemCodeDescriptionForMeta(String arg0, String arg1) {
    return false;
  }

  @Override
  public boolean verifyActiveLogicalIdForMeta(String arg0, String arg1) {
    return false;
  }

  @Override
  public boolean verifyActiveSystemCodeIdForMeta(Number arg0, String arg1, boolean arg2) {
    return false;
  }

  @Override
  public SystemCodeDescriptor getSystemCodeDescriptor(Number arg0) {
    return null;
  }

  @Override
  public Short getSystemCodeId(String metaId, String shortDescription) {
    return null;
  }
}
