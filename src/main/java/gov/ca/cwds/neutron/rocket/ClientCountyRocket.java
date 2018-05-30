package gov.ca.cwds.neutron.rocket;

import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.ParameterMode;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.procedure.ProcedureCall;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.cms.EsClientAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.inject.CmsSessionFactory;
import gov.ca.cwds.jobs.ClientIndexerJob;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomRowMapper;
import gov.ca.cwds.neutron.atom.AtomValidateDocument;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

/**
 * Populates the Client County table via stored procedure.
 * 
 * @author CWDS API Team
 */
public class ClientCountyRocket extends ClientIndexerJob
    implements AtomRowMapper<EsClientAddress>, AtomValidateDocument {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(ClientCountyRocket.class);

  private static final String INSERT_CLIENT_INITIAL_LOAD =
      "INSERT INTO GT_ID (IDENTIFIER)\n" + "SELECT x.IDENTIFIER \nFROM CLIENT_T x\n"
          + "WHERE x.IDENTIFIER BETWEEN ':fromId' AND ':toId' ";

  private AtomicInteger nextThreadNum = new AtomicInteger(0);

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao Client DAO
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param sessionFactory Hibernate session factory
   * @param flightPlan command line options
   * @param launchDirector launch director
   */
  @Inject
  public ClientCountyRocket(final ReplicatedClientDao dao, final ElasticsearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper,
      @CmsSessionFactory SessionFactory sessionFactory, FlightPlan flightPlan,
      AtomLaunchDirector launchDirector) {
    super(dao, esDao, lastRunFile, mapper, flightPlan, launchDirector);
  }

  /**
   * NEXT: turn into a fixed rocket setting, not override method.
   */
  @Override
  public String getInitialLoadViewName() {
    return "CLIENT_T";
  }

  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    return INSERT_CLIENT_INITIAL_LOAD;
  }

  /**
   * NEXT: turn into a fixed rocket setting, not override method.
   */
  @Override
  public String getJdbcOrderBy() {
    return " ORDER BY x.IDENTIFIER ";
  }

  protected void processStatement(final Pair<String, String> p, final Connection con)
      throws SQLException {
    try (Statement stmt = con.createStatement()) {
      stmt.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue()); // faster
      stmt.setMaxRows(0);
      stmt.setQueryTimeout(0);

      grabTransaction(); // HACK: fix Hibernate DAO.
      getFlightLog().markRangeStart(p);
      final String query = getInitialLoadQuery(getDBSchemaName()).replaceAll(":fromId", p.getLeft())
          .replaceAll(":toId", p.getRight());
      getLogger().info("query: {}", query);
      stmt.executeUpdate(query); // NOSONAR

      callProc();
      con.commit();
    } catch (Exception e) {
      LOGGER.error("ERROR CALLING CLIENT COUNTY PROC! SQL msg: {}", e.getMessage(), e);
      con.rollback(); // Clear cursors.
      throw e;
    }
  }

  /**
   * Read records from the given key range, typically within a single partition on large tables.
   * @param p partition range to read
   */
  @Override
  public List<ReplicatedClient> pullRange(final Pair<String, String> p, String sql) {
    final String threadName =
        "extract_" + nextThreadNumber() + "_" + p.getLeft() + "_" + p.getRight();
    nameThread(threadName);
    getLogger().info("BEGIN: extract thread {}", threadName);

    try (Connection con = NeutronJdbcUtils.prepConnection(getJobDao().getSessionFactory())) {
      processStatement(p, con);
    } catch (Exception e) {
      fail();
      throw CheeseRay.runtime(LOGGER, e, "PROC ERROR ON RANGE! {}-{} : {}", p.getLeft(),
          p.getRight(), e.getMessage());
    }

    return new ArrayList<>();
  }

  /**
   * Proc call for initial load: {@code CALL CWSRSQ.PRCCLNCNTY ('O', '', '', ?);}
   * 
   * <p>
   * Incremental changes are called by table trigger, not by batch.
   * </p>
   */
  protected void callProc() {
    if (LaunchCommand.isInitialMode()) { // initial mode only
      LOGGER.info("Call stored proc");
      final SessionFactory sessionFactory = getJobDao().getSessionFactory();
      final Session session = sessionFactory.getCurrentSession();
      grabTransaction(); // HACK. move to base DAO.
      final String schema = (String) sessionFactory.getProperties().get("hibernate.default_schema");

      final ProcedureCall proc = session.createStoredProcedureCall(schema + ".PRCCLNCNTY");
      proc.registerStoredProcedureParameter("PARM_CRUD", String.class, ParameterMode.IN);
      proc.registerStoredProcedureParameter("PARM_ID", String.class, ParameterMode.IN);
      proc.registerStoredProcedureParameter("PARM_TRIGTBL", String.class, ParameterMode.IN);
      proc.registerStoredProcedureParameter("RETCODE", Integer.class, ParameterMode.OUT);

      proc.setParameter("PARM_CRUD", "O");
      proc.setParameter("PARM_ID", "");
      proc.setParameter("PARM_TRIGTBL", "");
      proc.setCacheable(false);
      proc.execute();

      final Integer origOut = (Integer) proc.getOutputParameterValue("RETCODE");
      if (origOut == null) {
        throw CheeseRay.runtime(getLogger(), "RETCODE IS NULL???");
      }

      final int retcode = origOut.intValue();
      LOGGER.info("Client county proc: retcode: {}", retcode);

      if (retcode != 0) {
        throw CheeseRay.runtime(getLogger(), "PROC FAILED! {}", retcode);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void threadRetrieveByJdbc() {
    pullMultiThreadJdbc();
  }

  @Override
  public int nextThreadNumber() {
    return nextThreadNum.incrementAndGet();
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(ClientCountyRocket.class, args);
  }

}
