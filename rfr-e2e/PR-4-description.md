## Summary

ДЗ 03-hw по e2e-данным и People:

- `outcomeInvitations` в `@User` / `TestUser` / `UserRequest` + декоратор `UserWithOutcomeInvitationsFixture`
- UI-тесты вкладок People + методы в `PeoplePage`
- мишени EngineTestKit для outcome (Observed + Failing −1)
- пул `@StaticUser` → `UsersQueueExtension` / `StaticUserFixture` / `UsersQueue`
- skill `rfr-test-data`; отчёт ревью; правило слота пула в `rfr-test-review`
- CI: exclude `*Case`/`*Cases` из suite (фикстуры только через EngineTestKit)

## Definition of Done (задание)

### Вывод test-reviewer

Полный отчёт: [`rfr-e2e/rfr-test-review-notes.md`](https://github.com/MikeVasovsky/rfr/blob/03-hw/rfr-e2e/rfr-test-review-notes.md)

Кратко: слот `@StaticUser` и happy-path outcome по владению ок; дыры mid-create/`TearDown`/`UserExtension` put-after-create зафиксированы как техдолг (не приняты в этом PR).

### Принятые замечания → закрыты тестом или мишенью

| Принятое замечание / критерий | Закрытие |
|-------------------------------|----------|
| Outcome PENDING есть во время теста; после теста нет связи и Profile invitee | `UserExtensionTest.createsOutcomeInvitationAndCleansItAfterTest` |
| `outcomeInvitations = -1` → `IllegalArgumentException` до создания пользователя | `UserExtensionTest.failsWhenOutcomeInvitationsNegativeBeforeCreatingUser` |
| Слот пула возвращается после упавшего теста | `UsersQueueExtensionTest.returnsUserToQueueAfterFailedTest` |
| Четыре теста подряд на пуле из трёх — зелёные | `UsersQueueExtensionTest.fourSequentialTestsSucceedOnPoolOfThree` |
| People: состав, отсутствие чужих связей, кнопки по наличию, пустое состояние | `FriendsTabTest`, `IncomeInvitationsTabTest`, `OutcomeInvitationsTabTest` |

### Правило, добавленное в `rfr-test-review`

Файл: [`.cursor/skills/rfr-test-review/SKILL.md`](https://github.com/MikeVasovsky/rfr/blob/03-hw/.cursor/skills/rfr-test-review/SKILL.md)

> **Слот пула — ресурс; очередь им не владеет.** Взятый из `UsersQueue` пользователь принадлежит `StaticUserFixture` в Store (`put` до `acquire`/`create`, `close` возвращает только взятого). Статическая очередь хранит свободные слоты и ресурсом не владеет; `reset`/seed — только в тестах extension’а, не в UI-сценариях. Проба: после упавшего `@StaticUser`-теста размер пула восстановлен (`UsersQueueExtensionTest`).

## Test plan

- [ ] `./gradlew :rfr-e2e:test --tests 'io.student.rangiffler.jupiter.extension.UserExtensionTest'`
- [ ] `./gradlew :rfr-e2e:test --tests 'io.student.rangiffler.jupiter.extension.UsersQueueExtensionTest'`
- [ ] Стек up: `./gradlew :rfr-e2e:test --tests 'io.student.rangiffler.test.people.*'`
- [ ] CI e2e зелёный (suite без прямого прогона `*Case`/`*Cases`)
