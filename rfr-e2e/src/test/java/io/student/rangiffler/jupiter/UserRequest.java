package io.student.rangiffler.jupiter;

import io.student.rangiffler.data.fixture.SingleUserFixture;
import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.data.fixture.UserWithFriendsFixture;
import io.student.rangiffler.data.fixture.UserWithIncomeInvitationsFixture;
import io.student.rangiffler.jupiter.annotation.User;

/**
 * Запрос на тестового пользователя: поля из {@link User} и сборка цепочки {@link UserFixture}.
 */
public record UserRequest(int friends, int incomeInvitations) {

  public UserRequest {
    if (friends < 0) {
      throw new IllegalArgumentException("friends must be >= 0: " + friends);
    }
    if (incomeInvitations < 0) {
      throw new IllegalArgumentException("incomeInvitations must be >= 0: " + incomeInvitations);
    }
  }

  public static UserRequest from(User annotation) {
    return new UserRequest(annotation.friends(), annotation.incomeInvitations());
  }

  /**
   * Собирает цепочку фикстур: {@link SingleUserFixture}, при необходимости
   * {@link UserWithFriendsFixture} и {@link UserWithIncomeInvitationsFixture}.
   * Создание auth-user — в {@link SingleUserFixture}; друзья/инвайты — в декораторах.
   */
  public UserFixture fixture() {
    UserFixture fixture = SingleUserFixture.create(this);
    if (friends > 0) {
      fixture = new UserWithFriendsFixture(fixture);
    }
    if (incomeInvitations > 0) {
      fixture = new UserWithIncomeInvitationsFixture(fixture);
    }
    return fixture;
  }
}
