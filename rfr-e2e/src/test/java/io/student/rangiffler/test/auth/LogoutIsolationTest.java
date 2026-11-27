package io.student.rangiffler.test.auth;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.Session;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.user.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.MainPage;
import io.student.rangiffler.page.PeopleTab;
import io.student.rangiffler.session.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Изоляция BrowserContext: logout на основном {@link Page} не разлогинивает {@link UserSession}.
 */
@UiTest
@DisplayName("Изоляция logout между контекстами")
class LogoutIsolationTest extends AuthUiTest {

  @Test
  @DisplayName("Выход в одном контексте оставляет другой вошедшим")
  void logoutKeepsOtherContextSignedIn(Page page, @Session @User(friends = 1) UserSession bob) {
    TestUser friend = bob.user().friends().getFirst();

    Allure.step("На основном Page войти как Bob, выйти и увидеть гостевой лендинг", () ->
        openLogin()
            .fillLoginForm(bob.user().username(), bob.user().password())
            .clickSignIn()
            .checkThatUserIsAuthorized()
    );
    Allure.step("После Logout доступны Login и Register", () ->
        new MainPage(page).clickLogout().checkThatLoginAvailable()
    );
    Allure.step("В сессии Bob на Friends виден друг — сессия всё ещё авторизована", () ->
        bob.mainPage()
            .clickPeople()
            .openTab(PeopleTab.FRIENDS)
            .table()
            .checkThatPeople(List.of(friend.username()))
    );
  }
}
