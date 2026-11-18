package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.HeaderComponent;
import io.student.rangiffler.page.component.PhotoCard;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.student.rangiffler.utils.LocatorOptions.exactLocatorRole;
import static io.student.rangiffler.utils.LocatorOptions.exactRole;

/**
 * Кабинет «My Travels» ({@code /my-travels}): лента фото, счётчик стран и фильтр по стране.
 */
public final class MyTravelsPage {

  public static final String URL = TestConfig.getInstance().frontUrl() + "/my-travels";

  private final Page page;
  private final HeaderComponent header;
  private final Locator travelsFilter;
  private final Locator photoCards;
  private final Locator countriesVisited;

  public MyTravelsPage(Page page) {
    this.page = page;
    this.header = new HeaderComponent(page);
    this.travelsFilter = page.getByRole(AriaRole.GROUP, exactRole("Travels filter"));
    this.photoCards = page.getByRole(AriaRole.ARTICLE);
    this.countriesVisited = page.getByRole(AriaRole.BUTTON)
        .filter(new Locator.FilterOptions().setHasText("visited"));
  }

  @Step("Открыть страницу My Travels")
  public MyTravelsPage open() {
    page.navigate(URL);
    return this;
  }

  @Step("Проверить URL: {expectedUrl}")
  public MyTravelsPage checkThatUrl(String expectedUrl) {
    assertThat(page).hasURL(expectedUrl);
    return this;
  }

  @Step("Проверить, что пользователь авторизован")
  public MyTravelsPage checkThatUserIsAuthorized() {
    header.checkThatLogoutIsVisible();
    return this;
  }

  @Step("Открыть профиль через шапку")
  public ProfilePage clickProfile() {
    return header.clickProfile();
  }

  @Step("Показать только свои путешествия")
  public MyTravelsPage clickOnlyMyTravels() {
    travelsFilter.getByRole(AriaRole.BUTTON, exactLocatorRole("Only my travels")).click();
    return this;
  }

  @Step("Показать путешествия с друзьями")
  public MyTravelsPage clickWithFriends() {
    travelsFilter.getByRole(AriaRole.BUTTON, exactLocatorRole("With friends")).click();
    return this;
  }

  @Step("Проверить, что на ленте {expectedCount} фото")
  public MyTravelsPage checkThatPhotoCount(int expectedCount) {
    assertThat(photoCards).hasCount(expectedCount);
    return this;
  }

  @Step("Карточка фото с описанием {description}")
  public PhotoCard photoCard(String description) {
    Locator card = photoCards.filter(new Locator.FilterOptions().setHasText(description));
    return new PhotoCard(card);
  }

  @Step("Проверить счётчик стран: {label}")
  public MyTravelsPage checkThatCountriesVisited(String label) {
    assertThat(countriesVisited).hasText(label);
    return this;
  }

  @Step("Открыть список посещённых стран")
  public MyTravelsPage clickCountriesVisited() {
    countriesVisited.click();
    return this;
  }

  @Step("Выбрать страну {countryName}")
  public MyTravelsPage selectCountry(String countryName) {
    page.getByRole(AriaRole.TOOLTIP)
        .getByRole(AriaRole.BUTTON, exactLocatorRole(countryName))
        .click();
    return this;
  }

  @Step("Проверить фильтр страны {countryName}")
  public MyTravelsPage checkThatCountryFilter(String countryName) {
    assertThat(countryFilterChip(countryName)).isVisible();
    return this;
  }

  @Step("Сбросить фильтр страны {countryName}")
  public MyTravelsPage clickResetCountryFilter(String countryName) {
    countryFilterChip(countryName)
        .getByRole(AriaRole.BUTTON, exactLocatorRole("Reset country filter"))
        .click();
    return this;
  }

  private Locator countryFilterChip(String countryName) {
    return page.getByRole(AriaRole.BUTTON, exactRole("Country filter: " + countryName));
  }
}
