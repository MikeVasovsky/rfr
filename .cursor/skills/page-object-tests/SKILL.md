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

9. Локаторы: `getByRole` / `getByLabel` / `getByPlaceholder` / `getByText` / `getByTestId`. Без `first()`/`nth()`, MUI-классов и XPath; неуникальный элемент сужай родителем: `page.getByRole(AriaRole.BANNER).getByRole(…)`.

10. Поля ввода (`getByLabel`, и при необходимости `getByPlaceholder` / `getByRole` для combobox) — **только с полным совпадением** (`exact = true`). Кнопки, ссылки, текст — тоже с `exact`, если ищете по `name`/`text`.

11. Не раздувать аргументы локаторов: повторяющиеся `new Page.GetBy*Options().setExact(true)` выносить в `LocatorOptions` (`exactLabel()`, `exactRole(name)`, `exactLocatorRole(name)`, `exactText()`) и подключать static import. То же для любых часто создаваемых Options/фильтров в аргументах — отдельный метод ради читаемости.

12. Проверка не должна быть тавтологией локатора: `assertThat(heading("Travels map")).hasText("Travels map")` ничего не доказывает. Ожидаемое значение приходит из теста (`checkThatUsername(username)`).

13. Вход только через UI; `access_token`/`id_token` в `localStorage` не подставлять (до занятия 13).

14. Без комментариев к методам и полям: смысл методов передают имена и текст `@Step`. Над каждым классом — краткий Javadoc: за что отвечает класс.

15. Группы тестов — через JUnit `@Tag` (не свои аннотации). UI-тесты наследуют `@Tag("web")` с `BaseUiTest`; не-UI заглушки — `@Tag("fake")`. Новый класс вне иерархии `BaseUiTest` помечай тегом явно. Запуск группы: `-DincludeTags=web` или `.\allure-report.ps1 -Tag web`. Допустимые теги и команды — в `rfr-e2e/README.md`; новый тег сначала добавь в README и в этот пункт skill.

## Where code lives

- Config: `rfr-e2e/.../config/TestConfig.java`
- Pages: `rfr-e2e/.../page/`
- Components: `rfr-e2e/.../page/component/`
- Locator helpers: `rfr-e2e/.../utils/LocatorOptions.java`
- UI tests: `rfr-e2e/.../test/web/`
- Browser lifecycle: `BaseUiTest` (`@Tag("web")`)
- Run / Allure: `rfr-e2e/README.md`, `rfr-e2e/allure-report.ps1`
