package io.student.rangiffler.page.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.student.rangiffler.utils.LocatorOptions.exactText;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Таблица пользователей на People: поиск строки, пустое состояние, количество.
 */
public final class PeopleTable {

  private final Page page;
  private final Locator tableRows;
  private final Locator emptyUsersMessage;

  public PeopleTable(Page page) {
    this.page = page;
    this.tableRows = page.getByTestId("people-row");
    this.emptyUsersMessage = page.getByText("There are no users yet", exactText());
  }

  @Step("Строка пользователя {username}")
  public PeopleRow row(String username) {
    Locator row = page.getByTestId("people-row")
        .filter(new Locator.FilterOptions()
            .setHas(page.getByText(username, exactText())));
    return new PeopleRow(row);
  }

  @Step("Проверить, что в таблице {expectedCount} пользователей")
  public PeopleTable checkThatUsersCount(int expectedCount) {
    assertThat(tableRows).hasCount(expectedCount);
    return this;
  }

  @Step("Проверить, что в таблице ровно пользователи {usernames}")
  public PeopleTable checkThatPeople(List<String> usernames) {
    assertAll(
        Stream.concat(
            Stream.<Executable>of(() -> assertThat(tableRows).hasCount(usernames.size())),
            usernames.stream().<Executable>map(username -> () -> assertThat(rowLocator(username)).isVisible())
        )
    );
    return this;
  }

  @Step("Проверить пустой список пользователей")
  public PeopleTable checkThatNoUsersYet() {
    assertThat(emptyUsersMessage).isVisible();
    return this;
  }

  @Step("Проверить, что на вкладке нет пользователей")
  public PeopleTable checkThatEmpty() {
    return checkThatNoUsersYet();
  }

  @Step("Проверить, что пользователь {username} отсутствует в таблице")
  public PeopleTable checkThatUserAbsent(String username) {
    assertThat(rowLocator(username)).hasCount(0);
    return this;
  }

  private Locator rowLocator(String username) {
    return page.getByTestId("people-row")
        .filter(new Locator.FilterOptions()
            .setHas(page.getByText(username, exactText())));
  }
}
