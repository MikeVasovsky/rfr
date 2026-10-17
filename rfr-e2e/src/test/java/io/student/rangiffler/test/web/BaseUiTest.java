package io.student.rangiffler.test.web;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Базовый UI-тест: поднимает и гасит Playwright-браузер и вкладку {@link Page} вокруг каждого теста.
 */
public abstract class BaseUiTest {

  protected Playwright playwright;
  protected Browser browser;
  protected BrowserContext context;
  protected Page page;

  @BeforeEach
  void setUpBrowser() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(
        new BrowserType.LaunchOptions()
            .setHeadless(Boolean.parseBoolean(System.getenv().getOrDefault("HEADLESS", "true")))
    );
    context = browser.newContext(
        new Browser.NewContextOptions()
            .setViewportSize(1280, 720)
    );
    page = context.newPage();
    page.setDefaultTimeout(15_000);
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
}
