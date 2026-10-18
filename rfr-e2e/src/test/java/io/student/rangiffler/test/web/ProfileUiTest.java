package io.student.rangiffler.test.web;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.model.TestProfile;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.MyTravelsPage;
import io.student.rangiffler.page.ProfilePage;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * UI-тест заполнения профиля: данные вводятся на {@code /profile} и проверяются после сохранения.
 */
public class ProfileUiTest extends BaseUiTest {

  @Test
  @DisplayName("Пользователь может заполнить профиль и данные сохраняются")
  void shouldCreateAndPersistProfile(@User TestUser user) {
    TestProfile profile = RandomDataUtils.createProfile();

    Allure.step("Открыть лендинг и авторизоваться", () ->
        new LandingPage(page)
            .open()
            .clickLogin()
            .fillLoginForm(user.username(), user.password())
            .clickSignIn()
            .checkThatUrl(MyTravelsPage.URL)
    );

    Allure.step("Заполнить профиль, сохранить и проверить после reload", () ->
        new ProfilePage(page)
            .open()
            .fillProfileForm(profile.firstname(), profile.surname(), profile.country())
            .clickSave()
            .reload()
            .checkThatProfile(profile.firstname(), profile.surname(), profile.country())
            .checkThatAvatarIsSaved()
    );
  }
}
