package io.student.rangiffler.jupiter.extension.suite;

import io.student.rangiffler.data.db.FlywayAlignment;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Эксперимент HW04-2: сверка Flyway в {@link #launcherSessionOpened}.
 * Регистрация — ServiceLoader {@code META-INF/services/...LauncherSessionListener}.
 */
public final class StandSchemaLauncherSessionListener implements LauncherSessionListener {

  public static final AtomicInteger OPENED = new AtomicInteger();

  @Override
  public void launcherSessionOpened(LauncherSession session) {
    int n = OPENED.incrementAndGet();
    System.err.println("StandSchemaLauncherSessionListener.opened #" + n
        + " thread=" + Thread.currentThread().getName()
        + " pid=" + ProcessHandle.current().pid());
    if ("false".equalsIgnoreCase(System.getProperty("rfr.schemaCheck", "true"))) {
      return;
    }
    FlywayAlignment.verifyOrThrow();
  }
}
