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
- Вход только через UI; токены в `localStorage` не подставлять.
- Проход через Playwright MCP ≠ зелёный тест; успех — JUnit через Gradle (`README.md`, `allure-report.ps1`).
- Данные `@User TestUser`: `annotation/user` → `extension/user` → `data/fixture/user` (`UserRequest`, `SingleUserFixture`, декораторы), без широких `DELETE`/`TRUNCATE`.
- Dual UI `@Session UserSession`: meta `@MultiSessionUiTest` → `annotation/session` + `extension/session` (`SessionExtension`) → `data/fixture/session` (`BrowserRuntime`, `UserSessionFixture` / `GuestSessionFixture` via `SessionFixture`) + `UserRequest` для non-guest; handle `model/UserSession` (только фабрики PO: `landingPage` / `peoplePage`; login/register — в тесте через PO). `@Session(guest = true)` — credentials без create в БД. Фикстуры session без UI-шагов. Дизайн: `docs/e2e-user-session/ARCHITECTURE.md`. `@User` — только для `TestUser`.
- Пул `@StaticUser`: `annotation/pool` → `extension/pool` → `data/fixture/pool` (`UsersQueue`, `StaticUserFixture`; без create/delete в БД). Пакет `pool`, не `static` (keyword). Общий каркас: `data/fixture` (`UserFixture`, `TearDown`). Skill `rfr-test-data`.
- Тестовые username — префикс `rfr_`; после suite в `friendship`/`user` (auth+api) строк `rfr_%` быть не должно.
- EngineTestKit: мишени `jupiter/probe/user|pool|stand|allure/` (+ `ProbeReports`); оркестраторы `test/extension/user|pool|stand|allure/`. `@ProbeTarget` (`@Tag("probe")`) + exclude `probe`/`*Case` из suite.
- JDBC: plumbing `data.db.Databases` / `Sql`; разделы БД — небольшие immutable-классы (`AuthAccount`, `Profile`, `Friendship`, `Friendships`) с `create`/`exists`/`delete`, не DAO.
- Suite guards (ServiceLoader, порядок в `META-INF/services/...Extension`): `AllureEnvironmentExtension` (первая — `afterSuite` → `environment.properties`) → `StandReadinessExtension` (ждёт стенд перед `@Tag("ui")`) → `StandSchemaExtension` (Flyway). Каркас — `SuiteExtension`. Опрос стенда — `data/stand/StandReadiness` (`awaitFrom`); скрипт — `scripts/wait-for-stand.sh`. Выключить ожидание: `-Drfr.standCheck=false`; Flyway в мишенях: `-Drfr.schemaCheck=false`. Commit в отчёт: `-Drfr.commit` (Gradle).
- Теги: UI — `ui` (`BaseUiTest`) + `UI` (`@UiTest` на каждом UI-`*Test`); без стека — `fake` (в т.ч. EngineTestKit). Задача `:rfr-e2e:e2e` = тот же suite, что `test` (удобно `--tests '*TabTest'`). Новый тег сначала в README и в skill.
- Браузер UI: `BaseUiTest` (`Playwright.create` / launch / `page`). Dual — `@MultiSessionUiTest` + `SessionExtension` (timeout на `newPage()`). Action-timeout одиночных — `BrowserTimeoutExtension` (`TestConfig.defaultTimeoutMs`). `@UiTest` — мета-тег `UI`. `BrowserOptionsFactory` — для `@UsePlaywright`, не обязателен для текущего suite.
- Пакетов `api` / `assertions` нет — не заводить без явной нужды. Конфиг URL — `TestConfig` / `-Drfr.*` (см. README).
- Без комментариев к методам/полям (имена + `@Step`); над классом — краткий Javadoc. В Gradle/SQL/properties — без лишних комментариев.
