package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;

/**
 * Тестовый пользователь в auth БД: {@link #create()} создаёт запись, {@link #close()} удаляет.
 */
public final class SingleUserFixture implements UserFixture {

  private final AuthAccount account;
  private boolean closed;

  private SingleUserFixture(AuthAccount account) {
    this.account = account;
  }

  public static UserFixture create() {
    return new SingleUserFixture(AuthAccount.create(RandomDataUtils.createUser()));
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
