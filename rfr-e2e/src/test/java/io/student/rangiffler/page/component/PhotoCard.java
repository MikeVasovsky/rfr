package io.student.rangiffler.page.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.student.rangiffler.utils.LocatorOptions.exactLocatorRole;
import static io.student.rangiffler.utils.LocatorOptions.exactRole;

/**
 * Карточка фото на My Travels: описание, like и меню своих фото внутри одного {@code article}.
 */
public final class PhotoCard {

  private final Locator root;

  public PhotoCard(Locator root) {
    this.root = root;
  }

  @Step("Проверить видимость карточки фото")
  public PhotoCard checkThatVisible() {
    assertThat(root).isVisible();
    return this;
  }

  @Step("Проверить кнопку like")
  public PhotoCard checkThatLikeVisible() {
    assertThat(likeButton()).isVisible();
    return this;
  }

  @Step("Проверить кнопку Photo actions")
  public PhotoCard checkThatPhotoActionsVisible() {
    assertThat(photoActionsButton()).isVisible();
    return this;
  }

  @Step("Проверить, что меню Photo actions отсутствует")
  public PhotoCard checkThatPhotoActionsAbsent() {
    assertThat(photoActionsButton()).hasCount(0);
    return this;
  }

  private Locator likeButton() {
    return root.page().getByRole(AriaRole.BUTTON, exactRole("like"));
  }

  private Locator photoActionsButton() {
    return root.getByRole(AriaRole.BUTTON, exactLocatorRole("Photo actions"));
  }
}
