# HW04 task 1 — наблюдения `@BeforeSuite` / `RangifflerE2eSuite`

Эксперимент: сверка Flyway в `@BeforeSuite`, `StandSchemaExtension` убран из ServiceLoader, `e2e` гоняет suite-класс.

## Сколько раз тесты при `./gradlew e2e`, если не ограничивать движки

Без `includeEngines('junit-platform-suite')` и без `includeTestsMatching '*RangifflerE2eSuite'` Platform поднимает и **Jupiter**, и **Suite**:

- Jupiter находит все `*Test` → прогон №1;
- Suite находит `RangifflerE2eSuite` и снова запускает выбранные тесты → прогон №2.

Итог: **тесты выполняются дважды**. В рабочей конфигурации эксперимента `e2e` ограничен suite-engine + фильтром на `RangifflerE2eSuite` → **один** прогон через suite.

## `@BeforeSuite` при запуске одного класса / метода из IDE и `e2e --tests '*TabTest'`

| Запуск | `@BeforeSuite` |
|--------|----------------|
| IDE: один `*Test` класс / метод | **Нет** — suite-класс не участвует, сверка не вызывается |
| `./gradlew :rfr-e2e:e2e --tests '*TabTest'` | При **нашем** `includeTestsMatching '*RangifflerE2eSuite'` suite всё равно стартует (BeforeSuite был), `--tests` не подменяет entrypoint. Если убрать include suite и гонять Jupiter-фильтром `*TabTest`, suite/`@BeforeSuite` **не** участвуют |
| `./gradlew :rfr-e2e:test --tests '*DemoExtensionTest'` | **Нет** — Jupiter only, ServiceLoader пуст; тест зелёный даже без MySQL |
| `./gradlew :rfr-e2e:e2e` (suite only) | **Да**, один раз до дочерних тестов; без MySQL — `Rangiffler e2e > initializationError` + CTA/`ConnectException` |

## Падение `@BeforeSuite` в Gradle и Allure

- **Gradle:** suite падает на lifecycle до тестов; в консоли `IllegalStateException` из `FlywayAlignment` (CTA про рестарт стека); дочерние UI-тесты часто **не стартуют**.
- **Allure:** отдельного «красивого» test-case на `@BeforeSuite` обычно нет или suite выглядит как контейнер с ошибкой setup; в `environment` / дереве мало полезного по сравнению с падением Jupiter `BeforeAll` у extension.

## Вывод эксперимента

`@BeforeSuite` удобен только когда всегда гоняют **suite-класс**. IDE single-test и точечный `--tests` обходят guard — для локального e2e это слабо. Поэтому в проекте после revert снова `StandSchemaExtension` через ServiceLoader.
