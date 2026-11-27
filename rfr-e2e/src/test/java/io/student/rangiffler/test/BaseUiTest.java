package io.student.rangiffler.test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.student.rangiffler.jupiter.extension.BrowserLifecycleExtension;
import io.student.rangiffler.jupiter.extension.BrowserTimeoutExtension;
import io.student.rangiffler.jupiter.extension.user.UserExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Базовый UI-тест: поля браузера и вкладки {@link Page}.
 * Lifecycle — {@link BrowserLifecycleExtension}; action-timeout — {@link BrowserTimeoutExtension}.
 */
@Tag("ui")
@ExtendWith({UserExtension.class, BrowserLifecycleExtension.class, BrowserTimeoutExtension.class})
public abstract class BaseUiTest {

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

  public final void bindBrowser(
      Playwright playwright,
      Browser browser,
      BrowserContext context,
      Page page
  ) {
    this.playwright = playwright;
    this.browser = browser;
    this.context = context;
    this.page = page;
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
}
