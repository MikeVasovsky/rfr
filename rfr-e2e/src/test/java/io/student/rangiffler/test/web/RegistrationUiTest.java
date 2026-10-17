package io.student.rangiffler.test.web;

import io.qameta.allure.Allure;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.MyTravelsPage;
import io.student.rangiffler.page.RegisterPage;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * UI-тест регистрации и последующей авторизации нового пользователя через OAuth.
 */
public class RegistrationUiTest extends BaseUiTest {

  @Test
  @DisplayName("User can register and authorize successfully")
  void shouldRegisterAndAuthorize() {
    TestUser user = RandomDataUtils.createUser();
    String successMessage = "Congratulations! You've registered!";

    Allure.step("Open landing and go to registration", () ->
        new LandingPage(page)
            .open()
            .clickRegister()
    );

    Allure.step("Register a new user", () ->
        new RegisterPage(page)
            .fillRegistrationForm(user.username(), user.password())
            .clickSignUp()
            .checkThatSuccessMessage(successMessage)
            .clickSignIn()
    );

    Allure.step("Login with registered credentials and verify authorization", () ->
        new LandingPage(page)
            .clickLogin()
            .fillLoginForm(user.username(), user.password())
            .clickSignIn()
            .checkThatUrl(MyTravelsPage.URL)
            .checkThatUserIsAuthorized()
    );
  }
}
