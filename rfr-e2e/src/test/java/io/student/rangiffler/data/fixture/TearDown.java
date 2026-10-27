package io.student.rangiffler.data.fixture;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Переиспользуемый cleanup: шаги регистрируются через {@link #with}/{@link #withEach},
 * при {@link #close()} выполняются в порядке регистрации (повторный close — no-op).
 */
public final class TearDown implements AutoCloseable {

  private final List<Runnable> steps = new ArrayList<>();
  private boolean closed;

  public TearDown with(Runnable step) {
    steps.add(Objects.requireNonNull(step, "step"));
    return this;
  }

  public <T> TearDown withEach(Iterable<? extends T> items, Consumer<? super T> action) {
    Objects.requireNonNull(items, "items");
    Objects.requireNonNull(action, "action");
    steps.add(() -> {
      for (T item : items) {
        action.accept(item);
      }
    });
    return this;
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }
    closed = true;
    RuntimeException first = null;
    for (Runnable step : steps) {
      try {
        step.run();
      } catch (RuntimeException e) {
        if (first == null) {
          first = e;
        } else {
          first.addSuppressed(e);
        }
      }
    }
    if (first != null) {
      throw first;
    }
  }
}
