package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.dao.ApiUserDao;
import io.student.rangiffler.data.db.Sql;
import io.student.rangiffler.jupiter.UserRequest;
import io.student.rangiffler.model.TestUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.student.rangiffler.data.db.Databases.API;

/**
 * Декоратор: {@code incomeInvitations} PENDING (companion → main).
 */
public final class UserWithIncomeInvitationsFixture implements UserFixture {

  private final UserFixture delegate;
  private final List<TestUser> companions = new ArrayList<>();
  private final boolean mainProfileCreated;
  private boolean closed;

  public UserWithIncomeInvitationsFixture(UserFixture delegate) {
    this.delegate = Objects.requireNonNull(delegate);
    String me = delegate.user().username();
    this.mainProfileCreated = ApiUsers.ensure(me);
    byte[] mainId = ApiUsers.id(me);
    for (int i = 0; i < delegate.request().incomeInvitations(); i++) {
      TestUser inviter = ApiUsers.companion();
      API.run(() -> API.jdbc().sql(Sql.load("sql/api/insert_invitation.sql"))
          .params(ApiUsers.id(inviter.username()), mainId).update());
      companions.add(inviter);
    }
  }

  @Override
  public UserRequest request() {
    return delegate.request();
  }

  @Override
  public TestUser user() {
    return delegate.user();
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }
    ApiUsers.deleteFriendships(delegate.user().username());
    companions.forEach(ApiUsers::deleteCompanion);
    if (mainProfileCreated) {
      ApiUserDao.delete(delegate.user().username());
    }
    delegate.close();
    closed = true;
  }
}
