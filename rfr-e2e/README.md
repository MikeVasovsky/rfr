# rfr-e2e — запуск тестов и Allure

Перед UI-тестами поднимите стек: MySQL → `rfr-auth` → `rfr-api` → frontend (`http://localhost:3001`).

Один раз (браузер Playwright):

```powershell
..\gradlew.bat :rfr-e2e:installPlaywright
```

Команды ниже — из каталога `rfr-e2e` или из корня репозитория (для `gradlew` путь поправьте).

## Теги (JUnit `@Tag`)

| Тег | Что входит |
|-----|------------|
| `web` | UI-тесты (`BaseUiTest` и наследники) |
| `fake` | Заглушка пайплайна без браузера (`SimpleTest`) |

Новый UI-тест: наследуйте `BaseUiTest` (тег `web` уже на базе). Новый тег — сначала в эту таблицу и в skill PO.

## Рекомендуемый способ (очистка + прогон + отчёт)

Скрипт `allure-report.ps1` чистит старые `allure-results`, гоняет тесты и открывает отчёт.

```powershell
cd rfr-e2e

# вся группа UI
.\allure-report.ps1 -Tag web

# один класс
.\allure-report.ps1 -Tests io.student.rangiffler.test.web.ProfileUiTest

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
.\gradlew.bat :rfr-e2e:test --tests "io.student.rangiffler.test.web.ProfileUiTest"
.\gradlew.bat :rfr-e2e:test --tests "io.student.rangiffler.test.web.ProfileUiTest.shouldCreateAndPersistProfile"

# все тесты модуля
.\gradlew.bat :rfr-e2e:test
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
| Отладка одной фичи + свежий отчёт | `.\allure-report.ps1 -Tests …` или `-Tag web` |
| Полный прогон модуля | `.\allure-report.ps1 -AllTests` |
| Прогон из IDE | `-SkipTests` → IDE → `-FromExisting` |
| CI | чистый `test` + `allureReport`, без `allureServe` |
