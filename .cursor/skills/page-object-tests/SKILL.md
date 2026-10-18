---
name: page-object-tests
description: >-
  Develops UI tests with Page Object (Playwright Java, JUnit, Allure) for Rangiffler e2e.
  Use when creating or changing Page Objects, components, UI tests in rfr-e2e,
  or when the user asks how to structure Page Object tests.
---

# Page Object tests (rfr-e2e)

## Rules

1. Класс `final`, поля `private final`: `Page`, `Locator`, компоненты. Исключение — `public static final String URL` с базой из `TestConfig`; он есть у каждой страницы, даже если на неё попадают только переходом.

2. Конструктор принимает только `Page` и только присваивает поля: без переходов, кликов, проверок, ожиданий и SQL.

3. Композиция вместо наследования: общие части (`CredentialsForm`, `HeaderComponent`) — компоненты с тем же `Page`, базовых классов нет.

4. Каждый публичный метод страницы и компонента — с `@Step`, и каждый параметр попадает в текст шага: `@Step("Заполнить форму входа: {username} / {password}")`.

5. Переход возвращает объект следующей страницы, действие и проверка на текущей — `this`.

6. Одно бизнес-действие — один метод, имя которого называет форму: это пример необязательно называть так, называй по логике действия на странице `fillLoginForm(username, password)`, `fillRegistrationForm(...)`. Без безликих `fillForm`/`fill`/`submit` и цепочек `fillUsername().fillPassword()`; отдельный метод на поле — только если тесту нужно именно поле (валидация).

7. Действие отдельно от проверки: `fill…`/`click…` ничего не проверяют; `checkThat…` только проверяют через `PlaywrightAssertions.assertThat(locator)` и ничего не кликают. Без `locator.isVisible()` в JUnit-проверках и ручных ожиданий.

8. Без `checkThatOpened`, `checkPageLoaded`, `isLoaded`, `waitUntilLoaded`: `click`/`fill` ждут элемент, web-first assertions ждут состояние. URL и видимость страницы проверяем, только когда переход и есть предмет теста.

9. Локаторы: `getByRole` с точным `name`, `getByLabel`, `getByPlaceholder`, `getByText` с точным совпадением, `getByTestId`. Без `first()`/`nth()`, MUI-классов и XPath; неуникальный элемент сужай родителем: `page.getByRole(AriaRole.BANNER).getByRole(…)`.

10. Проверка не должна быть тавтологией локатора: `assertThat(heading("Travels map")).hasText("Travels map")` ничего не доказывает. Ожидаемое значение приходит из теста (`checkThatUsername(username)`).

11. Вход только через UI; `access_token`/`id_token` в `localStorage` не подставлять (до занятия 13).

12. Без комментариев к методам и полям: смысл методов передают имена и текст `@Step`. Над каждым классом — краткий Javadoc: за что отвечает класс.

## Where code lives

- Config: `rfr-e2e/.../config/TestConfig.java`
- Pages: `rfr-e2e/.../page/`
- Components: `rfr-e2e/.../page/component/`
- UI tests: `rfr-e2e/.../test/web/`
- Browser lifecycle: `BaseUiTest`
