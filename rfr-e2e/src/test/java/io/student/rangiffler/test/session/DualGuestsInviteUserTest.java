package io.student.rangiffler.test.session;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.MultiSessionUiTest;
import io.student.rangiffler.jupiter.annotation.session.Session;
import io.student.rangiffler.model.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Dual UI: два гостя регистрируются через UI и отправляют заявки пользователю из БД.
 */
@MultiSessionUiTest
final class DualGuestsInviteUserTest {

  @Test
  @DisplayName("Два гостя добавляют в друзья пользователя из БД, он принимает обе заявки")
  void shouldAcceptInvitationsFromTwoGuests(
      @Session UserSession host,
      @Session(guest = true) UserSession guestOne,
      @Session(guest = true) UserSession guestTwo
  ) {
    String hostUsername = host.user().orElseThrow().username();
    String guestOneUsername = guestOne.user().orElseThrow().username();
    String guestTwoUsername = guestTwo.user().orElseThrow().username();

    Allure.step("Host логинится, гости регистрируются и логинятся", () -> {
      SessionUi.login(host);
      SessionUi.registerAndLogin(guestOne);
      SessionUi.registerAndLogin(guestTwo);
    });

    Allure.step("Гости отправляют заявки host", () -> {
      guestOne.peoplePage()
          .open()
          .clickAllPeopleTab()
          .searchPeople(hostUsername)
          .clickAddUser(hostUsername);
      guestTwo.peoplePage()
          .open()
          .clickAllPeopleTab()
          .searchPeople(hostUsername)
          .clickAddUser(hostUsername);
    });

    Allure.step("Host видит обе входящие заявки и принимает их", () -> {
      host.peoplePage()
          .open()
          .clickIncomeInvitationsTab()
          .checkThatIncomeInvitation(guestOneUsername)
          .checkThatIncomeInvitation(guestTwoUsername)
          .clickAcceptUser(guestOneUsername)
          .clickAcceptUser(guestTwoUsername);
    });

    Allure.step("У host оба гостя в Friends", () ->
        host.peoplePage()
            .clickFriendsTab()
            .checkThatFriend(guestOneUsername)
            .checkThatFriend(guestTwoUsername)
    );
  }
}
