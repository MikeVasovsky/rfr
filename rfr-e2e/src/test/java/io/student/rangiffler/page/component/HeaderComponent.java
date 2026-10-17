package io.student.rangiffler.page.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Шапка авторизованного приложения (banner): действия и проверки вокруг Logout.
 */
public final class HeaderComponent {

  private final Locator logoutButton;

  public HeaderComponent(Page page) {
    this.logoutButton = page.getByRole(AriaRole.BANNER)
        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Logout").setExact(true));
  }

  @Step("Проверить видимость кнопки Logout")
  public HeaderComponent checkThatLogoutIsVisible() {
    assertThat(logoutButton).isVisible();
    return this;
  }
}
