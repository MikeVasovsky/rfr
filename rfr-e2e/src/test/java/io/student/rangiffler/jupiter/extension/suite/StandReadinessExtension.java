package io.student.rangiffler.jupiter.extension.suite;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.stand.StandChecks;
import io.student.rangiffler.data.stand.StandReadiness;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.Duration;

/**
 * Ждёт MySQL/auth/api/front перед первым UI-классом ({@code @Tag("ui")}).
 * Регистрация — только ServiceLoader; без тега {@code ui} и при {@code rfr.standCheck=false} — no-op.
 */
public final class StandReadinessExtension implements SuiteExtension {

  public static final String UI_TAG = "ui";

  @Override
  public boolean shouldRunBeforeSuite(ExtensionContext context) {
    if (!TestConfig.getInstance().standCheck()) {
      return false;
    }
    return hasUiTag(context);
  }

  @Override
  public void beforeSuite(ExtensionContext context) {
    TestConfig config = TestConfig.getInstance();
    StandReadiness.await(
        StandChecks.from(config),
        Duration.ofSeconds(config.standTimeoutSeconds())
    );
  }

  static boolean hasUiTag(ExtensionContext context) {
    if (context.getTags().contains(UI_TAG)) {
      return true;
    }
    return context.getTestClass()
        .map(StandReadinessExtension::typeHasUiTag)
        .orElse(false);
  }

  private static boolean typeHasUiTag(Class<?> type) {
    for (Class<?> current = type; current != null; current = current.getSuperclass()) {
      Tag tag = current.getAnnotation(Tag.class);
      if (tag != null && UI_TAG.equals(tag.value())) {
        return true;
      }
      Tag[] tags = current.getAnnotationsByType(Tag.class);
      for (Tag repeated : tags) {
        if (UI_TAG.equals(repeated.value())) {
          return true;
        }
      }
    }
    return false;
  }
}
