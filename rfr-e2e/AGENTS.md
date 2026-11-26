## Запуск

```bash
./gradlew :rfr-e2e:installPlaywright
./gradlew runE2e
# эквивалент: ./gradlew :rfr-e2e:test

# весь стенд + e2e (Win/Mac), стек не гасится:
./gradlew e2eStack
```

`runE2e` всегда перезапускает suite (UP-TO-DATE отключён). Фильтры: `-DincludeTags` / `-DexcludeTags` — детали в `README.md`.

## Неочевидное

- Page Objects — skill `page-object-tests` (`.cursor/skills/page-object-tests/SKILL.md`).
- `domain/` — enum UI-текстов: `Notification` (snackbar), `PeopleAction` (кнопки строки People). Проверка уведомлений — только через `page/component/Snackbar` (страницы делегируют `checkThatNotification` / `checkThatProfileUpdated`).
- Вход только через UI; токены в `localStorage` не подставлять.
- Проход через Playwright MCP ≠ зелёный тест; успех — JUnit через Gradle (`README.md`, `allure-report.ps1`).
- Данные `@User TestUser`: `annotation/user` → `extension/user` → `data/fixture/user` (`UserRequest`, `SingleUserFixture`, декораторы), без широких `DELETE`/`TRUNCATE`.
- Dual UI: `@Session` + `SessionExtension` → второй `BrowserContext` на том же `Browser`, что у `BaseUiTest`. Handles: `session/UserSession` (после UI-login в fixture есть `MainPage`) или `session/GuestSession` (открытый лендинг). Фикстуры — `session/fixture` (`UserSessionFixture` / `GuestSessionFixture`). Контракт параметра: `@Session @User(…) UserSession` или `@Session GuestSession` (без `@User`). Пользователя для сессии отдаёт `UserExtension.user(context, index)`. Таймауты session-контекста — `BrowserTimeoutExtension.apply`. Образец тестов — `FriendshipActionsTest`.
- Пул `@StaticUser`: `annotation/pool` → `extension/pool` → `data/fixture/pool` (`UsersQueue`, `StaticUserFixture`; без create/delete в БД). Пакет `pool`, не `static` (keyword). Общий каркас: `data/fixture` (`UserFixture`, `TearDown`). Skill `rfr-test-data`.
- Тестовые username — префикс `rfr_`; после suite в `friendship`/`user` (auth+api) строк `rfr_%` быть не должно.
- EngineTestKit: мишени `jupiter/probe/user|pool|stand|allure/` (+ `ProbeReports`); оркестраторы `test/extension/user|pool|stand|allure/`. `@ProbeTarget` (`@Tag("probe")`) + exclude `probe`/`*Case` из suite.
- JDBC: plumbing `data.db.Databases` / `Sql`; разделы БД — небольшие immutable-классы (`AuthAccount`, `Profile`, `Friendship`, `Friendships`) с `create`/`exists`/`delete`, не DAO.
- Suite guards (ServiceLoader, порядок в `META-INF/services/...Extension`): `AllureEnvironmentExtension` (первая — `afterSuite` → `environment.properties`) → `StandReadinessExtension` (ждёт стенд перед `@Tag("ui")`) → `StandSchemaExtension` (Flyway). Каркас — `SuiteExtension`. Опрос стенда — `data/stand/StandReadiness` (`awaitFrom`); скрипт — `scripts/wait-for-stand.sh`. Выключить ожидание: `-Drfr.standCheck=false`; Flyway в мишенях: `-Drfr.schemaCheck=false`. Commit в отчёт: `-Drfr.commit` (Gradle).
- Теги: UI — `ui` (`BaseUiTest`) + `UI` (`@UiTest` на каждом UI-`*Test`); без стека — `fake` (в т.ч. EngineTestKit). Задача `:rfr-e2e:e2e` = тот же suite, что `test` (удобно `--tests '*TabTest'`). Новый тег сначала в README и в skill.
- Браузер UI: `BrowserLifecycleExtension` поднимает/гасит Playwright в `BeforeEach`/`AfterEach` через `BaseUiTest.startBrowser`/`stopBrowser` (чтобы timeout/trace видели контекст уже в `BeforeEachCallback`). Dual — `@Session` + `SessionExtension` (второй context + UI-login). Action-timeout — `BrowserTimeoutExtension.apply` / `TestConfig.defaultTimeoutMs`; параметр `Page` резолвится из `BaseUiTest`. `@UiTest` — мета-тег `UI` + `BrowserTraceExtension` (screenshots/snapshots/sources, вложение только при падении). `BrowserOptionsFactory` — для `@UsePlaywright`, не обязателен для текущего suite.
- Пакетов `api` / `assertions` нет — не заводить без явной нужды. Конфиг URL — `TestConfig` / `-Drfr.*` (см. README).
- Без комментариев к методам/полям (имена + `@Step`); над классом — краткий Javadoc. В Gradle/SQL/properties — без лишних комментариев.
