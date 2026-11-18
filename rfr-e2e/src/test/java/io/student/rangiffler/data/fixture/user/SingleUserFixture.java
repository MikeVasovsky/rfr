package io.student.rangiffler.data.fixture.user;

import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.data.fixture.TearDown;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;

/**
 * Тестовый пользователь в auth БД: {@link #create()} создаёт запись, {@link #close()} удаляет.
 */
public final class SingleUserFixture implements UserFixture {

  private final AuthAccount account;
  private final TearDown tearDown;

  private SingleUserFixture(AuthAccount account) {
    this.account = account;
    this.tearDown = new TearDown()
        .with(() -> {
          Friendships.of(account.user()).delete();
          Profile.of(account.user()).delete();
        })
        .with(account::delete);
  }

  public static UserFixture create() {
    return new SingleUserFixture(AuthAccount.create(RandomDataUtils.createUser()));
  }

  public TestUser user() {
    return account.user();
  }

  @Override
  public void close() {
    tearDown.close();
  }
}
