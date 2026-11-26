package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Поднимает и гасит Playwright-браузер {@link BaseUiTest} в {@code BeforeEach}/{@code AfterEach},
 * чтобы другие extension'ы (timeout, trace) видели контекст уже в {@code BeforeEachCallback}.
 */
@Order(200)
public final class BrowserLifecycleExtension implements BeforeEachCallback, AfterEachCallback {

  @Override
  public void beforeEach(ExtensionContext context) {
    Object instance = context.getTestInstance().orElse(null);
    if (instance instanceof BaseUiTest uiTest) {
      uiTest.startBrowser();
    }
  }

  @Override
  public void afterEach(ExtensionContext context) {
    Object instance = context.getTestInstance().orElse(null);
    if (instance instanceof BaseUiTest uiTest) {
      uiTest.stopBrowser();
    }
  }
}
