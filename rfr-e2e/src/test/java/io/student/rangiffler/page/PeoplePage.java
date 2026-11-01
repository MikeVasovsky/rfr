package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.student.rangiffler.utils.LocatorOptions.exactRole;
import static io.student.rangiffler.utils.LocatorOptions.exactText;

/**
 * Страница People ({@code /people}): вкладки Friends / Income invitations и таблица пользователей.
 */
public final class PeoplePage {

  public static final String URL = TestConfig.getInstance().frontUrl() + "/people";

  private final Page page;
  private final Locator friendsTab;
  private final Locator incomeInvitationsTab;
  private final Locator emptyUsersMessage;
  private final Locator tableRows;

  public PeoplePage(Page page) {
    this.page = page;
    this.friendsTab = page.getByRole(AriaRole.TAB, exactRole("Friends"));
    this.incomeInvitationsTab = page.getByRole(AriaRole.TAB, exactRole("Income invitations"));
    this.emptyUsersMessage = page.getByText("There are no users yet", exactText());
    this.tableRows = page.getByRole(AriaRole.ROW);
  }

  @Step("Открыть страницу People")
  public PeoplePage open() {
    page.navigate(URL);
    return this;
  }

  @Step("Проверить URL: {expectedUrl}")
  public PeoplePage checkThatUrl(String expectedUrl) {
    assertThat(page).hasURL(expectedUrl);
    return this;
  }

  @Step("Открыть вкладку Friends")
  public PeoplePage clickFriendsTab() {
    friendsTab.click();
    return this;
  }

  @Step("Открыть вкладку Income invitations")
  public PeoplePage clickIncomeInvitationsTab() {
    incomeInvitationsTab.click();
    return this;
  }

  @Step("Проверить, что в таблице {expectedCount} пользователей")
  public PeoplePage checkThatUsersCount(int expectedCount) {
    assertThat(tableRows).hasCount(expectedCount + 1);
    return this;
  }

  @Step("Проверить пустой список пользователей")
  public PeoplePage checkThatNoUsersYet() {
    assertThat(emptyUsersMessage).isVisible();
    return this;
  }
}
