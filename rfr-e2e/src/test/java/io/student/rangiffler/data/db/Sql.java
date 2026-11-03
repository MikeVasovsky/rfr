package io.student.rangiffler.data.db;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Загрузка SQL из classpath ({@code src/test/resources/...}).
 */
public final class Sql {

  private Sql() {
  }

  public static String load(String classpathLocation) {
    try {
      return new ClassPathResource(classpathLocation).getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Не удалось загрузить SQL: " + classpathLocation, e);
    }
  }
}
