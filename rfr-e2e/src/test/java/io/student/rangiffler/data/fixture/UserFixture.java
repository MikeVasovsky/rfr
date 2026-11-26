package io.student.rangiffler.data.fixture;

import io.student.rangiffler.model.TestUser;

/**
 * Владелец тестового пользователя: {@link #create()} пишет данные, {@link #close()} откатывает.
 * В Store кладётся до {@link #create()}, чтобы cleanup сработал при падении подготовки.
 */
public interface UserFixture extends AutoCloseable {

  TestUser user();

  void create();

  @Override
  void close();
}
