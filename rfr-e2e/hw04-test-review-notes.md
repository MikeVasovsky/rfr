# HW04 — вывод test-reviewer

Агент: `.cursor/agents/test-reviewer.md` + skill `.cursor/skills/rfr-test-review/SKILL.md`.

## 1. Проба правила «SuiteExtension — только ServiceLoader»

Временная правка (не в финальном дереве): на `FrontReadyAfterDelayCase` повесили `@ExtendWith(StandReadinessExtension.class)` при уже существующей записи в `META-INF/services/...Extension`.

### Замечание агента (принято)

- **Файл / строка:** `FrontReadyAfterDelayCase.java` — `@ExtendWith(StandReadinessExtension.class)`.
- **Правило:** реализации `SuiteExtension` — только ServiceLoader; `@ExtendWith` / meta / `@RegisterExtension` на тестовом классе — ошибка ревью (ломает единый suite-порядок SPI).
- **Сценарий:** class-level `@ExtendWith` даёт второй путь регистрации рядом с SPI; порядок относительно `AllureEnvironmentExtension` (хук `afterSuite` до падения guards) больше не гарантируется одним файлом services.
- **Закрытие:** `@ExtendWith` снят; регистрация только ServiceLoader. Соседние мишени (`FrontNeverReadyCase`, `NoUiTagCase`, `StandCheckDisabledCase`, `AllureEnvironmentCase`) изначально без `@ExtendWith`.

## 2. Ревью финального diff HW04 (suite guards)

**Дефектов по skill нет.**

### Ресурсы (кратко)

| Ресурс | Владелец | Получение | Хранение | Освобождение |
|--------|----------|-----------|----------|--------------|
| `afterSuite`-хук | root Store | `SuiteExtension.beforeAll` до `beforeSuite` | `"afterSuiteHook"` | закрытие root Store |
| Ожидание стенда | `StandReadinessExtension` → `StandReadiness` | `beforeSuite` при `ui` + `standCheck` | latch `"beforeSuite"` | эпhemeral probes; без ui — no-op |
| Flyway | `StandSchemaExtension` | `beforeSuite` | latch | fail-fast |
| `environment.properties` | `AllureEnvironmentExtension` | `afterSuite` | Allure results dir | артефакт отчёта |
| HTTP-заглушка в EngineTestKit | оркестратор / `FrontStub` | `startFrontStub` | локально в тесте | `close()` → `server.stop` |

### Подтверждено запуском

См. прогоны DoD в PR (Gradle).

### По чтению кода

- SPI: Allure → StandReadiness → StandSchema; autodetection включён.
- Нет `@ExtendWith` на suite-impl в финальном дереве.
- No-op readiness без `ui` / при `standCheck=false`.
- `waitForStand` в Gradle e2e нет; exclude `probe` + `*Case`/`*Cases`.

### Не проверено ревьюером в Ask mode

- Полный `git diff` vs merge-base (Shell был недоступен агенту).
- Отдельная EngineTestKit-мишень «Allure пишет environment при падении Schema» (контракт закрыт порядком SPI + ручной красной проверкой DoD).

## Принятые замечания → закрытие

| Принятое | Закрытие |
|----------|----------|
| `@ExtendWith(StandReadinessExtension)` на probe | Снят; только SPI |
| Ready after delay / timeout / no ui / standCheck=false | `StandReadinessExtensionTest` + probe/stand/* |
| `environment.properties` со всеми ключами | `AllureEnvironmentExtensionTest` |
| Оркестраторы с `System.setProperty(rfr.*)` флейкают рядом с UI (`StandReadiness` читает stub URL) | `@Isolated` на `StandReadinessExtensionTest` / `AllureEnvironmentExtensionTest` |
| Java `HttpClient` (HTTP/2) зависает на Vite → stand never ready / CloseWait | `StandReadiness` HTTP client: `.version(HTTP_1_1)` |
