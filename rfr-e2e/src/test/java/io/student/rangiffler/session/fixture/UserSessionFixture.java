package io.student.rangiffler.session.fixture;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.MainPage;
import io.student.rangiffler.page.MyTravelsPage;
import io.student.rangiffler.session.UserSession;

/**
 * Сессия вошедшего пользователя: UI-login в {@link #create()}, в тест — {@link UserSession}.
 */
public final class UserSessionFixture implements SessionFixture<UserSession> {

  private final BrowserContext context;
  private final TestUser user;
  private MainPage mainPage;

  public UserSessionFixture(BrowserContext context, TestUser user) {
    this.context = context;
    this.user = user;
  }

  @Override
  public UserSession session() {
    if (mainPage == null) {
      throw new IllegalStateException("User session of " + user.username() + " was not created");
    }
    return new UserSession(user, mainPage);
  }

  @Override
  public void create() {
    Page page = context.newPage();
    new LandingPage(page).open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn();
    new MyTravelsPage(page).checkThatUserIsAuthorized();
    mainPage = new MainPage(page);
  }

  @Override
  public void close() {
    try {
      context.close();
    } catch (RuntimeException ignored) {
      // browser мог уже закрыться в BaseUiTest
    }
  }
}
