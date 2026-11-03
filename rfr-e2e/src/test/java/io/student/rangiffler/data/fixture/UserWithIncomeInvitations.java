package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.model.TestUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Декоратор: к пользователю добавляет N входящих invitations (PENDING) в api.
 */
public final class UserWithIncomeInvitations implements UserFixture {

  private final TestUser user;
  private final TearDown tearDown;

  private UserWithIncomeInvitations(TestUser user, TearDown tearDown) {
    this.user = user;
    this.tearDown = tearDown;
  }

  public static UserFixture wrap(UserFixture delegate, int incomeInvitations) {
    TestUser base = delegate.user();
    ApiUserSupport.ensureProfile(base);
    List<TestUser> inviters = new ArrayList<>();
    for (int i = 0; i < incomeInvitations; i++) {
      TestUser inviter = ApiUserSupport.createRelatedUser();
      Friendship.create(inviter, base, Friendship.PENDING);
      inviters.add(inviter);
    }
    TearDown tearDown = new TearDown()
        .with(() -> Friendships.of(base).deleteInvitations())
        .withEach(inviters, inviter -> Profile.of(inviter).delete())
        .with(delegate::close);
    return new UserWithIncomeInvitations(base.withIncomeInvitations(inviters), tearDown);
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
