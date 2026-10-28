---
name: rfr-test-review
description: >-
  Ревью тестового кода rfr-e2e на владение ресурсами теста. Применять к diff'ам
  в jupiter/ и data/, к новым предусловиям @User и декораторам UserFixture,
  к разбору падений с подозрением на утечку данных или общее состояние между тестами.
---

# Ревью владения ресурсами rfr-e2e

Ресурс — всё, что тест получает и обязан вернуть: строки в БД (`AuthAccount`, `Profile`,
`Friendship`), `Page` / `BrowserContext` / `Browser` / `Playwright`. Для каждого ресурса
найди владельца, момент получения, место хранения и момент освобождения.

## Правила (как в коде сейчас)

1. **Владелец данных — дерево декораторов.** В `ExtensionContext.Store` лежит только
   корень `UserFixture` из `UserRequest.fixture()` (ключ `"userFixture"`).
   `UserExtension.afterEach` делает `remove` + `close()`. Каждый декоратор владеет
   `delegate` и своими companions (`friends` / income inviters). Поля extension'а,
   статика и `ThreadLocal` ресурсами не владеют.

2. **`put` в Store — после успешного `fixture()`.** Сейчас
   `UserExtension.beforeEach` вызывает `UserRequest.fixture()` целиком, и только потом
   `store.put`. Конструкторы/`create` не атомарны (`AuthAccount`, `Profile`, друзья,
   `Friendship`). Если падение на середине `fixture()`, корень в Store не попадает →
   `afterEach` не вызовет `close()` → возможна утечка уже созданных строк.
   При ревью новых декораторов явно отмечай этот риск; идеал на будущее —
   регистрировать cleanup до/по ходу частичной подготовки.

3. **Симметрия create/close через `TearDown`.** Декоратор удаляет то, что создал,
   в порядке регистрации шагов `TearDown` (обычно: `Friendships.deleteAll` → companions
   `Profile`+`AuthAccount` → опционально main `Profile`, если создали через `ensure` →
   `delegate.close()`). Шаги независимы: ошибка на одном не глотает остальные
   (`addSuppressed`). Широких `DELETE`/`TRUNCATE` нет. Связи friendship снимает
   `Friendships.deleteAll()`, не `Profile.delete()`.

4. **Новое предусловие — новый декоратор.** Композиция в `UserRequest.fixture()`,
   не флаг в `SingleUserFixture` и не ветка в `UserExtension`. Extension знает только
   `UserRequest`, `UserFixture` и `publishReportEntry` (`created` / `cleaned`).

5. **Один `@User` — один параметр `TestUser` и один ключ Store.** Связанные пользователи
   живут в `TestUser.friends` / `TestUser.incomeInvitations` и в дереве фикстур,
   не в отдельных параметрах метода. Индекса параметра в ключе Store пока нет
   (второй `@User` на том же методе не поддержан).

6. **Браузерными ресурсами владеет `BaseUiTest`.** `@BeforeEach` / `@AfterEach`
   создают и закрывают `Playwright` → `Browser` → `BrowserContext` → `Page`.
   Тесты и `UserExtension` браузер не открывают и не закрывают. Аннотации
   `@UsePlaywright` в проекте нет.

7. **Слои.** В `jupiter` нет SQL и локаторов. JDBC — в `data/db`; оркестрация
   cleanup — в `data/fixture`. В `test` нет локаторов и SQL (кроме smoke/`fake`,
   где допускаются прямые вызовы `data/db` для проверки прекондишенов).

8. **Каждый вывод подтверждён.** Опора — `test/jupiter/UserExtensionTest`
   (EngineTestKit + `TestReport`, `AuthAccount.exists()`, counts через `Friendships`)
   или конкретная строка кода. Без найденной причины изменений не предлагай.

## Формат ответа

- Для каждого ресурса: владелец, получение, хранение, освобождение.
- Для каждого замечания: файл и строка, сценарий сбоя, способ воспроизвести
  (какой кейс EngineTestKit или какой падающий шаг `create()` / конструктора).
- Три списка: «подтверждено запуском», «по чтению кода», «не проверено».
- Если дефектов нет, так и скажи и перечисли ограничения решения.
