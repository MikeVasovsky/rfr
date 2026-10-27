package io.student.rangiffler.test.people;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.MyTravelsPage;
import io.student.rangiffler.page.PeoplePage;
import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * UI e2e: прекондишены дружбы и входящих заявок из {@link User} видны на People.
 */
public class PeopleFriendshipTest extends BaseUiTest {

  @Test
  @DisplayName("@User(friends=2, incomeInvitations=1): Friends и Income заполнены")
  void shouldShowFriendsAndIncomeInvitations(
      @User(friends = 2, incomeInvitations = 1) TestUser user
  ) {
    PeoplePage people = loginAndOpenPeople(user);

    Allure.step("Вкладка Friends: 2 пользователя", () ->
        people.clickFriendsTab().checkThatUsersCount(2)
    );
    Allure.step("Вкладка Income invitations: 1 пользователь", () ->
        people.clickIncomeInvitationsTab().checkThatUsersCount(1)
    );
  }

  @Test
  @DisplayName("@User(friends=2): Friends заполнены, Income пустой")
  void shouldShowOnlyFriends(@User(friends = 2) TestUser user) {
    PeoplePage people = loginAndOpenPeople(user);

    Allure.step("Вкладка Friends: 2 пользователя", () ->
        people.clickFriendsTab().checkThatUsersCount(2)
    );
    Allure.step("Вкладка Income invitations: пусто", () ->
        people.clickIncomeInvitationsTab().checkThatEmpty()
    );
  }

  @Test
  @DisplayName("@User(incomeInvitations=1): Income заполнен, Friends пустой")
  void shouldShowOnlyIncomeInvitations(@User(incomeInvitations = 1) TestUser user) {
    PeoplePage people = loginAndOpenPeople(user);

    Allure.step("Вкладка Friends: пусто", () ->
        people.clickFriendsTab().checkThatEmpty()
    );
    Allure.step("Вкладка Income invitations: 1 пользователь", () ->
        people.clickIncomeInvitationsTab().checkThatUsersCount(1)
    );
  }

  private PeoplePage loginAndOpenPeople(TestUser user) {
    return Allure.step("Авторизоваться и открыть People", () -> {
      new LandingPage(page)
          .open()
          .clickLogin()
          .fillLoginForm(user.username(), user.password())
          .clickSignIn()
          .checkThatUrl(MyTravelsPage.URL);
      return new MyTravelsPage(page)
          .clickPeople()
          .checkThatUrl(PeoplePage.URL);
    });
  }
}
