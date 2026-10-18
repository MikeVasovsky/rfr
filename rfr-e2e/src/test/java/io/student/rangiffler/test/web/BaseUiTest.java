package io.student.rangiffler.test.web;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.jupiter.extension.UserExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Locale;

/**
 * Базовый UI-тест: поднимает и гасит Playwright-браузер и вкладку {@link Page} вокруг каждого теста.
 */
@Tag("web")
@ExtendWith(UserExtension.class)
public abstract class BaseUiTest {

  private static final TestConfig CFG = TestConfig.getInstance();

  protected Playwright playwright;
  protected Browser browser;
  protected BrowserContext context;
  protected Page page;

  @BeforeEach
  void setUpBrowser() {
    playwright = Playwright.create();
    browser = browserType().launch(
        new BrowserType.LaunchOptions()
            .setHeadless(CFG.headless())
    );
    context = browser.newContext(
        new Browser.NewContextOptions()
            .setViewportSize(CFG.viewportWidth(), CFG.viewportHeight())
    );
    page = context.newPage();
    page.setDefaultTimeout(CFG.defaultTimeoutMs());
  }

  @AfterEach
  void tearDownBrowser() {
    if (context != null) {
      context.close();
    }
    if (browser != null) {
      browser.close();
    }
    if (playwright != null) {
      playwright.close();
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
