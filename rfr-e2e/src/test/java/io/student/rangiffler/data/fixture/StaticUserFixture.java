package io.student.rangiffler.data.fixture;

import io.student.rangiffler.model.TestUser;

/**
 * Фикстура из пула: {@link #create()} берёт пользователя из {@link UsersQueue},
 * {@link #close()} возвращает только взятого и ничего не удаляет из БД.
 */
public final class StaticUserFixture implements UserFixture {

  private TestUser user;

  public void create() {
    user = UsersQueue.acquire();
  }

  @Override
  public TestUser user() {
    return user;
  }

  @Override
  public void close() {
    if (user != null) {
      UsersQueue.release(user);
      user = null;
    }
  }
}
