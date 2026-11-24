package io.student.rangiffler.page.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.domain.PeopleAction;

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
    assertThat(button(PeopleAction.REMOVE)).isVisible();
    return this;
  }

  @Step("Проверить входящее invitation: Accept и Decline")
  public PeopleRow checkThatIncomeInvitation() {
    assertThat(root).isVisible();
    assertThat(button(PeopleAction.ACCEPT)).isVisible();
    assertThat(button(PeopleAction.DECLINE)).isVisible();
    return this;
  }

  @Step("Проверить исходящее invitation: Waiting... и Cancel")
  public PeopleRow checkThatOutcomeInvitation() {
    assertThat(root).isVisible();
    assertThat(root.getByText("Waiting...", exactLocatorText())).isVisible();
    assertThat(button(PeopleAction.CANCEL)).isVisible();
    return this;
  }

  @Step("Нажать Remove")
  public PeopleRow clickRemove() {
    button(PeopleAction.REMOVE).click();
    return this;
  }

  @Step("Нажать Accept")
  public PeopleRow clickAccept() {
    button(PeopleAction.ACCEPT).click();
    return this;
  }

  @Step("Нажать Decline")
  public PeopleRow clickDecline() {
    button(PeopleAction.DECLINE).click();
    return this;
  }

  @Step("Нажать Cancel")
  public PeopleRow clickCancel() {
    button(PeopleAction.CANCEL).click();
    return this;
  }

  @Step("Нажать Add")
  public PeopleRow clickAdd() {
    button(PeopleAction.ADD).click();
    return this;
  }

  private Locator button(PeopleAction action) {
    return root.getByRole(AriaRole.BUTTON, exactLocatorRole(action.title()));
  }
}
