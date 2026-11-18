package io.student.rangiffler.test.travels;

import io.qameta.allure.Allure;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.user.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.MyTravelsPage;
import io.student.rangiffler.test.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * UI-проверки mock-ленты My Travels: переключатель, фильтр страны и меню своих фото.
 * Лента не сохраняется, поэтому тест смотрит только на экран.
 */
@UiTest
public class MyTravelsMockTest extends BaseUiTest {

  private static final int ONLY_MY_PHOTOS = 4;
  private static final int WITH_FRIENDS_PHOTOS = 7;
  private static final String ONLY_MY_COUNTRIES = "4 countries visited";
  private static final String WITH_FRIENDS_COUNTRIES = "7 countries visited";
  private static final String FILTER_COUNTRY = "France";
  private static final String FILTER_DESCRIPTION = "Париж!";

  private static final String[] OWN_DESCRIPTIONS = {
      "Амстердам!",
      "Париж!",
      "Петербург!",
      "Нью-Йорк!"
  };

  private static final String[] FRIEND_DESCRIPTIONS = {
      "Старая Прага завораживает!",
      "Мюнхен - мой любимый город!",
      "Обожаю Таиланд!"
  };

  @Test
  @DisplayName("На mock-ленте переключатель меняет число карточек и счётчик стран")
  void shouldChangeFeedWhenTogglingFriends(@User TestUser user) {
    login(user);

    Allure.step("Only my travels: 4 фото и 4 страны", () ->
        new MyTravelsPage(page)
            .checkThatPhotoCount(ONLY_MY_PHOTOS)
            .checkThatCountriesVisited(ONLY_MY_COUNTRIES)
    );

    Allure.step("With friends: 7 фото и 7 стран", () ->
        new MyTravelsPage(page)
            .clickWithFriends()
            .checkThatPhotoCount(WITH_FRIENDS_PHOTOS)
            .checkThatCountriesVisited(WITH_FRIENDS_COUNTRIES)
    );

    Allure.step("Only my travels возвращает 4 фото и 4 страны", () ->
        new MyTravelsPage(page)
            .clickOnlyMyTravels()
            .checkThatPhotoCount(ONLY_MY_PHOTOS)
            .checkThatCountriesVisited(ONLY_MY_COUNTRIES)
    );
  }

  @Test
  @DisplayName("На mock-ленте фильтр по стране показывает чип и одну карточку")
  void shouldFilterFeedByCountry(@User TestUser user) {
    login(user);

    Allure.step("До фильтра на ленте 4 фото", () ->
        new MyTravelsPage(page)
            .checkThatPhotoCount(ONLY_MY_PHOTOS)
    );

    Allure.step("France: чип фильтра и одна карточка Париж", () ->
        new MyTravelsPage(page)
            .clickCountriesVisited()
            .selectCountry(FILTER_COUNTRY)
            .checkThatCountryFilter(FILTER_COUNTRY)
            .checkThatPhotoCount(1)
            .photoCard(FILTER_DESCRIPTION)
            .checkThatVisible()
            .checkThatLikeVisible()
    );

    Allure.step("Сброс фильтра возвращает 4 фото", () ->
        new MyTravelsPage(page)
            .clickResetCountryFilter(FILTER_COUNTRY)
            .checkThatPhotoCount(ONLY_MY_PHOTOS)
    );
  }

  @Test
  @DisplayName("На mock-ленте меню Photo actions есть только у своих фото")
  void shouldShowPhotoActionsOnlyOnOwnPhotos(@User TestUser user) {
    login(user);

    Allure.step("With friends показывает свои и чужие фото", () ->
        new MyTravelsPage(page)
            .clickWithFriends()
            .checkThatPhotoCount(WITH_FRIENDS_PHOTOS)
    );

    Allure.step("У своих фото есть Photo actions", () -> {
      MyTravelsPage travels = new MyTravelsPage(page);
      for (String description : OWN_DESCRIPTIONS) {
        travels.photoCard(description).checkThatPhotoActionsVisible();
      }
    });

    Allure.step("У фото друзей меню Photo actions нет", () -> {
      MyTravelsPage travels = new MyTravelsPage(page);
      for (String description : FRIEND_DESCRIPTIONS) {
        travels.photoCard(description).checkThatPhotoActionsAbsent();
      }
    });
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
