package io.student.rangiffler.jupiter.extension.suite;

import io.student.rangiffler.data.db.FlywayAlignment;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Suite-level guard: до тестов сверяет Flyway-версии локальных БД с миграциями бэкендов в checkout.
 * Регистрация в обычном режиме — ServiceLoader ({@code META-INF/services/...Extension}).
 */
public final class StandSchemaExtension implements BeforeAllCallback {

  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(StandSchemaExtension.class);
  private static final String CHECK_KEY = "flywayAlignment";

  @Override
  public void beforeAll(ExtensionContext context) {
    context.getRoot().getStore(NAMESPACE).computeIfAbsent(
        CHECK_KEY,
        key -> {
          FlywayAlignment.verifyOrThrow();
          return Boolean.TRUE;
        },
        Boolean.class
    );
  }
}
