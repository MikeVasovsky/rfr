package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.CredentialsForm;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Страница регистрации auth-сервера ({@code /register}): создание пользователя и возврат на фронт.
 */
public final class RegisterPage {

  public static final String URL = TestConfig.getInstance().authUrl() + "/register";

  private final Page page;
  private final CredentialsForm credentialsForm;
  private final Locator passwordSubmitInput;
  private final Locator signUpButton;
  private final Locator signInLink;

  public RegisterPage(Page page) {
    this.page = page;
    this.credentialsForm = new CredentialsForm(page);
    this.passwordSubmitInput = page.getByPlaceholder("Repeat your password");
    this.signUpButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign up").setExact(true));
    this.signInLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Sign in").setExact(true));
  }

  @Step("Заполнить форму регистрации: {username} / {password}")
  public RegisterPage fillRegistrationForm(String username, String password) {
    credentialsForm.fillCredentials(username, password);
    passwordSubmitInput.fill(password);
    return this;
  }

  @Step("Нажать Sign up")
  public RegisterPage clickSignUp() {
    signUpButton.click();
    return this;
  }

  @Step("Проверить сообщение об успешной регистрации: {expectedMessage}")
  public RegisterPage checkThatSuccessMessage(String expectedMessage) {
    assertThat(page.getByText(expectedMessage, new Page.GetByTextOptions().setExact(true))).isVisible();
    return this;
  }

  @Step("Нажать Sign in после регистрации")
  public LandingPage clickSignIn() {
    signInLink.click();
    return new LandingPage(page);
  }
}
