# HW04 — материалы для PR

## Таблица задачи 2: `SuiteExtension` / `@BeforeSuite` / `LauncherSessionListener`

| | `SuiteExtension` (ServiceLoader) | `@BeforeSuite` + `@Suite` | `LauncherSessionListener` |
|---|---|---|---|
| Когда срабатывает | `beforeAll` первого класса; once через root Store | Только при запуске suite-класса | `launcherSessionOpened` на worker JVM |
| Запуск одного класса/метода из IDE | Да (autodetection) | Обычно нет | Да (на сессию процесса) |
| `./gradlew e2e --tests '*TabTest'` | Да (Jupiter + SPI) | Зависит от entrypoint suite | Да |
| Знает `Store` / теги | Да (`ExtensionContext`) | Ограниченно | Нет |
| `EngineTestKit` | Да (при autodetection) | Зависит от selector | Нет (engine без новой LauncherSession) |
| `maxParallelForks = 2` | По разу на fork | По разу на fork | По разу на fork (разные pid) |
| Как падает | Extension / BeforeAll failure, CTA в стеке | Suite `initializationError` | Часто «Test Executor» / до test-case |

Почему остаётся `SuiteExtension`: нужны Store, теги (`ui`), предсказуемый порядок с другими guards, работа из IDE и внутри EngineTestKit; падение остаётся на уровне Jupiter, а не «ломает executor».

Эксперименты в истории: `4f99097` + revert `daf7b38`; `6ddcdf9` + revert `75d836a`.

## Порядок в `META-INF/services/...Extension`

1. `AllureEnvironmentExtension` — первая, чтобы `afterSuite`-хук попал в root Store **до** возможного падения readiness/schema.
2. `StandReadinessExtension` — ждёт стенд перед `@Tag("ui")` (MySQL нужна и для Flyway).
3. `StandSchemaExtension` — сверка миграций.

Порядок строк SPI = порядок `beforeAll` при autodetection. Ни одна suite-impl не подключена через `@ExtendWith`.

## DoD

- `e2e` не зависит от Gradle-`waitForStand`; wait внутри JUnit (`StandReadiness`).
- `:rfr-e2e:test` без `@Tag("ui")` не опрашивает стенд (`shouldRunBeforeSuite` no-op; мишени `NoUiTagCase` / `StandCheckDisabledCase`).
- `@ProbeTarget` исключён тегом `probe` + фильтром `*Case`/`*Cases`.

## Красные проверки (ручные) — выполнены 2026-11-12

1. Убрать повтор опроса в `StandReadiness.await` → мишень «сервис начал отвечать через 2 с» **красная** (восстановлено).
2. Allure после Schema + broken schema → `environment.properties` **нет**; Allure первая + broken schema → файл **есть**; MySQL down → файл с `rangiffler-*=unavailable`.

## DoD прогоны — выполнены 2026-11-12

- `:rfr-e2e:test` ×2 — BUILD SUCCESSFUL
- `:rfr-e2e:e2e` ×2 — BUILD SUCCESSFUL
- Front down + `standTimeout=10` + `LoginTest` → `Still down: rfr-gql-client`
- Скрин Environment: `hw04-allure-environment.png`
