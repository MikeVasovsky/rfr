package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.dao.AuthUserDao;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;

/**
 * Тестовый пользователь в auth БД: {@link #create()} создаёт запись,
 * {@link #close()} удаляет её.
 */
public final class UserFixture implements AutoCloseable {

  private final TestUser user;
  /** Защита от повторного {@link #close()} (двойное удаление из БД не нужно). */
  private boolean closed;

  private UserFixture(TestUser user) {
    this.user = user;
  }

  /**
   * Генерирует случайного {@link TestUser}, пишет его в auth БД через {@link AuthUserDao}
   * и возвращает fixture, который потом можно закрыть.
   */
  public static UserFixture create() {
    TestUser user = RandomDataUtils.createUser();
    AuthUserDao.create(user);
    return new UserFixture(user);
  }

  /** Данные созданного пользователя (логин/пароль для UI и т.п.). */
  public TestUser user() {
    return user;
  }

  /**
   * Удаляет пользователя из auth БД. Повторный вызов безопасен (no-op).
   * Вызывается из {@code UserExtension.afterEach} или try-with-resources.
   */
  @Override
  public void close() {
    if (closed) {
      return;
    }
    AuthUserDao.delete(user);
    closed = true;
  }
}
