package io.student.rangiffler.test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.jupiter.extension.BrowserLifecycleExtension;
import io.student.rangiffler.jupiter.extension.BrowserTimeoutExtension;
import io.student.rangiffler.jupiter.extension.user.UserExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Locale;

/**
 * Базовый UI-тест: поля браузера и вкладки {@link Page}.
 * Lifecycle — {@link BrowserLifecycleExtension}; action-timeout — {@link BrowserTimeoutExtension}.
 */
@Tag("ui")
@ExtendWith({UserExtension.class, BrowserLifecycleExtension.class, BrowserTimeoutExtension.class})
public abstract class BaseUiTest {

  private static final TestConfig CFG = TestConfig.getInstance();

  protected Playwright playwright;
  protected Browser browser;
  protected BrowserContext context;
  protected Page page;

  public final Page page() {
    return page;
  }

  public final Browser browser() {
    return browser;
  }

  public final BrowserContext context() {
    return context;
  }

  public final void startBrowser() {
    playwright = Playwright.create();
    playwright.selectors().setTestIdAttribute("data-test-id");
    browser = browserType().launch(
        new BrowserType.LaunchOptions()
            .setHeadless(CFG.headless())
    );
    context = browser.newContext(
        new Browser.NewContextOptions()
            .setViewportSize(CFG.viewportWidth(), CFG.viewportHeight())
    );
    page = context.newPage();
  }

  public final void stopBrowser() {
    if (context != null) {
      context.close();
      context = null;
      page = null;
    }
    if (browser != null) {
      browser.close();
      browser = null;
    }
    if (playwright != null) {
      playwright.close();
      playwright = null;
    }
  }

  private BrowserType browserType() {
    return switch (CFG.browser().toLowerCase(Locale.ROOT)) {
      case "firefox" -> playwright.firefox();
      case "webkit" -> playwright.webkit();
      default -> playwright.chromium();
    };
  }
}
