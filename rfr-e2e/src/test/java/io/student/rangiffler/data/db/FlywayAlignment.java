package io.student.rangiffler.data.db;

import org.springframework.dao.DataAccessException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Сверка Flyway-версий SQL бэкендов в checkout с {@code flyway_schema_history} локальных БД.
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

  private static void check(String schemaLabel, Databases database, Path migrationDir) {
    List<String> expected = expectedVersions(schemaLabel, migrationDir);
    List<String> applied = appliedVersions(database).stream().sorted().toList();
    if (expected.equals(applied)) {
      return;
    }
    throw new IllegalStateException("""
        Flyway schema mismatch for %s (%s).
        Expected from checkout %s: %s
        Applied in local DB: %s

        Call to action:
        1) Переключил ветку без рестарта MySQL / rfr-auth / rfr-api — код и схема разъехались.
        2) Перезапусти бэкенды (`./gradlew runAll --parallel`), чтобы Flyway накатил миграции.
        3) Если схема чужая — пересоздай БД (`./localenv.sh`) и снова подними auth+api.
        """.formatted(
        schemaLabel,
        database.name(),
        migrationDir.toAbsolutePath().normalize(),
        expected,
        applied
    ));
  }

  private static List<String> expectedVersions(String schemaLabel, Path migrationDir) {
    if (!Files.isDirectory(migrationDir)) {
      throw new IllegalStateException(
          "Нет каталога миграций " + schemaLabel + ": " + migrationDir.toAbsolutePath()
      );
    }
    try (Stream<Path> files = Files.list(migrationDir)) {
      return files
          .filter(Files::isRegularFile)
          .map(path -> path.getFileName().toString())
          .map(fileName -> parseVersion(fileName))
          .filter(Objects::nonNull)
          .sorted()
          .toList();
    } catch (IOException e) {
      throw new IllegalStateException("Не удалось прочитать миграции " + migrationDir, e);
    }
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

  private static String parseVersion(String fileName) {
    if (!fileName.regionMatches(true, 0, "V", 0, 1) || !fileName.contains("__")) {
      return null;
    }
    int end = fileName.indexOf("__");
    return fileName.substring(1, end).replace('_', '.');
  }
}
