package io.student.rangiffler.data.fixture.user;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.fixture.TearDown;
import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.model.TestUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Декоратор: к пользователю добавляет N исходящих invitations (PENDING) в api.
 */
public final class UserWithOutcomeInvitationsFixture implements UserFixture {

  private final TestUser user;
  private final TearDown tearDown;

  private UserWithOutcomeInvitationsFixture(TestUser user, TearDown tearDown) {
    this.user = user;
    this.tearDown = tearDown;
  }

  public static UserFixture wrap(UserFixture delegate, int outcomeInvitations) {
    TestUser base = delegate.user();
    ProfileFixture.ensureFor(base);
    List<ProfileFixture> invitees = new ArrayList<>();
    TearDown tearDown = new TearDown()
            .with(() -> Friendships.of(base).deleteInvitations());
    try {
      for (int i = 0; i < outcomeInvitations; i++) {
        ProfileFixture invitee = ProfileFixture.createRelated();
        tearDown.with(invitee::close);
        invitees.add(invitee);
        Friendship.create(base, invitee.user(), Friendship.PENDING);
      }
      tearDown.with(delegate::close);
      List<TestUser> inviteeUsers = invitees.stream().map(ProfileFixture::user).toList();
      return new UserWithOutcomeInvitationsFixture(base.withOutcomeInvitations(inviteeUsers), tearDown);
    }catch (Exception e){
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
