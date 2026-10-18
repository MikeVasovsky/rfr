package io.student.rangiffler.page.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.page.ProfilePage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.student.rangiffler.utils.LocatorOptions.exactLocatorRole;

/**
 * Шапка авторизованного приложения (banner): Profile, Logout и связанные проверки.
 */
public final class HeaderComponent {

  private final Page page;
  private final Locator profileButton;
  private final Locator logoutButton;

  public HeaderComponent(Page page) {
    this.page = page;
    Locator banner = page.getByRole(AriaRole.BANNER);
    this.profileButton = banner.getByRole(AriaRole.BUTTON, exactLocatorRole("Profile"));
    this.logoutButton = banner.getByRole(AriaRole.BUTTON, exactLocatorRole("Logout"));
  }

  @Step("Нажать Profile")
  public ProfilePage clickProfile() {
    profileButton.click();
    return new ProfilePage(page);
  }

  @Step("Проверить видимость кнопки Logout")
  public HeaderComponent checkThatLogoutIsVisible() {
    assertThat(logoutButton).isVisible();
    return this;
  }
}
