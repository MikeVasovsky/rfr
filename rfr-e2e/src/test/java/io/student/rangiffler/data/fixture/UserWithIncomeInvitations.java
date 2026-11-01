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
  private final List<TestUser> inviters;
  private final boolean deleteMainProfile;
  private boolean closed;

  private UserWithIncomeInvitations(
      UserFixture delegate,
      List<TestUser> inviters,
      boolean deleteMainProfile
  ) {
    this.delegate = delegate;
    this.inviters = inviters;
    this.deleteMainProfile = deleteMainProfile;
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
    return new UserWithIncomeInvitations(delegate, inviters, deleteMainProfile);
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
      ApiFriendship.deleteInvitations(delegateUserId);
    }
    for (TestUser inviter : inviters) {
      ApiProfile.of(inviter).delete();
    }
    if (deleteMainProfile && delegateUserId != null) {
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
