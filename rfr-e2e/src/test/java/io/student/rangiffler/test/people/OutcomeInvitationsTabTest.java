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
 * UI-проверки вкладки Outcome invitations: состав, отсутствие чужих связей, Waiting.../Cancel, пустое состояние.
 */
public class OutcomeInvitationsTabTest extends BaseUiTest {

  @Test
  @DisplayName("Без исходящих invitations вкладка Outcome invitations пустая")
  void shouldShowEmptyOutcomeInvitations(@User TestUser user) {
    login(user);

    Allure.step("На Outcome invitations нет пользователей", () ->
        new PeoplePage(page)
            .open()
            .clickOutcomeInvitationsTab()
            .checkThatNoUsersYet()
    );
  }

  @Test
  @DisplayName("На Outcome invitations только заявка с Waiting.../Cancel, без чужих связей")
  void shouldShowOnlyOutcomeInvitationWithWaitingCancel(
      @User(friends = 1, incomeInvitations = 1, outcomeInvitations = 1) TestUser user
  ) {
    login(user);

    TestUser friend = user.friends().get(0);
    TestUser income = user.incomeInvitations().get(0);
    TestUser outcome = user.outcomeInvitations().get(0);

    Allure.step("Outcome invitations: заявка с Waiting.../Cancel, чужие связи отсутствуют", () ->
        new PeoplePage(page)
            .open()
            .clickOutcomeInvitationsTab()
            .checkThatUsersCount(1)
            .checkThatOutcomeInvitation(outcome.username())
            .checkThatUserAbsent(friend.username())
            .checkThatUserAbsent(income.username())
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
