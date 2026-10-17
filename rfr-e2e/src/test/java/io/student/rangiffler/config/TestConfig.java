package io.student.rangiffler.config;

/**
 * Конфигурация тестового окружения: базовые URL фронтенда и auth-сервера
 * из system properties {@code front.url}/{@code auth.url} или localhost-дефолты.
 */
public final class TestConfig {

  private static final TestConfig INSTANCE = new TestConfig();

  private final String frontUrl;
  private final String authUrl;

  private TestConfig() {
    frontUrl = System.getProperty("front.url", "http://localhost:3001");
    authUrl = System.getProperty("auth.url", "http://localhost:9001");
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
}
