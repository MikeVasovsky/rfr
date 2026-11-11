package io.student.rangiffler.data.stand;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Опрос проверок стенда до успеха или {@code budget}; без бесконечного ожидания.
 */
public final class StandReadiness {

  private static final long POLL_SLEEP_MS = 500L;

  private StandReadiness() {
  }

  public static void await(List<StandCheck> checks, Duration budget) {
    Objects.requireNonNull(checks, "checks");
    Objects.requireNonNull(budget, "budget");
    if (checks.isEmpty()) {
      throw new IllegalArgumentException("checks must not be empty");
    }
    long deadlineNs = System.nanoTime() + budget.toNanos();
    while (true) {
      List<StandCheck> down = new ArrayList<>();
      for (StandCheck check : checks) {
        if (!check.ready()) {
          down.add(check);
        }
      }
      if (down.isEmpty()) {
        return;
      }
      if (System.nanoTime() >= deadlineNs) {
        throw new IllegalStateException(timeoutMessage(down, budget));
      }
      try {
        TimeUnit.MILLISECONDS.sleep(POLL_SLEEP_MS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Stand readiness wait interrupted", e);
      }
    }
  }

  private static String timeoutMessage(List<StandCheck> down, Duration budget) {
    String names = down.stream().map(StandCheck::name).collect(Collectors.joining(", "));
    String commands = down.stream()
        .map(check -> check.name() + ": " + check.startCommand())
        .collect(Collectors.joining("; "));
    return "Stand not ready within "
        + budget.toSeconds()
        + "s. Still down: "
        + names
        + ". Start: "
        + commands;
  }
}
