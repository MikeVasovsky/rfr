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
    closed = true;
    Exception first = null;
    for (Runnable action : actions) {
      try {
        action.run();
      } catch (Exception e) {
        if (first == null) {
          first = e;
        } else {
          first.addSuppressed(e);
        }
      }
    }
    if (first != null) {
      throw first instanceof RuntimeException re ? re : new RuntimeException(first);
    }
  }
}
