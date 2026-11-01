package io.student.rangiffler.data;

import io.student.rangiffler.data.fixture.SingleUserFixture;
import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.data.fixture.UserWithFriendsFixture;
import io.student.rangiffler.data.fixture.UserWithIncomeInvitations;
import io.student.rangiffler.jupiter.annotation.User;

public record UserRequest(int friends, int incomeInvitations) {

  public UserRequest {
    if (friends < 0 || incomeInvitations < 0) {
      throw new IllegalArgumentException(
          "friends and incomeInvitations must be >= 0, but were: " + friends + ", " + incomeInvitations
      );
    }
  }

  public static UserRequest from(User user) {
    return new UserRequest(user.friends(), user.incomeInvitations());
  }

  public UserFixture fixture() {
    UserFixture fixture = SingleUserFixture.create();
    if (friends > 0) {
      fixture = UserWithFriendsFixture.wrap(fixture, friends);
    }
    if (incomeInvitations > 0) {
      fixture = UserWithIncomeInvitations.wrap(fixture, incomeInvitations);
    }
    return fixture;
  }
}
