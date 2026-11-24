package io.student.rangiffler.test.session;

import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.model.UserSession;
import io.student.rangiffler.page.MyTravelsPage;

/**
 * UI login / register для dual-тестов: только цепочки Page Object, без скрытия в {@link UserSession}.
 */
final class SessionUi {

  private static final String REGISTER_SUCCESS = "Congratulations! You've registered!";

  private SessionUi() {
  }

  static MyTravelsPage login(UserSession session) {
    TestUser user = session.user().orElseThrow();
    return session.landingPage()
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .checkThatUrl(MyTravelsPage.URL)
        .checkThatUserIsAuthorized();
  }

  static MyTravelsPage registerAndLogin(UserSession session) {
    TestUser user = session.user().orElseThrow();
    return session.landingPage()
        .open()
        .clickRegister()
        .fillRegistrationForm(user.username(), user.password())
        .clickSignUp()
        .checkThatSuccessMessage(REGISTER_SUCCESS)
        .clickSignIn()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .checkThatUrl(MyTravelsPage.URL)
        .checkThatUserIsAuthorized();
  }
}
