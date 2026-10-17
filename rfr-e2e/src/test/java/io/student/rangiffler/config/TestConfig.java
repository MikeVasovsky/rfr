package io.student.rangiffler.config;

/**
 * Конфигурация тестового окружения: базовые URL фронтенда и auth-сервера
 * (из env {@code FRONT_URL}/{@code AUTH_URL} или localhost-дефолты).
 */
public final class TestConfig {

  private static final TestConfig INSTANCE = new TestConfig();

  private final String frontUrl;
  private final String authUrl;

  private TestConfig() {
    frontUrl = System.getenv().getOrDefault("FRONT_URL", "http://localhost:3001");
    authUrl = System.getenv().getOrDefault("AUTH_URL", "http://localhost:9001");
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
