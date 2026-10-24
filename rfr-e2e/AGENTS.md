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
- Данные — фикстуры `@User` → `UserExtension` → `UserFixture`, без широких `DELETE`/`TRUNCATE`.
- Теги: UI — `web` (`BaseUiTest`), заглушки — `fake`. Новый тег сначала в README и в skill.
- Пакетов `api` / `assertions` нет — не заводить без явной нужды. Конфиг URL — `TestConfig` / `-Drfr.*` (см. README).
- Без комментариев к методам/полям (имена + `@Step`); над классом — краткий Javadoc. В Gradle/SQL/properties — без лишних комментариев.
