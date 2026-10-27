package io.student.rangiffler.data.db;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

/**
 * Загрузка и выполнение SQL-скриптов из classpath ({@code src/test/resources/...}).
 */
public final class Sql {

  private Sql() {
  }

  /**
   * Читает SQL-файл как строку (для {@code JdbcClient.sql(...)} с плейсхолдерами).
   * Путь относительно classpath, например {@code sql/auth/insert_user.sql}.
   */
  public static String load(String classpathLocation) {
    try {
      return new ClassPathResource(classpathLocation).getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Не удалось загрузить SQL: " + classpathLocation, e);
    }
  }

  /**
   * Выполняет весь скрипт из файла на соединении выбранной схемы
   * (несколько statements через {@link ScriptUtils}).
   */
  public static void execute(Databases database, String classpathLocation) {
    Connection connection = DataSourceUtils.getConnection(database.dataSource());
    try {
      ScriptUtils.executeSqlScript(
          connection,
          new EncodedResource(new ClassPathResource(classpathLocation), StandardCharsets.UTF_8)
      );
    } catch (RuntimeException e) {
      throw new IllegalStateException("Не удалось выполнить SQL: " + classpathLocation, e);
    } finally {
      DataSourceUtils.releaseConnection(connection, database.dataSource());
    }
  }

  /**
   * Как {@link #execute}, но внутри транзакции схемы:
   * либо весь скрипт, либо откат.
   */
  public static void executeInTransaction(Databases database, String classpathLocation) {
    database.run(() -> execute(database, classpathLocation));
  }
}
