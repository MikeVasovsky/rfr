package io.student.rangiffler.jupiter.extension.suite;

import io.student.rangiffler.data.db.FlywayAlignment;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Suite-level guard: до тестов сверяет Flyway-версии локальных БД с миграциями бэкендов в checkout.
 * Регистрация — ServiceLoader ({@code META-INF/services/...Extension}).
 */
public final class StandSchemaExtension implements SuiteExtension {

  @Override
  public boolean shouldRunBeforeSuite(ExtensionContext context) {
    return !"false".equalsIgnoreCase(System.getProperty("rfr.schemaCheck", "true"));
  }

  @Override
  public void beforeSuite(ExtensionContext context) {
    FlywayAlignment.verifyOrThrow();
  }
}
