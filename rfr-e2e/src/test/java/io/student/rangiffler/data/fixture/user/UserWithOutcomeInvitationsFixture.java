package io.student.rangiffler.data.fixture.user;

import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.data.fixture.TearDown;
import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.model.TestUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Декоратор: к пользователю добавляет N исходящих invitations (PENDING) в api.
 */
public final class  UserWithOutcomeInvitationsFixture implements UserFixture {

  private final TestUser user;
  private final TearDown tearDown;

  private UserWithOutcomeInvitationsFixture(TestUser user, TearDown tearDown) {
    this.user = user;
    this.tearDown = tearDown;
  }

  public static UserFixture wrap(UserFixture delegate, int outcomeInvitations) {
    TestUser base = delegate.user();
    ApiUserSupport.ensureProfile(base);
    List<TestUser> invitees = new ArrayList<>();
    for (int i = 0; i < outcomeInvitations; i++) {
      TestUser invitee = ApiUserSupport.createRelatedUser();
      Friendship.create(base, invitee, Friendship.PENDING);
      invitees.add(invitee);
    }
    TearDown tearDown = new TearDown()
        .with(() -> Friendships.of(base).deleteInvitations())
        .withEach(invitees, invitee -> Profile.of(invitee).delete())
        .with(delegate::close);
    return new UserWithOutcomeInvitationsFixture(base.withOutcomeInvitations(invitees), tearDown);
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
