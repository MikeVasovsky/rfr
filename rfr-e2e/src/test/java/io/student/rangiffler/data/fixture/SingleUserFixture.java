package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.dao.AuthUserDao;
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
  private boolean closed;

  private SingleUserFixture(UserRequest request, TestUser user) {
    this.request = request;
    this.user = user;
  }

  /**
   * По {@link UserRequest} генерирует {@link TestUser}, пишет в auth БД
   * и возвращает fixture для cleanup. Friends/invitations — в декораторах.
   */
  public static UserFixture create(UserRequest request) {
    TestUser user = RandomDataUtils.createUser();
    AuthUserDao.create(user);
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
    if (closed) {
      return;
    }
    ApiUsers.deleteFriendships(user.username());
    AuthUserDao.delete(user);
    closed = true;
  }
}
