package io.student.rangiffler.test.auth;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Негативные UI-сценарии логина: неверный пароль и несуществующий пользователь.
 * Spring Security маскирует {@code UsernameNotFoundException} как Bad credentials.
 */
public class LoginNegativeTest extends AuthUiTest {

  private static final String BAD_CREDENTIALS = "Bad credentials";
  private static final String WRONG_PASSWORD = "wrong-password";

  @Test
  @DisplayName("Неверный пароль показывает Bad credentials")
  void shouldShowBadCredentialsWhenPasswordWrong(@User TestUser user) {
    Allure.step("Войти с неверным паролем и проверить ошибку", () ->
        openLogin()
            .fillLoginForm(user.username(), WRONG_PASSWORD)
            .clickSignInExpectingError()
            .checkThatError(BAD_CREDENTIALS)
    );
  }

  @Test
  @DisplayName("Несуществующий пользователь показывает Bad credentials")
  void shouldShowBadCredentialsWhenUserUnknown() {
    TestUser unknown = RandomDataUtils.createUser();

    Allure.step("Войти несуществующим пользователем и проверить ошибку", () ->
        openLogin()
            .fillLoginForm(unknown.username(), unknown.password())
            .clickSignInExpectingError()
            .checkThatError(BAD_CREDENTIALS)
    );
  }
}
