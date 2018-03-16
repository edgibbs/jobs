package gov.ca.cwds.jobs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.jobs.config.JobConfiguration;
import gov.ca.cwds.jobs.facility.FacilityRowMapper;
import gov.ca.cwds.jobs.util.JobReader;
import gov.ca.cwds.jobs.util.jdbc.JdbcJobReader;
import gov.ca.cwds.jobs.util.jdbc.RowMapper;
import gov.ca.cwds.neutron.jetpack.CheeseRay;

/**
 * @author CWDS Elasticsearch Team
 */
public class JdbcJobReaderTest extends Goddard {

  private static final Logger LOGGER = LoggerFactory.getLogger(JdbcJobReaderTest.class);

  File config;
  JobConfiguration jobConfiguration;
  FacilityRowMapper facilityRowMapper;
  SessionFactory sessionFactory;
  PreparedStatement statement;
  Function<Connection, PreparedStatement> func;
  JobConfiguration config_;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    config_ = mock(JobConfiguration.class);
    jobConfiguration = mock(JobConfiguration.class);
    facilityRowMapper = mock(FacilityRowMapper.class);
    sessionFactory = mock(SessionFactory.class);
    statement = mock(PreparedStatement.class);
    func = createPreparedStatementMaker();

    when(statement.executeQuery()).thenReturn(rs);
  }

  public Function<Connection, PreparedStatement> createPreparedStatementMaker() {
    return c -> {
      try {
        return c.prepareStatement("select * from test order by a");
      } catch (SQLException e) {
        throw CheeseRay.runtime(LOGGER, e, "FAILED TO PREPARE STATEMENT!", e.getMessage());
      }
    };
  }

  @Test
  public void testRead() throws Exception {
    SessionFactory sessionFactory =
        new Configuration().configure("test-lis-hibernate.cfg.xml").buildSessionFactory();
    JobReader<Item> jobReader = new JdbcJobReader<>(sessionFactory, new ItemMapper(), func);
    jobReader.init();
    int counter = 0;
    Item item;
    while ((item = jobReader.read()) != null) {
      counter++;
      Assert.assertEquals(counter, item.getA());
      Assert.assertEquals(String.valueOf(counter), item.getB());
    }
    Assert.assertEquals(3, counter);
  }

  private static class Item implements PersistentObject {

    private int a;
    private String b;

    public int getA() {
      return a;
    }

    public void setA(int a) {
      this.a = a;
    }

    public String getB() {
      return b;
    }

    public void setB(String b) {
      this.b = b;
    }

    @Override
    public Serializable getPrimaryKey() {
      return a;
    }

  }

  private static class ItemMapper implements RowMapper<Item> {

    @Override
    public Item mapRow(ResultSet resultSet) throws SQLException {
      Item item = new Item();
      item.setA(resultSet.getInt("a"));
      item.setB(resultSet.getString("b"));
      return item;
    }

  }
}
