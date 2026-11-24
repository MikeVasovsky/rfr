package io.student.rangiffler.page;

import com.microsoft.playwright.Page;
import io.student.rangiffler.model.TestUser;

/**
 * UI-login через Page Objects: лендинг → login → {@link MyTravelsPage}.
 */
public final class UiAuthenticator {

  public MyTravelsPage login(Page page, TestUser user) {
    return new LandingPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .checkThatUrl(MyTravelsPage.URL);
  }
}
