package io.student.rangiffler.data.fixture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Составной cleanup для фикстур: шаги через {@link #with} / {@link #withEach}, один раз в {@link #close}.
 */
public final class TearDown implements AutoCloseable {

  private final List<Runnable> actions = new ArrayList<>();
  private boolean closed;

  public TearDown with(Runnable action) {
    actions.add(action);
    return this;
  }

  public <T> TearDown withEach(Iterable<T> items, Consumer<T> action) {
    for (T item : items) {
      actions.add(() -> action.accept(item));
    }
    return this;
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }
    for (Runnable action : actions) {
      action.run();
    }
    closed = true;
  }
}
