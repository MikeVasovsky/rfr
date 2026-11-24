package io.student.rangiffler.model;

import com.microsoft.playwright.Page;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.MyTravelsPage;
import io.student.rangiffler.page.PeoplePage;
import io.student.rangiffler.page.RegisterPage;
import io.student.rangiffler.page.UiAuthenticator;

import java.util.Optional;

/**
 * Handle dual UI-сессии для теста: данные пользователя и фабрики Page Object.
 * Не {@link AutoCloseable} и не владеет lifecycle браузера ({@code SessionFixture}).
 */
public final class UserSession {

  private static final UiAuthenticator AUTHENTICATOR = new UiAuthenticator();
  private static final String REGISTER_SUCCESS = "Congratulations! You've registered!";

  private final Optional<TestUser> user;
  private final Page page;

  public UserSession(TestUser user, Page page) {
    this.user = Optional.of(user);
    this.page = page;
  }

  public Optional<TestUser> user() {
    return user;
  }

  public MyTravelsPage login() {
    return AUTHENTICATOR.login(page, user.orElseThrow());
  }

  public MyTravelsPage registerAndLogin() {
    TestUser credentials = user.orElseThrow();
    new LandingPage(page).open().clickRegister();
    new RegisterPage(page)
        .fillRegistrationForm(credentials.username(), credentials.password())
        .clickSignUp()
        .checkThatSuccessMessage(REGISTER_SUCCESS)
        .clickSignIn();
    return login();
  }

  public MyTravelsPage mainPage() {
    return new MyTravelsPage(page);
  }

  public PeoplePage peoplePage() {
    return new PeoplePage(page);
  }
}
