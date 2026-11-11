package io.student.rangiffler.config;

import java.util.Locale;

/**
 * Единая конфигурация e2e: URL сервисов и параметры Playwright.
 * Приоритет: system property {@code rfr.<key>} → env {@code RFR_<KEY>} → fallback-константа.
 * Значения читаются на каждый вызов (удобно для EngineTestKit и -D в прогоне).
 */
public final class TestConfig {

  private static final String DEFAULT_FRONT_URL = "http://localhost:3001";
  private static final String DEFAULT_AUTH_URL = "http://localhost:9001";
  private static final String DEFAULT_API_URL = "http://localhost:8081";
  private static final String DEFAULT_AUTH_JDBC_URL =
      "jdbc:mysql://localhost:3306/rangiffler-auth?serverTimezone=UTC";
  private static final String DEFAULT_API_JDBC_URL =
      "jdbc:mysql://localhost:3306/rangiffler-api?serverTimezone=UTC";
  private static final String DEFAULT_JDBC_USER = "root";
  private static final String DEFAULT_JDBC_PASSWORD = "secret";
  private static final String DEFAULT_BROWSER = "chromium";
  private static final String DEFAULT_HEADLESS = "true";
  private static final String DEFAULT_VIEWPORT_WIDTH = "1280";
  private static final String DEFAULT_VIEWPORT_HEIGHT = "720";
  private static final String DEFAULT_TIMEOUT_MS = "15000";
  private static final String DEFAULT_STAND_TIMEOUT_SECONDS = "180";
  private static final String DEFAULT_STAND_CHECK = "true";

  private static final TestConfig INSTANCE = new TestConfig();

  private TestConfig() {
  }

  public static TestConfig getInstance() {
    return INSTANCE;
  }

  public String frontUrl() {
    return value("frontUrl", DEFAULT_FRONT_URL);
  }

  public String authUrl() {
    return value("authUrl", DEFAULT_AUTH_URL);
  }

  public String apiUrl() {
    return value("apiUrl", DEFAULT_API_URL);
  }

  public String authJdbcUrl() {
    return value("authJdbcUrl", DEFAULT_AUTH_JDBC_URL);
  }

  public String apiJdbcUrl() {
    return value("apiJdbcUrl", DEFAULT_API_JDBC_URL);
  }

  public String jdbcUser() {
    return value("jdbcUser", DEFAULT_JDBC_USER);
  }

  public String jdbcPassword() {
    return value("jdbcPassword", DEFAULT_JDBC_PASSWORD);
  }

  public String browser() {
    return value("browser", DEFAULT_BROWSER);
  }

  public boolean headless() {
    return Boolean.parseBoolean(value("headless", DEFAULT_HEADLESS));
  }

  public int viewportWidth() {
    return Integer.parseInt(value("viewportWidth", DEFAULT_VIEWPORT_WIDTH));
  }

  public int viewportHeight() {
    return Integer.parseInt(value("viewportHeight", DEFAULT_VIEWPORT_HEIGHT));
  }

  public double defaultTimeoutMs() {
    return Double.parseDouble(value("defaultTimeoutMs", DEFAULT_TIMEOUT_MS));
  }

  public long standTimeoutSeconds() {
    return Long.parseLong(value("standTimeout", DEFAULT_STAND_TIMEOUT_SECONDS));
  }

  public boolean standCheck() {
    return Boolean.parseBoolean(value("standCheck", DEFAULT_STAND_CHECK));
  }

  static String value(String key, String fallback) {
    String property = System.getProperty("rfr." + key);
    if (property != null && !property.isBlank()) {
      return property;
    }
    String variable = System.getenv("RFR_" + key.replaceAll("([A-Z])", "_$1").toUpperCase(Locale.ROOT));
    if (variable != null && !variable.isBlank()) {
      return variable;
    }
    return fallback;
  }
}
