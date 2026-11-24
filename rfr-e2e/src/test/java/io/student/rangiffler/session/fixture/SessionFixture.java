package io.student.rangiffler.session.fixture;

/**
 * Владелец одного {@code BrowserContext} dual-сессии. В Store до {@link #create()},
 * чтобы {@link #close()} был безопасен при частичной подготовке.
 */
public interface SessionFixture<T> extends AutoCloseable {

  T session();

  void create();

  @Override
  void close();
}
