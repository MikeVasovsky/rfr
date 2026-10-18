package io.student.rangiffler.test.auth;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.LoginPage;
import io.student.rangiffler.page.MyTravelsPage;
import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * UI-тест авторизации пользователя, заранее созданного в auth БД через {@link User}.
 */
public class LoginTest extends BaseUiTest {

  @Test
  @DisplayName("Пользователь из БД может авторизоваться")
  void shouldAuthorizeExistingUser(@User TestUser user) {
    Allure.step("Открыть лендинг и перейти к логину", () ->
        new LandingPage(page)
            .open()
            .clickLogin()
    );

    Allure.step("Войти и проверить авторизацию", () ->
        new LoginPage(page)
            .fillLoginForm(user.username(), user.password())
            .clickSignIn()
            .checkThatUrl(MyTravelsPage.URL)
            .checkThatUserIsAuthorized()
    );
  }
}
