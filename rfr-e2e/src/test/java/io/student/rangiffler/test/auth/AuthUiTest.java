package io.student.rangiffler.test.auth;

import io.qameta.allure.Allure;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.LoginPage;
import io.student.rangiffler.test.BaseUiTest;

/**
 * База UI-тестов auth: общий вход на страницу логина через лендинг.
 */
public abstract class AuthUiTest extends BaseUiTest {

  protected LoginPage openLogin() {
    return Allure.step("Открыть лендинг и перейти к логину", () ->
        new LandingPage(page)
            .open()
            .clickLogin()
    );
  }
}
