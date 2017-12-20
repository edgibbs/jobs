package gov.ca.cwds.neutron.atom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.function.Function;

import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;

import gov.ca.cwds.data.BaseDaoImpl;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.exception.NeutronException;
import gov.ca.cwds.jobs.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.jobs.util.jdbc.NeutronRowMapper;
import gov.ca.cwds.neutron.jetpack.JobLogs;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

/**
 * Common functions and features for Hibernate calls.
 * 
 * @author CWDS API Team
 *
 * @param <T> normalized type
 * @param <M> de-normalized type or same as normalized type if normalization not needed
 */
public interface AtomHibernate<T extends PersistentObject, M extends ApiGroupNormalizer<?>>
    extends AtomShared, NeutronRowMapper<M> {

  /**
   * @return default CMS schema name
   */
  default String getDBSchemaName() {
    return System.getProperty("DB_CMS_SCHEMA");
  }

  /**
   * @return default CMS schema name
   */
  static String databaseSchemaName() {
    return System.getProperty("DB_CMS_SCHEMA");
  }

  /**
   * @return the job's main DAO.
   */
  BaseDaoImpl<T> getJobDao();

  /**
   * Identifier column for this table. Defaults to "IDENTIFIER", the most common key name in legacy
   * DB2.
   * 
   * @return Identifier column
   */
  default String getIdColumn() {
    return "IDENTIFIER";
  }

  /**
   * Get the legacy source table for this job, if any.
   * 
   * @return legacy source table
   */
  default String getLegacySourceTable() {
    return null;
  }

  /**
   * Get the table or view used to allocate bucket ranges. Called on full load only.
   * 
   * @return the table or view used to allocate bucket ranges
   */
  default String getDriverTable() {
    String ret = null;
    final Table tbl = getJobDao().getEntityClass().getDeclaredAnnotation(Table.class);
    if (tbl != null) {
      ret = tbl.name();
    }

    return ret;
  }

  /**
   * Optional method to customize JDBC ORDER BY clause on initial load.
   * 
   * @return custom ORDER BY clause for JDBC query
   */
  default String getJdbcOrderBy() {
    return null;
  }

  /**
   * Return SQL to run before SELECTing from a last change view.
   * 
   * @return prep SQL
   */
  default String getPrepLastChangeSQL() {
    return null;
  }

  @Override
  default M extract(final ResultSet rs) throws SQLException {
    return null;
  }

  /**
   * @see NeutronDB2Utils#isDB2OnZOS(BaseDaoImpl)
   * @return true if DB2 on mainframe
   * @throws NeutronException on error
   */
  default boolean isDB2OnZOS() throws NeutronException {
    return NeutronDB2Utils.isDB2OnZOS(getJobDao());
  }

  /**
   * Detect large data sets on the mainframe.
   * 
   * <p>
   * HACK: also checks schema name. Add a "database version" table or something.
   * </p>
   * 
   * @return true if is large data set on z/OS
   * @throws NeutronException on error
   */
  default boolean isLargeDataSet() throws NeutronException {
    final String schema = getDBSchemaName().toUpperCase().trim();
    return isDB2OnZOS() && (schema.endsWith("RSQ") || schema.endsWith("REP"));
  }

  /**
   * Return Function that creates a prepared statement for last change pre-processing, such as
   * inserting identifiers into a global temporary table.
   * 
   * @return prepared statement for last change pre-processing
   */
  default Function<Connection, PreparedStatement> getPreparedStatementMaker() {
    return c -> {
      try {
        return c.prepareStatement(getPrepLastChangeSQL());
      } catch (SQLException e) {
        throw JobLogs.runtime(getLogger(), e, "FAILED TO PREPARE STATEMENT! {}", e.getMessage());
      }
    };
  }

  /**
   * Execute JDBC prior to calling method {@link BasePersonRocket#pullBucketRange(String, String)}.
   * 
   * <blockquote>
   * 
   * <pre>
   * final Work work = new Work() {
   *   &#64;Override
   *   public void execute(Connection connection) throws SQLException {
   *     // Run JDBC here.
   *   }
   * };
   * session.doWork(work);
   * </pre>
   * 
   * </blockquote>
   * 
   * @param session current Hibernate session
   * @param lastRunTime last successful run datetime
   */
  default void prepHibernateLastChange(final Session session, final Date lastRunTime) {
    final String sql = getPrepLastChangeSQL();
    if (StringUtils.isNotBlank(sql)) {
      NeutronJdbcUtils.prepHibernateLastChange(session, lastRunTime, sql,
          getPreparedStatementMaker());
    }
  }

}
