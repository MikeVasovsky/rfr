---
name: rfr-test-data
description: >-
  Правила тестовых данных rfr-e2e (фикстуры @User, декораторы UserFixture, data/db).
  Применять при новых/правках предусловий, фикстур, JDBC-объектов строк и генерации пользователей.
---

# rfr-test-data v1

**Уникальность данных.** Username с префиксом `rfr_` + рандом (`RandomDataUtils`); не переиспользовать фиксированные логины между тестами (кроме пула `@StaticUser`).

**Владелец — дерево декораторов.** Ресурсы (auth/api-строки, партнёры, Friendship) принадлежат дереву `UserFixture` из `data/fixture/user/UserRequest.fixture()`; Store держит только корень и закрывает его. Каждый декоратор владеет своим origin и partners и чистит их в `TearDown` (`data/fixture`).

**Новое предусловие — новый декоратор** в `data/fixture/user/`, композиция в `UserRequest.fixture()`, без флагов в базовой фикстуре и без веток в `extension/user/UserExtension`.

**Пул отдельно от `@User`:** `data/fixture/pool` + `annotation/pool` + `extension/pool` (пакет `pool`, не `static`).

**SQL только в объектах строк `data/db`.** JDBC/SQL — в `Profile` / `Friendship` / `AuthAccount` и т.п.; не в `jupiter/`, не в `test/`, не в декораторах напрямую.

**Профиль API создаётся явно.** Api-профиль не «всплывает» сам: `ensureProfile` / `Profile.create` в декораторе или support, когда связь в api реально нужна.

**Пул заранее заведённых пользователей — только там, где создать нельзя.** Иначе всегда создавать и удалять через фикстуры; seed/пул — исключение для несоздаваемых сущностей.
