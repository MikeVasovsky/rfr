package io.student.rangiffler.test.fake;

import com.microsoft.playwright.TimeoutError;
import io.qameta.allure.Allure;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверяет, что {@code BrowserTimeoutExtension} выставил action-timeout из {@link TestConfig}.
 */
@Tag("fake")
class BrowserTimeoutExtensionTest extends BaseUiTest {

  @Test
  @DisplayName("Action-timeout на Page равен TestConfig.defaultTimeoutMs (5 с)")
  void actionTimeoutMatchesConfig() {
    double expectedMs = TestConfig.getInstance().defaultTimeoutMs();
    Allure.step("Конфиг ожидает 5000 мс", () -> assertEquals(5000.0, expectedMs));

    long started = System.nanoTime();
    TimeoutError error = null;
    try {
      page.locator("#rfr-timeout-probe-missing").click();
    } catch (TimeoutError e) {
      error = e;
    }
    long elapsedMs = (System.nanoTime() - started) / 1_000_000L;

    TimeoutError timeoutError = error;
    Allure.step("Клик по отсутствующему элементу упал с TimeoutError", () ->
        assertInstanceOf(TimeoutError.class, timeoutError)
    );
    Allure.step("В сообщении есть Timeout 5000ms", () ->
        assertTrue(
            timeoutError.getMessage().contains("Timeout 5000ms"),
            () -> "expected Timeout 5000ms in: " + timeoutError.getMessage()
        )
    );
    Allure.step("Фактическое ожидание около 5 с (не 15/30)", () ->
        assertTrue(
            elapsedMs >= 4500 && elapsedMs < 12000,
            () -> "elapsedMs=" + elapsedMs
        )
    );
  }
}
