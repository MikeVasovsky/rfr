package io.student.rangiffler.data.fixture.session;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.jupiter.annotation.session.Session;
import io.student.rangiffler.model.UserSession;

/**
 * Владелец одного UI-сеанса. В Store кладётся до {@link #start}, чтобы {@link #close()} был безопасен
 * при частичной подготовке.
 */
public interface SessionFixture extends AutoCloseable {

  void start(BrowserRuntime runtime, Session annotation, TestConfig config);

  UserSession session();

  @Override
  void close();
}
