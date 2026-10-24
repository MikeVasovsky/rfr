package io.student.rangiffler.config;

import java.util.Locale;

/**
 * Единая конфигурация e2e: URL сервисов и параметры Playwright.
 * Приоритет: system property {@code rfr.<key>} → env {@code RFR_<KEY>} → fallback-константа.
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

  private static final TestConfig INSTANCE = new TestConfig();

  private final String frontUrl;
  private final String authUrl;
  private final String apiUrl;
  private final String authJdbcUrl;
  private final String apiJdbcUrl;
  private final String jdbcUser;
  private final String jdbcPassword;
  private final String browser;
  private final boolean headless;
  private final int viewportWidth;
  private final int viewportHeight;
  private final double defaultTimeoutMs;

  private TestConfig() {
    frontUrl = value("frontUrl", DEFAULT_FRONT_URL);
    authUrl = value("authUrl", DEFAULT_AUTH_URL);
    apiUrl = value("apiUrl", DEFAULT_API_URL);
    authJdbcUrl = value("authJdbcUrl", DEFAULT_AUTH_JDBC_URL);
    apiJdbcUrl = value("apiJdbcUrl", DEFAULT_API_JDBC_URL);
    jdbcUser = value("jdbcUser", DEFAULT_JDBC_USER);
    jdbcPassword = value("jdbcPassword", DEFAULT_JDBC_PASSWORD);
    browser = value("browser", DEFAULT_BROWSER);
    headless = Boolean.parseBoolean(value("headless", DEFAULT_HEADLESS));
    viewportWidth = Integer.parseInt(value("viewportWidth", DEFAULT_VIEWPORT_WIDTH));
    viewportHeight = Integer.parseInt(value("viewportHeight", DEFAULT_VIEWPORT_HEIGHT));
    defaultTimeoutMs = Double.parseDouble(value("defaultTimeoutMs", DEFAULT_TIMEOUT_MS));
  }

  public static TestConfig getInstance() {
    return INSTANCE;
  }

  public String frontUrl() {
    return frontUrl;
  }

  public String authUrl() {
    return authUrl;
  }

  public String apiUrl() {
    return apiUrl;
  }

  public String authJdbcUrl() {
    return authJdbcUrl;
  }

  public String apiJdbcUrl() {
    return apiJdbcUrl;
  }

  public String jdbcUser() {
    return jdbcUser;
  }

  public String jdbcPassword() {
    return jdbcPassword;
  }

  public String browser() {
    return browser;
  }

  public boolean headless() {
    return headless;
  }

  public int viewportWidth() {
    return viewportWidth;
  }

  public int viewportHeight() {
    return viewportHeight;
  }

  public double defaultTimeoutMs() {
    return defaultTimeoutMs;
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
