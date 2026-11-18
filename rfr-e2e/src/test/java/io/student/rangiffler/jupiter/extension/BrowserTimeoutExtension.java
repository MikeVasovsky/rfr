package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.Page;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Ставит action-timeout Playwright на {@link Page} из {@link BaseUiTest}
 * после {@code @BeforeEach} (когда страница уже создана).
 */
public final class BrowserTimeoutExtension implements BeforeTestExecutionCallback {

  private static final TestConfig CFG = TestConfig.getInstance();

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    Object instance = context.getRequiredTestInstance();
    if (!(instance instanceof BaseUiTest uiTest)) {
      return;
    }
    Page page = uiTest.page();
    if (page != null) {
      page.setDefaultTimeout(CFG.defaultTimeoutMs());
    }
  }
}
