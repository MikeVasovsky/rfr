package io.student.rangiffler.test.session;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.MultiSessionUiTest;
import io.student.rangiffler.jupiter.annotation.session.Session;
import io.student.rangiffler.model.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Dual UI: заявка в друзья из одной сессии видна как входящая в другой.
 */
@MultiSessionUiTest
final class DualFriendshipInvitationTest {

  @Test
  @DisplayName("Alice отправляет заявку Bob, Bob видит её во входящих")
  void shouldShowIncomeInvitationAfterPeerSendsRequest(
      @Session UserSession alice,
      @Session UserSession bob
  ) {
    String aliceUsername = alice.user().orElseThrow().username();
    String bobUsername = bob.user().orElseThrow().username();

    Allure.step("Alice и Bob авторизуются", () -> {
      SessionUi.login(alice);
      SessionUi.login(bob);
    });

    Allure.step("Alice отправляет заявку Bob через All People", () ->
        alice.peoplePage()
            .open()
            .clickAllPeopleTab()
            .searchPeople(bobUsername)
            .clickAddUser(bobUsername)
    );

    Allure.step("У Alice заявка видна в Outcome invitations", () ->
        alice.peoplePage()
            .clickOutcomeInvitationsTab()
            .checkThatOutcomeInvitation(bobUsername)
    );

    Allure.step("У Bob заявка видна в Income invitations", () ->
        bob.peoplePage()
            .open()
            .clickIncomeInvitationsTab()
            .checkThatIncomeInvitation(aliceUsername)
    );
  }
}
