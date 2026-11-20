package io.student.rangiffler.jupiter.extension.suite;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Suite-level extension: {@link #beforeSuite} один раз через root Store;
 * {@link #afterSuite} — при закрытии root Store (значение {@link AutoCloseable} в Store).
 * Реализации регистрируются только через ServiceLoader ({@code META-INF/services/...Extension}).
 */
public interface SuiteExtension extends BeforeAllCallback, AfterAllCallback {

  @Override
  default void beforeAll(ExtensionContext context) {
    ExtensionContext.Store store = context.getRoot().getStore(namespace());
    store.getOrComputeIfAbsent(
        "afterSuiteHook",
        key -> (AutoCloseable) () -> {
          if (shouldRunAfterSuite(context)) {
            afterSuite(context);
          }
        },
        AutoCloseable.class
    );
    if (!shouldRunBeforeSuite(context)) {
      return;
    }
    store.computeIfAbsent(
        "beforeSuite",
        key -> {
          beforeSuite(context);
          return Boolean.TRUE;
        },
        Boolean.class
    );
  }

  @Override
  default void afterAll(ExtensionContext context) {
  }

  default boolean shouldRunBeforeSuite(ExtensionContext context) {
    return true;
  }

  default boolean shouldRunAfterSuite(ExtensionContext context) {
    return true;
  }

  default void beforeSuite(ExtensionContext context) {
  }

  default void afterSuite(ExtensionContext context) {
  }

  private ExtensionContext.Namespace namespace() {
    return ExtensionContext.Namespace.create(SuiteExtension.class, getClass());
  }
}
