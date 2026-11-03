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

  private final UserFixture delegate;
  private final TearDown tearDown;

  private UserWithIncomeInvitations(UserFixture delegate, TearDown tearDown) {
    this.delegate = delegate;
    this.tearDown = tearDown;
  }

  public static UserFixture wrap(UserFixture delegate, int incomeInvitations) {
    ApiUserSupport.ensureProfile(delegate.user());
    List<TestUser> inviters = new ArrayList<>();
    for (int i = 0; i < incomeInvitations; i++) {
      TestUser inviter = ApiUserSupport.createRelatedUser();
      Friendship.create(inviter, delegate.user(), Friendship.PENDING);
      inviters.add(inviter);
    }
    TearDown tearDown = new TearDown()
        .with(() -> Friendships.of(delegate.user()).deleteInvitations())
        .withEach(inviters, inviter -> Profile.of(inviter).delete())
        .with(delegate::close);
    return new UserWithIncomeInvitations(delegate, tearDown);
  }

  @Override
  public TestUser user() {
    return delegate.user();
  }

  @Override
  public void close() {
    tearDown.close();
  }
}
