package io.student.rangiffler.data.fixture.user;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.fixture.TearDown;
import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.model.TestUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Декоратор: к пользователю добавляет N принятых друзей в api.
 */
public final class UserWithFriendsFixture implements UserFixture {

  private final TestUser user;
  private final TearDown tearDown;

  private UserWithFriendsFixture(TestUser user, TearDown tearDown) {
    this.user = user;
    this.tearDown = tearDown;
  }

  public static UserFixture wrap(UserFixture delegate, int friendship) {
    TestUser base = delegate.user();
    ProfileFixture.ensureFor(base);
    List<ProfileFixture> friends = new ArrayList<>();
    for (int i = 0; i < friendship; i++) {
      ProfileFixture friend = ProfileFixture.createRelated();
      Friendship.create(base, friend.user(), Friendship.ACCEPTED);
      friends.add(friend);
    }
    TearDown tearDown = new TearDown()
        .with(() -> Friendships.of(base).deleteFriends())
        .withEach(friends, ProfileFixture::close)
        .with(delegate::close);
    List<TestUser> friendUsers = friends.stream().map(ProfileFixture::user).toList();
    return new UserWithFriendsFixture(base.withFriends(friendUsers), tearDown);
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
