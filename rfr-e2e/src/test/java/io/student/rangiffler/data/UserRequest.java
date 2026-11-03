package io.student.rangiffler.data;

import io.student.rangiffler.data.fixture.SingleUserFixture;
import io.student.rangiffler.data.fixture.UserFixture;
import io.student.rangiffler.data.fixture.UserWithFriendsFixture;
import io.student.rangiffler.data.fixture.UserWithIncomeInvitations;
import io.student.rangiffler.data.fixture.UserWithOutcomeInvitationsFixture;
import io.student.rangiffler.jupiter.annotation.User;

public record UserRequest(int friends, int incomeInvitations, int outcomeInvitations) {

  public UserRequest {
    if (friends < 0 || incomeInvitations < 0 || outcomeInvitations < 0) {
      throw new IllegalArgumentException(
          "friends, incomeInvitations and outcomeInvitations must be >= 0, but were: "
              + friends + ", " + incomeInvitations + ", " + outcomeInvitations
      );
    }
  }

  public UserRequest(User user) {
    this(user.friends(), user.incomeInvitations(), user.outcomeInvitations());
  }

  public static UserRequest from(User user) {
    return new UserRequest(user);
  }

  public UserFixture fixture() {
    UserFixture fixture = SingleUserFixture.create();
    if (friends > 0) {
      fixture = UserWithFriendsFixture.wrap(fixture, friends);
    }
    if (incomeInvitations > 0) {
      fixture = UserWithIncomeInvitations.wrap(fixture, incomeInvitations);
    }
    if (outcomeInvitations > 0) {
      fixture = UserWithOutcomeInvitationsFixture.wrap(fixture, outcomeInvitations);
    }
    return fixture;
  }
}
