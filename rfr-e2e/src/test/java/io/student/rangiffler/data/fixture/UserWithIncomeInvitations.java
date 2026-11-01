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
 * Декоратор: к пользователю добавляет N входящих invitations (PENDING) в api.
 */
public final class UserWithIncomeInvitations implements UserFixture {

  private final UserFixture delegate;
  private final TearDown tearDown;

  private UserWithIncomeInvitations(UserFixture delegate, TearDown tearDown) {
    this.delegate = delegate;
    this.tearDown = tearDown;
  }

  public static UserFixture wrap(UserFixture delegate, int incomeInvitations) {
    JdbcClient jdbc = API.jdbcClient();
    byte[] delegateUserId = requireUserId(jdbc, delegate.user());
    List<TestUser> inviters = new ArrayList<>();
    for (int i = 0; i < incomeInvitations; i++) {
      TestUser inviter = RandomDataUtils.createUser();
      TestProfile profile = RandomDataUtils.createProfile();
      ApiProfile.create(inviter, profile);
      byte[] inviterId = findUserId(jdbc, inviter);
      if (inviterId == null) {
        throw new IllegalStateException("id отправителя invitation не найден: " + inviter.username());
      }
      ApiFriendship.create(inviterId, delegateUserId, "PENDING");
      inviters.add(inviter);
    }
    boolean deleteMainProfile = !(delegate instanceof UserWithFriendsFixture);
    TearDown tearDown = new TearDown()
        .with(() -> {
          byte[] id = findUserId(API.jdbcClient(), delegate.user());
          if (id != null) {
            ApiFriendship.deleteInvitations(id);
          }
        })
        .withEach(inviters, inviter -> ApiProfile.of(inviter).delete())
        .with(() -> {
          if (!deleteMainProfile) {
            return;
          }
          byte[] id = findUserId(API.jdbcClient(), delegate.user());
          if (id != null) {
            ApiProfile.of(delegate.user()).delete();
          }
        })
        .with(delegate::close);
    return new UserWithIncomeInvitations(delegate, tearDown);
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
