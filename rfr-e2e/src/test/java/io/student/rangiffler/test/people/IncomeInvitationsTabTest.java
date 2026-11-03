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
 * UI-проверки вкладки Income invitations: состав, отсутствие чужих связей, Accept/Decline, пустое состояние.
 */
public class IncomeInvitationsTabTest extends BaseUiTest {

  @Test
  @DisplayName("Без входящих invitations вкладка Income invitations пустая")
  void shouldShowEmptyIncomeInvitations(@User TestUser user) {
    login(user);

    Allure.step("На Income invitations нет пользователей", () ->
        new PeoplePage(page)
            .open()
            .clickIncomeInvitationsTab()
            .checkThatNoUsersYet()
    );
  }

  @Test
  @DisplayName("На Income invitations только заявка с Accept/Decline, без чужих связей")
  void shouldShowOnlyIncomeInvitationWithAcceptDecline(
      @User(friends = 1, incomeInvitations = 1, outcomeInvitations = 1) TestUser user
  ) {
    login(user);

    TestUser friend = user.friends().get(0);
    TestUser income = user.incomeInvitations().get(0);
    TestUser outcome = user.outcomeInvitations().get(0);

    Allure.step("Income invitations: заявка с Accept/Decline, чужие связи отсутствуют", () ->
        new PeoplePage(page)
            .open()
            .clickIncomeInvitationsTab()
            .checkThatUsersCount(1)
            .checkThatIncomeInvitation(income.username())
            .checkThatUserAbsent(friend.username())
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
