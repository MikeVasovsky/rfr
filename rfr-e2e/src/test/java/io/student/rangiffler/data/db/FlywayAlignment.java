package io.student.rangiffler.data.db;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.ValidateOutput;
import org.flywaydb.core.api.output.ValidateResult;
import org.springframework.dao.DataAccessException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сверка миграций checkout с локальными БД через Flyway {@code validateWithResult}
 * (версии и контрольные суммы) и подпись применённых версий для Allure.
 */
public final class FlywayAlignment {

  private FlywayAlignment() {
  }

  public static void verifyOrThrow() {
    Path root = projectRoot();
    check(
        "rangiffler-auth",
        Databases.AUTH,
        root.resolve("rfr-auth/src/main/resources/db/migration/rangiffler-auth")
    );
    check(
        "rangiffler-api",
        Databases.API,
        root.resolve("rfr-api/src/main/resources/db/migration/rangiffler-api")
    );
  }

  /**
   * Прикладные версии Flyway через запятую; при недоступной БД — {@code unavailable}.
   */
  public static String appliedLabel(Databases database) {
    try {
      return String.join(",", appliedVersions(database).stream().sorted().toList());
    } catch (RuntimeException ignored) {
      return "unavailable";
    }
  }

  private static void check(String schemaLabel, Databases database, Path migrationDir) {
    if (!Files.isDirectory(migrationDir)) {
      throw new IllegalStateException(
          "Нет каталога миграций " + schemaLabel + ": " + migrationDir.toAbsolutePath()
      );
    }
    Path migrations = migrationDir.toAbsolutePath().normalize();
    Flyway flyway = Flyway.configure()
        .dataSource(database.dataSource())
        .locations("filesystem:" + migrations)
        .schemas(schemaLabel)
        .baselineOnMigrate(true)
        .baselineVersion("0")
        .load();
    ValidateResult result;
    try {
      result = flyway.validateWithResult();
    } catch (RuntimeException e) {
      throw new IllegalStateException(
          "Не удалось сверить схему " + schemaLabel + " (" + database.name() + ") с " + migrations
              + ". Подними MySQL и хотя раз прогони rfr-auth / rfr-api. Причина: " + e.getMessage(),
          e
      );
    }
    if (result.validationSuccessful) {
      return;
    }
    String details = result.invalidMigrations == null
        ? ""
        : result.invalidMigrations.stream()
            .map(FlywayAlignment::describeInvalid)
            .collect(Collectors.joining("; "));
    throw new IllegalStateException("""
        Flyway schema mismatch for %s (%s).
        Checkout: %s
        %s

        Call to action:
        1) Переключил ветку без рестарта MySQL / rfr-auth / rfr-api — код и схема разъехались.
        2) Перезапусти бэкенды (`./gradlew runAll --parallel`), чтобы Flyway накатил миграции.
        3) Если схема чужая — пересоздай БД (`./localenv.sh`) и снова подними auth+api.
        """.formatted(schemaLabel, database.name(), migrations, details));
  }

  private static String describeInvalid(ValidateOutput migration) {
    String message = migration.errorDetails == null ? "" : migration.errorDetails.errorMessage;
    return migration.version + " " + migration.description + " — " + message;
  }

  private static List<String> appliedVersions(Databases database) {
    try {
      return database.jdbcClient()
          .sql(Sql.load("sql/flyway/applied_versions.sql"))
          .query((rs, rowNum) -> rs.getString(1))
          .list();
    } catch (DataAccessException e) {
      throw new IllegalStateException(
          "Не удалось прочитать flyway_schema_history в " + database.name()
              + ". Подними MySQL и хотя раз прогони rfr-auth / rfr-api. "
              + "Причина: " + e.getMostSpecificCause().getMessage(),
          e
      );
    }
  }

  private static Path projectRoot() {
    Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    for (Path dir = current; dir != null; dir = dir.getParent()) {
      if (Files.isRegularFile(dir.resolve("settings.gradle"))
          && Files.isDirectory(dir.resolve("rfr-auth"))
          && Files.isDirectory(dir.resolve("rfr-api"))) {
        return dir;
      }
    }
    throw new IllegalStateException(
        "Не найден корень репозитория rfr от user.dir=" + current
            + ". Запускай e2e из checkout, где рядом лежат rfr-auth и rfr-api."
    );
  }

}
