package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.dao.ApiUserDao;
import io.student.rangiffler.model.TestUser;

/**
 * Тестовый профиль в api БД: {@link #create(TestUser)} создаёт запись,
 * {@link #close()} удаляет её.
 */
public final class ProfileFixture implements AutoCloseable {

  private final String username;
  private boolean closed;

  private ProfileFixture(String username) {
    this.username = username;
  }

  /**
   * Пишет минимальный профиль (country {@code ru}) в api БД
   * для username из {@link TestUser} и возвращает fixture для cleanup.
   */
  public static ProfileFixture create(TestUser user) {
    ApiUserDao.create(user.username());
    return new ProfileFixture(user.username());
  }

  public String username() {
    return username;
  }

  /**
   * Удаляет профиль из api БД. Повторный вызов безопасен (no-op).
   */
  @Override
  public void close() {
    if (closed) {
      return;
    }
    ApiUserDao.delete(username);
    closed = true;
  }
}
