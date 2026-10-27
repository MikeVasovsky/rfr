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
 * Декоратор: {@code friends} ACCEPTED-связей (main → companion).
 */
public final class UserWithFriendsFixture implements UserFixture {

  private final UserFixture delegate;
  private final List<TestUser> companions = new ArrayList<>();
  private final TearDown tearDown;

  public UserWithFriendsFixture(UserFixture delegate) {
    this.delegate = Objects.requireNonNull(delegate);
    String me = delegate.user().username();
    boolean mainProfileCreated = Profile.ensure(me);
    Profile main = Profile.require(me);
    for (int i = 0; i < delegate.request().friends(); i++) {
      TestUser friendUser = RandomDataUtils.createUser();
      AuthAccount.create(friendUser);
      Profile friend = Profile.create(friendUser.username());
      Friendship.accepted(main, friend);
      companions.add(friendUser);
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
