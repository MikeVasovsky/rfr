package io.student.rangiffler.test.session;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.MultiSessionUiTest;
import io.student.rangiffler.jupiter.annotation.session.Session;
import io.student.rangiffler.model.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Dual UI: принятие и удаление дружбы между пользователем из БД и гостем (UI-register).
 */
@MultiSessionUiTest
final class DualUserGuestFriendshipTest {

  @Test
  @DisplayName("Гость и пользователь становятся друзьями после Accept")
  void shouldBecomeFriendsAfterAccept(
      @Session UserSession user,
      @Session(guest = true) UserSession guest
  ) {
    String userUsername = user.user().orElseThrow().username();
    String guestUsername = guest.user().orElseThrow().username();

    Allure.step("Пользователь логинится, гость регистрируется и логинится", () -> {
      SessionUi.login(user);
      SessionUi.registerAndLogin(guest);
    });

    Allure.step("Гость отправляет заявку пользователю", () ->
        guest.peoplePage()
            .open()
            .clickAllPeopleTab()
            .searchPeople(userUsername)
            .clickAddUser(userUsername)
    );

    Allure.step("Пользователь принимает заявку гостя", () ->
        user.peoplePage()
            .open()
            .clickIncomeInvitationsTab()
            .clickAcceptUser(guestUsername)
    );

    Allure.step("У пользователя гость в Friends", () ->
        user.peoplePage()
            .clickFriendsTab()
            .checkThatFriend(guestUsername)
    );

    Allure.step("У гостя пользователь в Friends", () ->
        guest.peoplePage()
            .open()
            .clickFriendsTab()
            .checkThatFriend(userUsername)
    );
  }

  @Test
  @DisplayName("После Remove дружба пользователя и гостя пропадает у обоих")
  void shouldRemoveFriendshipFromBothSides(
      @Session UserSession user,
      @Session(guest = true) UserSession guest
  ) {
    String userUsername = user.user().orElseThrow().username();
    String guestUsername = guest.user().orElseThrow().username();

    Allure.step("Пользователь логинится, гость регистрируется и логинится", () -> {
      SessionUi.login(user);
      SessionUi.registerAndLogin(guest);
    });

    Allure.step("Гость отправляет заявку, пользователь принимает", () -> {
      guest.peoplePage()
          .open()
          .clickAllPeopleTab()
          .searchPeople(userUsername)
          .clickAddUser(userUsername);
      user.peoplePage()
          .open()
          .clickIncomeInvitationsTab()
          .clickAcceptUser(guestUsername);
    });

    Allure.step("Пользователь удаляет гостя из друзей", () ->
        user.peoplePage()
            .open()
            .clickFriendsTab()
            .checkThatFriend(guestUsername)
            .clickRemoveFriend(guestUsername)
            .checkThatUserAbsent(guestUsername)
    );

    Allure.step("У гостя пользователь больше не в Friends", () ->
        guest.peoplePage()
            .open()
            .clickFriendsTab()
            .checkThatUserAbsent(userUsername)
    );
  }
}
