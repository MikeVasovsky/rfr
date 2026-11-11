# HW04 task 2 — наблюдения `LauncherSessionListener`

Эксперимент: сверка Flyway в `StandSchemaLauncherSessionListener.launcherSessionOpened`, `StandSchemaExtension` убран из ServiceLoader `Extension`, `junit-platform-launcher` → `testImplementation`, `maxParallelForks = 2`.

Для прогонов без MySQL использовали `-Drfr.schemaCheck=false` (только счётчик/лог); для вида падения — дефолт `true` при остановленной MySQL.

## Срабатывает ли слушатель в `./gradlew :rfr-e2e:test` (без UI)

**Да.** Достаточно `--tests '*DemoExtensionTest'` (`@Tag("fake")`, UI нет).

Как отличить:
- в логе Gradle: `StandSchemaLauncherSessionListener.opened #1 ... pid=...`;
- при живой сверке и мёртвой MySQL прогон **не доходит до тестовых методов**: падает старт Test Executor.

## Сколько раз при `maxParallelForks = 2`

Два класса в одном `test`-таске (`DemoExtensionTest` + `SuiteExtensionTest`, `schemaCheck=false`):

```
StandSchemaLauncherSessionListener.opened #1 ... pid=21976
StandSchemaLauncherSessionListener.opened #1 ... pid=6796
```

**Два раза** — по одной `LauncherSession` на worker JVM (у каждого fork свой static-счётчик → оба печатают `#1`, но разные `pid`).

Это не Jupiter `parallelism=3` из `junit-platform.properties`.

## Срабатывает ли внутри `EngineTestKit` (`SuiteExtensionTest`)

**Нет.** `SuiteExtensionTest` запоминает `OPENED` до `EngineTestKit.engine(...).execute()` и после: значение то же. Снаружи Gradle-сессия даёт ровно один `opened #1`.

`EngineTestKit` гоняет engine напрямую, без новой Platform `LauncherSession` → SPI-слушатель не вызывается повторно.

## Как Gradle / IDE показывают исключение из `launcherSessionOpened`

**Gradle** (MySQL down, `schemaCheck=true`):

- `Test process encountered an unexpected problem` / `Could not start Gradle Test Executor`;
- cause: `IllegalStateException` из `FlywayAlignment` (`Connection refused`);
- тестовые методы часто **не стартуют**; в HTML-отчёте это выглядит как падение executor/suite setup, не как красный `@Test`.

**IDE:** аналогично — ошибка на уровне запуска сессии/процесса тестов до дерева методов; стек указывает на `launcherSessionOpened` → `FlywayAlignment`, а не на assertion в тесте.

## Вывод эксперимента

`LauncherSessionListener` цепляется раньше Jupiter и видит любой Gradle/`test` worker, но не знает `Store`/теги и не срабатывает в `EngineTestKit`. Падение «ломает executor», а не даёт CTA на уровне test-case. Для e2e удобнее `StandSchemaExtension` (Jupiter + ServiceLoader): один раз через root `Store`, виден при запуске класса из IDE, понятнее в отчёте.
