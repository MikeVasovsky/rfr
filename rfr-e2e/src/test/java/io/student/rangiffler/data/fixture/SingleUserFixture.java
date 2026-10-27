package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.jupiter.UserRequest;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;

/**
 * Один пользователь в auth БД без friends/invitations:
 * {@link #create(UserRequest)} создаёт запись, {@link #close()} удаляет её.
 */
public final class SingleUserFixture implements UserFixture {

  private final UserRequest request;
  private final TestUser user;
  private final TearDown tearDown;

  private SingleUserFixture(UserRequest request, TestUser user) {
    this.request = request;
    this.user = user;
    AuthAccount account = AuthAccount.of(user);
    this.tearDown = new TearDown()
        .with(() -> Friendships.of(user.username()).deleteAll())
        .with(account::delete);
  }

  /**
   * По {@link UserRequest} генерирует {@link TestUser}, пишет в auth БД
   * и возвращает fixture для cleanup. Friends/invitations — в декораторах.
   */
  public static UserFixture create(UserRequest request) {
    TestUser user = RandomDataUtils.createUser();
    AuthAccount.create(user);
    return new SingleUserFixture(request, user);
  }

  @Override
  public UserRequest request() {
    return request;
  }

  @Override
  public TestUser user() {
    return user;
  }

  @Override
  public void close() {
    tearDown.close();
  }
}
