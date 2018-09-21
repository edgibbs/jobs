package gov.ca.cwds.neutron.sqllite;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLiteJDBC {

  public static void main(String args[]) {

    try {
      Class.forName("org.sqlite.JDBC");
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }

    try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    System.out.println("Opened database successfully");
  }

}
