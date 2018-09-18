package gov.ca.cwds.neutron.inject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.rest.api.domain.cms.SystemCode;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeDescriptor;
import gov.ca.cwds.rest.api.domain.cms.SystemMeta;

public class NeutronSystemCodeCache implements SystemCodeCache {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronSystemCodeCache.class);

  private Map<Short, SystemCode> syscodes;
  private Map<String, SystemMeta> sysmetas;

  public NeutronSystemCodeCache(SystemCodeCache sourceCache) {
    final Set<SystemCode> codes = sourceCache.getAllSystemCodes();
    syscodes = new HashMap<>(codes.size());
    codes.stream().forEach(x -> syscodes.put(x.getSystemId(), x));

    final Set<SystemMeta> metas = sourceCache.getAllSystemMetas();
    sysmetas = new HashMap<>(metas.size());
    metas.stream().forEach(x -> sysmetas.put(x.getLogicalTableDsdName(), x));

    LOGGER.warn("");
  }

  @Override
  public Set<SystemMeta> getAllSystemMetas() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<SystemCode> getAllSystemCodes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SystemCode getSystemCode(Number var1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<SystemCode> getSystemCodesForMeta(String var1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getSystemCodeShortDescription(Number var1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Short getSystemCodeId(String var1, String var2) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SystemCodeDescriptor getSystemCodeDescriptor(Number var1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean verifyActiveSystemCodeIdForMeta(Number var1, String var2, boolean var3) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean verifyActiveLogicalIdForMeta(String var1, String var2) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean verifyActiveSystemCodeDescriptionForMeta(String var1, String var2) {
    // TODO Auto-generated method stub
    return false;
  }

}
