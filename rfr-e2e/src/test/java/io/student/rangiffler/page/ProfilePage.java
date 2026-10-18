package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.student.rangiffler.utils.LocatorOptions.exactLabel;
import static io.student.rangiffler.utils.LocatorOptions.exactRole;
import static io.student.rangiffler.utils.LocatorOptions.exactText;

/**
 * Страница профиля ({@code /profile}): заполнение firstname/surname/location/avatar и сохранение.
 */
public final class ProfilePage {

  public static final String URL = TestConfig.getInstance().frontUrl() + "/profile";

  private static final Path AVATAR_PATH = avatarPath();

  private final Page page;
  private final Locator firstNameInput;
  private final Locator surnameInput;
  private final Locator usernameInput;
  private final Locator locationCombobox;
  private final Locator avatarInput;
  private final Locator saveButton;
  private final Locator resetButton;
  private final Locator avatarImage;

  public ProfilePage(Page page) {
    this.page = page;
    this.firstNameInput = page.getByLabel("First Name", exactLabel());
    this.surnameInput = page.getByLabel("Surname", exactLabel());
    this.usernameInput = page.getByLabel("Username", exactLabel());
    this.locationCombobox = page.getByRole(AriaRole.COMBOBOX, exactRole("Location"));
    this.avatarInput = page.getByLabel("Update avatar", exactLabel());
    this.saveButton = page.getByRole(AriaRole.BUTTON, exactRole("Save"));
    this.resetButton = page.getByRole(AriaRole.BUTTON, exactRole("Reset"));
    this.avatarImage = page.getByRole(AriaRole.MAIN).locator("img[src^='data:im']:not([alt])");
  }

  @Step("Открыть страницу профиля")
  public ProfilePage open() {
    page.navigate(URL);
    return this;
  }

  @Step("Открыть /profile без сессии")
  public LandingPage openUnauthorized() {
    page.navigate(URL);
    return new LandingPage(page);
  }

  @Step("Заполнить First Name: {firstname}")
  public ProfilePage fillFirstName(String firstname) {
    firstNameInput.fill(firstname);
    return this;
  }

  @Step("Заполнить Surname: {surname}")
  public ProfilePage fillSurname(String surname) {
    surnameInput.fill(surname);
    return this;
  }

  @Step("Выбрать Location: {country}")
  public ProfilePage selectLocation(String country) {
    locationCombobox.click();
    page.getByRole(AriaRole.OPTION)
        .filter(new Locator.FilterOptions()
            .setHas(page.getByRole(AriaRole.IMG, exactRole(country))))
        .click();
    return this;
  }

  @Step("Загрузить аватар")
  public ProfilePage uploadAvatar() {
    avatarInput.setInputFiles(AVATAR_PATH);
    return this;
  }

  @Step("Заполнить профиль: {firstname} / {surname} / {country}")
  public ProfilePage fillProfileForm(String firstname, String surname, String country) {
    fillFirstName(firstname);
    fillSurname(surname);
    selectLocation(country);
    uploadAvatar();
    return this;
  }

  @Step("Нажать Save")
  public ProfilePage clickSave() {
    saveButton.click();
    return this;
  }

  @Step("Нажать Save и дождаться UpdateUser")
  public ProfilePage clickSaveAndWaitForUpdate() {
    page.waitForResponse(
        response -> {
          String postData = response.request().postData();
          return response.url().contains("/graphql")
              && postData != null
              && postData.contains("UpdateUser");
        },
        saveButton::click
    );
    return this;
  }

  @Step("Нажать Reset")
  public ProfilePage clickReset() {
    resetButton.click();
    return this;
  }

  @Step("Перезагрузить страницу")
  public ProfilePage reload() {
    page.reload();
    return this;
  }

  @Step("Проверить профиль: {firstname} / {surname} / {country}")
  public ProfilePage checkThatProfile(String firstname, String surname, String country) {
    assertThat(firstNameInput).hasValue(firstname);
    assertThat(surnameInput).hasValue(surname);
    assertThat(locationCombobox).containsText(country);
    return this;
  }

  @Step("Проверить ошибку First Name: {expectedMessage}")
  public ProfilePage checkThatFirstNameError(String expectedMessage) {
    assertThat(page.getByText(expectedMessage, exactText())).isVisible();
    return this;
  }

  @Step("Проверить ошибку Surname: {expectedMessage}")
  public ProfilePage checkThatSurnameError(String expectedMessage) {
    assertThat(page.getByText(expectedMessage, exactText())).isVisible();
    return this;
  }

  @Step("Проверить Username (disabled): {username}")
  public ProfilePage checkThatUsername(String username) {
    assertThat(usernameInput).isDisabled();
    assertThat(usernameInput).hasValue(username);
    return this;
  }

  @Step("Проверить, что аватар сохранён (data:im…)")
  public ProfilePage checkThatAvatarIsSaved() {
    assertThat(avatarImage).isVisible();
    assertThat(avatarImage).hasAttribute("src", Pattern.compile("^data:im.*"));
    return this;
  }

  private static Path avatarPath() {
    try {
      return Path.of(Objects.requireNonNull(
          ProfilePage.class.getClassLoader().getResource("img/avatar.png"),
          "Classpath resource img/avatar.png not found"
      ).toURI());
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Cannot resolve img/avatar.png", e);
    }
  }
}
