package io.student.rangiffler.page;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.HeaderComponent;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Кабинет «My Travels» ({@code /my-travels}): экран после успешной авторизации (карта и лента).
 */
public final class MyTravelsPage {

  public static final String URL = TestConfig.getInstance().frontUrl() + "/my-travels";

  private final Page page;
  private final HeaderComponent header;

  public MyTravelsPage(Page page) {
    this.page = page;
    this.header = new HeaderComponent(page);
  }

  @Step("Проверить URL: {expectedUrl}")
  public MyTravelsPage checkThatUrl(String expectedUrl) {
    assertThat(page).hasURL(expectedUrl);
    return this;
  }

  @Step("Проверить, что пользователь авторизован")
  public MyTravelsPage checkThatUserIsAuthorized() {
    header.checkThatLogoutIsVisible();
    return this;
  }

  @Step("Открыть профиль через шапку")
  public ProfilePage clickProfile() {
    return header.clickProfile();
  }
}
