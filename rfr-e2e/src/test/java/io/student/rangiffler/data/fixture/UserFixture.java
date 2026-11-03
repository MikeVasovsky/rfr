package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;

/**
 * Тестовый пользователь в auth БД: {@link #create()} создаёт запись, {@link #close()} удаляет.
 */
public final class UserFixture implements AutoCloseable {

  private final AuthAccount account;
  private boolean closed;

  private UserFixture(AuthAccount account) {
    this.account = account;
  }

  public static UserFixture create() {
    return new UserFixture(AuthAccount.create(RandomDataUtils.createUser()));
  }

  public TestUser user() {
    return account.user();
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }
    account.delete();
    closed = true;
  }
}
