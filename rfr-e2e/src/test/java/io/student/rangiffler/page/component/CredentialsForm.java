package io.student.rangiffler.page.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

/**
 * Переиспользуемый блок полей username/password на формах login и register auth-сервера.
 */
public final class CredentialsForm {

  private final Locator usernameInput;
  private final Locator passwordInput;

  public CredentialsForm(Page page) {
    this.usernameInput = page.getByPlaceholder("Type your username");
    this.passwordInput = page.getByPlaceholder("Type your password");
  }

  @Step("Заполнить учётные данные: {username} / {password}")
  public CredentialsForm fillCredentials(String username, String password) {
    usernameInput.fill(username);
    passwordInput.fill(password);
    return this;
  }
}
