package io.student.rangiffler.test.people;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.user.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.MyTravelsPage;
import io.student.rangiffler.page.PeoplePage;
import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * UI-проверки вкладки Friends на People: состав, отсутствие чужих связей, Remove, пустое состояние.
 */
public class FriendsTabTest extends BaseUiTest {

  @Test
  @DisplayName("Без друзей вкладка Friends пустая")
  void shouldShowEmptyFriends(@User TestUser user) {
    login(user);

    Allure.step("На Friends нет пользователей", () ->
        new PeoplePage(page)
            .open()
            .clickFriendsTab()
            .checkThatNoUsersYet()
    );
  }

  @Test
  @DisplayName("На Friends только друг с Remove, без чужих invitations")
  void shouldShowOnlyFriendWithRemove(
      @User(friends = 1, incomeInvitations = 1, outcomeInvitations = 1) TestUser user
  ) {
    login(user);

    TestUser friend = user.friends().get(0);
    TestUser income = user.incomeInvitations().get(0);
    TestUser outcome = user.outcomeInvitations().get(0);

    Allure.step("Friends: друг с Remove, invitations отсутствуют", () ->
        new PeoplePage(page)
            .open()
            .clickFriendsTab()
            .checkThatUsersCount(1)
            .checkThatFriend(friend.username())
            .checkThatUserAbsent(income.username())
            .checkThatUserAbsent(outcome.username())
    );
  }

  private void login(TestUser user) {
    Allure.step("Открыть лендинг и авторизоваться", () ->
        new LandingPage(page)
            .open()
            .clickLogin()
            .fillLoginForm(user.username(), user.password())
            .clickSignIn()
            .checkThatUrl(MyTravelsPage.URL)
    );
  }
}
