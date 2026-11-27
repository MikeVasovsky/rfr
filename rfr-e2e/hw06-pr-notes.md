# HW06 — материалы к PR

## Сообщение падения Б1

См. `hw06-b1-failure.txt` (красная проверка: invitee якобы видит Alice во входящих; откат `git revert` коммита `f7d7dae` → `e2e8b4d`).

## Скрин TraceViewer / сессия (Б3)

`hw06-b3-trace-session-step.jpg` — кадр screencast из `Playwright trace (session 1)` красной проверки Б2.

Открыть полный zip локально:

```bash
./gradlew :rfr-e2e:showTrace -Ptrace=rfr-e2e/hw06-session-trace.zip
```

## Промпт агенту по правилу 6 (rfr-test-review)

> В skill `rfr-test-review` дополни правило про владение вторым контекстом трассировкой сессии: стартует `SessionExtension` сразу после создания контекста и до входа; `Tracing` лежит в Store по ключу `trace-<index>`; останавливает и прикладывает `SessionExtension.afterTestExecution`, а не `close()` фикстуры, потому что Store закрывается после завершения теста в Allure; у зелёного теста trace не сохраняется. Одно-два предложения, стиль остальных правил.

## Ответ агента / что сделано

В `.cursor/skills/rfr-test-review/SKILL.md` правило про браузерные ресурсы заменено/дополнено: владелец основного контекста — `BrowserLifecycleExtension`/`BaseUiTest`, второго — `SessionExtension`+`SessionFixture`; трассировка сессии — start до UI-login, Store `trace-<index>`, stop/вложение в `afterTestExecution`, у зелёного без файла.

## Кто что делал

| Агент | Руками / по запросу |
|-------|---------------------|
| Б1–Б3 тесты и extensions, merge `05-hw`, DoD-фиксы (`newContext` из `test`, cleanup friendship после Accept), skill/AGENTS, красные проверки | Подтверждения вариантов, коммиты по запросу |

## DoD прогон

- `./gradlew :rfr-e2e:test` — зелёный (49)
- `./gradlew e2e` — зелёный, skipped=0
- `git grep newContext …/test` — пусто
- новых `*-trace.zip` в allure-results после зелёного нет
- Б1/Б2 ×3 — зелёные
