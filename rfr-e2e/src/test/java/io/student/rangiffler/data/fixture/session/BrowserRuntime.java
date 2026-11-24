package io.student.rangiffler.data.fixture.session;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import io.student.rangiffler.config.TestConfig;

import java.util.Locale;

/**
 * Владелец {@link Playwright} + {@link Browser} на один dual UI-тест.
 */
public final class BrowserRuntime implements AutoCloseable {

  private Playwright playwright;
  private Browser browser;
  private boolean closed;

  public void launch(TestConfig config) {
    playwright = Playwright.create();
    playwright.selectors().setTestIdAttribute("data-test-id");
    browser = browserType(config).launch(
        new BrowserType.LaunchOptions().setHeadless(config.headless())
    );
  }

  public BrowserContext newContext(TestConfig config) {
    return browser.newContext(
        new Browser.NewContextOptions()
            .setViewportSize(config.viewportWidth(), config.viewportHeight())
    );
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }
    closed = true;
    RuntimeException first = null;
    try {
      if (browser != null) {
        browser.close();
      }
    } catch (RuntimeException e) {
      first = e;
    }
    try {
      if (playwright != null) {
        playwright.close();
      }
    } catch (RuntimeException e) {
      if (first == null) {
        first = e;
      } else {
        first.addSuppressed(e);
      }
    }
    if (first != null) {
      throw first;
    }
  }

  private BrowserType browserType(TestConfig config) {
    return switch (config.browser().toLowerCase(Locale.ROOT)) {
      case "firefox" -> playwright.firefox();
      case "webkit" -> playwright.webkit();
      default -> playwright.chromium();
    };
  }
}
