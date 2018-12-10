package gov.ca.cwds.neutron.rocket;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.dao.cms.ReplicatedOtherAdultInPlacemtHomeDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedOtherAdultInPlacemtHome;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.rest.ElasticsearchConfiguration;

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
   * @param launchDirector launch director
   */
  public IndexResetRocket(final ReplicatedOtherAdultInPlacemtHomeDao dao,
      final ElasticsearchDao esDao, final ObjectMapper mapper, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, flightPlan.getLastRunLoc(), mapper, flightPlan, launchDirector);
  }

  /**
   * Where are the index's settings file?
   * 
   * @return path to index settings file
   */
  protected abstract String getIndexSettingsLocation();

  /**
   * Where are the index's mapping file?
   * 
   * @return path to index mapping file
   */
  protected abstract String getDocumentMappingLocation();

  @Override
  public Date launch(Date lastRunDate) {
    LOGGER.info("INDEX CHECK!");
    final FlightPlan plan = getFlightPlan();

    // If a range was requested, then don't create a new index or swap aliases.
    if (plan.isRangeGiven()) {
      return lastRunDate;
    }

    try {
      // If index name is provided, use it, else take alias from ES config.
      final String indexNameOverride = plan.getIndexName();
      String effectiveIndexName =
          StringUtils.isBlank(indexNameOverride) ? esDao.getConfig().getElasticsearchAlias()
              : indexNameOverride;

      if (!plan.isLastRunMode()) {
        // Force new index name for Initial Load when index name not provided.
        if (StringUtils.isBlank(indexNameOverride)) {
          effectiveIndexName = effectiveIndexName.concat("_")
              .concat(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        }

        // Initial Load no longer drops indexes. Intentionally a manual step.
        if (plan.isDropIndex() && !StringUtils.isBlank(indexNameOverride)) {
          esDao.deleteIndex(effectiveIndexName);
        }
      } else {
        // Drop index first, if requested.
        if (plan.isDropIndex()) {
          esDao.deleteIndex(effectiveIndexName);
        }
      }

      plan.setIndexName(effectiveIndexName);
      LaunchCommand.getInstance().getCommonFlightPlan().setIndexName(effectiveIndexName);

      // If the index is missing, create it.
      final ElasticsearchConfiguration config = esDao.getConfig();
      final String documentType = config.getElasticsearchDocType();

      final String settingFile =
          StringUtils.isNotBlank(config.getIndexSettingFile()) ? config.getIndexSettingFile()
              : getIndexSettingsLocation();

      final String mappingFile =
          StringUtils.isNotBlank(config.getDocumentMappingFile()) ? config.getDocumentMappingFile()
              : getDocumentMappingLocation();

      LOGGER.warn(
          "\nCreate index if missing: \neffective index name: {}, \nalias: {}, \nsetting file: {}, \nmapping file: {}",
          effectiveIndexName, config.getElasticsearchAlias(), settingFile, mappingFile);

      esDao.createIndexIfNeeded(effectiveIndexName, documentType, settingFile, mappingFile);
    } catch (Exception e) {
      throw CheeseRay.runtime(LOGGER, e, "ES INDEX MANAGEMENT ERROR! {}", e.getMessage());
    }

    return lastRunDate;
  }

}
