package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.jupiter.UserRequest;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Декоратор: {@code incomeInvitations} PENDING (companion → main).
 */
public final class UserWithIncomeInvitationsFixture implements UserFixture {

  private final UserFixture delegate;
  private final List<TestUser> companions = new ArrayList<>();
  private final TearDown tearDown;

  public UserWithIncomeInvitationsFixture(UserFixture delegate) {
    this.delegate = Objects.requireNonNull(delegate);
    String me = delegate.user().username();
    boolean mainProfileCreated = Profile.ensure(me);
    Profile main = Profile.require(me);
    for (int i = 0; i < delegate.request().incomeInvitations(); i++) {
      TestUser inviterUser = RandomDataUtils.createUser();
      AuthAccount.create(inviterUser);
      Profile inviter = Profile.create(inviterUser.username());
      Friendship.invitation(inviter, main);
      companions.add(inviterUser);
    }
    this.tearDown = new TearDown()
        .with(() -> Friendships.of(me).deleteAll())
        .withEach(companions, companion -> {
          Profile.of(companion.username()).delete();
          AuthAccount.of(companion).delete();
        })
        .with(() -> {
          if (mainProfileCreated) {
            Profile.of(me).delete();
          }
        })
        .with(delegate::close);
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
    tearDown.close();
  }
}
