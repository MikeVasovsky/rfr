package io.student.rangiffler.test.session;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.MultiSessionUiTest;
import io.student.rangiffler.jupiter.annotation.session.Session;
import io.student.rangiffler.model.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Dual UI: принятие заявки (дружба) и удаление дружбы между двумя пользователями из БД.
 */
@MultiSessionUiTest
final class DualFriendshipLifecycleTest {

  @Test
  @DisplayName("Alice и Bob становятся друзьями после Accept")
  void shouldBecomeFriendsAfterAccept(
      @Session UserSession alice,
      @Session UserSession bob
  ) {
    String aliceUsername = alice.user().orElseThrow().username();
    String bobUsername = bob.user().orElseThrow().username();

    Allure.step("Alice и Bob авторизуются", () -> {
      SessionUi.login(alice);
      SessionUi.login(bob);
    });

    Allure.step("Alice отправляет заявку Bob", () ->
        alice.peoplePage()
            .open()
            .clickAllPeopleTab()
            .searchPeople(bobUsername)
            .clickAddUser(bobUsername)
    );

    Allure.step("Bob принимает заявку Alice", () ->
        bob.peoplePage()
            .open()
            .clickIncomeInvitationsTab()
            .clickAcceptUser(aliceUsername)
    );

    Allure.step("У Bob Alice в Friends", () ->
        bob.peoplePage()
            .clickFriendsTab()
            .checkThatFriend(aliceUsername)
    );

    Allure.step("У Alice Bob в Friends", () ->
        alice.peoplePage()
            .open()
            .clickFriendsTab()
            .checkThatFriend(bobUsername)
    );
  }

  @Test
  @DisplayName("После Remove дружба пропадает у обоих")
  void shouldRemoveFriendshipFromBothSides(
      @Session UserSession alice,
      @Session UserSession bob
  ) {
    String aliceUsername = alice.user().orElseThrow().username();
    String bobUsername = bob.user().orElseThrow().username();

    Allure.step("Alice и Bob авторизуются", () -> {
      SessionUi.login(alice);
      SessionUi.login(bob);
    });

    Allure.step("Alice отправляет заявку, Bob принимает", () -> {
      alice.peoplePage()
          .open()
          .clickAllPeopleTab()
          .searchPeople(bobUsername)
          .clickAddUser(bobUsername);
      bob.peoplePage()
          .open()
          .clickIncomeInvitationsTab()
          .clickAcceptUser(aliceUsername);
    });

    Allure.step("Alice удаляет Bob из друзей", () ->
        alice.peoplePage()
            .open()
            .clickFriendsTab()
            .checkThatFriend(bobUsername)
            .clickRemoveFriend(bobUsername)
            .checkThatUserAbsent(bobUsername)
    );

    Allure.step("У Bob Alice больше не в Friends", () ->
        bob.peoplePage()
            .open()
            .clickFriendsTab()
            .checkThatUserAbsent(aliceUsername)
    );
  }
}
