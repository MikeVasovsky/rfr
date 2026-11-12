---
name: rfr-test-review
description: >-
  Ревью тестового кода rfr-e2e на владение ресурсами теста. Применять при diff'ах
  в jupiter/ и data/, новых предусловиях @User и декораторах UserFixture, разборе
  падений с подозрением на утечку данных или общее состояние.
---

# rfr-test-review

Ресурс — всё, что тест получает и обязан вернуть (строки в БД, Page, BrowserContext); для каждого ресурса найти владельца, момент получения, место хранения, момент освобождения.

**Владелец — дерево декораторов.** В Store лежит только корень UserFixture из UserRequest.fixture(), Store закрывает его как AutoCloseable; каждый декоратор владеет своим origin и partners. Поля extension'а, статика и ThreadLocal ресурсом не владеют.

**Слот пула — ресурс; очередь им не владеет.** Взятый из `UsersQueue` пользователь принадлежит `StaticUserFixture` в Store (`put` до `acquire`/`create`, `close` возвращает только взятого). Статическая очередь хранит свободные слоты и ресурсом не владеет; `reset`/seed — только в тестах extension’а, не в UI-сценариях. Проба: после упавшего `@StaticUser`-теста размер пула восстановлен (`UsersQueueExtensionTest`).

**Регистрация до частичной подготовки.** put в Store до create(); create() не атомарен (Account, Profile, партнёры, Friendship), поэтому close() обязан работать после падения на любом шаге.

**Симметрия create/close.** Декоратор удаляет ровно то, что создал, адресно по username, в обратном порядке (свои строки → partners → origin) через Teardown, чтобы шаги были независимы и ошибки не терялись. Без широких DELETE/TRUNCATE.

**Новое предусловие — новый декоратор.** Композиция в UserRequest.fixture(), а не флаг в BaseUserFixture и не ветка в UserExtension; extension знает только UserRequest и UserFixture.

**Один @User — один параметр TestUser и один ключ Store с индексом параметра.** Связанные пользователи — внутри TestUser и дерева фикстур, не отдельными параметрами.

**Браузерными ресурсами владеет @UsePlaywright.** Page и BrowserContext не создаются и не закрываются в тестах и extension'ах.

**Слои.** В jupiter нет SQL и локаторов; в data нет JUnit, SQL только в data/db, db не зависит от data; в test нет локаторов и SQL.

**Декларативная регистрация class-level extension.** Extension с областью класса (`BeforeAll`/`AfterAll` и всё, что должно жить на классном контексте) через `@ExtendWith` / meta-annotation с `@ExtendWith` вешается **только** на сам тестовый класс (`TYPE`) или на **статическое** поле класса. Любое другое место объявления такого extension — **ошибка** ревью: instance-поле, метод, параметр метода/конструктора/lifecycle, meta на этих элементах. Пример ошибки: `@Test void t(@ExtendWith(ClassScopedExtension.class) …)` или `@ExtendWith(ClassScopedExtension.class)` на методе. Method/parameter-level `@ExtendWith` допустим **только** если extension по смыслу method/parameter-scoped (не class-level).

**Без программной регистрации extension.** Предпочтение — `@ExtendWith` / meta-annotation с `@ExtendWith`; глобально — `junit-platform.properties` (в т.ч. `junit.jupiter.extensions.autodetection.enabled` для регистрации через ServiceLoader). `@RegisterExtension`, ручной `registerExtension` / API launcher ради «просто повесить» extension — **ошибка** ревью, если экземпляр **не** настраивается через конструктор или builder (аргументы, флаги, зависимости). Допустимо `@RegisterExtension` только когда без сконструированного экземпляра конфигурацию не выразить декларативно. Ошибка ревью: `@RegisterExtension static final X x = new X()` при наличии no-arg и отсутствии настройки — заменить на `@ExtendWith(X.class)` или на запись в `junit-platform.properties`, если нужен глобальный scope.

**Extension безопасен для любого тестового метода.** Нельзя опираться на то, что extension зарегистрирован только над «своими» тестами с нужной аннотацией/параметром. Callback'и (`beforeEach` / `afterEach` / resolver и т.д.) обязаны быть no-op или иначе безопасны, когда метод без `@User` / `@StaticUser` / нужного параметра: без NPE, без лишних create/delete, без порчи Store и без предположения «раз меня вызвали — значит ресурс нужен». Код должен выдерживать регистрацию на тестовом **классе** и **глобально** через `junit-platform.properties` (включая autodetection ServiceLoader), когда Jupiter вызывает extension и для чужих методов suite. Ошибка ревью: ранний обязательный side-effect без проверки аннотации/параметра; жёсткая привязка к одному probe/UI-классу; падение на методе без предусловия.

**Реализации `SuiteExtension` — только ServiceLoader.** `StandReadinessExtension`, `StandSchemaExtension`, `AllureEnvironmentExtension` и другие impl `SuiteExtension` регистрируются через `META-INF/services/org.junit.jupiter.api.extension.Extension` (при `junit.jupiter.extensions.autodetection.enabled=true`). Подключение через `@ExtendWith` / meta с `@ExtendWith` / `@RegisterExtension` на тестовом классе — **ошибка** ревью (ломает единый suite-порядок и обходит SPI). Проба: временный `@ExtendWith(StandReadinessExtension.class)` должен дать это замечание.

**Каждый вывод подтверждён** пробой в `jupiter/probe/user|pool/*` через оркестраторы `test/extension/user|pool/*` (EngineTestKit, exists() из db, цели Observed и Failing) или конкретной строкой кода; без найденной причины изменений не предлагать.

## Формат ответа

Для каждого ресурса — владелец, получение, хранение, освобождение; для каждого замечания — файл и строка, сценарий сбоя, как воспроизвести (какая проба или какой падающий шаг create()); три списка — «подтверждено запуском», «по чтению кода», «не проверено». Если дефектов нет — так и сказать и перечислить ограничения решения.
