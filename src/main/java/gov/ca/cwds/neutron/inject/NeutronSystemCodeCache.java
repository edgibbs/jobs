package gov.ca.cwds.neutron.inject;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.data.std.ApiObjectIdentity;
import gov.ca.cwds.rest.api.domain.cms.SystemCode;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeDescriptor;
import gov.ca.cwds.rest.api.domain.cms.SystemMeta;

/**
 * Murphy's Law: "If anything <strong>can</strong> go wrong, it <strong>will</strong>."
 * 
 * <p>
 * Simplistic code cache without refresh capability. Sufficient for Neutron "initial load" jobs,
 * whose system codes must not change while the job is running. Usually appropriate for on-going
 * Launch Command -- until system codes are modified by IBM quarterly release.
 * </p>
 * 
 * @author CWDS API Team
 */
public class NeutronSystemCodeCache extends ApiObjectIdentity implements SystemCodeCache {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronSystemCodeCache.class);

  // Core indexes.
  private Map<Short, SystemCode> mapCodeByKey;
  private Map<String, SystemMeta> mapMetaByKey;

  private Map<String, Set<SystemCode>> mapCodeByMeta;
  private Set<SystemCode> setCodes;
  private Set<SystemMeta> setMetas;

  public NeutronSystemCodeCache(SystemCodeCache sourceCache) {
    {
      final Set<SystemCode> codes = sourceCache.getAllSystemCodes();
      final Map<Short, SystemCode> theCodes = new HashMap<>(codes.size());
      codes.stream().forEach(x -> theCodes.put(x.getSystemId(), x));
      mapCodeByKey = Collections.unmodifiableMap(theCodes);
    }

    {
      final Set<SystemMeta> metas = sourceCache.getAllSystemMetas();
      final Map<String, SystemMeta> theMetas = new HashMap<>(metas.size());
      metas.stream().forEach(x -> theMetas.put(x.getLogicalTableDsdName(), x));
      mapMetaByKey = Collections.unmodifiableMap(theMetas);
    }

    {
      final Set<SystemCode> rawCodes = new HashSet<>(mapCodeByKey.size());
      this.mapCodeByKey.values().forEach(rawCodes::add);
      setCodes = Collections.unmodifiableSet(rawCodes);
    }

    {
      final Set<SystemMeta> rawMetas = new HashSet<>(mapMetaByKey.size());
      this.mapMetaByKey.values().forEach(rawMetas::add);
      setMetas = Collections.unmodifiableSet(rawMetas);
    }

    {
      final Map<String, Set<SystemCode>> codesByMeta = mapCodeByKey.values().stream()
          .collect(Collectors.groupingBy(SystemCode::getForeignKeyMetaTable,
              Collectors.mapping(Function.identity(), Collectors.<SystemCode>toSet())));
      mapCodeByMeta = Collections.unmodifiableMap(codesByMeta);
    }

    LOGGER.warn(
        "Neutron System code cache: mapCodeByKey: {}, mapMetaByKey: {}, setCodes: {}, setMetas: {}, mapCodeByMeta: {}",
        mapCodeByKey.size(), mapMetaByKey.size(), setCodes.size(), setMetas.size(),
        mapCodeByMeta.size());
  }

  @Override
  public Set<SystemMeta> getAllSystemMetas() {
    return setMetas;
  }

  @Override
  public Set<SystemCode> getAllSystemCodes() {
    return setCodes;
  }

  @Override
  public SystemCode getSystemCode(Number key) {
    return mapCodeByKey.get(key.shortValue());
  }

  @Override
  public String getSystemCodeShortDescription(Number key) {
    String ret = null;
    if (key != null && key.intValue() != 0) {
      final SystemCode code = mapCodeByKey.get(key.shortValue());
      ret = code != null ? code.getShortDescription() : null;
    }

    return ret;
  }

  @Override
  public SystemCodeDescriptor getSystemCodeDescriptor(Number key) {
    SystemCodeDescriptor ret = null;
    if (key != null && key.intValue() != 0) {
      final SystemCode code = mapCodeByKey.get(key.shortValue());
      ret = code != null ? code.getSystemCodeDescriptor() : null;
    }

    return ret;
  }

  @Override
  public Set<SystemCode> getSystemCodesForMeta(String key) {
    return mapCodeByMeta.get(key);
  }

  @Override
  public Short getSystemCodeId(String shortDescription, String meta) {
    return null; // not implemented
  }

  @Override
  public boolean verifyActiveSystemCodeIdForMeta(Number systemCodeId, String metaId,
      boolean checkCategoryIdValueIsZero) {
    return false; // not implemented
  }

  @Override
  public boolean verifyActiveLogicalIdForMeta(String logicalId, String metaId) {
    return false; // not implemented
  }

  @Override
  public boolean verifyActiveSystemCodeDescriptionForMeta(String shortDesc, String metaId) {
    return false; // not implemented
  }

}
