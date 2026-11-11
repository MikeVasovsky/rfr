package io.student.rangiffler.data.stand;

import io.student.rangiffler.config.TestConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Фабрика проверок стенда из {@link TestConfig} и {@code rfr.standChecks}.
 */
public final class StandChecks {

  private StandChecks() {
  }

  public static List<StandCheck> from(TestConfig config) {
    String raw = System.getProperty("rfr.standChecks", "mysql,auth,api,front");
    List<StandCheck> checks = new ArrayList<>();
    for (String token : raw.split(",")) {
      String id = token.trim().toLowerCase(Locale.ROOT);
      if (id.isEmpty()) {
        continue;
      }
      checks.add(check(id, config));
    }
    if (checks.isEmpty()) {
      throw new IllegalStateException("rfr.standChecks is empty");
    }
    return List.copyOf(checks);
  }

  private static StandCheck check(String id, TestConfig config) {
    return switch (id) {
      case "mysql" -> new MysqlStandCheck(
          config.authJdbcUrl(),
          config.jdbcUser(),
          config.jdbcPassword()
      );
      case "auth" -> new HttpStandCheck(
          "rfr-auth",
          config.authUrl() + "/",
          Set.of(200, 302),
          "./gradlew runAll --parallel"
      );
      case "api" -> new HttpStandCheck(
          "rfr-api",
          config.apiUrl() + "/graphiql",
          Set.of(401),
          "./gradlew runAll --parallel"
      );
      case "front" -> new HttpStandCheck(
          "rfr-gql-client",
          config.frontUrl() + "/",
          Set.of(200),
          "npm run dev"
      );
      default -> throw new IllegalArgumentException("Unknown stand check id: " + id);
    };
  }
}
