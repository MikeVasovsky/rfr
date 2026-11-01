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
 * UI-проверки прекондишенов {@link User}: друзья и входящие invitations видны на People.
 */
public class PeoplePreconditionsTest extends BaseUiTest {

  @Test
  @DisplayName("Без друзей вкладка Friends пустая")
  void shouldShowEmptyFriendsWithoutFixtureFriends(@User TestUser user) {
    login(user);

    Allure.step("На Friends нет пользователей", () ->
        new PeoplePage(page)
            .open()
            .checkThatUrl(PeoplePage.URL)
            .clickFriendsTab()
            .checkThatNoUsersYet()
    );
  }

  @Test
  @DisplayName("Друзья из фикстуры видны на Friends")
  void shouldShowFriendsFromFixture(@User(friends = 2) TestUser user) {
    login(user);

    Allure.step("На Friends две строки друзей", () ->
        new PeoplePage(page)
            .open()
            .clickFriendsTab()
            .checkThatUsersCount(2)
    );
  }

  @Test
  @DisplayName("Входящие invitations из фикстуры видны на Income invitations")
  void shouldShowIncomeInvitationsFromFixture(@User(incomeInvitations = 2) TestUser user) {
    login(user);

    Allure.step("На Income invitations две заявки", () ->
        new PeoplePage(page)
            .open()
            .clickIncomeInvitationsTab()
            .checkThatUsersCount(2)
    );
  }

  @Test
  @DisplayName("Друзья и входящие invitations из фикстуры видны вместе")
  void shouldShowFriendsAndIncomeInvitationsFromFixture(
      @User(friends = 1, incomeInvitations = 1) TestUser user
  ) {
    login(user);

    Allure.step("Friends и Income invitations по одной записи", () -> {
      PeoplePage people = new PeoplePage(page).open();
      people.clickFriendsTab().checkThatUsersCount(1);
      people.clickIncomeInvitationsTab().checkThatUsersCount(1);
    });
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
