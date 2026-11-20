package io.student.rangiffler.data.stand;

import io.student.rangiffler.config.TestConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Ожидание стенда: фабрика probes из {@link TestConfig} + опрос до успеха или budget.
 * Перед каждой попыткой таймаут запроса — минимум из 2 с и остатка {@code rfr.standTimeout}.
 * Критерии те же, что у {@code scripts/wait-for-stand.sh}.
 */
public final class StandReadiness {

  private static final long POLL_SLEEP_MS = 500L;
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(2);

  private StandReadiness() {
  }

  public static void awaitFrom(TestConfig config) {
    Objects.requireNonNull(config, "config");
    await(probes(config), Duration.ofSeconds(config.standTimeoutSeconds()));
  }

  private static void await(List<Probe> probes, Duration budget) {
    if (probes.isEmpty()) {
      throw new IllegalArgumentException("probes must not be empty");
    }
    long deadlineNs = System.nanoTime() + budget.toNanos();
    while (true) {
      List<Probe> down = new ArrayList<>();
      for (Probe probe : probes) {
        Duration remaining = remaining(deadlineNs);
        if (!remaining.isPositive()) {
          down.add(probe);
          continue;
        }
        Duration timeout = remaining.compareTo(REQUEST_TIMEOUT) < 0 ? remaining : REQUEST_TIMEOUT;
        if (!probe.check.ready(timeout)) {
          down.add(probe);
        }
      }
      if (down.isEmpty()) {
        return;
      }
      Duration remaining = remaining(deadlineNs);
      if (!remaining.isPositive()) {
        throw notReady(down, budget);
      }
      try {
        long sleepMs = Math.min(POLL_SLEEP_MS, Math.max(1L, remaining.toMillis()));
        TimeUnit.MILLISECONDS.sleep(sleepMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Stand readiness wait interrupted", e);
      }
    }
  }

  private static Duration remaining(long deadlineNs) {
    return Duration.ofNanos(deadlineNs - System.nanoTime());
  }

  private static IllegalStateException notReady(List<Probe> down, Duration budget) {
    String names = down.stream().map(Probe::name).collect(Collectors.joining(", "));
    String commands = down.stream()
        .map(probe -> probe.name() + ": " + probe.startCommand())
        .collect(Collectors.joining("; "));
    return new IllegalStateException(
        "Stand not ready within " + budget.toSeconds()
            + "s. Still down: " + names
            + ". Start: " + commands
    );
  }

  private static List<Probe> probes(TestConfig config) {
    String raw = System.getProperty("rfr.standChecks", "mysql,auth,api,front");
    List<Probe> probes = new ArrayList<>();
    for (String token : raw.split(",")) {
      String id = token.trim().toLowerCase(Locale.ROOT);
      if (!id.isEmpty()) {
        probes.add(probe(id, config));
      }
    }
    if (probes.isEmpty()) {
      throw new IllegalStateException("rfr.standChecks is empty");
    }
    return List.copyOf(probes);
  }

  private static Probe probe(String id, TestConfig config) {
    return switch (id) {
      case "mysql" -> new Probe(
          "MySQL",
          "docker compose up -d --wait",
          timeout -> mysqlReady(config.authJdbcUrl(), config.jdbcUser(), config.jdbcPassword(), timeout)
      );
      case "auth" -> new Probe(
          "rfr-auth",
          "./gradlew runAll --parallel",
          timeout -> httpReady(config.authUrl() + "/", Set.of(200, 302), timeout)
      );
      case "api" -> new Probe(
          "rfr-api",
          "./gradlew runAll --parallel",
          timeout -> httpReady(config.apiUrl() + "/graphiql", Set.of(401), timeout)
      );
      case "front" -> new Probe(
          "rfr-gql-client",
          "npm run dev",
          timeout -> httpReady(config.frontUrl() + "/", Set.of(200), timeout)
      );
      default -> throw new IllegalArgumentException("Unknown stand check id: " + id);
    };
  }

  private static boolean httpReady(String url, Set<Integer> okStatuses, Duration timeout) {
    if (timeout == null || !timeout.isPositive()) {
      return false;
    }
    HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(timeout)
        .build();
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create(url))
          .timeout(timeout)
          .GET()
          .build();
      HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
      return okStatuses.contains(response.statusCode());
    } catch (Exception ignored) {
      return false;
    } finally {
      client.close();
    }
  }

  private static boolean mysqlReady(String jdbcUrl, String user, String password, Duration timeout) {
    if (timeout == null || !timeout.isPositive()) {
      return false;
    }
    int millis = (int) Math.min(Integer.MAX_VALUE, Math.max(1L, timeout.toMillis()));
    Properties properties = new Properties();
    properties.setProperty("user", user);
    properties.setProperty("password", password);
    properties.setProperty("connectTimeout", Integer.toString(millis));
    properties.setProperty("socketTimeout", Integer.toString(millis));
    try (Connection ignored = DriverManager.getConnection(jdbcUrl, properties)) {
      return true;
    } catch (SQLException ignored) {
      return false;
    }
  }

  @FunctionalInterface
  private interface ReadyCheck {
    boolean ready(Duration timeout);
  }

  private record Probe(String name, String startCommand, ReadyCheck check) {
  }
}
