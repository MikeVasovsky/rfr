# rfr-test-review: полный отчёт замечаний (ветка 03-hw)

Ревью владения ресурсами по skill `.cursor/skills/rfr-test-review/SKILL.md`.

**Охват:** `outcomeInvitations` + декоратор, People UI-тесты, мишени `UserExtensionTest`, `@StaticUser` / `UsersQueue` / `UsersQueueExtension`, skill `rfr-test-data`.

**Прогон:** `./gradlew :rfr-e2e:test --tests '…UserExtensionTest' --tests '…UsersQueueExtensionTest'` — зелёные.

---

## Ресурсы (владелец / получение / хранение / освобождение)

| Ресурс | Владелец | Получение | Хранение | Освобождение |
|--------|----------|-----------|----------|--------------|
| AuthAccount main | `SingleUserFixture` | `AuthAccount.create` | поле + `TearDown` | `account.delete` в `close` корня |
| Profile main (api) | создаёт первый декоратор (`ensureProfile`); чистит корень | `ApiUserSupport.ensureProfile` | строка api по username | `Profile.delete` в `TearDown` `SingleUserFixture` |
| Friendship ACCEPTED + Profile friends | `UserWithFriendsFixture` | `createRelatedUser` + `Friendship.create(…, ACCEPTED)` | `TestUser.friends` + `TearDown` | `deleteFriends` → `Profile(friend).delete` → `delegate.close` |
| Friendship PENDING income + Profile inviter | `UserWithIncomeInvitations` | `Friendship.create(inviter, base, PENDING)` | `TestUser.incomeInvitations` + `TearDown` | `deleteInvitations` → `Profile(inviter).delete` → `delegate.close` |
| Friendship PENDING outcome + Profile invitee | `UserWithOutcomeInvitationsFixture` | `Friendship.create(base, invitee, PENDING)` | `TestUser.outcomeInvitations` + `TearDown` | `deleteInvitations` → `Profile(invitee).delete` → `delegate.close` |
| Слот пула `UsersQueue` | `StaticUserFixture` | `UsersQueue.acquire` в `create()` | поле `user`; Store **до** `create()` | `release` в `close`; `UsersQueueExtension.afterEach` |
| Page / BrowserContext | `BaseUiTest` | `@BeforeEach` | поля теста | `@AfterEach` |

Store держит только корень фикстуры (`@User` или `@StaticUser`). Статика `UsersQueue` — склад свободных слотов, не владелец взятого.

---

## Замечания (все найденные)

### 1. Частичный `wrap` без TearDown → утечка mid-create

- **Где:** `data/fixture/UserWithOutcomeInvitationsFixture.java` (~28–37); тот же паттерн у `UserWithIncomeInvitations`, `UserWithFriendsFixture`
- **Сценарий:** на 2-м партнёре падает `createRelatedUser` / `Friendship.create` после успеха 1-го → объект фикстуры не собран, в Store корня нет
- **Следствие:** осиротевшие `Profile` / `Friendship`; AuthAccount main тоже может остаться (уже создан в `SingleUserFixture.create()` внутри `UserRequest.fixture()` до падения декоратора)
- **Воспроизведение:** Failing-проба `@User(outcomeInvitations = 2)` с падением на 2-м шаге + `Profile.exists()` / `Friendship.exists()`; такой мишени в `UserExtensionTest` нет
- **Фикс:** наращивать `TearDown` в цикле / `close` в `finally` при ошибке `wrap` (менять `UserRequest` не обязательно)

### 2. `TearDown.close` обрывается на первом исключении

- **Где:** `data/fixture/TearDown.java` (~32–34)
- **Сценарий:** первый `Runnable` cleanup кидает → шаги по партнёрам и `delegate.close()` не выполняются
- **Следствие:** Profile партнёров и origin (auth) могут остаться в БД; ошибки последующих шагов теряются (против skill «шаги независимы, ошибки не терялись»)
- **Воспроизведение:** после успешного create заставить первый cleanup бросить, проверить `exists()` у invitee/main
- **Фикс:** выполнять все шаги, копить исключения, в конце пробросить составное

### 3. `UserExtension` кладёт фикстуру в Store после полной сборки дерева

- **Где:** `jupiter/extension/UserExtension.java` (~53–55): `put(FIXTURE_FACTORY.apply(u))`
- **Сценарий:** падение внутри `fixture()` / `wrap` → Store пуст → `afterEach` нечего закрывать
- **Контраст:** `UsersQueueExtension` (~35–37) делает `put` **до** `create()` — соответствует skill «регистрация до частичной подготовки»
- **Воспроизведение:** та же mid-create проба для `@User`
- **Фикс:** регистрировать корень до/по ходу создания **или** гарантировать cleanup внутри `wrap` (п.1)

### 4. (мягкое) `deleteInvitations` шире «своих» строк декоратора

- **Где:** `UserWithOutcomeInvitationsFixture` / `UserWithIncomeInvitations` → `Friendships.deleteInvitations`
- **Сценарий:** при `@User(friends=1, incomeInvitations=1, outcomeInvitations=1)` close outcome снимает **все** PENDING base, включая income
- **Следствие:** при текущем порядке декораторов утечки нет (income всё равно чистит Profile inviter’ов), но это не адресное удаление только своих пар
- **Риск:** смена порядка close / композиции в `UserRequest.fixture()`

### 5. (мягкое) Владение Profile main размыто между декоратором и корнем

- **Где:** `ApiUserSupport.ensureProfile` в `UserWith*Fixture.wrap`; удаление — в `SingleUserFixture.TearDown`
- **Сценарий:** создал декоратор, удаляет корень; при mid-create до сборки TearDown декоратора (п.1) main Profile может остаться вместе с AuthAccount
- **Замечание skill:** создание явное — ок; симметрия «кто создал — тот удалил» для main Profile формально нарушена (создаёт декоратор, чистит `SingleUserFixture`)

### 6. (мягкое) Нет Failing-мишени mid-create / обрыва TearDown

- **Где:** `jupiter/extension/UserExtensionTest.java` — есть Observed outcome и negative `−1`, нет пробы «падение на 2-м invitee» и нет пробы «первый шаг TearDown кидает»
- **Следствие:** замечания 1–2 подтверждены только чтением кода, не EngineTestKit
- **Skill:** «каждый вывод подтверждён пробой… Observed и Failing» — для этих дыр Failing отсутствует

### 7. (мягкое) Путь проб в skill не совпадает с репо

- **Где:** skill `rfr-test-review` ссылается на `jupiter/probe/UserExtensionTest`; фактически — `jupiter/extension/UserExtensionTest` (+ `UsersQueueExtensionTest`)
- **Следствие:** путаница при навигации; не баг runtime

### 8. (мягкое) `Friendship.exists()` не проверяет status

- **Где:** `data/db/Friendship.java` + `sql/api/find_friendship.sql` (только `requester_id` / `addressee_id`)
- **Сценарий:** мишень «есть PENDING» фактически проверяет «есть любая пара»; при ошибочном ACCEPTED assert всё равно зелёный
- **Где проявляется:** `UserExtensionTest.OutcomeInvitationObservedCase`

### 9. (мягкое) `UsersQueue` — общее JVM-состояние без изоляции параллели

- **Где:** `data/fixture/UsersQueue.java` (static `BlockingQueue`); `UsersQueueExtensionTest` делает `reset` в `@BeforeEach`/`@AfterEach` внешнего класса
- **Сценарий:** параллельный запуск нескольких классов с `@StaticUser` / общий `reset()` опустошает чужой пул; 4-й тест при parallel execution внутри класса упрётся в timeout 5 с
- **Мишени сейчас:** только последовательные EngineTestKit-кейсы — ок; contention / parallel **не проверены**
- **Дополнительно:** `UsersQueue.reset()` без аргументов (`clear`) в `@AfterEach` внешнего теста не затрагивает вложенный EngineTestKit после его завершения, но общий static state опасен между suite’ами

### 10. (мягкое) Двойной механизм закрытия у `UsersQueueExtension`

- **Где:** `put` AutoCloseable в Store **и** ручной `afterEach` → `remove` + `close`
- **Сценарий:** сейчас `remove` в `afterEach` снимает значение до auto-close Store — double-close нет (`StaticUserFixture.close` идемпотентен при `user == null`)
- **Риск:** при отказе от ручного `afterEach` полагаться только на Store; при ошибке в `Allure.step` вокруг `close` поведение зависит от порядка callback’ов — отдельной пробы нет

### 11. (мягкое) Ключ Store без индекса параметра

- **Где:** `UserExtension` / `UsersQueueExtension` — один ключ `userFixture` / `staticUserFixture`
- **Skill:** «один @User — один параметр и один ключ Store с индексом параметра»
- **Сценарий:** два `@User`/`@StaticUser` на одном методе сейчас невозможны (цикл `break` на первом); при расширении ключ без индекса затрёт соседа
- **Сейчас:** не баг (один параметр), расхождение со skill

### 12. (косметика) Двойной пробел в имени класса outcome-фикстуры

- **Где:** `public final class  UserWithOutcomeInvitationsFixture` (два пробела после `class`)
- **Следствие:** только стиль, на владение не влияет

### 13. (слой) SQL/`exists` в jupiter-пробах

- **Где:** `UserExtensionTest.OutcomeInvitationObservedCase` вызывает `Friendship.of(…).exists()`, `Profile.of(…).exists()`
- **Skill:** «в jupiter нет SQL» — пробы исключение по самому skill («проба… exists() из db»), но формально слой jupiter трогает db-API
- **Замечание:** держать такие вызовы только в EngineTestKit-мишенях, не в UI `test/people`

### 14. People UI — утечек данных не найдено; ограничения покрытия

- Кнопки Remove / Accept / Decline / Waiting… / Cancel **не нажимаются** — побочных GraphQL-мутаций friendship нет
- Cleanup полностью на дереве `@User`
- **Не проверялось ревью как владение:** корректность локаторов/флейки UI, compact-viewport (нет ROW), гонки GraphQL при медленном api — вне scope skill ресурсов

---

## Без дефектов владения (подтверждено)

- Happy-path outcome: мишень `createsOutcomeInvitationAndCleansItAfterTest` — после теста нет связи и Profile invitee
- Negative `outcomeInvitations = -1` → `IllegalArgumentException`, `creations == 0` (до `fixture()`)
- Слот пула: 4 подряд на пуле из 3 зелёные; после failed `@StaticUser` размер очереди снова 3
- `StaticUserFixture.close` не удаляет из БД; возвращает только взятого
- `UserExtension` / `UserRequest` для пула не менялись (отдельный extension) — соответствует заданию

---

## Три списка доказательств

### Подтверждено запуском

1. `UserExtensionTest` (включая outcome Observed + negative −1) — OK  
2. `UsersQueueExtensionTest` (пул из 3 + возврат после fail) — OK  

### По чтению кода

1. Mid-create в `wrap` без промежуточного TearDown / без корня в Store (п.1)  
2. `TearDown` не продолжает шаги после ошибки (п.2)  
3. `@User`-путь: put после create (п.3); `@StaticUser`-путь — ок  
4. Широкий `deleteInvitations` (п.4) — утечки при текущем порядке нет  
5. Размытое владение main Profile (п.5)  
6. Нет Failing mid-create / TearDown (п.6)  
7. Расхождение пути probe в skill (п.7)  
8. `Friendship.exists` без status (п.8)  
9. JVM-wide очередь и риск parallel/reset (п.9)  
10. Двойной close-путь Store + afterEach (п.10)  
11. Ключ Store без индекса (п.11)  
12. Косметика имени класса (п.12)  
13. Слой jupiter↔db в пробах (п.13)  
14. People UI данными не текут (п.14)  

### Не проверено

1. Реальный mid-create fail с `exists()` в БД  
2. Падение первого шага `TearDown` с проверкой остатка строк  
3. Параллельный contention по `UsersQueue` (timeout 5 с)  
4. Два `@User`/`@StaticUser` на одном методе  
5. Поведение Store auto-close без ручного `afterEach.remove`  
6. Смена порядка декораторов friends/income/outcome + широкий `deleteInvitations`  

---

## Правило, добавленное в skill `rfr-test-review`

Внесено в `.cursor/skills/rfr-test-review/SKILL.md`:

**Слот пула — ресурс; очередь им не владеет.**  
Взятый из `UsersQueue` пользователь принадлежит `StaticUserFixture` в Store (`put` до `acquire`/`create`, `close` возвращает только взятого). Статическая очередь хранит свободные слоты и ресурсом не владеет; `reset`/seed — только в тестах extension’а, не в UI-сценариях. Проба: после упавшего `@StaticUser`-теста размер пула восстановлен (`UsersQueueExtensionTest`).

Также в skill путь проб обновлён: `jupiter/extension/UserExtensionTest` (+ `UsersQueueExtensionTest`), вместо устаревшего `jupiter/probe/…`.

## Принятые замечания → закрытие тестом / мишенью

| Принятое | Закрыто |
|----------|---------|
| Outcome: PENDING во время теста, cleanup после | `UserExtensionTest.createsOutcomeInvitationAndCleansItAfterTest` (Observed) |
| Outcome: `−1` → IAE до create | `UserExtensionTest.failsWhenOutcomeInvitationsNegativeBeforeCreatingUser` (Failing) |
| Слот пула возвращается после fail | `UsersQueueExtensionTest.returnsUserToQueueAfterFailedTest` |
| Пул из 3 хватает на 4 подряд | `UsersQueueExtensionTest.fourSequentialTestsSucceedOnPoolOfThree` |
| People: состав / чужие связи / кнопки / empty | `FriendsTabTest`, `IncomeInvitationsTabTest`, `OutcomeInvitationsTabTest` |

Замечания 1–3, 6 (mid-create / TearDown / put-after-create) **не приняты в этом PR** — остаются техдолгом без Failing-мишеней.
