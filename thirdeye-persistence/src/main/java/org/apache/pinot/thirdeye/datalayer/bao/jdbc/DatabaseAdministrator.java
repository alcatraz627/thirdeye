package org.apache.pinot.thirdeye.datalayer.bao.jdbc;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

@Singleton
public class DatabaseAdministrator {

  private final DataSource dataSource;

  @Inject
  public DatabaseAdministrator(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public ResultSet executeQuery(String sql) {
    try (Connection connection = dataSource.getConnection()) {
      final Statement statement = connection.createStatement();
      return statement.executeQuery(sql);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<String> getTables() throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData md = connection.getMetaData();
      ResultSet rs = md.getTables(null, null, "%", null);

      List<String> tables = new ArrayList<>();
      while (rs.next()) {
        tables.add(rs.getString(3));
      }
      return tables;
    }
  }

  public void truncateTables() throws SQLException {
    runOnAllTables("TRUNCATE TABLE");
  }

  public void dropTables() throws SQLException {
    runOnAllTables("DROP TABLE");
  }

  private void runOnAllTables(final String command) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      String databaseName = connection.getCatalog();
      for (String table : getTables()) {
        connection
            .createStatement()
            .executeUpdate(String.format("%s %s.%s", command, databaseName, table));
      }
    }
  }
}