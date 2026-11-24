package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.MainPage;
import io.student.rangiffler.page.PeoplePage;

/**
 * Вход через UI на основном {@link Page} и переход на People.
 */
final class PeopleLogin {

  private PeopleLogin() {
  }

  static PeoplePage open(Page page, TestUser user) {
    new LandingPage(page).open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .checkThatUserIsAuthorized();
    return new MainPage(page).clickPeople();
  }
}
