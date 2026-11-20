## Summary

ДЗ 04: JUnit 6 — регистрация extension'ов и suite-guards в `rfr-e2e`.

- Каркас `SuiteExtension` (once `beforeSuite` / `afterSuite` через root Store)
- `StandReadinessExtension` — ожидание MySQL/auth/api/front перед `@Tag("ui")`
- `StandSchemaExtension` — сверка Flyway checkout ↔ локальные БД
- `AllureEnvironmentExtension` — `environment.properties` в Allure results
- Эксперименты задач 1–2 (`@BeforeSuite`, `LauncherSessionListener`) в истории + `git revert`
- EngineTestKit-мишени stand/allure; AI: test-reviewer + проба правила ServiceLoader

Подробности: [`hw04-pr-notes.md`](./hw04-pr-notes.md), вывод ревью: [`hw04-test-review-notes.md`](./hw04-test-review-notes.md).  
Скрин Environment: [`hw04-allure-environment.png`](./hw04-allure-environment.png).

## Definition of Done

- В `META-INF/services/...Extension`: Allure → StandReadiness → StandSchema; без `@ExtendWith` на suite-impl
- `e2e` не зависит от Gradle-`waitForStand`
- `:rfr-e2e:test` без `ui` не опрашивает стенд
- Попытки задач 1–2 в истории и откачены (`4f99097`/`daf7b38`, `6ddcdf9`/`75d836a`)
- `@ProbeTarget` не в основном suite (`probe` + `*Case`/`*Cases`)

## Таблица задачи 2

| | `SuiteExtension` | `@BeforeSuite` | `LauncherSessionListener` |
|---|---|---|---|
| Когда | `beforeAll` + root Store once | только suite-класс | `launcherSessionOpened` |
| IDE (один класс) | да | обычно нет | да |
| Store / теги | да | ограничено | нет |
| EngineTestKit | да | зависит | нет |
| parallel forks | раз на JVM | раз на JVM | раз на JVM |
| падение | BeforeAll / CTA | suite initError | часто Test Executor |

Почему остаётся `SuiteExtension`: нужны Store, тег `ui`, порядок с другими guards, IDE и EngineTestKit; падение остаётся на уровне Jupiter.

## Порядок SPI (обоснование)

1. `AllureEnvironmentExtension` — первая, чтобы `afterSuite`-хук попал в root Store до падения readiness/schema.
2. `StandReadinessExtension` — ждёт стенд (MySQL нужна и для Flyway).
3. `StandSchemaExtension` — сверка миграций.

## Вывод test-reviewer

Полный отчёт: [`hw04-test-review-notes.md`](./hw04-test-review-notes.md).

### Принятые замечания → закрытие

| Принятое | Закрытие |
|----------|----------|
| `@ExtendWith(StandReadinessExtension)` на probe (проба rule 13) | Снят; только SPI |
| Ready after delay / timeout / no ui / standCheck=false | `StandReadinessExtensionTest` |
| `environment.properties` со всеми ключами | `AllureEnvironmentExtensionTest` |
| Оркестраторы с `System.setProperty(rfr.*)` рядом с UI | `@Isolated` на stand/allure оркестраторах |
| Java `HttpClient` HTTP/2 зависает на Vite | `StandReadiness` → `.version(HTTP_1_1)` |

Финальное ревью suite-guards: **дефектов нет**.

## Проверки (локально)

- [x] `./gradlew :rfr-e2e:test` ×2 — OK
- [x] `./gradlew :rfr-e2e:e2e` ×2 — OK
- [x] Front down + `-Drfr.standTimeout=10` → `Still down: rfr-gql-client`
- [x] Красная: убрать poll → мишень «через 2 с» красная
- [x] Красная: Allure после Schema + сломанная схема → нет `environment.properties`; Allure первая + сломанная схема / MySQL down → файл есть (`unavailable` при MySQL down)
- [x] Скрин Environment в Allure

## Test plan

- [x] `./gradlew :rfr-e2e:test` ×2
- [x] `./gradlew :rfr-e2e:e2e` ×2
- [x] Front down timeout / red checks / Environment screenshot
