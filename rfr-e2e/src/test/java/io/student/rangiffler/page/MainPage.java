package io.student.rangiffler.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.HeaderComponent;

import static io.student.rangiffler.utils.LocatorOptions.exactRole;

/**
 * Главная после входа ({@code /my-travels}): навигация в People / Profile и выход.
 */
public final class MainPage {

  public static final String URL = TestConfig.getInstance().frontUrl() + "/my-travels";

  private final Page page;
  private final HeaderComponent header;

  public MainPage(Page page) {
    this.page = page;
    this.header = new HeaderComponent(page);
  }

  @Step("Открыть People")
  public PeoplePage clickPeople() {
    page.getByRole(AriaRole.LINK, exactRole("People")).click();
    return new PeoplePage(page);
  }

  @Step("Открыть профиль")
  public ProfilePage clickProfile() {
    return header.clickProfile();
  }

  @Step("Нажать Logout")
  public LandingPage clickLogout() {
    return header.clickLogout();
  }
}
