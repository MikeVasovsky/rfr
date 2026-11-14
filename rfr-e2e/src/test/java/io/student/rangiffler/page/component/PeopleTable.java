package io.student.rangiffler.page.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.student.rangiffler.utils.LocatorOptions.exactText;

/**
 * Таблица пользователей на People: поиск строки, пустое состояние, количество.
 */
public final class PeopleTable {

  private final Page page;
  private final Locator tableRows;
  private final Locator emptyUsersMessage;

  public PeopleTable(Page page) {
    this.page = page;
    this.tableRows = page.getByRole(AriaRole.ROW);
    this.emptyUsersMessage = page.getByText("There are no users yet", exactText());
  }

  @Step("Строка пользователя {username}")
  public PeopleRow row(String username) {
    Locator row = page.getByRole(AriaRole.ROW)
        .filter(new Locator.FilterOptions()
            .setHas(page.getByText(username, exactText())));
    return new PeopleRow(row);
  }

  @Step("Проверить, что в таблице {expectedCount} пользователей")
  public PeopleTable checkThatUsersCount(int expectedCount) {
    assertThat(tableRows).hasCount(expectedCount + 1);
    return this;
  }

  @Step("Проверить пустой список пользователей")
  public PeopleTable checkThatNoUsersYet() {
    assertThat(emptyUsersMessage).isVisible();
    return this;
  }

  @Step("Проверить, что пользователь {username} отсутствует в таблице")
  public PeopleTable checkThatUserAbsent(String username) {
    assertThat(rowLocator(username)).hasCount(0);
    return this;
  }

  private Locator rowLocator(String username) {
    return page.getByRole(AriaRole.ROW)
        .filter(new Locator.FilterOptions()
            .setHas(page.getByText(username, exactText())));
  }
}
