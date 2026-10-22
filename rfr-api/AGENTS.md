## Запуск

```bash
cd rfr-api && ../gradlew bootRun
# оба бэкенда: из корня ./gradlew runAll --parallel
```

Main: `io.student.rangiffler.RangifflerApiApplication` (порт **8081**, `/graphql`, `/graphiql`). Нужны MySQL и живой issuer `rfr-auth` (`http://localhost:9001`). Подробности схемы и mock — `README.md` в этом модуле.

## Как работает сервис

**Пользователь (lazy create):**
- Auth регистрирует только учётку; в `rangiffler-api.user` записи ещё нет.
- Первый `Query.user` (JWT `sub` = username) → `UserServiceImpl.createNewUserIfNotPresent`: find by username или insert с `country` = `ru` (seed Flyway). UUID api ≠ UUID auth.
- `Mutation.user` обновляет профиль уже существующего api-user (частично: firstname / surname / avatar / location); без строки в api → not found.

**Дружба (одна строка на пару):**
- Таблица `friendship`: `requester` + `addressee`, статус `PENDING` | `ACCEPTED`, unique пара (`user_low_id`/`user_high_id`), `@Version`.
- `Mutation.friendship`: `ADD` → создать `PENDING` (ответ `INVITATION_SENT`); `ACCEPT` — только addressee, `PENDING`→`ACCEPTED` (`FRIEND`); `REJECT` — addressee удаляет pending (`NOT_FRIEND`); `DELETE` — любой участник удаляет пару/заявку (`NOT_FRIEND`).
- GraphQL `FriendStatus` относительно текущего: `FRIEND` / `INVITATION_SENT` / `INVITATION_RECEIVED` / `NOT_FRIEND`.
- Списки: `users` (все кроме me + статус), nested `friends` / `incomeInvitations` / `outcomeInvitations` на `User`.

**Countries / mock:** справочник стран — БД. Feed / photos / likes — mock JSON (`resources/mock/`), в БД не пишутся; таблицы `photo`/`like`/`statistic` пока не используются кодом.

## Архитектура модуля

Пакеты под `io.student.rangiffler` (новые классы — в существующие пакеты):

| Пакет | Назначение |
|-------|------------|
| `config` | Resource Server (JWT), CORS, GraphQL exception resolver |
| `controller` | GraphQL query/mutation controllers (real + mock) |
| `data` (+ `entity` / `repository` / `projection`) | JPA-слой users / countries / friendship |
| `exception` | Доменные исключения → GraphQL errors |
| `service` (+ `api` / `impl` / `mock` / `cors`) | Бизнес-логика; mock-лента из JSON |
| `util` | Пагинация/сортировка GraphQL, байты↔строка |

Моки следует оставлять до команды замены их на реальную реализацию

Сгенерированные типы: `io.student.rangiffler.model.types` (DGS Codegen) — **не править руками**. Источник: `src/main/resources/graphql/query.graphqls` → `./gradlew :rfr-api:generateJava`.

- **Real (БД):** users, friendship, countries. **Mock (JSON):** feed / photos / likes — `resources/mock/`, mutations не персистятся.
- Таблицы `photo` / `like` / `statistic` в Flyway есть, в коде пока не используются.
- Контракт с фронтом = GraphQL-схема; при смене API сверять `rfr-gql-client`.
- Security: только **access** Bearer; `@PreAuthorize` — `read` на query, `write` на mutation.
- Схема БД — только Flyway (`ddl-auto: none`). Spring Boot 4 / Jackson 3: `tools.jackson.databind.*`.
