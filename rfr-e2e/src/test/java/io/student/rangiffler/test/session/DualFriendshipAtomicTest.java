package io.student.rangiffler.test.session;

import io.qameta.allure.Allure;
import io.student.rangiffler.domain.Notification;
import io.student.rangiffler.jupiter.annotation.MultiSessionUiTest;
import io.student.rangiffler.jupiter.annotation.session.Session;
import io.student.rangiffler.model.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Атомарные dual UI-проверки friendship: одно действие на тест, два пользователя из БД.
 */
@MultiSessionUiTest
final class DualFriendshipAtomicTest {

  @Test
  @DisplayName("Отправка заявки: Outcome у отправителя, Income у получателя")
  void shouldSendFriendshipInvitation(
      @Session UserSession alice,
      @Session UserSession bob
  ) {
    String aliceUsername = alice.user().orElseThrow().username();
    String bobUsername = bob.user().orElseThrow().username();

    Allure.step("Предусловие: оба логинятся", () -> loginBoth(alice, bob));

    Allure.step("Alice отправляет заявку Bob и ждёт уведомление", () ->
        sendInvitation(alice, bobUsername)
    );

    Allure.step("У Alice Bob в Outcome invitations", () ->
        alice.peoplePage()
            .clickOutcomeInvitationsTab()
            .checkThatOutcomeInvitation(bobUsername)
    );

    Allure.step("У Bob Alice в Income invitations", () ->
        bob.peoplePage()
            .open()
            .clickIncomeInvitationsTab()
            .checkThatIncomeInvitation(aliceUsername)
    );
  }

  @Test
  @DisplayName("Accept: оба видят друг друга в Friends")
  void shouldAcceptFriendshipInvitation(
      @Session UserSession alice,
      @Session UserSession bob
  ) {
    String aliceUsername = alice.user().orElseThrow().username();
    String bobUsername = bob.user().orElseThrow().username();

    Allure.step("Предусловие: login и заявка Alice → Bob", () -> {
      loginBoth(alice, bob);
      sendInvitation(alice, bobUsername);
    });

    Allure.step("Bob принимает заявку Alice", () ->
        bob.peoplePage()
            .open()
            .clickIncomeInvitationsTab()
            .clickAcceptUser(aliceUsername)
            .checkThatNotification(Notification.INVITATION_ACCEPTED)
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
  @DisplayName("Decline: заявка исчезает у обоих")
  void shouldDeclineFriendshipInvitation(
      @Session UserSession alice,
      @Session UserSession bob
  ) {
    String aliceUsername = alice.user().orElseThrow().username();
    String bobUsername = bob.user().orElseThrow().username();

    Allure.step("Предусловие: login и заявка Alice → Bob", () -> {
      loginBoth(alice, bob);
      sendInvitation(alice, bobUsername);
    });

    Allure.step("Bob отклоняет заявку Alice", () ->
        bob.peoplePage()
            .open()
            .clickIncomeInvitationsTab()
            .clickDeclineUser(aliceUsername)
            .checkThatNotification(Notification.INVITATION_DECLINED)
    );

    Allure.step("У Bob нет Alice во входящих", () ->
        bob.peoplePage()
            .clickIncomeInvitationsTab()
            .checkThatUserAbsent(aliceUsername)
    );

    Allure.step("У Alice нет Bob в исходящих", () ->
        alice.peoplePage()
            .open()
            .clickOutcomeInvitationsTab()
            .checkThatUserAbsent(bobUsername)
    );
  }

  @Test
  @DisplayName("Cancel: отправитель отменяет исходящую заявку")
  void shouldCancelOutgoingFriendshipInvitation(
      @Session UserSession alice,
      @Session UserSession bob
  ) {
    String aliceUsername = alice.user().orElseThrow().username();
    String bobUsername = bob.user().orElseThrow().username();

    Allure.step("Предусловие: login и заявка Alice → Bob", () -> {
      loginBoth(alice, bob);
      sendInvitation(alice, bobUsername);
    });

    Allure.step("Alice отменяет исходящую заявку Bob", () ->
        alice.peoplePage()
            .clickOutcomeInvitationsTab()
            .clickCancelOutcome(bobUsername)
            .checkThatNotification(Notification.INVITATION_CANCELLED)
    );

    Allure.step("У Alice нет Bob в исходящих", () ->
        alice.peoplePage()
            .clickOutcomeInvitationsTab()
            .checkThatUserAbsent(bobUsername)
    );

    Allure.step("У Bob нет Alice во входящих", () ->
        bob.peoplePage()
            .open()
            .clickIncomeInvitationsTab()
            .checkThatUserAbsent(aliceUsername)
    );
  }

  @Test
  @DisplayName("Remove со стороны отправителя заявки")
  void shouldRemoveFriendshipByRequester(
      @Session UserSession alice,
      @Session UserSession bob
  ) {
    String aliceUsername = alice.user().orElseThrow().username();
    String bobUsername = bob.user().orElseThrow().username();

    Allure.step("Предусловие: login, заявка и Accept", () -> {
      loginBoth(alice, bob);
      becomeFriends(alice, bobUsername, bob, aliceUsername);
    });

    Allure.step("Alice удаляет Bob из друзей", () ->
        alice.peoplePage()
            .open()
            .clickFriendsTab()
            .clickRemoveFriend(bobUsername)
            .checkThatNotification(Notification.FRIEND_DELETED)
            .checkThatUserAbsent(bobUsername)
    );

    Allure.step("У Bob нет Alice в Friends", () ->
        bob.peoplePage()
            .open()
            .clickFriendsTab()
            .checkThatUserAbsent(aliceUsername)
    );
  }

  @Test
  @DisplayName("Remove со стороны принявшего заявку")
  void shouldRemoveFriendshipByAddressee(
      @Session UserSession alice,
      @Session UserSession bob
  ) {
    String aliceUsername = alice.user().orElseThrow().username();
    String bobUsername = bob.user().orElseThrow().username();

    Allure.step("Предусловие: login, заявка и Accept", () -> {
      loginBoth(alice, bob);
      becomeFriends(alice, bobUsername, bob, aliceUsername);
    });

    Allure.step("Bob удаляет Alice из друзей", () ->
        bob.peoplePage()
            .open()
            .clickFriendsTab()
            .clickRemoveFriend(aliceUsername)
            .checkThatNotification(Notification.FRIEND_DELETED)
            .checkThatUserAbsent(aliceUsername)
    );

    Allure.step("У Alice нет Bob в Friends", () ->
        alice.peoplePage()
            .open()
            .clickFriendsTab()
            .checkThatUserAbsent(bobUsername)
    );
  }

  private static void loginBoth(UserSession alice, UserSession bob) {
    SessionUi.login(alice);
    SessionUi.login(bob);
  }

  private static void sendInvitation(UserSession from, String toUsername) {
    from.peoplePage()
        .open()
        .clickAllPeopleTab()
        .searchPeople(toUsername)
        .clickAddUser(toUsername)
        .checkThatNotification(Notification.INVITATION_SENT);
  }

  private static void becomeFriends(
      UserSession requester,
      String addresseeUsername,
      UserSession addressee,
      String requesterUsername
  ) {
    sendInvitation(requester, addresseeUsername);
    addressee.peoplePage()
        .open()
        .clickIncomeInvitationsTab()
        .clickAcceptUser(requesterUsername)
        .checkThatNotification(Notification.INVITATION_ACCEPTED);
  }
}
