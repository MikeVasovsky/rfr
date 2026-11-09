package io.student.rangiffler.data.fixture.user;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.fixture.TearDown;
import io.student.rangiffler.data.fixture.UserFixture;
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
    ProfileFixture.ensureFor(base);
    List<ProfileFixture> inviters = new ArrayList<>();
    TearDown tearDown = new TearDown()
        .with(() -> Friendships.of(base).deleteInvitations());
    try {
      for (int i = 0; i < incomeInvitations; i++) {
        ProfileFixture inviter = ProfileFixture.createRelated();
        tearDown.with(inviter::close);
        inviters.add(inviter);
        Friendship.create(inviter.user(), base, Friendship.PENDING);
      }
      tearDown.with(delegate::close);
      List<TestUser> inviterUsers = inviters.stream().map(ProfileFixture::user).toList();
      return new UserWithIncomeInvitations(base.withIncomeInvitations(inviterUsers), tearDown);
    } catch (Exception e) {
      tearDown.with(delegate::close).close();
      throw e;
    }
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
