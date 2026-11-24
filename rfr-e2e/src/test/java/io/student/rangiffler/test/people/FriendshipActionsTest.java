package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.domain.Notification;
import io.student.rangiffler.jupiter.annotation.Session;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.user.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.PeoplePage;
import io.student.rangiffler.page.PeopleTab;
import io.student.rangiffler.session.UserSession;
import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Атомарные действия дружбы на двух сессиях: основной {@link Page} и {@link UserSession}.
 */
@UiTest
@DisplayName("Действия с дружбой на двух сессиях")
class FriendshipActionsTest extends BaseUiTest {

  @Test
  @DisplayName("Отправленная заявка появляется у адресата во входящих")
  void invitationReachesAddressee(Page page, @User TestUser alice, @Session @User UserSession bob) {
    String username = bob.user().username();
    PeoplePage people = PeopleLogin.open(page, alice)
        .openTab(PeopleTab.ALL_PEOPLE)
        .searchPeople(username);
    people.table().row(username).clickAdd();
    people.checkThatNotification(Notification.INVITATION_SENT);
    bob.mainPage()
        .clickPeople()
        .openTab(PeopleTab.INCOME_INVITATIONS)
        .table()
        .checkThatPeople(List.of(alice.username()));
  }

  @Test
  @DisplayName("Принятая заявка делает отправителя и адресата друзьями")
  void acceptedInvitationMakesFriends(Page page, @Session @User(outcomeInvitations = 1) UserSession alice) {
    TestUser invitee = alice.user().outcomeInvitations().getFirst();
    PeoplePage people = PeopleLogin.open(page, invitee).openTab(PeopleTab.INCOME_INVITATIONS);
    people.table().row(alice.user().username()).clickAccept();
    people.checkThatNotification(Notification.INVITATION_ACCEPTED);
    alice.mainPage()
        .clickPeople()
        .openTab(PeopleTab.FRIENDS)
        .table()
        .checkThatPeople(List.of(invitee.username()));
  }

  @Test
  @DisplayName("Отклонённая заявка пропадает из исходящих отправителя")
  void declinedInvitationLeavesSender(Page page, @Session @User(outcomeInvitations = 1) UserSession alice) {
    PeoplePage people = PeopleLogin.open(page, alice.user().outcomeInvitations().getFirst())
        .openTab(PeopleTab.INCOME_INVITATIONS);
    people.table().row(alice.user().username()).clickDecline();
    people.checkThatNotification(Notification.INVITATION_DECLINED);
    alice.mainPage()
        .clickPeople()
        .openTab(PeopleTab.OUTCOME_INVITATIONS)
        .table()
        .checkThatEmpty();
  }

  @Test
  @DisplayName("Дружба, удалённая отправителем, пропадает у адресата")
  void friendRemovedByRequesterDisappearsForAddressee(Page page, @Session @User(friends = 1) UserSession alice) {
    TestUser friend = alice.user().friends().getFirst();
    PeoplePage people = alice.mainPage().clickPeople().openTab(PeopleTab.FRIENDS);
    people.table().row(friend.username()).clickRemove();
    people.checkThatNotification(Notification.FRIEND_DELETED);
    PeopleLogin.open(page, friend)
        .openTab(PeopleTab.FRIENDS)
        .table()
        .checkThatEmpty();
  }

  @Test
  @DisplayName("Дружба, удалённая адресатом, пропадает у отправителя")
  void friendRemovedByAddresseeDisappearsForRequester(Page page, @Session @User(friends = 1) UserSession alice) {
    PeoplePage people = PeopleLogin.open(page, alice.user().friends().getFirst()).openTab(PeopleTab.FRIENDS);
    people.table().row(alice.user().username()).clickRemove();
    people.checkThatNotification(Notification.FRIEND_DELETED);
    alice.mainPage()
        .clickPeople()
        .openTab(PeopleTab.FRIENDS)
        .table()
        .checkThatEmpty();
  }
}
