package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.student.rangiffler.utils.LocatorOptions.exactRole;

/**
 * Лендинг фронта ({@code /}): старт сценария — переход к регистрации или OAuth-логину.
 */
public final class LandingPage {

  public static final String URL = TestConfig.getInstance().frontUrl() + "/";

  private final Page page;
  private final Locator loginButton;
  private final Locator registerLink;

  public LandingPage(Page page) {
    this.page = page;
    this.loginButton = page.getByRole(AriaRole.BUTTON, exactRole("Login"));
    this.registerLink = page.getByRole(AriaRole.LINK, exactRole("Register"));
  }

  @Step("Открыть лендинг")
  public LandingPage open() {
    page.navigate(URL);
    return this;
  }

  @Step("Проверить URL: {expectedUrl}")
  public LandingPage checkThatUrl(String expectedUrl) {
    assertThat(page).hasURL(expectedUrl);
    return this;
  }

  @Step("Проверить, что видна кнопка Login")
  public LandingPage checkThatLoginIsVisible() {
    assertThat(loginButton).isVisible();
    return this;
  }

  @Step("Нажать Register")
  public RegisterPage clickRegister() {
    registerLink.click();
    return new RegisterPage(page);
  }

  @Step("Нажать Login")
  public LoginPage clickLogin() {
    loginButton.click();
    return new LoginPage(page);
  }
}
