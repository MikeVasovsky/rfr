package io.student.rangiffler.test.extension.allure;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.probe.allure.AllureEnvironmentCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * EngineTestKit-мишень: после прогона во временном каталоге есть {@code environment.properties}.
 * {@link Isolated}: подмена {@code rfr.*} / {@code allure.results.directory} не должна
 * пересекаться с параллельным suite.
 */
@Tag("fake")
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
class AllureEnvironmentExtensionTest {

  private static final Set<String> REQUIRED_KEYS = Set.of(
      "frontUrl",
      "authUrl",
      "apiUrl",
      "browser",
      "headless",
      "commit",
      "rangiffler-auth",
      "rangiffler-api"
  );

  @TempDir
  Path tempDir;

  @Test
  @DisplayName("После прогона во временном каталоге есть environment.properties со всеми ключами")
  void writesEnvironmentPropertiesWithAllKeys() throws Exception {
    Path resultsDir = tempDir.resolve("allure-results");
    EngineExecutionResults results = execute(
        AllureEnvironmentCase.class,
        Map.of(
            "allure.results.directory", resultsDir.toString(),
            "rfr.schemaCheck", "false",
            "rfr.standCheck", "false",
            "rfr.commit", "abc1234",
            "rfr.frontUrl", "http://front.test",
            "rfr.authUrl", "http://auth.test",
            "rfr.apiUrl", "http://api.test",
            "rfr.browser", "chromium",
            "rfr.headless", "true"
        )
    );
    Allure.step("Мишень успешна", () ->
        results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1))
    );
    Path environmentFile = resultsDir.resolve("environment.properties");
    Allure.step("Файл environment.properties существует", () ->
        assertTrue(Files.isRegularFile(environmentFile), "missing " + environmentFile)
    );
    Properties properties = load(environmentFile);
    Allure.step("Все ключи окружения на месте", () -> {
      for (String key : REQUIRED_KEYS) {
        assertTrue(properties.containsKey(key), "missing key: " + key);
      }
    });
    Allure.step("Значения из TestConfig / rfr.commit", () -> {
      assertEquals("http://front.test", properties.getProperty("frontUrl"));
      assertEquals("http://auth.test", properties.getProperty("authUrl"));
      assertEquals("http://api.test", properties.getProperty("apiUrl"));
      assertEquals("chromium", properties.getProperty("browser"));
      assertEquals("true", properties.getProperty("headless"));
      assertEquals("abc1234", properties.getProperty("commit"));
    });
  }

  private static Properties load(Path file) throws IOException {
    Properties properties = new Properties();
    try (InputStream in = Files.newInputStream(file)) {
      properties.load(in);
    }
    return properties;
  }

  private static EngineExecutionResults execute(Class<?> testClass, Map<String, String> properties) {
    Map<String, String> previous = new java.util.HashMap<>();
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      previous.put(entry.getKey(), System.getProperty(entry.getKey()));
      System.setProperty(entry.getKey(), entry.getValue());
    }
    try {
      return EngineTestKit.engine("junit-jupiter")
          .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "true")
          .selectors(selectClass(testClass))
          .execute();
    } finally {
      for (Map.Entry<String, String> entry : previous.entrySet()) {
        if (entry.getValue() == null) {
          System.clearProperty(entry.getKey());
        } else {
          System.setProperty(entry.getKey(), entry.getValue());
        }
      }
    }
  }
}
