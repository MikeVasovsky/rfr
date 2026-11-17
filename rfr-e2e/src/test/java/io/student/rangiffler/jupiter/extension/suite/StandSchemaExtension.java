package io.student.rangiffler.jupiter.extension.suite;

import io.student.rangiffler.data.db.FlywayAlignment;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Suite-level guard: до UI-тестов сверяет Flyway-версии локальных БД с миграциями бэкендов в checkout.
 * Регистрация — ServiceLoader ({@code META-INF/services/...Extension}).
 * Срабатывает только при {@code @Tag("ui")} и {@code rfr.schemaCheck} не {@code false}
 * (у задачи {@code test} свойство по умолчанию выключено, у {@code e2e} — включено).
 */
public final class StandSchemaExtension implements SuiteExtension {

  @Override
  public boolean shouldRunBeforeSuite(ExtensionContext context) {
    if ("false".equalsIgnoreCase(System.getProperty("rfr.schemaCheck", "true"))) {
      return false;
    }
    return StandReadinessExtension.hasUiTag(context);
  }

  @Override
  public void beforeSuite(ExtensionContext context) {
    FlywayAlignment.verifyOrThrow();
  }
}
