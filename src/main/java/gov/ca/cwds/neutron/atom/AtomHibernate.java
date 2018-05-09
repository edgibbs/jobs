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
import org.hibernate.Transaction;
import org.slf4j.Logger;

import gov.ca.cwds.data.BaseDaoImpl;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.rocket.BasePersonRocket;
import gov.ca.cwds.neutron.util.jdbc.NeutronDB2Utils;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtils;

/**
 * Common functions and features for Hibernate calls.
 * 
 * @author CWDS API Team
 *
 * @param <T> normalized type
 * @param <M> de-normalized type or same as normalized type, if normalization not needed
 */
public interface AtomHibernate<T extends PersistentObject, M extends ApiGroupNormalizer<?>>
    extends AtomShared, AtomRowMapper<M> {

  /**
   * @return the rocket's main DAO.
   */
  BaseDaoImpl<T> getJobDao();

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
   * Identifier column for this table. Defaults to "IDENTIFIER", the common key name in legacy CMS.
   * 
   * @return Identifier column
   */
  default String getIdColumn() {
    return "IDENTIFIER";
  }

  /**
   * Get the legacy source table for this rocket, if any.
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
   * Convenient pass-through method for {@link NeutronDB2Utils#isDB2OnZOS(BaseDaoImpl)}.
   * 
   * @see NeutronDB2Utils#isDB2OnZOS(BaseDaoImpl)
   * @return true if DB2 on mainframe
   * @throws NeutronCheckedException on error
   */
  default boolean isDB2OnZOS() throws NeutronCheckedException {
    return NeutronDB2Utils.isDB2OnZOS(getJobDao());
  }

  /**
   * "Work-around" (gentle euphemism for <strong>HACK</strong>) for annoying condition where a
   * transaction should have started but did not.
   * 
   * <p>
   * Get the current transaction from the current session or start a new transaction.
   * </p>
   * 
   * @return current, active transaction
   */
  default Transaction grabTransaction() {
    Transaction txn = null;
    final Session session = getJobDao().getSessionFactory().getCurrentSession();
    try {
      txn = session.beginTransaction();
    } catch (Exception e) { // NOSONAR
      txn = session.getTransaction();
    }
    return txn;
  }

  /**
   * Detect large data sets on the mainframe.
   * 
   * <p>
   * <strong>HACK:</strong> also checks schema name. Add a "database version" table or something.
   * </p>
   * 
   * @return true if is large data set on z/OS
   * @throws NeutronCheckedException on error
   */
  default boolean isLargeDataSet() throws NeutronCheckedException {
    final String schema = getDBSchemaName().toUpperCase().trim();

    // Not the best idea to check by replication schema name, but lack other options.
    return isDB2OnZOS()
        && (schema.endsWith("RSQ") || schema.endsWith("REP") || schema.endsWith("RSS"));
  }

  /**
   * Return Function that creates a prepared statement for last change pre-processing, such as
   * inserting identifiers into a global temporary table.
   * 
   * @param sql SQL to prepare
   * @return prepared statement for last change pre-processing
   */
  default Function<Connection, PreparedStatement> getPreparedStatementMaker(String sql) {
    return c -> {
      final Logger log = getLogger();
      try {
        log.info("PREPARE LAST CHANGE SQL:\n\n{}\n", sql);
        return c.prepareStatement(sql);
      } catch (SQLException e) {
        throw CheeseRay.runtime(log, e, "FAILED TO PREPARE STATEMENT! SQL: {}", sql);
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
   * @param pSql optional, roll-your-own SQL
   */
  default void prepHibernateLastChange(final Session session, final Date lastRunTime, String pSql) {
    final String sql = StringUtils.isNotBlank(pSql) ? pSql.trim() : getPrepLastChangeSQL();
    if (StringUtils.isNotBlank(sql)) {
      NeutronJdbcUtils.prepHibernateLastChange(session, lastRunTime, sql,
          getPreparedStatementMaker(sql));
    }
  }

}
