package io.student.rangiffler.model;

import com.microsoft.playwright.Page;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.PeoplePage;

import java.util.Optional;

/**
 * Handle dual UI-сессии для теста: данные пользователя и фабрики Page Object.
 * Не {@link AutoCloseable} и не владеет lifecycle браузера ({@code SessionFixture}).
 * Login / register — только в тесте через PO ({@link #landingPage()} и далее).
 */
public final class UserSession {

  private final Optional<TestUser> user;
  private final Page page;

  public UserSession(TestUser user, Page page) {
    this.user = Optional.of(user);
    this.page = page;
  }

  public Optional<TestUser> user() {
    return user;
  }

  public LandingPage landingPage() {
    return new LandingPage(page);
  }

  public PeoplePage peoplePage() {
    return new PeoplePage(page);
  }
}
