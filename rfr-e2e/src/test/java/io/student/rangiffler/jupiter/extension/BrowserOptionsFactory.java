package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import io.student.rangiffler.config.TestConfig;

/**
 * Опции Playwright для {@code @UsePlaywright}: headless, viewport и атрибут {@code data-test-id}.
 */
public final class BrowserOptionsFactory implements OptionsFactory {

  private static final TestConfig CFG = TestConfig.getInstance();

  @Override
  public Options getOptions() {
    return new Options()
        .setHeadless(CFG.headless())
        .setTestIdAttribute("data-test-id")
        .setContextOptions(
            new Browser.NewContextOptions()
                .setViewportSize(CFG.viewportWidth(), CFG.viewportHeight())
        );
  }
}
