package io.student.rangiffler.data.fixture.user;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.data.fixture.TearDown;
import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;

/**
 * Тестовый пользователь в auth БД: {@link #create()} создаёт запись, {@link #close()} удаляет.
 */
public final class SingleUserFixture implements UserFixture {

  private AuthAccount account;
  private TearDown tearDown;

  @Override
  public void create() {
    if (account != null) {
      return;
    }
    account = AuthAccount.create(RandomDataUtils.createUser());
    tearDown = new TearDown()
        .with(() -> {
          Friendships.of(account.user()).delete();
          Profile.of(account.user()).delete();
        })
        .with(() -> account.delete());
  }

  @Override
  public TestUser user() {
    if (account == null) {
      throw new IllegalStateException("SingleUserFixture.create() has not been called");
    }
    return account.user();
  }

  @Override
  public void close() {
    if (tearDown != null) {
      tearDown.close();
    }
  }
}
