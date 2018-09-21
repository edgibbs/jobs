package gov.ca.cwds.neutron.sqllite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SQLiteJDBC {

  static {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
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
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      e.printStackTrace();
    }

    System.out.println("Table created successfully");
  }

  public void insert() {
    try (final Connection c = connect(); final Statement stmt = c.createStatement()) {
      c.setAutoCommit(false);
      System.out.println("Opened database successfully");

      String sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
          + "VALUES (1, 'Paul', 32, 'California', 20000.00 );";
      stmt.executeUpdate(sql);

      sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
          + "VALUES (2, 'Allen', 25, 'Texas', 15000.00 );";
      stmt.executeUpdate(sql);

      sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
          + "VALUES (3, 'Teddy', 23, 'Norway', 20000.00 );";
      stmt.executeUpdate(sql);

      sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
          + "VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.00 );";
      stmt.executeUpdate(sql);

      c.commit();
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      e.printStackTrace();
    }

    System.out.println("Records created successfully");
  }

  public void select() {
    try (final Connection c = connect(); final Statement stmt = c.createStatement()) {
      c.setAutoCommit(false);
      System.out.println("Opened database successfully");
      final ResultSet rs = stmt.executeQuery("SELECT * FROM COMPANY;");

      while (rs.next()) {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int age = rs.getInt("age");
        String address = rs.getString("address");
        float salary = rs.getFloat("salary");

        System.out.println("ID = " + id);
        System.out.println("NAME = " + name);
        System.out.println("AGE = " + age);
        System.out.println("ADDRESS = " + address);
        System.out.println("SALARY = " + salary);
        System.out.println();
      }
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }

    System.out.println("Operation done successfully");
  }

  public Connection connect() {
    Connection c = null;
    try {
      c = DriverManager.getConnection("jdbc:sqlite:test.db");
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }

    System.out.println("Opened database successfully");
    return c;
  }

  public static void main(String args[]) {
    final SQLiteJDBC inst = new SQLiteJDBC();
    inst.createTable();
    inst.insert();
    inst.select();
  }

}
