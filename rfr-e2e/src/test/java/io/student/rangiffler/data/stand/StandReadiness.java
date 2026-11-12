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
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

/**
 * Ожидание стенда: фабрика probes из {@link TestConfig} + опрос до успеха или budget.
 * Критерии те же, что у {@code scripts/wait-for-stand.sh}.
 */
public final class StandReadiness {

  private static final long POLL_SLEEP_MS = 500L;
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(2);
  private static final HttpClient HTTP = HttpClient.newBuilder()
      .connectTimeout(REQUEST_TIMEOUT)
      .build();

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
        if (!probe.check.getAsBoolean()) {
          down.add(probe);
        }
      }
      if (down.isEmpty()) {
        return;
      }
      if (System.nanoTime() >= deadlineNs) {
        String names = down.stream().map(Probe::name).collect(Collectors.joining(", "));
        String commands = down.stream()
            .map(probe -> probe.name() + ": " + probe.startCommand())
            .collect(Collectors.joining("; "));
        throw new IllegalStateException(
            "Stand not ready within " + budget.toSeconds()
                + "s. Still down: " + names
                + ". Start: " + commands
        );
      }
      try {
        TimeUnit.MILLISECONDS.sleep(POLL_SLEEP_MS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Stand readiness wait interrupted", e);
      }
    }
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
          () -> mysqlReady(config.authJdbcUrl(), config.jdbcUser(), config.jdbcPassword())
      );
      case "auth" -> new Probe(
          "rfr-auth",
          "./gradlew runAll --parallel",
          () -> httpReady(config.authUrl() + "/", Set.of(200, 302))
      );
      case "api" -> new Probe(
          "rfr-api",
          "./gradlew runAll --parallel",
          () -> httpReady(config.apiUrl() + "/graphiql", Set.of(401))
      );
      case "front" -> new Probe(
          "rfr-gql-client",
          "npm run dev",
          () -> httpReady(config.frontUrl() + "/", Set.of(200))
      );
      default -> throw new IllegalArgumentException("Unknown stand check id: " + id);
    };
  }

  private static boolean httpReady(String url, Set<Integer> okStatuses) {
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create(url))
          .timeout(REQUEST_TIMEOUT)
          .GET()
          .build();
      HttpResponse<Void> response = HTTP.send(request, HttpResponse.BodyHandlers.discarding());
      return okStatuses.contains(response.statusCode());
    } catch (Exception ignored) {
      return false;
    }
  }

  private static boolean mysqlReady(String jdbcUrl, String user, String password) {
    Properties properties = new Properties();
    properties.setProperty("user", user);
    properties.setProperty("password", password);
    properties.setProperty("connectTimeout", "2000");
    properties.setProperty("socketTimeout", "2000");
    String url = jdbcUrl.contains("connectTimeout=")
        ? jdbcUrl
        : jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "connectTimeout=2000&socketTimeout=2000";
    try (Connection ignored = DriverManager.getConnection(url, properties)) {
      return true;
    } catch (SQLException ignored) {
      return false;
    }
  }

  private record Probe(String name, String startCommand, BooleanSupplier check) {
  }
}
