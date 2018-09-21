package gov.ca.cwds.neutron.sqllite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SQLiteJDBC {

  static {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
  }

  public void createTable() {
    try (final Connection c = connect(); final Statement stmt = c.createStatement()) {
      System.out.println("Opened database successfully");

      //@formatter:off
       String sql = "CREATE TABLE COMPANY " +
                      "(ID INT PRIMARY KEY     NOT NULL," +
                      " NAME           TEXT    NOT NULL, " + 
                      " AGE            INT     NOT NULL, " + 
                      " ADDRESS        CHAR(50), " + 
                      " SALARY         REAL)"; 
       //@formatter:on

      stmt.executeUpdate(sql);
      stmt.close();
      // c.close();
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    System.out.println("Table created successfully");
  }

  public Connection connect() {
    Connection c = null;
    try {
      c = DriverManager.getConnection("jdbc:sqlite:test.db");
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }

    System.out.println("Opened database successfully");
    return c;
  }

  public static void main(String args[]) {
    final SQLiteJDBC inst = new SQLiteJDBC();
    inst.createTable();
  }

}
