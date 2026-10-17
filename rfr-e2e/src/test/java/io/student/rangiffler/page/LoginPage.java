package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.CredentialsForm;

/**
 * Страница логина auth-сервера ({@code /login}) в OAuth/PKCE-потоке после нажатия Login на лендинге.
 */
public final class LoginPage {

  public static final String URL = TestConfig.getInstance().authUrl() + "/login";

  private final Page page;
  private final CredentialsForm credentialsForm;
  private final Locator signInButton;

  public LoginPage(Page page) {
    this.page = page;
    this.credentialsForm = new CredentialsForm(page);
    this.signInButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in").setExact(true));
  }

  @Step("Заполнить форму входа: {username} / {password}")
  public LoginPage fillLoginForm(String username, String password) {
    credentialsForm.fillCredentials(username, password);
    return this;
  }

  @Step("Нажать Sign in")
  public MyTravelsPage clickSignIn() {
    signInButton.click();
    return new MyTravelsPage(page);
  }
}
