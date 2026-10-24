package io.student.rangiffler.test.profile;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.MyTravelsPage;
import io.student.rangiffler.page.ProfilePage;
import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Позитивные UI-сценарии профиля ({@code /profile}) на пользователе из {@link User}:
 * сохранение полей, Reset к сохранённому, детерминированная смена страны.
 */
public class ProfileTest extends BaseUiTest {

  private static final String DEFAULT_LOCATION = "Russian Federation";
  private static final String COUNTRY_CANADA = "Canada";
  private static final String COUNTRY_FRANCE = "France";
  private static final String FIRST_NAME = "Alice";
  private static final String SURNAME = "Smith";
  private static final String DRAFT_FIRST_NAME = "Bob";
  private static final String DRAFT_SURNAME = "Jones";

  @Test
  @DisplayName("First Name сохраняется и читается после reload")
  void shouldPersistFirstNameAfterReload(@User TestUser user) {
    login(user);

    Allure.step("Сохранить только First Name и проверить после reload", () ->
        new ProfilePage(page)
            .open()
            .fillFirstName(FIRST_NAME)
            .clickSave()
            .reload()
            .checkThatProfile(FIRST_NAME, "", DEFAULT_LOCATION)
    );
  }

  @Test
  @DisplayName("Surname сохраняется и читается после reload")
  void shouldPersistSurnameAfterReload(@User TestUser user) {
    login(user);

    Allure.step("Сохранить только Surname и проверить после reload", () ->
        new ProfilePage(page)
            .open()
            .fillSurname(SURNAME)
            .clickSave()
            .reload()
            .checkThatProfile("", SURNAME, DEFAULT_LOCATION)
    );
  }

  @Test
  @DisplayName("Location сохраняется и читается после reload")
  void shouldPersistLocationAfterReload(@User TestUser user) {
    login(user);

    Allure.step("Сохранить Location и проверить после reload", () ->
        new ProfilePage(page)
            .open()
            .selectLocation(COUNTRY_CANADA)
            .clickSave()
            .reload()
            .checkThatProfile("", "", COUNTRY_CANADA)
    );
  }

  @Test
  @DisplayName("Avatar сохраняется и читается после reload")
  void shouldPersistAvatarAfterReload(@User TestUser user) {
    login(user);

    Allure.step("Сохранить только avatar и проверить после reload", () ->
        new ProfilePage(page)
            .open()
            .uploadAvatar()
            .clickSave()
            .reload()
            .checkThatAvatarIsSaved()
    );
  }

  @Test
  @DisplayName("Reset возвращает сохранённые значения профиля")
  void shouldResetToSavedProfileValues(@User TestUser user) {
    login(user);

    Allure.step("Сохранить профиль", () ->
        new ProfilePage(page)
            .open()
            .fillFirstName(FIRST_NAME)
            .fillSurname(SURNAME)
            .selectLocation(COUNTRY_CANADA)
            .clickSave()
            .reload()
            .checkThatProfile(FIRST_NAME, SURNAME, COUNTRY_CANADA)
    );

    Allure.step("Изменить поля и откатить Reset к сохранённым", () ->
        new ProfilePage(page)
            .fillFirstName(DRAFT_FIRST_NAME)
            .fillSurname(DRAFT_SURNAME)
            .selectLocation(COUNTRY_FRANCE)
            .clickReset()
            .checkThatProfile(FIRST_NAME, SURNAME, COUNTRY_CANADA)
    );
  }

  @Test
  @DisplayName("Смена страны сохраняется детерминированно")
  void shouldChangeCountryDeterministically(@User TestUser user) {
    login(user);

    Allure.step("Сохранить Location = Canada", () ->
        new ProfilePage(page)
            .open()
            .selectLocation(COUNTRY_CANADA)
            .clickSave()
            .reload()
            .checkThatProfile("", "", COUNTRY_CANADA)
    );

    Allure.step("Сменить Location на France и проверить после reload", () ->
        new ProfilePage(page)
            .selectLocation(COUNTRY_FRANCE)
            .clickSave()
            .reload()
            .checkThatProfile("", "", COUNTRY_FRANCE)
    );
  }

  private void login(TestUser user) {
    Allure.step("Открыть лендинг и авторизоваться", () ->
        new LandingPage(page)
            .open()
            .clickLogin()
            .fillLoginForm(user.username(), user.password())
            .clickSignIn()
            .checkThatUrl(MyTravelsPage.URL)
    );
  }
}
