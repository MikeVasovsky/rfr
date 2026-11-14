package io.student.rangiffler.page.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.student.rangiffler.utils.LocatorOptions.exactLocatorRole;
import static io.student.rangiffler.utils.LocatorOptions.exactLocatorText;

/**
 * Строка таблицы People: проверки и действия по статусу friendship.
 */
public final class PeopleRow {

  private final Locator root;

  public PeopleRow(Locator root) {
    this.root = root;
  }

  @Step("Проверить видимость строки пользователя")
  public PeopleRow checkThatVisible() {
    assertThat(root).isVisible();
    return this;
  }

  @Step("Проверить друга: кнопка Remove")
  public PeopleRow checkThatFriend() {
    assertThat(root).isVisible();
    assertThat(root.getByRole(AriaRole.BUTTON, exactLocatorRole("Remove"))).isVisible();
    return this;
  }

  @Step("Проверить входящее invitation: Accept и Decline")
  public PeopleRow checkThatIncomeInvitation() {
    assertThat(root).isVisible();
    assertThat(root.getByRole(AriaRole.BUTTON, exactLocatorRole("Accept"))).isVisible();
    assertThat(root.getByRole(AriaRole.BUTTON, exactLocatorRole("Decline"))).isVisible();
    return this;
  }

  @Step("Проверить исходящее invitation: Waiting... и Cancel")
  public PeopleRow checkThatOutcomeInvitation() {
    assertThat(root).isVisible();
    assertThat(root.getByText("Waiting...", exactLocatorText())).isVisible();
    assertThat(root.getByRole(AriaRole.BUTTON, exactLocatorRole("Cancel"))).isVisible();
    return this;
  }

  @Step("Нажать Remove")
  public PeopleRow clickRemove() {
    root.getByRole(AriaRole.BUTTON, exactLocatorRole("Remove")).click();
    return this;
  }

  @Step("Нажать Accept")
  public PeopleRow clickAccept() {
    root.getByRole(AriaRole.BUTTON, exactLocatorRole("Accept")).click();
    return this;
  }

  @Step("Нажать Decline")
  public PeopleRow clickDecline() {
    root.getByRole(AriaRole.BUTTON, exactLocatorRole("Decline")).click();
    return this;
  }

  @Step("Нажать Cancel")
  public PeopleRow clickCancel() {
    root.getByRole(AriaRole.BUTTON, exactLocatorRole("Cancel")).click();
    return this;
  }

  @Step("Нажать Add")
  public PeopleRow clickAdd() {
    root.getByRole(AriaRole.BUTTON, exactLocatorRole("Add")).click();
    return this;
  }
}
