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

  private final UserFixture origin;
  private final int count;
  private TestUser user;
  private TearDown tearDown;

  private UserWithFriendsFixture(UserFixture origin, int count) {
    this.origin = origin;
    this.count = count;
  }

  public static UserFixture wrap(UserFixture origin, int count) {
    return new UserWithFriendsFixture(origin, count);
  }

  @Override
  public void create() {
    origin.create();
    TestUser base = origin.user();
    List<ProfileFixture> friends = new ArrayList<>();
    tearDown = new TearDown()
        .with(() -> Friendships.of(base).deleteFriends())
        .with(() -> {
          for (int i = friends.size() - 1; i >= 0; i--) {
            friends.get(i).close();
          }
        })
        .with(origin::close);
    ProfileFixture.ensureFor(base);
    for (int i = 0; i < count; i++) {
      ProfileFixture friend = ProfileFixture.createRelated();
      friends.add(friend);
      Friendship.create(base, friend.user(), Friendship.ACCEPTED);
    }
    user = base.withFriends(friends.stream().map(ProfileFixture::user).toList());
  }

  @Override
  public TestUser user() {
    if (user == null) {
      throw new IllegalStateException("UserWithFriendsFixture.create() has not been called");
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
