## Конфигурация

Настройки тестов — `TestConfig`: `-Drfr.<ключ>=…` или переменная `RFR_<КЛЮЧ>`, иначе значение по умолчанию. Подробности запуска и тегов — `README.md` в этом модуле.

## Создание Page Objects

Используй скилл `page-object-tests` (`.cursor/skills/page-object-tests/SKILL.md`).

## Архитектура модуля

Пакеты под `io.student.rangiffler` (новые классы — в существующие пакеты):

| Пакет | Назначение |
|-------|------------|
| `config` | `TestConfig` |
| `jupiter` | `@User`, `UserExtension` |
| `data` | `dao` / `db` / `fixture` |
| `model` | `TestUser`, `TestProfile` |
| `page` (+ `page.component`) | Page Objects и компоненты |
| `utils` | `LocatorOptions`, `RandomDataUtils` |
| `test` | `BaseUiTest`; домены `auth` / `profile` / `fake` |

Пакетов `api` и `assertions` пока нет — не заводить без явной нужды.

- Page Objects и компоненты — по skill `page-object-tests`.
- Без комментариев к методам и полям: смысл передают имена и `@Step`. Над каждым классом — краткий Javadoc (за что отвечает класс). В Gradle / SQL / properties — без лишних комментариев.
- Вход в тестах только через UI; токены в `localStorage` не подставлять.
- Проход сценария через Playwright MCP — не запуск теста. Успех подтверждается JUnit-прогоном через Gradle (`README.md`, `allure-report.ps1`).
- Тестовые данные создаются и удаляются фикстурами (`@User` → `UserExtension` → `UserFixture`), без широких `DELETE`/`TRUNCATE`.
- Группы тестов — JUnit `@Tag`: UI наследуют `web` с `BaseUiTest`, заглушки — `fake`. Новый тег сначала в `README.md` и в skill.
