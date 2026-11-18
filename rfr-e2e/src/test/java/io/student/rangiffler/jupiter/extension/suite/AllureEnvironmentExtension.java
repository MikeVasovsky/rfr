package io.student.rangiffler.jupiter.extension.suite;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.FlywayAlignment;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Пишет {@code environment.properties} в каталог Allure-результатов в {@link #afterSuite}.
 * Регистрация — только ServiceLoader; в {@code META-INF/services} должна быть раньше guards,
 * чтобы хук успел зарегистрироваться до возможного падения их {@code beforeSuite}.
 */
public final class AllureEnvironmentExtension implements SuiteExtension {

  static final String RESULTS_DIRECTORY_PROPERTY = "allure.results.directory";
  static final String DEFAULT_RESULTS_DIRECTORY = "build/allure-results";
  static final String ENVIRONMENT_FILE = "environment.properties";

  @Override
  public void afterSuite(ExtensionContext context) {
    TestConfig config = TestConfig.getInstance();
    Properties properties = new Properties();
    properties.setProperty("frontUrl", config.frontUrl());
    properties.setProperty("authUrl", config.authUrl());
    properties.setProperty("apiUrl", config.apiUrl());
    properties.setProperty("browser", config.browser());
    properties.setProperty("headless", Boolean.toString(config.headless()));
    properties.setProperty("commit", System.getProperty("rfr.commit", ""));
    properties.setProperty("rangiffler-auth", FlywayAlignment.appliedLabel(Databases.AUTH));
    properties.setProperty("rangiffler-api", FlywayAlignment.appliedLabel(Databases.API));
    Path directory = resultsDirectory();
    try {
      Files.createDirectories(directory);
      try (OutputStream out = Files.newOutputStream(directory.resolve(ENVIRONMENT_FILE))) {
        properties.store(out, "rfr e2e environment");
      }
    } catch (IOException e) {
      throw new IllegalStateException(
          "Failed to write Allure " + ENVIRONMENT_FILE + " under " + directory.toAbsolutePath(),
          e
      );
    }
  }

  static Path resultsDirectory() {
    String configured = System.getProperty(RESULTS_DIRECTORY_PROPERTY);
    if (configured != null && !configured.isBlank()) {
      return Path.of(configured);
    }
    return Path.of(DEFAULT_RESULTS_DIRECTORY);
  }
}
