package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import io.student.rangiffler.config.TestConfig;

/**
 * Опции Playwright для {@code @UsePlaywright}: browser, headless, viewport, {@code data-test-id}.
 * Сейчас UI-suite поднимает браузер в {@code BaseUiTest}; фабрика — для явного {@code @UsePlaywright}.
 */
public final class BrowserOptionsFactory implements OptionsFactory {

  private static final TestConfig CFG = TestConfig.getInstance();

  @Override
  public Options getOptions() {
    return new Options()
        .setBrowserName(CFG.browser())
        .setHeadless(CFG.headless())
        .setTestIdAttribute("data-test-id")
        .setContextOptions(
            new Browser.NewContextOptions()
                .setViewportSize(CFG.viewportWidth(), CFG.viewportHeight())
        );
  }
}
