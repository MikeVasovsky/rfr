## Summary

ДЗ 03-hw по e2e-данным и People:

- `outcomeInvitations` в `@User` / `TestUser` / `UserRequest` + декоратор `UserWithOutcomeInvitationsFixture`
- UI-тесты вкладок People + методы в `PeoplePage`
- мишени EngineTestKit для outcome (Observed + Failing −1) с `@ProbeTarget`
- пул `@StaticUser` → `UsersQueueExtension` / `StaticUserFixture` / `UsersQueue` (3 пользователя по умолчанию)
- username тестов с префиксом `rfr_` (`RandomDataUtils`)
- skill `rfr-test-data`; отчёт ревью; правило слота пула в `rfr-test-review`
- suite: exclude `probe` + `*Case`/`*Cases`; задача `:rfr-e2e:e2e` (alias `test`)

## Definition of Done

- Ни один тест, кроме `@StaticUser`, не опирается на пользователей, созданных вне него (`@User` → свои фикстуры; пул только для `@StaticUser`)
- После прогона в очереди снова все три пользователя пула (`UsersQueue.DEFAULT_POOL` / `resetToDefaultPool`)
- После прогона в `friendship` нет строк с участием тестовых (`rfr_%`) пользователей
- Мишени `@ProbeTarget` не выполняются вне `UserExtensionTest` (tag `probe` исключён из suite)
- `UserExtensionTest` зелёный без браузера (`@Tag("fake")`)

## Как проверяем

```bash
./gradlew :rfr-e2e:test
./gradlew :rfr-e2e:test
./gradlew :rfr-e2e:e2e --tests '*TabTest'
./gradlew :rfr-e2e:e2e --tests '*TabTest'
```

Красные проверки контракта (временно убрать код и убедиться, что мишени красные):

- убрать `put` до `create` в `UsersQueueExtension` → Observed/Failing outcome в `UserExtensionTest` не про это; для пула: убрать `store.put` до `create` → при fail create слот может утечь; для **UserExtension** убрать регистрацию/cleanup → Observed cleanup красный
- убрать `UsersQueue.release` / возврат в `StaticUserFixture.close()` → мишень `fourSequentialTestsSucceedOnPoolOfThree` красная

SQL после прогона (ожидание `0`):

```sql
-- rangiffler-api
SELECT COUNT(*) FROM \`rangiffler-api\`.user WHERE username LIKE 'rfr_%';
SELECT COUNT(*) FROM \`rangiffler-api\`.friendship f
  JOIN \`rangiffler-api\`.user u1 ON u1.id = f.requester_id
  JOIN \`rangiffler-api\`.user u2 ON u2.id = f.addressee_id
 WHERE u1.username LIKE 'rfr_%' OR u2.username LIKE 'rfr_%';

-- rangiffler-auth
SELECT COUNT(*) FROM \`rangiffler-auth\`.user WHERE username LIKE 'rfr_%';
```

### Вывод test-reviewer

Полный отчёт: [`rfr-e2e/rfr-test-review-notes.md`](https://github.com/MikeVasovsky/rfr/blob/03-hw/rfr-e2e/rfr-test-review-notes.md)

### Принятые замечания → закрыты тестом или мишенью

| Принятое | Закрытие |
|----------|----------|
| Outcome PENDING во время теста; cleanup после | `createsOutcomeInvitationAndCleansItAfterTest` |
| `outcomeInvitations = -1` → IAE до create | `failsWhenOutcomeInvitationsNegativeBeforeCreatingUser` |
| Слот пула после fail | `returnsUserToQueueAfterFailedTest` |
| 4 теста подряд на пуле из 3 | `fourSequentialTestsSucceedOnPoolOfThree` |
| People: состав / чужие / кнопки / empty | `*TabTest` |

### Правило в `rfr-test-review`

[`.cursor/skills/rfr-test-review/SKILL.md`](https://github.com/MikeVasovsky/rfr/blob/03-hw/.cursor/skills/rfr-test-review/SKILL.md) — **Слот пула — ресурс; очередь им не владеет.**

## Test plan

- [ ] `./gradlew :rfr-e2e:test` ×2
- [ ] `./gradlew :rfr-e2e:e2e --tests '*TabTest'` ×2
- [ ] `UserExtensionTest` без браузера
- [ ] SELECT `rfr_%` → 0 в auth/api `user` и api `friendship`
- [ ] `UsersQueue.size() == 3` после suite
