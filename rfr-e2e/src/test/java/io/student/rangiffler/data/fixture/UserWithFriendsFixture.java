package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.model.TestUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Декоратор: к пользователю добавляет N принятых друзей в api.
 */
public final class UserWithFriendsFixture implements UserFixture {

  private final UserFixture delegate;
  private final TearDown tearDown;

  private UserWithFriendsFixture(UserFixture delegate, TearDown tearDown) {
    this.delegate = delegate;
    this.tearDown = tearDown;
  }

  public static UserFixture wrap(UserFixture delegate, int friendship) {
    ApiUserSupport.ensureProfile(delegate.user());
    List<TestUser> friends = new ArrayList<>();
    for (int i = 0; i < friendship; i++) {
      TestUser friend = ApiUserSupport.createRelatedUser();
      Friendship.create(delegate.user(), friend, Friendship.ACCEPTED);
      friends.add(friend);
    }
    TearDown tearDown = new TearDown()
        .with(() -> Friendships.of(delegate.user()).deleteFriends())
        .withEach(friends, friend -> Profile.of(friend).delete())
        .with(delegate::close);
    return new UserWithFriendsFixture(delegate, tearDown);
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
