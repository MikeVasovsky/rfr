package io.student.rangiffler.test.extension.stand;

import com.sun.net.httpserver.HttpServer;
import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.probe.stand.FrontNeverReadyCase;
import io.student.rangiffler.jupiter.probe.stand.FrontReadyAfterDelayCase;
import io.student.rangiffler.jupiter.probe.stand.NoUiTagCase;
import io.student.rangiffler.jupiter.probe.stand.StandCheckDisabledCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * EngineTestKit-мишени для {@code StandReadinessExtension} на подставном HTTP-сервере.
 */
@Tag("fake")
@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
class StandReadinessExtensionTest {

  @Test
  @DisplayName("Сервис начал отвечать через 2 с — тесты выполняются")
  void runsWhenServiceBecomesReadyAfterDelay() throws Exception {
    AtomicInteger hits = new AtomicInteger();
    AtomicBoolean ready = new AtomicBoolean(false);
    try (AutoCloseable server = startFrontStub(hits, ready)) {
      Thread enabler = new Thread(() -> {
        try {
          Thread.sleep(2000L);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        ready.set(true);
      }, "stand-ready-delay");
      enabler.setDaemon(true);
      enabler.start();
      EngineExecutionResults results = execute(
          FrontReadyAfterDelayCase.class,
          Map.of(
              "rfr.schemaCheck", "false",
              "rfr.standCheck", "true",
              "rfr.standTimeout", "10",
              "rfr.standChecks", "front",
              "rfr.frontUrl", baseUrl(server)
          )
      );
      Allure.step("Мишень успешна", () ->
          results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1))
      );
      Allure.step("Были HTTP-запросы к заглушке", () ->
          assertTrue(hits.get() > 0, "expected stand probes to hit mock front")
      );
    }
  }

  @Test
  @DisplayName("Сервис не ответил за таймаут — тесты не выполняются, в ошибке имя сервиса")
  void doesNotRunWhenServiceTimesOut() throws Exception {
    AtomicInteger hits = new AtomicInteger();
    AtomicBoolean ready = new AtomicBoolean(false);
    try (AutoCloseable server = startFrontStub(hits, ready)) {
      EngineExecutionResults results = execute(
          FrontNeverReadyCase.class,
          Map.of(
              "rfr.schemaCheck", "false",
              "rfr.standCheck", "true",
              "rfr.standTimeout", "2",
              "rfr.standChecks", "front",
              "rfr.frontUrl", baseUrl(server)
          )
      );
      Allure.step("Тестовый метод не стартовал", () ->
          results.testEvents().assertStatistics(stats -> stats.started(0).succeeded(0))
      );
      String failure = failureText(results);
      Allure.step("В ошибке есть rfr-gql-client", () ->
          assertTrue(
              failure.contains("rfr-gql-client"),
              "expected rfr-gql-client in failure but was: " + failure
          )
      );
      Allure.step("Были попытки опроса", () ->
          assertTrue(hits.get() > 0, "expected timeout path to probe mock front")
      );
    }
  }

  @Test
  @DisplayName("Класс без тега UI — к сервису нет ни одного запроса")
  void doesNotProbeStandWithoutUiTag() throws Exception {
    AtomicInteger hits = new AtomicInteger();
    AtomicBoolean ready = new AtomicBoolean(true);
    try (AutoCloseable server = startFrontStub(hits, ready)) {
      EngineExecutionResults results = execute(
          NoUiTagCase.class,
          Map.of(
              "rfr.schemaCheck", "false",
              "rfr.standCheck", "true",
              "rfr.standTimeout", "5",
              "rfr.standChecks", "front",
              "rfr.frontUrl", baseUrl(server)
          )
      );
      Allure.step("Мишень успешна", () ->
          results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1))
      );
      Allure.step("Запросов к заглушке не было", () ->
          assertEquals(0, hits.get(), "expected no stand HTTP calls without ui tag")
      );
    }
  }

  @Test
  @DisplayName("Проверка выключена — к сервису нет ни одного запроса")
  void doesNotProbeStandWhenCheckDisabled() throws Exception {
    AtomicInteger hits = new AtomicInteger();
    AtomicBoolean ready = new AtomicBoolean(false);
    try (AutoCloseable server = startFrontStub(hits, ready)) {
      EngineExecutionResults results = execute(
          StandCheckDisabledCase.class,
          Map.of(
              "rfr.schemaCheck", "false",
              "rfr.standCheck", "false",
              "rfr.standTimeout", "5",
              "rfr.standChecks", "front",
              "rfr.frontUrl", baseUrl(server)
          )
      );
      Allure.step("Мишень успешна", () ->
          results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1))
      );
      Allure.step("Запросов к заглушке не было", () ->
          assertEquals(0, hits.get(), "expected no stand HTTP calls when standCheck=false")
      );
    }
  }

  private static String failureText(EngineExecutionResults results) {
    StringBuilder text = new StringBuilder();
    for (var event : results.allEvents().failed().list()) {
      event.getPayload(org.junit.platform.engine.TestExecutionResult.class).ifPresent(result ->
          result.getThrowable().ifPresent(error -> {
            text.append(error).append('\n');
            Throwable cause = error.getCause();
            while (cause != null) {
              text.append(cause).append('\n');
              cause = cause.getCause();
            }
          })
      );
    }
    return text.toString();
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

  private static AutoCloseable startFrontStub(AtomicInteger hits, AtomicBoolean ready) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      hits.incrementAndGet();
      int status = ready.get() ? 200 : 503;
      byte[] body = status == 200 ? new byte[0] : new byte[0];
      exchange.sendResponseHeaders(status, body.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(body);
      }
    });
    server.setExecutor(Executors.newCachedThreadPool());
    server.start();
    String base = "http://127.0.0.1:" + server.getAddress().getPort();
    return new FrontStub(server, base);
  }

  private static String baseUrl(AutoCloseable server) {
    return ((FrontStub) server).baseUrl;
  }

  private record FrontStub(HttpServer server, String baseUrl) implements AutoCloseable {
    @Override
    public void close() {
      server.stop(0);
    }
  }
}
