package gov.ca.cwds.jobs.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.jobs.util.JobReader;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.jetpack.CheeseRay;

/**
 * @param <T> persistence class
 * @author CWDS Elasticsearch Team
 */
public class JdbcJobReader<T extends PersistentObject> implements JobReader<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(JdbcJobReader.class);

  private SessionFactory sessionFactory;
  private ResultSet resultSet;
  private RowMapper<T> rowMapper;
  private PreparedStatement statement;
  private final String query;
  private final Function<Connection, PreparedStatement> statementMaker;

  @Inject
  public JdbcJobReader(SessionFactory sessionFactory, RowMapper<T> rowMapper,
      Function<Connection, PreparedStatement> statementMaker) {
    this.sessionFactory = sessionFactory;
    this.rowMapper = rowMapper;
    this.query = null;
    this.statementMaker = statementMaker;
  }

  /**
   * SonarQube complains loudly about a "vulnerability" with
   * {@code connection.prepareStatement(query)}.
   */
  @Override
  public void init() throws NeutronCheckedException {
    try {
      final Connection con = sessionFactory.getSessionFactoryOptions().getServiceRegistry()
          .getService(ConnectionProvider.class).getConnection();
      con.setAutoCommit(false);
      con.setReadOnly(true); // may fail in some situations.

      // SonarQube complains loudly about this "vulnerability."
      statement = statementMaker.apply(con);
      statement.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());
      statement.setMaxRows(0);
      statement.setQueryTimeout(100000);
      resultSet = statement.executeQuery();
    } catch (SQLException e) {
      destroy();
      throw CheeseRay.checked(LOGGER, e, "JDBC READ INIT ERROR! {}", e.getMessage());
    }
  }

  @Override
  public T read() throws NeutronCheckedException {
    try {
      return resultSet.next() ? rowMapper.mapRow(resultSet) : null;
    } catch (SQLException e) {
      throw CheeseRay.checked(LOGGER, e, "JDBC READ ERROR! {}", e.getMessage());
    }
  }

  @Override
  public void destroy() throws NeutronCheckedException {
    try {
      if (statement != null) {
        statement.close();
        statement = null;
      }
    } catch (SQLException e) {
      throw CheeseRay.checked(LOGGER, e, "JDBC DESTROY ERROR! {}", e.getMessage());
    } finally {
      sessionFactory.close();
    }
  }

  public String getQuery() {
    return query;
  }

}
