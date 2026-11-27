package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Locale;

/**
 * Поднимает и гасит Playwright-браузер {@link BaseUiTest} в {@code BeforeEach}/{@code AfterEach},
 * чтобы другие extension'ы (timeout, trace) видели контекст уже в {@code BeforeEachCallback}.
 */
@Order(200)
public final class BrowserLifecycleExtension implements BeforeEachCallback, AfterEachCallback {

  private static final TestConfig CFG = TestConfig.getInstance();

  @Override
  public void beforeEach(ExtensionContext context) {
    Object instance = context.getTestInstance().orElse(null);
    if (!(instance instanceof BaseUiTest uiTest)) {
      return;
    }
    Playwright playwright = Playwright.create();
    playwright.selectors().setTestIdAttribute("data-test-id");
    Browser browser = browserType(playwright).launch(
        new BrowserType.LaunchOptions().setHeadless(CFG.headless())
    );
    BrowserContext browserContext = browser.newContext(
        new Browser.NewContextOptions()
            .setViewportSize(CFG.viewportWidth(), CFG.viewportHeight())
    );
    Page page = browserContext.newPage();
    uiTest.bindBrowser(playwright, browser, browserContext, page);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    Object instance = context.getTestInstance().orElse(null);
    if (instance instanceof BaseUiTest uiTest) {
      uiTest.stopBrowser();
    }
  }

  private static BrowserType browserType(Playwright playwright) {
    return switch (CFG.browser().toLowerCase(Locale.ROOT)) {
      case "firefox" -> playwright.firefox();
      case "webkit" -> playwright.webkit();
      default -> playwright.chromium();
    };
  }
}
