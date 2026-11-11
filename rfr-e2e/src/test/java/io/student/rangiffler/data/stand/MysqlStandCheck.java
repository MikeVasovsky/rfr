package io.student.rangiffler.data.stand;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * JDBC-проверка MySQL с коротким login/connect timeout (как mysqladmin ping по смыслу).
 */
public final class MysqlStandCheck implements StandCheck {

  private static final String START_COMMAND = "docker compose up -d --wait";

  private final String jdbcUrl;
  private final String user;
  private final String password;

  public MysqlStandCheck(String jdbcUrl, String user, String password) {
    this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl");
    this.user = Objects.requireNonNull(user, "user");
    this.password = Objects.requireNonNull(password, "password");
  }

  @Override
  public String name() {
    return "MySQL";
  }

  @Override
  public boolean ready() {
    Properties properties = new Properties();
    properties.setProperty("user", user);
    properties.setProperty("password", password);
    properties.setProperty("connectTimeout", "2000");
    properties.setProperty("socketTimeout", "2000");
    try (Connection ignored = DriverManager.getConnection(withTimeoutParams(jdbcUrl), properties)) {
      return true;
    } catch (SQLException ignored) {
      return false;
    }
  }

  @Override
  public String startCommand() {
    return START_COMMAND;
  }

  private static String withTimeoutParams(String jdbcUrl) {
    String sep = jdbcUrl.contains("?") ? "&" : "?";
    if (jdbcUrl.contains("connectTimeout=")) {
      return jdbcUrl;
    }
    return jdbcUrl + sep + "connectTimeout=2000&socketTimeout=2000";
  }
}
