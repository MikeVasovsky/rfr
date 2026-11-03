package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.student.rangiffler.utils.LocatorOptions.exactLocatorRole;
import static io.student.rangiffler.utils.LocatorOptions.exactLocatorText;
import static io.student.rangiffler.utils.LocatorOptions.exactRole;
import static io.student.rangiffler.utils.LocatorOptions.exactText;

/**
 * Страница People ({@code /people}): вкладки Friends / Income / Outcome invitations и таблица пользователей.
 */
public final class PeoplePage {

  public static final String URL = TestConfig.getInstance().frontUrl() + "/people";

  private final Page page;
  private final Locator friendsTab;
  private final Locator incomeInvitationsTab;
  private final Locator outcomeInvitationsTab;
  private final Locator emptyUsersMessage;
  private final Locator tableRows;

  public PeoplePage(Page page) {
    this.page = page;
    this.friendsTab = page.getByRole(AriaRole.TAB, exactRole("Friends"));
    this.incomeInvitationsTab = page.getByRole(AriaRole.TAB, exactRole("Income invitations"));
    this.outcomeInvitationsTab = page.getByRole(AriaRole.TAB, exactRole("Outcome invitations"));
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

  @Step("Открыть вкладку Outcome invitations")
  public PeoplePage clickOutcomeInvitationsTab() {
    outcomeInvitationsTab.click();
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

  @Step("Проверить, что пользователь {username} отсутствует в таблице")
  public PeoplePage checkThatUserAbsent(String username) {
    assertThat(userRow(username)).hasCount(0);
    return this;
  }

  @Step("Проверить друга {username}: строка и кнопка Remove")
  public PeoplePage checkThatFriend(String username) {
    Locator row = userRow(username);
    assertThat(row).isVisible();
    assertThat(row.getByRole(AriaRole.BUTTON, exactLocatorRole("Remove"))).isVisible();
    return this;
  }

  @Step("Проверить входящее invitation {username}: строка и кнопки Accept / Decline")
  public PeoplePage checkThatIncomeInvitation(String username) {
    Locator row = userRow(username);
    assertThat(row).isVisible();
    assertThat(row.getByRole(AriaRole.BUTTON, exactLocatorRole("Accept"))).isVisible();
    assertThat(row.getByRole(AriaRole.BUTTON, exactLocatorRole("Decline"))).isVisible();
    return this;
  }

  @Step("Проверить исходящее invitation {username}: строка, Waiting... и Cancel")
  public PeoplePage checkThatOutcomeInvitation(String username) {
    Locator row = userRow(username);
    assertThat(row).isVisible();
    assertThat(row.getByText("Waiting...", exactLocatorText())).isVisible();
    assertThat(row.getByRole(AriaRole.BUTTON, exactLocatorRole("Cancel"))).isVisible();
    return this;
  }

  private Locator userRow(String username) {
    return page.getByRole(AriaRole.ROW)
        .filter(new Locator.FilterOptions()
            .setHas(page.getByText(username, exactText())));
  }
}
