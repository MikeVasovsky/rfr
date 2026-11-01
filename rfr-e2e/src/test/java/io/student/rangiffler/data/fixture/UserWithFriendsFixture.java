package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.db.ApiFriendship;
import io.student.rangiffler.data.db.ApiProfile;
import io.student.rangiffler.model.TestProfile;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.ArrayList;
import java.util.List;

import static io.student.rangiffler.data.db.ApiProfile.findUserId;
import static io.student.rangiffler.data.db.Databases.API;

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
    JdbcClient jdbc = API.jdbcClient();
    byte[] delegateUserId = requireUserId(jdbc, delegate.user());
    List<TestUser> friends = new ArrayList<>();
    for (int i = 0; i < friendship; i++) {
      TestUser friend = RandomDataUtils.createUser();
      TestProfile profile = RandomDataUtils.createProfile();
      ApiProfile.create(friend, profile);
      byte[] friendId = findUserId(jdbc, friend);
      if (friendId == null) {
        throw new IllegalStateException("id друга не найден: " + friend.username());
      }
      ApiFriendship.create(delegateUserId, friendId, "ACCEPTED");
      friends.add(friend);
    }
    TearDown tearDown = new TearDown()
        .with(() -> {
          byte[] id = findUserId(API.jdbcClient(), delegate.user());
          if (id != null) {
            ApiFriendship.deleteFriends(id);
          }
        })
        .withEach(friends, friend -> ApiProfile.of(friend).delete())
        .with(() -> {
          byte[] id = findUserId(API.jdbcClient(), delegate.user());
          if (id != null) {
            ApiProfile.of(delegate.user()).delete();
          }
        })
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

  private static byte[] requireUserId(JdbcClient jdbc, TestUser user) {
    byte[] userId = findUserId(jdbc, user);
    if (userId == null) {
      ApiProfile.create(user, RandomDataUtils.createProfile());
      userId = findUserId(jdbc, user);
    }
    if (userId == null) {
      throw new IllegalStateException("id пользователя не найден в api: " + user.username());
    }
    return userId;
  }
}
