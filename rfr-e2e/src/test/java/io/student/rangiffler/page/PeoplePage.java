package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.domain.Notification;
import io.student.rangiffler.page.component.PeopleTable;
import io.student.rangiffler.page.component.Snackbar;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.student.rangiffler.utils.LocatorOptions.exactLabel;
import static io.student.rangiffler.utils.LocatorOptions.exactRole;

/**
 * Страница People ({@code /people}): вкладки и таблица пользователей ({@link PeopleTable}).
 */
public final class PeoplePage {

  public static final String URL = TestConfig.getInstance().frontUrl() + "/people";

  private final Page page;
  private final Locator friendsTab;
  private final Locator allPeopleTab;
  private final Locator incomeInvitationsTab;
  private final Locator outcomeInvitationsTab;
  private final Locator searchPeopleInput;
  private final Locator searchPeopleButton;
  private final PeopleTable peopleTable;
  private final Snackbar snackbar;

  public PeoplePage(Page page) {
    this.page = page;
    this.friendsTab = page.getByRole(AriaRole.TAB, exactRole("Friends"));
    this.allPeopleTab = page.getByRole(AriaRole.TAB, exactRole("All People"));
    this.incomeInvitationsTab = page.getByRole(AriaRole.TAB, exactRole("Income invitations"));
    this.outcomeInvitationsTab = page.getByRole(AriaRole.TAB, exactRole("Outcome invitations"));
    this.searchPeopleInput = page.getByLabel("search people", exactLabel());
    this.searchPeopleButton = page.getByRole(AriaRole.BUTTON, exactRole("search"));
    this.peopleTable = new PeopleTable(page);
    this.snackbar = new Snackbar(page);
  }

  public PeopleTable peopleTable() {
    return peopleTable;
  }

  public PeopleTable table() {
    return peopleTable;
  }

  @Step("Открыть страницу People")
  public PeoplePage open() {
    page.navigate(URL);
    return this;
  }

  @Step("Открыть вкладку {tab}")
  public PeoplePage openTab(PeopleTab tab) {
    page.getByRole(AriaRole.TAB, exactRole(tab.title())).click();
    return this;
  }

  @Step("Проверить URL: {expectedUrl}")
  public PeoplePage checkThatUrl(String expectedUrl) {
    assertThat(page).hasURL(expectedUrl);
    return this;
  }

  @Step("Проверить уведомление на странице People: {notification}")
  public PeoplePage checkThatNotification(Notification notification) {
    snackbar.checkThatShown(notification);
    return this;
  }

  @Step("Открыть вкладку Friends")
  public PeoplePage clickFriendsTab() {
    friendsTab.click();
    return this;
  }

  @Step("Открыть вкладку All People")
  public PeoplePage clickAllPeopleTab() {
    allPeopleTab.click();
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

  @Step("Найти людей по запросу: {query}")
  public PeoplePage searchPeople(String query) {
    searchPeopleInput.fill(query);
    searchPeopleButton.click();
    return this;
  }

  @Step("Отправить заявку в друзья пользователю {username}")
  public PeoplePage clickAddUser(String username) {
    peopleTable.row(username).clickAdd();
    return this;
  }

  @Step("Принять заявку от пользователя {username}")
  public PeoplePage clickAcceptUser(String username) {
    peopleTable.row(username).clickAccept();
    return this;
  }

  @Step("Отклонить заявку от пользователя {username}")
  public PeoplePage clickDeclineUser(String username) {
    peopleTable.row(username).clickDecline();
    return this;
  }

  @Step("Отменить исходящую заявку пользователю {username}")
  public PeoplePage clickCancelOutcome(String username) {
    peopleTable.row(username).clickCancel();
    return this;
  }

  @Step("Удалить из друзей пользователя {username}")
  public PeoplePage clickRemoveFriend(String username) {
    peopleTable.row(username).clickRemove();
    return this;
  }

  @Step("Проверить, что в таблице {expectedCount} пользователей")
  public PeoplePage checkThatUsersCount(int expectedCount) {
    peopleTable.checkThatUsersCount(expectedCount);
    return this;
  }

  @Step("Проверить пустой список пользователей")
  public PeoplePage checkThatNoUsersYet() {
    peopleTable.checkThatNoUsersYet();
    return this;
  }

  @Step("Проверить, что пользователь {username} отсутствует в таблице")
  public PeoplePage checkThatUserAbsent(String username) {
    peopleTable.checkThatUserAbsent(username);
    return this;
  }

  @Step("Проверить друга {username}: строка и кнопка Remove")
  public PeoplePage checkThatFriend(String username) {
    peopleTable.row(username).checkThatFriend();
    return this;
  }

  @Step("Проверить входящее invitation {username}: строка и кнопки Accept / Decline")
  public PeoplePage checkThatIncomeInvitation(String username) {
    peopleTable.row(username).checkThatIncomeInvitation();
    return this;
  }

  @Step("Проверить исходящее invitation {username}: строка, Waiting... и Cancel")
  public PeoplePage checkThatOutcomeInvitation(String username) {
    peopleTable.row(username).checkThatOutcomeInvitation();
    return this;
  }
}
