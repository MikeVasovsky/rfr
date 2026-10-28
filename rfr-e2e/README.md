# rfr-e2e — запуск тестов и Allure

Перед UI-тестами нужен живой стек: MySQL → `rfr-auth` (:9001) → `rfr-api` (:8081) → frontend (`http://localhost:3001`).  
Подробный пошаговый запуск — в корневом `README.md`; ниже — Gradle-ярлыки из **корня** репозитория.

## Стенд и таски Gradle

| Задача | Команда (из корня) | Что делает |
|--------|-------------------|------------|
| MySQL | `./localenv.sh` или `docker compose up -d --wait` | БД |
| Auth + API | `./gradlew runAll --parallel` | оба бэкенда (**нужен** `--parallel`) |
| Frontend | `cd rfr-gql-client && npm i && npm run dev` | Vite :3001 |
| Браузер Playwright | `./gradlew :rfr-e2e:installPlaywright` | Chromium, один раз |
| Все e2e модуля | `./gradlew runE2e` | то же, что `:rfr-e2e:test`; suite **всегда** перезапускается |
| Стенд + e2e одной командой | `./gradlew e2eStack` | MySQL + бэк + фронт (оставить работать) + `installPlaywright` + `runE2e` |

Windows: `.\gradlew.bat` вместо `./gradlew`. Нужны Docker Desktop и Node/npm в PATH. Логи фона `e2eStack`: `build/e2eStack/`.

Один раз (браузер Playwright), из `rfr-e2e`:

```powershell
..\gradlew.bat :rfr-e2e:installPlaywright
```

Команды ниже — из каталога `rfr-e2e` или из корня репозитория (для `gradlew` путь поправьте).

## Теги (JUnit `@Tag`)

| Тег | Что входит |
|-----|------------|
| `web` | UI-тесты (`BaseUiTest` и наследники в `auth` / `profile`) |
| `fake` | Заглушка пайплайна без браузера (`SimpleTest`) |

Пакеты по домену: `test.auth`, `test.profile`, база — `test.BaseUiTest`. Новый UI-тест: наследуйте `BaseUiTest` (тег `web` уже на базе). Новый тег — сначала в эту таблицу и в skill PO.

## Рекомендуемый способ (очистка + прогон + отчёт)

Скрипт `allure-report.ps1` чистит старые `allure-results`, гоняет тесты и открывает отчёт.

```powershell
cd rfr-e2e

# вся группа UI
.\allure-report.ps1 -Tag web

# один класс
.\allure-report.ps1 -Tests io.student.rangiffler.test.profile.ProfileTest

# все тесты модуля
.\allure-report.ps1 -AllTests

# только HTML без браузера Allure
.\allure-report.ps1 -Tag web -NoServe
```

Уже прогнали тесты вручную — только отчёт:

```powershell
.\allure-report.ps1 -FromExisting
```

Только сбросить историю перед ручным прогоном в IDE:

```powershell
.\allure-report.ps1 -SkipTests
# …запуск из IDE…
.\allure-report.ps1 -FromExisting
```

## Gradle без скрипта

Из **корня** репозитория:

```powershell
# группа по тегу
.\gradlew.bat :rfr-e2e:test -DincludeTags=web

# несколько тегов
.\gradlew.bat :rfr-e2e:test "-DincludeTags=web,fake"

# исключить тег
.\gradlew.bat :rfr-e2e:test -DexcludeTags=fake

# один класс / метод
.\gradlew.bat :rfr-e2e:test --tests "io.student.rangiffler.test.profile.ProfileTest"
.\gradlew.bat :rfr-e2e:test --tests "io.student.rangiffler.test.auth.LoginTest"

# все тесты модуля (эквивалент корневой задачи runE2e)
.\gradlew.bat :rfr-e2e:test
.\gradlew.bat runE2e
```

Отчёт (results **накапливаются** — без очистки увидите старые прогоны):

```powershell
.\gradlew.bat :rfr-e2e:allureServe
.\gradlew.bat :rfr-e2e:allureReport
```

HTML: `rfr-e2e/build/reports/allure-report/`.

## Как лучше работать

| Задача | Команда |
|--------|---------|
| Поднять стек вручную | `localenv` + `runAll --parallel` + `npm run dev` (см. таблицу выше) |
| Стенд + полный e2e | `.\gradlew.bat e2eStack` (из корня) |
| Отладка одной фичи + свежий отчёт | `.\allure-report.ps1 -Tests …` или `-Tag web` |
| Полный прогон модуля | `.\gradlew.bat runE2e` / `:rfr-e2e:test` или `.\allure-report.ps1 -AllTests` |
| Прогон из IDE | `-SkipTests` → IDE → `-FromExisting` |
| CI | чистый `test` + `allureReport`, без `allureServe` |
