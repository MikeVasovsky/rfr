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

  private final UserFixture origin;
  private final int count;
  private TestUser user;
  private TearDown tearDown;

  private UserWithOutcomeInvitationsFixture(UserFixture origin, int count) {
    this.origin = origin;
    this.count = count;
  }

  public static UserFixture wrap(UserFixture origin, int count) {
    return new UserWithOutcomeInvitationsFixture(origin, count);
  }

  @Override
  public void create() {
    origin.create();
    TestUser base = origin.user();
    List<ProfileFixture> invitees = new ArrayList<>();
    tearDown = new TearDown()
        .with(() -> Friendships.of(base).deleteInvitations())
        .with(() -> {
          for (int i = invitees.size() - 1; i >= 0; i--) {
            invitees.get(i).close();
          }
        })
        .with(origin::close);
    ProfileFixture.ensureFor(base);
    for (int i = 0; i < count; i++) {
      ProfileFixture invitee = ProfileFixture.createRelated();
      invitees.add(invitee);
      Friendship.create(base, invitee.user(), Friendship.PENDING);
    }
    user = base.withOutcomeInvitations(invitees.stream().map(ProfileFixture::user).toList());
  }

  @Override
  public TestUser user() {
    if (user == null) {
      throw new IllegalStateException("UserWithOutcomeInvitationsFixture.create() has not been called");
    }
    return user;
  }

  @Override
  public void close() {
    if (tearDown != null) {
      tearDown.close();
    } else {
      origin.close();
    }
  }
}
