package gov.ca.cwds.neutron.rocket;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;

/**
 * Drops and creates an Elasticsearch index, if requested.
 * 
 * @author CWDS API Team
 */
public abstract class IndexResetRocket
    extends BasePersonRocket<ReplicatedOtherAdultInPlacemtHome, ReplicatedOtherAdultInPlacemtHome> {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(IndexResetRocket.class);

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao arbitrary DAO to fulfill interface
   * @param esDao ElasticSearch DAO for the target index
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   */
  public IndexResetRocket(final ReplicatedOtherAdultInPlacemtHomeDao dao,
      final ElasticsearchDao esDao, final ObjectMapper mapper, FlightPlan flightPlan) {
    super(dao, esDao, flightPlan.getLastRunLoc(), mapper, flightPlan);
  }

  protected abstract String getIndexSettingsLocation();

  protected abstract String getDocumentMappingLocation();

  @Override
  public Date launch(Date lastRunDate) {
    LOGGER.info("INDEX CHECK!");

    try {
      // If index name is provided, use it, else take alias from ES config.
      final String indexNameOverride = getFlightPlan().getIndexName();
      String effectiveIndexName =
          StringUtils.isBlank(indexNameOverride) ? esDao.getConfig().getElasticsearchAlias()
              : indexNameOverride;

      if (!getFlightPlan().isLastRunMode()) {
        // Force new index name for Initial Load when Index name not provided
        if (StringUtils.isBlank(indexNameOverride)) {
          effectiveIndexName = effectiveIndexName.concat("_")
              .concat(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        }
        // Drop index first, if requested and name provided
        if (getFlightPlan().isDropIndex() && !StringUtils.isBlank(indexNameOverride)) {
          esDao.deleteIndex(effectiveIndexName);
        }
      } else {
        // Drop index first, if requested
        if (getFlightPlan().isDropIndex()) {
          esDao.deleteIndex(effectiveIndexName);
        }
      }

      getFlightPlan().setIndexName(effectiveIndexName);
      LaunchCommand.getInstance().getCommonFlightPlan().setIndexName(effectiveIndexName);

      // If the index is missing, create it.
      final String documentType = esDao.getConfig().getElasticsearchDocType();

      final String settingFile = StringUtils.isNotBlank(esDao.getConfig().getIndexSettingFile())
          ? esDao.getConfig().getIndexSettingFile()
          : getIndexSettingsLocation();

      final String mappingFile = StringUtils.isNotBlank(esDao.getConfig().getDocumentMappingFile())
          ? esDao.getConfig().getDocumentMappingFile()
          : getDocumentMappingLocation();

      LOGGER.warn(
          "\nCreate index if missing: \neffective index name: {}, \nalias: {}, \nsetting file: {}, \nmapping file: {}",
          effectiveIndexName, esDao.getConfig().getElasticsearchAlias(), settingFile, mappingFile);

      esDao.createIndexIfNeeded(effectiveIndexName, documentType, settingFile, mappingFile);
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "ES INDEX MANAGEMENT ERROR! {}", e.getMessage());
    }

    return lastRunDate;
  }

}
