package io.student.rangiffler.test.profile;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.model.TestProfile;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.MyTravelsPage;
import io.student.rangiffler.page.ProfilePage;
import io.student.rangiffler.test.BaseUiTest;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Негативные UI-сценарии страницы {@code /profile}: валидация, read-only username, Reset, доступ без сессии.
 */
public class ProfileNegativeTest extends BaseUiTest {

  private static final String DEFAULT_LOCATION = "Russian Federation";
  private static final String FIRST_NAME_TOO_LONG_ERROR =
      "First name length has to be not longer that 50 symbols";
  private static final String SURNAME_TOO_LONG_ERROR =
      "Surname length has to be not longer that 100 symbols";

  @Test
  @DisplayName("Слишком длинное First Name не сохраняется")
  void shouldShowErrorWhenFirstNameTooLong(@User TestUser user) {
    login(user);

    Allure.step("Ввести First Name длиннее 50 символов и нажать Save", () ->
        new ProfilePage(page)
            .open()
            .fillFirstName("a".repeat(51))
            .clickSave()
            .checkThatFirstNameError(FIRST_NAME_TOO_LONG_ERROR)
    );
  }

  @Test
  @DisplayName("Слишком длинное Surname не сохраняется")
  void shouldShowErrorWhenSurnameTooLong(@User TestUser user) {
    login(user);

    Allure.step("Ввести Surname длиннее 100 символов и нажать Save", () ->
        new ProfilePage(page)
            .open()
            .fillSurname("b".repeat(101))
            .clickSave()
            .checkThatSurnameError(SURNAME_TOO_LONG_ERROR)
    );
  }

  @Test
  @DisplayName("Username на профиле только для чтения")
  void shouldKeepUsernameReadOnly(@User TestUser user) {
    login(user);

    Allure.step("Проверить, что Username disabled и равен логину", () ->
        new ProfilePage(page)
            .open()
            .checkThatUsername(user.username())
    );
  }

  @Test
  @DisplayName("Reset откатывает несохранённые правки профиля")
  void shouldResetUnsavedProfileChanges(@User TestUser user) {
    TestProfile draft = RandomDataUtils.createProfile();
    login(user);

    Allure.step("Изменить профиль и откатить через Reset", () ->
        new ProfilePage(page)
            .open()
            .checkThatProfile("", "", DEFAULT_LOCATION)
            .fillFirstName(draft.firstname())
            .fillSurname(draft.surname())
            .selectLocation(draft.country())
            .clickReset()
            .checkThatProfile("", "", DEFAULT_LOCATION)
    );
  }

  @Test
  @DisplayName("Гость с /profile редиректится на лендинг")
  void shouldRedirectGuestFromProfileToLanding() {
    Allure.step("Открыть /profile без авторизации и проверить лендинг", () ->
        new ProfilePage(page)
            .openUnauthorized()
            .checkThatUrl(LandingPage.URL)
            .checkThatLoginIsVisible()
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
