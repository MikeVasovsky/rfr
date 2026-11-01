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
  private final List<TestUser> friends;
  private boolean closed;

  private UserWithFriendsFixture(UserFixture delegate, List<TestUser> friends) {
    this.delegate = delegate;
    this.friends = friends;
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
    return new UserWithFriendsFixture(delegate, friends);
  }

  @Override
  public TestUser user() {
    return delegate.user();
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }
    JdbcClient jdbc = API.jdbcClient();
    byte[] delegateUserId = findUserId(jdbc, delegate.user());
    if (delegateUserId != null) {
      ApiFriendship.deleteFriends(delegateUserId);
    }
    for (TestUser friend : friends) {
      ApiProfile.of(friend).delete();
    }
    if (delegateUserId != null) {
      ApiProfile.of(delegate.user()).delete();
    }
    delegate.close();
    closed = true;
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
