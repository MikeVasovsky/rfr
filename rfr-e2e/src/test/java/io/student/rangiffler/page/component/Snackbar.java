package io.student.rangiffler.page.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.domain.Notification;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.student.rangiffler.utils.LocatorOptions.exactLocatorText;

/**
 * Snackbar/alert уведомления приложения.
 */
public final class Snackbar {

  private final Locator alert;

  public Snackbar(Page page) {
    this.alert = page.getByRole(AriaRole.ALERT);
  }

  @Step("Проверить уведомление: {notification}")
  public Snackbar checkThatShown(Notification notification) {
    assertThat(alert.getByText(notification.text(), exactLocatorText())).isVisible();
    return this;
  }

  @Step("Проверить уведомление с текстом сервера: {text}")
  public Snackbar checkThatShown(String text) {
    assertThat(alert.getByText(text, exactLocatorText())).isVisible();
    return this;
  }
}
