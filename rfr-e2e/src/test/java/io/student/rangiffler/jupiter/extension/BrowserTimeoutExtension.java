package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Ставит action-timeout Playwright на контекст/страницу UI-теста.
 * {@link #apply(BrowserContext)} — для основного и session-контекстов.
 * Также резолвит параметр {@link Page} из {@link BaseUiTest}.
 */
@Order(300)
public final class BrowserTimeoutExtension implements BeforeEachCallback, ParameterResolver {

  private static final TestConfig CFG = TestConfig.getInstance();

  @Override
  public void beforeEach(ExtensionContext context) {
    Object instance = context.getTestInstance().orElse(null);
    if (!(instance instanceof BaseUiTest uiTest)) {
      return;
    }
    BrowserContext browserContext = uiTest.context();
    if (browserContext != null) {
      apply(browserContext);
    }
  }

  /**
   * Таймауты по умолчанию для любого {@link BrowserContext} (основной или session).
   */
  public static void apply(BrowserContext browser) {
    browser.setDefaultTimeout(CFG.defaultTimeoutMs());
    browser.setDefaultNavigationTimeout(CFG.defaultTimeoutMs());
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.getParameter().getType() == Page.class
        && extensionContext.getTestMethod()
        .filter(parameterContext.getDeclaringExecutable()::equals)
        .isPresent()
        && extensionContext.getTestInstance().filter(BaseUiTest.class::isInstance).isPresent();
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return ((BaseUiTest) extensionContext.getRequiredTestInstance()).page();
  }
}
