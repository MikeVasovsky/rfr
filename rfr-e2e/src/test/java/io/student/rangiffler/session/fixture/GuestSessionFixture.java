package io.student.rangiffler.session.fixture;

import com.microsoft.playwright.BrowserContext;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.session.GuestSession;

/**
 * Гостевая сессия: только открытый лендинг, без create пользователя в БД.
 */
public final class GuestSessionFixture implements SessionFixture<GuestSession> {

  private final BrowserContext context;
  private LandingPage landingPage;

  public GuestSessionFixture(BrowserContext context) {
    this.context = context;
  }

  @Override
  public GuestSession session() {
    if (landingPage == null) {
      throw new IllegalStateException("Guest session was not created");
    }
    return new GuestSession(landingPage);
  }

  @Override
  public void create() {
    landingPage = new LandingPage(context.newPage()).open();
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
