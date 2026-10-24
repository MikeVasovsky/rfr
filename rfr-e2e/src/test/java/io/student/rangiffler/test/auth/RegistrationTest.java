package io.student.rangiffler.test.auth;

import io.qameta.allure.Allure;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.MyTravelsPage;
import io.student.rangiffler.page.RegisterPage;
import io.student.rangiffler.test.BaseUiTest;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * UI-тест регистрации и последующей авторизации нового пользователя через OAuth.
 */
public class RegistrationTest extends BaseUiTest {

  @Test
  @DisplayName("Пользователь может зарегистрироваться и авторизоваться")
  void shouldRegisterAndAuthorize() {
    TestUser user = RandomDataUtils.createUser();
    String successMessage = "Congratulations! You've registered!";

    Allure.step("Открыть лендинг и перейти к регистрации", () ->
        new LandingPage(page)
            .open()
            .clickRegister()
    );

    Allure.step("Зарегистрировать нового пользователя", () ->
        new RegisterPage(page)
            .fillRegistrationForm(user.username(), user.password())
            .clickSignUp()
            .checkThatSuccessMessage(successMessage)
            .clickSignIn()
    );

    Allure.step("Войти с зарегистрированными данными и проверить авторизацию", () ->
        new LandingPage(page)
            .clickLogin()
            .fillLoginForm(user.username(), user.password())
            .clickSignIn()
            .checkThatUrl(MyTravelsPage.URL)
            .checkThatUserIsAuthorized()
    );
  }
}
