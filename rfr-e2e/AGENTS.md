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
- Данные `@User`: `annotation/user` → `extension/user` → `data/fixture/user` (`UserRequest`, `SingleUserFixture`, декораторы), без широких `DELETE`/`TRUNCATE`.
- Пул `@StaticUser`: `annotation/pool` → `extension/pool` → `data/fixture/pool` (`UsersQueue`, `StaticUserFixture`; без create/delete в БД). Пакет `pool`, не `static` (keyword). Общий каркас: `data/fixture` (`UserFixture`, `TearDown`). Skill `rfr-test-data`.
- Тестовые username — префикс `rfr_`; после suite в `friendship`/`user` (auth+api) строк `rfr_%` быть не должно.
- EngineTestKit: мишени `jupiter/probe/user|pool|stand|allure/` (+ `ProbeReports`); оркестраторы `test/extension/user|pool|stand|allure/`. `@ProbeTarget` (`@Tag("probe")`) + exclude `probe`/`*Case` из suite.
- JDBC: plumbing `data.db.Databases` / `Sql`; разделы БД — небольшие immutable-классы (`AuthAccount`, `Profile`, `Friendship`, `Friendships`) с `create`/`exists`/`delete`, не DAO.
- Suite guards (ServiceLoader, порядок в `META-INF/services/...Extension`): `AllureEnvironmentExtension` (первая — `afterSuite` → `environment.properties`) → `StandReadinessExtension` (ждёт стенд перед `@Tag("ui")`) → `StandSchemaExtension` (Flyway). Каркас — `SuiteExtension`. Опрос стенда — `data/stand/StandReadiness` (`awaitFrom`); скрипт — `scripts/wait-for-stand.sh`. Выключить ожидание: `-Drfr.standCheck=false`; Flyway в мишенях: `-Drfr.schemaCheck=false`. Commit в отчёт: `-Drfr.commit` (Gradle).
- Теги: UI — `ui` (`BaseUiTest`), без стека — `fake` (в т.ч. EngineTestKit). Задача `:rfr-e2e:e2e` = тот же suite, что `test` (удобно `--tests '*TabTest'`). Новый тег сначала в README и в skill.
- Пакетов `api` / `assertions` нет — не заводить без явной нужды. Конфиг URL — `TestConfig` / `-Drfr.*` (см. README).
- Без комментариев к методам/полям (имена + `@Step`); над классом — краткий Javadoc. В Gradle/SQL/properties — без лишних комментариев.
